package ks55team02.tossapi.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import ks55team02.customer.coupons.domain.UserCoupons;
import ks55team02.tossapi.domain.PayOrderDTO;
import ks55team02.tossapi.domain.PaymentDTO;
import ks55team02.tossapi.domain.PaymentHistoryDTO;

@Mapper
public interface PaymentMapper {

    // ID 생성
    String selectNextOrderId();
    String selectNextOrderItemId();
    String selectNextPaymentId();
    String selectNextPaymentHistoryId();

    // INSERT
    int insertOrder(PayOrderDTO payorderDTO);
    int insertOrderItem(Map<String, Object> itemData);
    int insertPayment(PaymentDTO paymentDTO);
    int insertPaymentHistory(PaymentHistoryDTO historyDTO);

    // SELECT
    List<UserCoupons> findUserCouponsByUserId(String userNo);
    List<Map<String, Object>> getOrderedProductsByOrderId(String orderId);
    Map<String, Object> selectUserInfoForShipping(String userNo);
    String findLatestOrderIdByUserNo(String userNo);
    String getPaymentIdByPgDlngId(String paymentKey);
    
    // 사용자 ID로 주문 및 결제 내역 조회
    List<Map<String, Object>> getUserOrderPaymentHistory(String userNo);
    
    PayOrderDTO getOrderDetailsByOrderId(String orderId); 
    
    /**
     * 주문 번호로 주문 상세 정보를 조회합니다. (이전 오류 수정)
     * @param orderId 조회할 주문 번호
     * @return 주문 정보 DTO
     */
    PayOrderDTO getOrderByOrdrNo(String orderId);

    // UPDATE
    int updateOrderStatus(Map<String, Object> params);
    int updatePaymentStatusAndCompletionDate(Map<String, Object> params);
    int updateUserCouponToUsed(String userCpnId);
    int updateUserInfoShippingAddress(Map<String, Object> shippingAddressData);
    /**
     * 상점별 매출 금액을 누적 업데이트합니다.
     * @param storeId 상점 ID
     * @param salesAmount 추가할 매출 금액
     */
    void updateTotalSalesAmount(@Param("storeId") String storeId, @Param("salesAmount") BigDecimal salesAmount);
    
    void deletePurchasedItemsFromCart(Map<String, Object> params);
}