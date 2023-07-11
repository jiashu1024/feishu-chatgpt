package com.zjs.feishubot.util.chatgpt;

import com.zjs.feishubot.entity.Account;
import com.zjs.feishubot.entity.Status;
import com.zjs.feishubot.entity.gpt.Models;
import com.zjs.feishubot.service.AccountService;
import com.zjs.feishubot.util.TaskPool;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 账号池
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Data

public class AccountPool {
  protected final AccountService accountService;

  protected final Environment environment;

  private int size;

  @Value("${proxy.url}")
  private String proxyUrl;

  public static Map<String, ChatService> accountPool = new HashMap<>();

  public static Map<String, ChatService> normalPool = new HashMap<>();

  public static Map<String, ChatService> plusPool = new HashMap<>();

  /**
   * 初始化账号池
   */
  @PostConstruct
  public void init() {
    List<Account> accounts = accountService.getAccounts();
    List<String> usefulAccounts = new ArrayList<>();
    for (Account account : accounts) {
      ChatService chatService = new ChatService(account.getAccount(), account.getPassword(), account.getToken(), proxyUrl);
      if (account.getToken() == null) {
        boolean ok = chatService.build();
        if (ok) {
          account.setToken(chatService.getAccessToken());
          accountService.updateTokenForAccount(account.getAccount(), chatService.getAccessToken());

        } else {
          //ChatGpt登录失败
          log.error("账号{}登录失败", account.getAccount());
          continue;
        }
      }

      //查询账号是否plus用户
      boolean b = chatService.queryAccountLevel();
      //如果token失效，重新登录，更新token，重新查询一次
      if (!b) {
        boolean ok = chatService.build();
        if (ok) {
          //重新登录成功
          account.setToken(chatService.getAccessToken());
          accountService.updateTokenForAccount(account.getAccount(), chatService.getAccessToken());
          b = chatService.queryAccountLevel();
          if (!b) {
            continue;
          }
        } else {
          //ChatGpt登录失败
          log.error("账号{}登录失败", account.getAccount());
          continue;
        }
      }



      usefulAccounts.add(account.getAccount());
      accountPool.put(account.getAccount(), chatService);
      size++;
      if (chatService.getLevel() == 3) {
        log.info("账号{}为normal用户", account.getAccount());
        normalPool.put(account.getAccount(), chatService);
      }
      if (chatService.getLevel() == 4) {
        log.info("账号{}为plus用户", account.getAccount());
        plusPool.put(account.getAccount(), chatService);
      }
    }

    log.info("normal账号池共{}个账号", normalPool.size());
    log.info("plus账号池共{}个账号", plusPool.size());
    TaskPool.init(usefulAccounts);
    TaskPool.runTask();
  }

  public ChatService getFreeChatService(String model) {

    List<ChatService> plusAccountList = new ArrayList<>();
    for (String s : plusPool.keySet()) {
      ChatService chatService = plusPool.get(s);
      if (chatService.getStatus() == Status.FINISHED) {
        plusAccountList.add(chatService);
      }
    }

    if (plusAccountList.size() > 0) {
      //无论什么模型，都优先使用plus账号
      return plusAccountList.get((int) (Math.random() * plusAccountList.size()));
    }

    if (Models.plusModelTitle.contains(model)) {
      //如果是plus模型，但是没有plus账号，返回null
      return null;
    }
    List<ChatService> normalAccountList = new ArrayList<>();
    for (String s : normalPool.keySet()) {
      ChatService chatService = normalPool.get(s);
      if (chatService.getStatus() == Status.FINISHED) {
        normalAccountList.add(chatService);
      }
    }
    if(normalAccountList.size() == 0) {
      return null;
    } else {
      return normalAccountList.get((int) (Math.random() * normalAccountList.size()));
    }
  }

  public void modifyChatService(ChatService chatService) {
    log.info("修改账号{}信息", chatService.getAccount());
    Account account = new Account();
    account.setAccount(chatService.getAccount());
    account.setToken(chatService.getAccessToken());
    account.setPassword(chatService.getPassword());
    accountService.updateTokenForAccount(chatService.getAccount(), chatService.getAccessToken());
  }

  public ChatService getChatService(String account) {
    if (account == null || account.equals("")) {
      if (plusPool.containsKey(account)) {
        return getFreeChatService(Models.PLUS_DEFAULT_MODEL);
      } else {
        return getFreeChatService(Models.NORMAL_DEFAULT_MODEL);
      }
    }
    return accountPool.get(account);
  }

}
