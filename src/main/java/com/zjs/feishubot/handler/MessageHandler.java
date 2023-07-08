package com.zjs.feishubot.handler;

import cn.hutool.json.JSONUtil;
import com.lark.oapi.Client;
import com.lark.oapi.service.contact.v3.model.User;
import com.lark.oapi.service.im.v1.model.*;
import com.zjs.feishubot.entity.Conversation;
import com.zjs.feishubot.entity.Status;
import com.zjs.feishubot.entity.gpt.Answer;
import com.zjs.feishubot.entity.gpt.ErrorCode;
import com.zjs.feishubot.entity.gpt.Models;
import com.zjs.feishubot.service.MessageService;
import com.zjs.feishubot.service.UserService;
import com.zjs.feishubot.util.chatgpt.AccountPool;
import com.zjs.feishubot.util.chatgpt.ChatService;
import com.zjs.feishubot.util.chatgpt.ConversationPool;
import com.zjs.feishubot.util.chatgpt.RequestIdSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class MessageHandler {


  protected final Client client;
  protected final AccountPool accountPool;
  protected final UserService userService;
  protected final MessageService messageService;
  protected final ConversationPool conversationPool;

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
      //log.warn("重复请求，requestId:{}", requestId);
      return false;
    }
    RequestIdSet.requestIdSet.add(requestId);

    String createTime = event.getEvent().getMessage().getCreateTime();
    Long createTimeLong = Long.valueOf(createTime);
    Long now = System.currentTimeMillis();
    if (now - createTimeLong > 1000 * 10) {
      // 根据消息事件的创建时间去重
      //log.warn("消息过期，requestId:{}", requestId);
      return false;
    }

    P2MessageReceiveV1Data messageEvent = event.getEvent();
    EventMessage message = messageEvent.getMessage();

    String chatType = message.getChatType();
    String msgType = message.getMessageType();

    log.info("chatType:{},msgType:{}", chatType, msgType);


    // 只处理私聊文本消息
    if (!chatType.equals("p2p") && !msgType.equals("text")) {
      log.warn("不支持的ChatType或MsgType,ChatGpt不处理");
      return false;
    }

    return true;
  }


  public void process(P2MessageReceiveV1 event) throws Exception {
    P2MessageReceiveV1Data messageEvent = event.getEvent();
    EventMessage message = messageEvent.getMessage();

    if (!checkInvalidEvent(event)) {
      return;
    }
    JSONObject jsonObject = new JSONObject(message.getContent());
    String text = jsonObject.optString("text");

    try {
      User user = userService.getUserByOpenId(event.getEvent().getSender().getSenderId().getOpenId());
      String name = user.getName();
      log.info("{}:{}", name, text);
    } catch (Exception e) {
      log.error("获取用户信息失败", e);
    }

    if (text == null || text.equals("")) {
      return;
    }
    String chatId = message.getChatId();
    //尝试从会话池中获取会话，从而保持上下文
    Conversation conversation = conversationPool.getConversation(chatId);
    String model;

    String account = null;
    boolean newChat = false;
    ChatService chatService;

    if (conversation == null) {
      //如果没有会话，新建会话，采用默认3.5的模型
      newChat = true;
      model = Models.DEFAULT_MODEL;
      chatService = accountPool.getFreeChatService(model);

      conversation = new Conversation();
      conversation.setChatId(chatId);
      conversation.setModel(model);
      conversationPool.addConversation(chatId, conversation);
    } else {
      //如果有会话，则需要判断是要建新会话意图还是以前老会话
      if (conversation.getConversationId() == null || conversation.getConversationId().equals("")) {
        //新会话意图
        newChat = true;
        model = conversation.getModel();
        chatService = accountPool.getFreeChatService(model);
      } else {
        //老会话意图
        account = conversation.getAccount();
        chatService = accountPool.getChatService(account);
        model = conversation.getModel();
      }
    }

    if (chatService == null) {
      if (accountPool.getSize() == 0) {
        messageService.sendTextMessageByChatId(chatId, "服务器未配置可用账户");
        return;
      }
      messageService.sendTextMessageByChatId(chatId, "目前无空闲该模型，请稍后再试，或者更换模型");
      return;
    }
    if (chatService.getStatus() == Status.RUNNING) {
      messageService.sendTextMessageByChatId(chatId, "目前该模型正在运行，请稍等...");
    }
    chatService.setStatus(Status.RUNNING);
    conversation.setStatus(Status.RUNNING);

    String title;
    if (chatService.getAccount() == null || chatService.getAccount().length() == 0) {
      title = model;
    } else {
      title = model + " : " + chatService.getAccount();
    }

    if (chatService.getLevel() == 4) {
      title += "  [plus] ";
    }


    CreateMessageResp resp = messageService.sendGptAnswerMessage(chatId, title, "正在生成中，请稍后");
    String messageId = resp.getData().getMessageId();

    Map<String, String> selections = createSelection(conversation);

    String modelParam = Models.modelMap.get(model).getSlug();
    if (newChat) {
      log.info("新建会话");
      conversation.setTitle(title);
      String finalTitle = title;
      chatService.newChat(text, modelParam, answer -> {
        processAnswer(answer, finalTitle, chatId, chatService, messageId, event, model, selections);
      });

    } else {
      log.info("继续会话");
      title = conversation.getTitle();
      String finalTitle1 = title;
      chatService.keepChat(text, modelParam, conversation.parentMessageId, conversation.conversationId, answer -> {
        processAnswer(answer, finalTitle1, chatId, chatService, messageId, event, model, selections);
      });
    }
    log.info("服务完成,account: {} ,model:{},chatId:{}",chatService.getAccount(),model,chatId);
  }

  private Map<String, String> createSelection(Conversation conversation) {
    //将curConversation转换成json格式
    String conversationStr = JSONUtil.toJsonStr(conversation);
    Map<String, String> selections = new HashMap<>();
    selections.put("使用当前上下文", conversationStr);

    for (String s : Models.normalModelTitle) {
      Conversation normalModelConversation = new Conversation();
      normalModelConversation.setModel(s);
      selections.put(s, JSONUtil.toJsonStr(normalModelConversation));
    }

    if (AccountPool.plusPool.isEmpty()) {
      return selections;
    }

    for (String modelTitle : Models.plusModelTitle) {
      Conversation plusModelConversation = new Conversation();
      plusModelConversation.setModel(modelTitle);
      selections.put(modelTitle, JSONUtil.toJsonStr(plusModelConversation));

    }
    return selections;
  }

  private void processAnswer(Answer answer, String title, String chatId, ChatService chatService, String messageId, P2MessageReceiveV1 event, String model, Map<String, String> selections) throws Exception {
    if (!answer.isSuccess()) {
      // gpt请求失败
      selections.remove("使用当前上下文");
      if (answer.getErrorCode() == ErrorCode.INVALID_JWT || answer.getErrorCode() == ErrorCode.INVALID_API_KEY) {

        messageService.modifyGptAnswerMessageCardWithSelection(messageId, title, (String) answer.getError(), selections);
        boolean build = chatService.build();
        if (build) {
          accountPool.modifyChatService(chatService);
          RequestIdSet.requestIdSet.remove(event.getRequestId());
          process(event);
        } else {
          messageService.modifyGptAnswerMessageCardWithSelection(messageId, title, "账号重新登录失败", selections);
        }
        return;
      }
      messageService.modifyGptAnswerMessageCardWithSelection(messageId, title, (String) answer.getError(), selections);
      chatService.setStatus(Status.FINISHED);
      conversationPool.getConversation(chatId).setStatus(Status.FINISHED);
      return;
    }
    Conversation conversation = conversationPool.getConversation(chatId);
    conversation.setChatId(chatId);
    conversation.setParentMessageId(answer.getMessage().getId());
    conversation.setConversationId(answer.getConversationId());
    conversation.setAccount(chatService.getAccount());
    conversation.setModel(model);
    conversation.setTitle(title);

    if (!answer.isFinished()) {
      chatService.setStatus(Status.RUNNING);
      conversation.setStatus(Status.RUNNING);
    } else {
      chatService.setStatus(Status.FINISHED);
      conversation.setStatus(Status.FINISHED);
    }

    selections.put("使用当前上下文", JSONUtil.toJsonStr(conversation));

    String content = answer.getAnswer();
    if (content == null || content.equals("")) {
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
}
