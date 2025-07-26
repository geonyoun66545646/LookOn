package ks55team02.tossapi.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

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

    // 장바구니 삭제 (필요 시 주석 해제)
    // void deletePurchasedItemsFromCart(Map<String, Object> deleteParams);

    // 매출 집계 (필요 시 주석 해제)
    // int updateTotalSalesAmount(Map<String, Object> updateSalesParams);
    
    // 이력 조회 (필요 시 주석 해제)
    // List<Map<String, Object>> getUserOrderPaymentHistory(String userNo);
}