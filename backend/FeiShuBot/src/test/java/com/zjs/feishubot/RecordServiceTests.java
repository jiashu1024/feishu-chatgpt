package com.zjs.feishubot;

import com.zjs.feishubot.config.KeyGenerateConfig;
import com.zjs.feishubot.entity.QuestionSummary;
import com.zjs.feishubot.entity.Record;
import com.zjs.feishubot.entity.gpt.Models;
import com.zjs.feishubot.service.RecordService;
import org.junit.jupiter.api.Test;
import org.redisson.api.RMap;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class RecordServiceTests {

  @Autowired
  private RecordService recordService;

  @Autowired
  private RedissonClient redissonClient;

  @Test
  public void testGetQuestionSummary() {
    List<QuestionSummary> summaryList = recordService.getQuestionSummary(7);
    for (QuestionSummary questionSummary : summaryList) {
      System.out.println(questionSummary);
    }
  }


  @Test
  public void prepareModelData() {
    RSet<String> set = redissonClient.getSet(KeyGenerateConfig.MODEL_SET_KEY);
    set.addAll(Models.modelMap.keySet());
  }

  @Test
  public void prepareUserUsageData() {

    // 清空所有用户使用数据统计
    RSet<String> set = redissonClient.getSet(KeyGenerateConfig.MODEL_SET_KEY);
    for (String model : set) {
      String key = KeyGenerateConfig.getUserUsageHashKey(model);
      RMap<String, Integer> map = redissonClient.getMap(key);
      map.clear();
    }

    // 重新统计用户使用数据
    List<Record> records = recordService.getAllRecords();
    for (Record record : records) {
      String model = record.getModel();
      String key = KeyGenerateConfig.getUserUsageHashKey(model);
      RMap<String, Integer> map = redissonClient.getMap(key);
      map.addAndGetAsync(record.getUserId(), 1);
    }
  }
}
