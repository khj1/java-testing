package sample.cafekiosk.spring.domain.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static sample.cafekiosk.spring.domain.product.ProductType.*;

/**
 * 한 문단엔 하나의 주제만을 담는다.
 */
class ProductTypeTest {

    @DisplayName("재고 타입이 포함되어 있는지 판단한다.")
    @Test
    void containsStockType() {
        boolean result = ProductType.containsStockType(BOTTLE);

        assertThat(result).isTrue();
    }

    @DisplayName("재고 타입이 포함되어 있는지 판단한다.")
    @Test
    void containsStockType2() {
        boolean result = ProductType.containsStockType(BAKERY);

        assertThat(result).isTrue();
    }

    @DisplayName("재고 타입이 포함되어 있는지 판단한다.")
    @Test
    void containsStockType3() {
        boolean result = ProductType.containsStockType(HANDMADE);

        assertThat(result).isFalse();
    }
}