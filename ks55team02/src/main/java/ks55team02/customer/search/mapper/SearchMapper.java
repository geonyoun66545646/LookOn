package ks55team02.customer.search.mapper;
import ks55team02.customer.search.domain.PostsSearch;

import ks55team02.customer.search.domain.ProductsSearch;
import ks55team02.customer.search.domain.UsersSearch;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface SearchMapper {

    
    List<ProductsSearch> searchProducts(Map<String, Object> paramMap);

    List<PostsSearch> searchPosts(Map<String, Object> paramMap);

    List<UsersSearch> searchUsers(Map<String, Object> paramMap);
    
    int getProductsCount(String keyword);
    int getPostsCount(String keyword);
    int getUsersCount(String keyword);
    
    String getNextSearchLogId();
    void insertSearchHistory(Map<String, Object> params);
}