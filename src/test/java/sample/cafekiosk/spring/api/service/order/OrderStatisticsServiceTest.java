package sample.cafekiosk.spring.api.service.order;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import sample.cafekiosk.spring.IntegrationTestSupport;
import sample.cafekiosk.spring.domain.history.MailSendHistory;
import sample.cafekiosk.spring.domain.history.MailSendHistoryRepository;
import sample.cafekiosk.spring.domain.order.Order;
import sample.cafekiosk.spring.domain.order.OrderRepository;
import sample.cafekiosk.spring.domain.order.OrderStatus;
import sample.cafekiosk.spring.domain.orderproduct.OrderProductRepository;
import sample.cafekiosk.spring.domain.product.Product;
import sample.cafekiosk.spring.domain.product.ProductRepository;
import sample.cafekiosk.spring.domain.product.ProductType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static sample.cafekiosk.spring.domain.product.ProductSellingStatus.SELLING;
import static sample.cafekiosk.spring.domain.product.ProductType.*;

class OrderStatisticsServiceTest extends IntegrationTestSupport {

    @Autowired
    private OrderStatisticsService orderStatisticsService;

    @Autowired
    private OrderProductRepository orderProductRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MailSendHistoryRepository mailSendHistoryRepository;

    @AfterEach
    void tearDown() {
        orderProductRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        mailSendHistoryRepository.deleteAllInBatch();
    }

    @DisplayName("주어진 날짜에 결제가 완료된 모든 주문의 총 매출액을 계산해 메일로 전송할 수 있다.")
    @Test
    void sendOrderStatisticsMail() {
        //given
        LocalDateTime date1 = LocalDateTime.of(2025, 4, 14, 23, 59, 59);
        LocalDateTime date2 = LocalDateTime.of(2025, 4, 15, 0, 0, 0);
        LocalDateTime date3 = LocalDateTime.of(2025, 4, 15, 23, 59, 59);
        LocalDateTime date4 = LocalDateTime.of(2025, 4, 16, 0, 0, 0);

        Product product1 = createProduct(BOTTLE, "001", 1000);
        Product product2 = createProduct(BAKERY, "002", 3000);
        Product product3 = createProduct(HANDMADE, "003", 5000);
        List<Product> products = List.of(product1, product2, product3);
        productRepository.saveAll(products);

        Order order1 = createPaymentCompletedOrder(products, date1);
        Order order2 = createPaymentCompletedOrder(products, date2);
        Order order3 = createPaymentCompletedOrder(products, date3);
        Order order4 = createPaymentCompletedOrder(products, date4);

        // stubbing
        when(mailSendClient.sendEmail(
            any(String.class), any(String.class), any(String.class), any(String.class))
        ).thenReturn(true);

        //when
        boolean result = orderStatisticsService.sendOrderStatisticsMail(LocalDate.of(2025, 4, 15), "test@test.com");

        //then
        assertThat(result).isTrue();

        List<MailSendHistory> histories = mailSendHistoryRepository.findAll();

        assertThat(histories).hasSize(1)
            .extracting("content")
            .contains("총 매출 합계는 18000원 입니다.");
    }

    private Product createProduct(ProductType type, String productNumber, int price) {
        return Product.builder()
            .productNumber(productNumber)
            .type(type)
            .sellingStatus(SELLING)
            .name("메뉴 이름")
            .price(price)
            .build();
    }

    private Order createPaymentCompletedOrder(List<Product> products, LocalDateTime now) {
        Order order = Order.builder()
            .products(products)
            .orderStatus(OrderStatus.PAYMENT_COMPLETED)
            .registeredDateTime(now)
            .build();

        return orderRepository.save(order);
    }
}