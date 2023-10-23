package com.zjs.feishubot.config;

public class KeyGenerateConfig {

  private static final String prefix = "chatgpt:";

  /**
   * 装所有账号的hash的key
   */
  public static final String ACCOUNTS_KEY = prefix + "accounts";

  /**
   * 记录按时间戳排序的记录id的z set的key
   */
  public static final String RECORD_ID_SET_KEY = prefix + "recordIdSet";

  /**
   * 记录id和记录的hash的key
   */
  public static final String RECORDS_KEY = prefix + "records";

  public static final String MODEL_SET_KEY = prefix + "modelSet";


  /**
   * 账号token的key
   */
  public static String getAccountTokenKey(String account) {
    return prefix + "token:" + account;
  }

  /**
   * lock的key
   */
  public static String getLockKey(String account) {
    return prefix + "lock:" + account;
  }

  public static String getUserUsageHashKey(String modelTitle) {
    return prefix + "userUsage:" + modelTitle;
  }


  public static String getUserLoginStatusKey(String userName) {
    return prefix + "login:" + userName;
  }
}
