package ks55team02.admin.adminpage.storeadmin.premiumadd.service.impl;

import ks55team02.admin.adminpage.storeadmin.premiumadd.domain.PremiumAddDTO;
import ks55team02.admin.adminpage.storeadmin.premiumadd.mapper.PremiumAddMapper;
import ks55team02.admin.adminpage.storeadmin.premiumadd.service.PremiumAddService; // PremiumAddService 임포트 추가

import lombok.RequiredArgsConstructor; // Lombok @RequiredArgsConstructor 임포트 추가
import lombok.extern.log4j.Log4j2; // Lombok @Log4j2 임포트 추가

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID; // UUID 생성을 위해 추가

@Service
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성하여 의존성 주입 (@Autowired 대체)
@Log4j2 // Lombok이 private static final Logger log = LoggerFactory.getLogger(클래스명.class); 코드를 자동 생성
@Transactional // 서비스 계층 전체에 트랜잭션 적용
public class PremiumAddServiceImpl implements PremiumAddService {

    private final PremiumAddMapper premiumAddMapper; // final 키워드와 @RequiredArgsConstructor로 주입

    /**
     * 새로운 구독 플랜 등록
     * @param premiumAddDTO 등록할 구독 플랜 정보
     * @return 등록 성공 여부
     */
    @Override
    public boolean registerSubscriptionPlan(PremiumAddDTO premiumAddDTO) {
        log.info("구독 플랜 등록 서비스 시작: {}", premiumAddDTO);
        // ID가 없다면 UUID로 자동 생성
        if (premiumAddDTO.getSbscrPlanId() == null || premiumAddDTO.getSbscrPlanId().isEmpty()) {
            premiumAddDTO.setSbscrPlanId("plan_" + UUID.randomUUID().toString().substring(0, 8));
        }

        // 유효성 검사 (Service 단에서 한번 더 검사)
        if (premiumAddDTO.getSbscrPlanNm() == null || premiumAddDTO.getSbscrPlanNm().isEmpty() ||
            premiumAddDTO.getSbscrPlanTermVal() == null || premiumAddDTO.getSbscrPlanTermVal() <= 0 ||
            premiumAddDTO.getSbscrPlanPrc() == null || premiumAddDTO.getSbscrPlanPrc().compareTo(BigDecimal.ZERO) < 0) {
            log.warn("필수 필드 누락 또는 유효하지 않은 값으로 인해 구독 플랜 등록 실패: {}", premiumAddDTO);
            return false;
        }

        int affectedRows = premiumAddMapper.insertSubscriptionPlan(premiumAddDTO);
        return affectedRows > 0;
    }

    /**
     * 모든 구독 플랜 조회
     * @return 모든 구독 플랜 목록
     */
    @Override
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션
    public List<PremiumAddDTO> getAllPremiumAdds() {
        log.info("모든 구독 플랜 조회 서비스 시작");
        return premiumAddMapper.findAllSubscriptionPlans();
    }

    /**
     * ID로 특정 구독 플랜 조회 (수정 폼에 사용)
     * @param planId 조회할 플랜 ID
     * @return 해당 플랜 정보 또는 null
     */
    @Override
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션
    public PremiumAddDTO getPlanById(String planId) {
        log.info("ID로 구독 플랜 조회 서비스 시작. planId: {}", planId);
        return premiumAddMapper.getPlanById(planId);
    }

    /**
     * ID로 특정 구독 플랜 조회 (인터페이스에 존재하므로 구현)
     * @param sbscrPlanId 조회할 플랜 ID
     * @return 해당 플랜 정보 또는 null
     */
    @Override
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션
    public PremiumAddDTO getSubscriptionPlanById(String sbscrPlanId) {
        log.info("ID로 구독 플랜 조회 서비스 시작 (getSubscriptionPlanById). sbscrPlanId: {}", sbscrPlanId);
        return premiumAddMapper.findSubscriptionPlanById(sbscrPlanId); // 매퍼에 이 메서드 이름으로 쿼리 정의 필요
    }

    /**
     * 구독 플랜 정보 업데이트 (인터페이스에 존재하므로 구현)
     * @param premiumAddDTO 업데이트할 구독 플랜 정보
     * @return 업데이트 성공 여부
     */
    @Override
    @Transactional
    public boolean updateSubscriptionPlan(PremiumAddDTO premiumAddDTO) {
        log.info("구독 플랜 업데이트 서비스 시작. premiumAddDTO: {}", premiumAddDTO);
        if (premiumAddDTO.getSbscrPlanId() == null || premiumAddDTO.getSbscrPlanId().isEmpty()) {
            log.warn("업데이트할 구독 플랜의 ID가 누락되었습니다.");
            return false;
        }
        int affectedRows = premiumAddMapper.updateSubscriptionPlan(premiumAddDTO);
        return affectedRows > 0;
    }

    /**
     * 구독 플랜 정보 수정 (modifySubscriptionPlan)
     * @param planId 수정할 플랜 ID
     * @param modifiedPlan 수정된 플랜 정보
     * @return 수정 성공 여부
     */
    @Override
    @Transactional
    public boolean modifySubscriptionPlan(String planId, PremiumAddDTO modifiedPlan) {
        log.info("구독 플랜 수정 서비스 시작. planId: {}, modifiedPlan: {}", planId, modifiedPlan);
        // DTO에 ID 설정 (경로 변수에서 받아온 ID가 정확한지 확인)
        modifiedPlan.setSbscrPlanId(planId);
        int result = premiumAddMapper.modifySubscriptionPlan(modifiedPlan); // 매퍼에 modifySubscriptionPlan 쿼리 필요
        return result > 0;
    }

    /**
     * 구독 플랜 삭제
     * @param sbscrPlanId 삭제할 구독 플랜 ID
     * @return 삭제 성공 여부
     */
    @Override
    public boolean deleteSubscriptionPlan(String sbscrPlanId) {
        log.info("구독 플랜 삭제 서비스 시작. planId: {}", sbscrPlanId);
        
        // 1. 해당 구독 플랜 ID에 연결된 결제 내역(자식 데이터)을 먼저 삭제합니다.
        // 이 작업이 성공해야 부모 데이터를 삭제할 수 있습니다.
        premiumAddMapper.deleteSubscriptionPaymentsByPlanId(sbscrPlanId);

        // 2. 결제 내역 삭제 후, 부모 데이터인 구독 플랜을 삭제합니다.
        int affectedRows = premiumAddMapper.deleteSubscriptionPlan(sbscrPlanId);

        return affectedRows > 0;
    }
}