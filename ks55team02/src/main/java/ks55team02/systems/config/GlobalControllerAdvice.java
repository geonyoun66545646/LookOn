package ks55team02.systems.config;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;


import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalControllerAdvice {

	/**
     * 모든 요청에 대해 모델(Model)에 공통 속성을 추가하는 메소드.
     * 컨트롤러 메소드가 실행되기 전에 호출되어, 모든 뷰(HTML)에서 "currentUrl" 변수를 사용할 수 있게 한다.
     *
     * @param model   뷰에 데이터를 전달할 모델 객체
     * @param request 현재 HTTP 요청 객체
     */
    @ModelAttribute
    public void addCommonAttributes(Model model, HttpServletRequest request) {
        StringBuffer requestURL = request.getRequestURL();
        String queryString = request.getQueryString();

        // 쿼리 스트링이 있는 경우에만 URL에 추가합니다.
        if (queryString != null) {
            requestURL.append("?").append(queryString);
        }
        
        // 모델에 "currentUrl"이라는 이름으로 완성된 전체 URL을 추가합니다.
        model.addAttribute("currentUrl", requestURL.toString());
    }
    

//	  @ExceptionHandler(NoResourceFoundException.class) public String
//	  NoResourceFoundHandle(HttpServletRequest request, Exception ex, Model model)
//	  { String uri = request.getRequestURI(); String viewName = "error/404";
//	  
//	  if(uri.startsWith("/admin")) { viewName = "error/404"; }
//	  
//	  StackTraceElement[] stackTrace = ex.getStackTrace(); StackTraceElement origin
//	  = stackTrace[0];
//	  log.error("[Exception] {}\n[method]:{} ({}:{}) - message={}",
//	  origin.getClassName(), origin.getMethodName(), origin.getFileName(),
//	  origin.getLineNumber(), ex.getMessage() );
//	  
//	  return viewName; }
//	  
//	  @ExceptionHandler(Exception.class) public String
//	  globalExceptionHandle(HttpServletRequest request, Exception ex, Model model)
//	  { StackTraceElement[] stackTrace = ex.getStackTrace(); StackTraceElement
//	  origin = stackTrace[0];
//	  log.error("[Exception] {}\n[method]:{} ({}:{}) - message={}",
//	  origin.getClassName(), origin.getMethodName(), origin.getFileName(),
//	  origin.getLineNumber(), ex.getMessage() );
//	  
//	  return "error/500"; }
//	 
//}

	/*
	 * @ExceptionHandler(NoResourceFoundException.class) public String
	 * NoResourceFoundHandle(HttpServletRequest request, Exception ex, Model model)
	 * { String uri = request.getRequestURI(); String viewName = "error/404";
	 * 
	 * if(uri.startsWith("/admin")) { viewName = "error/404"; }
	 * 
	 * StackTraceElement[] stackTrace = ex.getStackTrace(); StackTraceElement origin
	 * = stackTrace[0];
	 * log.error("[Exception] {}\n[method]:{} ({}:{}) - message={}",
	 * origin.getClassName(), origin.getMethodName(), origin.getFileName(),
	 * origin.getLineNumber(), ex.getMessage() );
	 * 
	 * return viewName; }
	 * 
	 * @ExceptionHandler(Exception.class) public String
	 * globalExceptionHandle(HttpServletRequest request, Exception ex, Model model)
	 * { StackTraceElement[] stackTrace = ex.getStackTrace(); StackTraceElement
	 * origin = stackTrace[0];
	 * log.error("[Exception] {}\n[method]:{} ({}:{}) - message={}",
	 * origin.getClassName(), origin.getMethodName(), origin.getFileName(),
	 * origin.getLineNumber(), ex.getMessage() );
	 * 
	 * return "error/500"; }
	 */
}

