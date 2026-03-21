package com.qkyd.framework.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * 鍥介檯鍖栨秷鎭簮閰嶇疆
 * 
 * @author qkyd
 */
@Configuration
public class MessageSourceConfig {

    /**
     * 閰嶇疆 MessageSource Bean 鐢ㄤ簬鍔犺浇 i18n 鍥介檯鍖栬祫婧愭枃浠?
     * 
     * @return MessageSource 瀹炰緥
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        // 璁剧疆鍩虹鍚嶇О锛屽搴?resources/i18n/messages.properties
        messageSource.setBasename("i18n/messages");
        // 璁剧疆榛樿缂栫爜
        messageSource.setDefaultEncoding("UTF-8");
        // 濡傛灉鎵句笉鍒版秷鎭唬鐮佺殑閿€硷紝浣跨敤娑堟伅浠ｇ爜鏈韩浣滀负杩斿洖鍊?
        messageSource.setUseCodeAsDefaultMessage(true);
        // 缂撳瓨鏃堕棿锛堢锛夛紝-1琛ㄧず姘镐箙缂撳瓨
        messageSource.setCacheSeconds(3600);
        return messageSource;
    }
}
