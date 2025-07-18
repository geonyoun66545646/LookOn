/**
 * 
 */

/**
 * 특정 페이지의 주문 내역을 불러와 화면에 렌더링하는 함수
 * @param {number} page - 불러올 페이지 번호 (기본값: 1)
 */
function loadOrderHistory(page = 1) {
    
    // 백엔드 API에 GET 요청
    $.ajax({
        type: 'GET',
        url: `/api/v1/mypage/orders?page=${page}`,
        dataType: 'json'
    })
    .done(function(paginationData) {
        
        // 1. 주문 목록 테이블의 tbody와 페이지네이션 컨테이너를 비웁니다.
        const $tbody = $('#order-history-tbody');
        const $paginationContainer = $('#pagination-container');
        $tbody.empty();
        $paginationContainer.empty();

        // 2. 주문 내역이 있는지 확인
        if (paginationData.list && paginationData.list.length > 0) {
            // 주문 내역이 있으면, "내역 없음" 메시지 숨기기
            $('#no-orders-message').hide();

            // 3. 주문 목록 렌더링: 받은 데이터(list)를 하나씩 반복하며 <tr> 행을 만듭니다.
            paginationData.list.forEach(order => {
                const orderRowHtml = `
                    <tr>
                        <td>${order.orderDate}</td>
                        <td>${order.orderNumber}</td>
                        <td class="product-info">
                            <!-- 상품 이미지가 있다면 표시 (지금은 productName만) -->
                            <span>${order.productName}</span>
                        </td>
                        <td>${order.quantity}</td>
                        <td>${order.price.toLocaleString()}원</td>
                    </tr>
                `;
                $tbody.append(orderRowHtml); // 만들어진 행을 tbody에 추가
            });

            // 4. 페이지네이션 렌더링: 받은 페이징 정보로 페이지 번호 링크를 만듭니다.
            // (이전 블록으로 가기 버튼)
            if (paginationData.hasPreviousBlock) {
                $paginationContainer.append(`<a href="#" onclick="loadOrderHistory(${paginationData.startPage - 1}); return false;">«</a>`);
            }
            
            // (페이지 번호 링크)
            for (let i = paginationData.startPage; i <= paginationData.endPage; i++) {
                // 현재 페이지는 'active' 클래스를 추가하여 강조
                const activeClass = (i === paginationData.currentPage) ? 'active' : '';
                $paginationContainer.append(`<a href="#" class="${activeClass}" onclick="loadOrderHistory(${i}); return false;">${i}</a>`);
            }

            // (다음 블록으로 가기 버튼)
            if (paginationData.hasNextBlock) {
                $paginationContainer.append(`<a href="#" onclick="loadOrderHistory(${paginationData.endPage + 1}); return false;">»</a>`);
            }

        } else {
            // 주문 내역이 없으면, "내역 없음" 메시지 보여주기
            $('#no-orders-message').show();
        }
    })
    .fail(function(xhr) {
        if (xhr.status === 401) {
            // 로그인되어 있지 않은 경우 (서버에서 401 응답 시)
            $('#no-orders-message').text('로그인 후 이용해주세요.').show();
        } else {
            // 그 외 서버 오류
            $('#no-orders-message').text('주문 내역을 불러오는 중 오류가 발생했습니다.').show();
            console.error("Order history loading error:", xhr);
        }
    });
}

// =======================================================================
//                          페이지 로딩 시 최초 실행
// =======================================================================

$(document).ready(function() {
    
    // ... (기존에 있던 다른 document ready 코드들) ...

    // 마이페이지가 로딩되면, 첫 번째 페이지의 주문 내역을 불러옵니다.
    loadOrderHistory(1);

});