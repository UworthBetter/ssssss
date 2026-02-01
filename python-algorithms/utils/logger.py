"""
结构化日志配置模块
提供统一的日志接口，支持文件输出和级别控制
"""
import logging
import sys
from pathlib import Path
from typing import Optional
from logging.handlers import RotatingFileHandler, TimedRotatingFileHandler
import os


# 日志级别映射
LOG_LEVELS = {
    "DEBUG": logging.DEBUG,
    "INFO": logging.INFO,
    "WARNING": logging.WARNING,
    "ERROR": logging.ERROR,
    "CRITICAL": logging.CRITICAL
}


class ColoredFormatter(logging.Formatter):
    """
    彩色日志格式化器（控制台输出）
    """

    # ANSI 颜色代码
    COLORS = {
        'DEBUG': '\033[36m',      # 青色
        'INFO': '\033[32m',       # 绿色
        'WARNING': '\033[33m',    # 黄色
        'ERROR': '\033[31m',      # 红色
        'CRITICAL': '\033[35m',   # 紫色
    }
    RESET = '\033[0m'

    def format(self, record):
        # 添加颜色
        levelname = record.levelname
        if levelname in self.COLORS:
            record.levelname = f"{self.COLORS[levelname]}{levelname}{self.RESET}"
        return super().format(record)


def setup_logger(
    name: str = "qkyd_ai",
    level: str = "INFO",
    log_dir: Optional[str | Path] = None,
    log_file: Optional[str] = "qkyd_ai.log",
    console_output: bool = True,
    file_output: bool = True,
    max_bytes: int = 10 * 1024 * 1024,  # 10MB
    backup_count: int = 5,
    use_colors: bool = True
) -> logging.Logger:
    """
    设置并返回配置好的日志记录器

    Args:
        name: 日志记录器名称
        level: 日志级别 (DEBUG, INFO, WARNING, ERROR, CRITICAL)
        log_dir: 日志文件目录（默认为项目根目录下的 logs 文件夹）
        log_file: 日志文件名
        console_output: 是否输出到控制台
        file_output: 是否输出到文件
        max_bytes: 单个日志文件最大字节数
        backup_count: 保留的备份文件数量
        use_colors: 是否使用彩色输出（仅控制台）

    Returns:
        配置好的 Logger 实例
    """
    # 获取或创建日志记录器
    logger = logging.getLogger(name)

    # 避免重复添加处理器
    if logger.handlers:
        return logger

    # 设置日志级别
    log_level = LOG_LEVELS.get(level.upper(), logging.INFO)
    logger.setLevel(log_level)

    # 日志格式
    detailed_format = (
        "[%(asctime)s] [%(name)s] [%(levelname)s] "
        "[%(filename)s:%(lineno)d] - %(message)s"
    )
    simple_format = "[%(asctime)s] [%(levelname)s] - %(message)s"
    date_format = "%Y-%m-%d %H:%M:%S"

    # 控制台处理器
    if console_output:
        console_handler = logging.StreamHandler(sys.stdout)
        console_handler.setLevel(log_level)

        if use_colors:
            console_formatter = ColoredFormatter(
                detailed_format,
                datefmt=date_format
            )
        else:
            console_formatter = logging.Formatter(
                detailed_format,
                datefmt=date_format
            )

        console_handler.setFormatter(console_formatter)
        logger.addHandler(console_handler)

    # 文件处理器
    if file_output:
        # 确定日志目录
        if log_dir is None:
            # 默认为项目根目录下的 logs 文件夹
            project_root = Path(__file__).parent.parent.parent
            log_dir = project_root / "logs"

        log_dir = Path(log_dir)
        log_dir.mkdir(parents=True, exist_ok=True)

        log_path = log_dir / log_file

        # 使用滚动文件处理器
        file_handler = RotatingFileHandler(
            log_path,
            maxBytes=max_bytes,
            backupCount=backup_count,
            encoding='utf-8'
        )
        file_handler.setLevel(log_level)

        # 文件使用简单格式（无颜色）
        file_formatter = logging.Formatter(
            detailed_format,
            datefmt=date_format
        )
        file_handler.setFormatter(file_formatter)
        logger.addHandler(file_handler)

    return logger


def get_logger(name: str = "qkyd_ai") -> logging.Logger:
    """
    获取已配置的日志记录器

    Args:
        name: 日志记录器名称

    Returns:
        Logger 实例
    """
    return logging.getLogger(name)


# ============================================================================
# 为不同模块创建专用日志记录器
# ============================================================================

# 主服务日志
service_logger = setup_logger("qkyd_ai.service", level="INFO")

# LLM 客户端日志
llm_logger = setup_logger("qkyd_ai.llm", level="INFO")

# 算法日志
algorithm_logger = setup_logger("qkyd_ai.algorithm", level="INFO")

# API 日志
api_logger = setup_logger("qkyd_ai.api", level="INFO")

# 错误日志（仅记录错误及以上）
error_logger = setup_logger("qkyd_ai.error", level="ERROR", log_file="errors.log")


# ============================================================================
# 便捷函数
# ============================================================================

def log_function_call(logger: logging.Logger):
    """
    装饰器：记录函数调用

    Usage:
        @log_function_call(get_logger(__name__))
        def my_function(arg1, arg2):
            ...
    """
    def decorator(func):
        def wrapper(*args, **kwargs):
            logger.debug(f"Calling {func.__name__} with args={args}, kwargs={kwargs}")
            try:
                result = func(*args, **kwargs)
                logger.debug(f"{func.__name__} completed successfully")
                return result
            except Exception as e:
                logger.error(f"{func.__name__} failed with error: {e}", exc_info=True)
                raise
        return wrapper
    return decorator


def log_async_function_call(logger: logging.Logger):
    """
    装饰器：记录异步函数调用

    Usage:
        @log_async_function_call(get_logger(__name__))
        async def my_async_function(arg1):
            ...
    """
    def decorator(func):
        async def wrapper(*args, **kwargs):
            logger.debug(f"Calling async {func.__name__} with args={args}, kwargs={kwargs}")
            try:
                result = await func(*args, **kwargs)
                logger.debug(f"{func.__name__} completed successfully")
                return result
            except Exception as e:
                logger.error(f"{func.__name__} failed with error: {e}", exc_info=True)
                raise
        return wrapper
    return decorator


# ============================================================================
# 初始化默认日志记录器
# ============================================================================

def init_logging(
    level: str = None,
    log_dir: str = None,
    console: bool = True,
    file: bool = True
):
    """
    初始化全局日志配置

    Args:
        level: 日志级别（从环境变量 LOG_LEVEL 读取）
        log_dir: 日志目录
        console: 是否输出到控制台
        file: 是否输出到文件
    """
    level = level or os.getenv("LOG_LEVEL", "INFO")

    # 更新所有现有日志记录器
    loggers = [
        ("qkyd_ai.service", service_logger),
        ("qkyd_ai.llm", llm_logger),
        ("qkyd_ai.algorithm", algorithm_logger),
        ("qkyd_ai.api", api_logger),
        ("qkyd_ai.error", error_logger),
    ]

    for name, logger in loggers:
        # 清除现有处理器
        logger.handlers.clear()

        # 重新配置
        setup_logger(
            name=name,
            level=level,
            log_dir=log_dir,
            console_output=console,
            file_output=file
        )


# 启动时自动初始化
init_logging()
