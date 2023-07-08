package com.zjs.feishubot.util.chatgpt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RefreshAccountToken {
  @Scheduled(cron = "0 0 0 ? * SUN")
  public void run() {
    log.info("开始刷新账号token");
    AccountUtil.refreshToken();
  }

}
