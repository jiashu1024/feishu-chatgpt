package com.zjs.feishubot.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.zjs.feishubot.config.ChatGPTInterfaceConfig;
import com.zjs.feishubot.config.KeyGenerateConfig;
import com.zjs.feishubot.entity.Account;
import com.zjs.feishubot.entity.RequestResult;
import com.zjs.feishubot.entity.Result;
import com.zjs.feishubot.entity.front.AccountStatistics;
import com.zjs.feishubot.entity.gpt.Model;
import com.zjs.feishubot.entity.gpt.Models;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

  protected final RedissonClient redissonClient;

  @Resource
  private ExecutorService ioThreadPool;

  public boolean isBusy(String account) {
    Account accountInfo = getAccount(account);
    if (accountInfo == null) {
      return true;
    } else {
      return accountInfo.isRunning();
    }
  }

  public void checkAccountsIsBusyAsync(List<Account> accounts) {
    List<CompletableFuture<Void>> futures = new ArrayList<>();
    for (Account account : accounts) {
      CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
        Account accountInfo = getAccount(account.getAccount());
        if (accountInfo == null) {
          account.setRunning(true);
        } else {
          account.setRunning(accountInfo.isRunning());
        }
      }, ioThreadPool);
      futures.add(future);
    }
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
  }

  public void addBusyAccount(String account) {
    Account accountInfo = getAccount(account);
    accountInfo.setRunning(true);
    updateAccountInfo(accountInfo);
  }

  public void removeBusyAccount(String account) {
    Account accountInfo = getAccount(account);
    accountInfo.setRunning(false);
    updateAccountInfo(accountInfo);
  }


  public Account getAccount(String account) {
    RMap<Object, Object> accountsMap = redissonClient.getMap(KeyGenerateConfig.ACCOUNTS_KEY);
    if (!accountsMap.isExists()) {
      return null;
    } else {
      Account res = (Account) accountsMap.get(account);
      if (res == null) {
        return null;
      }
      res.setToken(getToken(account));
      return res;
    }
  }

  public Result modifyAccount(Account account) {

    if (account == null || account.getAccount() == null || account.getPassword() == null) {
      log.error("account or password is null");
      return Result.fail("账号或密码为空");
    }
    Account accountInfo = getAccount(account.getAccount());
    if (accountInfo == null) {
      log.error("account not exist");
      return Result.fail("账号不存在");
    }

    if (!account.getPassword().equals(accountInfo.getPassword())) {
      account = build(account, false);
    }

    if (!account.isAvailable()) {
      log.error(account.getError());
      return Result.fail(account.getError());
    }
    queryAccountLevel(account);
    if (!account.isAvailable()) {
      log.error(account.getError());
      return Result.fail(account.getError());
    }
    updateAccountInfo(account);
    log.info("modify account success");
    return Result.success("修改成功");
  }

  public Result addAccount(Account account) {
    if (account == null || account.getAccount() == null || account.getPassword() == null) {
      log.error("account or password is null");
      return Result.fail("账号或密码为空");
    }
    Account accountInfo = getAccount(account.getAccount());
    if (accountInfo != null) {
      log.error("account already exist");
      return Result.fail("账号已经存在");
    }
    account = build(account, true);
    if (!account.isAvailable()) {
      log.error(account.getError());
      return Result.fail("账号或者密码错误");
    }

    queryAccountLevel(account);
    if (!account.isAvailable()) {
      log.error(account.getError());
      return Result.fail(account.getError());
    }
    if (account.getCreateTime() == null) {
      account.setCreateTime(DateTime.now().toTimestamp());
    }
    updateAccountInfo(account);


    log.info("add account success");
    return Result.success("添加成功");
  }

  public Result deleteAccount(Account account) {
    if (account == null || account.getAccount() == null) {
      log.error("account is null");
      return Result.fail("账号为空");
    }
    Account accountInfo = getAccount(account.getAccount());
    if (accountInfo == null) {
      log.error("account not exist");
      return Result.fail("账号不存在");
    }
    deleteAccountInfo(account.getAccount());
    log.info("delete account success");
    return Result.success("删除成功");
  }

  private void deleteAccountInfo(String account) {
    RMap<Object, Object> accountsMap = redissonClient.getMap(KeyGenerateConfig.ACCOUNTS_KEY);
    accountsMap.remove(account);
  }

  /**
   * 检查账号的plus或者normal权限是否对该user开放
   *
   * @param userOpenId            用户
   * @param account               gpt账号
   * @param isPlusModelPermission plus或者normal权限
   * @return
   */
  public boolean checkAccountPermission(String userOpenId, Account account, boolean isPlusModelPermission) {
    if (isPlusModelPermission) {
      if (account.isPlusPublic()) {
        return true;
      } else {
        Set<String> plusPublicUsers = account.getPlusPublicUsers();
        if (plusPublicUsers == null) {
          return false;
        }
        return plusPublicUsers.contains(userOpenId);
      }
    } else {
      if (account.isFreePublic()) {
        return true;
      } else {
        Set<String> normalPublicUsers = account.getFreePublicUsers();
        if (normalPublicUsers == null) {
          return false;
        }
        return normalPublicUsers.contains(userOpenId);
      }
    }
  }

  /**
   * 根据需要的模型title获取一个有权限的空闲账号,并检查账号的可用性
   *
   * @param modelTitle
   * @return
   */
  public Account getFreeAccountByModelAndCheck(String modelTitle, String userOpenId) {

//    1. 先查数据库的所有的账号
    List<Account> allAccountNoCheck = getAllAccountNoCheck();
//    2. 有权限的
    boolean plus = Models.plusModelTitle.contains(modelTitle);
    List<Account> havaPermissionAccounts = new ArrayList<>();
    for (Account account : allAccountNoCheck) {
      if (checkAccountPermission(userOpenId, account, plus)) {
        havaPermissionAccounts.add(account);
      }
    }
//    3. 空闲的
    List<Account> freeAccounts = new ArrayList<>();
    checkAccountsIsBusyAsync(havaPermissionAccounts);
    for (Account havaPermissionAccount : havaPermissionAccounts) {
      if (!havaPermissionAccount.isRunning()) {
        freeAccounts.add(havaPermissionAccount);
      }
    }


//    4. 检查等级 进行等级查询和可用性检查 筛选可用的

    queryAccountLevelAsync(freeAccounts);

    List<Account> availableAccounts = new ArrayList<>();
    for (Account freeAccount : freeAccounts) {
      if (freeAccount.isAvailable()) {
        availableAccounts.add(freeAccount);
      }
    }
    //等级查询，可用性检查后，需要和数据库同步
    updateAccountInfos(freeAccounts);

//    5. 如果需要plus模型，只能从plus账号池里选 (如果有自己配置的号，优先选择自己的号)
    List<Account> plusAccounts = new ArrayList<>();
    for (Account available : availableAccounts) {
      if (available.isPlusAccount()) {
        plusAccounts.add(available);
      }
    }

    if (plus) {
      //需要plus模型，则只能从plus账号池里选
      if (plusAccounts.size() > 0) {
        //优先选择自己配置的号
        for (Account plusAccount : plusAccounts) {
          if (plusAccount.getOwnerOpenId().equals(userOpenId)) {
            return plusAccount;
          }
        }
        //没有则随机选择一个
        return plusAccounts.get((int) (Math.random() * plusAccounts.size()));
      } else {
        return null;
      }
    } else {
      //不是plus模型，先选plus账号的，如果没有再选normal账号的
      if (plusAccounts.size() > 0) {
        //优先自己配置的
        for (Account plusAccount : plusAccounts) {
          if (plusAccount.getOwnerOpenId().equals(userOpenId)) {
            return plusAccount;
          }
        }
        //随机选择一个
        return plusAccounts.get((int) (Math.random() * plusAccounts.size()));
      } else {
        if (freeAccounts.size() > 0) {
          //优先自己配置的
          for (Account freeAccount : freeAccounts) {
            if (freeAccount.getOwnerOpenId().equals(userOpenId)) {
              return freeAccount;
            }
          }
          //没有就随机选一个
          return freeAccounts.get((int) (Math.random() * freeAccounts.size()));
        } else {
          return null;
        }
      }
    }
  }

  /**
   * 获取所有账号，不进行可用性，等级检查
   *
   * @return
   */
  public List<Account> getAllAccountNoCheck() {
    List<Account> accounts = new ArrayList<>();
    RMap<Object, Object> accountsMap = redissonClient.getMap(KeyGenerateConfig.ACCOUNTS_KEY);
    if (accountsMap.isEmpty()) {
      return accounts;
    }
    for (Object accountObj : accountsMap.values()) {
      Account account = (Account) accountObj;
      account.setToken(getToken(account.getAccount()));
      accounts.add(account);
    }
    return accounts;
  }

  public List<Account> getAllAccountCheck() {
    List<Account> accounts = new ArrayList<>();
    RMap<Object, Object> accountsMap = redissonClient.getMap(KeyGenerateConfig.ACCOUNTS_KEY);
    if (accountsMap.isEmpty()) {
      return accounts;
    }
    for (Object accountObj : accountsMap.values()) {
      Account account = (Account) accountObj;
      String token = getToken(account.getAccount());
      account.setToken(token);
      accounts.add(account);
    }

    queryAccountLevelAsync(accounts);
    updateAccountInfos(accounts);
    return accounts;
  }


  public void updateTokenForAccount(String account, String accessToken) {
    RBucket<Object> bucket = redissonClient.getBucket(KeyGenerateConfig.getAccountTokenKey(account));
    bucket.setAsync(accessToken, 1, TimeUnit.DAYS);
  }

  public void updateAccountInfo(Account accountInfo) {
    RMap<Object, Object> accountsMap = redissonClient.getMap(KeyGenerateConfig.ACCOUNTS_KEY);
    accountsMap.putAsync(accountInfo.getAccount(), accountInfo);
  }

  public void updateAccountInfos(List<Account> accountInfos) {
    RMap<String, Account> accountsMap = redissonClient.getMap(KeyGenerateConfig.ACCOUNTS_KEY);
    Map<String, Account> map = new HashMap<>();
    for (Account accountInfo : accountInfos) {
      map.put(accountInfo.getAccount(), accountInfo);
    }
    accountsMap.putAllAsync(map);
  }


  public void queryAccountLevelAsync(List<Account> accounts) {

    // 创建一个列表来存储所有的CompletableFuture
    List<CompletableFuture<Void>> futures = new ArrayList<>();

    for (Account account : accounts) {
      CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
        queryAccountLevel(account);
      }, ioThreadPool);
      futures.add(future);
    }

    // 等待所有的CompletableFuture都完成
    CompletableFuture<Void> allFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    allFuture.join();
  }


  /**
   * 查询账号等级
   * 将查询结果更新到account对象中
   *
   * @param account
   * @return 是否查询成功
   */
  public void queryAccountLevel(Account account) {

    String url = ChatGPTInterfaceConfig.proxyUrl + ChatGPTInterfaceConfig.ACCOUNT_LEVEL_URL;

    String token = account.getToken();
    if (account.getToken() == null) {
      token = getToken(account.getAccount());
      if (token == null) {
        build(account, false);
      }
    }

    HttpResponse response = HttpRequest.get(url).header("Authorization", token).execute();
    String body = response.body();
    JSONObject jsonObject = new JSONObject(body);
    String models = jsonObject.optString("models");
    if (models == null || models.length() == 0) {
      log.warn("账号{}查询模型解析失败 : {}", account.getAccount(), body);
      JSONObject detail = jsonObject.optJSONObject("detail");
      if (detail != null) {
        String message = detail.optString("message");
        account.setError(message);
      } else {
        account.setError("查询模型解析失败" + body);
      }
      account.setAvailable(false);
      return;
    }

    JSONArray objects = JSONUtil.parseArray(models);


    List<Model> list = JSONUtil.toList(objects, Model.class);
    boolean plus = false;
    for (Model model : list) {
      Models.modelMap.put(model.getTitle(), model);
      if (model.getSlug().startsWith("gpt-4")) {
        Models.plusModelTitle.add(model.getTitle());
        plus = true;
      } else {
        Models.normalModelTitle.add(model.getTitle());
      }
    }
    account.setPlusAccount(plus);
    account.setAvailable(true);

  }

  public RequestResult login(String account, String password) {
    log.debug("账号{}开始登录", account);
    String loginUrl = ChatGPTInterfaceConfig.proxyUrl + ChatGPTInterfaceConfig.LOGIN_URL;
    HashMap<String, Object> paramMap = new HashMap<>();
    paramMap.put("username", account);
    paramMap.put("password", password);

    String params = JSONUtil.toJsonPrettyStr(paramMap);
    String result = HttpUtil.post(loginUrl, params);

    JSONObject jsonObject = new JSONObject(result);

    if (jsonObject.opt("errorMessage") != null) {
      log.error("账号{}登录失败：{}", account, jsonObject.opt("errorMessage"));
      return RequestResult.fail(jsonObject.opt("errorMessage").toString());
    }
    log.info("账号{}登录成功", account);
    return RequestResult.success(jsonObject.opt("accessToken").toString());
  }

  public Account build(Account account, boolean registry) {

    RequestResult login = login(account.getAccount(), account.getPassword());
    if (login.isSuccess()) {
      account.setToken("Bearer " + login.getData());
      updateTokenForAccount(account.getAccount(), account.getToken());
      account.setAvailable(true);
    } else {
      account.setError(login.getError());
      account.setAvailable(false);
    }

    if (!registry) {
      updateAccountInfo(account);
    }
    return account;
  }

  public String getToken(String account) {
    return (String) redissonClient.getBucket(KeyGenerateConfig.getAccountTokenKey(account)).get();
  }

  public List<Account> getPlusAccountList(String userId) {
    List<Account> accounts = getAllAccountNoCheck();
    List<Account> plusAccounts = new ArrayList<>();
    for (Account account : accounts) {
      if (account.isAvailable() && account.isPlusAccount() && checkAccountPermission(userId, account, true)) {
        plusAccounts.add(account);
      }
    }
    return plusAccounts;
  }

  /**
   * 获取账号统计信息
   * 因为前端高频3秒请求一次，使前端能快速感知账号状态变化
   * 获取所有账号时，三分之一的请求，会去openai查询账号真实状态，进行限流
   *
   * @return
   */
  public AccountStatistics getAccountStatistics() {
    int random = RandomUtil.randomInt(0, 100);
    List<Account> accounts;
    if (random < 30) {
      accounts = getAllAccountCheck();
    } else {
      accounts = getAllAccountNoCheck();
    }
    AccountStatistics accountStatistics = new AccountStatistics();
    for (Account account : accounts) {
      if (account.isPlusAccount()) {
        if (account.isAvailable()) {
          accountStatistics.setPlusAccountAvailableNum(accountStatistics.getPlusAccountAvailableNum() + 1);
        }
        if (account.isPlusPublic()) {
          accountStatistics.setPlusAccountPlusPublicNum(accountStatistics.getPlusAccountPlusPublicNum() + 1);
        }

        if (account.isFreePublic()) {
          accountStatistics.setPlusAccountFreePublicNum(accountStatistics.getPlusAccountFreePublicNum() + 1);
        }
        accountStatistics.setPlusAccountNum(accountStatistics.getPlusAccountNum() + 1);
      } else {
        if (account.isAvailable()) {
          accountStatistics.setFreeAccountAvailableNum(accountStatistics.getFreeAccountAvailableNum() + 1);
        }
//        if (account.isPlusPublic()) {
//          accountStatistics.setNormalAccountPlusPublicNum(accountStatistics.getNormalAccountPlusPublicNum() + 1);
//        }
        if (account.isFreePublic()) {
          accountStatistics.setFreeAccountFreePublicNum(accountStatistics.getFreeAccountFreePublicNum() + 1);
        }
        accountStatistics.setFreeAccountNum(accountStatistics.getFreeAccountNum() + 1);
      }
    }
    return accountStatistics;
  }

//  public List<Account> getNormalAccountList(String userId) {
//    List<Account> accounts = getAllAccount();
//    List<Account> normalAccounts = new ArrayList<>();
//    for (Account account : accounts) {
//      if (account.isAvailable() && !account.isPlusAccount() && checkAccountPermission(userId, account, false)) {
//        normalAccounts.add(account);
//      }
//    }
//    return normalAccounts;
//  }
}
