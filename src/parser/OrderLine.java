package parser;

import java.math.BigDecimal;

public record OrderLine(int userId, String name, int orderId, int productId, BigDecimal value, String date) {}
