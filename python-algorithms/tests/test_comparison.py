"""
同步 vs 异步性能对比测试

展示异步架构相比同步架构的性能优势
"""
import asyncio
import time
import statistics
from typing import List, Dict, Any, Callable
from dataclasses import dataclass
import json
from datetime import datetime

# 模拟同步版本
class SyncSafetyGuardian:
    """同步版本的跌倒检测器"""

    def _threshold_detection(self, data: dict) -> dict:
        """模拟同步检测（带网络延迟）"""
        time.sleep(0.001)  # 模拟 1ms 处理延迟
        acc_x = data.get('acc_x', 0)
        acc_y = data.get('acc_y', 0)
        acc_z = data.get('acc_z', 0)
        magnitude = (acc_x**2 + acc_y**2 + acc_z**2) ** 0.5
        return {
            "is_fall": magnitude > 20.0,
            "magnitude": magnitude
        }

    def detect_batch_sync(self, requests: List[dict]) -> List[dict]:
        """同步批量处理"""
        results = []
        for req in requests:
            results.append(self._threshold_detection(req))
        return results


# 异步版本
class AsyncSafetyGuardian:
    """异步版本的跌倒检测器"""

    async def _threshold_detection(self, data: dict) -> dict:
        """模拟异步检测（带网络延迟）"""
        await asyncio.sleep(0.001)  # 模拟 1ms 处理延迟
        acc_x = data.get('acc_x', 0)
        acc_y = data.get('acc_y', 0)
        acc_z = data.get('acc_z', 0)
        magnitude = (acc_x**2 + acc_y**2 + acc_z**2) ** 0.5
        return {
            "is_fall": magnitude > 20.0,
            "magnitude": magnitude
        }

    async def detect_batch_async(self, requests: List[dict]) -> List[dict]:
        """异步批量处理（并发）"""
        tasks = [self._threshold_detection(req) for req in requests]
        return await asyncio.gather(*tasks)


@dataclass
class ComparisonResult:
    """对比测试结果"""
    test_name: str
    sync_time: float
    async_time: float
    speedup: float
    throughput_improvement: float
    request_count: int


class PerformanceComparison:
    """性能对比测试"""

    def __init__(self):
        self.results: List[ComparisonResult] = []

        # 准备测试数据
        self.test_data = [
            {"acc_x": 5, "acc_y": 5, "acc_z": 25},
            {"acc_x": 10, "acc_y": 10, "acc_z": 5},
            {"acc_x": 0, "acc_y": 0, "acc_z": 30},
            {"acc_x": 15, "acc_y": 5, "acc_z": 10},
            {"acc_x": 3, "acc_y": 3, "acc_z": 22},
        ] * 20  # 100 个请求

    def run_single_comparison(
        self,
        test_name: str,
        sync_func: Callable,
        async_func: Callable,
        iterations: int = 10
    ) -> ComparisonResult:
        """运行单次对比测试"""

        print(f"\n📊 Testing: {test_name}")

        # 测试同步版本
        sync_times = []
        for _ in range(iterations):
            start = time.time()
            sync_func()
            sync_times.append(time.time() - start)

        sync_avg = statistics.mean(sync_times)

        # 测试异步版本
        async def test_async():
            async_times = []
            for _ in range(iterations):
                start = time.time()
                await async_func()
                async_times.append(time.time() - start)
            return statistics.mean(async_times)

        async_avg = asyncio.run(test_async())

        speedup = sync_avg / async_avg if async_avg > 0 else 0

        result = ComparisonResult(
            test_name=test_name,
            sync_time=sync_avg,
            async_time=async_avg,
            speedup=speedup,
            throughput_improvement=speedup,
            request_count=len(self.test_data)
        )

        self.results.append(result)

        print(f"  Sync:  {sync_avg:.4f}s")
        print(f"  Async: {async_avg:.4f}s")
        print(f"  ✨ Speedup: {speedup:.2f}x")

        return result

    def run_all_comparisons(self):
        """运行所有对比测试"""
        print("\n" + "=" * 80)
        print("⚡ Sync vs Async Performance Comparison")
        print("=" * 80)

        sync_detector = SyncSafetyGuardian()
        async_detector = AsyncSafetyGuardian()

        # 测试 1: 小批量（10 个请求）
        small_batch = self.test_data[:10]

        self.run_single_comparison(
            "Small Batch (10 requests)",
            lambda: sync_detector.detect_batch_sync(small_batch),
            lambda: async_detector.detect_batch_async(small_batch)
        )

        # 测试 2: 中批量（50 个请求）
        medium_batch = self.test_data[:50]

        self.run_single_comparison(
            "Medium Batch (50 requests)",
            lambda: sync_detector.detect_batch_sync(medium_batch),
            lambda: async_detector.detect_batch_async(medium_batch)
        )

        # 测试 3: 大批量（100 个请求）
        large_batch = self.test_data[:100]

        self.run_single_comparison(
            "Large Batch (100 requests)",
            lambda: sync_detector.detect_batch_sync(large_batch),
            lambda: async_detector.detect_batch_async(large_batch)
        )

        # 测试 4: 并发场景模拟
        print("\n📊 Testing: Concurrent Simulation (100 requests)")

        # 同步：串行处理
        sync_start = time.time()
        for _ in range(100):
            sync_detector._threshold_detection(self.test_data[0])
        sync_time = time.time() - sync_start

        # 异步：并发处理
        async def concurrent_test():
            start = time.time()
            tasks = [async_detector._threshold_detection(self.test_data[0]) for _ in range(100)]
            await asyncio.gather(*tasks)
            return time.time() - start

        async_time = asyncio.run(concurrent_test())

        speedup = sync_time / async_time if async_time > 0 else 0

        result = ComparisonResult(
            test_name="Concurrent (100 requests)",
            sync_time=sync_time,
            async_time=async_time,
            speedup=speedup,
            throughput_improvement=speedup,
            request_count=100
        )

        self.results.append(result)

        print(f"  Sync:  {sync_time:.4f}s")
        print(f"  Async: {async_time:.4f}s")
        print(f"  ✨ Speedup: {speedup:.2f}x")

        # 生成报告
        self.generate_report()

    def generate_report(self):
        """生成对比报告"""
        print("\n" + "=" * 80)
        print("📊 Performance Comparison Report")
        print("=" * 80 + "\n")

        print(f"{'Test Case':<30} {'Sync':<12} {'Async':<12} {'Speedup':<10}")
        print("-" * 80)

        for result in self.results:
            print(
                f"{result.test_name:<30} "
                f"{result.sync_time:<12.4f} "
                f"{result.async_time:<12.4f} "
                f"{result.speedup:<10.2f}x"
            )

        # 计算平均加速比
        avg_speedup = statistics.mean([r.speedup for r in self.results])
        print("-" * 80)
        print(f"{'Average Speedup':<30} {'':<12} {'':<12} {avg_speedup:<10.2f}x\n")

        # 结论
        print("🎯 Conclusions:")
        print(f"  • 异步架构平均提速 {avg_speedup:.2f}x")
        print(f"  • 批量越大，异步优势越明显")
        print(f"  • 高并发场景下异步架构性能显著优于同步")

        # 保存 JSON 报告
        report_path = "logs/sync_async_comparison.json"
        import os
        os.makedirs("logs", exist_ok=True)

        report_data = {
            "timestamp": datetime.now().isoformat(),
            "summary": {
                "average_speedup": round(avg_speedup, 2),
                "test_count": len(self.results)
            },
            "results": [
                {
                    "test_name": r.test_name,
                    "sync_time": round(r.sync_time, 4),
                    "async_time": round(r.async_time, 4),
                    "speedup": round(r.speedup, 2),
                    "request_count": r.request_count
                }
                for r in self.results
            ]
        }

        with open(report_path, "w", encoding="utf-8") as f:
            json.dump(report_data, f, indent=2, ensure_ascii=False)

        print(f"\n✅ Report saved to: {report_path}\n")


# ============================================================================
# 主函数
# ============================================================================

def main():
    """主函数"""
    comparison = PerformanceComparison()
    comparison.run_all_comparisons()


if __name__ == "__main__":
    main()
