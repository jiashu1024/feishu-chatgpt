package com.zjs.feishubot.config;

public class KeyGenerateConfig {

  /**
   * 装所有账号的hash的key
   */
  public static final String ACCOUNTS_KEY = "accounts";

  /**
   * 记录按时间戳排序的记录id的z set的key
   */
  public static final String RECORD_ID_SET_KEY = "recordIdSet";

  /**
   * 记录id和记录的hash的key
   */
  public static final String RECORDS_KEY = "records";

  public static final String MODEL_SET_KEY = "modelSet";


  /**
   * 账号token的key
   */
  public static String getAccountTokenKey(String account) {
    return "token:" + account;
  }

  /**
   * lock的key
   */
  public static String getLockKey(String account) {
    return "lock:" + account;
  }

  public static String getUserUsageHashKey(String modelTitle) {
    return "userUsage:" + modelTitle;
  }


  public static String getUserLoginStatusKey(String userName) {
    return "login:" + userName;
  }
}
