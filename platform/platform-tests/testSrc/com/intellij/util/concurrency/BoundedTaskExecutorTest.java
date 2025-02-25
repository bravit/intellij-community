// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.util.concurrency;

import com.intellij.diagnostic.ThreadDumper;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.LoggedErrorProcessor;
import com.intellij.util.ConcurrencyUtil;
import com.intellij.util.TimeoutUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.UIUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class BoundedTaskExecutorTest extends CatchLogErrorsInAllThreadsTestCase {
  private static final Logger LOG = Logger.getInstance(BoundedTaskExecutorTest.class);

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    awaitAppPoolQuiescence("Can't start test: ");
  }

  @Override
  protected void tearDown() throws Exception {
    //noinspection SSBasedInspection
    try {
      awaitAppPoolQuiescence("After tear down ");
    }
    finally {
      super.tearDown();
    }
  }

  private String getPoolName() {
    return "A"+getName();
  }

  private static void awaitAppPoolQuiescence(String msg) {
    long start = System.currentTimeMillis();
    while (true) {
      List<Thread> alive = Thread.getAllStackTraces().keySet().stream()
        .filter(thread -> thread.getName().startsWith(AppScheduledExecutorService.POOLED_THREAD_PREFIX))
        .filter(thread -> thread.getState() == Thread.State.RUNNABLE)
        .filter(thread -> thread.getStackTrace().length != 0) // there can be RUNNABLE zombies with empty stacktrace
        .collect(Collectors.toList());

      long finish = System.currentTimeMillis();
      if (alive.isEmpty()) break;
      if (finish-start > 10000) {
        System.err.println(ThreadDumper.dumpThreadsToString());
        throw new RuntimeException(msg+alive.size() +" threads are still alive: "+alive);
      }
    }
  }

  public void testReallyBound() throws InterruptedException {
    for (int maxTasks=1; maxTasks<5;maxTasks++) {
      LOG.debug("maxTasks = " + maxTasks);
      ExecutorService backendExecutor = Executors.newCachedThreadPool(ConcurrencyUtil.newNamedThreadFactory("maxTasks = " + maxTasks));
      ExecutorService executor = AppExecutorUtil.createBoundedApplicationPoolExecutor(getPoolName(), backendExecutor, maxTasks);
      AtomicInteger running = new AtomicInteger();
      AtomicInteger max = new AtomicInteger();
      AtomicInteger executed = new AtomicInteger();
      int N = 10000;
      for (int i = 0; i < N; i++) {
        executor.execute(() -> {
          int r = running.incrementAndGet();
          try {
            TimeoutUtil.sleep(1);
            max.accumulateAndGet(r, Math::max);
            executed.incrementAndGet();
          }
          finally {
            running.decrementAndGet();
          }
        });
      }

      executor.shutdown();
      if (!executor.awaitTermination(N + 50000, TimeUnit.MILLISECONDS)) {
        fail(ThreadDumper.dumpThreadsToString());
      }
      backendExecutor.shutdownNow();
      if (!backendExecutor.awaitTermination(100, TimeUnit.SECONDS)) {
        fail(ThreadDumper.dumpThreadsToString());
      }
      assertEquals(maxTasks, max.get());
      assertEquals(N, executed.get());
    }
  }

  public void testCallableReallyReturnsValue() throws Exception{
    ExecutorService backendExecutor = Executors.newCachedThreadPool(ConcurrencyUtil.newNamedThreadFactory(getName()));
    ExecutorService executor = AppExecutorUtil.createBoundedApplicationPoolExecutor(getPoolName(), backendExecutor, 1);

    Future<Integer> f1 = executor.submit(() -> 42);
    int result = f1.get();
    assertEquals(42, result);
    executor.shutdownNow();
    if (!executor.awaitTermination(100, TimeUnit.SECONDS)) fail(ThreadDumper.dumpThreadsToString());
    backendExecutor.shutdownNow();
    if (!backendExecutor.awaitTermination(100, TimeUnit.SECONDS)) fail(ThreadDumper.dumpThreadsToString());
  }

  public void testEarlyCancelPreventsRunning() throws ExecutionException, InterruptedException {
    AtomicBoolean run = new AtomicBoolean();
    ExecutorService backendExecutor = Executors.newCachedThreadPool(ConcurrencyUtil.newNamedThreadFactory(getName()));
    ExecutorService executor = AppExecutorUtil.createBoundedApplicationPoolExecutor(getPoolName(), backendExecutor, 1);

    int delay = 1000;
    Future<?> s1 = executor.submit(() -> TimeoutUtil.sleep(delay));
    Future<Integer> f1 = executor.submit(() -> {
      run.set(true);
      return 42;
    });
    f1.cancel(false);
    s1.get();
    assertTrue(f1.isDone());
    assertTrue(f1.isCancelled());
    assertFalse(run.get());
    assertTrue(s1.isDone());
    executor.shutdownNow();
    if (!executor.awaitTermination(100, TimeUnit.SECONDS)) fail(ThreadDumper.dumpThreadsToString());
    backendExecutor.shutdownNow();
    if (!backendExecutor.awaitTermination(100, TimeUnit.SECONDS)) fail(ThreadDumper.dumpThreadsToString());
  }

  public void testStressWhenSomeTasksCallOtherTasksGet() throws Exception {
    BoundedScheduledExecutorTest.doTestBoundedExecutor(
      getName(),
      (backendExecutor, maxSimultaneousTasks) -> AppExecutorUtil.createBoundedApplicationPoolExecutor(getPoolName(), backendExecutor, maxSimultaneousTasks),
      maxSimultaneousTasks -> 3000,
      (executor, runnable, i)-> executor.submit(runnable));

  }

  public void testSequentialSubmitsMustExecuteSequentially() throws ExecutionException, InterruptedException {
    ExecutorService backendExecutor = Executors.newCachedThreadPool(ConcurrencyUtil.newNamedThreadFactory(getName()));
    ExecutorService executor = AppExecutorUtil.createBoundedApplicationPoolExecutor(getPoolName(), backendExecutor, 1);
    int N = 100000;
    StringBuffer log = new StringBuffer(N*4);
    StringBuilder expected = new StringBuilder(N * 4);

    Future<?>[] futures = new Future[N];
    for (int i = 0; i < N; i++) {
      String text = i + " ";
      futures[i] = executor.submit(() -> log.append(text));
    }
    for (int i = 0; i < N; i++) {
      expected.append(i).append(" ");
      futures[i].get();
    }

    String logs = log.toString();
    assertEquals(expected.toString(), logs);
    executor.shutdownNow();
    if (!executor.awaitTermination(100, TimeUnit.SECONDS)) fail(ThreadDumper.dumpThreadsToString());
    backendExecutor.shutdownNow();
    if (!backendExecutor.awaitTermination(100, TimeUnit.SECONDS)) fail(ThreadDumper.dumpThreadsToString());
  }

  public void testStressForHorribleABAProblemWhenFirstThreadFinishesTaskAndIsAboutToDecrementCountAndSecondThreadIncrementsCounterToTwoThenSkipsExecutionThenDecrementsItBackAndTheFirstThreadFinishedDecrementingSuccessfully() throws Exception {
    ExecutorService backendExecutor = Executors.newCachedThreadPool(ConcurrencyUtil.newNamedThreadFactory(getName()));
    int maxSimultaneousTasks = 1;
    final Disposable myDisposable = Disposer.newDisposable();
    ExecutorService executor = AppExecutorUtil.createBoundedApplicationPoolExecutor(getPoolName(), backendExecutor, maxSimultaneousTasks, myDisposable);
    AtomicInteger running = new AtomicInteger();
    AtomicInteger maxThreads = new AtomicInteger();

    try {
      int N = 100000;
      for (int i = 0; i < N; i++) {
        final int finalI = i;
        Future<?> future = executor.submit(new Runnable() {
          @Override
          public void run() {
            maxThreads.accumulateAndGet(running.incrementAndGet(), Math::max);

            try {
              if (finalI % 100 == 0) {
                TimeoutUtil.sleep(1);
              }
            }
            finally {
              running.decrementAndGet();
            }
          }

          @Override
          public String toString() {
            return "iteration " + finalI;
          }
        });
        CountDownLatch waitCompleted = new CountDownLatch(1);
        CountDownLatch waitStarted = new CountDownLatch(1);
        UIUtil.invokeLaterIfNeeded(() -> {
          try {
            waitStarted.countDown();
            ((BoundedTaskExecutor)executor).waitAllTasksExecuted(1, TimeUnit.MINUTES);
            waitCompleted.countDown();
          }
          catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
        waitStarted.await();
        executor.execute(new Runnable() {
          @Override
          public void run() {
            maxThreads.accumulateAndGet(running.incrementAndGet(), Math::max);

            try {
              Thread.yield();
            }
            finally {
              running.decrementAndGet();
            }
          }

          @Override
          public String toString() {
            return "check for " + finalI;
          }
        });
        //TimeoutUtil.sleep(1);
        executor.execute(new Runnable() {
          @Override
          public void run() {
            maxThreads.accumulateAndGet(running.incrementAndGet(), Math::max);

            try {
              Thread.yield();
            }
            finally {
              running.decrementAndGet();
            }
          }

          @Override
          public String toString() {
            return "check 2 for " + finalI;
          }
        });
        assertTrue(waitCompleted.await(1, TimeUnit.MINUTES));
        assertTrue(future.isDone());
      }
      UIUtil.invokeAndWaitIfNeeded((Runnable)() -> {
        try {
          ((BoundedTaskExecutor)executor).waitAllTasksExecuted(1, TimeUnit.MINUTES);
        }
        catch (Exception e) {
          throw new RuntimeException(e);
        }
      });
    }
    finally {
      Disposer.dispose(myDisposable);
      assertTrue(executor.isShutdown());
    }

    assertEquals("Max threads was: " + maxThreads + " but bound was 1", 1, maxThreads.get());
    backendExecutor.shutdownNow();
    if (!backendExecutor.awaitTermination(1, TimeUnit.MINUTES)) fail(ThreadDumper.dumpThreadsToString());
  }

  public void testShutdownNowMustCancel() throws InterruptedException {
    ExecutorService executor = AppExecutorUtil.createBoundedApplicationPoolExecutor(getPoolName(),AppExecutorUtil.getAppExecutorService(), 1);
    int N = 100000;
    StringBuffer log = new StringBuffer(N*4);

    Future<?>[] futures = new Future[N];
    for (int i = 0; i < N; i++) {
      futures[i] = executor.submit(() -> log.append(" "));
    }
    List<Runnable> runnables = executor.shutdownNow();
    assertTrue(executor.isShutdown());

    Thread.sleep(1000); // wait for a rare chance of a task executing right now
    assertEquals(N - log.length(), runnables.size());

    BoundedScheduledExecutorTest.checkEveryPossibleSubmitThrows(executor);

    for (int i = 0; i < log.length(); i++) {
      assertFalse(futures[i].isCancelled());
      assertTrue(futures[i].isDone());
    }
    for (int i = log.length(); i < N; i++) {
      assertTrue(futures[i].isCancelled());
      assertTrue(futures[i].isDone());
    }

    if (!executor.awaitTermination(100, TimeUnit.SECONDS)) fail(ThreadDumper.dumpThreadsToString());
  }

  public void testShutdownMustDisableSubmit() throws ExecutionException, InterruptedException {
    ExecutorService executor = AppExecutorUtil.createBoundedApplicationPoolExecutor(getPoolName(),AppExecutorUtil.getAppExecutorService(), 1);
    int N = 100000;
    StringBuffer log = new StringBuffer(N*4);

    Future<?>[] futures = new Future[N];
    for (int i = 0; i < N; i++) {
      futures[i] = executor.submit(() -> log.append(" "));
    }
    executor.shutdown();
    assertTrue(executor.isShutdown());

    BoundedScheduledExecutorTest.checkEveryPossibleSubmitThrows(executor);

    for (int i = 0; i < N; i++) {
      assertFalse(futures[i].isCancelled());
      futures[i].get();
    }

    String logs = log.toString();
    assertEquals(StringUtil.repeat(" ",N), logs);
    if (!executor.awaitTermination(100, TimeUnit.SECONDS)) fail(ThreadDumper.dumpThreadsToString());
  }

  public void testNoExtraThreadsAreEverCreated() throws InterruptedException, ExecutionException {
    for (int nMaxThreads=1; nMaxThreads<10; nMaxThreads++) {
      LOG.debug("nMaxThreads = " + nMaxThreads);
      ExecutorService executor = AppExecutorUtil.createBoundedApplicationPoolExecutor(getPoolName(),nMaxThreads);
      int N = 1000000;
      Set<Thread> workers = ContainerUtil.newConcurrentSet();

      CountDownLatch allStarted = new CountDownLatch(1);
      List<Future<?>> saturate = ContainerUtil.map(Collections.nCopies(nMaxThreads, null), o -> executor.submit(new Runnable() {
        @Override
        public void run() {
          try {
            allStarted.await();
          }
          catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        }

        @Override
        public String toString() {
          return "warmup";
        }
      }));


      List<Future<?>> futures = new ArrayList<>(N);
      for (int i = 0; i < N; i++) {
        final int finalI = i;
        futures.add(executor.submit(new Runnable() {
          @Override
          public void run() {
            workers.add(Thread.currentThread());
          }

          @Override
          public String toString() {
            return "Runnable test "+finalI;
          }
        }));
        //System.out.println("i = " + i+" submitted");
      }

      allStarted.countDown();
      ConcurrencyUtil.getAll(saturate);
      ConcurrencyUtil.getAll(futures);

      //System.out.println("workers.size() = " + workers.size());
      assertTrue("Must create no more than "+nMaxThreads+" workers but got: "+workers,
                 workers.size() <= nMaxThreads);
      executor.shutdownNow();
      if (!executor.awaitTermination(100, TimeUnit.SECONDS)) fail(ThreadDumper.dumpThreadsToString());
    }
  }

  public void testAwaitTerminationDoesWait() throws InterruptedException {
    for (int maxTasks=1; maxTasks<10; maxTasks++) {
      LOG.debug("maxTasks = " + maxTasks);
      ExecutorService executor = AppExecutorUtil.createBoundedApplicationPoolExecutor(getPoolName(),AppExecutorUtil.getAppExecutorService(), maxTasks);
      int N = 1000;
      StringBuffer log = new StringBuffer(N*4);

      Future<?>[] futures = new Future[N];
      for (int i = 0; i < N; i++) {
        int finalI = i;
        futures[i] = executor.submit(() -> {
          if (finalI < 100) {
            TimeoutUtil.sleep(2);
          }
          return log.append(" ");
        });
      }
      executor.shutdown();
      if (!executor.awaitTermination(100, TimeUnit.SECONDS)) fail(ThreadDumper.dumpThreadsToString());

      for (Future<?> future : futures) {
        assertTrue(future.isDone());
        assertFalse(future.isCancelled());
      }
      assertEquals(N, log.length());
    }
  }

  public void testAwaitTerminationDoesNotCompletePrematurely() throws InterruptedException {
    ExecutorService executor2 = AppExecutorUtil.createBoundedApplicationPoolExecutor(getPoolName(),AppExecutorUtil.getAppExecutorService(), 1);
    Future<?> future = executor2.submit(() -> TimeoutUtil.sleep(10000));
    executor2.shutdown();
    assertFalse(executor2.awaitTermination(1, TimeUnit.SECONDS));
    assertFalse(future.isDone());
    assertFalse(future.isCancelled());
    if (!executor2.awaitTermination(100, TimeUnit.SECONDS)) fail(ThreadDumper.dumpThreadsToString());
    assertTrue(future.isDone());
    assertFalse(future.isCancelled());
  }

  public void testErrorsThrownInFiredAndForgottenTaskMustBeLogged() {
    ExecutorService executor = AppExecutorUtil.createBoundedApplicationPoolExecutor(getPoolName(),AppExecutorUtil.getAppExecutorService(), 1);
    Throwable error = LoggedErrorProcessor.executeAndReturnLoggedError(() -> {
      try {
        AtomicBoolean executed = new AtomicBoolean();
        executor.execute(() -> {
          try {
            throw new Error("error "+getName());
          }
          finally {
            executed.set(true);
          }
        });
        while (!executed.get()) {}
        TimeoutUtil.sleep(100); // that tiny moment between throwing new Error() and catching it in BoundedTaskExecutor.wrapAndExecute()
      }
      finally {
        executor.shutdownNow();
      }
    });
    assertEquals("error " + getName(), error.getMessage());
  }

  public void testSeveralWaitAllTasksExecutedDontCauseDeadlock() throws Throwable {
    BoundedTaskExecutor executor = (BoundedTaskExecutor)AppExecutorUtil.createBoundedApplicationPoolExecutor(getPoolName(), AppExecutorUtil.getAppExecutorService(), 2);
    for (int i=0; i<1000 ;i++) {
      Future<?> future1 = executor.submit(() -> TimeoutUtil.sleep(1));
      Future<?> future2 = executor.submit(() -> TimeoutUtil.sleep(1));
      Future<?> wait1 = AppExecutorUtil.getAppExecutorService().submit(() -> {
        try {
          executor.waitAllTasksExecuted(10, TimeUnit.SECONDS);
        }
        catch (Exception e) {
          throw new RuntimeException(e);
        }
      });
      Future<?> wait2 = AppExecutorUtil.getAppExecutorService().submit(() -> {
        try {
          executor.waitAllTasksExecuted(10, TimeUnit.SECONDS);
        }
        catch (Exception e) {
          throw new RuntimeException(e);
        }
      });
      future1.get();
      future2.get();
      wait1.get();
      wait2.get();
    }
  }

  public void testIsTerminatedMustQueryIfAllTasksAreExecuted() throws InterruptedException, ExecutionException {
    ExecutorService executor = AppExecutorUtil.createBoundedApplicationPoolExecutor(getPoolName(),AppExecutorUtil.getAppExecutorService(), 1);
    Future<?> future = executor.submit(() -> TimeoutUtil.sleep(2_000));
    executor.shutdown();
    assertTrue(executor.isShutdown());
    assertFalse(executor.isTerminated());
    future.get();
    TimeoutUtil.sleep(20); // to let BoundedExecutor catchup the task termination
    assertTrue(executor.toString(), executor.isTerminated());
  }

  public void testShutdownMustBeIdempotentByExecutorServiceContract() {
    ExecutorService executor = AppExecutorUtil.createBoundedApplicationPoolExecutor(getPoolName(),AppExecutorUtil.getAppExecutorService(), 1);
    executor.shutdown();
    assertTrue(executor.isShutdown());
    executor.shutdown();
    assertTrue(executor.isShutdown());
  }
}
