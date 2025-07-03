package ks55team02.customer.register.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ks55team02.customer.register.domain.UserJoinRequest;
import ks55team02.customer.register.mapper.CustomerRegisterMapper;
import ks55team02.customer.register.service.CustomerRegisterService;
@RestController
@RequestMapping("/api/customer/register")
public class CustomerRegisterController {
	
	    private static final Logger log = LoggerFactory.getLogger(CustomerRegisterController.class);

	    private final CustomerRegisterService customerRegisterService;
	    private final CustomerRegisterMapper customerRegisterMapper;

	    // 생성자 주입
	    public CustomerRegisterController(CustomerRegisterService customerRegisterService, CustomerRegisterMapper customerRegisterMapper) {
	        this.customerRegisterService = customerRegisterService;
	        this.customerRegisterMapper = customerRegisterMapper;
	    }

	    /**
	     * 회원가입 처리를 위한 API
	     * @param userJoinRequest 회원가입 폼 데이터 (JSON)
	     * @return 성공/실패 메시지를 담은 ResponseEntity
	     */
	    @PostMapping("/join")
	    public ResponseEntity<String> joinUser(@RequestBody UserJoinRequest userJoinRequest) {
	        try {
	            // TODO: 추후에 정규식 등 서버사이드 유효성 검사 로직 추가 위치
	            
	            customerRegisterService.joinUser(userJoinRequest);
	            return ResponseEntity.ok("회원가입이 성공적으로 완료되었습니다.");

	        } catch (IllegalArgumentException e) {
	            log.warn("회원가입 실패 (잘못된 요청): {}", e.getMessage());
	            return ResponseEntity.badRequest().body(e.getMessage());
	        } catch (Exception e) {
	            log.error("회원가입 처리 중 서버 오류 발생", e);
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 처리 중 오류가 발생했습니다.");
	        }
	    }

	    /**
	     * 아이디 중복 확인 API
	     * @param userLgnId 검사할 아이디
	     * @return 중복 여부 (true: 중복, false: 사용 가능)
	     */
	    @GetMapping("/check-id")
	    public ResponseEntity<Boolean> checkId(@RequestParam String userLgnId) {
	        boolean isDuplicate = customerRegisterMapper.idCheck(userLgnId);
	        return ResponseEntity.ok(isDuplicate);
	    }

	    /**
	     * 닉네임 중복 확인 API
	     * @param userNcnm 검사할 닉네임
	     * @return 중복 여부
	     */
	    @GetMapping("/check-nickname")
	    public ResponseEntity<Boolean> checkNickname(@RequestParam String userNcnm) {
	        boolean isDuplicate = customerRegisterMapper.nicknameCheck(userNcnm);
	        return ResponseEntity.ok(isDuplicate);
	    }
	    
	    /**
	     * 이메일 중복 확인 API
	     * @param emlAddr 검사할 이메일
	     * @return 중복 여부
	     */
	    @GetMapping("/check-email")
	    public ResponseEntity<Boolean> checkEmail(@RequestParam String emlAddr) {
	        boolean isDuplicate = customerRegisterMapper.emailCheck(emlAddr);
	        return ResponseEntity.ok(isDuplicate);
	    }
	
}
