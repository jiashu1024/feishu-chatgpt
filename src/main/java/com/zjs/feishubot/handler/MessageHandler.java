package com.zjs.feishubot.handler;

import cn.hutool.json.JSONUtil;
import com.lark.oapi.Client;
import com.lark.oapi.service.contact.v3.model.User;
import com.lark.oapi.service.im.v1.model.*;
import com.zjs.feishubot.entity.Conversation;
import com.zjs.feishubot.entity.Model;
import com.zjs.feishubot.entity.Status;
import com.zjs.feishubot.entity.gpt.Answer;
import com.zjs.feishubot.entity.gpt.ErrorCode;
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

    private boolean checkInvalidEvent(P2MessageReceiveV1 event) {
        String requestId = event.getRequestId();
        if (RequestIdSet.requestIdSet.contains(requestId)) {
            log.warn("重复请求，requestId:{}", requestId);
            return false;
        }
        RequestIdSet.requestIdSet.add(requestId);
        String createTime = event.getEvent().getMessage().getCreateTime();
        Long createTimeLong = Long.valueOf(createTime);
        Long now = System.currentTimeMillis();
        if (now - createTimeLong > 1000 * 10) {
            log.warn("消息过期，requestId:{}", requestId);
            return false;
        }

        P2MessageReceiveV1Data messageEvent = event.getEvent();
        EventMessage message = messageEvent.getMessage();

        String chatType = message.getChatType();
        String msgType = message.getMessageType();

        log.info("chatType:{},msgType:{}", chatType, msgType);

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
        //尝试从会话池中获取会话
        Conversation conversation = conversationPool.getConversation(chatId);
        Model model;


        String account = null;
        boolean newChat = false;
        ChatService chatService;

        if (conversation == null) {
            //如果没有会话，新建会话，采用默认3.5的模型
            newChat = true;
            model = Model.GPT_3_5;
            chatService = accountPool.getFreeChatService(model);

            conversation = new Conversation();
            conversation.setChatId(chatId);
            conversation.setModel(model);
            conversationPool.addConversation(chatId, conversation);
        } else {
            //如果有会话，则需要判断是要建新会话意图还是以前老会话
            chatService = null;
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
            messageService.sendTextMessageByChatId(chatId, "目前无空闲该模型，请稍后再试，或者更换模型");
            return;
        }
        if (chatService.getStatus() == Status.RUNNING) {
            messageService.sendTextMessageByChatId(chatId, "目前该模型正在运行，请稍等...");
        }
        chatService.setStatus(Status.RUNNING);
        conversation.setStatus(Status.RUNNING);
        log.info("chatId:{},model:{} 提供服务", chatId, model);

        String title = model.key + " : " + chatService.getAccount();

        CreateMessageResp resp = messageService.sendGptAnswerMessage(chatId, title, "正在生成中，请稍后");
        String messageId = resp.getData().getMessageId();

        Map<String, String> selections = createSelection(conversation);

        log.info("title:{}", title);
        if (newChat) {
            log.info("新建会话");
            ChatService finalChatService = chatService;

            chatService.newChat(text, model.value, answer -> {
                processAnswer(answer, title, chatId, finalChatService, messageId, event, model, selections);
            });
            //conversation = ConversationPool.getConversation(chatId);
            // chatService.genTitle(conversation.conversationId);
        } else {
            log.info("继续会话");
//            if (conversation.getStatus() == Status.RUNNING) {
//                messageService.modifyGptAnswerMessageCardWithSelection(messageId, title, "账号繁忙中,请稍等...", selections);
//            }
            ChatService finalChatService1 = chatService;
            chatService.keepChat(text, model.value, conversation.parentMessageId, conversation.conversationId, answer -> {
                String newTitle = model.key + " : " + finalChatService1.getAccount();
                processAnswer(answer, newTitle, chatId, finalChatService1, messageId, event, model, selections);
            });
        }
    }

    private Map<String, String> createSelection(Conversation conversation) {
        //将curConversation转换成json格式
        String conversationStr = JSONUtil.toJsonStr(conversation);
        Map<String, String> selections = new HashMap<>();
        selections.put("使用当前上下文", conversationStr);

        Conversation conversation35 = new Conversation();
        conversation35.setModel(Model.GPT_3_5);
        selections.put(Model.GPT_3_5.key, JSONUtil.toJsonStr(conversation35));

        Conversation conversation4Brose = new Conversation();
        conversation4Brose.setModel(Model.PLUS_GPT_4_BROWSING);
        selections.put(Model.PLUS_GPT_4_BROWSING.key, JSONUtil.toJsonStr(conversation4Brose));

        Conversation conversation4 = new Conversation();
        conversation4.setModel(Model.PLUS_4_DEFAULT);
        selections.put(Model.PLUS_4_DEFAULT.key, JSONUtil.toJsonStr(conversation4));

        Conversation conversationMobile = new Conversation();
        conversationMobile.setModel(Model.PLUS_GPT_4_MOBILE);
        selections.put(Model.PLUS_GPT_4_MOBILE.key, JSONUtil.toJsonStr(conversationMobile));
        return selections;
    }

    private void processAnswer(Answer answer, String title, String chatId, ChatService chatService, String messageId, P2MessageReceiveV1 event, Model model, Map<String, String> selections) throws Exception {
        if (!answer.isSuccess()) {
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

        if (!answer.isFinished()) {
            chatService.setStatus(Status.RUNNING);
            conversation.setStatus(Status.RUNNING);
        } else {
            chatService.setStatus(Status.FINISHED);
            conversation.setStatus(Status.FINISHED);
            // log.info(answer.toString());
        }

        selections.put("使用当前上下文", JSONUtil.toJsonStr(conversation));


        String content = answer.getAnswer();
        if (content == null || content.equals("")) {
            return;
        }
        PatchMessageResp resp1 = messageService.modifyGptAnswerMessageCardWithSelection(messageId, title, content, selections);

        if (answer.isFinished() && resp1.getCode() == 230020) {
            //log.warn("任务已完成，消息发送失败，正在重试");
            while (answer.isFinished() && resp1.getCode() != 0) {
                log.warn("重试中 code: {} msg: {}", resp1.getCode(), resp1.getMsg());
                TimeUnit.MILLISECONDS.sleep(500);
                resp1 = messageService.modifyGptAnswerMessageCardWithSelection(messageId, title, content, selections);
            }
            log.info("重试成功");
        }
    }
}
