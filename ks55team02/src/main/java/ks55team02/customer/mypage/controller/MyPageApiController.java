package ks55team02.customer.mypage.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import ks55team02.customer.login.domain.LoginUser;
import ks55team02.customer.mypage.domain.UserUpdateRequest;
import ks55team02.customer.mypage.service.FileStorageService;
import ks55team02.customer.mypage.service.MyPageService;

@RestController
@RequestMapping("/api/v1/mypage")
public class MyPageApiController {

	private final MyPageService myPageService;
	private final FileStorageService fileStorageService;

    @PutMapping("/info")
    public ResponseEntity<Map<String, Object>> updateUserInfo(
            @RequestBody UserUpdateRequest request,
            HttpSession session) {
        
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인 필요"));
        }

        boolean isUpdated = myPageService.updateUserCoreInfo(loginUser.getUserNo(), request);
        return ResponseEntity.ok(Map.of("success", isUpdated));
    }

    // 프로필 수정 (파일 포함)
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
    		// 1. @RequestPart를 @RequestParam으로 변경하여 개별 텍스트 데이터 수신
            @RequestParam("userNcnm") String userNcnm,
            @RequestParam("selfIntroCn") String selfIntroCn,
            // 2. JS에서 사용한 name="profileImageFile"과 일치시키고, required=false 설정
            @RequestParam(name = "profileImageFile", required = false) MultipartFile profileImageFile,
            HttpSession session) throws IOException {

        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        // 3. 서비스 호출 방식 변경: DTO 대신 개별 데이터 전달
        myPageService.updateProfile(loginUser.getUserNo(), userNcnm, selfIntroCn, profileImageFile);

        // 4. 성공 응답 반환
        return ResponseEntity.ok(Map.of("success", true, "message", "프로필이 성공적으로 저장되었습니다."));
    }
    
    public MyPageApiController(MyPageService myPageService, FileStorageService fileStorageService) {
        this.myPageService = myPageService;
        this.fileStorageService = fileStorageService; // ✅ 명시적 주입
    }
    @ExceptionHandler(IOException.class)
    public ResponseEntity<?> handleFileUploadError() {
        return ResponseEntity.status(500).body("파일 업로드 실패");
    }
}