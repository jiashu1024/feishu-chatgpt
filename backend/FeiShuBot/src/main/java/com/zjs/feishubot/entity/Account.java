package com.zjs.feishubot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Data
public class Account {
  private String account;
  private String password;
  @JsonIgnore
  private String token;
  /**
   * plus能力是否公开
   */
  private boolean plusPublic;
  /**
   * 普通能力是否公开
   */
  private boolean freePublic;
  /**
   * 是否是plus账号
   */
  private boolean plusAccount;
  /**
   * 账号不可用时的错误信息
   */
  private String error;
  /**
   * 账号是否可用
   */
  private boolean available;
  /**
   * 账号是否正在运行
   */
  private boolean running;

  /**
   * 账号所属的用户id
   */
  private String ownerOpenId;
  /**
   * 账号所属的用户名称
   */
  private String ownerUserName;
  /**
   * 账号创建时间
   */
  private Timestamp createTime;

  /**
   * free能力授权的用户
   */
  private Set<String> freePublicUsers;
  /**
   * plus能力授权的用户
   */
  private Set<String> plusPublicUsers;

  public void addFreePublicUser(String userOpenId) {
    if (freePublicUsers == null) {
      freePublicUsers = new HashSet<>();
    }
    if (freePublicUsers.contains(userOpenId)) {
      return;
    }
    freePublicUsers.add(userOpenId);
  }

  public void addPlusPublicUser(String userOpenId) {
    if (plusPublicUsers == null) {
      plusPublicUsers = new HashSet<>();
    }
    if (plusPublicUsers.contains(userOpenId)) {
      return;
    }
    plusPublicUsers.add(userOpenId);
  }

  public Account() {
    this.plusPublicUsers = new HashSet<>();
    this.freePublicUsers = new HashSet<>();
  }

  public Account(String account, String password) {
    this.account = account;
    this.password = password;
  }


  @Override
  public String toString() {
    return "Account{" +
      "account='" + account + '\'' +
      ", password='" + password + '\'' +
      ", token='" + (token == null ? null : token.length()) + '\'' +
      ", plusPublic=" + plusPublic +
      ", freePublic=" + freePublic +
      ", plusAccount=" + plusAccount +
      ", error='" + error + '\'' +
      ", available=" + available +
      ", running=" + running +
      ", ownerOpenId='" + ownerOpenId + '\'' +
      ", ownerUserName='" + ownerUserName + '\'' +
      ", createTime=" + createTime +
      ", freePublicUsers=" + freePublicUsers +
      ", plusPublicUsers=" + plusPublicUsers +
      '}';
  }
}
