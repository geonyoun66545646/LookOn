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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import ks55team02.customer.register.domain.UserJoinRequest;
import ks55team02.customer.register.mapper.CustomerRegisterMapper;
import ks55team02.customer.register.service.CustomerRegisterService;
import lombok.extern.slf4j.Slf4j;

/**
 * 고객 회원가입 관련 API 요청을 처리하는 REST 컨트롤러 클래스입니다.
 * 회원가입 처리 및 아이디, 닉네임, 이메일, 전화번호 중복 확인 기능을 제공합니다.
 */
@RestController
@RequestMapping("/api/customer/register")
@Slf4j
public class CustomerRegisterController {


	private final CustomerRegisterService customerRegisterService;
	private final CustomerRegisterMapper customerRegisterMapper;

	/**
	 * CustomerRegisterController의 생성자입니다.
	 * Spring에 의해 CustomerRegisterService와 CustomerRegisterMapper 빈이 주입됩니다.
	 *
	 * @param customerRegisterService 회원가입 비즈니스 로직을 처리하는 서비스
	 * @param customerRegisterMapper  회원가입 관련 데이터베이스 작업을 처리하는 매퍼
	 */
	public CustomerRegisterController(CustomerRegisterService customerRegisterService,
			CustomerRegisterMapper customerRegisterMapper) {
		this.customerRegisterService = customerRegisterService;
		this.customerRegisterMapper = customerRegisterMapper;
	}

	/**
	 * 새로운 사용자의 회원가입 요청을 처리하는 API 엔드포인트입니다.
	 * JSON 형식의 회원가입 폼 데이터를 받아 회원가입 로직을 수행합니다.
	 *
	 * @param userJoinRequest 요청 본문(RequestBody)으로 전송된 회원가입 폼 데이터 {@link UserJoinRequest} DTO
	 * @return                회원가입 처리 결과에 따른 HTTP 응답 엔티티 (성공/실패 메시지 포함)
	 */
	@PostMapping("/join")
	@ResponseBody
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
	 * 사용자 아이디의 중복 여부를 확인하는 API 엔드포인트입니다.
	 *
	 * @param userLgnId 중복 여부를 검사할 사용자 로그인 아이디
	 * @return          아이디의 중복 여부를 나타내는 Boolean 값 (true: 중복됨, false: 사용 가능)을 담은 HTTP 응답 엔티티
	 */
	@GetMapping("/check-id")
	public ResponseEntity<Boolean> checkId(@RequestParam String userLgnId) {
		boolean isDuplicate = customerRegisterMapper.idCheck(userLgnId);
		return ResponseEntity.ok(isDuplicate);
	}

	/**
	 * 사용자 닉네임의 중복 여부를 확인하는 API 엔드포인트입니다.
	 *
	 * @param userNcnm 중복 여부를 검사할 사용자 닉네임
	 * @return         닉네임의 중복 여부를 나타내는 Boolean 값 (true: 중복됨, false: 사용 가능)을 담은 HTTP 응답 엔티티
	 */
	@GetMapping("/check-nickname")
	public ResponseEntity<Boolean> checkNickname(@RequestParam String userNcnm) {
		boolean isDuplicate = customerRegisterMapper.nicknameCheck(userNcnm);
		return ResponseEntity.ok(isDuplicate);
	}

	/**
	 * 사용자의 이메일 주소 중복 여부를 확인하는 API 엔드포인트입니다.
	 *
	 * @param emlAddr 중복 여부를 검사할 이메일 주소
	 * @return        이메일 주소의 중복 여부를 나타내는 Boolean 값 (true: 중복됨, false: 사용 가능)을 담은 HTTP 응답 엔티티
	 */
	@GetMapping("/check-email")
	public ResponseEntity<Boolean> checkEmail(@RequestParam String emlAddr) {
		boolean isDuplicate = customerRegisterMapper.emailCheck(emlAddr);
		return ResponseEntity.ok(isDuplicate);
	}

	/**
	 * 사용자의 전화번호 중복 여부를 확인하는 API 엔드포인트입니다.
	 *
	 * @param telno 중복 여부를 검사할 전화번호 (예: "010-1234-5678" 형식)
	 * @return      전화번호의 중복 여부를 나타내는 Boolean 값 (true: 중복됨, false: 사용 가능)을 담은 HTTP 응답 엔티티
	 */
	@GetMapping("/check-telno")
	public ResponseEntity<Boolean> checkTelno(@RequestParam String telno) {
		boolean isDuplicate = customerRegisterMapper.telnoCheck(telno);
		return ResponseEntity.ok(isDuplicate);
	}
}
