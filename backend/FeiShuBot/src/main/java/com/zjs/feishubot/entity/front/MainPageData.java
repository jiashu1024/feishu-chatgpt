package com.zjs.feishubot.entity.front;

import com.zjs.feishubot.entity.Record;
import lombok.Data;

import java.util.List;

@Data
public class MainPageData {
  /**
   * 账号统计
   */
  private AccountStatistics accountStatistics;

  /**
   * 每日统计
   */
  private DailyStatistics dailyStatistics;

  /**
   * 最近记录数
   */
  private List<Record> records;

  /**
   * 用户使用情况
   */
  private List<UserUsage> userUsageList;
}
