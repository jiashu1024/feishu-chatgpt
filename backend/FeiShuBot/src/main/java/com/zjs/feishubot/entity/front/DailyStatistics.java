package com.zjs.feishubot.entity.front;

import lombok.Data;

@Data
public class DailyStatistics {
  /**
   * plus模型成功请求次数
   */
  private int plusSuccessRequestNum;
  /**
   * plus模型失败请求次数
   */
  private int plusFailedRequestNum;
  /**
   * 普通模型成功请求次数
   */
  private int freeSuccessRequestNum;
  /**
   * 普通模型失败请求次数
   */
  private int freeFailedRequestNum;
  /**
   * 今日使用人数
   */
  private int peopleNum;
}
