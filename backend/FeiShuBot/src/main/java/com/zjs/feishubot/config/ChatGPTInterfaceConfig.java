package com.zjs.feishubot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChatGPTInterfaceConfig {


  @Autowired
  private ChatGPTInterfaceConfig(MyConfig myConfig) {
    proxyUrl = myConfig.getProxyUrl();
  }

  public static String proxyUrl;

  public static final String LOGIN_URL = "/chatgpt/login";
  public static final String CHAT_URL = "/chatgpt/backend-api/conversation";
  public static final String LIST_URL = "/chatgpt/backend-api/conversations?offset=0&limit=20";
  public static final String GEN_TITLE_URL = "/chatgpt/backend-api/conversation/gen_title/";
  public static final String ACCOUNT_LEVEL_URL = "/chatgpt/backend-api/models?history_and_training_disabled=false";

}
