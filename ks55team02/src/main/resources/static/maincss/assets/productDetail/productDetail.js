$(document).ready(function() {
    console.log("DOM이 로드되었습니다. 스크립트 실행 시작.");

    // --- 1. 전역 변수 및 요소 선택 ---
    const colorSelect = $('.details-row-color .product-option-select');
    const sizeSelect = $('.details-row-size .product-option-select');
    const totalPriceSpan = $('#totalPrice');
    const selectionSummary = $('#selectionSummary');
    const selectedOptionsContainer = $('#selectedOptionsContainer');
    const selectedOptionCurrentPriceSpan = $('#selectedOptionCurrentPrice'); 
    let selectedOptions = []; // 선택된 옵션 목록 (배열)

    // --- 2. 헬퍼 함수 정의 ---

    // 가격을 '1,000원' 형태로 포맷팅하는 함수
    function formatPrice(price) {
        return price.toLocaleString('ko-KR') + '원';
    }

    // 상품의 실제 최종 가격을 화면에서 읽어오는 함수
    function getBaseProductPrice() {
        const priceText = $('.final-price-wrapper .final-price').text() || $('.product-price .new-price').text();
        return parseInt(priceText.replace(/[^0-9]/g, '')) || 0;
    }

    // 화면에 표시된 옵션 목록을 기반으로 총액을 계산하고 업데이트하는 함수
    function updateTotalPrice() {
        let total = 0;
        const basePrice = getBaseProductPrice();
        
        $('.selected-option-item-custom').each(function() {
            const quantity = parseInt($(this).find('.option-qty-input-custom').val(), 10);
            total += basePrice * quantity;
        });
        
        totalPriceSpan.text(formatPrice(total));
        console.log("총 상품금액 업데이트:", formatPrice(total));
    }

    // 선택된 옵션을 화면에 표시하는 함수
    function updateOptionsDisplay() {
        selectedOptionsContainer.empty();
        const basePrice = getBaseProductPrice();

        if (selectedOptions.length > 0) {
            selectionSummary.show();
            
            selectedOptions.forEach((option, index) => {
                const optionPrice = basePrice * option.quantity;
                const optionElement = $(`
                    <div class="selected-option-item-custom" data-index="${index}">
                        <div class="option-name-display-custom">
                            <span>${option.color} / ${option.size}</span>
                            <span class="option-item-price">${formatPrice(basePrice)}</span>
                        </div>
                        <div class="option-controls-custom">
                            <button class="qty-control-button minus" type="button">-</button>
                            <input type="text" class="option-qty-input-custom" value="${option.quantity}" readonly>
                            <button class="qty-control-button plus" type="button">+</button>
                            <button class="remove-option-button" type="button">×</button>
                        </div>
                    </div>
                `);
                selectedOptionsContainer.append(optionElement);
            });
        } else {
            selectionSummary.hide();
        }
        updateTotalPrice();
    }

    // 옵션이 모두 선택되었을 때, 선택 목록에 추가하는 함수
    function handleOptionSelection() {
        const selectedColorValue = colorSelect.val();
        const selectedSizeValue = sizeSelect.val();

        if (!selectedColorValue || !selectedSizeValue) {
            selectedOptionCurrentPriceSpan.text('옵션을 모두 선택해주세요.');
            return;
        }
        
        selectedOptionCurrentPriceSpan.text(formatPrice(getBaseProductPrice()));

        const colorText = colorSelect.find('option:selected').text();
        const sizeText = sizeSelect.find('option:selected').text();

		// [디버깅 로그 1] 'productDataOptions' 변수 자체를 확인합니다.
		console.log("--- handleOptionSelection 함수 실행 ---");
		console.log("현재 productDataOptions 변수:", productDataOptions);

		if (!productDataOptions || productDataOptions.length === 0) {
		    console.error("오류 원인: 'productDataOptions' 변수가 비어있거나 null입니다. 서버가 옵션 데이터를 전달하지 않았을 가능성이 높습니다.");
		    alert("상품 옵션 정보를 불러올 수 없습니다. 페이지를 새로고침해주세요.");
		    return;
		}

		const colorOptionGroup = productDataOptions.find(opt => opt.optNm === '색상');

		// [디버깅 로그 2] '색상' 그룹을 찾았는지, 그 안에 'optNo'가 있는지 확인
		if (!colorOptionGroup || !colorOptionGroup.optNo) {
		    console.error("오류 원인: '색상' 옵션 그룹을 찾지 못했거나, 찾은 그룹 안에 'optNo' 프로퍼티가 없습니다.");
		    console.log("찾으려고 시도한 '색상' 그룹 객체:", colorOptionGroup);
		    alert("상품의 대표 옵션 정보(색상)가 올바르지 않습니다. 관리자에게 문의하세요.");
		    return;
		}

		const representativeOptNo = colorOptionGroup.optNo;

		// [디버깅 로그 3] 최종적으로 찾은 대표 옵션 번호 확인
		console.log("성공적으로 찾은 대표 옵션 번호 (optNo):", representativeOptNo);
		console.log("------------------------------------");

		const isExisting = selectedOptions.some(opt => opt.color === colorText && opt.size === sizeText);
		if (isExisting) {
		    alert('이미 추가된 옵션입니다.');
		} else {
		    selectedOptions.push({
		        color: colorText,
		        size: sizeText,
		        quantity: 1,
				optNo: representativeOptNo // 대표 옵션 '색상'의 실제 opt_no를 저장
		    });
		    updateOptionsDisplay();
		}

        
        colorSelect.val('');
        sizeSelect.val('');
        selectedOptionCurrentPriceSpan.text('옵션을 모두 선택해주세요.');
    }


    // --- 3. 이벤트 리스너 등록 ---

    // 컬러 또는 사이즈 select 박스 값이 변경될 때마다 실행
    $('.product-option-select').on('change', handleOptionSelection);

    // 수량 변경 및 삭제 버튼 이벤트 (이벤트 위임)
    selectedOptionsContainer.on('click', function(e) {
        const $target = $(e.target);
        const $item = $target.closest('.selected-option-item-custom');
        if ($item.length === 0) return;

        const index = $item.data('index');
        let option = selectedOptions[index];

        if ($target.hasClass('plus')) {
            if (option.quantity < 10) option.quantity++;
        } 
        else if ($target.hasClass('minus')) {
            if (option.quantity > 1) option.quantity--;
        } 
        else if ($target.hasClass('remove-option-button')) {
            selectedOptions.splice(index, 1);
        }
        
        updateOptionsDisplay();
    });
    
    // ===================== 장바구니 버튼 클릭 이벤트 2025.07.15 gy =====================

	$('#addToCartBtn').on('click', function() {
	    if (!currentProductGdsNo || !currentProductStoreId) {
		    Swal.fire({
		        icon: 'error',
		        title: '오류',
		        text: '상품 정보가 불완전합니다. 페이지를 새로고침해주세요.',
		        confirmButtonText: '확인'
		    });
		    return;
		}

	    if (!isLoggedIn) {
	        Swal.fire({
	            icon: 'warning',
	            title: '로그인 필요',
	            text: '장바구니에 상품을 담으려면 로그인이 필요합니다. 로그인 페이지로 이동하시겠습니까?',
	            showCancelButton: true,
	            confirmButtonText: '로그인',
	            cancelButtonText: '취소'
	        }).then((result) => {
	            if (result.isConfirmed) {
	                window.location.href = '/login';
	            }
	        });
	        return;
	    }

	    if (selectedOptions.length === 0) {
	        Swal.fire({
	            icon: 'warning',
	            title: '옵션 선택',
	            text: '장바구니에 담으려면 최소 하나 이상의 옵션을 선택해주세요.',
	            confirmButtonText: '확인'
	        });
	        return;
	    }

	    const $addToCartBtn = $('#addToCartBtn');
	    $addToCartBtn.prop('disabled', true).text('추가 중...');

		const payload = {
		    gdsNo: currentProductGdsNo,
		    storeId: currentProductStoreId,
		    selectedOptions: selectedOptions.map(option => ({
		        optNo: option.optNo,
		        color: option.color,
		        size: option.size,
		        quantity: option.quantity
		    }))
		};

	    console.log("서버로 전송할 최종 Payload:", JSON.stringify(payload, null, 2));

		fetch('/api/cart/add', {
		    method: 'POST',
		    headers: {
		        'Content-Type': 'application/json'
		    },
		    body: JSON.stringify(payload)
		})
		.then(response => {
		    if (!response.ok) {
		        return response.text().then(text => { throw new Error(text || '장바구니 추가 중 알 수 없는 오류'); });
		    }
		    return response.text();
		})
		.then(() => {
		    Swal.fire({
		        icon: 'success',
		        title: '장바구니에 추가되었습니다!',
		        showConfirmButton: true,
		        confirmButtonText: '장바구니로 이동',
		        cancelButtonText: '계속 쇼핑하기',
		        showCancelButton: true
		    }).then((result) => {
		        if (result.isConfirmed) {
		            window.location.href = '/cart';
		        }
		    });
		})
		.catch(error => {
		    console.error('장바구니 추가 중 오류:', error);
		    Swal.fire({
		        icon: 'error',
		        title: '장바구니 추가 실패',
		        text: error.message,
		        confirmButtonText: '확인'
		    });
		})
		.finally(() => {
		    $addToCartBtn.prop('disabled', false).text('장바구니');
		});
	});
	
	/* ========================== 구매하기 버튼 2025.07.11 gy ========================== */
	
	// 구매하기 버튼 클릭 이벤트 수정 (confirm alert 추가)
	$('.btn-buy-now').on('click', function(e) {
	    e.preventDefault(); // 기본 링크 동작 방지

	    const productName = $('.product-details .product-title').text().trim();
	    const productImageSrc = $('.product-main-image img').attr('src'); 
	    
	    // productDetail.html에서 선언된 전역 변수 currentProductGdsNo 사용을 가정
	    const productGdsNo = typeof currentProductGdsNo !== 'undefined' ? currentProductGdsNo : null; 

	    if (selectedOptions.length === 0) {
	        alert('구매하시려면 최소 하나 이상의 옵션을 선택해주세요.');
	        return;
	    }

	    // ⭐ 바로 구매하시겠습니까? confirm alert 추가 ⭐
	    if (confirm('선택하신 상품을 바로 구매하시겠습니까?')) {
	        let buyNowData = [];

	        selectedOptions.forEach(option => {
	            const uniqueItemId = `${productName}_${option.color}_${option.size}`;
	            const basePrice = getBaseProductPrice(); 
	            buyNowData.push({
	                id: uniqueItemId, 
	                gdsNo: productGdsNo, // 상품의 실제 고유 번호 (백엔드 처리에 필요)
	                name: `${productName} ${option.color} ${option.size}`,
	                price: basePrice,
	                quantity: option.quantity,
	                image: productImageSrc,
	                color: option.color, // 색상 옵션 값
	                size: option.size // 사이즈 옵션 값
	            });
	        });

	        // sessionStorage에 구매할 상품 데이터 저장
	        sessionStorage.setItem('buy_now_data', JSON.stringify(buyNowData));
	        
	        // checkout.html 페이지로 이동
	        window.location.href = '/checkout';
	    } else {
	        // 사용자가 '취소'를 누른 경우
	        console.log("구매하기 취소됨.");
	    }
	});


    console.log("페이지 로드 시 초기화 완료.");
});