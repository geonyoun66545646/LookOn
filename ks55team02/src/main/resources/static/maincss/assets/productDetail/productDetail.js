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
	const mainImage = document.getElementById('product-detail-zoom');
    const galleryItems = document.querySelectorAll('.product-detail-gallery-item');
    const imageCounter = document.querySelector('.product-detail-image-counter span'); // For updating the counter
    const totalImages = galleryItems.length;

	
	// 이미지
	if (imageCounter) {
	        imageCounter.textContent = totalImages;
	    }

	    galleryItems.forEach((item, index) => {
	        item.addEventListener('click', function(e) {
	            e.preventDefault(); // Prevent default link behavior

	            // Remove 'active' class from all thumbnails
	            galleryItems.forEach(thumb => thumb.classList.remove('active'));

	            // Add 'active' class to the clicked thumbnail
	            this.classList.add('active');

	            // Update the main image src and data-zoom-image
	            const newImageSrc = this.getAttribute('data-image');
	            const newZoomImageSrc = this.getAttribute('data-zoom-image');

	            mainImage.src = newImageSrc;
	            mainImage.setAttribute('data-zoom-image', newZoomImageSrc);

	            // Update the image counter
	            if (imageCounter) {
	                const currentCountElement = document.querySelector('.product-detail-image-counter');
	                currentCountElement.innerHTML = `${index + 1}/${totalImages} <i class="icon-arrows"></i>`;
	            }

	            // (Optional) Reinitialize a zoom library here if you're using one like elevateZoom
	            // if (typeof $.fn.elevateZoom !== 'undefined') {
	            //     $(mainImage).elevateZoom({
	            //         zoomType: "inner",
	            //         cursor: "crosshair",
	            //         zoomWindowFadeIn: 500,
	            //         zoomWindowFadeOut: 500
	            //     });
	            // }
	        });
	    });

	    // Thumbnail scroll functionality
	    const thumbnailGallery = document.getElementById('product-detail-zoom-gallery');
	    const scrollAmount = 100; // Pixels to scroll per click

	    document.querySelector('.product-detail-thumbnail-up').addEventListener('click', function() {
	        thumbnailGallery.scrollBy({
	            top: -scrollAmount,
	            behavior: 'smooth'
	        });
	    });

	    document.querySelector('.product-detail-thumbnail-down').addEventListener('click', function() {
	        thumbnailGallery.scrollBy({
	            top: scrollAmount,
	            behavior: 'smooth'
	        });
	    });
	
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
    
    // 장바구니 버튼 클릭 이벤트
    $('.btn-cart').on('click', function(e) {
        e.preventDefault();
        const productName = $('.product-details .product-title').text().trim();
        const productImageSrc = $('.product-main-image img').attr('src'); 

        if (selectedOptions.length === 0) {
            alert('최소 하나 이상의 옵션을 추가해주세요.');
            return;
        }

		let cartData = JSON.parse(sessionStorage.getItem('cart_data')) || { products: [] };

        selectedOptions.forEach(option => {
            const uniqueItemId = `${productName}_${option.color}_${option.size}`;
            const existingItemInCart = cartData.products.find(p => p.id === uniqueItemId);

            if (existingItemInCart) {
                existingItemInCart.quantity += option.quantity;
            } else {
                const basePrice = getBaseProductPrice(); // ⭐ 장바구니에 담을 때도 최신 가격을 가져옴
                cartData.products.push({
                    id: uniqueItemId, 
                    name: `${productName} ${option.color} ${option.size}`,
                    price: basePrice,
                    quantity: option.quantity,
                    image: productImageSrc 
                });
            }
        });

        sessionStorage.setItem('cart_data', JSON.stringify(cartData));
        selectedOptions = []; 
        updateOptionsDisplay(); 
        if (confirm('장바구니에 상품이 추가되었습니다. 장바구니로 이동하시겠습니까?')) {
            window.location.href = '/cart'; 
        }
    });

    console.log("페이지 로드 시 초기화 완료.");
});