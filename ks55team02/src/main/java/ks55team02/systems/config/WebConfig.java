package ks55team02.systems.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import lombok.extern.slf4j.Slf4j;

@Configuration("systemWebConfig")
@Slf4j
public class WebConfig implements WebMvcConfigurer{

    @Value("${file.path}")
    private String fileRealPath;
    
//    @Value("${profile.image.path}")
//    private String profileImagePath;  // 프로필 이미지 전용 경로

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // --- 경로 디버깅 로그 ---
        log.info("==================== 리소스 핸들러 설정 시작 ====================");
        log.info("1. application.properties에서 주입된 file.path: {}", fileRealPath);

        String osPrefix = getOSFilePath();
        log.info("2. OS 감지 후 생성된 접두사 (osPrefix): {}", osPrefix);

        String sanitizedRealPath = fileRealPath.replace("\\", "/");
        log.info("3. 역슬래시를 슬래시로 변환한 경로 (sanitizedRealPath): {}", sanitizedRealPath);
        
        String resourceLocation = osPrefix + sanitizedRealPath + "/attachment/";
        log.info("4. 최종 조합된 리소스 위치 (resourceLocation): {}", resourceLocation);
        log.info("===============================================================");

        registry.addResourceHandler("/attachment/**")
                .addResourceLocations(resourceLocation)
                .setCachePeriod(3600)
                .resourceChain(true)
                .addResolver(new PathResourceResolver());

        WebMvcConfigurer.super.addResourceHandlers(registry);
        
    }

    public String getOSFilePath() {
        String rootPath = "file:"; // Linux/Mac 기본값
        String os = System.getProperty("os.name").toLowerCase();
        
        if(os.contains("win")) {

            rootPath = "file:///c"; // Windows 접두사

        }
        
        return rootPath;
    }
}