package ks55team02.customer.search.service.impl;

import ks55team02.customer.search.domain.PostsSearch;
import ks55team02.customer.search.domain.ProductsSearch;
import ks55team02.customer.search.domain.Search;
import ks55team02.customer.search.domain.UsersSearch;
import ks55team02.customer.search.mapper.SearchMapper;
import ks55team02.customer.search.service.SearchService;
import ks55team02.util.CustomerPagination;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final SearchMapper searchMapper;
    
    // 페이지네이션을 위한 상수 정의
    private static final int PAGE_SIZE = 10;  // 한 페이지에 보여줄 아이템 개수
    private static final int BLOCK_SIZE = 10; // 페이지네이션 블록에 보여줄 페이지 번호 개수

    /**
     * ▼▼▼ 기존에 있던 코드 (수정 없음) ▼▼▼
     * '전체' 탭의 서버 렌더링을 위한 메서드.
     * 각 항목별로 5개의 미리보기 데이터를 가져옵니다.
     */
    @Override
    public Search searchAll(String keyword) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("keyword", keyword);
        paramMap.put("limit", 5);
        paramMap.put("offset", 0);

        List<ProductsSearch> products = searchMapper.searchProducts(paramMap);
        List<PostsSearch> posts = searchMapper.searchPosts(paramMap);
        List<UsersSearch> users = searchMapper.searchUsers(paramMap);
        
        return new Search(products, posts, users);
    }

    /**
     * '상품' 탭의 페이지네이션을 위한 메서드.
     * @param keyword 검색어
     * @param page 요청 페이지 번호
     * @return 페이지네이션 정보가 포함된 상품 목록
     */
    @Override
    public CustomerPagination<ProductsSearch> getProductsList(String keyword, int page) {
        // 1. 검색어에 해당하는 전체 상품 개수를 가져옵니다.
        int totalCount = searchMapper.getProductsCount(keyword);

        // 2. DB에서 데이터를 가져올 시작 위치(offset)를 계산합니다.
        int offset = (page - 1) * PAGE_SIZE;

        // 3. DB에 전달할 파라미터를 Map에 담습니다.
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("keyword", keyword);
        paramMap.put("limit", PAGE_SIZE);
        paramMap.put("offset", offset);

        // 4. 해당 페이지의 상품 목록만 가져옵니다.
        List<ProductsSearch> products = searchMapper.searchProducts(paramMap);

        // 5. 최종적으로 데이터 목록과 페이지 정보를 담은 CustomerPagination 객체를 생성하여 반환합니다.
        return new CustomerPagination<>(products, totalCount, page, PAGE_SIZE, BLOCK_SIZE);
    }

    /**
     * '게시글' 탭의 페이지네이션을 위한 메서드.
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
        return new CustomerPagination<>(users, totalCount, page, PAGE_SIZE, BLOCK_SIZE);
    }
}