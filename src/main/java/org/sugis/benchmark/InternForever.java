package org.sugis.benchmark;

import java.lang.management.ManagementFactory;

public class InternForever
{
    public static void main(String[] args)
    {
        System.out.println("Running on " + ManagementFactory.getRuntimeMXBean().getName());
        char[] chars = new char[64];
        while (true) {
            for (int i = chars.length - 1; i >= 0; i--) {
                if (chars[i]++ < 127) {
                    break;
                }
                chars[i] = 0;
            }

            new String(chars).intern();
        }
    }
}
