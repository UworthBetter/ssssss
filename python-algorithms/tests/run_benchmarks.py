"""
性能测试运行器

提供统一的入口来运行所有性能测试
"""
import sys
import os
import asyncio
import argparse
from pathlib import Path

# 添加项目根目录到路径
project_root = Path(__file__).parent.parent
sys.path.insert(0, str(project_root))


def print_banner():
    """打印横幅"""
    print("\n" + "=" * 70)
    print(" " * 15 + "🚀 QKYD AI Algorithms - Performance Tests")
    print("=" * 70 + "\n")


def print_menu():
    """打印菜单"""
    print("请选择要运行的测试:")
    print("  1. 基准性能测试 (Benchmark)")
    print("     - 单次请求延迟")
    print("     - 并发处理能力")
    print("     - LLM 缓存效果")
    print()
    print("  2. 负载测试 (Load Test)")
    print("     - 持续负载稳定性")
    print("     - 峰值并发处理")
    print("     - 资源使用监控")
    print()
    print("  3. 同步 vs 异步对比 (Comparison)")
    print("     - 性能加速比")
    print("     - 吞吐量对比")
    print()
    print("  4. 运行所有测试")
    print()
    print("  0. 退出")
    print()


async def run_benchmark():
    """运行基准性能测试"""
    print("\n🔹 运行基准性能测试...\n")
    from tests.test_performance import main as perf_main
    await perf_main()


async def run_load_test():
    """运行负载测试"""
    print("\n🔹 运行负载测试...\n")
    from tests.test_load import main as load_main
    await load_main()


def run_comparison():
    """运行同步异步对比测试"""
    print("\n🔹 运行同步 vs 异步对比测试...\n")
    from tests.test_comparison import main as comp_main
    comp_main()


async def run_all_tests():
    """运行所有测试"""
    print("\n🔹 运行所有性能测试...\n")

    print(">>> 第 1/3: 基准性能测试")
    await run_benchmark()

    print("\n>>> 第 2/3: 负载测试")
    await run_load_test()

    print("\n>>> 第 3/3: 同步 vs 异步对比")
    run_comparison()

    print("\n" + "=" * 70)
    print("✅ 所有测试完成!")
    print("=" * 70)
    print("\n📁 测试报告保存在 logs/ 目录:")
    print("   • performance_report.json  - 基准性能报告")
    print("   • load_test_report.json    - 负载测试报告")
    print("   • sync_async_comparison.json - 同步异步对比报告")
    print("   • load_test_charts.png     - 负载测试图表")
    print()


def main():
    """主函数"""
    print_banner()

    # 命令行参数
    parser = argparse.ArgumentParser(description="QKYD AI Algorithms 性能测试")
    parser.add_argument(
        "--all",
        action="store_true",
        help="运行所有测试"
    )
    parser.add_argument(
        "--benchmark",
        action="store_true",
        help="运行基准性能测试"
    )
    parser.add_argument(
        "--load",
        action="store_true",
        help="运行负载测试"
    )
    parser.add_argument(
        "--compare",
        action="store_true",
        help="运行同步异步对比测试"
    )

    args = parser.parse_args()

    # 如果有命令行参数，直接运行对应测试
    if args.all:
        asyncio.run(run_all_tests())
        return
    elif args.benchmark:
        asyncio.run(run_benchmark())
        return
    elif args.load:
        asyncio.run(run_load_test())
        return
    elif args.compare:
        run_comparison()
        return

    # 交互式菜单
    while True:
        print_menu()
        choice = input("请输入选项 (0-4): ").strip()

        if choice == "0":
            print("\n👋 再见!\n")
            break
        elif choice == "1":
            asyncio.run(run_benchmark())
        elif choice == "2":
            asyncio.run(run_load_test())
        elif choice == "3":
            run_comparison()
        elif choice == "4":
            asyncio.run(run_all_tests())
        else:
            print("\n❌ 无效选项，请重新选择\n")

        input("\n按 Enter 继续...")
        print("\n" * 2)


if __name__ == "__main__":
    main()
