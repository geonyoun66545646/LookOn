// --- 모달 열기/닫기 및 메인 탭 로직 (기존 코드와 동일) ---
$(document).ready(function() {
    const openModalBtn = document.getElementById('open-category-modal');
    const closeModalBtn = document.getElementById('close-category-modal');
    const modalContainer = document.getElementById('category-modal');

    const openModal = () => modalContainer.classList.add('show');
    const closeModal = () => modalContainer.classList.remove('show');

    if (openModalBtn) {
        openModalBtn.addEventListener('click', openModal);
    }
    if (closeModalBtn) {
        closeModalBtn.addEventListener('click', closeModal);
    }

    if (modalContainer) {
        modalContainer.addEventListener('click', (event) => {
            if (event.target === modalContainer) {
                closeModal();
            }
        });
    }

    window.addEventListener('keydown', (event) => {
        if (event.key === 'Escape' && modalContainer && modalContainer.classList.contains('show')) {
            closeModal();
        }
    });

    const mainTabs = document.querySelectorAll('.lookon-main-tabs li');
    if (mainTabs.length > 0) {
        mainTabs.forEach(tab => {
            tab.addEventListener('click', function() {
                mainTabs.forEach(t => t.classList.remove('active'));
                this.classList.add('active');
            });
        });
    }

    // --- 카테고리 동적 로드 (jQuery $.ajax 기반) ---

    // UI 요소 선택 (jQuery 사용)
    const $mainCategoryListUl = $('.lookon-main-category-list ul');
    const $subCategoryGridItemsContainer = $('.lookon-sub-category-grid .lookon-grid-items');
    const $subCategoryHeaderH2 = $('.lookon-sub-category-header h2');

    /**
     * 메인 카테고리(1차 카테고리)를 서버에서 AJAX로 가져와 동적으로 렌더링합니다.
     */
    function loadMainCategories() {
        $.ajax({
            // CustomerProductController에 추가한 1차 카테고리 API 엔드포인트
            url: '/products/categories/primary',
            type: 'GET',
            dataType: 'json', // 서버에서 JSON 데이터를 받을 것임을 명시
            success: function(mainCategories) {
                $mainCategoryListUl.empty(); // 기존 목록 비우기

                if (mainCategories && mainCategories.length > 0) {
                    $.each(mainCategories, function(index, category) {
                        const $listItem = $('<li></li>');
                        // ProductCategory 도메인의 categoryName 필드 사용
                        const $anchor = $('<a></a>').attr('href', '#').text(category.categoryName);
                        $listItem.append($anchor);
                        // ProductCategory 도메인의 categoryId 필드 사용
                        $listItem.data('categoryId', category.categoryId); // jQuery .data()로 ID 저장
                        $mainCategoryListUl.append($listItem);

                        // 첫 번째 메인 카테고리를 기본으로 활성화하고 서브 카테고리 로드
                        if (index === 0) {
                            $listItem.addClass('active');
                            // 선택된 카테고리의 이름과 ID로 서브 카테고리 섹션 업데이트
                            updateSubCategorySection(category.categoryName, category.categoryId);
                        }
                    });

                    // 동적으로 생성된 li 요소들에 클릭 이벤트 리스너를 한 번만 바인딩 (이벤트 위임 대신 각 요소에 바인딩)
                    $mainCategoryListUl.find('li').on('click', function() {
                        $mainCategoryListUl.find('li').removeClass('active'); // 모든 active 클래스 제거
                        $(this).addClass('active'); // 현재 클릭된 요소에 active 클래스 추가

                        const selectedCategoryName = $(this).find('a').text();
                        const selectedCategoryId = $(this).data('categoryId'); // jQuery .data()로 ID 가져오기
                        updateSubCategorySection(selectedCategoryName, selectedCategoryId);
                    });

                } else {
                    $mainCategoryListUl.append('<li><a href="#">카테고리 없음</a></li>');
                }
            },
            error: function(xhr, status, error) {
                console.error("메인 카테고리 로드 중 오류 발생:", error);
                $mainCategoryListUl.append('<li><a href="#">카테고리 로드 실패</a></li>');
            }
        });
    }

    /**
     * 선택된 메인 카테고리에 해당하는 서브 카테고리(2차 카테고리)를 서버에서 AJAX로 가져와 렌더링합니다.
     * @param {string} mainCategoryId - 선택된 메인 카테고리 ID
     */
    function loadSubCategories(mainCategoryId) {
        $.ajax({
            // CustomerProductController에 추가한 2차 카테고리 API 엔드포인트
            url: '/products/categories/sub/' + mainCategoryId,
            type: 'GET',
            dataType: 'json',
            success: function(subCategories) {
                $subCategoryGridItemsContainer.empty(); // 기존 서브 카테고리 아이템 모두 제거

                if (subCategories && subCategories.length > 0) {
                    $.each(subCategories, function(i, subCategory) {
                        const $gridItem = $('<div></div>').addClass('lookon-grid-item');
                        // ProductCategory 도메인의 categoryId 필드 사용 (서브 카테고리 ID)
                        $gridItem.data('subCategoryId', subCategory.categoryId); 
                        $gridItem.data('subCategoryName', subCategory.categoryName); // 서브 카테고리 이름도 저장

                        const $iconPlaceholder = $('<div></div>').addClass('lookon-icon-placeholder');
                        const $span = $('<span></span>').text(subCategory.categoryName);

                        $gridItem.append($iconPlaceholder).append($span);
                        $subCategoryGridItemsContainer.append($gridItem);

                        // ⭐⭐⭐ 변경된 부분: 서브 카테고리 클릭 시 상품 목록 페이지로 이동 ⭐⭐⭐
                        $gridItem.on('click', function() {
                            const clickedSubCategoryId = $(this).data('subCategoryId');
                            const clickedSubCategoryName = $(this).data('subCategoryName');
                            console.log('서브 카테고리 클릭:', clickedSubCategoryName, clickedSubCategoryId);
                            
                            // 카테고리 모달 닫기
                            closeModal();
                            
                            // 해당 카테고리 ID로 상품 목록 페이지로 이동
                            // CustomerProductController의 @GetMapping("/category/{categoryId}") 엔드포인트 활용
                            window.location.href = `/products/category/${clickedSubCategoryId}`;
                        });
                        // ⭐⭐⭐ 변경된 부분 끝 ⭐⭐⭐
                    });
                } else {
                    $subCategoryGridItemsContainer.append('<p style="text-align:center; color:#888;">해당 카테고리에 서브 카테고리가 없습니다.</p>');
                }
            },
            error: function(xhr, status, error) {
                console.error(`서브 카테고리 (ID: ${mainCategoryId}) 로드 중 오류 발생:`, error);
                $subCategoryGridItemsContainer.append('<p style="text-align:center; color:#888;">서브 카테고리 로드 실패</p>');
            }
        });
    }

    /**
     * 메인 카테고리 선택 시 헤더 업데이트 및 서브 카테고리 로드 함수를 호출합니다.
     * @param {string} categoryName - 선택된 메인 카테고리 이름
     * @param {string} categoryId - 선택된 메인 카테고리 ID
     */
    function updateSubCategorySection(categoryName, categoryId) {
        // 'N' 뱃지 여부는 백엔드 데이터에 따라 달라질 수 있습니다. 현재는 하드코딩되어 있습니다.
        $subCategoryHeaderH2.html(`${categoryName} <span class="lookon-new-badge">N</span>`); 
        loadSubCategories(categoryId); // 서브 카테고리 로드 함수 호출
    }

    // --- 페이지 초기 로드 시 메인 카테고리 로드 ---
    loadMainCategories();
});