package domain;

public class Product {
    private int productId;
    private String value;

    public Product(int productId, String value) {
        this.productId = productId;
        this.value = value;
    }

    public int getProductId() {
        return productId;
    }

    public String getValue() {
        return value;
    }
}
