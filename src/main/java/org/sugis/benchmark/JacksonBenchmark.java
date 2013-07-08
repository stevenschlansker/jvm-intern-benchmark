package org.sugis.benchmark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@SuppressWarnings("static-method")
public class JacksonBenchmark
{

    private static final int N_ENTRIES = 10000;

    public static class SimpleObject
    {
        public int myInt;
        public double myDouble;
        public List<String> myStrings = new ArrayList<String>();

        public SimpleObject() { }

        public SimpleObject(Random random) {
            myInt = random.nextInt();
            myDouble = random.nextDouble();

            for (int i = 0; i < random.nextInt(10); i++) {
                myStrings.add(UUID.randomUUID().toString());
            }
        }
    }

    @State(Scope.Benchmark)
    public static class SerData
    {
        private static final TypeReference<Map<String, SimpleObject>> TYPE = new TypeReference<Map<String, SimpleObject>>() {};

        private final ObjectMapper mapper = new ObjectMapper();
        private final Random random = new Random();

        private final byte[] data;

        public SerData() {
            Map<String, SimpleObject> map = new HashMap<String, SimpleObject>();

            for (int i = 0; i < N_ENTRIES; i++) {
                map.put(UUID.randomUUID().toString(), new SimpleObject(random));
            }

            try {
                data = mapper.writeValueAsBytes(map);
            } catch (IOException e) {
                e.printStackTrace();
                throw new ExceptionInInitializerError(e);
            }
        }
    }

    @GenerateMicroBenchmark
    @BenchmarkMode(Mode.SampleTime)
    public Object testJacksonSpeed(SerData data) throws Exception {
        return data.mapper.readValue(data.data, SerData.TYPE);
    }

    public static void main(String[] args) throws Exception
    {
        new JacksonBenchmark().testJacksonSpeed(new SerData());
        System.out.println("Done!");
    }
}
