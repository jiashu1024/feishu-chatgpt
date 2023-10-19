package com.zjs.feishubot.listener;

import com.zjs.feishubot.entity.Account;
import com.zjs.feishubot.service.AccountService;
import com.zjs.feishubot.util.TaskPool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitProjectListener implements ApplicationListener<ContextRefreshedEvent> {

  protected final AccountService accountService;

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    log.info("version : v2.0.5");
    List<Account> accounts = accountService.getAllAccountCheck();
    // List<Account> availableAccounts = accountService.getAvailableAccounts();
    int plusCount = 0;
    int normalCount = 0;
    int availableCount = 0;
    for (Account account : accounts) {
      if (account.isAvailable()) {
        availableCount++;
        if (account.isPlusAccount()) {
          plusCount++;
        } else {
          normalCount++;
        }
      } else {
        log.error("账号不可用：{}", account);
      }
    }

    log.info("当前可用账号数量：{}", availableCount);
    log.info("当前可用plus账号数量：{}", plusCount);
    log.info("当前可用普通账号数量：{}", normalCount);
    TaskPool.init(accounts);
    TaskPool.runTask();
  }
}
