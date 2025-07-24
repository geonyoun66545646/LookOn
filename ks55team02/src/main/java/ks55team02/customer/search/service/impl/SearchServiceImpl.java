package ks55team02.customer.search.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import ks55team02.customer.search.domain.PostsSearch;
import ks55team02.customer.search.domain.ProductsSearch;
import ks55team02.customer.search.domain.Search;
import ks55team02.customer.search.domain.UsersSearch;
import ks55team02.customer.search.mapper.SearchMapper;
import ks55team02.customer.search.service.SearchService;
import ks55team02.util.CustomerPagination;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

	private final SearchMapper searchMapper;

	// 페이지네이션을 위한 상수 정의
	private static final int PAGE_SIZE = 10;
	private static final int BLOCK_SIZE = 10;

	// ▼▼▼ 이미지 기본 경로를 상수로 정의하여 재사용합니다 ▼▼▼
	private static final String PRODUCT_IMAGE_BASE_URL = "/images/products/";
	private static final String PROFILE_IMAGE_BASE_URL = "/images/profiles/";

	/**
	 * '전체' 탭의 서버 렌더링을 위한 메서드.
	 */
	@Override
	public Search searchAll(String keyword, String userNo, String ipAddress) {

		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("keyword", keyword);
		paramMap.put("limit", 5);
		paramMap.put("offset", 0);

		List<ProductsSearch> products = searchMapper.searchProducts(paramMap);
		List<PostsSearch> posts = searchMapper.searchPosts(paramMap);
		List<UsersSearch> users = searchMapper.searchUsers(paramMap);

		// ▼▼▼ [수정] 조회된 상품과 사용자의 이미지 경로를 완성합니다 ▼▼▼
		for (ProductsSearch product : products) {
			String imagePath = product.getImgFilePathNm();
			if (imagePath != null && !imagePath.isEmpty() && !imagePath.startsWith("/")) {
				product.setImgFilePathNm(PRODUCT_IMAGE_BASE_URL + imagePath);
			}
		}

		for (UsersSearch user : users) {
			String imagePath = user.getPrflImg();
			if (imagePath != null && !imagePath.isEmpty() && !imagePath.startsWith("/")) {
				user.setPrflImg(PROFILE_IMAGE_BASE_URL + imagePath);
			}
		}
		// ▲▲▲ 여기까지 수정 ▲▲▲

		if (userNo != null && !userNo.trim().isEmpty() && keyword != null && !keyword.trim().isEmpty()) {
			Map<String, Object> historyParams = new HashMap<>();
			historyParams.put("searchLogId", searchMapper.getNextSearchLogId());
			historyParams.put("userNo", userNo);
			historyParams.put("keyword", keyword);
			historyParams.put("ipAddress", ipAddress);
			searchMapper.insertSearchHistory(historyParams);
		}

		return new Search(products, posts, users);
	}

	/**
	 * '상품' 탭의 페이지네이션을 위한 메서드.
	 */
	@Override
	public CustomerPagination<ProductsSearch> getProductsList(String keyword, int page) {
		int totalCount = searchMapper.getProductsCount(keyword);
		int offset = (page - 1) * PAGE_SIZE;

		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("keyword", keyword);
		paramMap.put("limit", PAGE_SIZE);
		paramMap.put("offset", offset);

		List<ProductsSearch> products = searchMapper.searchProducts(paramMap);
		
		if (products != null && !products.isEmpty()) {
	        log.info("▶▶▶ 경로 변경 전 첫 번째 상품 이미지: {}", products.get(0).getImgFilePathNm());
	    }

		for (ProductsSearch product : products) {
            String imagePath = product.getImgFilePathNm();
            if (imagePath != null && !imagePath.isEmpty() && !imagePath.startsWith("/")) {
                // 수정된 상수를 사용합니다.
                product.setImgFilePathNm(PRODUCT_IMAGE_BASE_URL + imagePath);
            }
        }
		// ▲▲▲ 여기까지 수정 ▲▲▲
		
		if (products != null && !products.isEmpty()) {
	        log.info("▶▶▶ 경로 변경 후 첫 번째 상품 이미지: {}", products.get(0).getImgFilePathNm());
	    }

		return new CustomerPagination<>(products, totalCount, page, PAGE_SIZE, BLOCK_SIZE);
	}

	/**
	 * '게시글' 탭의 페이지네이션을 위한 메서드 (수정 없음).
	 */
	@Override
	public CustomerPagination<PostsSearch> getPostsList(String keyword, int page) {
		int totalCount = searchMapper.getPostsCount(keyword);
		int offset = (page - 1) * PAGE_SIZE;
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("keyword", keyword);
		paramMap.put("limit", PAGE_SIZE);
		paramMap.put("offset", offset);
		List<PostsSearch> posts = searchMapper.searchPosts(paramMap);
		return new CustomerPagination<>(posts, totalCount, page, PAGE_SIZE, BLOCK_SIZE);
	}

	/**
	 * '사용자' 탭의 페이지네이션을 위한 메서드.
	 */
	@Override
	public CustomerPagination<UsersSearch> getUsersList(String keyword, int page) {
		int totalCount = searchMapper.getUsersCount(keyword);
		int offset = (page - 1) * PAGE_SIZE;

		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("keyword", keyword);
		paramMap.put("limit", PAGE_SIZE);
		paramMap.put("offset", offset);

		List<UsersSearch> users = searchMapper.searchUsers(paramMap);

		// ▼▼▼ [수정] 조회된 사용자 목록의 프로필 이미지 경로를 완성합니다 ▼▼▼
		for (UsersSearch user : users) {
			String imagePath = user.getPrflImg();
			if (imagePath != null && !imagePath.isEmpty() && !imagePath.startsWith("/")) {
				user.setPrflImg(PROFILE_IMAGE_BASE_URL + imagePath);
			}
		}
		// ▲▲▲ 여기까지 수정 ▲▲▲

		return new CustomerPagination<>(users, totalCount, page, PAGE_SIZE, BLOCK_SIZE);
	}
}