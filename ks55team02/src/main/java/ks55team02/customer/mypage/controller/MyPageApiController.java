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
import ks55team02.customer.mypage.domain.PasswordChangeRequest;
import ks55team02.customer.mypage.domain.UserUpdateRequest;
import ks55team02.customer.mypage.mapper.ProfileMapper;
import ks55team02.customer.mypage.service.MyPageUserInfoService;
import ks55team02.customer.mypage.service.ProfileService;
import ks55team02.customer.mypage.service.SecuritySettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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


	// 회원 정보 수정
	@PutMapping("/info")
    public ResponseEntity<Map<String, Object>> updateUserInfo(
            HttpServletRequest request,
            @RequestBody @Valid UserUpdateRequest requestData) {

        Map<String, Object> response = new HashMap<>();
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("loginUser") == null) {
            response.put("success", false);
            response.put("message", "인증 정보가 유효하지 않습니다. 다시 로그인해주세요.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");

        try {
            // 새로 만든 userInfoService의 메소드를 호출합니다.
            String resultMessage = myPageUserInfoMapper.updateUserInfo(loginUser.getUserNo(), requestData);
            
            response.put("success", true);
            response.put("message", resultMessage); // "성공", "변경없음" 등의 메시지가 담김
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // Service에서 던진 '중복 데이터' 예외를 여기서 잡습니다.
            response.put("success", false);
            response.put("message", e.getMessage()); // "이미 사용 중인..." 메시지
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response); // 409 Conflict

        } catch (Exception e) {
            // 그 외 예측하지 못한 모든 서버 에러를 처리합니다.
            response.put("success", false);
            response.put("message", "서버 내부 오류로 정보 수정에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
	/**
     * 프로필 정보(닉네임, 자기소개, 이미지) 수정을 처리하는 API 엔드포인트입니다.
     * multipart/form-data 형태로 텍스트와 파일 데이터를 함께 받습니다.
     *
     * @param userNcnm    폼 데이터로 전송된 새로운 닉네임
     * @param selfIntroCn 폼 데이터로 전송된 새로운 자기소개
     * @param profileImage 폼 데이터로 전송된 프로필 이미지 파일
     * @param session     현재 로그인된 사용자의 세션 정보
     * @return            처리 결과에 따른 JSON 응답
     */
    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestParam("userNcnm") String userNcnm,
            @RequestParam("selfIntroCn") String selfIntroCn,
            @RequestParam(value = "profileImageFile", required = false) MultipartFile profileImage,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        // 1. 세션에서 로그인된 사용자 정보 (userNo) 가져오기
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            response.put("success", false);
            response.put("message", "로그인 세션이 만료되었거나 유효하지 않습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            // 2. Service 계층 호출하여 프로필 업데이트 로직 수행
            profileService.updateProfile(loginUser.getUserNo(), userNcnm, selfIntroCn, profileImage);

            // 3. 성공 응답 반환
            response.put("success", true);
            response.put("message", "프로필이 성공적으로 저장되었습니다.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // 4. 모든 종류의 예외를 처리 (파일 저장 실패, DB 오류 등)
            log.error("프로필 업데이트 중 에러 발생 - userNo: {}", loginUser.getUserNo(), e);
            response.put("success", false);
            response.put("message", "프로필 저장 중 오류가 발생했습니다. 다시 시도해주세요.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    /**
     * 닉네임 중복 여부를 실시간으로 확인하는 API 엔드포인트
     * @param userNcnm 검사할 닉네임
     * @param session 현재 로그인한 사용자를 식별하기 위함
     * @return 사용 가능 여부를 담은 JSON 응답 ({"isAvailable": true/false})
     */
    @GetMapping("/profile/check-nickname")
    public ResponseEntity<Map<String, Object>> checkNickname(
            @RequestParam("userNcnm") String userNcnm,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");

        // 비로그인 상태에서는 검사 무의미
        if (loginUser == null) {
            response.put("isAvailable", false);
            response.put("message", "로그인 상태가 아닙니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // ProfileMapper에 추가했던 중복 검사 메소드를 그대로 사용합니다.
        int duplicateCount = profileMapper.countByUserNcnmForOthers(loginUser.getUserNo(), userNcnm);
        
        // 중복된 닉네임이 없으면(0이면) 사용 가능
        boolean isAvailable = (duplicateCount == 0);
        response.put("isAvailable", isAvailable);
        
        return ResponseEntity.ok(response);
    }

    
    /**
     * 비밀번호 변경 요청을 처리하는 API 엔드포인트.
     * HTTP PUT 요청을 통해 현재 비밀번호, 새 비밀번호를 받아 처리합니다.
     *
     * @param request 비밀번호 변경 요청 DTO (현재 비밀번호, 새 비밀번호, 새 비밀번호 확인)
     * @param bindingResult @Valid 검증 결과
     * @param session 현재 로그인된 사용자의 세션 정보
     * @return 처리 결과에 따른 JSON 응답 (성공 메시지 또는 에러 메시지)
     */
    @PutMapping("/security/password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @Valid @RequestBody PasswordChangeRequest request,
            BindingResult bindingResult,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        // 1. DTO 유효성 검사 (Jakarta Validation)
        if (bindingResult.hasErrors()) {
            String errorMessages = bindingResult.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            log.warn("비밀번호 변경 요청 DTO 유효성 검사 실패: {}", errorMessages);
            response.put("status", "error");
            response.put("message", "입력값이 올바르지 않습니다: " + errorMessages);
            return ResponseEntity.badRequest().body(response); // HTTP 400 Bad Request
        }

        // 2. 세션에서 로그인된 사용자 정보 (userNo) 가져오기
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
            // 3. Service 계층 호출하여 비밀번호 변경 로직 수행
            boolean success = securitySettingsService.changePassword(userNo, request);

            if (success) {
                response.put("status", "success");
                response.put("message", "비밀번호가 성공적으로 변경되었습니다.");
                log.info("비밀번호 변경 성공 - userNo: {}", userNo);
                return ResponseEntity.ok(response); // HTTP 200 OK
            } else {
                // Service에서 false를 반환하는 경우는 없도록 설계했지만, 방어 코드
                response.put("status", "error");
                response.put("message", "비밀번호 변경에 실패했습니다. 알 수 없는 오류.");
                log.error("비밀번호 변경 Service에서 false 반환 - userNo: {}", userNo);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // HTTP 500 Internal Server Error
            }
        } catch (IllegalArgumentException e) {
            // Service에서 발생시킨 비즈니스 로직 예외 (예: 현재 비밀번호 불일치, 새 비밀번호 불일치)
            log.warn("비밀번호 변경 비즈니스 로직 오류 - userNo: {}, 오류: {}", userNo, e.getMessage());
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response); // HTTP 400 Bad Request
        } catch (RuntimeException e) {
            // 그 외 Service에서 발생 가능한 런타임 예외 (예: DB 업데이트 실패)
            log.error("비밀번호 변경 중 시스템 오류 발생 - userNo: {}, 오류: {}", userNo, e.getMessage(), e);
            response.put("status", "error");
            response.put("message", "비밀번호 변경 중 시스템 오류가 발생했습니다. 다시 시도해주세요.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // HTTP 500 Internal Server Error
        }
    }
    // >>> 비밀번호 변경 API 메서드 추가 끝
    
    // 기존 handleFileUploadError 메서드 (생략)
    @ExceptionHandler(IOException.class)
    public ResponseEntity<?> handleFileUploadError() {
        return ResponseEntity.status(500).body("파일 업로드 실패");
    }

}