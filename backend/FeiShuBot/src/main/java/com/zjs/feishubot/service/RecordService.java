package com.zjs.feishubot.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.lark.oapi.service.contact.v3.model.User;
import com.zjs.feishubot.config.KeyGenerateConfig;
import com.zjs.feishubot.entity.*;
import com.zjs.feishubot.entity.Record;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RMap;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.protocol.ScoredEntry;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.*;

@Component
@RequiredArgsConstructor
public class RecordService {

  protected final RedissonClient redissonClient;

  public Record saveRecord(User user, Account account, String model, Mode mode, String question, String answer, Status status, String errorMessage, boolean isPlusModel) {
    RScoredSortedSet<Object> recordIdSet = redissonClient.getScoredSortedSet(KeyGenerateConfig.RECORD_ID_SET_KEY);
    RMap<Object, Object> records = redissonClient.getMap(KeyGenerateConfig.RECORDS_KEY);
    int size = recordIdSet.size();
    Record record = new Record();
    record.setId(size + 1);
    record.setUserId(user.getOpenId());
    record.setUserName(user.getName());
    record.setAvatar(user.getAvatar().getAvatarOrigin());
    record.setAccount(account.getAccount());
    record.setAccountLevel(account.isPlusAccount() ? "plus" : "free");
    record.setModel(model);
    record.setMode(mode);
    record.setQuestion(question);
    record.setAnswer(answer);
    record.setStatus(status);
    record.setErrorMessage(errorMessage);
    record.setPlusModel(isPlusModel);

    DateTime now = DateTime.now();
    record.setTime(now.toTimestamp());
    recordIdSet.addAsync(now.getTime(), record.getId());
    records.putAsync(record.getId(), record);
    return record;
  }

//  public void updateRecord(String id, String answer, Status status, String errorMessage) {
//    RMap<Object, Object> records = redissonClient.getMap(KeyGenerateConfig.RECORDS_KEY);
//    Record record = (Record) records.get(id);
//    record.setAnswer(answer);
//    record.setStatus(status);
//    record.setErrorMessage(errorMessage);
//    records.putAsync(id, record);
//    if (record.getStatus() == Status.SUCCESS) {
//      saveUserUsage(record);
//    }
//  }

  public void updateRecord(Record record) {
    RMap<Object, Object> records = redissonClient.getMap(KeyGenerateConfig.RECORDS_KEY);
    records.putAsync(record.getId(), record);
    if (record.getStatus() == Status.SUCCESS) {
      saveUserUsage(record);
    }
  }

  public List<Record> getAllRecords() {
    RMap<Object, Object> map = redissonClient.getMap(KeyGenerateConfig.RECORDS_KEY);
    Collection<Object> values = map.values();
    List<Record> records = new ArrayList<>();
    for (Object value : values) {
      records.add((Record) value);
    }
    return records;
  }

  public List<Record> getRecordsByDaysAgo(int days) {
    // 获取days天前零点的时间戳
    DateTime today = DateUtil.beginOfDay(DateTime.now());
    DateTime ago = DateUtil.offsetDay(today, -days);
    // 获取days天前零点的所有记录id
    RScoredSortedSet<Integer> scoredSortedSet = redissonClient.getScoredSortedSet(KeyGenerateConfig.RECORD_ID_SET_KEY);
    Iterable<Integer> recordIds = scoredSortedSet.valueRange(ago.getTime(), true, DateTime.now().getTime(), true);
    //将所有id放入到Set里
    Set<Integer> ids = new HashSet<>();
    recordIds.forEach(ids::add);

    // 获取days天前零点的所有记录
    RMap<Integer, Record> records = redissonClient.getMap(KeyGenerateConfig.RECORDS_KEY);
    Map<Integer, Record> all = records.getAll(ids);
    List<Record> result = new ArrayList<>();
    all.forEach((k, v) -> result.add(v));
    return result;
  }

  /**
   * 获取最近几天的提问统计 (包含今天)
   *
   * @param days 天数
   * @return
   */
  public List<QuestionSummary> getQuestionSummary(int days) {
    days--;
    List<QuestionSummary> questionSummaries = new ArrayList<>();
    List<Record> records = getRecordsByDaysAgo(days);

    DateTime today = DateUtil.beginOfDay(DateTime.now());
    DateTime ago = DateUtil.offsetDay(today, -days);
    Map<String, QuestionSummary> map = new HashMap<>();

    for (int i = 0; i <= days; i++) {
      DateTime day = DateUtil.offsetDay(ago, i);
      QuestionSummary questionSummary = new QuestionSummary();
      questionSummary.setDate(day.toSqlDate().toLocalDate());
      questionSummaries.add(questionSummary);
      map.put(day.toString("yyyy-MM-dd"), questionSummary);
    }

    for (Record record : records) {
      Timestamp time = record.getTime();
      DateTime dateTime = DateUtil.date(time);
      String date = dateTime.toString("yyyy-MM-dd");
      QuestionSummary questionSummary = map.get(date);
      if (record.isPlusModel()) {
        if (record.getStatus() == Status.SUCCESS) {
          questionSummary.setPlusSuccessCount(questionSummary.getPlusSuccessCount() + 1);
        } else {
          questionSummary.setPlusFailCount(questionSummary.getPlusFailCount() + 1);
        }
      } else {
        if (record.getStatus() == Status.SUCCESS) {
          questionSummary.setFreeSuccessCount(questionSummary.getFreeSuccessCount() + 1);
        } else {
          questionSummary.setFreeFailCount(questionSummary.getFreeFailCount() + 1);
        }
      }
    }
    return questionSummaries;
  }

  public void saveUserUsage(Record record) {
    RMap<String, Integer> map = redissonClient.getMap(KeyGenerateConfig.getUserUsageHashKey(record.getModel()));
    map.addAndGetAsync(record.getUserId(), 1);
  }

  public List<Record> getRecord(int cursor, int i) {
    RScoredSortedSet<Integer> set = redissonClient.getScoredSortedSet(KeyGenerateConfig.RECORD_ID_SET_KEY);
    int size = set.size();
    if (cursor + i > size) {
      i = size - cursor;
      if (i <= 0) {
        return new ArrayList<>();
      }
    }

    Collection<ScoredEntry<Integer>> scoredEntries = set.entryRangeReversed(cursor, cursor + i - 1);
    List<Integer> ids = new ArrayList<>();
    scoredEntries.forEach(scoredEntry -> ids.add(scoredEntry.getValue()));
    RMap<Integer, Record> records = redissonClient.getMap(KeyGenerateConfig.RECORDS_KEY);
    Set<Integer> idSet = new HashSet<>(ids);
    Map<Integer, Record> all = records.getAll(idSet);
    List<Record> result = new ArrayList<>();
    all.forEach((k, v) -> result.add(v));
    result.sort(Comparator.comparing(Record::getTime).reversed());
    return result;
  }
}
