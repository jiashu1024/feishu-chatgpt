package com.zjs.feishubot.service;

import com.zjs.feishubot.config.KeyGenerateConfig;
import com.zjs.feishubot.config.MyConfig;
import com.zjs.feishubot.entity.Result;
import com.zjs.feishubot.entity.front.Code;
import com.zjs.feishubot.util.JwtUtil;
import com.zjs.feishubot.util.TransparentCaptcha;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoginService {
  protected final RedissonClient redissonClient;
  protected final MyConfig myConfig;

  public Result getCaptcha() {

    TransparentCaptcha captcha = new TransparentCaptcha(130, 48);
    UUID uuid = UUID.randomUUID();
    String key = uuid.toString();
    RBucket<Object> bucket = redissonClient.getBucket(key);


    bucket.set(captcha.text());
    bucket.expire(60, TimeUnit.SECONDS);

    Map<String, String> data = new HashMap<>();
    data.put("key", key);
    data.put("image", captcha.toBase64());
    return Result.success(data);
  }


  public Result login(Map<String, String> form) {
    String key = form.get("key");
    String captcha = form.get("captcha");
    RBucket<Object> bucket = redissonClient.getBucket(key);
    String text = (String) bucket.get();
    if (text == null) {
      return Result.fail("验证码已过期", Code.CAPTCHA_EXPIRED);
    }
    if (!text.equalsIgnoreCase(captcha)) {
      bucket.delete();
      return Result.fail("验证码错误", Code.CAPTCHA_ERROR);
    }
    bucket.delete();

    String username = form.get("username");
    String password = form.get("password");
    if (myConfig.getUserName().equals(username) && myConfig.getPassword().equals(password)) {
      bucket.delete();
      String jwt = JwtUtil.createJWT(username);
      Result success = Result.success("登录成功");
      Map<String, String> data = new HashMap<>();
      data.put("token", jwt);
      success.setData(data);

      RBucket<Boolean> tokenBucket = redissonClient.getBucket(KeyGenerateConfig.getUserLoginStatusKey(username));
      tokenBucket.set(true, 1, TimeUnit.HOURS);
      return success;
    }
    bucket.delete();
    return Result.fail("用户名或密码错误");
  }
}
