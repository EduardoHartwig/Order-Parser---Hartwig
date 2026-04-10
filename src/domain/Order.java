package domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {
    private final int orderId;
    private final String date;
    private final List<Product> products;

    public Order(int orderId, String date) {
        this.orderId = orderId;
        this.date = date;
        this.products = new ArrayList<>();
    }

    public int getOrderId() {
        return orderId;
    }

    public String getDate() {
        return date;
    }

    public List<Product> getProducts() {
        return Collections.unmodifiableList(products);
    }

    public void addProduct(Product product) {
        this.products.add(product);
    }

    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (Product product : products) {
            total = total.add(product.getValue());
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }
}
