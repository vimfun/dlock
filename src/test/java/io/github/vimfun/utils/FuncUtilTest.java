package io.github.vimfun.utils;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.Test;

public class FuncUtilTest {
    @Test
    public void testAsyncWith() {

    }

    @Test
    public void testCallPages() {

    }

    @Test
    public void testConcat() {

    }

    @Test
    public void testConcat2() {

    }

    @Test
    public void testFindAny() {

    }

    @Test
    public void testGet() {

    }

    @Test
    public void testGet2() {

    }

    @Test
    public void testGroupingBy() {

    }

    @Test
    public void testGroupingBy2() {

    }

    @Test
    public void testJoin() {

    }

    @Test
    public void testJoin2() {

    }

    @Test
    public void testLogDuration() {

    }

    @Test
    public void testToList() {

    }

    @Test
    public void testToList2() {

    }

    @Test
    public void testToList3() {

    }

    @Test
    public void testToList4() {

    }

    @Test
    public void testToMap() {

    }

    @Test
    public void testToMap2() {

    }

    @Test
    public void testToMap3() {

    }

    @Test
    public void testToSet() {

    }

    @Test
    public void testToSet2() {

    }

    @Test
    public void testUnion() {

    }

    @Test
    public void testUnion2() {

    }

    @Test
    void testGetWithMatchingElement() {
        List<String> list = Arrays.asList("a", "b", "c");
        Predicate<String> predicate = s -> s.equals("b");
        Supplier<String> supplier = () -> "default";

        String result = FuncUtil.get(list, predicate, supplier);

        assertEquals("b", result);
    }

    @Test
    void testGetWithNoMatchingElement() {
        List<String> list = Arrays.asList("a", "b", "c");
        Predicate<String> predicate = s -> s.equals("d");
        Supplier<String> supplier = () -> "default";

        String result = FuncUtil.get(list, predicate, supplier);

        assertEquals("default", result);
    }

    @Test
    void testGetWithEmptyCollection() {
        List<String> list = Collections.emptyList();
        Predicate<String> predicate = s -> s.equals("d");
        Supplier<String> supplier = () -> "default";

        String result = FuncUtil.get(list, predicate, supplier);

        assertEquals("default", result);
    }

    @Test
    void testGetWithNullPredicate() {
        List<String> list = Arrays.asList("a", "b", "c");
        Predicate<String> predicate = null;
        Supplier<String> supplier = () -> "default";

        assertThrows(NullPointerException.class, () -> FuncUtil.get(list, predicate, supplier));
    }

    @Test
    void testGetWithNullSupplier() {
        List<String> list = Arrays.asList("a", "b", "c");
        Predicate<String> predicate = s -> s.equals("b");
        Supplier<String> supplier = null;

        assertThrows(NullPointerException.class, () -> FuncUtil.get(list, predicate, supplier));
    }
}
