package ks55team02.orderproduct.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.orderproduct.domain.OrderDTO;


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
    
    /**
     * 특정 사용자의 가장 최근 주문에 대한 상세 정보(기본 정보 + 상품 목록)를 조회합니다.
     *
     * @param userNo 조회할 사용자의 고유 번호
     * @return 'orderInfo'와 'orderedProducts' 키를 포함하는 Map.
     *         조회된 데이터가 없으면 비어있는 Map 또는 null을 반환합니다.
     */
    Map<String, Object> getLatestOrderDetailsForUser(String userNo);
    
}
