package ks55team02.customer.store.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import ks55team02.common.domain.store.StoreImage;

public interface StoreImageService {

    void addStoreImage(MultipartFile file);

    void addStoreImages(MultipartFile[] files);

    void deleteStoreImage(StoreImage storeImage);

    StoreImage getStoreImageById(String imgId);

    List<StoreImage> getStoreImageList();
}
