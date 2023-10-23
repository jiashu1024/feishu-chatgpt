package com.zjs.feishubot.util.chatgpt;

import cn.hutool.json.JSONUtil;
import com.zjs.feishubot.config.ChatGPTInterfaceConfig;
import com.zjs.feishubot.config.KeyGenerateConfig;
import com.zjs.feishubot.entity.Account;
import com.zjs.feishubot.entity.gpt.*;
import com.zjs.feishubot.service.AccountService;
import com.zjs.feishubot.util.Task;
import com.zjs.feishubot.util.TaskPool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;


@Slf4j
@Component
@RequiredArgsConstructor
public class ChatService {

  protected final RedissonClient redissonClient;

  protected final AccountService accountService;

  //private StringBuilder errorString;

//
//  public boolean build() {
//    if (password == null || password.equals("")) {
//      log.error("账号{}密码为空", account);
//      return false;
//    }
//    log.info("账号{}开始登录", account);
//    String loginUrl = proxyUrl + LOGIN_URL;
//    HashMap<String, Object> paramMap = new HashMap<>();
//    paramMap.put("username", account);
//    paramMap.put("password", password);
//
//    String params = JSONUtil.toJsonPrettyStr(paramMap);
//    String result = HttpUtil.post(loginUrl, params);
//
//    JSONObject jsonObject = new JSONObject(result);
//
//    if (jsonObject.opt("errorMessage") != null) {
//      log.error("账号{}登录失败：{}", account, jsonObject.opt("errorMessage"));
//      return false;
//    }
//    accessToken = jsonObject.optString("accessToken");
//    log.info("账号{}登录成功", account);
//    return true;
//  }
//
//  public String getToken() {
//    return "Bearer " + accessToken;
//  }

  private void chat(String content, String model, AnswerProcess process, String parentMessageId, String conversationId, Account account) throws InterruptedException {

    RLock lock = redissonClient.getLock(KeyGenerateConfig.getLockKey(account.getAccount()));
    lock.lock();

    String createConversationUrl = ChatGPTInterfaceConfig.proxyUrl + ChatGPTInterfaceConfig.CHAT_URL;
    UUID uuid = UUID.randomUUID();
    String messageId = uuid.toString();

    String param = CreateConversationBody.of(messageId, content, parentMessageId, conversationId, model);


//    HttpRequest authorization = HttpRequest.post(createConversationUrl).header("Authorization", account.getToken()).body(param);
//    HttpResponse execute = authorization.execute();
//    System.out.println(execute.getStatus());
//    System.out.println(execute.body());
    request(param, createConversationUrl, process, account);
    //post(param, createConversationUrl, process, account);
    lock.unlock();
  }

  /**
   * 新建会话
   *
   * @param content 对话内容
   * @param model   模型
   * @param process 回调
   */
  public void newChat(String content, String model, AnswerProcess process, Account account) throws InterruptedException {
    chat(content, model, process, "", "", account);
  }

  /**
   * 继续会话
   *
   * @param content         对话内容
   * @param model           模型
   * @param parentMessageId 父消息id
   * @param conversationId  会话id
   * @param process         回调
   */
  public void keepChat(String content, String model, String parentMessageId, String conversationId, AnswerProcess process, Account account) throws InterruptedException {
    chat(content, model, process, parentMessageId, conversationId, account);
  }

  /**
   * @param param   请求参数
   * @param urlStr  请求地址
   * @param process 回答处理器
   * @param account 对应的账号
   */
  private void request(String param, String urlStr, AnswerProcess process, Account account) {
    CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpPost request = new HttpPost(urlStr);

    request.setHeader("Accept", "text/event-stream");
    request.setHeader("Authorization", account.getToken());
    request.setHeader("Content-Type", "application/json");
    request.setEntity(new StringEntity(param, StandardCharsets.UTF_8));

    try (CloseableHttpResponse response = httpClient.execute(request)) {
      // 处理响应
      if (response.getStatusLine().getStatusCode() == 200) {
        // 从响应获取输入流，并处理SSE事件
        try (InputStream inputStream = response.getEntity().getContent()) {
          handleSseEvents(inputStream, process, account);
        }
      } else {
        // 处理错误响应
        log.error("请求失败，状态码：{}", response.getStatusLine().getStatusCode());
        log.error("请求地址:" + urlStr);
        log.error("请求参数：" + param);

        Answer answer = new Answer();
        answer.setFinished(true);
        answer.setSuccess(false);
        answer.setErrorCode(response.getStatusLine().getStatusCode());
        //获取错误信息
        HttpEntity entity = response.getEntity();
        if (entity != null) {
          String responseBody = EntityUtils.toString(entity, "UTF-8");
          answer.setError(responseBody);
          log.error("响应内容：{}", responseBody);
        }
        TaskPool.addTask(new Task(process, answer, account.getAccount()));
        accountService.removeBusyAccount(account.getAccount());
      }
    } catch (IOException e) {
      log.error("请求出错", e);
      // 处理异常
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private void handleSseEvents(InputStream inputStream, AnswerProcess process, Account account) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    String line;

    int count = 0;
    Answer answer;

    while ((line = reader.readLine()) != null) {

      try {
//        log.debug("line:  " + line);
        answer = parse(line, account);
        if (answer == null) {
          continue;
        }
        count++;
        if (!answer.isDeal()) {
          continue;
        }

        answer.setSeq(count);
        //每10次事件推送 才向飞书发送一次 为了防止飞书发消息太快被限制频率
        if (answer.isSuccess() && !answer.isFinished() && count % 10 != 0) {
          continue;
        }
        if (answer.isSuccess() && !answer.getMessage().getAuthor().getRole().equals("assistant")) {
          continue;
        }
        //异步处理
        TaskPool.addTask(new Task(process, answer, account.getAccount()));
      } catch (Exception e) {
        log.error("解析ChatGpt响应出错", e);
        log.error("响应内容：[{}]", line);
      }
    }
    accountService.removeBusyAccount(account.getAccount());
  }


//  /**
//   * 向gpt发起请求
//   *
//   * @param param   请求参数
//   * @param urlStr  请求的地址
//   * @param process 响应处理器
//   */
//  private void post(String param, String urlStr, AnswerProcess process, Account account) {
//    URL url = null;
//    Answer answer = null;
//    try {
//      url = new URL(urlStr);
//      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//      connection.setRequestMethod("POST");
//      connection.setRequestProperty("Accept", "text/event-stream");
//      connection.setRequestProperty("Authorization", account.getToken());
//      connection.setRequestProperty("Content-Type", "application/json");
//      //设置请求体
//      connection.setDoOutput(true);
//
//      try (OutputStream output = connection.getOutputStream()) {
//        output.write(param.getBytes(StandardCharsets.UTF_8));
//      }
//
//
//      // 获取并处理响应
//      int status = connection.getResponseCode();
//      log.info("gpt接口请求状态码：{}", status);
//      Reader streamReader = null;
//      boolean error = false;
//      if (status > 299) {
//        streamReader = new InputStreamReader(connection.getErrorStream());
//        log.error("请求失败，状态码：{}", status);
//        answer = new Answer();
//        answer.setFinished(true);
//        answer.setSuccess(false);
//        answer.setErrorCode(status);
//        answer.setError(ErrorCode.errorCodes.get(status));
//
//        error = true;
//      } else {
//        streamReader = new InputStreamReader(connection.getInputStream());
//      }
//
//      BufferedReader reader = new BufferedReader(streamReader);
//      String line;
//
//      int count = 0;
//
//      StringBuilder sb = new StringBuilder();
//      while (error && (line = reader.readLine()) != null) {
//        sb.append(line);
//      }
//      if (error) {
//        if (answer.getError() == null) {
//          answer.setError(sb.toString());
//        }
//        log.error("请求失败");
//        log.error("请求地址:" + urlStr);
//        log.error("请求参数：" + param);
//        log.error("响应内容：{}", sb);
//        TaskPool.addTask(new Task(process, answer, account.getAccount()));
//      }
//
//      while (!error && (line = reader.readLine()) != null) {
//        if (line.isEmpty()) {
//          continue;
//        }
//        try {
//          count++;
//          answer = parse(line, account);
//
//          if (answer == null) {
//            continue;
//          }
//          if (!answer.isDeal()) {
//            continue;
//          }
//
//          answer.setSeq(count);
//          //每10行 才处理一次 为了防止飞书发消息太快被限制频率
//          if (answer.isSuccess() && !answer.isFinished() && count % 10 != 0) {
//            continue;
//          }
//
//          if (answer.isSuccess() && !answer.getMessage().getAuthor().getRole().equals("assistant")) {
//            continue;
//          }
//
//          //异步处理
//          TaskPool.addTask(new Task(process, answer, account.getAccount()));
//        } catch (Exception e) {
//          log.error("解析ChatGpt响应出错", e);
//          log.error("响应内容：[{}]", line);
//        }
//      }
//
//      //解除账号繁忙状态
//      accountService.removeBusyAccount(account.getAccount());
//
////      if (error) {
////        answer = new Answer();
////        answer.setError(errorString.toString());
////        answer.setErrorCode(ErrorCode.RESPONSE_ERROR);
////        answer.setSuccess(false);
////
////        try {
////          JSONObject jsonObject = new JSONObject(errorString.toString());
////          String detail = jsonObject.optString("detail");
////          if (detail != null) {
////            JSONObject detailObject = new JSONObject(detail);
////            String code = detailObject.optString("code");
////            if (code.equals("account_deactivated")) {
////              answer.setErrorCode(ErrorCode.ACCOUNT_DEACTIVATED);
////            } else if (detail.contains("Only one message")) {
////              log.warn("账号{}忙碌中", account);
////              answer.setErrorCode(ErrorCode.BUSY);
////              answer.setError(detail);
////            }
////
////            if (code.equals("conversation_not_found")) {
////              answer.setErrorCode(ErrorCode.CONVERSATION_NOT_FOUNT);
////              answer.setError(detail);
////            }
////          }
////
////        } catch (JSONException ignored) {
////        }
////        TaskPool.addTask(new Task(process, answer, account.getAccount()));
////      }
//
//      reader.close();
//      connection.disconnect();
//    } catch (Exception e) {
//      log.error("请求出错", e);
//    }
//  }


  private static Answer parse(String body, Account account) {

    Answer answer;

    if (body.equals("data: [DONE]") || body.isEmpty()) {
      return null;
    }
    if (body.startsWith("data:")) {

      body = body.substring(body.indexOf("{"));
      answer = JSONUtil.toBean(body, Answer.class);
      answer.setSuccess(true);
      String error = (String) answer.getError();

      if (("Our systems have detected unusual activity from your system. Please try again later.".equals(error))) {
        answer.setSuccess(false);
        answer.setErrorCode(ErrorCode.DETECTED_UNUSUAL_ACTIVITY);
        answer.setError(ErrorCode.errorCodes.get(ErrorCode.DETECTED_UNUSUAL_ACTIVITY));
        answer.setDeal(true);
        return answer;
      } else if (StringUtils.hasText(error)) {
        answer.setSuccess(false);
        answer.setError(error);
        answer.setErrorCode(ErrorCode.RESPONSE_ERROR);
        answer.setDeal(true);
        return answer;
      }

      Message message1 = answer.getMessage();
      if (message1 == null) {
        answer.setDeal(false);
        return answer;
      } else {
        answer.setDeal(true);
      }

      if (message1.getStatus().equals("finished_successfully")) {
        answer.setFinished(true);
      }
      Message message = answer.getMessage();
      Content content = message.getContent();
      List<String> parts = content.getParts();
      if (parts != null) {
        String part = parts.get(0);
        answer.setAnswer(part);
      }
      if (content.getText() != null) {
        answer.setAnswer(content.getText());
      }

    } else {
      answer = new Answer();
      answer.setSuccess(false);
      JSONObject jsonObject = new JSONObject(body);
      String detail = jsonObject.optString("detail");
      if (detail != null && detail.contains("Only one message")) {
        log.warn("账号{}忙碌中", account);
        answer.setErrorCode(ErrorCode.BUSY);
        answer.setError(detail);
        return answer;
      }
      if (detail != null && detail.contains("code")) {
        JSONObject error = jsonObject.optJSONObject("detail");
        String code = (String) error.opt("code");
        switch (code) {
          case "invalid_jwt":
            answer.setErrorCode(ErrorCode.INVALID_JWT);
            break;
          case "invalid_api_key":
            answer.setErrorCode(ErrorCode.INVALID_API_KEY);
            break;
          case "model_cap_exceeded":
            answer.setErrorCode(ErrorCode.CHAT_LIMIT);
            break;
          case "account_deactivated":
            answer.setErrorCode(ErrorCode.ACCOUNT_DEACTIVATED);
            break;
          case "conversation_not_found":
            answer.setErrorCode(ErrorCode.CONVERSATION_NOT_FOUNT);
            answer.setError(detail);
            break;
          default:
            log.error("未知错误：" + body);
            answer.setError(body);
//                  log.warn("账号{} token失效", account);
            break;
        }
        answer.setError(error.get("message"));
        return answer;
      }
//      log.error("未知错误：{}", body);
//      log.error("账号{}未知错误：{}", account, body);
//      answer.setError(body);
    }
    return answer;
  }

//  public void refreshToken() {
//    build();
//  }

  /**
   * 查询账号可用模型从而判断账号是否plus用户
   *
   * @return 查询是否成功 不成功的原因一般为token失效
   */
//  public boolean queryAccountLevel() {
//    String url = proxyUrl + ACCOUNT_LEVEL_URL;
//    HttpResponse response = HttpRequest.get(url).header("Authorization", getToken()).execute();
//    String body = response.body();
//    JSONObject jsonObject = new JSONObject(body);
//    String models = jsonObject.optString("models");
//    if (models == null || models.length() == 0) {
//      log.warn("账号{}查询模型解析失败 : {}", account, body);
//      return false;
//    }
//    JSONArray objects = JSONUtil.parseArray(models);
//    List<Model> list = JSONUtil.toList(objects, Model.class);
//    boolean plus = false;
//    for (Model model : list) {
//      Models.modelMap.put(model.getTitle(), model);
//      if (model.getSlug().startsWith("gpt-4")) {
//        Models.plusModelTitle.add(model.getTitle());
//        plus = true;
//      } else {
//        Models.normalModelTitle.add(model.getTitle());
//      }
//    }
//    this.setLevel(plus ? 4 : 3);
//    return true;
//  }
}
