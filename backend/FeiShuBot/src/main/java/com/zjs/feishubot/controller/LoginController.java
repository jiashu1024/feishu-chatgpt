package com.zjs.feishubot.controller;

import com.zjs.feishubot.entity.Result;
import com.zjs.feishubot.service.LoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@Slf4j
@RequiredArgsConstructor
@ResponseBody
public class LoginController {

  protected final LoginService loginService;

  @GetMapping("/getCaptcha")
  public Result getCaptcha() {
    return loginService.getCaptcha();
  }

  @PostMapping("/login")
  public Result login(@RequestBody Map<String, String> form) {
    return loginService.login(form);
  }
}
