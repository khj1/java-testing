package sample.cafekiosk.spring.domain.stock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import sample.cafekiosk.spring.IntegrationTestSupport;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@Transactional
class StockRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private StockRepository stockRepository;

    @DisplayName("상품 번호 리스트로 재고를 조회할 수 있다.")
    @Test
    void findAllByProductNumberIn() {
        Stock stock1 = new Stock("001", 2);
        Stock stock2 = new Stock("002", 2);
        stockRepository.saveAll(List.of(stock1, stock2));

        List<Stock> stocks = stockRepository.findAllByProductNumberIn(List.of("001", "002"));

        assertThat(stocks).hasSize(2)
            .extracting("productNumber", "quantity")
            .containsExactlyInAnyOrder(
                tuple("001", 2),
                tuple("002", 2)
            );
    }
}