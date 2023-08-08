package com.zjs.feishubot;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.lark.oapi.service.contact.v3.model.User;
import com.zjs.feishubot.entity.Account;
import com.zjs.feishubot.service.AccountService;
import com.zjs.feishubot.util.FeiShuUtil;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;


@SpringBootTest
class FeiShuBotApplicationTests {

  @Autowired
  private RedissonClient redissonClient;

  @Autowired
  private AccountService accountService;

  @Autowired
  private FeiShuUtil feiShuUtil;

  @Test
  void prepareAccountData() {
//    List<Account> accounts = new ArrayList<>();
//    Account account1 = new Account("cioubipawhorfi@mail.com", "1014@GPT.mima");
//    account1.setToken("Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Ik1UaEVOVUpHTkVNMVFURTRNMEZCTWpkQ05UZzVNRFUxUlRVd1FVSkRNRU13UmtGRVFrRXpSZyJ9.eyJodHRwczovL2FwaS5vcGVuYWkuY29tL3Byb2ZpbGUiOnsiZW1haWwiOiJjaW91YmlwYXdob3JmaUBtYWlsLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlfSwiaHR0cHM6Ly9hcGkub3BlbmFpLmNvbS9hdXRoIjp7InVzZXJfaWQiOiJ1c2VyLXYxOG1FVVhtd2I1d1dIazVmZU92cGxndiJ9LCJpc3MiOiJodHRwczovL2F1dGgwLm9wZW5haS5jb20vIiwic3ViIjoiYXV0aDB8NjQ3MjVjMjAzODYxZTBlZWEwZTkyMWRlIiwiYXVkIjpbImh0dHBzOi8vYXBpLm9wZW5haS5jb20vdjEiLCJodHRwczovL29wZW5haS5vcGVuYWkuYXV0aDBhcHAuY29tL3VzZXJpbmZvIl0sImlhdCI6MTY4OTU2MDY4NCwiZXhwIjoxNjkwNzcwMjg0LCJhenAiOiJUZEpJY2JlMTZXb1RIdE45NW55eXdoNUU0eU9vNkl0RyIsInNjb3BlIjoib3BlbmlkIHByb2ZpbGUgZW1haWwgbW9kZWwucmVhZCBtb2RlbC5yZXF1ZXN0IG9yZ2FuaXphdGlvbi5yZWFkIG9yZ2FuaXphdGlvbi53cml0ZSBvZmZsaW5lX2FjY2VzcyJ9.DGQt01TmEXTOfgXDjw1RQIQd3nPnI1O09thRRzYtrfm3jBv0XgHrhRRoD_1uyIBqAH_2SdtX2WPGbWaBkuGNgdEU0KO9W1bQnIunZk9z8xrSbwfeygUnsAVk1PTxilBZb9PzVJigXF8DRFl0RT_6yeMfXnUktjxQ4Wx7WVajf8WwZcNGa6oJuXK6Ig9vD-aLH67rkKvaZA_2INuJSnFEmHhxXlN8hZHdAJdi8Ecsy1XGQaBP3Rn8HQiwLtAP_q-vIMVywFVEWU-LauVI0qVrplFSDA2j6_EKrRoL2Ki_gKxIaC1AgbFJF60CC1k3vj-YAqgeGPwy7a7fZvDY4-P2ew");
//    Account account2 = new Account("linyueyao1220@gmail.com", "1014@GPT.mima");
//    account2.setToken("Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Ik1UaEVOVUpHTkVNMVFURTRNMEZCTWpkQ05UZzVNRFUxUlRVd1FVSkRNRU13UmtGRVFrRXpSZyJ9.eyJodHRwczovL2FwaS5vcGVuYWkuY29tL3Byb2ZpbGUiOnsiZW1haWwiOiJsaW55dWV5YW8xMjIwQGdtYWlsLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlfSwiaHR0cHM6Ly9hcGkub3BlbmFpLmNvbS9hdXRoIjp7InVzZXJfaWQiOiJ1c2VyLWczR2pKTWJoYk1ZSzkwaldEMXp6S2h6VCJ9LCJpc3MiOiJodHRwczovL2F1dGgwLm9wZW5haS5jb20vIiwic3ViIjoiYXV0aDB8NjQ1ODUxMDdiMmYyYmQzZDA5ZjNlMWE4IiwiYXVkIjpbImh0dHBzOi8vYXBpLm9wZW5haS5jb20vdjEiLCJodHRwczovL29wZW5haS5vcGVuYWkuYXV0aDBhcHAuY29tL3VzZXJpbmZvIl0sImlhdCI6MTY4OTU2MDYyNiwiZXhwIjoxNjkwNzcwMjI2LCJhenAiOiJUZEpJY2JlMTZXb1RIdE45NW55eXdoNUU0eU9vNkl0RyIsInNjb3BlIjoib3BlbmlkIHByb2ZpbGUgZW1haWwgbW9kZWwucmVhZCBtb2RlbC5yZXF1ZXN0IG9yZ2FuaXphdGlvbi5yZWFkIG9yZ2FuaXphdGlvbi53cml0ZSBvZmZsaW5lX2FjY2VzcyJ9.Nrj4dquExzCjRHN1MQ1dnk5bSMK-mBSJw2ZIqPfxi2Le2LmOdJq2z4jwhr4jprMYp8xi9lEWnasvmH_DwwDpwXLv85w9ALiIhoNevaJHoPA8rve0gEkOuJ0cRmyhIiQqONQNY22x5NMwaGF_jh4g7eN2TfJF3yknN4p3gjhr8LBOUrz9HcJZ4Yl3-THulVXDDVDJSePp9doFvPe38rcuKlrS-F6KkYoJUGD9l_pWT3UdIydwBjuwprc9y_UJS8YjtYMOISLffsnHlQNCvuEy2jAHMDLpmV7jWkbRdfrJNZ-AEQPV_iIXXQVBRIFyyS_LabpsBqy1jx8OHrdiqHUrpg");
//    Account account3 = new Account("linyueyao@stu.xupt.edu.cn", "1014@GPT.mima");
//    account3.setToken("Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Ik1UaEVOVUpHTkVNMVFURTRNMEZCTWpkQ05UZzVNRFUxUlRVd1FVSkRNRU13UmtGRVFrRXpSZyJ9.eyJodHRwczovL2FwaS5vcGVuYWkuY29tL3Byb2ZpbGUiOnsiZW1haWwiOiJsaW55dWV5YW9Ac3R1Lnh1cHQuZWR1LmNuIiwiZW1haWxfdmVyaWZpZWQiOnRydWV9LCJodHRwczovL2FwaS5vcGVuYWkuY29tL2F1dGgiOnsidXNlcl9pZCI6InVzZXItaEtGdTByd25RT3lxZlBsOEYwWW52VUkyIn0sImlzcyI6Imh0dHBzOi8vYXV0aDAub3BlbmFpLmNvbS8iLCJzdWIiOiJhdXRoMHw2NDU4NTU0ODg2NzgzOTEzODMwMDAyOTQiLCJhdWQiOlsiaHR0cHM6Ly9hcGkub3BlbmFpLmNvbS92MSIsImh0dHBzOi8vb3BlbmFpLm9wZW5haS5hdXRoMGFwcC5jb20vdXNlcmluZm8iXSwiaWF0IjoxNjg5NTYwNTY1LCJleHAiOjE2OTA3NzAxNjUsImF6cCI6IlRkSkljYmUxNldvVEh0Tjk1bnl5d2g1RTR5T282SXRHIiwic2NvcGUiOiJvcGVuaWQgcHJvZmlsZSBlbWFpbCBtb2RlbC5yZWFkIG1vZGVsLnJlcXVlc3Qgb3JnYW5pemF0aW9uLnJlYWQgb3JnYW5pemF0aW9uLndyaXRlIG9mZmxpbmVfYWNjZXNzIn0.i_jLzomoTZCy_vjFGdFKrca29lj5tMDLAa1mYxgPqz9EUm3mtMt4z88cu7mh1iAJTc71etdIGOURwGIQm4DOtuHxeThp59f9M2P_qD7f9EKb0ysFHOB69MPSgoxcKUnWjQd1hTLrihTq-0Jt0H4wYLgQwZOSF385lH3dfWEDuYyLeq_l6yc68wV_jfleTkSxOOGbDMFVGCuw11i4exrH11pkZZNaNCgs3K3GV1bOA4X86C0TeLvgzMIGMU2wqp7yt922tlCwj8kobZ_gR1S6_xnX301LsJ6f3-_XRg3j_WdVZiHfmF_D5Md9ZG1RvkrJ8qImoTUCOnOPr66ncFbevw");
//    Account account4 = new Account("zhang2018@stu.xupt.edu.cn", "1014@GPT.mima");
//    account4.setToken("Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Ik1UaEVOVUpHTkVNMVFURTRNMEZCTWpkQ05UZzVNRFUxUlRVd1FVSkRNRU13UmtGRVFrRXpSZyJ9.eyJodHRwczovL2FwaS5vcGVuYWkuY29tL3Byb2ZpbGUiOnsiZW1haWwiOiJ6aGFuZzIwMThAc3R1Lnh1cHQuZWR1LmNuIiwiZW1haWxfdmVyaWZpZWQiOnRydWV9LCJodHRwczovL2FwaS5vcGVuYWkuY29tL2F1dGgiOnsidXNlcl9pZCI6InVzZXItZVY1RlJQSFFPZTlCSFdTMGFiZjc3Z1FBIn0sImlzcyI6Imh0dHBzOi8vYXV0aDAub3BlbmFpLmNvbS8iLCJzdWIiOiJhdXRoMHw2NDU3YjY1MjIxMmFkMTg1MTMyYzgwN2YiLCJhdWQiOlsiaHR0cHM6Ly9hcGkub3BlbmFpLmNvbS92MSIsImh0dHBzOi8vb3BlbmFpLm9wZW5haS5hdXRoMGFwcC5jb20vdXNlcmluZm8iXSwiaWF0IjoxNjg5NTYwNDkwLCJleHAiOjE2OTA3NzAwOTAsImF6cCI6IlRkSkljYmUxNldvVEh0Tjk1bnl5d2g1RTR5T282SXRHIiwic2NvcGUiOiJvcGVuaWQgcHJvZmlsZSBlbWFpbCBtb2RlbC5yZWFkIG1vZGVsLnJlcXVlc3Qgb3JnYW5pemF0aW9uLnJlYWQgb3JnYW5pemF0aW9uLndyaXRlIG9mZmxpbmVfYWNjZXNzIn0.x_jQV13eklFHRtxomwAItpuBQH1InjPofkqNDQn9b0AVWbK7ugGMWnAvVg-XCWHqasFN8bVk9eaZ7hGx316uPLFGpmi4AlDzlhIPaqaodaPUfRSggMXfi9cpI_2zjb9zH9WbIiIw6ZIDqTER7bDO_G3jU3ntbG58Um3MoCA-gKGbL0djP9IWiV8B42HaCHN2PkbMgTVqv1Za39nyV-vd1zRQI4tcCr9w91xfUbla6pbSlqQVlk1Hd-RmUzlnX_hcpu3LZ2uo53FdkZPmiloIUn8dfL4XdM_rX1fZo8jt69T2wYYVWxoczRkzd1zdfac9A8jwH68161eQmYh7WfLIuA");
//    Account account5 = new Account("robankvercarllmas@mail.com", "1130600015GPT");
//    account5.setToken("Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Ik1UaEVOVUpHTkVNMVFURTRNMEZCTWpkQ05UZzVNRFUxUlRVd1FVSkRNRU13UmtGRVFrRXpSZyJ9.eyJodHRwczovL2FwaS5vcGVuYWkuY29tL3Byb2ZpbGUiOnsiZW1haWwiOiJyb2Jhbmt2ZXJjYXJsbG1hc0BtYWlsLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlfSwiaHR0cHM6Ly9hcGkub3BlbmFpLmNvbS9hdXRoIjp7InVzZXJfaWQiOiJ1c2VyLTNDd293ODZGdXNEMjVsZGhXVkpLVEdmSiJ9LCJpc3MiOiJodHRwczovL2F1dGgwLm9wZW5haS5jb20vIiwic3ViIjoiYXV0aDB8NjRhZWE0NWI3OGQxODI5ZmVhYmM3MTdiIiwiYXVkIjpbImh0dHBzOi8vYXBpLm9wZW5haS5jb20vdjEiLCJodHRwczovL29wZW5haS5vcGVuYWkuYXV0aDBhcHAuY29tL3VzZXJpbmZvIl0sImlhdCI6MTY4OTUxOTkwMiwiZXhwIjoxNjkwNzI5NTAyLCJhenAiOiJUZEpJY2JlMTZXb1RIdE45NW55eXdoNUU0eU9vNkl0RyIsInNjb3BlIjoib3BlbmlkIHByb2ZpbGUgZW1haWwgbW9kZWwucmVhZCBtb2RlbC5yZXF1ZXN0IG9yZ2FuaXphdGlvbi5yZWFkIG9yZ2FuaXphdGlvbi53cml0ZSBvZmZsaW5lX2FjY2VzcyJ9.oTgHTk_9RMcIoOmADHut-dK969HwbkABR0s_N8zhtxBVT6zwKPcg3WY5a5QJX8cCQxLR6xpr4jQ1FPxNw6TpdyHA935YCcdebt26Leo145laKz3VapoJ6N3n5BSh2d2JiOvmijB5En9zS_yQZqQDST2HWdIzCdvYnsLvEKonqfXVZdW0vV8Z97Wlu58jF89VuUy_CVeBXwozO-Fldjm3j5R0xJIu2wBj_mjycObVE7utUU1uzKAqcTH_wCcuQcm1mBumEmiwY6lzTQsjsF2qK22InHpxd2v4moqQ7mTN533yeB9ZrlQCTAJcnQBAV8WGtq2akQ4au1Ayd-GI35aFfg");
//
//    accounts.add(account1);
//    accounts.add(account2);
//    accounts.add(account3);
//    accounts.add(account4);
//    accounts.add(account5);
//
//    for (Account account : accounts) {
//      account.setNormalPublic(true);
//      account.setPlusPublic(true);
//      accountService.updateTokenForAccount(account.getAccount(), account.getToken());
//      redissonClient.getMap(KeyGenerateConfig.ACCOUNTS_KEY).put(account.getAccount(), account);
//    }

    List<Account> accounts = accountService.getAllAccountNoCheck();
    for (Account account : accounts) {
      account.setFreePublic(true);

//      User adminUser = feiShuUtil.getAdminUser();
//      account.setOwnerOpenId(adminUser.getOpenId());
//      account.setOwnerUserName(adminUser.getName());
//      if (account.isPlusAccount()) {
//        account.setPlusPublic(false);
//        account.addPlusPublicUser(adminUser.getOpenId());
////       account.setPlusPublicUsers(null);
//      }
      if (account.getCreateTime() == null) {
        account.setCreateTime(DateTime.now().toTimestamp());
      }
      accountService.updateAccountInfo(account);
    }


  }


  @Test
  public void getAllUser() {
    List<User> allUser = feiShuUtil.getAllUser();
    for (User user : allUser) {
      System.out.println(user);
    }
  }

  @Test
  public void test() {
    DateTime date = DateUtil.date();
    DateTime dateTime = DateUtil.beginOfDay(date);
    DateTime agoTime = DateUtil.offsetDay(dateTime, -7);
    System.out.println(agoTime);
  }
}
