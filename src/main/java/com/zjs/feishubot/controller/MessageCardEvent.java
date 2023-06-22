package com.zjs.feishubot.controller;

import cn.hutool.json.JSONUtil;
import com.zjs.feishubot.entity.Conversation;
import com.zjs.feishubot.util.chatgpt.ConversationPool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MessageCardEvent {

    protected final ConversationPool conversationPool;

    @PostMapping("/cardEvent")
    @ResponseBody
    public String event(@RequestBody String body) {
        JSONObject payload = new JSONObject(body);
        if (payload.opt("challenge") != null) {
            JSONObject res = new JSONObject();
            res.put("challenge", payload.get("challenge"));
            return res.toString();
        }

        String chatId = String.valueOf(payload.get("open_chat_id"));

        JSONObject action = (JSONObject) payload.get("action");
        String option = (String) action.get("option");

        Conversation bean = JSONUtil.toBean(option, Conversation.class);

        if (bean.getParentMessageId() == null) {
            bean.setParentMessageId("");
        }
        if (bean.getConversationId() == null) {
            bean.setConversationId("");
        }
        bean.setChatId(chatId);
        conversationPool.addConversation(chatId, bean);
        return "";
    }
}
