package sample.cafekiosk.spring.domain.stock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.Collection;
import java.util.List;

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

    /**
     * @DynamicTest 를 활용한 재고 차감 시나리오
     * 공통의 환경에서 시작해서 단계적으로 상태의 변화를 테스트 해보고 싶을 때 사용한다.
     */
    @DisplayName("재고 차감 시나리오")
    @TestFactory
    Collection<DynamicTest> stockDeductionDynamicTest() {
        Stock stock = Stock.create("001", 1);

        return List.of(
            DynamicTest.dynamicTest("재고를 주어진 개수만큼 차감할 수 있다.", () -> {
                //given
                int quantity = 1;

                //when
                stock.deductQuantity(quantity);

                //then
                assertThat(stock.getQuantity()).isZero();
            }),
            DynamicTest.dynamicTest("재고 보다 많은 수의 수량으로 차감 시도하는 경우 예외가 발생한다.", () -> {
                //given
                int quantity = 1;

                //when //then
                assertThatThrownBy(() -> stock.deductQuantity(5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Quantity exceeds maximum quantity");
            })
        );
    }
}