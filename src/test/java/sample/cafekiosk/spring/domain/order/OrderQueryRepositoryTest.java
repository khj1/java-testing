package sample.cafekiosk.spring.domain.order;

import jakarta.persistence.EntityManager;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sample.cafekiosk.spring.api.controller.order.request.OrderSearchCond;
import sample.cafekiosk.spring.api.service.order.response.OrderResponse;
import sample.cafekiosk.spring.domain.product.Product;
import sample.cafekiosk.spring.domain.product.ProductRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static sample.cafekiosk.spring.domain.order.OrderStatus.*;
import static sample.cafekiosk.spring.domain.product.ProductSellingStatus.SELLING;
import static sample.cafekiosk.spring.domain.product.ProductType.HANDMADE;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
public class OrderQueryRepositoryTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PageableHandlerMethodArgumentResolverCustomizer pageableCustomizer;

    @DisplayName("주문 ID로 주문을 조회할 수 있다.")
    @Test
    void searchByName() {
        //given
        Product product1 = createProduct("아메리카노", "001", 5000);
        Product product2 = createProduct("카페라떼", "002", 7000);
        Product product3 = createProduct("카푸치노", "003", 9000);
        List<Product> products = List.of(product1, product2, product3);
        productRepository.saveAll(products);

        Order order1 = Order.create(products, LocalDateTime.of(2025, 4, 17, 3, 14));
        Order order2 = Order.create(products, LocalDateTime.of(2025, 4, 18, 3, 14));
        List<Order> savedOrders = orderRepository.saveAll(List.of(order1, order2));
        List<Long> savedOrderIds = savedOrders.stream()
            .map(Order::getId)
            .toList();

        Long id1 = savedOrderIds.get(0);
        Long id2 = savedOrderIds.get(1);

        //when
        Order findOrder1 = orderRepository.searchById(id1);
        Order findOrder2 = orderRepository.searchById(id2);

        //then
        assertThat(findOrder1.getId()).isEqualTo(id1);
        assertThat(findOrder2.getId()).isEqualTo(id2);
    }

    @DisplayName("주문 ID로 해당 주문에 포함된 모든 상품을 조회할 수 있다.")
    @Test
    void searchOrderProductsById() {
        //given
        Product product1 = createProduct("아메리카노", "001", 5000);
        Product product2 = createProduct("카페라떼", "002", 7000);
        Product product3 = createProduct("카푸치노", "003", 9000);
        List<Product> products = List.of(product1, product2, product3);
        productRepository.saveAll(products);

        Order order = Order.create(products, LocalDateTime.of(2025, 4, 17, 3, 14));
        Order savedOrder = orderRepository.save(order);
        Long savedId = savedOrder.getId();

        //when
        List<Product> findProducts = orderRepository.searchProductsBy(savedId);

        //then
        assertThat(findProducts).hasSize(3)
            .extracting("name", "productNumber", "price")
            .containsExactlyInAnyOrder(
                tuple("아메리카노", "001", 5000),
                tuple("카페라떼", "002", 7000),
                tuple("카푸치노", "003", 9000)
            );
    }

    static Stream<Arguments> searchScenarios() {
        LocalDateTime dateTime1 = LocalDateTime.of(2025, 4, 17, 3, 14);
        LocalDateTime dateTime2 = LocalDateTime.of(2025, 4, 18, 3, 14);

        return Stream.of(
            Arguments.of("상태+날짜 모두 있을 때", PAYMENT_COMPLETED, dateTime1, 1,
                List.of(tuple(PAYMENT_COMPLETED, dateTime1))

            ),
            Arguments.of("상태만 있을 때", CANCELED, null, 2,
                List.of(tuple(CANCELED, dateTime2),
                    tuple(CANCELED, dateTime1))

            ),
            Arguments.of("날짜만 있을 때", null, dateTime1, 3,
                List.of(tuple(PAYMENT_COMPLETED, dateTime1),
                    tuple(CANCELED, dateTime1),
                    tuple(INIT, dateTime1))
            ),
            Arguments.of("아무 조건 없을 때", null, null, 4,
                List.of(
                    tuple(PAYMENT_COMPLETED, dateTime1),
                    tuple(CANCELED, dateTime2),
                    tuple(CANCELED, dateTime1),
                    tuple(INIT, dateTime1)
                )
            )
        );
    }

    @DisplayName("주문 상태와 주문 등록 날짜에 따라 주문을 조회할 수 있다.")
    @MethodSource("searchScenarios")
    @ParameterizedTest(name = "{0}")
    void searchByOrderStatusAndRegisteredDateTime(
        String scenarioName,
        OrderStatus orderStatus,
        LocalDateTime dateTime,
        int expectCount,
        List<Tuple> expectTuples
    ) {
        //given
        Product product1 = createProduct("아메리카노", "001", 5000);
        Product product2 = createProduct("카페라떼", "002", 7000);
        Product product3 = createProduct("카푸치노", "003", 9000);
        List<Product> products = List.of(product1, product2, product3);
        productRepository.saveAll(products);

        LocalDateTime dateTime1 = LocalDateTime.of(2025, 4, 17, 3, 14);
        LocalDateTime dateTime2 = LocalDateTime.of(2025, 4, 18, 3, 14);

        Order order1 = createOrder(products, PAYMENT_COMPLETED, dateTime1);
        Order order2 = createOrder(products, CANCELED, dateTime2);
        Order order3 = createOrder(products, CANCELED, dateTime1);
        Order order4 = createOrder(products, INIT, dateTime1);
        orderRepository.saveAll(List.of(order1, order2, order3, order4));

        //when
        List<Order> orders = orderRepository.searchBy(orderStatus, dateTime);

        //then
        assertThat(orders).hasSize(expectCount)
            .extracting("orderStatus", "registeredDateTime")
            .containsExactlyInAnyOrderElementsOf(expectTuples);
    }

    @DisplayName("주문 조회 결과를 DTO로 받아올 수 있다.")
    @Test
    void searchOrderAndReturnDto() {
        //given
        Product product1 = createProduct("아메리카노", "001", 5000);
        Product product2 = createProduct("카페라떼", "002", 7000);
        Product product3 = createProduct("카푸치노", "003", 9000);
        List<Product> products = List.of(product1, product2, product3);
        productRepository.saveAll(products);

        LocalDateTime dateTime1 = LocalDateTime.of(2025, 4, 17, 3, 14);
        LocalDateTime dateTime2 = LocalDateTime.of(2025, 4, 18, 3, 14);

        Order order1 = createOrder(products, PAYMENT_COMPLETED, dateTime1);
        Order order2 = createOrder(products, CANCELED, dateTime2);
        Order order3 = createOrder(products, CANCELED, dateTime1);
        Order order4 = createOrder(products, INIT, dateTime1);
        orderRepository.saveAll(List.of(order1, order2, order3, order4));

        //when
        List<OrderResponse> responses = orderRepository.findOrderDtos();

        //then
        assertThat(responses).hasSize(4)
            .extracting("totalPrice", "registeredDateTime")
            .containsExactlyInAnyOrder(
                tuple(21_000, dateTime1),
                tuple(21_000, dateTime2),
                tuple(21_000, dateTime1),
                tuple(21_000, dateTime1)
            );
    }

    // fetchJoin() 적용해야할까?
    @DisplayName("주문 금액이 가장 큰 주문을 조회할 수 있다.")
    @Test
    void searchWithMaxTotalPrice() {
        //given
        Product product1 = createProduct("아메리카노", "001", 5000);
        Product product2 = createProduct("카페라떼", "002", 7000);
        Product product3 = createProduct("카푸치노", "003", 9000);
        List<Product> products = List.of(product1, product2, product3);
        productRepository.saveAll(products);

        List<Product> products1 = List.of(product1, product2);
        List<Product> products2 = List.of(product2, product3);
        LocalDateTime dateTime1 = LocalDateTime.of(2025, 4, 17, 3, 14);
        LocalDateTime dateTime2 = LocalDateTime.of(2025, 4, 18, 3, 14);

        Order order1 = createOrder(products1, PAYMENT_COMPLETED, dateTime1);
        Order order2 = createOrder(products2, CANCELED, dateTime2);
        orderRepository.saveAll(List.of(order1, order2));

        //when
        Order order = orderRepository.findHighestTotalPriceOrder();

        //then
        assertThat(order)
            .extracting("totalPrice", "orderStatus")
            .contains(16_000, CANCELED);
    }

    @DisplayName("주문 상태 별 총 매출액을 계산할 수 있다.")
    @Test
    void calculateSumByOrderStatus() {
        //given
        Product product1 = createProduct("아메리카노", "001", 5000);
        Product product2 = createProduct("카페라떼", "002", 7000);
        Product product3 = createProduct("카푸치노", "003", 9000);
        List<Product> products = List.of(product1, product2, product3);
        productRepository.saveAll(products);

        List<Product> products1 = List.of(product1, product2);
        List<Product> products2 = List.of(product2, product3);
        LocalDateTime dateTime1 = LocalDateTime.of(2025, 4, 17, 3, 14);
        LocalDateTime dateTime2 = LocalDateTime.of(2025, 4, 18, 3, 14);

        Order order1 = createOrder(products1, PAYMENT_COMPLETED, dateTime1);
        Order order2 = createOrder(products2, PAYMENT_COMPLETED, dateTime1);
        Order order3 = createOrder(products2, CANCELED, dateTime2);
        orderRepository.saveAll(List.of(order1, order2, order3));

        //when
        int totalPrice = orderRepository.calculateTotalPriceBy(PAYMENT_COMPLETED);

        //then
        assertThat(totalPrice).isEqualTo(28_000);
    }

    @DisplayName("특정 등록날짜 이내의 주문 상태를 한번에 변경할 수 있다.")
    @Test
    void updateOrderStatusBy() {
        //given
        Product product1 = createProduct("아메리카노", "001", 5000);
        Product product2 = createProduct("카페라떼", "002", 7000);
        Product product3 = createProduct("카푸치노", "003", 9000);
        List<Product> products = List.of(product1, product2, product3);
        productRepository.saveAll(products);

        List<Product> products1 = List.of(product1, product2);
        List<Product> products2 = List.of(product2, product3);
        LocalDateTime dateTime1 = LocalDateTime.of(2025, 4, 17, 3, 14);
        LocalDateTime dateTime2 = LocalDateTime.of(2025, 4, 18, 3, 14);

        Order order1 = createOrder(products1, PAYMENT_COMPLETED, dateTime1);
        Order order2 = createOrder(products2, PAYMENT_COMPLETED, dateTime2);
        Order order3 = createOrder(products2, CANCELED, dateTime1);
        orderRepository.saveAll(List.of(order1, order2, order3));

        //when
        LocalDateTime start = LocalDateTime.of(2025, 4, 17, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 4, 18, 0, 0);
        orderRepository.updateOrderStatusBetween(COMPLETED, start, end);

        em.flush();
        em.clear();

        List<Order> orders = orderRepository.searchBy(COMPLETED, null);

        //then
        assertThat(orders).hasSize(2)
            .extracting("orderStatus", "registeredDateTime")
            .containsExactlyInAnyOrder(
                tuple(COMPLETED, dateTime1),
                tuple(COMPLETED, dateTime1)
            );
    }

    // 페이징 처리
    @DisplayName("주문을 특정 갯수만큼만 조회할 수 있다.")
    @Test
    void searchAll() {
        //given
        //given
        Product product1 = createProduct("아메리카노", "001", 5000);
        Product product2 = createProduct("카페라떼", "002", 7000);
        Product product3 = createProduct("카푸치노", "003", 9000);
        List<Product> products = List.of(product1, product2, product3);
        productRepository.saveAll(products);

        List<Product> products1 = List.of(product1, product2);
        List<Product> products2 = List.of(product2, product3);
        LocalDateTime dateTime1 = LocalDateTime.of(2025, 4, 17, 3, 14);
        LocalDateTime dateTime2 = LocalDateTime.of(2025, 4, 18, 3, 14);

        Order order1 = createOrder(products1, PAYMENT_COMPLETED, dateTime1);
        Order order2 = createOrder(products1, PAYMENT_COMPLETED, dateTime2);
        Order order3 = createOrder(products1, PAYMENT_COMPLETED, dateTime1);
        Order order4 = createOrder(products2, PAYMENT_COMPLETED, dateTime2);
        Order order5 = createOrder(products2, CANCELED, dateTime1);
        orderRepository.saveAll(List.of(order1, order2, order3, order4, order5));

        OrderSearchCond condition = new OrderSearchCond(PAYMENT_COMPLETED, null);
        Pageable pageable = PageRequest.of(1, 2, Sort.by("id").descending());

        OrderResponse orderResponse1 = OrderResponse.of(order2);
        OrderResponse orderResponse2 = OrderResponse.of(order1);

        //when
        Page<OrderResponse> orderPage = orderRepository.findOrdersPage(condition, pageable);

        //then
        assertThat(orderPage.getContent())
            .containsExactly(orderResponse1, orderResponse2);

        assertThat(orderPage.getTotalElements()).isEqualTo(4);
        assertThat(orderPage.getTotalPages()).isEqualTo(2);
        assertThat(orderPage.getNumber()).isEqualTo(1);
        assertThat(orderPage.getSize()).isEqualTo(2);
    }

    private Product createProduct(String name, String productNumber, int price) {
        return Product.builder()
            .name(name)
            .type(HANDMADE)
            .productNumber(productNumber)
            .price(price)
            .sellingStatus(SELLING)
            .build();
    }

    private Order createOrder(List<Product> products, OrderStatus orderStatus, LocalDateTime registeredDateTime) {
        return Order.builder()
            .products(products)
            .orderStatus(orderStatus)
            .registeredDateTime(registeredDateTime)
            .build();
    }
}
