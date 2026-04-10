package parser;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public final class OrderLineParser implements LineParser<OrderLine> {
    
    private static final int USER_ID_SIZE = 10;
    private static final int NAME_SIZE = 45;
    private static final int ORDER_ID_SIZE = 10;
    private static final int PRODUCT_ID_SIZE = 10;
    private static final int VALUE_SIZE = 12;
    private static final int DATE_SIZE = 8;

    @Override
    public OrderLine parse(String line) {
        if (line == null || line.length() < USER_ID_SIZE + NAME_SIZE + ORDER_ID_SIZE + PRODUCT_ID_SIZE + VALUE_SIZE + DATE_SIZE) {
            throw new IllegalArgumentException("Linha inválida ou incompleta");
        }

        int position = 0;

        int userId = Integer.parseInt(line.substring(position, position + USER_ID_SIZE).trim());
        position += USER_ID_SIZE;

        String name = line.substring(position, position + NAME_SIZE).trim();
        position += NAME_SIZE;

        int orderId = Integer.parseInt(line.substring(position, position + ORDER_ID_SIZE).trim());
        position += ORDER_ID_SIZE;

        int productId = Integer.parseInt(line.substring(position, position + PRODUCT_ID_SIZE).trim());
        position += PRODUCT_ID_SIZE;

        String rawValue = line.substring(position, position + VALUE_SIZE).trim();
        BigDecimal value = formatDecimal(rawValue)
                .orElseThrow(() -> new IllegalArgumentException("Valor monetário inválido: " + rawValue));
        position += VALUE_SIZE;

        String date = formatDate(line.substring(position, position + DATE_SIZE).trim());

        return new OrderLine(userId, name, orderId, productId, value, date);
    }

    private Optional<BigDecimal> formatDecimal(String value) {
        if (value.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(new BigDecimal(value).setScale(2, RoundingMode.HALF_UP));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private String formatDate(String dateStr) {
        if (dateStr.length() != 8) {
            throw new IllegalArgumentException("Data inválida: " + dateStr);
        }
        return dateStr.substring(0, 4) + "-" + dateStr.substring(4, 6) + "-" + dateStr.substring(6, 8);
    }
}
