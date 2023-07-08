package com.zjs.feishubot.entity;

import com.zjs.feishubot.entity.gpt.Model;
import lombok.Data;

@Data
public class Conversation {
    public String conversationId;
    public String chatId;
    public String account;
    public String model;
    public String parentMessageId;
    public volatile Status status;
    public String title;
}
