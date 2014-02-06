package com.netflix.fabricator.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for creating various types of thread pools
 * @author elandau
 *
 */
public final class Executors2 {

    static public Executor newBoundedQueueFixedPool(int numThreads, int queueDepth, ThreadFactory factory) {
        final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(queueDepth);
        
        RejectedExecutionHandler handler = new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable arg0, ThreadPoolExecutor arg1) {
                try {
                    queue.put(arg0);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }
        };
        
        return new ThreadPoolExecutor(numThreads, numThreads, 1, TimeUnit.MINUTES, queue, factory, handler);
    }
    
    static public Executor newBoundedQueueFixedPool(int numThreads, int queueDepth) {
        final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(queueDepth);
        
        RejectedExecutionHandler handler = new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable arg0, ThreadPoolExecutor arg1) {
                try {
                    queue.put(arg0);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }
        };
        
        return new ThreadPoolExecutor(numThreads, numThreads, 1, TimeUnit.MINUTES, queue, handler);
    }

}
