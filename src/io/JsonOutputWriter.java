package io;

import domain.Order;
import domain.Product;
import domain.User;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class JsonOutputWriter {

    public static void writeToFile(String filePath, List<User> users) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            String json = buildJsonString(users);
            writer.write(json);
        }
    }

    public static String buildJsonString(List<User> users) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            sb.append("  {\n");
            sb.append("    \"user_id\": ").append(user.getUserId()).append(",\n");
            sb.append("    \"name\": \"").append(escapeJsonString(user.getName())).append("\",\n");
            sb.append("    \"orders\": [\n");

            List<Order> orders = user.getOrders();
            for (int j = 0; j < orders.size(); j++) {
                Order order = orders.get(j);
                sb.append("      {\n");
                sb.append("        \"order_id\": ").append(order.getOrderId()).append(",\n");
                sb.append("        \"total\": \"").append(order.getTotal()).append("\",\n");
                sb.append("        \"date\": \"").append(order.getDate()).append("\",\n");
                sb.append("        \"products\": [\n");

                List<Product> products = order.getProducts();
                for (int k = 0; k < products.size(); k++) {
                    Product product = products.get(k);
                    sb.append("          { \"product_id\": ").append(product.getProductId())
                            .append(", \"value\": \"").append(product.getValue()).append("\" }");
                    if (k < products.size() - 1) {
                        sb.append(",");
                    }
                    sb.append("\n");
                }

                sb.append("        ]\n");
                sb.append("      }");
                if (j < orders.size() - 1) {
                    sb.append(",");
                }
                sb.append("\n");
            }

            sb.append("    ]\n");
            sb.append("  }");
            if (i < users.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }

        sb.append("]");
        return sb.toString();
    }

    private static String escapeJsonString(String str) {
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
