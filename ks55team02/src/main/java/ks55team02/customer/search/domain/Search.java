package ks55team02.customer.search.domain;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Search {
	private List<ProductsSearch> products;
    private List<PostsSearch> posts; // posts를 users보다 위로 이동
    private List<UsersSearch> users;
}
