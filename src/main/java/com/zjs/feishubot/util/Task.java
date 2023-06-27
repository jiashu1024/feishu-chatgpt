package com.zjs.feishubot.util;

import com.zjs.feishubot.entity.gpt.Answer;
import com.zjs.feishubot.util.chatgpt.AnswerProcess;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Data
@Slf4j
public class Task {
    private AnswerProcess process;
    private Answer answer;
    private String account;

    public Task(AnswerProcess process, Answer answer, String account) {
        this.process = process;
        this.answer = answer;
        this.account = account;
    }

    public void run() {
        try {
            process.process(answer);
        } catch (Exception e) {
            log.error("处理ChatGpt响应出错", e);
            log.error(answer.toString());

        }
    }
}
