package com.zjs.feishubot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
@Slf4j
public class ThreadPoolConfig {

  @Bean
  @Qualifier("ioThreadPool")
  public ExecutorService myThreadPool() {
    int processors = Runtime.getRuntime().availableProcessors();
    int corePoolSize = 2 * processors; // 核心线程数
    int maxPoolSize = 4 * processors; // 最大线程数
    long keepAliveTime = 30; // 线程空闲时间（秒）
    int queueCapacity = 100; // 队列大小
    log.info("io线程池初始化：corePoolSize={}, maxPoolSize={}, keepAliveTime={}, queueCapacity={}",
      corePoolSize, maxPoolSize, keepAliveTime, queueCapacity);

    ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS,
      new LinkedBlockingQueue<>(queueCapacity), Executors.defaultThreadFactory(),
      new ThreadPoolExecutor.CallerRunsPolicy());
    //对线程池进行预热
    threadPoolExecutor.prestartAllCoreThreads();

    return threadPoolExecutor;
  }

}
