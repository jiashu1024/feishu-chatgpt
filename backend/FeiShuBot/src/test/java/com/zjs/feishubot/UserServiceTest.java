package com.zjs.feishubot;

import com.zjs.feishubot.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserServiceTest {

  @Autowired
  private UserService userService;

  @Test
  public void getUserUsage() {
    userService.getUserUsage();
  }
}
