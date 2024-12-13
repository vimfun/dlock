package io.github.vimfun.utils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.Test;

import io.vavr.collection.Stream;

import static io.github.vimfun.utils.ClusterLock.*;

public class ClusterLockTest {

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
    @Test
    public void testBuildSupplierCallable() {

    }

    @Test
    public void testWithLock() {

    }

    @Test
    public void testWithLock2() {

    }

    @Test
    public void testWithLock3() {

    }

    @Test
    public void testWithLock4() {

    }

    @Test
    public void testWithLock5() {

    }

    @Test
    public void testWithLocks() {

    }

    @Test
    public void testWithLocks2() {

    }
}
