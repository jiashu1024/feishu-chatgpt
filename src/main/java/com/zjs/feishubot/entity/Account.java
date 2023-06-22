package com.zjs.feishubot.entity;

import lombok.Data;

@Data
public class Account {
    private String account;
    private String password;
    private String level;
    private String token;
}
