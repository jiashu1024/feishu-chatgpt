package com.zjs.feishubot.service;

import com.zjs.feishubot.entity.Account;
import com.zjs.feishubot.util.chatgpt.AccountUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {
    public List<Account> getAccounts() {
        return AccountUtil.readAccounts().getAccounts();
    }

    public void updateTokenForAccount(String accountName, String newToken) {
        AccountUtil.updateToken(accountName, newToken);
    }
}
