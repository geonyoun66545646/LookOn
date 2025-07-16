package ks55team02.orderProduct.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.orderProduct.domain.OrderDTO;


@Mapper
public interface OrderProductsMapper {
    
    /**
     * XML ID: getCombinedOrderDetailsByOrderId
     * 주문 ID로 주문/결제/배송지 등 상세 정보를 한번에 조회합니다. (JOIN 활용)
     * @param orderId 조회할 주문의 ID
     * @return 주문 상세 정보를 담은 Map 객체
     */
	OrderDTO getCombinedOrderDetailsByOrderId(String ordrNo);
    
    /**
     * XML ID: getOrderedProductsByOrderId
     * 주문 ID로 해당 주문에 포함된 상품 목록을 조회합니다. (JOIN 활용)
     * @param orderId 조회할 주문의 ID
     * @return 상품 목록을 담은 List<Map>
     */
    List<Map<String, Object>> getOrderedProductsByOrderId(String ordrNo);
    
    String selectNextOrderId();
    
    // ** 특정 userNo의 가장 최근 주문 ID를 조회하는 메서드**
    String selectLatestOrderIdByUserNo(String userNo);
    
    String findLatestOrderIdByUserNo(String userNo); 
    
}
