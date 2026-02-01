"""
负载测试脚本

模拟高并发场景下的服务表现，测试:
1. 持续负载下的稳定性
2. 峰值并发处理能力
3. 内存使用情况
4. 错误率变化趋势
"""
import asyncio
import time
import psutil
import os
from typing import List, Dict, Any
from dataclasses import dataclass, field
from datetime import datetime
import json
import matplotlib.pyplot as plt
import numpy as np

# 导入算法模块
from algorithms.fall_detection.safety_guardian import SafetyGuardian
from algorithms.risk_assessment.health_oracle import HealthOracle
from algorithms.abnormal_detection.threshold_detector import ThresholdDetector
from utils.async_llm_client import AsyncLLMClient
from utils.logger import get_logger

logger = get_logger("qkyd_ai.load_test")


@dataclass
class LoadTestResult:
    """负载测试结果"""
    name: str
    duration: float
    total_requests: int
    successful_requests: int
    failed_requests: int
    avg_rps: float  # 每秒请求数
    peak_rps: float
    avg_latency: float
    p50_latency: float
    p95_latency: float
    p99_latency: float
    memory_mb: float
    cpu_percent: float
    latency_samples: List[float] = field(default_factory=list)
    rps_samples: List[float] = field(default_factory=list)
    error_timeline: List[Dict[str, Any]] = field(default_factory=list)

    def to_dict(self) -> Dict[str, Any]:
        return {
            "name": self.name,
            "duration": round(self.duration, 2),
            "total_requests": self.total_requests,
            "successful_requests": self.successful_requests,
            "failed_requests": self.failed_requests,
            "avg_rps": round(self.avg_rps, 2),
            "peak_rps": round(self.peak_rps, 2),
            "avg_latency": round(self.avg_latency * 1000, 2),  # ms
            "p50_latency": round(self.p50_latency * 1000, 2),
            "p95_latency": round(self.p95_latency * 1000, 2),
            "p99_latency": round(self.p99_latency * 1000, 2),
            "memory_mb": round(self.memory_mb, 2),
            "cpu_percent": round(self.cpu_percent, 2),
            "error_rate": round(self.failed_requests / self.total_requests * 100, 2) if self.total_requests > 0 else 0
        }


class LoadTest:
    """负载测试框架"""

    def __init__(self):
        self.process = psutil.Process(os.getpid())
        self.llm_client = None
        self.results: List[LoadTestResult] = []

    async def setup(self):
        """初始化测试环境"""
        logger.info("Setting up load test environment...")
        try:
            self.llm_client = AsyncLLMClient()
            logger.info("✅ LLM Client initialized")
        except Exception as e:
            logger.warning(f"⚠️  LLM Client not available: {e}")

    async def teardown(self):
        """清理测试环境"""
        if self.llm_client:
            await self.llm_client.close()

    def _get_memory_mb(self) -> float:
        """获取内存使用（MB）"""
        return self.process.memory_info().rss / 1024 / 1024

    def _get_cpu_percent(self) -> float:
        """获取 CPU 使用率"""
        return self.process.cpu_percent(interval=0.1)

    async def _measure_resource_usage(self, duration: float) -> Dict[str, float]:
        """测量资源使用情况"""
        memory_samples = []
        cpu_samples = []

        start_time = time.time()
        while time.time() - start_time < duration:
            memory_samples.append(self._get_memory_mb())
            cpu_samples.append(self._get_cpu_percent())
            await asyncio.sleep(0.5)

        return {
            "memory_mb": statistics.mean(memory_samples) if memory_samples else 0,
            "cpu_percent": statistics.mean(cpu_samples) if cpu_samples else 0
        }

    async def run_sustained_load_test(
        self,
        name: str,
        request_func,
        duration: float = 60.0,
        target_rps: float = 100.0
    ) -> LoadTestResult:
        """
        运行持续负载测试

        Args:
            name: 测试名称
            request_func: 请求函数
            duration: 测试持续时间（秒）
            target_rps: 目标每秒请求数

        Returns:
            LoadTestResult
        """
        logger.info(f"Running sustained load test: {name} (duration={duration}s, target_rps={target_rps})")

        latencies = []
        errors = []
        start_time = time.time()

        # 计算请求间隔
        interval = 1.0 / target_rps if target_rps > 0 else 0

        async def send_requests():
            while time.time() - start_time < duration:
                req_start = time.time()
                try:
                    await request_func()
                    latency = time.time() - req_start
                    latencies.append(latency)
                except Exception as e:
                    errors.append({
                        "time": time.time() - start_time,
                        "error": str(e)
                    })

                # 控制请求速率
                elapsed = time.time() - req_start
                wait_time = max(0, interval - elapsed)
                if wait_time > 0:
                    await asyncio.sleep(wait_time)

        # 启动资源监控
        monitor_task = asyncio.create_task(self._measure_resource_usage(duration))

        # 启动请求任务（可以多个协程并发）
        tasks = [asyncio.create_task(send_requests()) for _ in range(10)]  # 10 并发发送器

        # 等待所有任务完成
        await asyncio.gather(*tasks, return_exceptions=True)

        # 获取资源使用数据
        resource_data = await monitor_task

        actual_duration = time.time() - start_time
        total_requests = len(latencies) + len(errors)

        # 计算 RPS 样本
        rps_samples = []
        window_size = 1.0  # 1秒窗口
        for i in range(int(actual_duration)):
            window_start = i
            window_end = i + window_size
            window_requests = sum(
                1 for l in latencies
                if window_start <= sum(latencies[:latencies.index(l) + 1], 0.0) < window_end
            )
            rps_samples.append(window_requests / window_size)

        result = LoadTestResult(
            name=name,
            duration=actual_duration,
            total_requests=total_requests,
            successful_requests=len(latencies),
            failed_requests=len(errors),
            avg_rps=total_requests / actual_duration,
            peak_rps=max(rps_samples) if rps_samples else 0,
            avg_latency=np.mean(latencies) if latencies else 0,
            p50_latency=np.percentile(latencies, 50) if latencies else 0,
            p95_latency=np.percentile(latencies, 95) if latencies else 0,
            p99_latency=np.percentile(latencies, 99) if latencies else 0,
            memory_mb=resource_data["memory_mb"],
            cpu_percent=resource_data["cpu_percent"],
            latency_samples=latencies,
            rps_samples=rps_samples,
            error_timeline=errors
        )

        self.results.append(result)
        logger.info(f"  ✅ {name}: avg_rps={result.avg_rps:.2f}, avg_latency={result.avg_latency*1000:.2f}ms, errors={len(errors)}")

        return result

    async def run_spike_test(
        self,
        name: str,
        request_func,
        base_concurrency: int = 10,
        spike_concurrency: int = 100,
        spike_duration: float = 10.0
    ) -> LoadTestResult:
        """
        运行峰值测试

        模拟从正常负载突然增加到峰值负载的场景
        """
        logger.info(f"Running spike test: {name} (base={base_concurrency}, spike={spike_concurrency})")

        latencies = []
        errors = []
        start_time = time.time()

        async def worker():
            while True:
                req_start = time.time()
                try:
                    await request_func()
                    latencies.append(time.time() - req_start)
                except Exception as e:
                    errors.append({"time": time.time() - start_time, "error": str(e)})

        # 启动基准并发
        base_tasks = [asyncio.create_task(worker()) for _ in range(base_concurrency)]
        await asyncio.sleep(5.0)  # 基准运行 5 秒

        # 突然增加并发
        logger.info(f"  📈 Spiking from {base_concurrency} to {spike_concurrency}...")
        spike_tasks = [asyncio.create_task(worker()) for _ in range(spike_concurrency - base_concurrency)]

        # 峰值运行
        await asyncio.sleep(spike_duration)

        # 取消所有任务
        for task in base_tasks + spike_tasks:
            task.cancel()

        try:
            await asyncio.gather(*base_tasks + spike_tasks, return_exceptions=True)
        except asyncio.CancelledError:
            pass

        duration = time.time() - start_time

        result = LoadTestResult(
            name=name,
            duration=duration,
            total_requests=len(latencies) + len(errors),
            successful_requests=len(latencies),
            failed_requests=len(errors),
            avg_rps=(len(latencies) + len(errors)) / duration,
            peak_rps=0,  # 不适用于峰值测试
            avg_latency=np.mean(latencies) if latencies else 0,
            p50_latency=np.percentile(latencies, 50) if latencies else 0,
            p95_latency=np.percentile(latencies, 95) if latencies else 0,
            p99_latency=np.percentile(latencies, 99) if latencies else 0,
            memory_mb=self._get_memory_mb(),
            cpu_percent=self._get_cpu_percent(),
            latency_samples=latencies,
            rps_samples=[],
            error_timeline=errors
        )

        self.results.append(result)
        return result

    # ========================================================================
    # 运行所有测试
    # ========================================================================

    async def run_all_tests(self):
        """运行所有负载测试"""
        print("\n" + "=" * 80)
        print("🔥 Starting Load Tests")
        print("=" * 80 + "\n")

        await self.setup()

        try:
            # 准备测试函数
            detector = SafetyGuardian(llm_client=None)

            async def simple_request():
                return detector._threshold_detection({
                    "acc_x": 5,
                    "acc_y": 5,
                    "acc_z": 25
                })

            # 1. 低负载测试
            print("\n--- 1. Low Load Test (10 RPS, 30s) ---\n")
            await self.run_sustained_load_test(
                "Low Load (10 RPS)",
                simple_request,
                duration=30.0,
                target_rps=10.0
            )

            # 2. 中等负载测试
            print("\n--- 2. Medium Load Test (100 RPS, 30s) ---\n")
            await self.run_sustained_load_test(
                "Medium Load (100 RPS)",
                simple_request,
                duration=30.0,
                target_rps=100.0
            )

            # 3. 高负载测试
            print("\n--- 3. High Load Test (500 RPS, 30s) ---\n")
            await self.run_sustained_load_test(
                "High Load (500 RPS)",
                simple_request,
                duration=30.0,
                target_rps=500.0
            )

            # 4. 峰值测试
            print("\n--- 4. Spike Test (10 -> 100 concurrent) ---\n")
            await self.run_spike_test(
                "Spike Test",
                simple_request,
                base_concurrency=10,
                spike_concurrency=100,
                spike_duration=10.0
            )

            # 5. 生成报告
            self.generate_report()
            self.generate_charts()

        finally:
            await self.teardown()

    def generate_report(self):
        """生成负载测试报告"""
        print("\n" + "=" * 80)
        print("📊 Load Test Report")
        print("=" * 80 + "\n")

        for result in self.results:
            print(f"### {result.name}")
            print(f"  Duration:       {result.duration:.1f}s")
            print(f"  Total Requests: {result.total_requests}")
            print(f"  Successful:     {result.successful_requests} ({result.successful_requests/result.total_requests*100:.1f}%)")
            print(f"  Failed:         {result.failed_requests}")
            print(f"  Avg RPS:        {result.avg_rps:.2f}")
            print(f"  Avg Latency:    {result.avg_latency*1000:.2f}ms")
            print(f"  P95 Latency:    {result.p95_latency*1000:.2f}ms")
            print(f"  Memory:         {result.memory_mb:.2f} MB")
            print(f"  CPU:            {result.cpu_percent:.1f}%")
            print()

        # 保存 JSON 报告
        report_path = "logs/load_test_report.json"
        import os
        os.makedirs("logs", exist_ok=True)

        report_data = {
            "timestamp": datetime.now().isoformat(),
            "results": [r.to_dict() for r in self.results]
        }

        with open(report_path, "w", encoding="utf-8") as f:
            json.dump(report_data, f, indent=2, ensure_ascii=False)

        print(f"✅ Report saved to: {report_path}\n")

    def generate_charts(self):
        """生成性能图表"""
        import os
        os.makedirs("logs", exist_ok=True)

        # 按测试类型分组结果
        sustained_tests = [r for r in self.results if "Load" in r.name]

        if not sustained_tests:
            return

        # 创建图表
        fig, axes = plt.subplots(2, 2, figsize=(14, 10))
        fig.suptitle('Load Test Results', fontsize=16)

        # 1. RPS 对比
        ax1 = axes[0, 0]
        names = [r.name for r in sustained_tests]
        avg_rps = [r.avg_rps for r in sustained_tests]
        ax1.bar(names, avg_rps, color='steelblue')
        ax1.set_ylabel('Requests Per Second')
        ax1.set_title('Achieved RPS vs Target')
        ax1.tick_params(axis='x', rotation=15)

        # 2. 延迟对比
        ax2 = axes[0, 1]
        avg_lat = [r.avg_latency * 1000 for r in sustained_tests]
        p95_lat = [r.p95_latency * 1000 for r in sustained_tests]
        x = np.arange(len(names))
        width = 0.35
        ax2.bar(x - width/2, avg_lat, width, label='Avg', color='steelblue')
        ax2.bar(x + width/2, p95_lat, width, label='P95', color='coral')
        ax2.set_ylabel('Latency (ms)')
        ax2.set_title('Latency Comparison')
        ax2.set_xticks(x)
        ax2.set_xticklabels(names, rotation=15)
        ax2.legend()

        # 3. 错误率
        ax3 = axes[1, 0]
        error_rates = [r.failed_requests / r.total_requests * 100 if r.total_requests > 0 else 0
                       for r in sustained_tests]
        ax3.bar(names, error_rates, color='salmon')
        ax3.set_ylabel('Error Rate (%)')
        ax3.set_title('Error Rate by Load Level')
        ax3.tick_params(axis='x', rotation=15)

        # 4. 资源使用
        ax4 = axes[1, 1]
        memory = [r.memory_mb for r in sustained_tests]
        cpu = [r.cpu_percent for r in sustained_tests]
        ax4_twin = ax4.twinx()
        ax4.bar(names, memory, width=0.4, color='lightblue', label='Memory (MB)')
        ax4_twin.plot(names, cpu, 'o-', color='orange', label='CPU (%)', linewidth=2)
        ax4.set_ylabel('Memory (MB)')
        ax4_twin.set_ylabel('CPU (%)')
        ax4.set_title('Resource Usage')
        ax4.tick_params(axis='x', rotation=15)
        ax4.legend(loc='upper left')
        ax4_twin.legend(loc='upper right')

        plt.tight_layout()
        chart_path = "logs/load_test_charts.png"
        plt.savefig(chart_path, dpi=300, bbox_inches='tight')
        print(f"✅ Charts saved to: {chart_path}\n")


# ============================================================================
# 主函数
# ============================================================================

async def main():
    """主函数"""
    import statistics  # 确保导入
    load_test = LoadTest()
    await load_test.run_all_tests()


if __name__ == "__main__":
    asyncio.run(main())
