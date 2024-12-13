package io.github.vimfun.utils;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.vavr.Tuple2;
import io.vavr.collection.Stream;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: zhaotao
 * @date: 2020/8/17 15:55
 */
@Slf4j
public class FuncUtil {

    public static <T, K> Map<K, T> toMap(Collection<T> xs, Function<T, K> km) {
        return toMap(xs, km, x -> x);
    }

    public static <T, K, V> Map<K, V> toMap(Collection<T> xs, Function<T, K> km, Function<T, V> vm) {
        return toMap(xs, x -> true, km, vm);
    }

    public static <T, K, V> Map<K, V> toMap(Collection<T> xs, Predicate<T> pred, Function<T, K> km, Function<T, V> vm) {
        if (isEmpty(xs)) {
            return new HashMap<>();
        }
        return xs.stream().filter(pred).collect(Collectors.toMap(km, vm, (v1, v2) -> v2));
    }

    public static <T, K> Map<K, List<T>> groupingBy(Collection<T> xs, Function<T, K> km) {
        if (isEmpty(xs)) {
            return new HashMap<>();
        }
        return xs.stream().collect(Collectors.groupingBy(km));
    }

    public static <T, K> Map<K, List<T>> groupingBy(Collection<T> xs, Predicate<T> pred, Function<T, K> km) {
        if (isEmpty(xs)) {
            return new HashMap<>();
        }
        return xs.stream().filter(pred).collect(Collectors.groupingBy(km));
    }

    public static <T, R> List<R> toList(Collection<T> xs, Function<T, R> m) {
        return toList(xs, x -> true, m);
    }

    public static <T, R> List<R> toList(Collection<T> xs, Predicate<T> pred, Function<T, R> m) {
        if (isEmpty(xs)) {
            return new ArrayList<>();
        }
        return xs.stream().filter(pred).map(m).collect(Collectors.toList());
    }

    public static <T, R> List<R> toList(Collection<T> xs, BiFunction<T, Integer, R> m) {
        if (isEmpty(xs)) {
            return new ArrayList<>();
        }
        return Stream.ofAll(xs).zipWithIndex().map(t -> m.apply(t._1, t._2)).toJavaList();
    }

    public static <T, R> List<R> toList(Collection<T> xs, Predicate<T> pred, BiFunction<T, Integer, R> m) {
        if (isEmpty(xs)) {
            return new ArrayList<>();
        }
        return Stream.ofAll(xs).filter(pred).zipWithIndex().map(t -> m.apply(t._1, t._2)).toJavaList();
    }

    public static <T, R> Set<R> toSet(Collection<T> xs, Function<T, R> m) {
        return toSet(xs, x -> true, m);
    }

    public static <T, R> Set<R> toSet(Collection<T> xs, Predicate<T> pred, Function<T, R> m) {
        if (isEmpty(xs)) {
            return new HashSet<>();
        }
        return xs.stream().filter(pred).map(m).collect(Collectors.toSet());
    }

    public static <T, C extends Collection<T>, Cc extends Collection<C>> List<T> concat(Cc xss) {
        return Stream.ofAll(xss).flatMap(xs -> xs).toJavaList();
    }

    public static <T, Ct extends Collection<T>> List<T> concat(Ct... xss) {
        return Stream.of(xss).flatMap(xs -> xs).toJavaList();
    }

    public static <T, C extends Collection<T>, Cc extends Collection<C>> Set<T> union(Cc xss) {
        return Stream.ofAll(xss).flatMap(xs -> xs).toJavaSet();
    }

    public static <T, Ct extends Collection<T>> Set<T> union(Ct... xss) {
        return Stream.of(xss).flatMap(xs -> xs).toJavaSet();
    }

    public static <T> List<T> join(Collection<Future<T>> futures) {
        return futures.stream().map(FuncUtil::getFuture).collect(Collectors.toList());
    }

    public static <K, V> Map<K, V> join(Map<K, Future<V>> futuresMap) {
        return futuresMap.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> getFuture(e.getValue())));
    }
    private static <T> T getFuture(Future<T> future) {                                                                         
        try {                                                                                                           
            return future.get();                                                                                        
        } catch (InterruptedException e) {                                                                              
            Thread.currentThread().interrupt();                                                                         
            throw new IllegalStateException("Future got interrupted", e);                                               
        } catch (ExecutionException e) {                                                                                
            if (e.getCause() instanceof RuntimeException) {                                                             
                throw (RuntimeException) e.getCause();                                                                  
            } else {                                                                                                    
                throw new RuntimeException("Failed execution", e);                                                      
            }                                                                                                           
        }                                                                                                               
    }  

    public <U> Function<Supplier<U>, CompletableFuture<U>> asyncWith(Executor executor) {
        return supplier -> CompletableFuture.supplyAsync(supplier, executor);
    }

    public static <T> T get(Collection<T> xs, Predicate<T> pred, Supplier<T> defValSupplier) {
        return findAny(xs, pred).orElseGet(defValSupplier);
    }

    public static <T> T get(Collection<T> xs, Predicate<T> pred, T defVal) {
        return findAny(xs, pred).orElse(defVal);
    }

    public static <T> Optional<T> findAny(Collection<T> xs, Predicate<T> pred) {
        return xs.stream().filter(pred).findAny();
    }

    public static <T, S> List<T> callPages(Collection<S> keys, int keyNumOfPage, Function<Collection<S>, List<T>> convertor) {
        log.debug("keyNumOfPage: {}", keyNumOfPage);

        if (keys.size() <= keyNumOfPage)
            return convertor.apply(keys);

        List<T> res = new ArrayList<>(keys.size());
        Stream<S> ks = Stream.ofAll(keys);

        Tuple2<Stream<S>, Stream<S>> ksT2 = ks.splitAt(keyNumOfPage);
        List<T> res_ = convertor.apply(ksT2._1.toJavaList());
        res.addAll(res_);
        log.debug("res_: {}", res_);
        while (!ksT2._2.isEmpty()){
            ksT2 = ksT2._2.splitAt(keyNumOfPage);
            res_ = convertor.apply(ksT2._1.toJavaList());
            log.debug("res_: {}", res_);
            res.addAll(res_);
        }
        return res;
    }

    public static <T> T logDuration(Supplier<T> supplier, String method, Object... args) {
        long start = System.currentTimeMillis();
        try {
            return supplier.get();
        } finally {
            long end = System.currentTimeMillis();
            log.info("method: {} args: {} duration: {}ms", method, args, end - start);
        }
    }

    public static void main(String[] args) {
        Function<Collection<Integer>, List<Object>> cv = xs -> xs.stream().map(x -> "" + x).collect(Collectors.toList());
        List<Object> r = callPages(Stream.range(0, 10).asJava(), 1, cv);
        System.out.println(r);
        r = callPages(Stream.range(0, 10).asJava(), 10, cv);
        System.out.println(r);
        r = callPages(Stream.range(0, 10).asJava(), 2, cv);
        System.out.println(r);
        r = callPages(Stream.range(0, 10).asJava(), 3, cv);
        System.out.println(r);
        r = callPages(Stream.range(0, 10).asJava(), 9, cv);
        System.out.println(r);
        r = callPages(Stream.range(0, 10).asJava(), 19, cv);
        System.out.println(r);
    }
}
