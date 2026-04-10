package domain;

import java.math.BigDecimal;

public class Product {
    private final int productId;
    private final BigDecimal value;

    public Product(int productId, BigDecimal value) {
        this.productId = productId;
        this.value = value;
    }

    public int getProductId() {
        return productId;
    }

    public BigDecimal getValue() {
        return value;
    }
}
