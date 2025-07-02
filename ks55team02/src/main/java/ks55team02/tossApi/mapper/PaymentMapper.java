package ks55team02.tossApi.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.tossApi.domain.OrderDTO;
import ks55team02.tossApi.domain.PaymentDTO;
import ks55team02.tossApi.domain.PaymentHistoryDTO;

@Mapper
public interface PaymentMapper {
	
	// === ID 생성 ===
    String selectNextPaymentId();
    String selectNextPaymentHistoryId();
    
    // === 주문 (Orders) 관련 ===
    /**
     * '결제 대기' 상태의 주문 정보를 orders 테이블에 저장합니다.
     * @param orderDTO 저장할 주문 정보
     */
    void insertOrder(OrderDTO orderDTO);

    /**
     * 주문 상태를 업데이트합니다. (예: '결제 완료'로 변경)
     * @param params orderId와 status를 담은 Map
     */
    void updateOrderStatus(Map<String, Object> params);

    // === 결제 (Payments) 관련 ===
	void insertPayment(PaymentDTO paymentDTO);
    void insertPaymentHistory(PaymentHistoryDTO paymentHistoryDTO);
    
    String selectNextOrderId();
}
