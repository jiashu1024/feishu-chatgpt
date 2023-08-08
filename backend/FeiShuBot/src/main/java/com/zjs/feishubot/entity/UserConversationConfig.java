package com.zjs.feishubot.entity;

import lombok.Data;

@Data
public class UserConversationConfig {
  /**
   * gpt的会话id
   */
  private String conversationId;
  /**
   * 对话id，用于区分不同用户
   */
  private String chatId;
  /**
   * 服务该会话的gpt账号
   */
  private String account;
  /**
   * 服务该会话的gpt模型
   */
  private String model;
  /**
   * gpt中的上下文消息id
   */
  private String parentMessageId;

  /**
   * 消息中的标题
   */
  private String title;

  /**
   * 回复模式
   */
  private Mode mode;

  /**
   * 是否是plus账号
   */
  private boolean isPlus;
}
