/* Copyright (c) 2021 OceanBase and/or its affiliates. All rights reserved.
oblogclient is licensed under Mulan PSL v2.
You can use this software according to the terms and conditions of the Mulan PSL v2.
You may obtain a copy of Mulan PSL v2 at:
         http://license.coscl.org.cn/MulanPSL2
THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
See the Mulan PSL v2 for more details. */

package com.oceanbase.clogproxy.common.util;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TaskExecutor {

    private static class Singleton {
        private static final TaskExecutor INSTANCE = new TaskExecutor();
    }

    public static TaskExecutor instance() {
        return Singleton.INSTANCE;
    }

    private TaskExecutor() { }

    public static class Task<T> {
        protected Future<T> future;
        protected Failure failure;

        public T get() {
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                if (failure != null) {
                    failure.onError(e);
                }
                return null;
            }
        }

        public void join() {
            get();
        }
    }

    public static class BackgroundTask extends Task<Void> {

        @Override
        public void join() {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                if (failure != null) {
                    failure.onError(e);
                }
            }
        }
    }

    public interface Failure {
        void onError(Exception e);
    }

    private ExecutorService asyncTasks = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
        60L, TimeUnit.SECONDS, new SynchronousQueue<>(), (ThreadFactory) Thread::new);

    private ExecutorService bgTasks = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
        60L, TimeUnit.SECONDS, new SynchronousQueue<>(), (ThreadFactory) Thread::new);

    private Map<String, ConcurrentTask> concurrentTasks = Maps.newConcurrentMap();

    public <T> Task<T> async(Callable<T> callable) {
        return async(callable, null);
    }

    public <T> Task<T> async(Callable<T> callable, Failure failure) {
        Task<T> task = new Task<>();
        task.future = asyncTasks.submit(callable);
        task.failure = failure;
        return task;
    }

    public BackgroundTask background(Callable<Void> callable) {
        return background(callable, null);
    }

    public BackgroundTask background(Callable<Void> callable, Failure failure) {
        BackgroundTask task = new BackgroundTask();
        task.future = bgTasks.submit(callable);
        task.failure = failure;
        return task;
    }

    public static class ConcurrentTask {
        private ExecutorService concurrentTasks;

        public ConcurrentTask(int parallelism) {
            // Never exceed actual CPU core count for init count, or got an Exception
            concurrentTasks = new ForkJoinPool(Math.min(parallelism,
                Runtime.getRuntime().availableProcessors()),
                ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true);
        }

        public <T> Future<T> concurrent(Callable<T> callable) {
            return concurrentTasks.submit(callable);
        }
    }

    public ConcurrentTask refConcurrent(String name, int parallelism) {
        ConcurrentTask task = concurrentTasks.get(name);
        if (task != null) {
            return task;
        }
        task = new ConcurrentTask(parallelism);
        concurrentTasks.put(name, task);
        return task;
    }

    public int getAsyncTaskCount() {
        return ((ThreadPoolExecutor) asyncTasks).getActiveCount();
    }

    public int getBgTaskCount() {
        return ((ThreadPoolExecutor) bgTasks).getActiveCount();
    }

    public int getConcurrentTaskCount() {
        int count = 0;
        for (ConcurrentTask t : concurrentTasks.values()) {
            count += ((ForkJoinPool) t.concurrentTasks).getActiveThreadCount();
        }
        return count;
    }
}
