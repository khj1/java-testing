package sample.cafekiosk.spring.api.service.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sample.cafekiosk.spring.domain.product.ProductRepository;

@RequiredArgsConstructor
@Component
public class ProductNumberFactory {

    public static final String FIRST_PRODUCT_NUMBER = "001";

    private final ProductRepository productRepository;

    public String createProductNumber() {
        String latestProductNumber = productRepository.findLatestProductNumber();
        if (latestProductNumber == null) {
            return FIRST_PRODUCT_NUMBER;
        }
        int newProductNumberInt = Integer.parseInt(latestProductNumber) + 1;

        return String.format("%03d", newProductNumberInt);
    }
}
