package com.zjs.feishubot.config;

import lombok.RequiredArgsConstructor;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

  @Bean
  RedissonClient redissonClient(Environment environment) {
    Config config = new Config();
    config.setCodec(JsonJacksonCodec.INSTANCE);
    config.useSingleServer().setAddress("redis://" + environment.getProperty("spring.redis.host") + ":" + environment.getProperty("spring.redis.port"));
    config.useSingleServer().setPassword(environment.getProperty("spring.redis.password"));
    return Redisson.create(config);
  }

}
