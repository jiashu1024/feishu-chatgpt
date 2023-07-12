package com.zjs.feishubot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FeiShuBotApplication {

  public static void main(String[] args) {
    SpringApplication.run(FeiShuBotApplication.class, args);
  }

}
