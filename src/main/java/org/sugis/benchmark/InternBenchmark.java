package org.sugis.benchmark;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@SuppressWarnings("static-method")
public class InternBenchmark
{
    private static final int N_STRINGS = 10000;
    private static final int N_STRINGS_LEN = Integer.toString(N_STRINGS).length();
    private static final int ID_LEN = UUID.randomUUID().toString().length();

    @State(Scope.Thread)
    public static class ThreadUniqueId {
        final String id = UUID.randomUUID().toString();
    }

    private static void incrementCharBuf(char[] buf)
    {
        int pos = buf.length - 1;
        while (buf[pos]++ > '9') {
            buf[pos] = 0;
            pos--;
        }
    }

    private static int runTest(ThreadUniqueId id, InternImpl interner, int padLen) {
        int result = 0;
        char[] buf = new char[ID_LEN + padLen];

        Arrays.fill(buf, ID_LEN, ID_LEN + padLen, '0');

        id.id.getChars(0, ID_LEN, buf, 0);

        for (int i = 0; i < N_STRINGS; i++) {
            incrementCharBuf(buf);

            result += interner.intern(new String(buf)).length();
        }
        return result;
    }

    @GenerateMicroBenchmark
    @BenchmarkMode(Mode.SampleTime)
    public int testShortStringNoIntern(ThreadUniqueId id) {
        return runTest(id, new NopInternImpl(), N_STRINGS_LEN);
    }

    @GenerateMicroBenchmark
    @BenchmarkMode(Mode.SampleTime)
    public int testLongStringNoIntern(ThreadUniqueId id) {
        return runTest(id, new NopInternImpl(), N_STRINGS_LEN * 10);
    }

    @GenerateMicroBenchmark
    @BenchmarkMode(Mode.SampleTime)
    public int testShortStringJdkIntern(ThreadUniqueId id) {
        return runTest(id, new JdkInternImpl(), N_STRINGS_LEN);
    }

    @GenerateMicroBenchmark
    @BenchmarkMode(Mode.SampleTime)
    public int testLongStringJdkIntern(ThreadUniqueId id) {
        return runTest(id, new JdkInternImpl(), N_STRINGS_LEN * 10);
    }

    @GenerateMicroBenchmark
    @BenchmarkMode(Mode.SampleTime)
    public int testShortStringShmIntern(ThreadUniqueId id, ShmInternImpl intern) {
        return runTest(id, intern, N_STRINGS_LEN);
    }

    @GenerateMicroBenchmark
    @BenchmarkMode(Mode.SampleTime)
    public int testLongStringShmIntern(ThreadUniqueId id, ShmInternImpl intern) {
        return runTest(id, intern, N_STRINGS_LEN * 10);
    }

    @GenerateMicroBenchmark
    @BenchmarkMode(Mode.SampleTime)
    public int testShortStringChmIntern(ThreadUniqueId id, ChmInternImpl chmIntern) {
        return runTest(id, chmIntern, N_STRINGS_LEN);
    }

    @GenerateMicroBenchmark
    @BenchmarkMode(Mode.SampleTime)
    public int testLongStringChmIntern(ThreadUniqueId id, ChmInternImpl chmIntern) {
        return runTest(id, chmIntern, N_STRINGS_LEN * 10);
    }


    public interface InternImpl
    {
        String intern(String input);
    }

    public static class NopInternImpl implements InternImpl
    {
        @Override
        public String intern(String input)
        {
            return input;
        }
    }

    public static class JdkInternImpl implements InternImpl
    {
        @Override
        public String intern(String input)
        {
            return input.intern();
        }
    }

    @State(Scope.Benchmark)
    public static class ShmInternImpl implements InternImpl
    {
        private final Map<String, String> interner = Collections.synchronizedMap(new HashMap<String, String>());
        @Override
        public String intern(String input)
        {
            synchronized (interner) {
                String interned = interner.get(input);
                if (interned != null) {
                    return interned;
                }
                interner.put(input, input);
                return input;
            }
        }
    }

    @State(Scope.Benchmark)
    public static class ChmInternImpl implements InternImpl
    {
        private final ConcurrentMap<String, String> interner = new ConcurrentHashMap<String, String>();
        @Override
        public String intern(String input)
        {
            String result = interner.putIfAbsent(input, input);
            return result == null ? input : result;
        }
    }
}
