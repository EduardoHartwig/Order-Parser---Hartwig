package domain;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private int orderId;
    private String date;
    private List<Product> products;

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
        return products;
    }

    public void addProduct(Product product) {
        this.products.add(product);
    }

    public String getTotal() {
        double total = 0.0;
        for (Product product : products) {
            total += Double.parseDouble(product.getValue());
        }
        return String.format(java.util.Locale.US, "%.2f", total);
    }
}
