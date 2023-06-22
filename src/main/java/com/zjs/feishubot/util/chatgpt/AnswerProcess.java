package com.zjs.feishubot.util.chatgpt;

import com.zjs.feishubot.entity.gpt.Answer;

public interface AnswerProcess {
    void process(Answer answer) throws Exception;
}
