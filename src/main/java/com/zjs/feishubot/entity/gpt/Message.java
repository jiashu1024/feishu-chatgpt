package com.zjs.feishubot.entity.gpt;

import lombok.Data;

@Data
public class Message {
    private String id;
    private Author author;
    private Double createTime;
    private Object updateTime;
    private Content content;
    private String status;
    private Boolean endTurn;
    private Double weight;

}
