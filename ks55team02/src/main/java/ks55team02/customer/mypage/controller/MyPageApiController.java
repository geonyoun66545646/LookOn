package ks55team02.customer.mypage.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import ks55team02.customer.login.domain.LoginUser;
import ks55team02.customer.mypage.domain.OrderHistory;
import ks55team02.customer.mypage.domain.PasswordChangeRequest;
import ks55team02.customer.mypage.domain.UserUpdateRequest;
import ks55team02.customer.mypage.mapper.ProfileMapper;
import ks55team02.customer.mypage.service.MyPageUserInfoService;
import ks55team02.customer.mypage.service.OrderHistoryService;
import ks55team02.customer.mypage.service.ProfileService;
import ks55team02.customer.mypage.service.SecuritySettingsService;
import ks55team02.util.CustomerPagination;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 고객 마이페이지 관련 API 요청을 처리하는 REST 컨트롤러 클래스입니다.
 * 회원 정보 수정, 프로필 관리, 비밀번호 변경, 주문 내역 조회 등의 기능을 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
@Slf4j
public class MyPageApiController {

	@Qualifier("myPageUserInfoService")
	private final MyPageUserInfoService myPageUserInfoMapper;
	private final SecuritySettingsService securitySettingsService;
	private final ProfileService profileService;
	private final ProfileMapper profileMapper;
	private final OrderHistoryService orderHistoryService;


	/**
     * 현재 로그인된 회원의 정보를 수정하는 API 엔드포인트입니다.
     * 클라이언트로부터 PUT 요청을 통해 수정할 사용자 데이터를 JSON 형태로 받아 처리합니다.
     *
     * @param request       HTTP 요청 객체. 세션 정보를 얻기 위해 사용됩니다.
     * @param requestData   요청 본문(RequestBody)으로 전송된 사용자 수정 정보 {@link UserUpdateRequest} DTO.
     * {@code @Valid} 어노테이션을 통해 DTO에 정의된 유효성 검사 규칙이 자동으로 적용됩니다.
     * @return              회원 정보 수정 결과에 따른 JSON 응답 (성공 여부, 메시지 포함)
     */
	@PutMapping("/info")
    public ResponseEntity<Map<String, Object>> updateUserInfo(
            HttpServletRequest request,
            @RequestBody @Valid UserUpdateRequest requestData) {

        Map<String, Object> response = new HashMap<>();
        HttpSession session = request.getSession(false); // 기존 세션이 없으면 null 반환

        // 1. 로그인 세션 유효성 검사
        if (session == null || session.getAttribute("loginUser") == null) {
            response.put("success", false);
            response.put("message", "인증 정보가 유효하지 않습니다. 다시 로그인해주세요.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");

        try {
        	// 2. 서비스 계층을 호출하여 회원 정보 수정 로직을 수행합니다.
            String resultMessage = myPageUserInfoMapper.updateUserInfo(loginUser.getUserNo(), requestData);
            
            // 3. 성공 응답 반환
            response.put("success", true);
            response.put("message", resultMessage); // "성공", "변경없음" 등의 메시지가 서비스로부터 반환됩니다.
            return ResponseEntity.ok(response); // HTTP 200 OK

        } catch (IllegalArgumentException e) {
        	// 4. 서비스에서 발생시킨 '중복 데이터' 관련 예외(예: 이미 사용 중인 이메일)를 처리합니다.
            response.put("success", false);
            response.put("message", e.getMessage()); // "이미 사용 중인..." 메시지
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response); // 409 Conflict

        } catch (Exception e) {
        	// 5. 그 외 예측하지 못한 모든 서버 내부 오류를 처리합니다.
            response.put("success", false);
            response.put("message", "서버 내부 오류로 정보 수정에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
	/**
     * 프로필 정보(닉네임, 자기소개, 프로필 이미지) 수정을 처리하는 API 엔드포인트입니다.
     * 클라이언트로부터 PUT 요청과 함께 {@code multipart/form-data} 형태로 텍스트 데이터와 파일 데이터를 받아 처리합니다.
     *
     * @param userNcnm      폼 데이터로 전송된 새로운 닉네임 문자열
     * @param selfIntroCn   폼 데이터로 전송된 새로운 자기소개 내용 문자열
     * @param profileImage  폼 데이터로 전송된 프로필 이미지 파일. 필수가 아님 (required = false).
     * @param session       현재 로그인된 사용자의 HTTP 세션 정보. 사용자 식별(userNo)에 사용됩니다.
     * @return              프로필 수정 처리 결과에 따른 JSON 응답 (성공 여부, 메시지 포함)
     */
    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestParam("userNcnm") String userNcnm,
            @RequestParam("selfIntroCn") String selfIntroCn,
            @RequestParam(value = "profileImageFile", required = false) MultipartFile profileImage,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        // 1. 세션에서 로그인된 사용자 정보 (userNo)를 가져옵니다.
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            response.put("success", false);
            response.put("message", "로그인 세션이 만료되었거나 유효하지 않습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
        	// 2. 서비스 계층을 호출하여 프로필 업데이트 로직을 수행합니다.
            profileService.updateProfile(loginUser.getUserNo(), userNcnm, selfIntroCn, profileImage);

         	// 3. 성공 응답을 반환합니다.
            response.put("success", true);
            response.put("message", "프로필이 성공적으로 저장되었습니다.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
        	// 4. 모든 종류의 예외(파일 저장 실패, 데이터베이스 오류 등)를 처리합니다.
            log.error("프로필 업데이트 중 에러 발생 - userNo: {}", loginUser.getUserNo(), e);
            response.put("success", false);
            response.put("message", "프로필 저장 중 오류가 발생했습니다. 다시 시도해주세요.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 사용자가 입력한 닉네임의 중복 여부를 실시간으로 확인하는 API 엔드포인트입니다.
     * 본인 닉네임은 중복 검사 대상에서 제외됩니다.
     *
     * @param userNcnm 검사할 닉네임 문자열 (쿼리 파라미터로 전송)
     * @param session  현재 로그인한 사용자를 식별하기 위한 세션 정보. 본인 닉네임을 제외하는 데 사용됩니다.
     * @return         사용 가능 여부({@code isAvailable}: true/false)와 메시지({@code message})를 담은 JSON 응답
     */
    @GetMapping("/profile/check-nickname")
    public ResponseEntity<Map<String, Object>> checkNickname(
            @RequestParam("userNcnm") String userNcnm,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");

        // 1. 비로그인 상태일 경우 검사가 무의미하므로 처리합니다.
        if (loginUser == null) {
            response.put("isAvailable", false);
            response.put("message", "로그인 상태가 아닙니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // 2. ProfileMapper를 사용하여 본인을 제외한 다른 사용자의 닉네임 중복 여부를 확인합니다.
        int duplicateCount = profileMapper.countByUserNcnmForOthers(loginUser.getUserNo(), userNcnm);
        
        // 3. 중복된 닉네임이 없으면(0이면) 사용 가능하다고 판단합니다.
        boolean isAvailable = (duplicateCount == 0);
        response.put("isAvailable", isAvailable);
        
        return ResponseEntity.ok(response);
    }

    
    /**
     * 비밀번호 변경 요청을 처리하는 API 엔드포인트입니다.
     * 클라이언트로부터 PUT 요청을 통해 현재 비밀번호와 새로운 비밀번호를 JSON 형태로 받아 처리합니다.
     *
     * @param request       비밀번호 변경 요청 데이터를 담은 {@link PasswordChangeRequest} DTO.
     * {@code @Valid} 어노테이션을 통해 DTO에 정의된 유효성 검사 규칙이 자동으로 적용됩니다.
     * @param bindingResult {@code @Valid} 어노테이션에 의해 수행된 유효성 검사의 결과를 담는 객체.
     * 검사 실패 시 어떤 필드에서 어떤 오류가 발생했는지 상세 정보를 포함합니다.
     * @param session       현재 로그인된 사용자의 HTTP 세션 정보. 사용자 식별(userNo)에 사용됩니다.
     * @return              비밀번호 변경 처리 결과에 따른 JSON 응답 (상태, 메시지 포함)
     */
    @PutMapping("/security/password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @Valid @RequestBody PasswordChangeRequest request,
            BindingResult bindingResult,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        // 1. 요청 DTO의 유효성 검사 (Jakarta Validation)를 수행합니다.
        if (bindingResult.hasErrors()) {
            String errorMessages = bindingResult.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            log.warn("비밀번호 변경 요청 DTO 유효성 검사 실패: {}", errorMessages);
            response.put("status", "error");
            response.put("message", "입력값이 올바르지 않습니다: " + errorMessages);
            return ResponseEntity.badRequest().body(response); // HTTP 400 Bad Request
        }

        // 2. 세션에서 로그인된 사용자 정보 (userNo)를 가져옵니다.
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser"); // "loginUser"는 세션에 저장된 키 이름으로 가정
        if (loginUser == null) {
            log.warn("세션에 로그인 사용자 정보 없음. 비정상 접근 또는 세션 만료.");
            response.put("status", "error");
            response.put("message", "로그인 세션이 만료되었거나 유효하지 않습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response); // HTTP 401 Unauthorized
        }
        String userNo = loginUser.getUserNo();
        if (userNo == null || userNo.isEmpty()) {
            log.error("세션 loginUser 객체에 userNo가 없음 - loginUser: {}", loginUser);
            response.put("status", "error");
            response.put("message", "사용자 정보를 식별할 수 없습니다. 다시 로그인 해주세요.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // HTTP 500 Internal Server Error
        }

        try {
        	// 3. 서비스 계층을 호출하여 비밀번호 변경 로직을 수행합니다.
            boolean success = securitySettingsService.changePassword(userNo, request);

            if (success) {
            	// 4. 비밀번호 변경 성공 시 응답을 구성합니다.
                response.put("status", "success");
                response.put("message", "비밀번호가 성공적으로 변경되었습니다.");
                log.info("비밀번호 변경 성공 - userNo: {}", userNo);
                return ResponseEntity.ok(response); // HTTP 200 OK
            } else {
            	// Service에서 false를 반환하는 경우는 없도록 설계했지만, 방어 코드로 남겨둡니다.
                response.put("status", "error");
                response.put("message", "비밀번호 변경에 실패했습니다. 알 수 없는 오류.");
                log.error("비밀번호 변경 Service에서 false 반환 - userNo: {}", userNo);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // HTTP 500 Internal Server Error
            }
        } catch (IllegalArgumentException e) {
        	// 5. 서비스에서 발생시킨 비즈니스 로직 예외 (예: 현재 비밀번호 불일치, 새 비밀번호와 확인 비밀번호 불일치)를 처리합니다.
            log.warn("비밀번호 변경 비즈니스 로직 오류 - userNo: {}, 오류: {}", userNo, e.getMessage());
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response); // HTTP 400 Bad Request
        } catch (RuntimeException e) {
        	// 6. 그 외 서비스에서 발생 가능한 런타임 예외 (예: 데이터베이스 업데이트 실패)를 처리합니다.
            log.error("비밀번호 변경 중 시스템 오류 발생 - userNo: {}, 오류: {}", userNo, e.getMessage(), e);
            response.put("status", "error");
            response.put("message", "비밀번호 변경 중 시스템 오류가 발생했습니다. 다시 시도해주세요.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // HTTP 500 Internal Server Error
        }
    }

    
    /**
     * 파일 업로드 처리 중 발생하는 {@link IOException}을 전역적으로 처리하는 예외 핸들러 메소드입니다.
     * 이 컨트롤러 내에서 파일 업로드 관련 IO 예외가 발생하면 이 메소드가 호출됩니다.
     *
     * @return 파일 업로드 실패 메시지를 담은 HTTP 500 응답
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<?> handleFileUploadError() {
        return ResponseEntity.status(500).body("파일 업로드 실패");
    }

    
    
    
    /**
     * 현재 로그인된 사용자의 주문 내역을 페이징하여 조회하는 API 엔드포인트입니다.
     * 클라이언트로부터 GET 요청과 함께 페이지 번호를 받아 해당 페이지의 주문 내역을 반환합니다.
     *
     * @param page    요청된 페이지 번호. 기본값은 1입니다. (@RequestParam 어노테이션의 defaultValue 사용)
     * @param session 현재 HTTP 세션 객체. 로그인 사용자 정보(userNo)를 얻기 위해 사용됩니다.
     * @return        페이징된 주문 내역 정보({@link CustomerPagination}<{@link OrderHistory}>})를 담은 JSON 응답.
     * 로그인되어 있지 않거나 오류 발생 시 적절한 HTTP 상태 코드와 null 또는 에러 응답을 반환할 수 있습니다.
     */
    @GetMapping("/orders")
    public ResponseEntity<CustomerPagination<OrderHistory>> getMyOrderHistory(
            @RequestParam(defaultValue = "1") int page,
            HttpSession session) {

    	// 1. 세션에서 로그인된 사용자 정보를 가져옵니다.
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
        	
        	// 2. 로그인되어 있지 않으면, 빈 데이터를 담아 401 Unauthorized 응답을 반환합니다.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        String userNo = loginUser.getUserNo();
        
        // 3. 한 페이지에 보여줄 데이터 개수를 10개로 고정합니다.
        int pageSize = 10;

        // 4. 서비스 계층을 호출하여 페이징된 주문 내역 데이터를 가져옵니다.
        CustomerPagination<OrderHistory> orderHistoryData = orderHistoryService.getMyOrderHistory(userNo, page, pageSize);
        
        // 5. 성공 응답 (HTTP 200 OK)과 함께 페이징된 데이터를 반환합니다.
        return ResponseEntity.ok(orderHistoryData);
    }
}