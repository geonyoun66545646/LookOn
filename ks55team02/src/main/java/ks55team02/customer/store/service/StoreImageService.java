package ks55team02.customer.store.service;

import org.springframework.web.multipart.MultipartFile;
import ks55team02.customer.store.domain.StoreImage;

import java.util.List;

public interface StoreImageService {

    void addStoreImage(MultipartFile file);

    void addStoreImages(MultipartFile[] files);

    void deleteStoreImage(StoreImage storeImage);

    StoreImage getStoreImageById(String imgId);

    List<StoreImage> getStoreImageList();
}
