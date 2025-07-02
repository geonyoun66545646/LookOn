package ks55team02.customer.store.mapper;

import org.apache.ibatis.annotations.Mapper;
import ks55team02.customer.store.domain.StoreImage;

import java.util.List;

@Mapper
public interface StoreImageMapper {

    int addStoreImage(StoreImage storeImage);

    int addStoreImages(List<StoreImage> storeImageList);

    StoreImage getStoreImageById(String imgId);

    List<StoreImage> getStoreImageList();

    int deleteStoreImageById(String imgId);
}
