package sample.cafekiosk.spring.domain.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static sample.cafekiosk.spring.domain.product.ProductType.*;


class ProductTypeTest {

    /**
     * 한 문단엔 하나의 주제만을 담는다.
     */
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

    /**
     * 위 테스트를 한 번의 테스트로 끝내는 방법
     *
     * @ParameterizedTest
     * @ValueSoruce
     * @CsvSources
     */
    @DisplayName("상품 타입이 재고 관련 타입인지 체크한다.")
    @CsvSource({"HANDMADE, false", "BOTTLE, true", "BAKERY, true"})
    @ParameterizedTest
    void containsStockType4(ProductType productType, boolean expected) {
        //when
        boolean result = ProductType.containsStockType(productType);

        //then
        assertThat(result).isEqualTo(expected);
    }

    /**
     * @MethodSource 를 활용해 파라미터가 많을 경우를 대비할 수 있다.
     * <p>
     * 프로덕션 코드에서는 private 메서드를 하위에 위치시키지만,
     * MethodSource의 경우 given 절의 역할을 하기 때문에 테스트 코드 상단에 두는게 가독성이 좋다.
     */
    private static Stream<Arguments> provideProductTypesForCheckingStockType() {
        return Stream.of(
            Arguments.of(HANDMADE, false),
            Arguments.of(BOTTLE, true),
            Arguments.of(BAKERY, true)
        );
    }

    @DisplayName("상품 타입이 재고 관련 타입인지 체크한다.")
    @MethodSource("provideProductTypesForCheckingStockType")
    @ParameterizedTest
    void containsStockType5(ProductType productType, boolean expected) {
        //when
        boolean result = ProductType.containsStockType(productType);

        //then
        assertThat(result).isEqualTo(expected);
    }

}