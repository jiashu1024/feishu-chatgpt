package com.zjs.feishubot.entity;

import lombok.Data;

import java.time.LocalDate;

@Data
/**
 * 一天的提问统计
 */
public class QuestionSummary {
  /**
   * 日期
   */
  private LocalDate date;

  /**
   * plus能力提问成功次数
   */
  private int plusSuccessCount;
  /**
   * plus能力提问失败次数
   */
  private int plusFailCount;
  /**
   * free能力提问成功次数
   */
  private int freeSuccessCount;
  /**
   * free能力提问失败次数
   */
  private int freeFailCount;
}
