package ks55team02.admin.adminpage.storeadmin.appAdmin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import ks55team02.common.domain.store.AppStore;
import ks55team02.common.domain.store.Store;
import ks55team02.common.domain.store.StoreAccount;

@Mapper
public interface AppAdminMapper {
	
	// 신청 상세 아이디 조회
	AppStore getAppAdminById(String aplyId);
	
	// AppAdmin 목록의 전체 개수를 가져오는 메서드
	public int getAppAdminCount(AppStore appStore);
	
	// 전체 개수 조회
	List<AppStore> getAppAdminList(
        @Param("appStore") AppStore appStore,
        @Param("limitStart") int limitStart,
        @Param("pageSize") int pageSize
	        );
	
	// 신청 상태 변경
	int updateAppStatus(AppStore appStore);
	
	/*
	 * 특정 사용자의 회원 등급을 업데이트합니다.
     * @param userNo 등급을 변경할 사용자의 번호 (PK)
     * @param gradeCode 새로운 회원 등급 코드
     * @return 업데이트된 행의 수
     */
    int updateUserGrade(@Param("userNo") String userNo, @Param("mbrGrdCd") String gradeCode);
	
	/**
     * stores 테이블에 새로운 상점 정보를 삽입합니다.
     * @param store 삽입할 상점 정보 DTO
     * @return 삽입된 행의 수
     */
    int insertStore(Store store);

    /**
     * store_account 테이블에 새로운 계좌 정보를 삽입합니다.
     * @param storeAccount 삽입할 계좌 정보 DTO
     * @return 삽입된 행의 수
     */
    int insertStoreAccount(StoreAccount storeAccount);
    
    /**
     * stores 테이블에서 가장 큰 store_id의 숫자 부분을 조회합니다.
     * @return 가장 큰 ID의 숫자, 데이터가 없으면 null 반환
     */
    Integer selectMaxStoreIdNum();

    /**
     * store_account 테이블에서 가장 큰 actno_id의 숫자 부분을 조회합니다.
     * @return 가장 큰 ID의 숫자, 데이터가 없으면 null 반환
     */
    Integer selectMaxAccountIdNum();
}
	

