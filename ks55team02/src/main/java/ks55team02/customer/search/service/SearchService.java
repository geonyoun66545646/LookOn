package ks55team02.customer.search.service;

import ks55team02.customer.search.domain.PostsSearch;
import ks55team02.customer.search.domain.ProductsSearch;
import ks55team02.customer.search.domain.Search;
import ks55team02.customer.search.domain.UsersSearch;
import ks55team02.util.CustomerPagination;

public interface SearchService {

    // '전체' 탭 미리보기를 위한 메서드 (그대로 유지)
	 Search searchAll(String keyword, String userNo, String ipAddress);
	
    // ▼▼▼ 개별 탭의 페이지네이션을 위한 메서드들 (수정) ▼▼▼
    CustomerPagination<ProductsSearch> getProductsList(String keyword, int page);
    CustomerPagination<PostsSearch> getPostsList(String keyword, int page);
    CustomerPagination<UsersSearch> getUsersList(String keyword, int page);
}