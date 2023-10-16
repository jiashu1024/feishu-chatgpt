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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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

  @Test
  public void deleteRecord() {
    RMap<Object, Object> map = redissonClient.getMap(KeyGenerateConfig.RECORDS_KEY);
    RSet<Object> set = redissonClient.getSet(KeyGenerateConfig.RECORD_ID_SET_KEY);
    Set<Object> objects = map.keySet();
    System.out.println("删除前记录池的大小为:" + objects.size());

    int count  = 0;

    Set<String> keys = new HashSet<>();
    keys.add("你好");
    keys.add("你好啊");
    keys.add("写个java的冒泡排序");
    keys.add("介绍下自己");
    keys.add("你是谁");


    for (Object object : objects) {
      Record record = (Record) map.get(object);
      if (keys.contains(record.getQuestion())) {
        System.out.println(object);
        count++;
        map.remove(object);
//        System.out.println(set.contains(object));
//        if (set.contains(object)) {
//          set.remove(object);
//        }
      }
    }
    System.out.println("删除的记录个数为:" + count);

    map = redissonClient.getMap(KeyGenerateConfig.RECORDS_KEY);
    objects = map.keySet();
    System.out.println("删除后记录池的大小为:" + objects.size());
  }
}
