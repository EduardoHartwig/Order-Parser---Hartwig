package service;

import domain.Order;
import domain.Product;
import domain.User;
import parser.LineParser;
import parser.OrderLine;

import java.util.*;

public class OrderNormalizationService implements NormalizationService {

    private final LineParser<OrderLine> lineParser;
    private final ProcessingLogger logger;

    public OrderNormalizationService(LineParser<OrderLine> lineParser, ProcessingLogger logger) {
        this.lineParser = lineParser;
        this.logger = logger;
    }

    @Override
    public List<User> normalizeOrders(List<String> lines) {
        Map<Integer, User> userMap = new LinkedHashMap<>();
        Map<String, Order> orderMap = new HashMap<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            try {
                OrderLine orderLine = lineParser.parse(line);

                User user = userMap.computeIfAbsent(orderLine.userId(), 
                    id -> new User(id, orderLine.name()));

                String orderKey = orderLine.userId() + "-" + orderLine.orderId();

                Order order = orderMap.computeIfAbsent(orderKey, 
                    key -> {
                        Order newOrder = new Order(orderLine.orderId(), orderLine.date());
                        user.addOrder(newOrder);
                        return newOrder;
                    });

                Product product = new Product(orderLine.productId(), orderLine.value());
                order.addProduct(product);

            } catch (Exception e) {
                logger.error("Erro ao processar linha " + (i + 1) + ": " + line);
                logger.error("Detalhes: " + e.getMessage());
            }
        }

        List<User> users = new ArrayList<>(userMap.values());
        users.sort(Comparator.comparingInt(User::getUserId));
        return users;
    }
}
