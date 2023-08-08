package com.zjs.feishubot.entity;

import lombok.Data;

@Data
public class RequestResult {
  private boolean success;
  private String data;
  private String error;

  public static RequestResult success(String token) {
    RequestResult loginResult = new RequestResult();
    loginResult.setSuccess(true);
    loginResult.setData(token);
    return loginResult;
  }

  public static RequestResult success() {
    RequestResult loginResult = new RequestResult();
    loginResult.setSuccess(true);
    return loginResult;
  }

  public static RequestResult fail(String error) {
    RequestResult loginResult = new RequestResult();
    loginResult.setSuccess(false);
    loginResult.setError(error);
    return loginResult;
  }

}
