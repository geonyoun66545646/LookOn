package ks55team02.admin.adminpage.storeadmin.storemngadmin.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param; // @Param 어노테이션 임포트

import ks55team02.common.domain.store.Store; // Store 클래스 임포트 확인

@Mapper
public interface StoreMngAdminMapper {

    /**
     * 상점 목록 전체 개수 조회 (검색 조건 포함)
     * @param store 검색 조건을 담고 있는 Store 객체 (SearchCriteria 상속 가정)
     * @return 전체 상점 개수
     */
    int getStoreCount(Store store);

    /**
     * 상점 목록 조회 (페이지네이션 및 검색 조건 포함)
     * @param store 페이지네이션 및 검색 조건을 담고 있는 Store 객체 (SearchCriteria 상속 가정)
     * @param limitStart LIMIT 구문의 시작 인덱스
     * @param pageSize LIMIT 구문의 가져올 레코드 개수
     * @return 상점 목록
     */
    List<Store> getStoreList(
            @Param("store") Store store, // 매개변수 이름을 XML에서 사용할 이름으로 지정
            @Param("limitStart") int limitStart,
            @Param("pageSize") int pageSize);
    
    void updateStoreStatus(@Param("storeId") String storeId,
            @Param("newStatus") String newStatus,
            @Param("infoMdfcnDt") LocalDateTime infoMdfcnDt,
            @Param("delPrcrYn") String delPrcrYn); // delPrcrYn 파라미터 유지
    
}

