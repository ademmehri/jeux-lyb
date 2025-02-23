package src;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TimeoutExecuter {
    public static <T> T execute(Callable<T> task, long timeout, TimeUnit timeUnit) throws TimeoutException, InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<T> future = executor.submit(task);
        try {
            return future.get(timeout, timeUnit);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw e;
        } finally {
            executor.shutdown();
        }
    }

    public static <T> T execute(Callable<T> task, long timeout) throws TimeoutException, InterruptedException, ExecutionException {
        return TimeoutExecuter.execute(task, timeout, TimeUnit.MILLISECONDS);
    }
}
