package ks55team02.customer.mypage.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.customer.mypage.domain.OrderHistory;

@Mapper
public interface OrderHistoryMapper {

	/**
     * 주어진 사용자 번호(userNo)에 해당하는 사용자의 전체 주문 내역 개수를 조회합니다.
     * 이 값은 주문 내역 목록의 페이징 처리를 위해 총 레코드 수를 계산하는 데 사용됩니다.
     *
     * @param userNo 조회할 사용자의 고유 번호
     * @return     해당 사용자의 전체 주문 건수 (long 타입)
     */
    long countMyOrders(String userNo);

    /**
     * 주어진 파라미터({@code Map<String, Object> params})를 사용하여 특정 사용자의 주문 내역 목록을 페이징하여 조회합니다.
     * 이 맵은 주로 사용자 번호(userNo), 페이지당 항목 수(size), 시작 오프셋(offset) 등의 정보를 포함합니다.
     *
     * @param params 주문 내역 조회 및 페이징 조건을 담은 Map 객체.
     * 필수 키: "userNo" (String), "size" (int), "offset" (int).
     * @return       페이징 처리된 주문 내역 목록을 담은 {@code List<OrderHistory>}
     */
    List<OrderHistory> findMyOrders(Map<String, Object> params);
}
