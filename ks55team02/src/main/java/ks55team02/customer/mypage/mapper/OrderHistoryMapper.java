package ks55team02.customer.mypage.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.customer.mypage.domain.OrderHistory;

@Mapper
public interface OrderHistoryMapper {

	/**
     * 특정 사용자의 전체 주문 내역 개수를 조회합니다.
     * @param userNo 조회할 사용자의 번호
     * @return 해당 사용자의 전체 주문 건수
     */
    long countMyOrders(String userNo);

    /**
     * 특정 사용자의 주문 내역 목록을 페이징하여 조회합니다.
     * @param params 맵 객체 (userNo, size, offset 포함)
     * @return 페이징 처리된 주문 내역 DTO 목록
     */
    List<OrderHistory> findMyOrders(Map<String, Object> params);
}
