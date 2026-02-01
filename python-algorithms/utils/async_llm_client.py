"""
异步 LLM 客户端
使用 AsyncOpenAI 实现非阻塞的 LLM 调用，提升并发性能
"""
import os
import json
import asyncio
from typing import Optional, Dict, Any, List, Tuple
from functools import lru_cache
import hashlib
from dotenv import load_dotenv
from openai import AsyncOpenAI
from tenacity import retry, stop_after_attempt, wait_exponential, retry_if_exception_type
from utils.logger import llm_logger

load_dotenv(override=True)


class LLMError(Exception):
    """LLM 调用异常基类"""
    pass


class LLMConfigError(LLMError):
    """配置错误"""
    pass


class LLMParseError(LLMError):
    """解析错误"""
    pass


class AsyncLLMClient:
    """
    异步 LLM 客户端

    特性:
    - 使用 AsyncOpenAI 实现非阻塞调用
    - 内置重试机制（指数退避）
    - 响应缓存（减少重复调用）
    - 结构化输出支持
    - 连接池管理
    """

    def __init__(
        self,
        api_key: Optional[str] = None,
        base_url: Optional[str] = None,
        model: Optional[str] = None,
        timeout: float = 30.0,
        max_retries: int = 3
    ):
        """
        初始化异步 LLM 客户端

        Args:
            api_key: API 密钥（默认从环境变量读取）
            base_url: API 基础 URL（默认从环境变量读取）
            model: 模型名称（默认从环境变量读取）
            timeout: 请求超时时间（秒）
            max_retries: 最大重试次数
        """
        self.api_key = api_key or os.getenv("LLM_API_KEY")
        self.base_url = base_url or os.getenv("LLM_BASE_URL", "https://api.openai.com/v1")
        self.model = model or os.getenv("LLM_MODEL", "gpt-3.5-turbo")
        self.timeout = timeout
        self.max_retries = max_retries

        # 验证配置
        if not self.api_key:
            llm_logger.error("LLM_API_KEY is not set in environment variables")
            raise LLMConfigError("LLM_API_KEY is not set in environment variables")

        # 初始化异步客户端（内置连接池）
        self.client = AsyncOpenAI(
            api_key=self.api_key,
            base_url=self.base_url,
            timeout=timeout,
            max_retries=max_retries
        )

        # 缓存字典（key: hash of input, value: response）
        self._cache: Dict[str, Any] = {}
        self._cache_lock = asyncio.Lock()

    def _generate_cache_key(self, system_prompt: str, user_content: str) -> str:
        """生成缓存键"""
        content = f"{system_prompt}:{user_content}:{self.model}"
        return hashlib.sha256(content.encode()).hexdigest()

    async def chat_completion(
        self,
        system_prompt: str,
        user_content: str,
        temperature: float = 0.7,
        use_cache: bool = True,
        response_format: Optional[Dict[str, str]] = None
    ) -> str:
        """
        发送聊天完成请求（异步）

        Args:
            system_prompt: 系统提示词
            user_content: 用户内容
            temperature: 温度参数（0-2）
            use_cache: 是否使用缓存
            response_format: 响应格式（如 {"type": "json_object"}）

        Returns:
            模型响应文本

        Raises:
            LLMError: 调用失败时抛出
        """
        # 检查缓存
        if use_cache:
            cache_key = self._generate_cache_key(system_prompt, user_content)
            async with self._cache_lock:
                if cache_key in self._cache:
                    return self._cache[cache_key]

        try:
            response = await self.client.chat.completions.create(
                model=self.model,
                messages=[
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": user_content}
                ],
                temperature=temperature,
                response_format=response_format
            )

            result = response.choices[0].message.content

            # 缓存结果
            if use_cache:
                async with self._cache_lock:
                    self._cache[cache_key] = result

            return result

        except Exception as e:
            raise LLMError(f"LLM API Error: {str(e)}") from e

    async def analyze_json(
        self,
        system_prompt: str,
        data: Dict[str, Any],
        use_cache: bool = True,
        ensure_json: bool = True
    ) -> Dict[str, Any]:
        """
        JSON 分析辅助方法

        Args:
            system_prompt: 系统提示词
            data: 要分析的数据（会被转为 JSON 字符串）
            use_cache: 是否使用缓存
            ensure_json: 是否强制使用 JSON 模式

        Returns:
            解析后的 JSON 字典

        Raises:
            LLMParseError: JSON 解析失败时抛出
        """
        user_content = json.dumps(data, ensure_ascii=False, indent=2)

        # 使用结构化输出（如果支持）
        response_format = {"type": "json_object"} if ensure_json else None

        response = await self.chat_completion(
            system_prompt=system_prompt,
            user_content=user_content,
            use_cache=use_cache,
            response_format=response_format
        )

        return self._parse_json_response(response)

    def _parse_json_response(self, response: str) -> Dict[str, Any]:
        """
        解析 JSON 响应（处理 Markdown 代码块包裹）

        Args:
            response: 原始响应文本

        Returns:
            解析后的字典

        Raises:
            LLMParseError: 解析失败时抛出
        """
        try:
            clean = response.strip()

            # 移除 Markdown 代码块标记
            if "```json" in clean:
                clean = clean.split("```json")[1].split("```")[0].strip()
            elif "```" in clean:
                clean = clean.split("```")[1].split("```")[0].strip()

            return json.loads(clean)

        except json.JSONDecodeError as e:
            raise LLMParseError(f"Failed to parse JSON response: {e}") from e

    async def batch_analyze(
        self,
        tasks: List[Tuple[str, Dict[str, Any]]],
        system_prompt: str,
        concurrency: int = 5
    ) -> List[Dict[str, Any]]:
        """
        批量分析（并发控制）

        Args:
            tasks: 任务列表 [(system_prompt, data), ...]
            concurrency: 最大并发数
            system_prompt: 共享的系统提示词

        Returns:
            结果列表
        """
        semaphore = asyncio.Semaphore(concurrency)

        async def process_one(data: Dict[str, Any]) -> Dict[str, Any]:
            async with semaphore:
                return await self.analyze_json(system_prompt, data)

        return await asyncio.gather(*[process_one(data) for _, data in tasks])

    def clear_cache(self):
        """清空缓存"""
        self._cache.clear()

    async def close(self):
        """关闭客户端连接"""
        await self.client.close()

    async def __aenter__(self):
        """异步上下文管理器入口"""
        return self

    async def __aexit__(self, exc_type, exc_val, exc_tb):
        """异步上下文管理器退出"""
        await self.close()


# 单例模式
_global_client: Optional[AsyncLLMClient] = None


@lru_cache(maxsize=1)
def get_llm_client() -> AsyncLLMClient:
    """获取全局 LLM 客户端单例"""
    global _global_client
    if _global_client is None:
        _global_client = AsyncLLMClient()
    return _global_client
