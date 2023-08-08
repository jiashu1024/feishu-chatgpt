package com.zjs.feishubot.entity.front;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class UserUsage {
  private String userId;
  private String userName;
  private String avatar;
  /**
   * 所有成功的请求次数
   */
  private int allCount;
  /**
   * 所有成功的免费模型请求次数
   */
  private int freeCount;
  /**
   * 所有成功的付费模型的请求次数
   */
  private int plusCount;

  /**
   * 每个模型的使用次数
   */
  private Map<String, Integer> modelUsageCountMap;

  public UserUsage() {
    this.modelUsageCountMap = new HashMap<>();
  }
}
