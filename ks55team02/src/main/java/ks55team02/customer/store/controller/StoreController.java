package ks55team02.customer.store.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import ks55team02.admin.adminpage.useradmin.userlist.domain.UserList;
import ks55team02.common.domain.store.AppStore;
import ks55team02.customer.login.domain.LoginUser;
import ks55team02.customer.store.service.AppStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@RequestMapping("/store")
@Controller
@Log4j2
public class StoreController {

	private final AppStoreService appStoreService;

	// Store main page mapping
	@GetMapping("/storeMain")
	public String storeMain() {
		return "customer/store/storeMainView";
	}

	// Method to get the current logged-in user's number (userNo) from the session
	private String getCurrentUserId(HttpSession session) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");

        if (loginUser != null) {
            String userNo = loginUser.getUserNo();
            log.info("현재 로그인된 사용자 번호: {}", userNo);
            return userNo;
        } else {
            log.warn("세션에 로그인된 사용자 정보 (LoginUser)가 없습니다.");
            return null; // Return null if no login information is found
        }
    }

	// Load store application page
	@GetMapping("/appStore")
	public String getAppStore(Model model, HttpSession session) {
		String targetUserNo = getCurrentUserId(session);
		UserList userInfo = null; // Initialize userInfo object

		// Retrieve user information only if targetUserNo is not null
		if (targetUserNo != null) {
			userInfo = appStoreService.getUserInfo(targetUserNo);
		}

		// If userInfo is null (not logged in or failed to retrieve user info)
		// Create an empty UserList object to prevent Thymeleaf errors
		if (userInfo == null) {
			userInfo = new UserList();
			log.warn("userInfo가 null이므로 빈 UserList 객체를 모델에 추가합니다. (로그인되지 않았을 수 있습니다)");
		}

		// Add AppStore object (for form initialization) and userInfo object to the model
		model.addAttribute("appStore", new AppStore());
		model.addAttribute("userInfo", userInfo);
		model.addAttribute("title", "상점신청 페이지");

		// Set the contract application date to today's date and pass it to the view
		model.addAttribute("todayDate", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)); // YYYY-MM-DD format

		return "customer/store/appStoreView";
	}

    // Submit store application data (INSERT processing)
	@PostMapping("/appStore")
	@ResponseBody
	public ResponseEntity<String> addAppStore(
	        @ModelAttribute AppStore appStore, // ★★★ 이 DTO 하나로 대부분의 데이터를 받습니다.
	        // ★★★ 파일만 별도의 @RequestParam으로 받습니다.
	        @RequestParam(name = "businessRegistrationDoc", required = false) MultipartFile brnoImg,
	        @RequestParam(name = "telecommunicationLicense", required = false) MultipartFile cmmDclrImg,
	        @RequestParam(name = "productEvidence", required = false) MultipartFile selGdsProofImg,
	        @RequestParam(name = "idCardCopy", required = false) MultipartFile rrnoCardCopyImg,
	        @RequestParam(name = "bankAccountCopy", required = false) MultipartFile bankbookCopyImg,
	        @RequestParam(name = "etcDocs", required = false) MultipartFile etcDocImg,
	        @RequestParam(name = "storeLogoImg", required = false) MultipartFile storeLogoImg,
	        HttpSession session
	) {
	    try {
	        String loggedInUserNo = getCurrentUserId(session);
	        if (loggedInUserNo == null) {
	            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
	        }
	        appStore.setAplyUserNo(loggedInUserNo);

	        // ★★★ DTO에 데이터가 이미 바인딩 되었으므로 별도의 set 작업이 대부분 필요 없습니다.
	        // String -> LocalDate, String -> Integer/Long 같은 타입 변환은 Spring이 자동으로 처리해줍니다.

	        // 초기 신청 상태 설정
	        appStore.setAplyStts("APLY_WAIT");

	        // 파일 묶음 생성
	        Map<String, MultipartFile> uploadedFiles = new HashMap<>();
	        uploadedFiles.put("brnoImg", brnoImg);
	        uploadedFiles.put("cmmDclrImg", cmmDclrImg);
	        uploadedFiles.put("selGdsProofImg", selGdsProofImg);
	        uploadedFiles.put("rrnoCardCopyImg", rrnoCardCopyImg);
	        uploadedFiles.put("bankbookCopyImg", bankbookCopyImg);
	        uploadedFiles.put("etcDocImg", etcDocImg);
	        uploadedFiles.put("storeLogoImg", storeLogoImg);

	        // 서비스 호출
	        appStoreService.addStoreApplication(appStore, uploadedFiles);

	        return new ResponseEntity<>("상점 신청 및 계좌 정보가 성공적으로 저장되었습니다.", HttpStatus.OK);
	    } catch (Exception e) {
	        log.error("상점 신청 중 오류 발생: {}", e.getMessage(), e);
	        return new ResponseEntity<>("상점 신청 중 오류가 발생했습니다: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}

}
