package com.zjs.feishubot.entity;

import lombok.Data;

@Data
/**
 * 返回给前端的结果
 */
public class Result {
  private boolean success;
  private int code;
  private Object data;
  private String error;
  private String message;

  public static Result success(String message) {
    Result result = new Result();
    result.setSuccess(true);
    result.setMessage(message);
    return result;
  }

  public static Result fail(String error, int code) {
    Result result = new Result();
    result.setSuccess(false);
    result.setError(error);
    result.setCode(code);
    return result;
  }

  public static Result success(Object data) {
    Result result = new Result();
    result.setSuccess(true);
    result.setData(data);
    return result;
  }

  public static Result success() {
    Result result = new Result();
    result.setSuccess(true);
    return result;
  }

  public static Result fail(String error) {
    Result result = new Result();
    result.setSuccess(false);
    result.setError(error);
    return result;
  }

  @Override
  public String toString() {
    return "Result{" +
      "success=" + success +
      ", data=" + data +
      ", error='" + error + '\'' +
      ", message='" + message + '\'' +
      '}';
  }
}
