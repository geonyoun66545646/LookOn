$(document).ready(function() {
    // --- 1. 전역 변수 및 요소 선택 ---
    const colorSelect = $('.details-row-color .product-option-select');
    const sizeSelect = $('.details-row-size .product-option-select');
    const totalPriceSpan = $('#totalPrice');
    const selectionSummary = $('#selectionSummary');
    const selectedOptionsContainer = $('#selectedOptionsContainer');
    const selectedOptionCurrentPriceSpan = $('#selectedOptionCurrentPrice'); 
    let selectedOptions = []; // 선택된 옵션 목록 (배열)

    // 이미지 갤러리 관련 요소
    const mainImage = document.getElementById('product-detail-zoom');
    const galleryItems = document.querySelectorAll('.product-detail-gallery-item');
    const imageCounter = document.querySelector('.product-detail-image-counter span'); 
    const thumbnailGallery = document.querySelector('.product-detail-image-gallery'); // ID -> 클래스로 변경
    const thumbUpButton = document.querySelector('.product-detail-thumbnail-nav-btn.up'); // 클래스 세분화
    const thumbDownButton = document.querySelector('.product-detail-thumbnail-nav-btn.down'); // 클래스 세분화

    // --- 2. 초기화 및 헬퍼 함수 ---

    /**
     * 숫자를 통화 형식(예: 10,000원)으로 변환합니다.
     * @param {number} price - 가격
     * @returns {string} 포맷팅된 가격 문자열
     */
    function formatPrice(price) {
        return price.toLocaleString('ko-KR') + '원';
    }

    /**
     * 페이지에서 상품의 기본 가격을 가져옵니다.
     * @returns {number} 기본 가격
     */
    function getBaseProductPrice() {
        const priceText = $('.final-price-wrapper .final-price').text() || '0';
        return parseInt(priceText.replace(/[^0-9]/g, ''), 10) || 0;
    }

    /**
     * 선택된 옵션들의 총 가격을 계산하고 화면에 업데이트합니다.
     */
    function updateTotalPrice() {
        const basePrice = getBaseProductPrice();
        let total = selectedOptions.reduce((sum, option) => sum + (basePrice * option.quantity), 0);
        
        totalPriceSpan.text(formatPrice(total));
    }

    /**
     * 선택된 옵션 목록을 화면에 다시 그립니다.
     */
    function updateOptionsDisplay() {
        selectedOptionsContainer.empty();
        const basePrice = getBaseProductPrice();

        if (selectedOptions.length > 0) {
            selectionSummary.show();
            
            selectedOptions.forEach((option, index) => {
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

    /**
     * 옵션 선택 시 처리 로직입니다.
     */
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

        const isExisting = selectedOptions.some(opt => opt.color === colorText && opt.size === sizeText);
        
        if (isExisting) {
            alert('이미 추가된 옵션입니다.');
        } else {
            selectedOptions.push({
                color: colorText,
                size: sizeText,
                quantity: 1
            });
            updateOptionsDisplay();
        }
        
        // 옵션 선택 후 드롭다운 초기화
        colorSelect.val('');
        sizeSelect.val('');
        selectedOptionCurrentPriceSpan.text('옵션을 모두 선택해주세요.');
    }

    // --- 3. 이벤트 리스너 등록 ---
    
    // 이미지 갤러리 기능 초기화
    if (mainImage && galleryItems.length > 0) {
        const totalImages = galleryItems.length;

        const updateImageCounter = (currentIndex) => {
            if (imageCounter) {
                const counterContainer = imageCounter.closest('.product-detail-image-counter');
                if (counterContainer) {
                    counterContainer.innerHTML = `${currentIndex + 1}/${totalImages} <i class="icon-arrows"></i>`;
                }
            }
        };

        galleryItems.forEach((item, index) => {
            item.addEventListener('click', function(e) {
                e.preventDefault();

                // 활성 클래스 관리
                galleryItems.forEach(thumb => thumb.classList.remove('active'));
                this.classList.add('active');

                // 메인 이미지 변경
                const newImageSrc = this.getAttribute('data-image');
                mainImage.src = newImageSrc;

                // 카운터 업데이트
                updateImageCounter(index);
            });
        });

        // 초기 카운터 설정
        updateImageCounter(0);
    }

    // 썸네일 스크롤 버튼 기능
    if (thumbnailGallery && thumbUpButton && thumbDownButton) {
        const scrollAmount = 100; // 한 번에 스크롤할 양
        thumbUpButton.addEventListener('click', () => {
            thumbnailGallery.scrollBy({ top: -scrollAmount, behavior: 'smooth' });
        });
        thumbDownButton.addEventListener('click', () => {
            thumbnailGallery.scrollBy({ top: scrollAmount, behavior: 'smooth' });
        });
    }
    
    // 옵션 선택 이벤트
    $('.product-option-select').on('change', handleOptionSelection);

    // 수량 변경 및 삭제 버튼 이벤트 (이벤트 위임)
    selectedOptionsContainer.on('click', '.qty-control-button, .remove-option-button', function() {
        const $target = $(this);
        const $item = $target.closest('.selected-option-item-custom');
        if ($item.length === 0) return;

        const index = $item.data('index');
        let option = selectedOptions[index];

        if ($target.hasClass('plus') && option.quantity < 10) {
            option.quantity++;
        } else if ($target.hasClass('minus') && option.quantity > 1) {
            option.quantity--;
        } else if ($target.hasClass('remove-option-button')) {
            selectedOptions.splice(index, 1);
        }
        
        updateOptionsDisplay();
    });
    
    // 장바구니 버튼 클릭 이벤트
    $('.btn-cart').on('click', function(e) {
        e.preventDefault();
        
        if (selectedOptions.length === 0) {
            alert('최소 하나 이상의 옵션을 추가해주세요.');
            return;
        }

        const productName = $('.product-details .product-title').text().trim();
        const productImageSrc = mainImage ? mainImage.getAttribute('src') : ''; 
        let cartData = JSON.parse(sessionStorage.getItem('cart_data')) || { products: [] };

        selectedOptions.forEach(option => {
            const uniqueItemId = `${productName}_${option.color}_${option.size}`;
            const existingItemInCart = cartData.products.find(p => p.id === uniqueItemId);

            if (existingItemInCart) {
                existingItemInCart.quantity += option.quantity;
            } else {
                cartData.products.push({
                    id: uniqueItemId, 
                    name: `${productName} (${option.color} / ${option.size})`,
                    price: getBaseProductPrice(),
                    quantity: option.quantity,
                    image: productImageSrc 
                });
            }
        });

        sessionStorage.setItem('cart_data', JSON.stringify(cartData));
        selectedOptions = []; // 선택 옵션 초기화
        updateOptionsDisplay(); // 화면 갱신
        
        if (confirm('장바구니에 상품이 추가되었습니다. 장바구니로 이동하시겠습니까?')) {
            window.location.href = '/cart'; // 장바구니 페이지로 이동
        }
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


    console.log("페이지 로드 시 모든 스크립트 초기화 완료.");
});