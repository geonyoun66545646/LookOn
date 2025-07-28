package ks55team02.admin.adminpage.storeadmin.appadmin.mapper;

import ks55team02.common.domain.store.StoreImage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AppAdminFileMapper {

    /**
     * 이미지 ID로 이미지 정보 조회
     * @param imgId 이미지 ID
     * @return StoreImage DTO
     */
    StoreImage findImageById(String imgId);
}