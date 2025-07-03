// DOM이 완전히 로드된 후에 스크립트가 실행되도록 합니다.
document.addEventListener('DOMContentLoaded', function() {

	// ===============================================
	// 1. 요소 선택 (DOM Elements)
	// ===============================================

	// 툴바 요소 (카테고리는 Thymeleaf로 초기 렌더링되므로 JS에서 동적 로드하지 않음)
	const mainCategoryNav = document.querySelector('.main-category-nav');
	const genderButtons = document.querySelectorAll('.filter-button[data-filter-group="gender"]');

	// 모달 관련 요소
	const modalTriggers = document.querySelectorAll('.app-modal-trigger');
	const modalOverlay = document.querySelector('.app-filter-modal-overlay');
	const modalCloseButton = document.querySelector('.app-modal-close-button');
	const modalTabButtons = document.querySelectorAll('.app-modal-tab-button');
	const modalTabPanes = document.querySelectorAll('.app-modal-tab-pane');
	const viewProductsButton = document.querySelector('.app-view-products-button'); // 상품보기 버튼

	// 드래그 스크롤 대상 요소 (메인 카테고리 네비게이션)
	const slider = document.querySelector('.main-category-nav');

	// 모달 내 동적으로 로드될 필터 옵션 컨테이너
	const colorOptionsContainer = document.getElementById('color-options-container');
	const apparelSizeOptionsContainer = document.getElementById('apparel-size-options-container');
	const shoeSizeOptionsContainer = document.getElementById('shoe-size-options-container');
	const brandOptionsContainer = document.getElementById('brand-options-container');
	const productTypeOptionsContainer = document.getElementById('product-type-options-container');
	const saleTypeOptionsContainer = document.getElementById('sale-type-options-container'); // 상품유형 내 판매유형

	// 가격 직접 입력 관련 요소
	const minPriceInput = document.querySelector('.app-min-price-input');
	const maxPriceInput = document.querySelector('.app-max-price-input');
	const applyPriceButton = document.querySelector('.app-apply-price-button');

	// ⭐ 정렬 드롭다운 관련 요소 추가
	const sortDropdownButton = document.querySelector('.sort-dropdown-button');
	const sortDropdownContainer = document.querySelector('.sort-dropdown-container');
	const sortOptionLinks = document.querySelectorAll('.sort-dropdown-menu .sort-option-link');


	// ===============================================
	// 2. 함수 정의 (Function Definitions)
	// ===============================================

	/**
	 * 필터 모달을 여는 함수
	 */
	function openModal() {
		if (modalOverlay) {
			modalOverlay.classList.add('show');
			document.body.classList.add('app-modal-open');
			// 모달이 열릴 때 동적 필터 옵션들을 로드
			loadDynamicFilterOptions();
		}
	}

	/**
	 * 필터 모달을 닫는 함수
	 */
	function closeModal() {
		if (modalOverlay) {
			modalOverlay.classList.remove('show');
			document.body.classList.remove('app-modal-open');
		}
	}

	/**
	 * 모달 내의 탭을 전환하는 함수
	 * @param {Event} event - 클릭 이벤트 객체
	 */
	function switchTab(event) {
		const targetTab = event.currentTarget.dataset.tab;
		modalTabButtons.forEach(btn => btn.classList.remove('active'));
		modalTabPanes.forEach(pane => pane.classList.remove('active'));
		event.currentTarget.classList.add('active');
		const targetPane = document.getElementById(targetTab + '-content');
		if (targetPane) {
			targetPane.classList.add('active');
		}
		// 탭 전환 시 해당 탭의 동적 옵션을 로드 (이미 로드된 경우 다시 로드하지 않도록 개선 필요)
		// loadDynamicFilterOptions(targetTab); // 선택된 탭에만 로드하도록 변경 가능
	}

	/**
	 * 동적으로 필터 옵션을 가져와 렌더링하는 함수 (초기 로드 또는 모달 열릴 때)
	 */
	function loadDynamicFilterOptions() {
		// 컬러 옵션 로드
		fetchAndRenderOptions('/api/filter-options/colors', colorOptionsContainer, 'color');
		// 의류 표준 사이즈 로드
		fetchAndRenderOptions('/api/filter-options/sizes/apparel', apparelSizeOptionsContainer, 'apparelSize');
		// 신발 표준 사이즈 로드
		fetchAndRenderOptions('/api/filter-options/sizes/shoe', shoeSizeOptionsContainer, 'shoeSize');
		// 브랜드 로드
		fetchAndRenderOptions('/api/filter-options/brands', brandOptionsContainer, 'brand');
		// 상품 유형 로드 (예: "상의", "하의" 등)
		fetchAndRenderOptions('/api/filter-options/product-types', productTypeOptionsContainer, 'productType');
		// 판매 유형 로드 (예: "신상품", "베스트" 등) - HTML에 'app-product-type-filter-group'과 'app-sale-type-filter-group'이 분리되어 있으므로
		// 판매 유형에 해당하는 API가 있다면 호출 (현재 API 주석 처리됨)
		// fetchAndRenderOptions('/api/filter-options/sale-types', saleTypeOptionsContainer, 'saleType');
	}

	/**
	 * API에서 옵션 데이터를 가져와 체크박스/라디오 형태로 렌더링하는 범용 함수
	 * @param {string} apiUrl - 옵션 데이터를 가져올 API URL
	 * @param {HTMLElement} containerElement - 옵션을 삽입할 DOM 컨테이너 요소
	 * @param {string} namePrefix - input 요소의 name 속성 접두사
	 * @param {string} type - 'checkbox' 또는 'radio'
	 */
	function fetchAndRenderOptions(apiUrl, containerElement, namePrefix, type = 'checkbox') {
		// 컨테이너 요소가 없거나, 이미 자식이 있는 경우 (이미 로드된 경우) 다시 로드하지 않음
		if (!containerElement || containerElement.children.length > 0) {
			return;
		}

		fetch(apiUrl)
			.then(response => {
				if (!response.ok) {
					throw new Error(`HTTP error! status: ${response.status}`);
				}
				return response.json();
			})
			.then(options => {
				containerElement.innerHTML = ''; // 기존 내용 비우기
				options.forEach(option => {
					const label = document.createElement('label');
					label.classList.add('app-filter-option');
					// API 응답의 객체 속성 이름에 따라 'value', 'name' 또는 'label', 'id' 등을 사용
					label.innerHTML = `<input type="${type}" name="${namePrefix}" value="${option.value}"> ${option.name}`;
					containerElement.appendChild(label);
				});
			})
			.catch(error => {
				console.error(`${namePrefix} 옵션 로드 오류:`, error);
				containerElement.innerHTML = `<p>${namePrefix} 옵션을 불러올 수 없습니다.</p>`;
			});
	}

	/**
	 * 모달 내에서 선택된 모든 필터 값을 수집하는 함수
	 * @returns {Object} 필터 이름(key)과 선택된 값들(value)을 포함하는 객체
	 */
	function collectAllFilters() {
		const filters = {};

		// 1. 성별 필터
		const selectedGenderButton = document.querySelector('.filter-button[data-filter-group="gender"].active');
		if (selectedGenderButton) {
			filters.gender = selectedGenderButton.dataset.gender;
		}

		// 2. 스타일 필터 (하드코딩된 체크박스)
		const selectedStyles = Array.from(document.querySelectorAll('#style-content input[name="style"]:checked'))
			.map(input => input.value);
		if (selectedStyles.length > 0) {
			filters.styles = selectedStyles;
		}

		// 3. 컬러 필터 (동적 로드된 체크박스)
		const selectedColors = Array.from(document.querySelectorAll('#color-content input[name="color"]:checked'))
			.map(input => input.value);
		if (selectedColors.length > 0) {
			filters.colors = selectedColors;
		}

		// 4. 가격 필터
		const selectedPriceRangeRadio = document.querySelector('#price-content input[name="priceRange"]:checked');
		if (selectedPriceRangeRadio && selectedPriceRangeRadio.value !== 'all') {
			filters.priceRange = selectedPriceRangeRadio.value;
		} else { // 직접 입력 가격
			const min = parseInt(minPriceInput.value);
			const max = parseInt(maxPriceInput.value);
			if (!isNaN(min) && min >= 0) {
				filters.minPrice = min;
			}
			if (!isNaN(max) && max >= 0) {
				filters.maxPrice = max;
			}
		}

		// 5. 사이즈 필터 (의류, 신발, 패션소품)
		const selectedApparelSizes = Array.from(document.querySelectorAll('#apparel-size-options-container input[name="apparelSize"]:checked'))
			.map(input => input.value);
		if (selectedApparelSizes.length > 0) {
			filters.apparelSizes = selectedApparelSizes;
		}
		const selectedShoeSizes = Array.from(document.querySelectorAll('#shoe-size-options-container input[name="shoeSize"]:checked'))
			.map(input => input.value);
		if (selectedShoeSizes.length > 0) {
			filters.shoeSizes = selectedShoeSizes;
		}
		const selectedAccessorySizes = Array.from(document.querySelectorAll('#size-content input[name="accessorySize"]:checked'))
			.map(input => input.value);
		if (selectedAccessorySizes.length > 0) {
			filters.accessorySizes = selectedAccessorySizes;
		}

		// 6. 브랜드 필터
		const selectedBrands = Array.from(document.querySelectorAll('#brand-options-container input[name="brand"]:checked'))
			.map(input => input.value);
		if (selectedBrands.length > 0) {
			filters.brands = selectedBrands;
		}
		// TODO: 브랜드 검색어 입력 필드의 값도 필요하다면 추가

		// 7. 할인율 필터
		const selectedDiscountRates = Array.from(document.querySelectorAll('.app-discount-rate-filter-group input[name="discountRate"]:checked'))
			.map(input => input.value);
		if (selectedDiscountRates.length > 0) {
			filters.discountRates = selectedDiscountRates;
		}

		// 8. 상품유형 필터
		const selectedProductTypes = Array.from(document.querySelectorAll('#product-type-options-container input[name="productType"]:checked'))
			.map(input => input.value);
		if (selectedProductTypes.length > 0) {
			filters.productTypes = selectedProductTypes;
		}
		// 판매 유형 (saleType)도 동적으로 로드된다면 해당 값 수집 로직 추가
		const selectedSaleTypes = Array.from(document.querySelectorAll('#sale-type-options-container input[name="saleType"]:checked'))
			.map(input => input.value);
		if (selectedSaleTypes.length > 0) {
			filters.saleTypes = selectedSaleTypes;
		}

		// 9. 품절포함 필터
		const includeSoldOut = document.querySelector('.app-misc-filter-group input[name="includeSoldOut"]:checked');
		if (includeSoldOut) {
			filters.includeSoldOut = true;
		}

		console.log("수집된 필터:", filters);
		return filters;
	}

	/**
	 * 수집된 필터로 상품 목록을 AJAX로 새로고침하는 함수
	 * @param {Object} filters - collectAllFilters()에서 반환된 필터 객체
	 * @param {string} [sortByParam] - 정렬 기준 (예: 'new', 'price_asc'). 이 값이 있으면 filters 객체의 sortBy를 덮어씁니다.
	 */
	function applyFiltersAndLoadProducts(filters, sortByParam = null) {
		const queryParams = new URLSearchParams();

		// 현재 URL에서 기존 파라미터를 가져와 유지
		const currentUrlParams = new URLSearchParams(window.location.search);
		for (const [key, value] of currentUrlParams.entries()) {
			if (!filters[key] && key !== 'sortBy') { // 필터에 없거나 sortBy가 아니면 기존 파라미터 유지
				queryParams.append(key, value);
			}
		}

		// 필터 객체를 URL 쿼리 파라미터로 변환 (배열 처리 포함)
		for (const key in filters) {
			if (Array.isArray(filters[key])) {
				filters[key].forEach(value => queryParams.append(key, value));
			} else {
				queryParams.set(key, filters[key]);
			}
		}

		// 정렬 기준이 별도로 제공되면 filters 객체의 sortBy를 덮어씀
		if (sortByParam) {
			queryParams.set('sortBy', sortByParam);
		} else if (filters.sortBy) { // filters 객체에 sortBy가 있다면 사용
			queryParams.set('sortBy', filters.sortBy);
		} else { // sortBy 파라미터가 명시되지 않았다면 'new'를 기본값으로 설정
			queryParams.set('sortBy', 'new');
		}

		// 현재 URL의 카테고리 ID를 유지
		const currentCategoryId = currentUrlParams.get('categoryId');
		if (currentCategoryId) {
			queryParams.set('categoryId', currentCategoryId);
		}

		const apiUrl = `/customer/products/list?${queryParams.toString()}`; // 💡 직접 URL 이동

		// 상품 보기 버튼이나 정렬 버튼 클릭 시 페이지를 새로고침하여 서버에서 새로운 목록을 가져옴
		window.location.href = apiUrl;

		// --- AJAX로 상품 목록만 업데이트하는 경우 (현재는 페이지 새로고침) ---
		// fetch(apiUrl)
		//   .then(response => {
		//     if (!response.ok) {
		//       throw new Error(`HTTP error! status: ${response.status}`);
		//     }
		//     // Thymeleaf fragment를 반환한다면 text()로, JSON을 반환한다면 json()으로 받음
		//     return response.text(); 
		//   })
		//   .then(htmlContent => {
		//     // TODO: 가져온 HTML content를 사용하여 상품 진열 영역을 업데이트하는 로직 구현
		//     // 예: document.getElementById('product-display-area').innerHTML = htmlContent;
		//     console.log('필터링된 상품 목록 HTML (또는 데이터) 로드 완료');
		//     closeModal(); // 상품 보기 후 모달 닫기
		//   })
		//   .catch(error => {
		//     console.error('상품 로드 오류:', error);
		//     // 사용자에게 오류 메시지 표시
		//   });
	}


	// ===============================================
	// 3. 이벤트 리스너 등록 (Event Listeners)
	// ===============================================

	// ----- 1. 툴바 기능 (카테고리 링크 클릭 이벤트 위임) ----- 
	if (mainCategoryNav) {
		mainCategoryNav.addEventListener('click', function(event) {
			const clickedLink = event.target.closest('.main-category-link');

			if (clickedLink) {
				// 드래그 중에는 링크 이동 방지
				if (slider && slider.dataset.isDragging === 'true' && event.type === 'click') { // 클릭 이벤트만 방지
					event.preventDefault();
					return;
				}

				// 클릭된 카테고리 링크에 active 클래스 적용 (UI 업데이트)
				mainCategoryNav.querySelectorAll('.main-category-link').forEach(item => item.classList.remove('active'));
				clickedLink.classList.add('active');
				console.log('선택된 카테고리:', clickedLink.textContent, 'URL:', clickedLink.href);

				// 카테고리 링크는 페이지 이동을 수행하므로, 별도로 applyFiltersAndLoadProducts를 호출할 필요 없음
				// 다만, AJAX로 카테고리 변경 시 상품 목록을 업데이트하려면 event.preventDefault()를 하고
				// applyFiltersAndLoadProducts를 호출해야 합니다. (현재는 페이지 이동)
			}
		});
	}

	// 성별 필터 버튼 클릭 이벤트
	genderButtons.forEach(button => {
		button.addEventListener('click', function() {
			// 이미 active면 해제, 아니면 active 설정 (토글)
			if (this.classList.contains('active')) {
				this.classList.remove('active');
			} else {
				genderButtons.forEach(btn => btn.classList.remove('active')); // 다른 성별 버튼 active 해제
				this.classList.add('active');
			}
			console.log('선택된 성별 필터:', this.textContent);
			// 성별 필터는 모달 외부에도 있으므로, 클릭 시 바로 상품을 다시 로드할 수 있습니다.
			// 모달의 '상품보기' 버튼과 일관성을 위해 주석 처리하거나, 필요에 따라 활성화하세요.
			// applyFiltersAndLoadProducts(collectAllFilters()); 
		});
	});


	// ----- 2. 모달 기능 ----- 
	// '상품 필터' 버튼 및 드롭다운 모달 트리거 클릭 시 모달 열기
	modalTriggers.forEach(trigger => {
		trigger.addEventListener('click', openModal);
	});
	// 모달 닫기 버튼 클릭 시
	if (modalCloseButton) {
		modalCloseButton.addEventListener('click', closeModal);
	}
	// 모달 오버레이 배경 클릭 시 닫기
	if (modalOverlay) {
		modalOverlay.addEventListener('click', function(event) {
			if (event.target === modalOverlay) { // 클릭된 요소가 정확히 오버레이 자체일 때만 닫기
				closeModal();
			}
		});
	}
	// ESC 키 눌렀을 때 모달 닫기
	document.addEventListener('keydown', function(event) {
		if (event.key === 'Escape' && modalOverlay && modalOverlay.classList.contains('show')) {
			closeModal();
		}
	});
	// 모달 탭 버튼 클릭 시 탭 전환
	modalTabButtons.forEach(button => {
		button.addEventListener('click', switchTab);
	});

	// 가격 직접 입력 '적용' 버튼 클릭 이벤트
	if (applyPriceButton) {
		applyPriceButton.addEventListener('click', function() {
			// 직접 입력 적용 버튼 클릭 시 라디오 버튼 선택 해제
			const priceRangeRadios = document.querySelectorAll('#price-content input[name="priceRange"]');
			priceRangeRadios.forEach(radio => radio.checked = false);
			console.log('가격 직접 입력 적용:', minPriceInput.value, '~', maxPriceInput.value);
			// 이 시점에 바로 상품을 로드하지 않고, 최종 '상품보기' 버튼 클릭 시 applyFiltersAndLoadProducts에서 처리됩니다.
		});
	}

	// '상품보기' 버튼 클릭 이벤트 (모든 필터를 수집하여 적용)
	if (viewProductsButton) {
		viewProductsButton.addEventListener('click', function() {
			const filters = collectAllFilters(); // 모든 필터 값 수집
			applyFiltersAndLoadProducts(filters); // 수집된 필터로 상품 로드
		});
	}

	// ----- 3. 드래그 스크롤 기능 -----
	if (slider) {
		let isDown = false;
		let startX;
		let scrollLeft;
		let isDragging = false; // 드래그 중인지 여부를 나타내는 플래그

		slider.addEventListener('mousedown', (e) => {
			isDown = true;
			isDragging = false; // 마우스 다운 시에는 아직 드래그가 아님
			slider.classList.add('active-drag');
			startX = e.pageX - slider.offsetLeft;
			scrollLeft = slider.scrollLeft;
		});

		slider.addEventListener('mouseleave', () => {
			isDown = false;
			slider.classList.remove('active-drag');
		});

		slider.addEventListener('mouseup', () => {
			isDown = false;
			slider.classList.remove('active-drag');
			// 마우스 업 시 드래그 상태를 false로 설정 (링크 클릭 방지 로직에 사용)
			// 0ms setTimeout을 사용하여 클릭 이벤트가 발생하기 전에 isDragging을 업데이트
			setTimeout(() => {
				slider.dataset.isDragging = 'false';
			}, 0);
		});

		slider.addEventListener('mousemove', (e) => {
			if (!isDown) return; // 마우스가 눌러지지 않았다면 아무것도 하지 않음
			e.preventDefault(); // 기본 드래그 동작(예: 텍스트 선택) 방지
			isDragging = true; // 마우스 이동 감지 시 드래그 중으로 설정
			slider.dataset.isDragging = 'true'; // dataset으로 HTML에 드래그 상태 저장

			const x = e.pageX - slider.offsetLeft;
			const walk = (x - startX) * 2; // 스크롤 속도 조절 (2배 빠르게)
			slider.scrollLeft = scrollLeft - walk;
		});
	}

	// ⭐ ⭐ ⭐ 새로 추가된 정렬 드롭다운 기능 ⭐ ⭐ ⭐
	// 정렬 드롭다운 버튼 클릭 이벤트
	if (sortDropdownButton && sortDropdownContainer) {
		sortDropdownButton.addEventListener('click', function(event) {
			sortDropdownContainer.classList.toggle('active'); // active 클래스 토글
			event.stopPropagation(); // 이벤트 버블링 방지 (문서 클릭 시 바로 닫히는 것 방지)
		});
	}

	// 문서 전체 클릭 시 드롭다운 닫기 (드롭다운 외부 클릭 감지)
	document.addEventListener('click', function(event) {
		if (sortDropdownContainer && !sortDropdownContainer.contains(event.target)) {
			sortDropdownContainer.classList.remove('active');
		}
	});

	// 정렬 옵션 링크 클릭 이벤트
	sortOptionLinks.forEach(link => {
		link.addEventListener('click', function(event) {
			event.preventDefault(); // 링크의 기본 동작(페이지 이동) 방지

			const sortBy = this.dataset.sortBy; // data-sort-by 값 가져오기
			const selectedText = this.textContent; // 선택된 옵션의 텍스트 가져오기

			// 버튼 텍스트 업데이트
			if (sortDropdownButton) {
				sortDropdownButton.textContent = selectedText;
				// CSS의 ::after로 화살표 아이콘을 추가했다면, 여기서는 추가할 필요 없음
				// 만약 HTML에 직접 화살표가 있다면 다시 추가해 줘야 합니다. (예: `sortDropdownButton.insertAdjacentHTML('beforeend', ' ▾');`)
			}

			// 드롭다운 닫기
			if (sortDropdownContainer) {
				sortDropdownContainer.classList.remove('active');
			}

			// 필터 및 정렬 적용 함수 호출 (정렬 기준만 변경하여 페이지 새로고침)
			// collectAllFilters()를 호출하여 현재 적용된 다른 필터들도 함께 전달합니다.
			applyFiltersAndLoadProducts(collectAllFilters(), sortBy);
		});
	});

	// 페이지 로드 시 현재 URL의 'sortBy' 파라미터를 확인하여 드롭다운 버튼 텍스트 업데이트
	// 이를 통해 사용자가 페이지 새로고침 후에도 이전에 선택한 정렬 상태를 볼 수 있습니다.
	const initialUrlParams = new URLSearchParams(window.location.search);
	const currentSortBy = initialUrlParams.get('sortBy');
	if (sortDropdownButton) {
		if (currentSortBy) {
			let foundMatch = false;
			sortOptionLinks.forEach(link => {
				if (link.dataset.sortBy === currentSortBy) {
					sortDropdownButton.textContent = link.textContent;
					foundMatch = true;
					// CSS의 ::after로 화살표 아이콘이 있다면, 여기서는 추가할 필요 없음
					// sortDropdownButton.insertAdjacentHTML('beforeend', ' ▾'); // 이모지 화살표
				}
			});
			// URL에 sortBy는 있지만, 해당하는 옵션 링크가 없을 경우 기본값으로 설정
			if (!foundMatch) {
				const defaultLink = document.querySelector('.sort-option-link[data-sort-by="new"]');
				if (defaultLink) {
					sortDropdownButton.textContent = defaultLink.textContent;
					// defaultLink.insertAdjacentHTML('beforeend', ' ▾'); // 이모지 화살표
				}
			}
		} else {
			// sortBy 파라미터가 없으면 '신상품순' (data-sort-by="new")을 기본값으로 설정
			const defaultLink = document.querySelector('.sort-option-link[data-sort-by="new"]');
			if (defaultLink) {
				sortDropdownButton.textContent = defaultLink.textContent;
				// defaultLink.insertAdjacentHTML('beforeend', ' ▾'); // 이모지 화살표
			}
		}
		// 모든 경우에 버튼 텍스트 업데이트 후 화살표 아이콘이 CSS ::after로 처리된다고 가정
		// 만약 HTML로 직접 추가해야 한다면 아래 줄을 활성화
		sortDropdownButton.insertAdjacentHTML('beforeend', ' ▾'); // 모든 버튼 초기화 후 화살표 추가
	}

	// DOM 로드 시 동적 필터 옵션을 로드 (모달 열릴 때도 로드하지만, 미리 로드 필요 시 여기에 추가)
	// loadDynamicFilterOptions(); // 모달이 항상 열릴 때 로드되므로, 여기서 미리 로드할 필요 없을 수 있습니다.
});