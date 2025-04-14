package sample.cafekiosk.spring.api.service.product;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import sample.cafekiosk.spring.api.controller.product.dto.request.ProductCreateRequest;
import sample.cafekiosk.spring.api.service.product.dto.ProductResponse;
import sample.cafekiosk.spring.domain.product.Product;
import sample.cafekiosk.spring.domain.product.ProductRepository;
import sample.cafekiosk.spring.domain.product.ProductSellingStatus;
import sample.cafekiosk.spring.domain.product.ProductType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static sample.cafekiosk.spring.domain.product.ProductSellingStatus.*;
import static sample.cafekiosk.spring.domain.product.ProductType.HANDMADE;

@ActiveProfiles("test")
@SpringBootTest
class ProductServiceTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @AfterEach
    void tearDown() {
        productRepository.deleteAllInBatch();
    }

    @DisplayName("신규 상품을 등록한다. 상품 번호는 가장 최근 상품의 상품 번호에서 1 증가한 값이다.")
    @Test
    void createProduct() {
        Product product1 = createProduct("001", HANDMADE, SELLING, "아메리카노", 4000);
        Product product2 = createProduct("002", HANDMADE, HOLD, "카페라떼", 4000);
        Product product3 = createProduct("003", HANDMADE, STOP_SELLING, "팥 빙수", 7000);

        productRepository.saveAll(List.of(product1, product2, product3));

        ProductCreateRequest request = ProductCreateRequest.of(HANDMADE, SELLING, "유자차", 4500);
        ProductResponse response = productService.createProduct(request);

        assertThat(response).isNotNull()
                .extracting("productNumber", "type", "sellingStatus", "name", "price")
                .contains("004", HANDMADE, SELLING, "유자차", 4500);
    }

    @DisplayName("기존 상품이 없는 경우 신규 상품을 등록하면 상품 번호는 001 번으로 등록된다.")
    @Test
    void createProductWhenNoProducts() {
        ProductCreateRequest request = ProductCreateRequest.of(HANDMADE, SELLING, "유자차", 4500);
        ProductResponse response = productService.createProduct(request);

        assertThat(response).isNotNull()
                .extracting("productNumber", "type", "sellingStatus", "name", "price")
                .contains("001", HANDMADE, SELLING, "유자차", 4500);
    }

    @DisplayName("판매중인 상품들만 조회한다.")
    @Test
    void getSellingProducts() {
        Product product1 = createProduct("001", HANDMADE, SELLING, "아메리카노", 4000);
        Product product2 = createProduct("002", HANDMADE, HOLD, "카페라떼", 4000);
        Product product3 = createProduct("003", HANDMADE, STOP_SELLING, "팥 빙수", 7000);

        productRepository.saveAll(List.of(product1, product2, product3));

        List<ProductResponse> products = productService.getSellingProducts();

        assertThat(products).hasSize(2)
                .extracting("productNumber", "type", "sellingStatus", "name", "price")
                .containsExactlyInAnyOrder(
                        tuple("001", HANDMADE, SELLING, "아메리카노", 4000),
                        tuple("002", HANDMADE, HOLD, "카페라떼", 4000)
                );
    }

    private static Product createProduct(String productNumber, ProductType type, ProductSellingStatus sellingStatus, String name, int price) {
        return Product.builder()
                .productNumber(productNumber)
                .type(type)
                .sellingStatus(sellingStatus)
                .name(name)
                .price(price)
                .build();
    }
}