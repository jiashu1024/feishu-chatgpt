package com.zjs.feishubot;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.lark.oapi.service.contact.v3.model.User;
import com.zjs.feishubot.entity.Account;
import com.zjs.feishubot.service.AccountService;
import com.zjs.feishubot.util.FeiShuUtil;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;


@SpringBootTest
class FeiShuBotApplicationTests {

  @Autowired
  private RedissonClient redissonClient;

  @Autowired
  private AccountService accountService;

  @Autowired
  private FeiShuUtil feiShuUtil;

  @Test
  void prepareAccountData() {



    List<Account> accounts = accountService.getAllAccountNoCheck();
    for (Account account : accounts) {
      account.setFreePublic(true);

//      User adminUser = feiShuUtil.getAdminUser();
//      account.setOwnerOpenId(adminUser.getOpenId());
//      account.setOwnerUserName(adminUser.getName());
//      if (account.isPlusAccount()) {
//        account.setPlusPublic(false);
//        account.addPlusPublicUser(adminUser.getOpenId());
////       account.setPlusPublicUsers(null);
//      }
      if (account.getCreateTime() == null) {
        account.setCreateTime(DateTime.now().toTimestamp());
      }
      accountService.updateAccountInfo(account);
    }


  }


  @Test
  public void getAllUser() {
    List<User> allUser = feiShuUtil.getAllUser();
    for (User user : allUser) {
      System.out.println(user);
    }
  }

  @Test
  public void test() {
    DateTime date = DateUtil.date();
    DateTime dateTime = DateUtil.beginOfDay(date);
    DateTime agoTime = DateUtil.offsetDay(dateTime, -7);
    System.out.println(agoTime);
  }
}
