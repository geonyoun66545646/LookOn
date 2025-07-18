package ks55team02.tossapi.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import ks55team02.tossapi.domain.PayOrderDTO;
import ks55team02.tossapi.domain.PaymentDTO;
import ks55team02.tossapi.domain.PaymentHistoryDTO;

@Mapper
public interface PaymentMapper {
	
	// === ID 생성 ===
    String selectNextPaymentId();
    String selectNextPaymentHistoryId();
    String selectNextOrderId();
    String selectNextOrderItemId(); // [기존] 새로운 order_items ID 생성 메서드

    // === 주문 (Orders) 관련 ===
    /**
     * '결제 대기' 상태의 주문 정보를 orders 테이블에 저장합니다.
     * @param orderDTO 저장할 주문 정보
     */
    void insertOrder(PayOrderDTO payorderDTO);

    /**
     * 주문 상태를 업데이트합니다. (예: '결제 완료'로 변경)
     * @param params orderId와 status를 담은 Map
     */
    void updateOrderStatus(Map<String, Object> params); 

    /**
     * 주문에 포함된 각 상품 정보를 order_items 테이블에 저장합니다.
     * @param itemMap 주문 상품 상세 정보를 담은 Map
     */
    void insertOrderItem(Map<String, Object> itemMap); 

    /**
     * 결제 상태를 업데이트하고 결제 완료 일시를 설정합니다.
     * @param params pgDlngId, ordrNo, stlmSttsCd를 담은 Map
     */
    void updatePaymentStatusAndCompletionDate(Map<String, Object> params);
    
    // 사용자 ID로 주문 및 결제 내역 조회
    List<Map<String, Object>> getUserOrderPaymentHistory(String userNo);
    
    String findLatestOrderIdByUserNo(String userNo);
    Map<String, Object> getPaymentDetailsByOrderId(String ordrNo);
    // <<<<<<<<< [수정됨] 반환 타입을 Map<String, Object>에서 OrderDTO로 변경 >>>>>>>>>
    PayOrderDTO getOrderDetailsByOrderId(String ordrNo); 

    /**
     * 특정 주문 ID에 해당하는 모든 주문 항목(상품 및 옵션) 정보를 조회합니다.
     * order_items, products, product_options 테이블을 조인하여 상세 정보를 반환합니다.
     * @param orderId 주문 번호
     * @return 주문 항목 정보 목록 (Map 형태로 반환)
     */
    // <<<<<<<<< [기존/확인] 주문 항목 상세 정보 조회 메서드 >>>>>>>>>
    List<Map<String, Object>> getOrderedProductsByOrderId(String ordrNo); 

    /**
     * 특정 사용자가 보유한 특정 발행 쿠폰 코드(pblcn_cpn_cd)에 해당하는 쿠폰의 상세 정보와
     * 사용자 쿠폰의 현재 상태를 함께 조회합니다.
     * user_coupons 테이블과 coupons 테이블을 조인하여 Map 형태로 반환합니다.
     *
     * @param userNo 사용자 번호
     * @param pblcnCpnCd 발행 쿠폰 코드 (사용자가 입력하는 쿠폰 코드)
     * @return 쿠폰 상세 정보를 담은 Map (없으면 null)
     */
    Map<String, Object> getUserCouponDetails(@Param("userNo") String userNo, @Param("pblcnCpnCd") String pblcnCpnCd);

    /**
     * 특정 사용자 쿠폰(user_cpn_id)의 사용 여부(use_yn)를 '사용됨'(0)으로 업데이트합니다.
     * @param userCpnId 사용자 쿠폰 ID
     */
    void updateUserCouponToUsed(String userCpnId);
    
    // 추가: 결제 ID (pgDlngId)로 stlm_id 조회
    String getPaymentIdByPgDlngId(String pgDlngId);

    // 추가: 사용자 쿠폰 목록 조회
    List<Map<String, Object>> selectUserCoupons(String userNo);
    
    /**
     * 결제 상태 변경 이력을 payments_history 테이블에 저장합니다.
     * @param paymentHistoryDTO 저장할 결제 이력 정보
     */
    void insertPaymentHistory(PaymentHistoryDTO paymentHistoryDTO);
    
    /**
     * '결제 대기' 상태의 초기 결제 정보를 payments 테이블에 저장합니다.
     * @param paymentDTO 저장할 결제 정보
     */
    void insertPayment(PaymentDTO paymentDTO);
}
