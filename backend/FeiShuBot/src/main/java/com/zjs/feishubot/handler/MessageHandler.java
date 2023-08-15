package com.zjs.feishubot.handler;

import cn.hutool.json.JSONUtil;
import com.lark.oapi.Client;
import com.lark.oapi.service.contact.v3.model.User;
import com.lark.oapi.service.im.v1.model.*;
import com.zjs.feishubot.entity.*;
import com.zjs.feishubot.entity.gpt.Answer;
import com.zjs.feishubot.entity.gpt.ErrorCode;
import com.zjs.feishubot.entity.gpt.Models;
import com.zjs.feishubot.service.AccountService;
import com.zjs.feishubot.service.MessageService;
import com.zjs.feishubot.service.RecordService;
import com.zjs.feishubot.service.UserService;
import com.zjs.feishubot.util.chatgpt.ChatService;
import com.zjs.feishubot.util.chatgpt.ConversationPool;
import com.zjs.feishubot.util.chatgpt.RequestIdSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class MessageHandler {

  protected final Client client;
  protected final UserService userService;
  protected final MessageService messageService;
  protected final ConversationPool conversationPool;
  protected final AccountService accountService;
  protected final RecordService recordService;
  protected final ChatService chatService;

  protected final ExecutorService executorService;

  /**
   * 飞书推送事件会重复，用于去重
   *
   * @param event
   * @return
   */
  private boolean checkInvalidEvent(P2MessageReceiveV1 event) {
    String requestId = event.getRequestId();
    // 根据内存记录的消息事件id去重
    if (RequestIdSet.requestIdSet.contains(requestId)) {
      log.debug("重复请求，requestId:{}", requestId);
      return false;
    }
    RequestIdSet.requestIdSet.add(requestId);
    String createTime = event.getEvent().getMessage().getCreateTime();
    Long createTimeLong = Long.valueOf(createTime);
    Long now = System.currentTimeMillis();
    if (now - createTimeLong > 1000 * 20) {
      // 根据消息事件的创建时间去重
      log.debug("消息过期，requestId:{}", requestId);
      return false;
    }

    P2MessageReceiveV1Data messageEvent = event.getEvent();
    EventMessage message = messageEvent.getMessage();

    String chatType = message.getChatType();
    String msgType = message.getMessageType();

    log.info("chatType:{},msgType:{}", chatType, msgType);


    // 只处理私聊文本消息
    if (!chatType.equals("p2p") && !msgType.equals("text")) {
      log.debug("不支持的ChatType或MsgType,ChatGpt不处理");
      return false;
    }

    return true;
  }


  public void process(P2MessageReceiveV1 event, String messageId) throws Exception {
    log.debug("{} process the request event id",event.getRequestId());
    P2MessageReceiveV1Data messageEvent = event.getEvent();
    EventMessage message = messageEvent.getMessage();

    if (messageId.equals("0")) {
      log.debug("retry request");
    }

    if (!checkInvalidEvent(event)) {
      return;
    }
    JSONObject jsonObject = new JSONObject(message.getContent());
    String text = jsonObject.optString("text");
    User user;

    try {
      user = userService.getUserByOpenId(event.getEvent().getSender().getSenderId().getOpenId());
      String name = user.getName();
      log.info("{}:{}", name, text);
    } catch (Exception e) {
      log.error("获取用户信息失败", e);
      return;
    }

    String openId = user.getOpenId();

    if (text == null || text.isEmpty()) {
      return;
    }
    String chatId = message.getChatId();
    //尝试从会话池中获取会话，从而保持上下文
    UserConversationConfig conversation = conversationPool.getConversation(chatId);

    boolean newChat = false;
    String model;
    Account account;
    Mode mode;

    if (conversation == null) {
      model = Models.DEFAULT_MODEL;
      //如果没有历史会话，新建会话，采用默认3.5的模型
      newChat = true;
      conversation = new UserConversationConfig();
      mode = Mode.FAST;

      long start = System.currentTimeMillis();
      account = accountService.getFreeAccountByModelAndCheck(model, openId);
      long end = System.currentTimeMillis();
      log.info("获取空闲账号耗时：{}ms", end - start);

      if (account != null) {
        conversation.setPlus(account.isPlusAccount());
        conversation.setChatId(chatId);
        conversation.setModel(model);
        //默认使用fast模式
        conversation.setMode(mode);

        if (text.equals("/fast")) {
          conversation.setMode(Mode.FAST);
        } else if (text.equals("/keep")) {
          mode = Mode.KEEP;
          conversation.setMode(Mode.KEEP);
        }

        conversationPool.addConversation(chatId, conversation);
      }

    } else {

      if (text.equals("/fast")) {
        conversation.setMode(Mode.FAST);
        return;
      } else if (text.equals("/keep")) {
        conversation.setMode(Mode.KEEP);
        return;
      }

      mode = conversation.getMode();

      //如果有会话，则需要判断是要建新会话意图还是以前老会话
      if (conversation.getConversationId() == null || conversation.getConversationId().isEmpty()) {
        //通过下拉选择框 建立新会话意图
        newChat = true;
        model = conversation.getModel();
        account = accountService.getFreeAccountByModelAndCheck(model, openId);
      } else {
        //已经有会话记录，继续会话
        Account oldAccount = accountService.getAccount(conversation.getAccount());
        boolean ok = true;

        if (oldAccount == null) {
          log.warn("会话服务的gpt账号{}已经被删除", conversation.getAccount());
          messageService.sendTextMessageByChatId(chatId, "会话服务的gpt账号已经被删除");
          ok = false;
        } else {
          if (oldAccount.isPlusAccount() != conversation.isPlus()) {
            log.warn("会话服务的gpt账号{}的plus状态已经发生变化", conversation.getAccount());
            messageService.sendTextMessageByChatId(chatId, "会话服务的gpt账号的plus状态已经发生变化");
            ok = false;
          }
          if (!oldAccount.isAvailable()) {
            log.warn("会话服务的gpt账号{}已经不可用", conversation.getAccount());
            messageService.sendTextMessageByChatId(chatId, "会话服务的gpt账号已经不可用");
            ok = false;
          }

          boolean havePermission = accountService.checkAccountPermission(openId, oldAccount, conversation.isPlus());
          if (!havePermission) {
            log.warn("会话服务的gpt账号{}未授权使用", conversation.getAccount());
            messageService.sendTextMessageByChatId(chatId, "会话服务的gpt账号未授权使用");
            ok = false;
          }
        }

        if (ok) {
          //还能使用会话中的模型和账号
          model = conversation.getModel();
          account = accountService.getAccount(conversation.getAccount());

        } else {
          model = Models.DEFAULT_MODEL;
          //无法使用了，新建会话
          log.warn("会话服务的gpt账号{}无法使用，新建会话", conversation.getAccount());
          messageService.sendTextMessageByChatId(chatId, "上次服务的gpt账号无法使用，新建会话");
          newChat = true;
          account = accountService.getFreeAccountByModelAndCheck(model, openId);
        }
      }
    }

    if (account == null) {
      if (accountService.getAllAccountNoCheck().isEmpty()) {
        log.debug("服务器未配置账号");
        messageService.sendTextMessageByChatId(chatId, "服务器未配置账户");
        return;
      }

      //keep模式，保证上下文，只能等待
      if (conversation.getMode() == Mode.KEEP) {
        log.debug("无空闲模型");
        messageService.sendTextMessageByChatId(chatId, "目前无空闲该模型，请稍后再试，或者更换模型");
        return;
      }


      //如果是fast模式，则需要切换账号服务
      if (conversation.getMode() == Mode.FAST) {
        log.debug("fast 模式，尝试切换账号");
          newChat = true;
          messageService.sendTextMessageByChatId(chatId, "正在查找空闲账号...");
          log.debug("正在查找空闲账号...");
          int retry = 0;
          while (retry < 4 && (account == null || account.isRunning())) {
            log.debug("开始第{}次尝试查找可用空闲账号", retry + 1);
            account = accountService.getFreeAccountByModelAndCheck(model, openId);
            retry++;
          }
      }
    }




    if (account == null) {
      log.debug("目前无空闲账号");
      messageService.sendTextMessageByChatId(chatId, "目前无空闲该模型，请稍后再试，或者更换模型");
      return;
    }

    if (conversation.getMode() == Mode.FAST) {
      //不是新会话下，有可能上次服务的账号不是 plus 账号，要切换到 plus 账号
      //fast模式优先使用 plus 账号服务，因为 plus 的响应会快
      if (!account.isPlusAccount() && !newChat) {
        //List<Account> allAccountNoCheck = accountService.getAllAccountNoCheck();
        Account newAccount = accountService.getFreeAccountByModelAndCheck(model, openId);
        if (newAccount.isPlusAccount()) {
          log.info("发现有可用 plus 账号，切换到 plus 账号服务");
          account = newAccount;
          newChat = true;
        }
      }
    }


    String firstText = "正在生成中，请稍后...";

//    if (accountService.isBusy(account.getAccount())) {
//      //keep模式只能等待
//      if (conversation.getMode() == Mode.KEEP) {
//        firstText = "目前该账号正在运行，请稍等...";
//      } else {
    //fast模式，切换账号，创建新会话
//        newChat = true;
//        account = accountService.getFreeAccountByModel(model,userId);
//        if (accountService.isBusy(account.getAccount())) {
//          firstText = "目前该账号正在运行，请稍等...";
//        } else {
//          conversation.setAccount(account.getAccount());
//          //fast模式下，切换账号和原账号模型保持一致
//          //如果原账号是plus模型，则切换到的是normal账号，则用normal的模型
//          if (chatService.getLevel() == 4) {
//            if (Models.plusModelTitle.contains(conversation.getModel())) {
//              conversation.setModel(conversation.getModel());
//            } else {
//              conversation.setModel(Models.PLUS_DEFAULT_MODEL);
//            }
//          } else {
//            conversation.setModel(Models.NORMAL_DEFAULT_MODEL);
//          }
//        }
//      }
//    }
    conversation.setAccount(account.getAccount());
    accountService.addBusyAccount(account.getAccount());

    boolean isPlusModel = Models.plusModelTitle.contains(model);

    log.debug("save record");
    Record record = recordService.saveRecord(user, account, model, mode, text, null, Status.RUNNING, null, isPlusModel);


    String title = model + " : " + account.getAccount();


    if (account.isPlusAccount()) {
      title += "  [plus] ";
    }

    if (conversation.getMode() == Mode.KEEP) {
      title += "「" + mode + "」";
    }


    if (!messageId.equals("0")) {
      messageService.modifyGptAnswerMessageCard(messageId, title, firstText);
    } else {
      CreateMessageResp resp = messageService.sendGptAnswerMessage(chatId, title, firstText);
      messageId = resp.getData().getMessageId();
    }


    Map<String, String> selections = createSelection(conversation, openId);

    String modelSlug = Models.modelMap.get(conversation.getModel()).getSlug();


    conversation.setPlus(account.isPlusAccount());
    conversation.setAccount(account.getAccount());
    conversation.setModel(model);
    conversation.setTitle(title);
    conversation.setMode(mode);
    conversationPool.addConversation(chatId, conversation);


    final String finalTitle = title;
    Account finalAccount = account;
    String finalMessageId = messageId;
    UserConversationConfig finalConversation = conversation;
    if (newChat) {
      log.info("新建会话");
      conversation.setTitle(title);
      chatService.newChat(text, modelSlug, answer -> {
        processAnswer(answer, finalTitle, chatId, finalAccount, finalMessageId, event, selections, record, finalConversation);
      }, account);
    } else {
      log.info("继续会话");
      chatService.keepChat(text, modelSlug, conversation.getParentMessageId(), conversation.getConversationId(), answer -> {
        processAnswer(answer, finalTitle, chatId, finalAccount, finalMessageId, event, selections, record, finalConversation);
      }, account);
    }
  }

  private Map<String, String> createSelection(UserConversationConfig conversation, String userId) {
    //将curConversation转换成json格式
    String conversationStr = JSONUtil.toJsonStr(conversation);
    Map<String, String> selections = new HashMap<>();
    selections.put("使用当前上下文", conversationStr);

    for (String s : Models.normalModelTitle) {
      UserConversationConfig normalModelConversation = new UserConversationConfig();
      normalModelConversation.setModel(s);
      selections.put(s, JSONUtil.toJsonStr(normalModelConversation));
    }

    if (accountService.getPlusAccountList(userId).isEmpty()) {
      return selections;
    }

    for (String modelTitle : Models.plusModelTitle) {
      UserConversationConfig plusModelConversation = new UserConversationConfig();
      plusModelConversation.setModel(modelTitle);
      selections.put(modelTitle, JSONUtil.toJsonStr(plusModelConversation));
    }
    return selections;
  }

  private void retry(Account account, String chatId, P2MessageReceiveV1 event, String messageId) throws Exception {
    //重新处理该 event，删除去重记录
    accountService.addBusyAccount(account.getAccount());
    conversationPool.conversationMap.remove(chatId);
    executorService.submit(() -> {
      try {
        TimeUnit.SECONDS.sleep(10);
        accountService.removeBusyAccount(account.getAccount());
      } catch (Exception e) {
        log.error(String.valueOf(e));
      }
    });
    RequestIdSet.requestIdSet.remove(event.getRequestId());
    process(event, messageId);
  }

  private void processAnswer(Answer answer, String title, String chatId, Account account, String messageId, P2MessageReceiveV1 event, Map<String, String> selections, Record record, UserConversationConfig conversationConfig) throws Exception {
    log.debug("process answer : {}",answer);
    if (!answer.isSuccess()) {
      // gpt请求失败
      selections.remove("使用当前上下文");
      if (answer.getErrorCode() == ErrorCode.INVALID_JWT || answer.getErrorCode() == ErrorCode.INVALID_API_KEY) {
        //token过期，重新登录，重新处理
        log.warn("账号{} {}", account.getAccount(), answer.getError());
        messageService.modifyGptAnswerMessageCardWithSelection(messageId, title, (String) answer.getError(), selections);
        accountService.login(account.getAccount(), account.getPassword());
        Account build = accountService.build(account, false);
        if (build.isAvailable()) {
//          accountService.removeBusyAccount(account.getAccount());
          RequestIdSet.requestIdSet.remove(event.getRequestId());
          process(event, messageId);
          return;
        } else {
          messageService.modifyGptAnswerMessageCardWithSelection(messageId, title, build.getError(), selections);
          record.setAnswer(null);
          record.setStatus(Status.FAILED);
          record.setErrorMessage((String) answer.getError());
        }
      } else if (answer.getErrorCode() == ErrorCode.ACCOUNT_DEACTIVATED) {
        //账号被封禁
        answer.setError(ErrorCode.map.get(ErrorCode.ACCOUNT_DEACTIVATED));
        log.error("账号{} {}", account.getAccount(), ErrorCode.map.get(ErrorCode.ACCOUNT_DEACTIVATED));
        account.setAvailable(false);
        account.setError(ErrorCode.map.get(ErrorCode.ACCOUNT_DEACTIVATED));
        accountService.updateAccountInfo(account);
      } else if (answer.getErrorCode() == ErrorCode.BUSY) {
        //如果是 Fast 模式，需要重试
        if (conversationConfig.getMode() == Mode.FAST) {
          //重新处理该 event，删除去重记录
          retry(account, chatId, event, messageId);
          return;
        }
      } else if (answer.getErrorCode() == ErrorCode.CONVERSATION_NOT_FOUNT) {
        if (conversationConfig.getMode() == Mode.FAST) {
          conversationPool.removeConversation(chatId);
          retry(account, chatId, event, messageId);
          return;
        }
      }
      messageService.modifyGptAnswerMessageCardWithSelection(messageId, title, (String) answer.getError(), selections);

      record.setAnswer(null);
      record.setStatus(Status.FAILED);
      record.setErrorMessage((String) answer.getError());
      accountService.removeBusyAccount(account.getAccount());


    } else {
      // gpt请求成功
      UserConversationConfig conversation = conversationPool.getConversation(chatId);
      conversation.setParentMessageId(answer.getMessage().getId());
      conversation.setConversationId(answer.getConversationId());

      if (answer.isFinished()) {
        record.setAnswer(answer.getAnswer());
        record.setStatus(Status.SUCCESS);
        record.setErrorMessage(null);
//        accountService.removeBusyAccount(account.getAccount());
      }

      selections.put("使用当前上下文", JSONUtil.toJsonStr(conversation));

      String content = answer.getAnswer();
      if (content == null || content.isEmpty()) {
        return;
      }
      PatchMessageResp resp1 = messageService.modifyGptAnswerMessageCardWithSelection(messageId, title, content, selections);

      if (answer.isFinished() && resp1.getCode() == 230020) {
        //保证最后完整的gpt响应 不会被飞书消息频率限制
        while (answer.isFinished() && resp1.getCode() != 0) {
          log.warn("重试中 code: {} msg: {}", resp1.getCode(), resp1.getMsg());
          TimeUnit.MILLISECONDS.sleep(500);
          resp1 = messageService.modifyGptAnswerMessageCardWithSelection(messageId, title, content, selections);
        }
        log.info("重试成功");
      }
    }

    if (record.getStatus() != Status.RUNNING && !record.isUpdated()) {
      log.info("service finished,account: {} ,title:{},chatId:{}", account.getAccount(), title, chatId);
      recordService.updateRecord(record);
      record.setUpdated(true);
//      accountService.removeBusyAccount(account.getAccount());
    }


  }
}
