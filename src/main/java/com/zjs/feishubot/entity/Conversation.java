package com.zjs.feishubot.entity;

import lombok.Data;

@Data
public class Conversation {
    public String conversationId;
    public String chatId;
    public String account;
    public Model model;
    public String parentMessageId;
    public volatile Status status;
}
