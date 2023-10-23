package com.zjs.feishubot.util;

import com.lark.oapi.Client;
import com.lark.oapi.core.request.RequestOptions;
import com.lark.oapi.service.contact.v3.model.FindByDepartmentUserReq;
import com.lark.oapi.service.contact.v3.model.FindByDepartmentUserResp;
import com.lark.oapi.service.contact.v3.model.User;
import com.zjs.feishubot.config.MyConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeiShuUtil {

  protected final MyConfig myConfig;
  protected final Client client;

  public List<User> getAllUser() {
    List<User> users = new ArrayList<>();
    FindByDepartmentUserReq req = FindByDepartmentUserReq.newBuilder()
      .userIdType("open_id")
      .departmentIdType("open_department_id")
      .departmentId("0")
      .pageSize(50)
      .build();
    try {
      FindByDepartmentUserResp resp = client.contact().user().findByDepartment(req, RequestOptions.newBuilder().build());
      if (resp.success()) {
        return Arrays.asList(resp.getData().getItems());
      } else {
        log.error("获取用户列表失败 code : {} msg : {}", resp.getCode(), resp.getMsg());
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return users;
  }

  public String getAdminUserId() {
    List<User> allUser = getAllUser();
    for (User user : allUser) {
      if (user.getIsTenantManager()) {
        return user.getUserId();
      }
    }
    return null;
  }

  public User getAdminUser() {
    List<User> allUser = getAllUser();
    for (User user : allUser) {
      if (user.getIsTenantManager()) {
        return user;
      }
    }

    return null;
  }

  public List<User> getUserExceptUserId(String userId) {
    List<User> allUser = getAllUser();
    List<User> users = new ArrayList<>();
    for (User user : allUser) {
      if (!user.getUserId().equals(userId)) {
        users.add(user);
      }
    }
    return users;
  }

  public List<User> getUsersExceptAdmin() {
    List<User> allUser = getAllUser();
    List<User> users = new ArrayList<>();
    for (User user : allUser) {
      if (!user.getIsTenantManager()) {
        users.add(user);
      }
    }
    return users;
  }


}
