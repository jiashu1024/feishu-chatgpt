package com.zjs.feishubot.entity.front;

import lombok.Data;

@Data
public class AccountStatistics {
  /**
   * plus账号plus模型公开个数
   */
  private int plusAccountPlusPublicNum;

  /**
   * plus账号free模型公开个数
   */
  private int plusAccountFreePublicNum;
  /**
   * plus账号可用个数
   */
  private int plusAccountAvailableNum;
  /**
   * plus账号总个数
   */
  private int plusAccountNum;
  /**
   * 免费账号账号免费模型公开个数
   */
  private int freeAccountFreePublicNum;

  /**
   * 普通账号可用个数
   */
  private int freeAccountAvailableNum;
  /**
   * 普通账号总个数
   */
  private int freeAccountNum;
}
