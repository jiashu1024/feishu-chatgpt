package com.zjs.feishubot.util.chatgpt;

import com.zjs.feishubot.entity.Account;
import com.zjs.feishubot.entity.Model;
import com.zjs.feishubot.entity.Status;
import com.zjs.feishubot.service.AccountService;
import com.zjs.feishubot.util.TaskPool;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 账号池
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Data

public class AccountPool {
    protected final AccountService accountService;

    protected final Environment environment;

    private int size;

    @Value("${proxy.url}")
    private String proxyUrl;

    public static Map<String, ChatService> accountPool = new HashMap<>();

    public static Map<String, ChatService> normalPool = new HashMap<>();

    public static Map<String, ChatService> plusPool = new HashMap<>();

    /**
     * 初始化账号池
     */
    @PostConstruct
    public void init() {
        List<Account> accounts = accountService.getAccounts();
        List<String> usefulAccounts = new ArrayList<>();
        for (Account account : accounts) {

            ChatService chatService = new ChatService(account.getAccount(), account.getPassword(), account.getToken(), proxyUrl);
            chatService.setLevel(Integer.parseInt(account.getLevel()));
            if (account.getToken() == null) {
                boolean ok = chatService.build();
                if (ok) {
                    account.setToken(chatService.getAccessToken());
                    accountService.updateTokenForAccount(account.getAccount(), chatService.getAccessToken());
                } else {
                    //ChatGpt登录失败
                    log.error("账号{}登录失败", account.getAccount());
                    continue;
                }
            }
            usefulAccounts.add(account.getAccount());
            accountPool.put(account.getAccount(), chatService);
            size++;
            if (account.getLevel().equals("3")) {
                normalPool.put(account.getAccount(), chatService);
            }
            if (account.getLevel().equals("4")) {
                plusPool.put(account.getAccount(), chatService);
            }
        }
        TaskPool.init(usefulAccounts);
        TaskPool.runTask();
        log.info("账号池初始化完成 {}", accountPool.size());

    }

    public ChatService getFreeChatService(Model model) {
        for (String s : plusPool.keySet()) {
            ChatService chatService = plusPool.get(s);
            if (chatService.getStatus() == Status.FINISHED) {
                return chatService;
            }
        }
        if (model == Model.PLUS_4_DEFAULT || model == Model.PLUS_GPT_4_BROWSING
                || model == Model.PLUS_GPT_4_MOBILE) {
            return null;
        }
        for (String s : normalPool.keySet()) {
            ChatService chatService = normalPool.get(s);
            if (chatService.getStatus() == Status.FINISHED) {
                return chatService;
            }
        }
        return null;
    }

    public void modifyChatService(ChatService chatService) {
        log.info("修改账号{}信息", chatService.getAccount());
        Account account = new Account();
        account.setAccount(chatService.getAccount());
        account.setToken(chatService.getAccessToken());
        account.setPassword(chatService.getPassword());
        accountService.updateTokenForAccount(chatService.getAccount(), chatService.getAccessToken());
    }

    public ChatService getChatService(String account) {
        if (account == null || account.equals("")) {
            return getFreeChatService(Model.GPT_3_5);
        }
        return accountPool.get(account);
    }

}
