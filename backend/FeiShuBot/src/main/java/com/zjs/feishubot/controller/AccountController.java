package com.zjs.feishubot.controller;

import com.zjs.feishubot.entity.Account;
import com.zjs.feishubot.entity.Result;
import com.zjs.feishubot.entity.front.AccountStatistics;
import com.zjs.feishubot.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@ResponseBody
@Slf4j
@RequiredArgsConstructor
public class AccountController {

  protected final AccountService accountService;

  @PostMapping("/modifyAccount")
  public Result modifyAccount(@RequestBody Account account) {
    log.info("modify account: {}", account);
    return accountService.modifyAccount(account);
  }

  @PostMapping("/addAccount")
  public Result addAccount(@RequestBody Account account) {
    log.info("add account: {}", account);
    return accountService.addAccount(account);
  }

  @PostMapping("/deleteAccount")
  public Result deleteAccount(@RequestBody Account account) {
    log.info("delete account: {}", account);
    return accountService.deleteAccount(account);
  }

  @GetMapping("/allAccount")
  public List<Account> getAllAccount() {
    List<Account> accounts = accountService.getAllAccountNoCheck();
    accounts.sort((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()));
    return accounts;
  }

  @GetMapping("/accountStatistics")
  public AccountStatistics getAccountStatistics() {
    return accountService.getAccountStatistics();
  }
}
