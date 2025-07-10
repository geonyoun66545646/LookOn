//package ks55team02.config;
//
//import java.util.List;
//
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//import ks55team02.customer.register.domain.User;
//import ks55team02.customer.register.mapper.CustomerRegisterMapper;
//import ks55team02.systems.crypto.utils.PasswordEncryptor;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class PasswordMigrationRunner implements CommandLineRunner {
//
//	private final CustomerRegisterMapper customerRegisterMapper;
//    private final PasswordEncryptor passwordEncryptor;
//    
//    
// // 이 플래그는 러너가 한 번 실행된 후 다시 실행되지 않도록 제어하는 용도입니다.
//    // 마이그레이션 완료 후 이 클래스를 삭제하거나 @Component 주석 처리할 것이므로, 필수는 아닙니다.
////     private static boolean alreadyRun = true;
//
//    @Override
//    public void run(String... args) throws Exception {
//        // // 한 번만 실행되도록 제어 (필요시 주석 해제하여 사용)
//        // if (alreadyRun) {
//        //     log.info("비밀번호 마이그레이션 러너는 이미 실행되었습니다. 스킵합니다.");
//        //     return;
//        // }
//
//        log.info("====== 비밀번호 마이그레이션 러너 시작 ======");
//
//        try {
//            // 1. DB에서 모든 사용자 정보 조회 (user_no, user_lgn_id, user_pswd_encpt_val 필요)
//            List<User> usersToMigrate = customerRegisterMapper.findAllUsersForPasswordMigration();
//
//            int migratedCount = 0;
//            for (User user : usersToMigrate) {
//            	if (user == null) {
//                    log.warn("조회된 사용자 목록에 null User 객체가 포함되어 있어 해당 항목을 건너뜁니다.");
//                    continue; // null인 경우 이 반복을 건너뛰고 다음 항목으로 넘어갑니다.
//                }
//            	
//                String userNo = user.getUserNo();
//                String userLgnId = user.getUserLgnId(); // 로깅을 위해 로그인 ID 사용
//                String currentPswdVal = user.getUserPswdEncptVal();
//
//                if (userNo == null || userLgnId == null) {
//                    log.warn("사용자 (UserNo: {}, UserLgnId: {}) 정보가 불완전하여 마이그레이션에서 스킵합니다.", userNo, userLgnId);
//                    continue;
//                }
//                
//                // 현재 비밀번호 값이 null이거나 비어있으면 스킵
//                if (currentPswdVal == null || currentPswdVal.isEmpty()) {
//                    log.warn("사용자 [{}] (UserNo: {})의 비밀번호가 비어있어 마이그레이션에서 스킵합니다.", userLgnId, userNo);
//                    continue;
//                }
//
//                // 2. {noop} 접두사 제거 (만약 있다면)
//                String plainPasswordCandidate = currentPswdVal;
//                if (plainPasswordCandidate.startsWith("{noop}")) {
//                    plainPasswordCandidate = plainPasswordCandidate.substring("{noop}".length());
//                }
//                // 여기서는 DB에 구 암호화 방식이 남아있지 않고, 평문 또는 {noop}평문 상태라고 가정합니다.
//                // 만약 여전히 다른 복호화 불가능한 해시 값이 있다면, 해당 비밀번호는 마이그레이션할 수 없습니다.
//
//                // 3. 새로운 SHA-256 + Salt 방식으로 암호화
//                String newHashedPassword = passwordEncryptor.hashPassword(plainPasswordCandidate);
//
//                // 4. DB에 업데이트
//                // user 객체에 새로 암호화된 비밀번호를 설정
//                user.setUserPswdEncptVal(newHashedPassword);
//                // 매퍼를 통해 업데이트 요청
//                customerRegisterMapper.updateUserPassword(user);
//
//                log.info("사용자 [{}] (UserNo: {}) 비밀번호 마이그레이션 완료.", userLgnId, userNo);
//                migratedCount++;
//            }
//
//            log.info("====== 비밀번호 마이그레이션 러너 완료. 총 {}명의 사용자 비밀번호가 마이그레이션되었습니다. ======", migratedCount);
//
//            // 마이그레이션 성공 후 플래그 설정 (필요시)
//            // alreadyRun = true;
//
//        } catch (Exception e) {
//            log.error("비밀번호 마이그레이션 중 오류 발생: {}", e.getMessage(), e);
//            // 오류 발생 시 애플리케이션 시작을 중단할지 여부는 프로젝트 정책에 따릅니다.
//            // throw e; // 필요시 오류를 다시 던져 애플리케이션 시작을 중단
//        }
//    }
//}
