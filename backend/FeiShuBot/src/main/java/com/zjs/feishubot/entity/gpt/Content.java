package com.zjs.feishubot.entity.gpt;

import lombok.Data;

import java.util.List;

@Data
public class Content {
  private String content_type;
  private List<String> parts;
  private String text;

  @Override
  public String toString() {
    return "Content{" +
      "content_type='" + content_type + '\'' +
      ", parts=" + parts.get(0).length() +
      ", text='" + text + '\'' +
      '}';
  }
}
