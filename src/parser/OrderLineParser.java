package parser;

public class OrderLineParser {
    
    private static final int USER_ID_SIZE = 10;
    private static final int NAME_SIZE = 45;
    private static final int ORDER_ID_SIZE = 10;
    private static final int PRODUCT_ID_SIZE = 10;
    private static final int VALUE_SIZE = 12;
    private static final int DATE_SIZE = 8;

    public static class OrderLine {
        public int userId;
        public String name;
        public int orderId;
        public int productId;
        public String value;
        public String date;
    }

    public static OrderLine parse(String line) {
        if (line == null || line.length() < USER_ID_SIZE + NAME_SIZE + ORDER_ID_SIZE + PRODUCT_ID_SIZE + VALUE_SIZE + DATE_SIZE) {
            throw new IllegalArgumentException("Linha inválida ou incompleta");
        }

        OrderLine orderLine = new OrderLine();
        
        int position = 0;

        String userIdStr = line.substring(position, position + USER_ID_SIZE).trim();
        orderLine.userId = Integer.parseInt(userIdStr);
        position += USER_ID_SIZE;

        String nameStr = line.substring(position, position + NAME_SIZE);
        orderLine.name = nameStr.trim();
        position += NAME_SIZE;

        String orderIdStr = line.substring(position, position + ORDER_ID_SIZE).trim();
        orderLine.orderId = Integer.parseInt(orderIdStr);
        position += ORDER_ID_SIZE;

        String productIdStr = line.substring(position, position + PRODUCT_ID_SIZE).trim();
        orderLine.productId = Integer.parseInt(productIdStr);
        position += PRODUCT_ID_SIZE;

        String valueStr = line.substring(position, position + VALUE_SIZE).trim();
        orderLine.value = formatDecimal(valueStr);
        position += VALUE_SIZE;

        String dateStr = line.substring(position, position + DATE_SIZE).trim();
        orderLine.date = formatDate(dateStr);

        return orderLine;
    }

    private static String formatDecimal(String value) {
        if (value.isEmpty()) {
            return "0.00";
        }
        try {
            double doubleValue = Double.parseDouble(value);
            return String.format(java.util.Locale.US, "%.2f", doubleValue);
        } catch (NumberFormatException e) {
            return "0.00";
        }
    }

    private static String formatDate(String dateStr) {
        if (dateStr.length() != 8) {
            throw new IllegalArgumentException("Data inválida: " + dateStr);
        }
        return dateStr.substring(0, 4) + "-" + dateStr.substring(4, 6) + "-" + dateStr.substring(6, 8);
    }
}
