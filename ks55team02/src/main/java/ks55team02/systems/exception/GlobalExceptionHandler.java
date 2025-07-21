package ks55team02.systems.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	// [수정 시작] 불필요한 .map, favicon.ico 등의 로그를 error 대신 warn으로 처리하도록 수정
	@ExceptionHandler(NoResourceFoundException.class)
	public String NoResourceFoundHandle(HttpServletRequest request, Exception ex, Model model) {
	    String uri = request.getRequestURI();
	    String viewName = "error/404";
	
	    if(uri.startsWith("/admin")) {
	        viewName = "error/404";
	    }
	
	    StackTraceElement[] stackTrace = ex.getStackTrace();
	    StackTraceElement origin = stackTrace[0];
	
	    // 예상되는 리소스 누락에 대한 로그는 WARN 레벨로 변경 (프로그램 정상 동작)
	    // .map, .ico 파일 등 브라우저가 자동으로 요청하는 파일들에 대한 오류를 분리 처리
	    String message = ex.getMessage();
	    if (message.contains(".map") || message.contains("favicon.ico") || message.contains("devtools.json")) {
	        log.warn("[Expected Exception] {}\n[method]:{} ({}:{}) - message={}",
	                origin.getClassName(), origin.getMethodName(), origin.getFileName(),
	                origin.getLineNumber(), message);
	    } else {
	        // 그 외의 예상치 못한 리소스 누락은 ERROR로 유지 (개발자 확인 필요)
	        log.error("[Exception] {}\n[method]:{} ({}:{}) - message={}",
	                origin.getClassName(), origin.getMethodName(), origin.getFileName(),
	                origin.getLineNumber(), message);
	    }
	
	    return viewName;
	}
	// [수정 끝]
	
	@ExceptionHandler(Exception.class)
	public String globalExceptionHandle(HttpServletRequest request, Exception ex, Model model) {
	    StackTraceElement[] stackTrace = ex.getStackTrace();
	    StackTraceElement origin = stackTrace[0];
	    log.error("[Exception] {}\n[method]:{} ({}:{}) - message={}",
	            origin.getClassName(), origin.getMethodName(), origin.getFileName(),
	            origin.getLineNumber(), ex.getMessage());
	
	    return "error/500";
	}
}