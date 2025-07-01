package ks55team02.seller.products.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");

        // 추가적인 정적 리소스 경로 설정 (필요 시)
        // registry.addResourceHandler("/uploads/**")
        //         .addResourceLocations("file:///C:/your/absolute/path/to/uploads/");
    }
}