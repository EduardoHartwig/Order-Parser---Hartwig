package service;

import domain.Order;
import domain.Product;
import domain.User;
import parser.OrderLineParser;

import java.util.*;

public class OrderNormalizationService {

    public static List<User> normalizeOrders(List<String> lines) {
        Map<Integer, User> userMap = new LinkedHashMap<>();
        Map<String, Order> orderMap = new HashMap<>();

        for (String line : lines) {
            try {
                OrderLineParser.OrderLine orderLine = OrderLineParser.parse(line);

                User user = userMap.computeIfAbsent(orderLine.userId, 
                    id -> new User(id, orderLine.name));

                String orderKey = orderLine.userId + "-" + orderLine.orderId;

                Order order = orderMap.computeIfAbsent(orderKey, 
                    key -> {
                        Order newOrder = new Order(orderLine.orderId, orderLine.date);
                        user.addOrder(newOrder);
                        return newOrder;
                    });

                Product product = new Product(orderLine.productId, orderLine.value);
                order.addProduct(product);

            } catch (Exception e) {
                System.err.println("Erro ao processar linha: " + line);
                System.err.println("Detalhes: " + e.getMessage());
            }
        }

        return new ArrayList<>(userMap.values());
    }
}
