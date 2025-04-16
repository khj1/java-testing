package sample.cafekiosk.spring.api.service.order;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import sample.cafekiosk.spring.IntegrationTestSupport;
import sample.cafekiosk.spring.api.service.order.request.OrderCreateServiceRequest;
import sample.cafekiosk.spring.api.service.order.response.OrderResponse;
import sample.cafekiosk.spring.domain.order.OrderRepository;
import sample.cafekiosk.spring.domain.orderproduct.OrderProductRepository;
import sample.cafekiosk.spring.domain.product.Product;
import sample.cafekiosk.spring.domain.product.ProductRepository;
import sample.cafekiosk.spring.domain.product.ProductType;
import sample.cafekiosk.spring.domain.stock.Stock;
import sample.cafekiosk.spring.domain.stock.StockRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static sample.cafekiosk.spring.domain.product.ProductSellingStatus.SELLING;
import static sample.cafekiosk.spring.domain.product.ProductType.*;

/*
테스트 코드에서 @Transactional 을 사용하면 안되는 이유

테스트 코드에 @Transactional 을 붙이면
비즈니스 코드에 @Transactional 키워드가 누락되도 테스트가 통과하기 때문에 문제가 발생할 수 있다.
 */
class OrderServiceTest extends IntegrationTestSupport {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private OrderService orderService;

    /**
     * deleteAll vs deleteAllInBatch
     * deleteAll
     * - 전체 데이터를 조회한 후 반복문을 돌면서 데이터를 건별로 삭제한다. (느림)
     * - 연관된 엔티티가 있다면 함께 제거한다. (cascade, orphanRemoval)
     * deleteAllInBatch
     * - 데이터를 벌크성으로 한번에 제거한다. (빠름)
     * - 엔티티를 영속성 컨텍스트에 올리지 않고 바로 지운다.
     * - 연관관계를 무시하고 제거한다.
     * <p>
     * 연관된 엔티티 없이 단순히 테이블을 지우고 싶다면 deleteAllInBatch()
     * 연관된 엔티티도 한께 제거하고 싶다면 deleteAll()
     * <p>
     * 테스트 초기화 목적이라면 deleteAllInBatch() 를 사용하는 것이 훨씬 효율적이다.
     * 테스트에서는 보통 DB 상태만 초기화 하면 되기 때문에 cascade, orphanRemoval 같은 연관 로직까지 작동시킬 필요는 없다.
     */
    @AfterEach
    void tearDown() {
        orderProductRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        stockRepository.deleteAllInBatch();
    }

    @DisplayName("주문번호 리스트를 받아 주문을 생성한다.")
    @Test
    void createOrder() {
        Product product1 = createProduct(HANDMADE, "001", 1000);
        Product product2 = createProduct(HANDMADE, "002", 3000);
        Product product3 = createProduct(HANDMADE, "003", 5000);
        productRepository.saveAll(List.of(product1, product2, product3));

        OrderCreateServiceRequest request = new OrderCreateServiceRequest(List.of("001", "002"));

        LocalDateTime registeredDateTime = LocalDateTime.now();
        OrderResponse response = orderService.createOrder(request, registeredDateTime);

        assertThat(response.id()).isNotNull();
        assertThat(response)
            .extracting("registeredDateTime", "totalPrice")
            .contains(registeredDateTime, 4000);
        assertThat(response.products()).hasSize(2)
            .extracting("productNumber", "price")
            .containsExactlyInAnyOrder(
                tuple("001", 1000),
                tuple("002", 3000)
            );
    }

    @DisplayName("중복되는 상품번호 리스트로 주문을 생성할 수 있다.")
    @Test
    void createOrderWithDuplicatedProductNumber() {
        Product product1 = createProduct(HANDMADE, "001", 1000);
        Product product2 = createProduct(HANDMADE, "002", 3000);
        Product product3 = createProduct(HANDMADE, "003", 5000);
        productRepository.saveAll(List.of(product1, product2, product3));

        OrderCreateServiceRequest request = new OrderCreateServiceRequest(List.of("001", "001"));

        LocalDateTime registeredDateTime = LocalDateTime.now();
        OrderResponse response = orderService.createOrder(request, registeredDateTime);

        assertThat(response.id()).isNotNull();
        assertThat(response)
            .extracting("registeredDateTime", "totalPrice")
            .contains(registeredDateTime, 2000);
        assertThat(response.products()).hasSize(2)
            .extracting("productNumber", "price")
            .containsExactlyInAnyOrder(
                tuple("001", 1000),
                tuple("001", 1000)
            );
    }

    @DisplayName("재고 상품이 포함되어 있는 주문번호 리스트를 받아 주문을 생성한다.")
    @Test
    void createOrderWithStock() {
        Product product1 = createProduct(BOTTLE, "001", 1000);
        Product product2 = createProduct(BAKERY, "002", 3000);
        Product product3 = createProduct(HANDMADE, "003", 5000);
        productRepository.saveAll(List.of(product1, product2, product3));

        Stock stock1 = Stock.create("001", 2);
        Stock stock2 = Stock.create("002", 2);
        stockRepository.saveAll(List.of(stock1, stock2));

        OrderCreateServiceRequest request = new OrderCreateServiceRequest(List.of("001", "001", "002", "003"));

        LocalDateTime registeredDateTime = LocalDateTime.now();
        OrderResponse response = orderService.createOrder(request, registeredDateTime);

        assertThat(response.id()).isNotNull();
        assertThat(response)
            .extracting("registeredDateTime", "totalPrice")
            .contains(registeredDateTime, 10_000);
        assertThat(response.products()).hasSize(4)
            .extracting("productNumber", "price")
            .containsExactlyInAnyOrder(
                tuple("001", 1000),
                tuple("001", 1000),
                tuple("002", 3000),
                tuple("003", 5000)
            );

        List<Stock> stocks = stockRepository.findAll();
        assertThat(stocks).hasSize(2)
            .extracting("productNumber", "quantity")
            .containsExactlyInAnyOrder(
                tuple("001", 0),
                tuple("002", 1)
            );
    }

    @DisplayName("재고가 부족한 상품으로 주문을 생성하려는 경우 예외가 발생한다.")
    @Test
    void createOrderWithNoStock() {
        Product product1 = createProduct(BOTTLE, "001", 1000);
        Product product2 = createProduct(BAKERY, "002", 3000);
        Product product3 = createProduct(HANDMADE, "003", 5000);
        productRepository.saveAll(List.of(product1, product2, product3));

        Stock stock1 = Stock.create("001", 1);
        Stock stock2 = Stock.create("002", 1);
        stockRepository.saveAll(List.of(stock1, stock2));

        OrderCreateServiceRequest request = new OrderCreateServiceRequest(List.of("001", "001", "002", "003"));

        LocalDateTime registeredDateTime = LocalDateTime.now();

        assertThatThrownBy(() -> orderService.createOrder(request, registeredDateTime))
            .isInstanceOf(IllegalArgumentException.class);
    }

    private static Product createProduct(ProductType type, String productNumber, int price) {
        return Product.builder()
            .productNumber(productNumber)
            .type(type)
            .sellingStatus(SELLING)
            .name("메뉴 이름")
            .price(price)
            .build();
    }
}