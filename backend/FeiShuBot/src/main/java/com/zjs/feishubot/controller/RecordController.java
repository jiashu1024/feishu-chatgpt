package com.zjs.feishubot.controller;

import com.zjs.feishubot.entity.QuestionSummary;
import com.zjs.feishubot.entity.Record;
import com.zjs.feishubot.service.RecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
@ResponseBody
public class RecordController {


  protected final RecordService recordService;

  /**
   * 获取最近几天的提问统计
   *
   * @param days 天数 (包括今天)
   * @return
   */
  @GetMapping("/getQuestionSummary")
  public List<QuestionSummary> getQuestionSummary(int days) {
    if (days <= 0) {
      return null;
    }
    return recordService.getQuestionSummary(days);
  }

  @GetMapping("/getRecord")
  public List<Record> getRecord(int cursor) {
    List<Record> list = recordService.getRecord(cursor, 10);
    return list;
  }
}
