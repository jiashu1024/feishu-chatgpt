package com.zjs.feishubot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "my-config")
public class MyConfig {

  private String appId;
  private String appSecret;
  private String encryptKey;
  private String verificationToken;
  private String proxyUrl;
  private String userName;
  private String password;
}
