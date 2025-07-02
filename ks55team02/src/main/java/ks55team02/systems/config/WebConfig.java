package ks55team02.systems.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;
import lombok.extern.slf4j.Slf4j; // 로그를 위해 Slf4j 추가

@Configuration("systemWebConfig") // 빈 이름 충돌 방지를 위해 명시적으로 지정
@Slf4j // 로그 사용을 위해 추가
public class WebConfig implements WebMvcConfigurer{
	
	// application.properties에서 설정된 파일의 실제 루트 경로를 주입받습니다.
	// 예: /home/teamproject/ 또는 D:/upload/
	@Value("${file.path}")
	private String fileRealPath;
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// 모든 첨부파일 요청을 "/attachment/**" 패턴으로 처리합니다.
		// 이 요청들은 fileRealPath + "attachment/" 경로에서 파일을 찾습니다.
		// 예를 들어, fileRealPath가 "/home/teamproject/"라면,
		// 최종 리소스 위치는 "file:/home/teamproject/attachment/"가 됩니다.
		// 그 하위의 "store_images", "inquiry_images" 등은 FilesUtils에서 관리합니다.
		
		String rootPath = getOSFilePath();
		
		registry.addResourceHandler("/attachment/**")
				.addResourceLocations(rootPath + fileRealPath + "/attachment/")
				.setCachePeriod(3600)
				.resourceChain(true)
				.addResolver(new PathResourceResolver());

		WebMvcConfigurer.super.addResourceHandlers(registry);
	}
	
	// 이 getOSFilePath() 메서드는 현재 addResourceHandlers에서 사용되지 않습니다.
	// file.path 값이 이미 OS에 맞는 절대 경로를 포함해야 합니다.
	// 따라서 이 메서드는 현재 웹 리소스 핸들링 문제와 직접적인 관련이 없습니다.
	public String getOSFilePath() {
		String rootPath = "file:///";
		String os = System.getProperty("os.name").toLowerCase();
		
		if(os.contains("win")) {
			rootPath = "file:///c:"; // Windows 환경 예시
		}else if(os.contains("linux")) {
			rootPath = "file://"; // Linux 환경 예시
		}else if(os.contains("mac")) {			
			rootPath = "file://"; // Mac 환경 예시
		}
		
		return rootPath;
	}
	
}
