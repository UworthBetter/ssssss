package com.qkyd.framework.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * 国际化消息源配置
 * 
 * @author qkyd
 */
@Configuration
public class MessageSourceConfig {

    /**
     * 配置 MessageSource Bean 用于加载 i18n 国际化资源文件
     * 
     * @return MessageSource 实例
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        // 设置基础名称，对应 resources/i18n/messages.properties
        messageSource.setBasename("i18n/messages");
        // 设置默认编码
        messageSource.setDefaultEncoding("UTF-8");
        // 如果找不到消息代码的键值，使用消息代码本身作为返回值
        messageSource.setUseCodeAsDefaultMessage(true);
        // 缓存时间（秒），-1表示永久缓存
        messageSource.setCacheSeconds(3600);
        return messageSource;
    }
}
