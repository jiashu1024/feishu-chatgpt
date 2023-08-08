package com.zjs.feishubot.service;

import com.lark.oapi.Client;
import com.lark.oapi.service.contact.v3.model.GetUserReq;
import com.lark.oapi.service.contact.v3.model.GetUserResp;
import com.lark.oapi.service.contact.v3.model.User;
import com.zjs.feishubot.config.KeyGenerateConfig;
import com.zjs.feishubot.entity.front.UserUsage;
import com.zjs.feishubot.entity.gpt.Models;
import com.zjs.feishubot.util.FeiShuUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
  protected final Client client;

  protected final FeiShuUtil feiShuUtil;

  protected final RedissonClient redissonClient;

  public User getUserByOpenId(String openId) throws Exception {
    // 创建请求对象
    GetUserReq req = GetUserReq.newBuilder()
      .userId(openId)
      .userIdType("open_id")
      .departmentIdType("open_department_id")
      .build();
    // 发起请求
    GetUserResp resp = client.contact().user().get(req);

    // 处理服务端错误
    if (!resp.success()) {
      log.error("code:{},msg:{},reqId:{}"
        , resp.getCode(), resp.getMsg(), resp.getRequestId());
      throw new Exception("获取用户信息失败");
    }
    return resp.getData().getUser();
  }

  public List<User> getAllUser() {
    return feiShuUtil.getAllUser();
  }

  public List<UserUsage> getUserUsage() {
    //获取所有用户
    List<User> allUser = getAllUser();
    Map<String, UserUsage> map = new HashMap<>();
    for (User user : allUser) {
      UserUsage userUsage = new UserUsage();
      userUsage.setUserId(user.getOpenId());
      userUsage.setUserName(user.getName());
      userUsage.setAvatar(user.getAvatar().getAvatarOrigin());
      map.put(user.getOpenId(), userUsage);
    }

    // 1. 获取所有模型
    RSet<String> set = redissonClient.getSet(KeyGenerateConfig.MODEL_SET_KEY);

    Set<String> modelTitle = set.readAll();
    for (String title : modelTitle) {
      // 2. 获取每一个模型的用户使用统计
      RMap<String, Integer> userToCountMap = redissonClient.getMap(KeyGenerateConfig.getUserUsageHashKey(title));
      if (userToCountMap.isExists()) {
        Map<String, Integer> stringIntegerMap = userToCountMap.readAllMap();
        for (String openId : stringIntegerMap.keySet()) {
          UserUsage userUsage = map.get(openId);
          if (userUsage != null) {
            Map<String, Integer> modelUsageCountMap = userUsage.getModelUsageCountMap();
            modelUsageCountMap.put(title, stringIntegerMap.get(openId));
          }
        }
      }
    }

    List<UserUsage> userUsageList = new ArrayList<>(map.values());
    //统计每个用户所有模型的总使用次数
    for (UserUsage userUsage : userUsageList) {
      Map<String, Integer> modelUsageCountMap = userUsage.getModelUsageCountMap();
      for (String model : modelUsageCountMap.keySet()) {
        Integer count = modelUsageCountMap.get(model);
        if (Models.isPlusModel(model)) {
          userUsage.setPlusCount(userUsage.getPlusCount() + count);
        } else {
          userUsage.setFreeCount(userUsage.getFreeCount() + count);
        }
        userUsage.setAllCount(userUsage.getAllCount() + count);
      }
    }

    userUsageList.sort((o1, o2) -> o2.getAllCount() - o1.getAllCount());
    return userUsageList;
  }
}

