package com.zjs.feishubot.util.chatgpt;

import com.zjs.feishubot.entity.Conversation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class ConversationPool {
    public volatile Map<String, Conversation> conversationMap = new HashMap<>();

    public void addConversation(String chatId, Conversation conversation) {
        log.info("chatId:{} 会话变更前:{}", chatId, conversationMap.get(chatId));
        log.info("chatId:{} 会话变更后:{}", chatId, conversation);
        conversationMap.put(chatId, conversation);
    }

    public Conversation getConversation(String chatId) {
        return conversationMap.get(chatId);
    }
}
