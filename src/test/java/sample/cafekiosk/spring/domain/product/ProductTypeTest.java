package sample.cafekiosk.spring.domain.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static sample.cafekiosk.spring.domain.product.ProductType.*;

class ProductTypeTest {

    @DisplayName("재고 타입이 포함되어 있는지 판단한다.")
    @Test
    void containsStockType() {
        boolean bottleResult = ProductType.containsStockType(BOTTLE);
        boolean bakeryResult = ProductType.containsStockType(BAKERY);
        boolean handmadeResult = ProductType.containsStockType(HANDMADE);

        assertThat(bottleResult).isTrue();
        assertThat(bakeryResult).isTrue();
        assertThat(handmadeResult).isFalse();
    }
}