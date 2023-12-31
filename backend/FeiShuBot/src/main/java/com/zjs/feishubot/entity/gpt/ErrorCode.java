package com.zjs.feishubot.entity.gpt;

import java.util.HashMap;
import java.util.Map;

public class ErrorCode {

  public static final int INVALID_JWT = 401;

  /**
   * 账号繁忙
   */
  public static final int BUSY = 429;
  public static final int INVALID_API_KEY = 3;

  public static final int DETECTED_UNUSUAL_ACTIVITY = 601;

  /**
   * 4.0  3小时25次的对话限制
   */
  public static final int CHAT_LIMIT = 4;

  public static final int RESPONSE_ERROR = 600;

  public static final int ACCOUNT_DEACTIVATED = 6;

  public static final int CONVERSATION_NOT_FOUNT = 404;

  public static final Map<Integer, String> map = new HashMap<>();

  public static final Map<Integer,String> errorCodes = new HashMap<>();

  static {
//    map.put(INVALID_JWT, "无效的access token");
//    map.put(BUSY, "账号繁忙中");
//    map.put(INVALID_API_KEY, "无效的api key");
//    map.put(CHAT_LIMIT, "4.0接口被限制了");
//    map.put(RESPONSE_ERROR, "响应错误");
//    map.put(ACCOUNT_DEACTIVATED, "账号被停用");
    errorCodes.put(401,"账号 token 过期");
    errorCodes.put(429,"账号繁忙中");
    errorCodes.put(404,"未发现该会话");
    errorCodes.put(601,"Our systems have detected unusual activity from your system. Please try again later. 代理异常");
    errorCodes.put(600,"响应错误");
  }


}
