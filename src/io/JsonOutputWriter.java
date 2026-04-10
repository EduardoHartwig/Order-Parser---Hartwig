package io;

import domain.Order;
import domain.Product;
import domain.User;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.StringJoiner;

public final class JsonOutputWriter implements OutputWriter {

    @Override
    public void writeToFile(String filePath, List<User> users) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(filePath), StandardCharsets.UTF_8)) {
            String json = buildJsonString(users);
            writer.write(json);
        }
    }

    public String buildJsonString(List<User> users) {
        StringJoiner usersJoiner = new StringJoiner(",\n", "[\n", "\n]");

        for (User user : users) {
            StringJoiner ordersJoiner = new StringJoiner(",\n");

            for (Order order : user.getOrders()) {
                StringJoiner productsJoiner = new StringJoiner(",\n");

                for (Product product : order.getProducts()) {
                    productsJoiner.add("          { \"product_id\": " + product.getProductId()
                            + ", \"value\": \"" + product.getValue().toPlainString() + "\" }");
                }

                ordersJoiner.add("      {\n"
                        + "        \"order_id\": " + order.getOrderId() + ",\n"
                        + "        \"total\": \"" + order.getTotal().toPlainString() + "\",\n"
                        + "        \"date\": \"" + order.getDate() + "\",\n"
                        + "        \"products\": [\n"
                        + productsJoiner + "\n"
                        + "        ]\n"
                        + "      }");
            }

            usersJoiner.add("  {\n"
                    + "    \"user_id\": " + user.getUserId() + ",\n"
                    + "    \"name\": \"" + escapeJsonString(user.getName()) + "\",\n"
                    + "    \"orders\": [\n"
                    + ordersJoiner + "\n"
                    + "    ]\n"
                    + "  }");
        }

        return usersJoiner.toString();
    }

    private String escapeJsonString(String str) {
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    @Override
    public String getFileExtension() {
        return ".json";
    }
}
