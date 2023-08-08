package com.zjs.feishubot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.sql.Timestamp;

@Data
/**
 * 记录用户的服务记录
 */
public class Record {
  /**
   * 记录id
   */
  private int id;
  /**
   * 用户open id
   */
  private String userId;

  /**
   * 用户名
   */
  private String userName;

  /**
   * 用户头像
   */
  private String avatar;


  /**
   * 服务的账号
   */
  private String account;

  /**
   * 服务的账号等级
   */
  private String accountLevel;

  /**
   * 服务的模型
   */
  private String model;

  private boolean plusModel;

  /**
   * 服务的时间
   */
  private Timestamp time;


  /**
   * 服务模式
   */
  private Mode mode;

  /**
   * 服务的问题
   */
  private String question;

  /**
   * 服务的回答
   */
  private String answer;

  /**
   * 错误信息
   */
  private String errorMessage;

  /**
   * 服务状态
   */
  private Status status;

  @JsonIgnore
  private boolean isUpdated;
}
