"""
算法服务性能基准测试

测试内容:
1. 单次请求延迟
2. 并发请求性能
3. LLM 缓存效果
4. 同步 vs 异步对比
5. 不同算法基准性能
"""
import asyncio
import time
import statistics
from typing import List, Dict, Any, Callable
from dataclasses import dataclass
from datetime import datetime
import json

# 导入算法模块
from algorithms.fall_detection.safety_guardian import SafetyGuardian
from algorithms.risk_assessment.health_oracle import HealthOracle
from algorithms.data_quality.data_sentinel import DataSentinel
from algorithms.abnormal_detection.threshold_detector import ThresholdDetector
from algorithms.abnormal_detection.statistical_detector import StatisticalDetector

# 导入异步 LLM 客户端
from utils.async_llm_client import AsyncLLMClient
from utils.logger import get_logger

logger = get_logger("qkyd_ai.performance")


@dataclass
class BenchmarkResult:
    """基准测试结果"""
    name: str
    total_time: float
    avg_time: float
    min_time: float
    max_time: float
    p50_time: float
    p95_time: float
    p99_time: float
    throughput: float  # 请求/秒
    success_rate: float
    errors: int

    def to_dict(self) -> Dict[str, Any]:
        return {
            "name": self.name,
            "total_time": round(self.total_time, 4),
            "avg_time": round(self.avg_time, 4),
            "min_time": round(self.min_time, 4),
            "max_time": round(self.max_time, 4),
            "p50_time": round(self.p50_time, 4),
            "p95_time": round(self.p95_time, 4),
            "p99_time": round(self.p99_time, 4),
            "throughput": round(self.throughput, 2),
            "success_rate": round(self.success_rate * 100, 2),
            "errors": self.errors
        }


class PerformanceBenchmark:
    """性能基准测试框架"""

    def __init__(self):
        self.llm_client = None
        self.results: List[BenchmarkResult] = []

    async def setup(self):
        """初始化测试环境"""
        logger.info("Setting up benchmark environment...")
        try:
            self.llm_client = AsyncLLMClient()
            logger.info("✅ LLM Client initialized")
        except Exception as e:
            logger.warning(f"⚠️  LLM Client not available: {e}")

    async def teardown(self):
        """清理测试环境"""
        if self.llm_client:
            await self.llm_client.close()

    async def run_benchmark(
        self,
        name: str,
        func: Callable,
        iterations: int = 100,
        concurrency: int = 1
    ) -> BenchmarkResult:
        """
        运行基准测试

        Args:
            name: 测试名称
            func: 异步测试函数
            iterations: 迭代次数
            concurrency: 并发数

        Returns:
            BenchmarkResult
        """
        logger.info(f"Running benchmark: {name} (iterations={iterations}, concurrency={concurrency})")

        times = []
        errors = 0
        start_time = time.time()

        if concurrency == 1:
            # 串行执行
            for i in range(iterations):
                try:
                    req_start = time.time()
                    await func()
                    req_time = time.time() - req_start
                    times.append(req_time)

                    if (i + 1) % 20 == 0:
                        logger.debug(f"  Progress: {i + 1}/{iterations}")
                except Exception as e:
                    errors += 1
                    logger.debug(f"  Error in iteration {i}: {e}")
        else:
            # 并发执行
            semaphore = asyncio.Semaphore(concurrency)

            async def run_one():
                async with semaphore:
                    try:
                        req_start = time.time()
                        await func()
                        return time.time() - req_start, None
                    except Exception as e:
                        return None, e

            tasks = [run_one() for _ in range(iterations)]
            results = await asyncio.gather(*tasks)

            for req_time, error in results:
                if error:
                    errors += 1
                else:
                    times.append(req_time)

        total_time = time.time() - start_time

        if not times:
            logger.error(f"  ❌ All requests failed for {name}")
            return BenchmarkResult(
                name=name,
                total_time=total_time,
                avg_time=0,
                min_time=0,
                max_time=0,
                p50_time=0,
                p95_time=0,
                p99_time=0,
                throughput=0,
                success_rate=0,
                errors=errors
            )

        # 计算统计数据
        sorted_times = sorted(times)
        result = BenchmarkResult(
            name=name,
            total_time=total_time,
            avg_time=statistics.mean(times),
            min_time=min(times),
            max_time=max(times),
            p50_time=sorted_times[len(sorted_times) // 2],
            p95_time=sorted_times[int(len(sorted_times) * 0.95)] if len(sorted_times) > 20 else max(times),
            p99_time=sorted_times[int(len(sorted_times) * 0.99)] if len(sorted_times) > 100 else max(times),
            throughput=iterations / total_time,
            success_rate=(iterations - errors) / iterations,
            errors=errors
        )

        self.results.append(result)
        logger.info(f"  ✅ {name}: avg={result.avg_time:.4f}s, throughput={result.throughput:.2f} req/s")

        return result

    # ========================================================================
    # 具体测试用例
    # ========================================================================

    async def test_threshold_detector_no_llm(self, iterations: int = 1000):
        """测试: 阈值检测器（不使用 LLM）"""
        detector = ThresholdDetector()

        async def detect():
            return detector.detect({
                "value": 25.5,
                "min": 0,
                "max": 20
            })

        return await self.run_benchmark(
            "ThresholdDetector (no LLM)",
            detect,
            iterations=iterations,
            concurrency=1
        )

    async def test_statistical_detector_no_llm(self, iterations: int = 1000):
        """测试: 统计检测器（不使用 LLM）"""
        detector = StatisticalDetector()

        async def detect():
            return detector.detect({
                "value": 25.5,
                "history": [10, 12, 11, 13, 10, 12, 11, 14]
            })

        return await self.run_benchmark(
            "StatisticalDetector (no LLM)",
            detect,
            iterations=iterations,
            concurrency=1
        )

    async def test_fall_detection_no_llm(self, iterations: int = 1000):
        """测试: 跌倒检测（不使用 LLM）"""
        detector = SafetyGuardian(llm_client=None)

        async def detect():
            return detector._threshold_detection({
                "acc_x": 5,
                "acc_y": 5,
                "acc_z": 25
            })

        return await self.run_benchmark(
            "FallDetection (no LLM)",
            detect,
            iterations=iterations,
            concurrency=1
        )

    async def test_fall_detection_with_llm(self, iterations: int = 10):
        """测试: 跌倒检测（使用 LLM）- 由于 LLM 调用较慢，减少迭代次数"""
        if not self.llm_client:
            logger.warning("⚠️  Skipping LLM test (client not available)")
            return None

        detector = SafetyGuardian(llm_client=self.llm_client)

        async def detect():
            return await detector.detect_fall({
                "acc_x": 0,
                "acc_y": 0,
                "acc_z": 25,
                "heart_rate": 110,
                "location": "Bathroom",
                "timestamp": "02:15:00",
                "age": 72
            }, use_llm=True)

        return await self.run_benchmark(
            "FallDetection (with LLM)",
            detect,
            iterations=iterations,
            concurrency=1
        )

    async def test_risk_assessment_no_llm(self, iterations: int = 1000):
        """测试: 风险评估（不使用 LLM）"""
        oracle = HealthOracle(llm_client=None)

        async def assess():
            # 不使用 LLM，直接计算
            hr_risk = oracle._calculate_heart_rate_risk(110)
            move_risk = oracle._calculate_movement_risk(2.0)
            sleep_risk = oracle._calculate_sleep_risk(5)
            age_bonus = oracle._calculate_age_factor(75)
            return hr_risk + move_risk + sleep_risk + age_bonus

        return await self.run_benchmark(
            "RiskAssessment (no LLM)",
            assess,
            iterations=iterations,
            concurrency=1
        )

    async def test_concurrent_fall_detection(self, concurrency: int = 10, iterations: int = 100):
        """测试: 并发跌倒检测（不使用 LLM）"""
        detector = SafetyGuardian(llm_client=None)

        async def detect():
            return detector._threshold_detection({
                "acc_x": 5,
                "acc_y": 5,
                "acc_z": 25
            })

        return await self.run_benchmark(
            f"Concurrent FallDetection (concurrency={concurrency})",
            detect,
            iterations=iterations,
            concurrency=concurrency
        )

    async def test_cache_effectiveness(self, iterations: int = 50):
        """测试: LLM 缓存效果"""
        if not self.llm_client:
            logger.warning("⚠️  Skipping cache test (client not available)")
            return None

        # 第一次运行（缓存未命中）
        result1 = await self.run_benchmark(
            "LLM Call (cache miss)",
            lambda: self.llm_client.chat_completion(
                "Test prompt",
                "Test content that should be cached"
            ),
            iterations=10,
            concurrency=1
        )

        # 第二次运行（缓存命中）
        result2 = await self.run_benchmark(
            "LLM Call (cache hit)",
            lambda: self.llm_client.chat_completion(
                "Test prompt",
                "Test content that should be cached"
            ),
            iterations=iterations,
            concurrency=1
        )

        return {
            "cache_miss": result1.to_dict() if result1 else None,
            "cache_hit": result2.to_dict() if result2 else None,
            "speedup": result1.avg_time / result2.avg_time if result1 and result2 else None
        }

    # ========================================================================
    # 运行所有测试
    # ========================================================================

    async def run_all_benchmarks(self):
        """运行所有基准测试"""
        print("\n" + "=" * 80)
        print("🚀 Starting Algorithm Performance Benchmark")
        print("=" * 80 + "\n")

        await self.setup()

        try:
            # 1. 纯算法性能（无 LLM）
            print("\n--- 1. Pure Algorithm Performance (No LLM) ---\n")
            await self.test_threshold_detector_no_llm(iterations=1000)
            await self.test_statistical_detector_no_llm(iterations=1000)
            await self.test_fall_detection_no_llm(iterations=1000)
            await self.test_risk_assessment_no_llm(iterations=1000)

            # 2. LLM 增强性能
            if self.llm_client:
                print("\n--- 2. LLM-Enhanced Performance ---\n")
                await self.test_fall_detection_with_llm(iterations=10)

                # 3. 缓存效果
                print("\n--- 3. Cache Effectiveness ---\n")
                cache_result = await self.test_cache_effectiveness(iterations=50)
                if cache_result and cache_result.get("speedup"):
                    print(f"  📊 Cache speedup: {cache_result['speedup']:.2f}x")

            # 4. 并发性能
            print("\n--- 4. Concurrent Performance ---\n")
            await self.test_concurrent_fall_detection(concurrency=1, iterations=100)
            await self.test_concurrent_fall_detection(concurrency=10, iterations=100)
            await self.test_concurrent_fall_detection(concurrency=50, iterations=100)

            # 5. 生成报告
            self.generate_report()

        finally:
            await self.teardown()

    def generate_report(self):
        """生成性能报告"""
        print("\n" + "=" * 80)
        print("📊 Performance Benchmark Report")
        print("=" * 80 + "\n")

        for result in self.results:
            print(f"### {result.name}")
            print(f"  Average:  {result.avg_time*1000:.2f} ms")
            print(f"  P50:      {result.p50_time*1000:.2f} ms")
            print(f"  P95:      {result.p95_time*1000:.2f} ms")
            print(f"  P99:      {result.p99_time*1000:.2f} ms")
            print(f"  Throughput: {result.throughput:.2f} req/s")
            print(f"  Success:   {result.success_rate*100:.1f}%")
            print()

        # 保存 JSON 报告
        report_path = "logs/performance_report.json"
        import os
        os.makedirs("logs", exist_ok=True)

        report_data = {
            "timestamp": datetime.now().isoformat(),
            "results": [r.to_dict() for r in self.results]
        }

        with open(report_path, "w", encoding="utf-8") as f:
            json.dump(report_data, f, indent=2, ensure_ascii=False)

        print(f"✅ Report saved to: {report_path}\n")


# ============================================================================
# 主函数
# ============================================================================

async def main():
    """主函数"""
    benchmark = PerformanceBenchmark()
    await benchmark.run_all_benchmarks()


if __name__ == "__main__":
    asyncio.run(main())
