package sample.cafekiosk.spring.domain.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static sample.cafekiosk.spring.domain.product.ProductSellingStatus.*;
import static sample.cafekiosk.spring.domain.product.ProductType.HANDMADE;

@ActiveProfiles("test")
@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @DisplayName("원하는 판매 상태를 가진 상품들을 조회한다.")
    @Test
    void findAllBySellingStatusIn() {
        Product product1 = createProduct("001", HANDMADE, SELLING, "아메리카노", 4000);
        Product product2 = createProduct("002", HANDMADE, HOLD, "카페라떼", 4000);
        Product product3 = createProduct("003", HANDMADE, STOP_SELLING, "팥 빙수", 7000);

        productRepository.saveAll(List.of(product1, product2, product3));

        List<Product> products = productRepository.findAllBySellingStatusIn(List.of(SELLING, HOLD));

        assertThat(products).hasSize(2)
                .extracting("productNumber", "name", "sellingStatus")
                .containsExactlyInAnyOrder(
                        tuple("001", "아메리카노", SELLING),
                        tuple("002", "카페라떼", HOLD)
                );
    }

    @DisplayName("상품 번호 리스트로 상품들을 조회한다.")
    @Test
    void findAllByProductNumberIn() {
        Product product1 = createProduct("001", HANDMADE, SELLING, "아메리카노", 4000);
        Product product2 = createProduct("002", HANDMADE, HOLD, "카페라떼", 4000);
        Product product3 = createProduct("003", HANDMADE, STOP_SELLING, "팥 빙수", 7000);

        productRepository.saveAll(List.of(product1, product2, product3));

        List<Product> products = productRepository.findAllByProductNumberIn(List.of("001", "002"));

        assertThat(products).hasSize(2)
                .extracting("productNumber", "name", "sellingStatus")
                .containsExactlyInAnyOrder(
                        tuple("001", "아메리카노", SELLING),
                        tuple("002", "카페라떼", HOLD)
                );
    }

    @DisplayName("가장 최근에 등록된 상품의 번호를 조회한다.")
    @Test
    void findLatestProduct() {
        Product product1 = createProduct("001", HANDMADE, SELLING, "아메리카노", 4000);
        Product product2 = createProduct("002", HANDMADE, HOLD, "카페라떼", 4000);
        Product product3 = createProduct("003", HANDMADE, STOP_SELLING, "팥 빙수", 7000);

        productRepository.saveAll(List.of(product1, product2, product3));

        String latestProductNumber = productRepository.findLatestProductNumber();

        assertThat(latestProductNumber).isEqualTo("003");
    }

    @DisplayName("가장 최근에 등록된 상품의 번호를 조회할 때 상품이 하나도 없다면 null을 반환한다..")
    @Test
    void findLatestProductWhenProductIsEmpty() {
        String latestProductNumber = productRepository.findLatestProductNumber();

        assertThat(latestProductNumber).isNull();
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