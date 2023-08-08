package com.zjs.feishubot.controller;

import com.zjs.feishubot.service.AccountService;
import com.zjs.feishubot.service.RecordService;
import com.zjs.feishubot.util.FeiShuUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
@ResponseBody
public class DataController {

  protected final RecordService recordService;

  protected final AccountService accountService;

  protected final FeiShuUtil feiShuUtil;
//
//  @GetMapping("/mainPageData")
//  public MainPageData getMainPageData() {
//    MainPageData mainPageData = new MainPageData();
//    AccountStatistics accountStatistics = new AccountStatistics();
//    DailyStatistics dailyStatistics = new DailyStatistics();
//    mainPageData.setDailyStatistics(dailyStatistics);
//    mainPageData.setAccountStatistics(accountStatistics);
//
//    List<Record> allRecords = recordService.getAllRecords();
//    //获取今天0点0分的时间戳
//
//    LocalDateTime localDateTime = LocalDateTimeUtil.beginOfDay(LocalDateTimeUtil.now());
//    Timestamp timestamp = Timestamp.valueOf(localDateTime);
//
//
//    Set<String> userSet = new HashSet<>();
//    Map<String, UserUsage> map = new HashMap<>();
//
//    for (Record record : allRecords) {
//      if (record.getTime().after(timestamp)) {
//        //今天的记录
//        userSet.add(record.getUserId());
//        if (record.isPlusModel()) {
//          if (record.getStatus() == Status.SUCCESS) {
//            dailyStatistics.setPlusSuccessRequestNum(dailyStatistics.getPlusSuccessRequestNum() + 1);
//          } else {
//            dailyStatistics.setPlusFailedRequestNum(dailyStatistics.getPlusFailedRequestNum() + 1);
//          }
//        } else {
//          if (record.getStatus() == Status.SUCCESS) {
//            dailyStatistics.setFreeSuccessRequestNum(dailyStatistics.getFreeSuccessRequestNum() + 1);
//          } else {
//            dailyStatistics.setFreeFailedRequestNum(dailyStatistics.getFreeFailedRequestNum() + 1);
//          }
//        }
//      }
//      if (map.containsKey(record.getUserId())) {
//        UserUsage userUsage = map.get(record.getUserId());
//        userUsage.setAllCount(userUsage.getAllCount() + 1);
//      } else {
//        UserUsage userUsage = new UserUsage();
//        userUsage.setUserId(record.getUserId());
//        userUsage.setUserName(record.getUserName());
//        userUsage.setAvatar(record.getAvatar());
//        userUsage.setAllCount(1);
//        map.put(record.getUserId(), userUsage);
//      }
//    }
//    dailyStatistics.setPeopleNum(userSet.size());
//
//    List<UserUsage> userUsageList = new ArrayList<>(map.values());
//    userUsageList.sort((o1, o2) -> o2.getAllCount() - o1.getAllCount());
//    mainPageData.setUserUsageList(userUsageList);
//
//    allRecords.sort((o1, o2) -> o2.getTime().compareTo(o1.getTime()));
//    //只返回allRecords的前30条记录
//    if (allRecords.size() > 30) {
//      allRecords = allRecords.subList(0, 30);
//    }
//    mainPageData.setRecords(allRecords);


//    List<Account> accounts = accountService.getAllAccountNoCheck();
//    for (Account account : accounts) {
//      accountStatistics.setAccountNum(accountStatistics.getAccountNum() + 1);
//      if (account.isAvailable()) {
//        accountStatistics.setAvailableAccountNum(accountStatistics.getAvailableAccountNum() + 1);
//      }
//
//      if (account.isPlusAccount()) {
//        accountStatistics.setPlusAccountNum(accountStatistics.getPlusAccountNum() + 1);
//        if (account.isAvailable()) {
//          accountStatistics.setPlusAccountAvailableNum(accountStatistics.getPlusAccountAvailableNum() + 1);
//        }
//        if (account.isPlusPublic()) {
//          accountStatistics.setPlusAccountPublicNum(accountStatistics.getPlusAccountPublicNum() + 1);
//        }
//      } else {
//        accountStatistics.setFreeAccountNum(accountStatistics.getFreeAccountNum() + 1);
//        if (account.isAvailable()) {
//          accountStatistics.setFreeAccountAvailableNum(accountStatistics.getFreeAccountAvailableNum() + 1);
//        }
//        if (account.isFreePublic()) {
//          accountStatistics.setFreeAccountPublicNum(accountStatistics.getFreeAccountPublicNum() + 1);
//        }
//      }
//    }
//    return mainPageData;
//  }
}
