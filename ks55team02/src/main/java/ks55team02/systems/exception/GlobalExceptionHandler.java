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

	@ExceptionHandler(NoResourceFoundException.class)
	public String NoResourceFoundHandle(HttpServletRequest request, Exception ex, Model model) {
		String uri = request.getRequestURI();
		String viewName = "error/404";
		
		if(uri.startsWith("/admin")) {
			viewName = "error/404";
		}
		
		StackTraceElement[] stackTrace = ex.getStackTrace();
		StackTraceElement origin = stackTrace[0];
		log.error("[Exception] {}\n[method]:{} ({}:{}) - message={}",
					origin.getClassName(),
					origin.getMethodName(),
					origin.getFileName(),
					origin.getLineNumber(),
					ex.getMessage()
				);
		
		return viewName;
	}

	@ExceptionHandler(Exception.class)
	public String globalExceptionHandle(HttpServletRequest request, Exception ex, Model model) {
		StackTraceElement[] stackTrace = ex.getStackTrace();
		StackTraceElement origin = stackTrace[0];
		log.error("[Exception] {}\n[method]:{} ({}:{}) - message={}",
					origin.getClassName(),
					origin.getMethodName(),
					origin.getFileName(),
					origin.getLineNumber(),
					ex.getMessage()
				);
		
		return "error/500";
	}
}
