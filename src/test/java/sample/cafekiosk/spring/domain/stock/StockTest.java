package sample.cafekiosk.spring.domain.stock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StockTest {

    @DisplayName("재고 수량을 차감할 수 있다.")
    @Test
    void deductQuantity() {
        Stock stock = new Stock("001", 4);

        stock.deductQuantity(3);

        assertThat(stock.getQuantity()).isEqualTo(1);
    }

    @DisplayName("재고보다 많은 수량으로 차감을 시도하는 경우 예외가 발생한다.")
    @Test
    void deductOverMaxQuantity() {
        Stock stock = new Stock("001", 4);

        assertThatThrownBy(() -> stock.deductQuantity(5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity exceeds maximum quantity");
    }
}