package ks55team02.tossApi.mapper;

import java.util.List;
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
    
    // 사용자 ID로 주문 및 결제 내역 조회
    // OrderDTO에 PaymentDTO 정보도 함께 담을 수 있도록 Join하여 조회하거나,
    // List<Map<String, Object>> 형태로 유연하게 데이터를 가져올 수 있습니다.
    // 여기서는 복합적인 정보를 가져오기 위해 Map 리스트를 반환하도록 하겠습니다.
    List<Map<String, Object>> getUserOrderPaymentHistory(String userNo);
    
    String findLatestOrderIdByUserNo(String userNo);
    Map<String, Object> getPaymentDetailsByOrderId(String orderId);
    Map<String, Object> getOrderDetailsByOrderId(String orderId);
    List<Map<String, Object>> getOrderedProductsByOrderId(String orderId);

}
