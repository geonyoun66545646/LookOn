package ks55team02.admin.adminpage.storeadmin.appadmin.service;

import ks55team02.common.domain.store.StoreImage;
import org.springframework.core.io.Resource;

public interface AppAdminFileService {

    /**
     * 이미지 ID로 이미지 정보 조회
     * @param imgId 이미지 ID
     * @return StoreImage DTO
     */
    StoreImage getImageInfo(String imgId);

    /**
     * StoreImage 정보를 바탕으로 실제 파일 리소스를 로드
     * @param imageInfo DB에서 조회한 StoreImage DTO
     * @return Spring Resource 객체
     */
    Resource loadFileAsResource(StoreImage imageInfo);
}