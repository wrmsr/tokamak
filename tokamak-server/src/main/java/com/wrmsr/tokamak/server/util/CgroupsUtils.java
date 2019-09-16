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

public class CgroupsUtils
{
    /*
    /sys/fs/cgroup/memory/memory.limit_in_bytes

    http://man7.org/linux/man-pages/man7/cgroups.7.html

    /sys/fs/cgroup
    /sys/fs/cgroup/systemd
    /sys/fs/cgroup/systemd/cgroup.clone_children
    /sys/fs/cgroup/systemd/tasks
    /sys/fs/cgroup/systemd/notify_on_release
    /sys/fs/cgroup/systemd/cgroup.procs
    /sys/fs/cgroup/pids
    /sys/fs/cgroup/pids/pids.current
    /sys/fs/cgroup/pids/cgroup.clone_children
    /sys/fs/cgroup/pids/pids.max
    /sys/fs/cgroup/pids/pids.events
    /sys/fs/cgroup/pids/tasks
    /sys/fs/cgroup/pids/notify_on_release
    /sys/fs/cgroup/pids/cgroup.procs
    /sys/fs/cgroup/hugetlb
    /sys/fs/cgroup/hugetlb/hugetlb.2MB.failcnt
    /sys/fs/cgroup/hugetlb/hugetlb.1GB.limit_in_bytes
    /sys/fs/cgroup/hugetlb/cgroup.clone_children
    /sys/fs/cgroup/hugetlb/hugetlb.1GB.max_usage_in_bytes
    /sys/fs/cgroup/hugetlb/hugetlb.2MB.max_usage_in_bytes
    /sys/fs/cgroup/hugetlb/hugetlb.2MB.usage_in_bytes
    /sys/fs/cgroup/hugetlb/tasks
    /sys/fs/cgroup/hugetlb/notify_on_release
    /sys/fs/cgroup/hugetlb/hugetlb.1GB.usage_in_bytes
    /sys/fs/cgroup/hugetlb/cgroup.procs
    /sys/fs/cgroup/hugetlb/hugetlb.1GB.failcnt
    /sys/fs/cgroup/hugetlb/hugetlb.2MB.limit_in_bytes
    /sys/fs/cgroup/net_prio
    /sys/fs/cgroup/net_prio/cgroup.clone_children
    /sys/fs/cgroup/net_prio/net_prio.ifpriomap
    /sys/fs/cgroup/net_prio/tasks
    /sys/fs/cgroup/net_prio/notify_on_release
    /sys/fs/cgroup/net_prio/cgroup.procs
    /sys/fs/cgroup/net_prio/net_prio.prioidx
    /sys/fs/cgroup/perf_event
    /sys/fs/cgroup/perf_event/cgroup.clone_children
    /sys/fs/cgroup/perf_event/tasks
    /sys/fs/cgroup/perf_event/notify_on_release
    /sys/fs/cgroup/perf_event/cgroup.procs
    /sys/fs/cgroup/net_cls
    /sys/fs/cgroup/net_cls/cgroup.clone_children
    /sys/fs/cgroup/net_cls/net_cls.classid
    /sys/fs/cgroup/net_cls/tasks
    /sys/fs/cgroup/net_cls/notify_on_release
    /sys/fs/cgroup/net_cls/cgroup.procs
    /sys/fs/cgroup/freezer
    /sys/fs/cgroup/freezer/freezer.parent_freezing
    /sys/fs/cgroup/freezer/cgroup.clone_children
    /sys/fs/cgroup/freezer/freezer.state
    /sys/fs/cgroup/freezer/tasks
    /sys/fs/cgroup/freezer/notify_on_release
    /sys/fs/cgroup/freezer/cgroup.procs
    /sys/fs/cgroup/freezer/freezer.self_freezing
    /sys/fs/cgroup/devices
    /sys/fs/cgroup/devices/devices.list
    /sys/fs/cgroup/devices/cgroup.clone_children
    /sys/fs/cgroup/devices/devices.allow
    /sys/fs/cgroup/devices/tasks
    /sys/fs/cgroup/devices/notify_on_release
    /sys/fs/cgroup/devices/cgroup.procs
    /sys/fs/cgroup/devices/devices.deny
    /sys/fs/cgroup/memory
    /sys/fs/cgroup/memory/memory.memsw.usage_in_bytes
    /sys/fs/cgroup/memory/memory.use_hierarchy
    /sys/fs/cgroup/memory/memory.kmem.tcp.max_usage_in_bytes
    /sys/fs/cgroup/memory/memory.kmem.slabinfo
    /sys/fs/cgroup/memory/memory.kmem.tcp.usage_in_bytes
    /sys/fs/cgroup/memory/memory.kmem.failcnt
    /sys/fs/cgroup/memory/cgroup.clone_children
    /sys/fs/cgroup/memory/memory.force_empty
    /sys/fs/cgroup/memory/memory.max_usage_in_bytes
    /sys/fs/cgroup/memory/cgroup.event_control
    /sys/fs/cgroup/memory/memory.swappiness
    /sys/fs/cgroup/memory/memory.limit_in_bytes
    /sys/fs/cgroup/memory/memory.kmem.usage_in_bytes
    /sys/fs/cgroup/memory/memory.pressure_level
    /sys/fs/cgroup/memory/memory.memsw.failcnt
    /sys/fs/cgroup/memory/memory.memsw.limit_in_bytes
    /sys/fs/cgroup/memory/memory.kmem.max_usage_in_bytes
    /sys/fs/cgroup/memory/memory.kmem.tcp.limit_in_bytes
    /sys/fs/cgroup/memory/memory.stat
    /sys/fs/cgroup/memory/tasks
    /sys/fs/cgroup/memory/notify_on_release
    /sys/fs/cgroup/memory/memory.kmem.tcp.failcnt
    /sys/fs/cgroup/memory/memory.oom_control
    /sys/fs/cgroup/memory/memory.kmem.limit_in_bytes
    /sys/fs/cgroup/memory/cgroup.procs
    /sys/fs/cgroup/memory/memory.soft_limit_in_bytes
    /sys/fs/cgroup/memory/memory.failcnt
    /sys/fs/cgroup/memory/memory.memsw.max_usage_in_bytes
    /sys/fs/cgroup/memory/memory.usage_in_bytes
    /sys/fs/cgroup/memory/memory.move_charge_at_immigrate
    /sys/fs/cgroup/blkio
    /sys/fs/cgroup/blkio/blkio.sectors
    /sys/fs/cgroup/blkio/blkio.io_serviced
    /sys/fs/cgroup/blkio/blkio.time_recursive
    /sys/fs/cgroup/blkio/blkio.throttle.read_bps_device
    /sys/fs/cgroup/blkio/blkio.throttle.write_bps_device
    /sys/fs/cgroup/blkio/blkio.weight_device
    /sys/fs/cgroup/blkio/blkio.io_queued
    /sys/fs/cgroup/blkio/blkio.throttle.write_iops_device
    /sys/fs/cgroup/blkio/blkio.io_merged
    /sys/fs/cgroup/blkio/cgroup.clone_children
    /sys/fs/cgroup/blkio/blkio.time
    /sys/fs/cgroup/blkio/blkio.io_service_bytes_recursive
    /sys/fs/cgroup/blkio/blkio.io_wait_time
    /sys/fs/cgroup/blkio/blkio.sectors_recursive
    /sys/fs/cgroup/blkio/blkio.io_service_time_recursive
    /sys/fs/cgroup/blkio/blkio.leaf_weight
    /sys/fs/cgroup/blkio/blkio.weight
    /sys/fs/cgroup/blkio/blkio.io_serviced_recursive
    /sys/fs/cgroup/blkio/blkio.io_service_bytes
    /sys/fs/cgroup/blkio/blkio.io_queued_recursive
    /sys/fs/cgroup/blkio/blkio.leaf_weight_device
    /sys/fs/cgroup/blkio/tasks
    /sys/fs/cgroup/blkio/blkio.throttle.io_service_bytes
    /sys/fs/cgroup/blkio/blkio.throttle.io_serviced
    /sys/fs/cgroup/blkio/notify_on_release
    /sys/fs/cgroup/blkio/blkio.reset_stats
    /sys/fs/cgroup/blkio/blkio.io_merged_recursive
    /sys/fs/cgroup/blkio/blkio.io_service_time
    /sys/fs/cgroup/blkio/cgroup.procs
    /sys/fs/cgroup/blkio/blkio.io_wait_time_recursive
    /sys/fs/cgroup/blkio/blkio.throttle.read_iops_device
    /sys/fs/cgroup/cpuacct
    /sys/fs/cgroup/cpuacct/cpuacct.usage
    /sys/fs/cgroup/cpuacct/cpuacct.stat
    /sys/fs/cgroup/cpuacct/cgroup.clone_children
    /sys/fs/cgroup/cpuacct/cpuacct.usage_percpu_sys
    /sys/fs/cgroup/cpuacct/cpuacct.usage_user
    /sys/fs/cgroup/cpuacct/cpuacct.usage_percpu_user
    /sys/fs/cgroup/cpuacct/cpuacct.usage_percpu
    /sys/fs/cgroup/cpuacct/tasks
    /sys/fs/cgroup/cpuacct/notify_on_release
    /sys/fs/cgroup/cpuacct/cgroup.procs
    /sys/fs/cgroup/cpuacct/cpuacct.usage_sys
    /sys/fs/cgroup/cpuacct/cpuacct.usage_all
    /sys/fs/cgroup/cpu
    /sys/fs/cgroup/cpu/cpu.stat
    /sys/fs/cgroup/cpu/cpu.rt_runtime_us
    /sys/fs/cgroup/cpu/cpu.rt_period_us
    /sys/fs/cgroup/cpu/cgroup.clone_children
    /sys/fs/cgroup/cpu/cpu.cfs_period_us
    /sys/fs/cgroup/cpu/cpu.cfs_quota_us
    /sys/fs/cgroup/cpu/tasks
    /sys/fs/cgroup/cpu/notify_on_release
    /sys/fs/cgroup/cpu/cpu.shares
    /sys/fs/cgroup/cpu/cgroup.procs
    /sys/fs/cgroup/cpuset
    /sys/fs/cgroup/cpuset/cpuset.mems
    /sys/fs/cgroup/cpuset/cpuset.sched_relax_domain_level
    /sys/fs/cgroup/cpuset/cpuset.mem_exclusive
    /sys/fs/cgroup/cpuset/cpuset.memory_pressure
    /sys/fs/cgroup/cpuset/cpuset.cpus
    /sys/fs/cgroup/cpuset/cpuset.mem_hardwall
    /sys/fs/cgroup/cpuset/cpuset.memory_migrate
    /sys/fs/cgroup/cpuset/cgroup.clone_children
    /sys/fs/cgroup/cpuset/cpuset.memory_spread_page
    /sys/fs/cgroup/cpuset/cpuset.sched_load_balance
    /sys/fs/cgroup/cpuset/cpuset.cpu_exclusive
    /sys/fs/cgroup/cpuset/cpuset.effective_mems
    /sys/fs/cgroup/cpuset/cpuset.effective_cpus
    /sys/fs/cgroup/cpuset/tasks
    /sys/fs/cgroup/cpuset/notify_on_release
    /sys/fs/cgroup/cpuset/cpuset.memory_spread_slab
    /sys/fs/cgroup/cpuset/cgroup.procs
    */
}
