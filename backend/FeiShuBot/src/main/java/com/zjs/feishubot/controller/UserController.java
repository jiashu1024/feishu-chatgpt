package com.zjs.feishubot.controller;

import com.lark.oapi.service.contact.v3.model.User;
import com.zjs.feishubot.entity.front.UserUsage;
import com.zjs.feishubot.service.UserService;
import com.zjs.feishubot.util.FeiShuUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@ResponseBody
@Slf4j
@RequiredArgsConstructor
public class UserController {

  protected final FeiShuUtil feiShuUtil;

  protected final UserService userService;

  @GetMapping("/allUser")
  public List<User> getAllUser() {
    return feiShuUtil.getAllUser();
  }

  @GetMapping("/userUsage")
  public List<UserUsage> getUserUsage() {
    return userService.getUserUsage();
  }

  @GetMapping("/getHistogramData")
  public Map<String, Object> getHistogramData() {
    List<UserUsage> userUsage = userService.getUserUsage();
    Map<String, Object> res = new HashMap<>();

    //获取所有模型在所有请求中的占比
    Map<String, Integer> modelHistogramData = new HashMap<>();
    int allCount = 0;
    int freeCount = 0;
    int plusCount = 0;
    for (UserUsage usage : userUsage) {
      usage.getModelUsageCountMap().forEach((k, v) -> {
        modelHistogramData.put(k, modelHistogramData.getOrDefault(k, 0) + v);
      });
      allCount += usage.getAllCount();
      freeCount += usage.getFreeCount();
      plusCount += usage.getPlusCount();
    }
    res.put("modelHistogramData", modelHistogramData);
    res.put("allCount", allCount);
    res.put("freeCount", freeCount);
    res.put("plusCount", plusCount);

    //获取免费模型中用户的占比
    //用户名 和对应免费模型的次数
    Map<String, Integer> freeModelHistogramData = new HashMap<>();
    for (UserUsage usage : userUsage) {
      freeModelHistogramData.put(usage.getUserName(), usage.getFreeCount());
    }
    res.put("freeModelHistogramData", freeModelHistogramData);

    //获取付费模型中用户的占比
    //用户名 和对应付费模型的次数
    Map<String, Integer> plusModelHistogramData = new HashMap<>();
    for (UserUsage usage : userUsage) {
      plusModelHistogramData.put(usage.getUserName(), usage.getPlusCount());
    }
    res.put("plusModelHistogramData", plusModelHistogramData);
    return res;
  }


}
