package com.qkyd;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * web瀹瑰櫒涓繘琛岄儴缃?
 * 
 * @author ruoyi
 */
public class QkydServletInitializer extends SpringBootServletInitializer
{
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application)
    {
        return application.sources(QkydApplication.class);
    }
}

