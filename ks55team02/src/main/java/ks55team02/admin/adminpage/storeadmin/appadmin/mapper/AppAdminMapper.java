package ks55team02.admin.adminpage.storeadmin.appadmin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import ks55team02.admin.common.domain.SearchCriteria;
import ks55team02.common.domain.store.AppStore;
import ks55team02.common.domain.store.Store;
import ks55team02.common.domain.store.StoreAccount;

@Mapper
public interface AppAdminMapper {

    int getAppAdminCount(@Param("searchCriteria") SearchCriteria searchCriteria);

    List<AppStore> getAppAdminList(@Param("searchCriteria") SearchCriteria searchCriteria, @Param("limitStart") int limitStart, @Param("pageSize") int pageSize);

    AppStore getAppAdminById(String aplyId);

    int updateAppStatus(AppStore appStore);

    int updateUserGrade(@Param("userNo") String userNo, @Param("mbrGrdCd") String gradeCode);

    int insertStore(Store store);

    int insertStoreAccount(StoreAccount storeAccount);

    Integer selectMaxStoreIdNum();
    
    Integer selectMaxAccountIdNum();
}