package com.zjs.feishubot.entity.gpt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Models {

  /**
   * 默认模型
   */
  public static final String DEFAULT_MODEL = "Default (GPT-3.5)";

  /**
   * 模型title和对应模型
   */
  public static Map<String,Model> modelMap = new HashMap<>();

  /**
   * plus模型title 用于判断请求是否是plus模型
   */
  public static Set<String> plusModelTitle = new HashSet<>();

  /**
   * normal模型title
   */
  public static Set<String> normalModelTitle = new HashSet<>();
}
