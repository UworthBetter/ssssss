package com.qkyd.framework.config;

import java.util.TimeZone;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 绋嬪簭娉ㄨВ閰嶇疆
 *
 * @author qkyd
 */
@Configuration
// 琛ㄧず閫氳繃aop妗嗘灦鏆撮湶璇ヤ唬鐞嗗璞?AopContext鑳藉璁块棶
@EnableAspectJAutoProxy(exposeProxy = true)
// 鎸囧畾瑕佹壂鎻忕殑Mapper绫荤殑鍖呯殑璺緞
@MapperScan("com.qkyd.**.mapper")
public class ApplicationConfig
{
    /**
     * 鏃跺尯閰嶇疆
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonObjectMapperCustomization()
    {
        return jacksonObjectMapperBuilder -> jacksonObjectMapperBuilder.timeZone(TimeZone.getDefault());
    }
}


