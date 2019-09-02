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

package com.wrmsr.util;

import com.wrmsr.tokamak.util.LinuxProc;
import junit.framework.TestCase;

public class LinuxProcTest
        extends TestCase
{
    public static final String PROC_STAT_LINE = "26725 (cat) R 26692 26725 26692 34825 26725 4218880 227 0 0 0 0 0 0 0 20 0 1 0 7415864476 6082560 152 18446744073709551615 4194304 4238788 140732266433936 140732266433936 139721341711200 0 0 0 0 0 0 0 17 0 0 0 0 0 0 6336016 6337300 22945792 140732266436725 140732266436745 140732266436745 140732266438639 0";

    public void testProcStats()
            throws Throwable
    {
        LinuxProc.Stats stats = LinuxProc.parseStats(PROC_STAT_LINE);
        System.out.println(stats);
        System.out.println(stats.getPid());
        System.out.println(stats.getCommand());
        System.out.println(LinuxProc.Stat.BY_NUM);
    }
}
