package ks55team02.customer.store.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.common.domain.store.StoreImage;

@Mapper
public interface StoreImageMapper {

    int addStoreImage(StoreImage storeImage);

    int addStoreImages(List<StoreImage> storeImageList);

    StoreImage getStoreImageById(String imgId);

    List<StoreImage> getStoreImageList();

    int deleteStoreImageById(String imgId);
}
