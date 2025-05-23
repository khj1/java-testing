package sample.cafekiosk.spring.api.service.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sample.cafekiosk.spring.api.service.product.dto.request.ProductCreateServiceRequest;
import sample.cafekiosk.spring.api.service.product.dto.response.ProductResponse;
import sample.cafekiosk.spring.domain.product.Product;
import sample.cafekiosk.spring.domain.product.ProductRepository;
import sample.cafekiosk.spring.domain.product.ProductSellingStatus;

import java.util.List;

/**
 * readOnly = true (읽기 전용)
 * CRUD 에서 CUD 동작 x / only Read
 * JPA / CUD 스냅샷 저장, 변경 감지 X (성능 향상)
 * <p>
 * CQRS - Command / Read 의 분리
 */
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ProductService {

    public static final String FIRST_PRODUCT_NUMBER = "001";

    private final ProductRepository productRepository;
    private final ProductNumberFactory productNumberFactory;

    // 동시성 이슈
    @Transactional
    public ProductResponse createProduct(ProductCreateServiceRequest request) {
        String newProductNumber = productNumberFactory.createProductNumber();

        Product product = request.toEntity(newProductNumber);
        Product saved = productRepository.save(product);

        return ProductResponse.of(saved);
    }

    public List<ProductResponse> getSellingProducts() {
        List<Product> products = productRepository.findAllBySellingStatusIn(ProductSellingStatus.forDisplay());

        return products.stream()
            .map(ProductResponse::of)
            .toList();
    }
}
