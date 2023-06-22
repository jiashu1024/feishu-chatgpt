package com.zjs.feishubot.entity.gpt;

import lombok.Data;

@Data
public class Answer {

    private boolean success;
    private Message message;
    private String conversationId;
    private Object error;
    private boolean finished;
    private String answer;

    private int errorCode;
}
