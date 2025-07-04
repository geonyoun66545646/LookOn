// DOM이 완전히 로드된 후에 스크립트가 실행되도록 합니다.
document.addEventListener('DOMContentLoaded', function() {

	// ===============================================
	// 1. 요소 선택 (DOM Elements) - 새로운 HTML 구조에 맞게 수정
	// ===============================================

	// 모달 관련 요소
	const modalTriggers = document.querySelectorAll('.app-modal-trigger');
	const modalOverlay = document.querySelector('.app-filter-modal-overlay');
	const modalCloseButton = document.querySelector('.app-modal-close-button');
	const modalTabButtons = document.querySelectorAll('.app-modal-tab-button');
	const modalTabPanes = document.querySelectorAll('.app-modal-tab-pane');
	const viewProductsButton = document.querySelector('.app-view-products-button');

	// 가격 직접 입력 관련 요소
	const pricePane = document.getElementById('price-pane');
	const priceDirectInputWrapper = pricePane ? pricePane.querySelector('.app-price-direct-input') : null;
	const priceInputs = priceDirectInputWrapper ? priceDirectInputWrapper.querySelectorAll('.app-price-input') : [];
	
	// 정렬 드롭다운 관련 요소
	const sortDropdownButton = document.querySelector('.sort-dropdown-button');
	const sortDropdownContainer = document.querySelector('.sort-dropdown-container');
	const sortOptionLinks = document.querySelectorAll('.sort-dropdown-menu .sort-option-link');

	// 드래그 스크롤 대상 요소
	const slider = document.querySelector('.main-category-nav');


	// ===============================================
	// 2. 함수 정의 (Function Definitions)
	// ===============================================

	/**
	 * 필터 모달 열기
	 */
	function openModal() {
		if (modalOverlay) {
			modalOverlay.classList.add('show');
			document.body.classList.add('app-modal-open');
			// 기본으로 첫 번째 탭을 활성화 (또는 이전에 선택된 탭)
			if (!document.querySelector('.app-modal-tab-button.active')) {
				const firstTabButton = document.querySelector('.app-modal-tab-button');
				if (firstTabButton) {
					firstTabButton.click();
				}
			}
		}
	}

	/**
	 * 필터 모달 닫기
	 */
	function closeModal() {
		if (modalOverlay) {
			modalOverlay.classList.remove('show');
			document.body.classList.remove('app-modal-open');
		}
	}

	/**
	 * 모달 내 탭 전환 (Lazy Loading 적용)
	 * @param {Event} event - 클릭 이벤트 객체
	 */
	function switchTab(event) {
		const targetTabName = event.currentTarget.dataset.tab;
		
		modalTabButtons.forEach(btn => btn.classList.remove('active'));
		modalTabPanes.forEach(pane => pane.classList.remove('active'));

		event.currentTarget.classList.add('active');
		const targetPane = document.getElementById(`${targetTabName}-pane`);
		
		if (targetPane) {
			targetPane.classList.add('active');
			// 데이터가 로드되지 않은 탭인 경우에만 동적 옵션을 로드 (Lazy Loading)
			if (targetPane.dataset.loaded !== 'true') {
				loadOptionsForTab(targetTabName, targetPane);
				targetPane.dataset.loaded = 'true';
			}
		}
	}

	/**
	 * 특정 탭에 대한 동적 필터 옵션을 로드하는 라우터 함수
	 * @param {string} tabName - 'color', 'brand' 등 탭의 이름
	 * @param {HTMLElement} paneElement - 옵션이 렌더링될 탭 패널 요소
	 */
	function loadOptionsForTab(tabName, paneElement) {
		const container = paneElement.querySelector('.app-filter-options-grid');
		if (!container) return;

		// 각 탭에 맞는 API와 렌더링 함수를 호출
		switch (tabName) {
			case 'color':
				// fetchAndRenderOptions('/api/filter-options/colors', container, 'color', 'checkbox', renderColorOption);
				console.log('컬러 탭 데이터 로드 시뮬레이션 (현재는 HTML에 하드코딩됨)');
				break;
			case 'size':
				// fetchAndRenderOptions('/api/filter-options/sizes', container, 'size', 'checkbox', renderDefaultOption);
				console.log('사이즈 탭 데이터 로드 시뮬레이션 (현재는 HTML에 하드코딩됨)');
				break;
			case 'brand':
				// fetchAndRenderOptions('/api/filter-options/brands', container, 'brand', 'checkbox', renderBrandOption);
				console.log('브랜드 탭 데이터 로드 시뮬레이션 (현재는 HTML에 하드코딩됨)');
				break;
			// 'price', 'discount', 'type' 탭은 현재 HTML에 정적으로 정의되어 있어 로드할 필요 없음
		}
	}

	/**
	 * [범용] API에서 옵션 데이터를 가져와 렌더링하는 함수 (렌더링 로직 분리)
	 * @param {string} apiUrl - API URL
	 * @param {HTMLElement} container - 옵션을 삽입할 DOM 컨테이너
	 * @param {string} nameAttr - input의 name 속성
	 * @param {string} type - 'checkbox' 또는 'radio'
	 * @param {Function} renderFunc - 각 옵션 항목의 HTML을 생성하는 콜백 함수
	 */
	function fetchAndRenderOptions(apiUrl, container, nameAttr, type, renderFunc) {
		fetch(apiUrl)
			.then(response => response.ok ? response.json() : Promise.reject(`HTTP error! status: ${response.status}`))
			.then(options => {
				container.innerHTML = ''; // 기존 내용 비우기
				options.forEach(option => {
					container.insertAdjacentHTML('beforeend', renderFunc(option, nameAttr, type));
				});
			})
			.catch(error => {
				console.error(`${nameAttr} 옵션 로드 오류:`, error);
				container.innerHTML = `<p>${nameAttr} 옵션을 불러올 수 없습니다.</p>`;
			});
	}

	// --- 각 옵션 타입별 렌더링 함수 ---
	const renderColorOption = (option, name, type) => `
		<label class="app-color-option">
			<input type="${type}" name="${name}" value="${option.value}" hidden>
			<span class="color-swatch" style="background-color: ${option.hexCode}; ${option.hexCode === '#ffffff' ? 'border: 1px solid #e0e0e0;' : ''}"></span>
			<span>${option.name}</span>
		</label>`;

	const renderBrandOption = (option, name, type) => `
		<label class="app-filter-option">
			<input type="${type}" name="${name}" value="${option.value}">
			<span class="brand-name">${option.name}<small>${option.engName || ''}</small></span>
		</label>`;

	const renderDefaultOption = (option, name, type) => `
		<label class="app-filter-option">
			<input type="${type}" name="${name}" value="${option.value}">
			<span>${option.name}</span>
		</label>`;


	/**
	 * 모달에서 선택된 모든 필터 값을 수집하는 함수
	 * @returns {Object} 필터 객체
	 */
	function collectAllFilters() {
		const filters = {};
		const form = document.querySelector('.app-filter-modal-content'); // 모달 전체를 form처럼 사용

		// 체크박스/라디오 버튼 값 수집 (일반화된 방식)
		const checkedInputs = form.querySelectorAll('input:checked');
		checkedInputs.forEach(input => {
			const name = input.name;
			if (!name) return;

			// 가격 범위는 별도 처리
			if (name === 'priceRange') return;

			// 배열로 값 저장 (checkbox의 경우)
			if (input.type === 'checkbox') {
				if (!filters[name]) filters[name] = [];
				filters[name].push(input.value);
			} else { // 단일 값 저장 (radio의 경우)
				filters[name] = input.value;
			}
		});

		// 가격 필터 특별 처리
		const selectedPriceRadio = form.querySelector('input[name="priceRange"]:checked');
		if (selectedPriceRadio) {
			if (selectedPriceRadio.value === 'direct') {
				const minPrice = parseInt(priceInputs[0].value.replace(/,/g, ''), 10);
				const maxPrice = parseInt(priceInputs[1].value.replace(/,/g, ''), 10);
				if (!isNaN(minPrice)) filters.minPrice = minPrice;
				if (!isNaN(maxPrice)) filters.maxPrice = maxPrice;
			} else if (selectedPriceRadio.value !== 'all') {
				filters.priceRange = selectedPriceRadio.value;
			}
		}

		console.log("수집된 필터:", filters);
		return filters;
	}

	/**
	 * 수집된 필터로 상품 목록을 로드하는 함수 (페이지 이동 방식)
	 * @param {Object} filters - 필터 객체
	 * @param {string} [sortByParam] - 정렬 기준
	 */
	function applyFiltersAndLoadProducts(filters, sortByParam = null) {
		const queryParams = new URLSearchParams(window.location.search);
		
		// 기존 필터 파라미터 초기화 (카테고리, 정렬 제외)
        const keysToRemove = ['color', 'priceRange', 'minPrice', 'maxPrice', 'discountRate', 'size', 'brand', 'includeSoldOut'];
        keysToRemove.forEach(key => queryParams.delete(key));

		// 새 필터 객체를 URL 쿼리 파라미터로 변환
		for (const key in filters) {
			if (Array.isArray(filters[key])) {
				filters[key].forEach(value => queryParams.append(key, value));
			} else {
				queryParams.set(key, filters[key]);
			}
		}

		// 정렬 기준 설정
		const currentSortBy = sortByParam || queryParams.get('sortBy') || 'new';
		queryParams.set('sortBy', currentSortBy);

		// 페이지 이동
		window.location.href = `/customer/products/list?${queryParams.toString()}`;
	}

	/**
	 * 가격 필터에서 라디오 버튼 선택에 따라 직접 입력 필드를 토글하는 함수
	 */
	function handlePriceInputToggle() {
		if (!pricePane || !priceDirectInputWrapper) return;
		
		const selectedPriceRadio = pricePane.querySelector('input[name="priceRange"]:checked');
		if (selectedPriceRadio && selectedPriceRadio.value === 'direct') {
			priceDirectInputWrapper.style.display = 'flex';
		} else {
			priceDirectInputWrapper.style.display = 'none';
		}
	}


	// ===============================================
	// 3. 이벤트 리스너 등록 (Event Listeners)
	// ===============================================

	// 모달 열기/닫기
	modalTriggers.forEach(trigger => trigger.addEventListener('click', openModal));
	if (modalCloseButton) modalCloseButton.addEventListener('click', closeModal);
	if (modalOverlay) modalOverlay.addEventListener('click', (e) => e.target === modalOverlay && closeModal());
	document.addEventListener('keydown', (e) => e.key === 'Escape' && modalOverlay.classList.contains('show') && closeModal());

	// 모달 탭 전환
	modalTabButtons.forEach(button => button.addEventListener('click', switchTab));

	// 가격 필터 라디오 버튼 변경 감지
	if (pricePane) {
		pricePane.addEventListener('change', (e) => {
			if (e.target.name === 'priceRange') {
				handlePriceInputToggle();
			}
		});
	}

	// '상품보기' 버튼 클릭
	if (viewProductsButton) {
		viewProductsButton.addEventListener('click', () => {
			const filters = collectAllFilters();
			applyFiltersAndLoadProducts(filters);
		});
	}

	// 정렬 드롭다운
	if (sortDropdownButton && sortDropdownContainer) {
		sortDropdownButton.addEventListener('click', (e) => {
			sortDropdownContainer.classList.toggle('active');
			e.stopPropagation();
		});

		document.addEventListener('click', (e) => {
			if (!sortDropdownContainer.contains(e.target)) {
				sortDropdownContainer.classList.remove('active');
			}
		});

		sortOptionLinks.forEach(link => {
			link.addEventListener('click', (e) => {
				e.preventDefault();
				const sortBy = e.currentTarget.dataset.sortBy;
				sortDropdownButton.textContent = e.currentTarget.textContent;
				sortDropdownContainer.classList.remove('active');
				applyFiltersAndLoadProducts(collectAllFilters(), sortBy);
			});
		});
	}
	
	// 페이지 로드 시 정렬 버튼 텍스트 초기화
	function initializeSortButtonText() {
		if (!sortDropdownButton) return;
		const initialSortBy = new URLSearchParams(window.location.search).get('sortBy');
		let buttonText = '신상품순'; // 기본값
		if (initialSortBy) {
			const activeLink = document.querySelector(`.sort-option-link[data-sort-by="${initialSortBy}"]`);
			if (activeLink) {
				buttonText = activeLink.textContent;
			}
		}
		sortDropdownButton.textContent = buttonText;
	}


	// 드래그 스크롤 기능 (기존과 동일)
	if (slider) {
		let isDown = false, startX, scrollLeft, isDragging = false;
		slider.addEventListener('mousedown', (e) => {
			isDown = true; isDragging = false;
			slider.classList.add('active-drag');
			startX = e.pageX - slider.offsetLeft;
			scrollLeft = slider.scrollLeft;
		});
		slider.addEventListener('mouseleave', () => { isDown = false; slider.classList.remove('active-drag'); });
		slider.addEventListener('mouseup', () => {
			isDown = false; slider.classList.remove('active-drag');
			setTimeout(() => { slider.dataset.isDragging = 'false'; }, 0);
		});
		slider.addEventListener('mousemove', (e) => {
			if (!isDown) return;
			e.preventDefault();
			isDragging = true; slider.dataset.isDragging = 'true';
			const x = e.pageX - slider.offsetLeft;
			const walk = (x - startX) * 2;
			slider.scrollLeft = scrollLeft - walk;
		});
		slider.addEventListener('click', (e) => {
			if (slider.dataset.isDragging === 'true') {
				e.preventDefault();
			}
		}, true); // Use capture phase to prevent click
	}

	// ===============================================
	// 4. 초기화 함수 호출 (Initialization)
	// ===============================================
	initializeSortButtonText();
	handlePriceInputToggle(); // 페이지 로드 시 가격 직접 입력 필드 상태 확인

});