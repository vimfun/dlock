package io.github.vimfun.utils;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.vavr.collection.Stream;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;

/**
 * ClusterLock 工具类，解决分布式锁的繁琐易错的使用问题
 */
@Slf4j
public class ClusterLock {

    public static class BetterLock<L extends Lock> {
        private Lock lock;
        private RLock rLock;

        public BetterLock(L lock) {
            if (lock instanceof RLock) {
                this.rLock = (RLock)lock;
            } else {
                this.lock = lock;
            }
        }

        public boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) {
            try {
                if (rLock != null) {
                    return rLock.tryLock(waitTime, leaseTime, unit);
                } else {
                    return lock.tryLock(waitTime, unit);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        public boolean tryLock(long waitTime, TimeUnit unit) {
            try {
                return lock.tryLock(waitTime, unit);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public static void withLock(Supplier<String> keyGen, Function<String, Lock> lockGetter, Runnable runnable) {
        Supplier<Integer> supplier = () -> {
            runnable.run();
            return 0;
        };
        withLock(keyGen, lockGetter, supplier);
    }
    public static <T> T withLock(Supplier<String> keyGen, Function<String, Lock> lockGetter, Callable<T> callable) {
        Supplier<T> supplier = () -> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
        return withLock(keyGen, lockGetter, supplier);
    }

    public static <T> T withLock(Supplier<String> keyGen, Function<String, Lock> lockGetter, Supplier<T> supplier) {
        return withLock(keyGen, lockGetter, 1, TimeUnit.MINUTES, supplier);
    }

    public static <T> T withLock(Supplier<String> keyGen, Function<String, Lock> lockGetter, long leaseTime, TimeUnit unit, Supplier<T> supplier) {
        Function<Supplier<T>, T> sc = s -> s.get();
        return buildSupplierCallable(keyGen, lockGetter, lock -> lock.tryLock(leaseTime, unit), sc).apply(supplier);
    }

    public static <T> T withLocks(Supplier<Collection<String>> keyGen, Function<String, Lock> lockGetter, Supplier<T> supplier) {
        List<String> lockKeys = keyGen.get().stream().distinct().sorted().collect(Collectors.toList());
        Function<Supplier<T>, T> sc = s -> s.get();
        for (String key : lockKeys) {
            sc = buildSupplierCallable(() -> key, lockGetter, lock -> lock.tryLock(500, TimeUnit.MILLISECONDS), sc);
        }
        return sc.apply(supplier);
    }

    public static <T, L extends Lock> T withLock(Supplier<String> keyGen, Function<String, L> lockGetter, Function<BetterLock<L>, Boolean> lockTryer, Supplier<T> supplier) {
        Function<Supplier<T>, T> sc = s -> s.get();
        return buildSupplierCallable(keyGen, lockGetter, lockTryer, sc).apply(supplier);
    }

    public static <T, L extends Lock> T withLocks(Supplier<Collection<String>> keyGen, Function<String, L> lockGetter, Function<BetterLock<L>, Boolean> lockTryer, Supplier<T> supplier) {
        List<String> lockKeys = keyGen.get().stream().distinct().sorted().collect(Collectors.toList());
        Function<Supplier<T>, T> sc = s -> s.get();
        for (String key : lockKeys) {
            sc = buildSupplierCallable(() -> key, lockGetter, lockTryer, sc);
        }
        return sc.apply(supplier);
    }

    public static <T, L extends Lock> Function<Supplier<T>, T> buildSupplierCallable(
            Supplier<String> keyGen, Function<String, L> lockGetter,
            Function<BetterLock<L>, Boolean> lockTryer, Function<Supplier<T>, T> supplierCallable) {
        return s -> {
            String lockKey = keyGen.get();
            L lock = lockGetter.apply(lockKey);
            try {
                if (lockTryer.apply(new BetterLock<L>(lock))) {
                    log.info("lock key: {} get", lockKey);
                    try {
                        return supplierCallable.apply(s);
                    } finally {
                        lock.unlock();
                        log.info("lock key: {} released", lockKey);
                    }
                } else {
                    log.warn("lock key: {} cannot get", lockKey);
                    throw new RuntimeException("cannot get lock key: " + lockKey);
                }
            } catch (Exception e) {
                log.error("lock key: {} run with error", lockKey, e);
                throw new RuntimeException(e);
            }
        };
    }

    public static class RL implements Lock {

        @Override
        public void lock() {}

        @Override
        public void lockInterruptibly() throws InterruptedException {
            throw new UnsupportedOperationException("Unimplemented method 'lockInterruptibly'");
        }

        @Override
        public boolean tryLock() {
            return true;
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            return true;
        }

        @Override
        public void unlock() {
        }

        @Override
        public Condition newCondition() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'newCondition'");
        }
    }

    public static void main(String[] args) {
        Function<Supplier<String>, String> fsa = sa -> sa.get();
        String res = buildSupplierCallable(() -> "abc", k -> new RL(), lock -> lock.tryLock(0, TimeUnit.MILLISECONDS), fsa).apply(() -> "hello");
        System.out.println(res);

        res = withLocks(
            () -> Stream.rangeBy(0, 10, 1).map(x -> x + "").asJava(),
            k -> new RL(),
            () -> "hello");
        System.out.println(res);
    }
}
