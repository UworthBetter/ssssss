package com.qkyd.health.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 异步任务配置类
 *
 * @author ueit
 * @date 2026-01-29
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    // Spring 默认的异步配置已足够使用
    // 如需自定义线程池，可在此配置 TaskExecutor
}
