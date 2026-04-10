package domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class User {
    private final int userId;
    private final String name;
    private final List<Order> orders;

    public User(int userId, String name) {
        this.userId = userId;
        this.name = name;
        this.orders = new ArrayList<>();
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public List<Order> getOrders() {
        return Collections.unmodifiableList(orders);
    }

    public void addOrder(Order order) {
        this.orders.add(order);
    }
}
