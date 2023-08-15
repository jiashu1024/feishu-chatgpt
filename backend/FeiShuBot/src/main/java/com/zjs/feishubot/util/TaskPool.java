package com.zjs.feishubot.util;

import com.zjs.feishubot.entity.Account;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class TaskPool {
  private static final Map<String, BlockingQueue<Task>> taskPool = new HashMap<>();

  public static void init(List<Account> accounts) {
    for (Account account : accounts) {
      taskPool.put(account.getAccount(), new LinkedBlockingQueue<>());
    }
  }

  public static void addTask(Task task) throws InterruptedException {
    BlockingQueue<Task> queue = taskPool.get(task.getAccount());
    if (queue == null) {
      queue = new LinkedBlockingQueue<>();
      taskPool.put(task.getAccount(), queue);
    }
    queue.put(task);
  }

  public static void addAccount(String account) {
    taskPool.put(account, new LinkedBlockingQueue<>());
  }

  public static void runTask() {
    Set<String> accounts = taskPool.keySet();
    for (String account : accounts) {
      new Thread(() -> {
        BlockingQueue<Task> queue = taskPool.get(account);
        while (true) {
          try {
            Task task = queue.take();
            task.run();
          } catch (InterruptedException e) {
            log.error("task pool error", e);
          }
        }
      }).start();
    }
  }


}
