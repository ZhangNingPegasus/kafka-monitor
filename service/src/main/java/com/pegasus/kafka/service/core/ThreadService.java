package com.pegasus.kafka.service.core;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The service for thread
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Service
public class ThreadService implements InitializingBean, DisposableBean {
    private static final int MAX_POOL_SIZE = 64;
    private static final Logger logger = LoggerFactory.getLogger(ThreadService.class);
    private final AtomicInteger rejectThreadNum = new AtomicInteger(1);
    private final BlockingQueue threadQueue = new ArrayBlockingQueue<>(MAX_POOL_SIZE);
    private ExecutorService executorService;

    public void submit(Runnable task) {
        executorService.submit(task);
    }

    public void submit(List<Runnable> taskList) {
        for (Runnable task : taskList) {
            executorService.submit(task);
        }
    }

    public int getSize() {
        return threadQueue.size();
    }

    @Override
    public void afterPropertiesSet() {
        executorService = new ThreadPoolExecutor(MAX_POOL_SIZE,
                MAX_POOL_SIZE,
                0L,
                TimeUnit.MILLISECONDS,
                threadQueue,
                new ThreadFactoryBuilder()
                        .setNameFormat("pool-thread-%d")
                        .setDaemon(false)
                        .setUncaughtExceptionHandler((thread, exception) -> {
                            exception.printStackTrace();
                            logger.error("", exception);
                        })
                        .build(),
                (runnable, executor) -> {
                    throw new RuntimeException(String.format("线程池已满. 线程[%s]被拒绝[%s]. ", runnable.toString(), rejectThreadNum.getAndIncrement()));
                });
    }

    @Override
    public void destroy() {
        executorService.shutdownNow();
    }


}
