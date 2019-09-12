/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wrmsr.tokamak.server.util;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.util.collect.StreamableIterable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;

public final class LinuxProc
{
    private LinuxProc()
    {
    }

    private static final Map<String, Integer> SIZE_BASES = ImmutableMap.of(
            "kB", 1024,
            "mB", 1024 * 1024
    );

    private static int parseSize(String str)
    {
        List<String> parts = Splitter.on(" ").splitToList(str);
        checkArgument(parts.size() == 2);
        return Integer.parseInt(parts.get(0)) * SIZE_BASES.get(parts.get(1));
    }

    public enum Stat
    {
        PID(0),
        COMM(1),
        STATE(2),
        PPID(3),
        PGRP(4),
        SESSION(5),
        TTY_NR(6),
        TPGID(7),
        FLAGS(8),
        MINFLT(9),
        CMINFLT(10),
        MAJFLT(11),
        CMAJFLT(12),
        UTIME(13),
        STIME(14),
        CUTIME(15),
        CSTIME(16),
        PRIORITY(17),
        NICE(18),
        NUM_THREADS(19),
        ITREALVALUE(20),
        STARTTIME(21),
        VSIZE(22),
        RSS(23),
        RSSLIM(24),
        STARTCODE(25),
        ENDCODE(26),
        STARTSTACK(27),
        KSTKESP(28),
        KSTKEIP(29),
        SIGNAL(30),
        BLOCKED(31),
        SIGIGNORE(32),
        SIGCATCH(33),
        WCHAN(34),
        NSWAP(35),
        CNSWAP(36),
        EXIT_SIGNAL(37),
        PROCESSOR(38),
        RT_PRIORITY(39),
        POLICY(40),
        DELAYACCT_BLKIO_TICKS(41),
        GUEST_TIME(42),
        CGUEST_TIME(43),
        START_DATA(44),
        END_DATA(45),
        START_BRK(46),
        ARG_START(47),
        ARG_END(48),
        ENV_START(49),
        ENV_END(50),
        EXIT_CODE(51);

        private final int num;

        Stat(int num)
        {
            this.num = num;
        }

        public int getNum()
        {
            return num;
        }

        public static final Map<Integer, Stat> BY_NUM = Arrays.stream(Stat.class.getEnumConstants())
                .collect(toImmutableMap(s -> ((Stat) s).getNum(), s -> ((Stat) s)));
    }

    public static final class Stats
            implements StreamableIterable<String>
    {
        private final List<String> stats;

        public Stats(List<String> stats)
        {
            this.stats = ImmutableList.copyOf(stats);
        }

        @Override
        public String toString()
        {
            return "Stats{" +
                    "stats=" + stats +
                    '}';
        }

        public String get(int pos)
        {
            return stats.get(pos);
        }

        public String get(Stat stat)
        {
            return get(stat.getNum());
        }

        public int getInt(int pos)
        {
            return Integer.parseInt(stats.get(pos));
        }

        public int getInt(Stat stat)
        {
            return getInt(stat.getNum());
        }

        public int getPid()
        {
            return getInt(Stat.PID);
        }

        public String getCommand()
        {
            return get(Stat.COMM);
        }

        @Override
        public Iterator<String> iterator()
        {
            return stats.iterator();
        }
    }

    public static Stats parseStats(String line)
    {
        int pl = line.indexOf('(');
        int pr = line.lastIndexOf(')');
        checkArgument(pl != -1 && pr != -1 && pl < pr);
        return new Stats(
                ImmutableList.<String>builder()
                        .add(line.substring(0, pl).trim())
                        .add(line.substring(pl + 1, pr))
                        .addAll(Splitter.on(CharMatcher.whitespace()).split(line.substring(pr + 1)))
                        .build());
    }

    public static String readStatLine(String pid)
            throws IOException
    {
        Path path = Paths.get(String.format("/proc/%s/stat", pid));
        return new String(Files.readAllBytes(path), Charsets.UTF_8);
    }

    public static Stats getStats(String pid)
            throws IOException
    {
        String line = readStatLine(pid);
        return parseStats(line);
    }

    public static List<Stats> getStatsChain(String pid)
            throws IOException
    {
        ImmutableList.Builder<Stats> builder = ImmutableList.builder();
        HashSet<String> seen = new HashSet<>();
        while (true) {
            if (seen.contains(pid)) {
                break;
            }
            seen.add(pid);
            Stats stats = getStats(pid);
            builder.add(stats);
            pid = stats.get(Stat.PPID);
        }
        return builder.build();
    }
}
