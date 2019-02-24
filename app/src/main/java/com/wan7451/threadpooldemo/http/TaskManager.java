package com.wan7451.threadpooldemo.http;

import android.os.AsyncTask;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;

public class TaskManager {
    /**
     * 线程池
     */
    private ThreadPoolExecutor executor;
    /**
     * 任务队列
     */
    private final LinkedBlockingQueue<Runnable> queue;
    private final RejectedExecutionHandler handler;
    private final ThreadFactory factory;

    public TaskManager() {
        queue = new LinkedBlockingQueue<>();
        handler = new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                try {
                    queue.put(r);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        factory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "net");
            }
        };
        int coreSize = Runtime.getRuntime().availableProcessors();
        this.executor = new ThreadPoolExecutor(coreSize,
                coreSize * 2,
                1, TimeUnit.SECONDS,
                queue,
                factory,
                handler);
        this.executor.execute(runnable);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                Runnable take = null;
                //阻塞，等待队列
                try {
                    take = queue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (take != null) {
                    executor.execute(take);
                }
            }
        }
    };

    /**
     * 执行任务
     * @param runnable
     */
    public void execute(@NonNull Runnable runnable) {
        try {
            //添加队列
            queue.put(runnable);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
