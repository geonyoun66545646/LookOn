// DOM이 완전히 로드된 후에 스크립트가 실행되도록 합니다.
document.addEventListener('DOMContentLoaded', function() {

	// ===============================================
	// 1. 요소 선택 (DOM Elements)
	// ===============================================

	// 모달 관련 요소
	const modalTriggers = document.querySelectorAll('.app-modal-trigger');
	const modalOverlay = document.querySelector('.app-filter-modal-overlay');
	const modalCloseButton = document.querySelector('.app-modal-close-button');
	const modalTabButtons = document.querySelectorAll('.app-modal-tab-button');
	const modalTabPanes = document.querySelectorAll('.app-modal-tab-pane');
	const viewProductsButton = document.querySelector('.app-view-products-button');
	const resetFiltersButton = document.querySelector('.app-reset-filters-button');
	const selectedFiltersContainer = document.querySelector('.selected-filters-container');

	// 가격 직접 입력 관련 요소
	const pricePane = document.getElementById('price-pane');
	const priceDirectInputWrapper = pricePane ? pricePane.querySelector('.app-price-direct-input') : null;
	const minPriceInput = priceDirectInputWrapper ? priceDirectInputWrapper.querySelector('input[name="minPrice"]') : null;
	const maxPriceInput = priceDirectInputWrapper ? priceDirectInputWrapper.querySelector('input[name="maxPrice"]') : null;
	const applyPriceButton = priceDirectInputWrapper ? priceDirectInputWrapper.querySelector('.app-apply-price-button') : null;

	// 정렬 드롭다운 관련 요소
	const sortDropdownButton = document.querySelector('.sort-dropdown-button');
	const sortDropdownContainer = document.querySelector('.sort-dropdown-container');
	const sortOptionLinks = document.querySelectorAll('.sort-dropdown-menu .sort-option-link');

	// 드래그 스크롤 대상 요소
	const slider = document.querySelector('.main-category-nav');

	// 초기 필터 데이터 (HTML에서 가져옴)
	const initialFilterDataElement = document.getElementById('initialFilterData');
	const initialCategoryId = initialFilterDataElement ? initialFilterDataElement.dataset.currentCategoryId : '';
	const initialCategoryName = initialFilterDataElement ? initialFilterDataElement.dataset.currentCategoryName : '';
	const initialParentCategoryId = initialFilterDataElement ? initialFilterDataElement.dataset.parentCategoryId : '';
	const initialParentCategoryName = initialFilterDataElement ? initialFilterDataElement.dataset.parentCategoryName : '';


	// ===============================================
	// 2. 함수 정의 (Function Definitions)
	// ===============================================

	/**
		 * 키워드로 브랜드를 비동기 검색하고, 결과를 브랜드 목록 컨테이너에 렌더링합니다.
		 * @param {string} keyword - 검색할 키워드
		 */
	function searchBrandsAjax(keyword) {
		$.ajax({
			url: '/api/brands/search', // Controller에 만든 API 엔드포인트
			type: 'GET',
			data: { keyword: keyword },
			success: function(brands) {
				const $brandListContainer = $('#brandListContainer');
				$brandListContainer.empty(); // 기존 목록을 모두 비움

				if (brands && brands.length > 0) {
					brands.forEach(brand => {
						// 현재 URL 파라미터를 읽어와서, 이미 선택된 브랜드인지 확인
						const queryParams = new URLSearchParams(window.location.search);
						const selectedBrands = queryParams.getAll('brand');
						const isChecked = selectedBrands.includes(brand.storeId);

						// 새로운 브랜드 라벨 HTML 생성
						const newLabel = `
	                            <label class="app-filter-option">
	                                <input type="checkbox" name="brand" value="${brand.storeId}" ${isChecked ? 'checked' : ''}>
	                                <span class="brand-name">${brand.storeConm}</span>
	                            </label>
	                        `;
						$brandListContainer.append(newLabel);
					});
				} else {
					$brandListContainer.append('<p class="text-muted text-center">검색 결과가 없습니다.</p>');
				}
			},
			error: function() {
				// 에러 발생 시 처리
				const $brandListContainer = $('#brandListContainer');
				$brandListContainer.empty();
				$brandListContainer.append('<p class="text-danger text-center">브랜드 목록을 불러오는 데 실패했습니다.</p>');
			}
		});
	}

	/**
	 * 필터 모달 열기
	 */
	function openModal(targetTabName) {
		if (!modalOverlay) return;

		modalOverlay.classList.add('show');
		document.body.classList.add('app-modal-open');

		// 모든 탭 비활성화
		modalTabButtons.forEach(btn => btn.classList.remove('active'));
		modalTabPanes.forEach(pane => pane.classList.remove('active'));

		// 지정된 탭 또는 기본 탭(첫 번째)을 활성화
		const tabNameToOpen = targetTabName || (modalTabButtons[0] ? modalTabButtons[0].dataset.tab : null);

		if (tabNameToOpen) {
			const targetButton = document.querySelector(`.app-modal-tab-button[data-tab="${tabNameToOpen}"]`);
			const targetPane = document.getElementById(`${tabNameToOpen}-pane`);
			if (targetButton) targetButton.classList.add('active');
			if (targetPane) targetPane.classList.add('active');
		}

		updateSelectedFiltersDisplay();
	}

	/**
	 * 필터 모달 닫기 함수
	 */
	function closeModal() {
		if (modalOverlay) {
			modalOverlay.classList.remove('show');
			document.body.classList.remove('app-modal-open');
		}
	}

	/**
	 * 모달 내 탭 전환 함수
	 */
	function switchTab(event) {
		const targetTabName = event.currentTarget.dataset.tab;
		modalTabButtons.forEach(btn => btn.classList.remove('active'));
		modalTabPanes.forEach(pane => pane.classList.remove('active'));
		event.currentTarget.classList.add('active');
		const targetPane = document.getElementById(`${targetTabName}-pane`);
		if (targetPane) targetPane.classList.add('active');
	}

	/**
	 * 모달에서 선택된 모든 필터 값을 수집하는 함수
	 * @returns {Object} 필터 객체
	 */
	function collectAllFilters() {
		const filters = {};
		const form = document.querySelector('.app-filter-modal-content');

		// 체크박스/라디오 버튼 값 수집
		form.querySelectorAll('input:checked').forEach(input => {
			const name = input.name;
			if (!name) return;

			if (name === 'priceRange' || name === 'discountRate') {
				// 'all' 값은 필터에 포함하지 않음
				if (input.value !== 'all') {
					filters[name] = input.value;
				}
			} else if (name === 'includeSoldOut') {
				// 품절포함 체크박스는 true일 때만 필터에 포함
				if (input.checked) {
					filters[name] = input.value;
				}
			} else {
				// 다른 체크박스 (color, size, brand)
				if (!filters[name]) filters[name] = [];
				filters[name].push(input.value);
			}
		});

		// 가격 직접 입력 필터 처리
		const selectedPriceRadio = form.querySelector('input[name="priceRange"]:checked');
		if (selectedPriceRadio && selectedPriceRadio.value === 'direct') {
			// 숫자 형식 검증 및 콤마 제거
			const minPriceVal = minPriceInput.value.replace(/,/g, '');
			const maxPriceVal = maxPriceInput.value.replace(/,/g, '');

			const minPrice = minPriceVal ? parseInt(minPriceVal, 10) : '';
			const maxPrice = maxPriceVal ? parseInt(maxPriceVal, 10) : '';

			// 유효한 숫자일 경우에만 추가
			if (!isNaN(minPrice) && minPrice !== '') filters.minPrice = minPrice;
			if (!isNaN(maxPrice) && maxPrice !== '') filters.maxPrice = maxPrice;

			// 'direct' 선택 시 priceRange는 전송하지 않음 (minPrice/maxPrice로 처리되므로)
			delete filters.priceRange;
		}

		// 성별 필터는 모달 내 input이 아닌 별도 버튼이므로 여기서 수집
		const genderButton = document.querySelector('.filter-button[data-filter-group="gender"].active');
		if (genderButton && genderButton.dataset.gender) {
			filters.gender = genderButton.dataset.gender;
		}


		console.log("수집된 필터:", filters);
		return filters;
	}

	/**
	 * 수집된 필터로 상품 목록을 로드하는 함수 (페이지 이동 방식)
	 * @param {Object} filters - 필터 객체
	 * @param {string} [sortByParam] - 정렬 기준 (선택 사항)
	 * @param {boolean} [resetFilters = false] - 필터를 완전히 초기화할 것인지 여부 (초기화 버튼 전용)
	 */
	function applyFiltersAndLoadProducts(filters, sortByParam = null, resetFilters = false) {
		const url = new URL(window.location.origin + window.location.pathname); // 현재 도메인과 경로 유지

		// 기존 쿼리 파라미터에서 categoryId와 sortBy만 유지하고 나머지는 삭제
		const currentCategoryId = initialFilterDataElement ? initialFilterDataElement.dataset.currentCategoryId : '';
		const currentSortBy = new URLSearchParams(window.location.search).get('sortBy') || 'new';

		if (!resetFilters) { // 필터 초기화가 아닌 경우에만 기존 카테고리와 정렬 유지
			if (currentCategoryId) {
				url.searchParams.set('categoryId', currentCategoryId);
			}
			url.searchParams.set('sortBy', sortByParam || currentSortBy);
		} else { // 필터 초기화인 경우, 카테고리와 정렬만 남기고 모두 지움
			if (currentCategoryId) {
				url.searchParams.set('categoryId', currentCategoryId);
			}
			url.searchParams.set('sortBy', 'new'); // 초기화 시 정렬은 '신상품순'으로
			filters = {}; // 필터 객체 초기화
		}

		// 새 필터 객체를 URL 쿼리 파라미터로 추가
		for (const key in filters) {
			if (Array.isArray(filters[key])) {
				filters[key].forEach(value => {
					if (value !== '' && value !== null) { // 빈 값은 추가하지 않음
						url.searchParams.append(key, value);
					}
				});
			} else {
				if (filters[key] !== '' && filters[key] !== null && filters[key] !== undefined) { // 빈 값, null, undefined는 추가하지 않음
					url.searchParams.set(key, filters[key]);
				}
			}
		}

		// 페이지 이동
		window.location.href = url.toString();
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
			// 'direct'가 아닐 때 직접입력 값 초기화
			if (minPriceInput) minPriceInput.value = '';
			if (maxPriceInput) maxPriceInput.value = '';
		}
	}

	/**
	 * 현재 선택된 필터들을 모달 푸터에 태그 형태로 표시합니다.
	 */
	function updateSelectedFiltersDisplay() {
		if (!selectedFiltersContainer) {
			console.warn("selected-filters-container 요소를 찾을 수 없습니다.");
			return;
		}

		selectedFiltersContainer.innerHTML = ''; // 기존 필터 태그 초기화

		// 현재 모달 내 필터 요소들의 현재 상태를 직접 수집
		const currentModalFilters = collectAllFilters();

		console.log("updateSelectedFiltersDisplay: 현재 모달 필터 상태:", currentModalFilters);


		// 2. 성별 필터
		const gender = currentModalFilters.gender;
		if (gender) {
			const genderText = gender === 'M' ? '남성' : '여성';
			appendFilterTag('성별', genderText, 'gender', gender);
		}

		// 3. 가격 필터
		if (currentModalFilters.priceRange && currentModalFilters.priceRange !== 'all') {
			let priceText = '';
			switch (currentModalFilters.priceRange) {
				case '0-50000': priceText = '5만원 이하'; break;
				case '50000-100000': priceText = '5만원 ~ 10만원'; break;
				case '100000-200000': priceText = '10만원 ~ 20만원'; break;
				case '200000-300000': priceText = '20만원 ~ 30만원'; break;
				case '300000-max': priceText = '30만원 이상'; break;
			}
			if (priceText) {
				appendFilterTag('가격', priceText, 'priceRange', currentModalFilters.priceRange);
			}
		} else if (currentModalFilters.minPrice || currentModalFilters.maxPrice) {
			let priceText = '';
			const min = currentModalFilters.minPrice ? Number(currentModalFilters.minPrice).toLocaleString() : '';
			const max = currentModalFilters.maxPrice ? Number(currentModalFilters.maxPrice).toLocaleString() : '';
			if (min && max) {
				priceText = `${min}원 ~ ${max}원`;
			} else if (min) {
				priceText = `${min}원 이상`;
			} else if (max) {
				priceText = `${max}원 이하`;
			}
			if (priceText) {
				appendFilterTag('가격', priceText, 'minMaxPrice', `${currentModalFilters.minPrice || ''}-${currentModalFilters.maxPrice || ''}`, true);
			}
		}


		// 4. 색상 필터
		const colors = currentModalFilters.color || [];
		colors.forEach(color => {
			appendFilterTag('색상', color, 'color', color);
		});

		// 5. 사이즈 필터
		const sizes = currentModalFilters.size || [];
		sizes.forEach(size => {
			appendFilterTag('사이즈', size, 'size', size);
		});

		// 6. 브랜드 필터
		const brands = currentModalFilters.brand || [];
		brands.forEach(brandId => {
			// TODO: 실제 브랜드 이름을 가져오는 로직 필요. 지금은 ID를 그대로 표시하거나,
			// HTML의 allBrands 데이터를 참조하여 이름 매핑 (data-brand-name 속성 활용 등)
			const brandNameElement = document.querySelector(`input[name="brand"][value="${brandId}"] + .brand-name`);
			const brandName = brandNameElement ? brandNameElement.textContent.split('<small>')[0].trim() : brandId;
			appendFilterTag('브랜드', brandName, 'brand', brandId);
		});

		// 7. 할인율 필터
		const discountRate = currentModalFilters.discountRate;
		if (discountRate && discountRate !== 'all') {
			let discountText = '';
			switch (discountRate) {
				case '10': discountText = '10% 이상'; break;
				case '30': discountText = '30% 이상'; break;
				case '50': discountText = '50% 이상'; break;
				case '70': discountText = '70% 이상'; break;
			}
			appendFilterTag('할인율', discountText, 'discountRate', discountRate);
		}

		// 8. 품절포함 필터
		const includeSoldOut = currentModalFilters.includeSoldOut === 'true';
		if (includeSoldOut) {
			appendFilterTag('판매상태', '품절포함', 'includeSoldOut', 'true');
		}
	}

	/**
	 * 필터 태그를 생성하여 selectedFiltersContainer에 추가합니다.
	 * @param {string} groupName - 필터 그룹의 표시 이름 (예: '색상', '사이즈')
	 * @param {string} valueName - 필터 값의 표시 이름 (예: '블랙', 'M', '나이키')
	 * @param {string} paramName - URL 쿼리 파라미터의 이름 (예: 'color', 'size', 'minMaxPrice')
	 * @param {string} paramValue - URL 쿼리 파라미터의 실제 값 (예: 'Black', 'M', '10000-50000')
	 * @param {boolean} isMinMaxPriceTag - 이 태그가 min/max 가격 범위 태그인지 여부
	 * @param {boolean} isCategoryTag - 이 태그가 카테고리 태그인지 여부 (제거 불가)
	 */
	function appendFilterTag(groupName, valueName, paramName, paramValue, isMinMaxPriceTag = false, isCategoryTag = false) {
		const tag = document.createElement('span');
		tag.classList.add('selected-filter-tag');
		tag.dataset.paramName = paramName;
		tag.dataset.paramValue = paramValue; // URL 파라미터의 실제 값

		tag.innerHTML = `${groupName}: ${valueName}`;
		if (!isCategoryTag) { // 카테고리 태그는 삭제 버튼 없음
			tag.innerHTML += ` <button type="button" class="remove-filter-btn">x</button>`;
		}
		selectedFiltersContainer.appendChild(tag);

		if (!isCategoryTag) {
			tag.querySelector('.remove-filter-btn').addEventListener('click', function() {
				removeFilter(paramName, paramValue, isMinMaxPriceTag);
			});
		}
	}

	/**
	 * 특정 필터를 제거하고 모달 내 UI를 업데이트합니다. (페이지 새로고침 없음)
	 * @param {string} paramName - 제거할 필터의 URL 파라미터 이름 (예: 'color', 'size')
	 * @param {string} paramValue - 제거할 필터의 URL 파라미터 값 (예: 'Black', 'M')
	 * @param {boolean} isMinMaxPriceTag - 가격 직접입력 필터 태그인지 여부
	 */
	function removeFilter(paramName, paramValue, isMinMaxPriceTag = false) {
		const form = document.querySelector('.app-filter-modal-content');

		if (isMinMaxPriceTag) { // 가격 직접입력 태그 제거
			// 모달 내 라디오 버튼 '직접입력' 해제, '전체' 선택
			const directRadio = form.querySelector('input[name="priceRange"][value="direct"]');
			if (directRadio) directRadio.checked = false;
			const allPriceRadio = form.querySelector('input[name="priceRange"][value="all"]');
			if (allPriceRadio) allPriceRadio.checked = true;
			// 직접입력 필드 값 초기화
			if (minPriceInput) minPriceInput.value = '';
			if (maxPriceInput) maxPriceInput.value = '';
		} else if (paramName === 'includeSoldOut') { // 품절포함 태그 제거
			const includeSoldOutCheckbox = form.querySelector('input[name="includeSoldOut"]');
			if (includeSoldOutCheckbox) {
				includeSoldOutCheckbox.checked = false;
			}
		} else if (paramName === 'priceRange') { // 가격 범위 태그 제거 (예: '5만원 이하')
			const allPriceRadio = form.querySelector('input[name="priceRange"][value="all"]');
			if (allPriceRadio) allPriceRadio.checked = true;
		} else if (paramName === 'discountRate') { // 할인율 태그 제거
			const allDiscountRadio = form.querySelector('input[name="discountRate"][value="all"]');
			if (allDiscountRadio) allDiscountRadio.checked = true;
		} else if (paramName === 'gender') { // 성별 태그 제거 (툴바 버튼)
			document.querySelectorAll('.filter-button[data-filter-group="gender"]').forEach(btn => {
				btn.classList.remove('active');
			});
		} else if (['color', 'size', 'brand'].includes(paramName)) { // 다중 선택 가능한 일반 필터 (color, size, brand)
			// 모달 내 해당 input의 체크 상태도 해제
			const inputToRemove = form.querySelector(`input[name="${paramName}"][value="${paramValue}"]`);
			if (inputToRemove) {
				inputToRemove.checked = false;
			}
		}

		// 가격 직접 입력 필드 표시 상태 다시 확인 (필터 제거 후)
		handlePriceInputToggle();
		// 필터 UI 상태가 변경되었으므로, 하단의 선택된 필터 태그 디스플레이를 업데이트
		updateSelectedFiltersDisplay();
	}


	/**
	 * 페이지 로드 시 URL 파라미터를 기반으로 필터 모달의 초기 상태를 설정하고 태그를 표시합니다.
	 */
	function initializeModalStateFromUrl() {
		const queryParams = new URLSearchParams(window.location.search);
		const form = document.querySelector('.app-filter-modal-content');

		// 모든 필터 UI 요소 초기화 (재로드 시 중복 체크 방지)
		form.querySelectorAll('input[type="checkbox"]').forEach(input => input.checked = false);
		form.querySelectorAll('input[type="radio"]').forEach(input => input.checked = false);
		form.querySelector('input[name="priceRange"][value="all"]').checked = true;
		form.querySelector('input[name="discountRate"][value="all"]').checked = true;
		if (minPriceInput) minPriceInput.value = '';
		if (maxPriceInput) maxPriceInput.value = '';
		handlePriceInputToggle(); // 가격 직접 입력 필드 숨김

		// 가격 범위 라디오 버튼 및 직접 입력 초기화
		const priceRange = queryParams.get('priceRange');
		const minPrice = queryParams.get('minPrice');
		const maxPrice = queryParams.get('maxPrice');

		if (priceRange) {
			const radio = form.querySelector(`input[name="priceRange"][value="${priceRange}"]`);
			if (radio) radio.checked = true;
		} else if (minPrice || maxPrice) {
			const directRadio = form.querySelector('input[name="priceRange"][value="direct"]');
			if (directRadio) directRadio.checked = true;
			if (minPriceInput) minPriceInput.value = minPrice;
			if (maxPriceInput) maxPriceInput.value = maxPrice;
		}
		handlePriceInputToggle(); // 가격 직접 입력 필드 상태 업데이트 (이전에 숨겼다가 다시 필요한 경우 표시)


		// 체크박스 필터 초기화 (color, size, brand)
		['color', 'size', 'brand'].forEach(paramName => {
			const values = queryParams.getAll(paramName); // URL 파라미터는 단수형으로 받을 것으로 가정
			values.forEach(value => {
				const input = form.querySelector(`input[name="${paramName}"][value="${value}"]`);
				if (input) input.checked = true;
			});
		});

		// 할인율 라디오 버튼 초기화
		const discountRate = queryParams.get('discountRate'); // 단일 선택이므로 get
		if (discountRate) {
			const radio = form.querySelector(`input[name="discountRate"][value="${discountRate}"]`);
			if (radio) radio.checked = true;
		} else {
			const allRadio = form.querySelector('input[name="discountRate"][value="all"]');
			if (allRadio) allRadio.checked = true;
		}

		// 상품유형 (품절포함) 체크박스 초기화
		const includeSoldOut = queryParams.get('includeSoldOut');
		const includeSoldOutCheckbox = form.querySelector('input[name="includeSoldOut"]');
		if (includeSoldOutCheckbox) {
			includeSoldOutCheckbox.checked = (includeSoldOut === 'true');
		}

		// gender 버튼 초기화 (툴바에 있는 버튼)
		const currentGender = queryParams.get('gender');
		document.querySelectorAll('.filter-button[data-filter-group="gender"]').forEach(btn => {
			if (btn.dataset.gender === currentGender) {
				btn.classList.add('active');
			} else {
				btn.classList.remove('active');
			}
		});

		// 필터 태그 디스플레이 업데이트 (초기 로드 시 한 번)
		updateSelectedFiltersDisplay();
	}


	/**
	 * 페이지 로드 시 정렬 버튼 텍스트 초기화
	 */
	function initializeSortButtonText() {
		if (!sortDropdownButton) return;
		const initialSortBy = new URLSearchParams(window.location.search).get('sortBy');
		let buttonText = '신상품순'; // 기본값 (HTML의 <otherwise>와 일치)
		if (initialSortBy) {
			// HTML의 th:text 로직과 동일하게 매핑
			switch (initialSortBy) {
				case 'new': buttonText = '신상품순'; break;
				case 'priceAsc': buttonText = '낮은가격순'; break;
				case 'priceDesc': buttonText = '높은가격순'; break;
				case 'discountDesc': buttonText = '할인률순'; break;
				case 'popularity': buttonText = '인기순(판매량)'; break;
			}
		}
		sortDropdownButton.textContent = buttonText;
	}


	// ===============================================
	// 3. 이벤트 리스너 등록 (Event Listeners)
	// ===============================================

	// --- 브랜드 검색 및 자동완성 로직 (AJAX 방식) ---
	let searchTimer; // 디바운싱을 위한 타이머 변수
	const $brandSearchInput = $('.app-brand-search-input');

	$brandSearchInput.on('keyup', function() {
		clearTimeout(searchTimer); // 이전 타이머를 취소
		const keyword = $(this).val();

		// 300ms 디바운싱: 사용자가 타이핑을 멈춘 후 0.3초가 지나면 검색 실행
		searchTimer = setTimeout(() => {
			searchBrandsAjax(keyword); // 위에서 만든 AJAX 함수 호출
		}, 300);
	});

	// 모달 열기/닫기
	// 모든 '.app-modal-trigger' 버튼에 대한 이벤트 리스너
	modalTriggers.forEach(trigger => {
		trigger.addEventListener('click', function() {
			// 클릭된 버튼의 data-modal-tab 속성 값을 가져옴
			const targetTab = this.dataset.modalTab;
			// 해당 탭을 지정하여 모달을 엶
			openModal(targetTab);
		});
	});

	// ⭐ 모달 닫기 버튼 (X 버튼) 클릭 시: 모달만 닫고, 필터 적용은 '상품보기' 버튼에 위임 ⭐
	if (modalCloseButton) {
		modalCloseButton.addEventListener('click', () => {
			closeModal();
			// 필터 적용 로직 제거. 모달 내 선택 상태는 유지됨.
		});
	}

	if (modalOverlay) modalOverlay.addEventListener('click', (e) => e.target === modalOverlay && closeModal());
	document.addEventListener('keydown', (e) => e.key === 'Escape' && modalOverlay.classList.contains('show') && closeModal());

	// 모달 탭 전환
	modalTabButtons.forEach(button => button.addEventListener('click', switchTab));

	// ⭐ 필터 옵션 변경 감지 (체크박스/라디오 버튼 및 가격 직접 입력 적용 버튼) ⭐
	// 모달 내용 전체에 이벤트 위임 (성능 최적화)
	const filterOptionsContainer = document.querySelector('.app-modal-body');
	if (filterOptionsContainer) {
		filterOptionsContainer.addEventListener('change', (e) => {
			const target = e.target;
			// 체크박스/라디오 버튼 변경 시 (가격 직접 입력 필드 제외)
			if (target.matches('input[type="checkbox"], input[type="radio"]') &&
				!['minPrice', 'maxPrice'].includes(target.name)) {
				updateSelectedFiltersDisplay(); // 모달 내 태그 업데이트
			}
			// 가격 라디오 버튼 변경 시
			if (target.name === 'priceRange') {
				handlePriceInputToggle(); // 직접입력 필드 토글
			}
		});
	}
	// ⭐ 가격 직접 입력 '적용' 버튼 클릭 시 ⭐
	if (applyPriceButton) {
		applyPriceButton.addEventListener('click', () => {
			updateSelectedFiltersDisplay(); // 가격 필터 태그 업데이트
		});
	}
	// ⭐ 가격 직접 입력 필드에서 엔터 키 입력 시 ⭐
	if (minPriceInput) {
		minPriceInput.addEventListener('keypress', (e) => {
			if (e.key === 'Enter') {
				e.preventDefault(); // 기본 엔터 동작 방지 (폼 제출 등)
				updateSelectedFiltersDisplay();
			}
		});
	}
	if (maxPriceInput) {
		maxPriceInput.addEventListener('keypress', (e) => {
			if (e.key === 'Enter') {
				e.preventDefault(); // 기본 엔터 동작 방지 (폼 제출 등)
				updateSelectedFiltersDisplay();
			}
		});
	}


	// ⭐ 성별 필터 버튼 클릭 시 ⭐
	document.querySelectorAll('.filter-button[data-filter-group="gender"]').forEach(button => {
		button.addEventListener('click', function() {
			const clickedGender = this.dataset.gender;
			const isActive = this.classList.contains('active');

			// 토글 로직: 이미 활성화된 버튼을 누르면 비활성화, 아니면 활성화
			document.querySelectorAll('.filter-button[data-filter-group="gender"]').forEach(btn => {
				btn.classList.remove('active');
			});
			if (!isActive) {
				this.classList.add('active');
			}
			// 성별 필터는 즉시 적용되므로, 모든 필터 수집 후 페이지 로드
			const filters = collectAllFilters();
			applyFiltersAndLoadProducts(filters);
		});
	});


	// '상품보기' 버튼 클릭
	if (viewProductsButton) {
		viewProductsButton.addEventListener('click', () => {
			const filters = collectAllFilters();
			applyFiltersAndLoadProducts(filters);
			closeModal(); // 상품보기 클릭 후 모달 닫기
		});
	}

	// ⭐ '필터 초기화' 버튼 클릭 ⭐
	if (resetFiltersButton) {
		resetFiltersButton.addEventListener('click', () => {
			// 모달 내 모든 필터 요소 초기화 (체크 해제, 값 비우기 등)
			document.querySelectorAll('.app-filter-modal-content input[type="checkbox"]').forEach(input => input.checked = false);
			document.querySelectorAll('.app-filter-modal-content input[type="radio"]').forEach(input => input.checked = false);
			document.querySelector('.app-filter-modal-content input[name="priceRange"][value="all"]').checked = true; // 가격 '전체'
			document.querySelector('.app-filter-modal-content input[name="discountRate"][value="all"]').checked = true; // 할인율 '전체'
			if (minPriceInput) minPriceInput.value = '';
			if (maxPriceInput) maxPriceInput.value = '';
			handlePriceInputToggle(); // 직접입력 숨김

			// 툴바 성별 필터도 초기화
			document.querySelectorAll('.filter-button[data-filter-group="gender"]').forEach(btn => {
				btn.classList.remove('active');
			});

			// 선택된 필터 태그 디스플레이 초기화
			selectedFiltersContainer.innerHTML = '';

			// 필터 초기화 후 상품 목록 로드 (카테고리/정렬만 유지하고 다른 필터는 모두 제거)
			applyFiltersAndLoadProducts({}, null, true); // 세 번째 인자를 true로 넘겨 초기화임을 알림
			closeModal(); // 모달 닫기
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

				// 기존 필터 상태를 유지하면서 정렬 기준만 변경하여 페이지 로드
				// (모달 내의 현재 선택 상태를 반영하기 위해 collectAllFilters 사용)
				const filters = collectAllFilters();
				applyFiltersAndLoadProducts(filters, sortBy);
			});
		});
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
	initializeSortButtonText(); // 정렬 버튼 텍스트 초기화
	initializeModalStateFromUrl(); // 페이지 로드 시 URL 파라미터를 읽어 필터 상태 및 태그 초기화

});