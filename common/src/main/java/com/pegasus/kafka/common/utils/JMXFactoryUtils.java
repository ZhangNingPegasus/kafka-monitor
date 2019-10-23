package com.pegasus.kafka.common.utils;


import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.*;

public class JMXFactoryUtils {
    private static final ThreadFactory daemonThreadFactory = new DaemonThreadFactory();

    public static JMXConnector connectWithTimeout(final JMXServiceURL url, long timeout, TimeUnit unit) throws IOException {
        final BlockingQueue<Object> blockQueue = new ArrayBlockingQueue<>(1);
        ExecutorService executor = Executors.newSingleThreadExecutor(daemonThreadFactory);
        executor.submit(() -> {
            try {
                JMXConnector connector = JMXConnectorFactory.connect(url);
                if (!blockQueue.offer(connector)) {
                    connector.close();
                }
            } catch (Throwable t) {
                blockQueue.offer(t);
            }
        });
        Object result;
        try {
            result = blockQueue.poll(timeout, unit);
            if (result == null) {
                if (!blockQueue.offer("")) {
                    result = blockQueue.take();
                }
            }
        } catch (InterruptedException e) {
            throw initCause(new InterruptedIOException(e.getMessage()), e);
        } finally {
            executor.shutdown();
        }
        if (result == null)
            throw new SocketTimeoutException(String.format("Kafka JMX 连接超时: %s", url));
        if (result instanceof JMXConnector)
            return (JMXConnector) result;
        try {
            throw (Throwable) result;
        } catch (Throwable e) {
            throw new IOException(e.toString(), e);
        }
    }

    private static <T extends Throwable> T initCause(T wrapper, Throwable wrapped) {
        wrapper.initCause(wrapped);
        return wrapper;
    }

    private static class DaemonThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        }
    }
}
