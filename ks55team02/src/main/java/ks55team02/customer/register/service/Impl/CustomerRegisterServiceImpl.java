package ks55team02.customer.register.service.Impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.customer.register.domain.User;
import ks55team02.customer.register.domain.UserJoinRequest;
import ks55team02.customer.register.domain.UserProfile;
import ks55team02.customer.register.domain.UserSecurity;
import ks55team02.customer.register.mapper.CustomerRegisterMapper;
import ks55team02.customer.register.service.CustomerRegisterService;
import ks55team02.systems.crypto.utils.PasswordEncryptor;

@Service
public class CustomerRegisterServiceImpl implements CustomerRegisterService {
	
	private static final Logger log = LoggerFactory.getLogger(CustomerRegisterServiceImpl.class);

    private final CustomerRegisterMapper customerRegisterMapper;
    
 // 생성자를 통한 의존성 주입 (가장 권장되는 방식)
    public CustomerRegisterServiceImpl(CustomerRegisterMapper customerRegisterMapper) {
        this.customerRegisterMapper = customerRegisterMapper;
    }
    
    @Override
    @Transactional // 이 메소드 내의 모든 DB 작업은 하나의 트랜잭션으로 처리됨
    public void joinUser(UserJoinRequest userJoinRequest) {
    	
    	// 1. 새로운 user_no 생성
        String lastUserNo = customerRegisterMapper.getLastUserNo();
        int nextUserNum = 1; // 기본값 (회원이 한 명도 없을 경우)
        if (lastUserNo != null && !lastUserNo.isEmpty()) {
            try {
                nextUserNum = Integer.parseInt(lastUserNo.substring(lastUserNo.lastIndexOf("_") + 1)) + 1;
            } catch (NumberFormatException e) {
                log.error("마지막 회원 번호 파싱 실패: {}", lastUserNo, e);
                // 파싱 실패 시 예외를 던지거나, 대체 정책(예: 현재시간 기반 ID)을 수립할 수 있습니다.
                // 여기서는 간단하게 기본값 1을 사용하도록 두겠습니다.
            }
        }
        String newUserNo = "user_no_" + nextUserNum;	       
        log.info("새로운 회원 번호 생성: {}", newUserNo);

        // 2. UserJoinRequest DTO를 각 테이블 DTO로 변환 및 값 설정
        // 2-1. User 객체 설정
        User user = new User();
        // 사용자 번호
        user.setUserNo(newUserNo);     
        // 사용자 아이디
        user.setUserLgnId(userJoinRequest.getUserLgnId());
        
        String rawPassword = userJoinRequest.getUserPswd();
        String hashedPassword = PasswordEncryptor.hashPassword(rawPassword);
        user.setUserPswdEncptVal(hashedPassword); // 암호화된 비밀번호를 DB 저장용 필드에 설정
        
        // 사용자 이름
        user.setUserNm(userJoinRequest.getUserNm());
        
        // 사용자 닉네임
        user.setUserNcnm(userJoinRequest.getUserNcnm());
        
        // 사용자 이메일
        user.setEmlAddr(userJoinRequest.getEmlAddr());
        
        // 전화번호를 합쳐서 저장 (예: 010-1234-5678)
        String telno = String.join("-", userJoinRequest.getTelno1(), userJoinRequest.getTelno2(), userJoinRequest.getTelno3());
        user.setTelno(telno);
        
        // 생년월일 
        String birthdateString = userJoinRequest.getUserBrdt();
        if (birthdateString != null && !birthdateString.isEmpty()) {
            // HTML의 <input type="date">는 'yyyy-MM-dd' 형식의 문자열을 반환합니다.
            user.setUserBrdt(LocalDate.parse(birthdateString, DateTimeFormatter.ISO_LOCAL_DATE));
        }
        
        // 사용자 주소
        user.setZipCd(userJoinRequest.getZipCd());
        user.setAddr(userJoinRequest.getAddr());
        user.setDaddr(userJoinRequest.getDaddr());
        
        // 서비스 단에서 설정하는 기본값들
        user.setMbrGrdCd("grd_cd_3"); // 예시: 기본 회원 등급
        user.setUserStatus("활동"); // 기본 상태 '활동'
        user.setGenderSeCd(userJoinRequest.getGenderSeCd());
        
        // 2-2. UserProfile 객체 설정
        UserProfile userProfile = new UserProfile();
        userProfile.setUserNo(newUserNo);
        
        // 체크박스가 선택되지 않으면 null이 오므로, 'N'으로 기본값 처리
        // 사용자의 푸쉬알림 수신 동의 체크
        boolean pushAgreed = userJoinRequest.getPushNtfctnRcptnAgreYn() != null;
        userProfile.setPushNtfctnRcptnAgreYn(pushAgreed);
        
        // 사용자의 이메일 수신 동의 체크
        boolean emailAgreed = userJoinRequest.getEmlRcptnAgreYn() != null;
        userProfile.setEmlRcptnAgreYn(emailAgreed);

        // 2-3. UserSecurity 객체 설정 (user_no만 필요)
        UserSecurity userSecurity = new UserSecurity();
        userSecurity.setUserNo(newUserNo);

        userSecurity.setMfaUseYn(false); 	

        // 3. Mapper를 호출하여 3개 테이블에 순차적으로 INSERT
        customerRegisterMapper.addUser(user);
        customerRegisterMapper.addUserSecurity(userSecurity);
        customerRegisterMapper.addUserProfile(userProfile);
        
        log.info("회원가입 성공: {}", user.getUserLgnId());
    }

}
