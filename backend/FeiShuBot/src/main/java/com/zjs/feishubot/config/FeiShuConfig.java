package com.zjs.feishubot.config;

import com.lark.oapi.Client;
import com.lark.oapi.sdk.servlet.ext.ServletAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FeiShuConfig {

  protected final MyConfig myConfig;

  @Bean
  public ServletAdapter getServletAdapter() {
    return new ServletAdapter();
  }

  @Bean
  public Client getClient() {
    return Client.newBuilder(myConfig.getAppId(), myConfig.getAppSecret()).build();
  }
}
