package com.qkyd.web.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.qkyd.common.config.QkydConfig;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * SpringDoc OpenAPI閰嶇疆
 *
 * @author qkyd
 */
@Configuration
public class SwaggerConfig
{
    /** 绯荤粺鍩虹閰嶇疆 */
    @Autowired
    private QkydConfig qkydConfig;

    /** 鏄惁寮€鍚痵wagger */
    @Value("${swagger.enabled}")
    private boolean enabled;

    /** 璁剧疆璇锋眰鐨勭粺涓€鍓嶇紑 */
    @Value("${swagger.pathMapping}")
    private String pathMapping;

    /**
     * 鍒涘缓OpenAPI Bean
     */
    @Bean
    public OpenAPI customOpenAPI()
    {
        return new OpenAPI()
                .info(new Info()
                        .title("耆康云盾健康监测平台 API 文档")
                        .description("面向健康监测、风险预警、设备管理与运营分析的接口文档。")
                        .contact(new Contact().name(qkydConfig.getName()))
                        .version("版本号: " + qkydConfig.getVersion())
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("Spring Boot Wiki Documentation")
                        .url("https://spring.io/projects/spring-boot"))
                .components(new Components()
                        .addSecuritySchemes("Authorization",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .name("Authorization")))
                .addSecurityItem(new SecurityRequirement().addList("Authorization"));
    }
}



