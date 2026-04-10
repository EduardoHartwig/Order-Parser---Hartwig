import org.junit.Test;

import domain.Order;
import domain.Product;
import domain.User;
import parser.LineParser;
import parser.OrderLine;
import parser.OrderLineParser;
import service.OrderNormalizationService;
import service.ProcessingLogger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class OrderNormalizationServiceTest {

    // ======================== HELPERS ========================

    private String buildLine(String userId, String name, String orderId, String productId, String value, String date) {
        return padLeft(userId, 10)
                + padRight(name, 45)
                + padLeft(orderId, 10)
                + padLeft(productId, 10)
                + padLeft(value, 12)
                + date;
    }

    private String padLeft(String s, int len) {
        return String.format("%" + len + "s", s);
    }

    private String padRight(String s, int len) {
        return String.format("%-" + len + "s", s);
    }

    /**
     * Logger que registra as chamadas para inspeção nos testes.
     */
    private static class SpyLogger implements ProcessingLogger {
        final List<String> infoMessages = new ArrayList<>();
        final List<String> errorMessages = new ArrayList<>();

        @Override
        public void info(String message) {
            infoMessages.add(message);
        }

        @Override
        public void error(String message) {
            errorMessages.add(message);
        }
    }

    private OrderNormalizationService createService(SpyLogger logger) {
        return new OrderNormalizationService(new OrderLineParser(), logger);
    }

    // ======================== AGRUPAMENTO - CASO BÁSICO ========================

    @Test
    public void testSingleLine_OneUserOneOrderOneProduct() {
        SpyLogger logger = new SpyLogger();
        OrderNormalizationService service = createService(logger);

        List<String> lines = List.of(
                buildLine("1", "João", "100", "500", "25.50", "20240315")
        );

        List<User> users = service.normalizeOrders(lines);

        assertEquals(1, users.size());
        User user = users.get(0);
        assertEquals(1, user.getUserId());
        assertEquals("João", user.getName());
        assertEquals(1, user.getOrders().size());

        Order order = user.getOrders().get(0);
        assertEquals(100, order.getOrderId());
        assertEquals("2024-03-15", order.getDate());
        assertEquals(1, order.getProducts().size());

        Product product = order.getProducts().get(0);
        assertEquals(500, product.getProductId());
        assertEquals(new BigDecimal("25.50"), product.getValue());
    }

    @Test
    public void testEmptyList_ReturnsEmptyResult() {
        SpyLogger logger = new SpyLogger();
        OrderNormalizationService service = createService(logger);

        List<User> users = service.normalizeOrders(Collections.emptyList());

        assertTrue(users.isEmpty());
        assertTrue(logger.errorMessages.isEmpty());
    }

    // ======================== AGRUPAMENTO - MESMO USER, ORDERS DIFERENTES ========================

    @Test
    public void testSameUser_DifferentOrders() {
        SpyLogger logger = new SpyLogger();
        OrderNormalizationService service = createService(logger);

        List<String> lines = List.of(
                buildLine("1", "Maria", "100", "500", "10.00", "20240101"),
                buildLine("1", "Maria", "200", "600", "20.00", "20240202")
        );

        List<User> users = service.normalizeOrders(lines);

        assertEquals(1, users.size());
        User user = users.get(0);
        assertEquals(1, user.getUserId());
        assertEquals(2, user.getOrders().size());

        assertEquals(100, user.getOrders().get(0).getOrderId());
        assertEquals(200, user.getOrders().get(1).getOrderId());
    }

    // ======================== AGRUPAMENTO - MESMA ORDER, PRODUCTS DIFERENTES ========================

    @Test
    public void testSameOrder_DifferentProducts() {
        SpyLogger logger = new SpyLogger();
        OrderNormalizationService service = createService(logger);

        List<String> lines = List.of(
                buildLine("1", "Pedro", "100", "500", "15.00", "20240101"),
                buildLine("1", "Pedro", "100", "600", "25.00", "20240101"),
                buildLine("1", "Pedro", "100", "700", "35.00", "20240101")
        );

        List<User> users = service.normalizeOrders(lines);

        assertEquals(1, users.size());
        User user = users.get(0);
        assertEquals(1, user.getOrders().size());

        Order order = user.getOrders().get(0);
        assertEquals(3, order.getProducts().size());
        assertEquals(500, order.getProducts().get(0).getProductId());
        assertEquals(600, order.getProducts().get(1).getProductId());
        assertEquals(700, order.getProducts().get(2).getProductId());
        assertEquals(new BigDecimal("75.00"), order.getTotal());
    }

    // ======================== AGREGAÇÃO - MÚLTIPLOS USERS ========================

    @Test
    public void testMultipleUsers_EachWithOwnOrders() {
        SpyLogger logger = new SpyLogger();
        OrderNormalizationService service = createService(logger);

        List<String> lines = List.of(
                buildLine("1", "Ana", "100", "500", "10.00", "20240101"),
                buildLine("2", "Bruno", "200", "600", "20.00", "20240202"),
                buildLine("3", "Carla", "300", "700", "30.00", "20240303")
        );

        List<User> users = service.normalizeOrders(lines);

        assertEquals(3, users.size());
        assertEquals(1, users.get(0).getUserId());
        assertEquals("Ana", users.get(0).getName());
        assertEquals(2, users.get(1).getUserId());
        assertEquals("Bruno", users.get(1).getName());
        assertEquals(3, users.get(2).getUserId());
        assertEquals("Carla", users.get(2).getName());
    }

    @Test
    public void testMultipleUsers_MultipleOrdersAndProducts() {
        SpyLogger logger = new SpyLogger();
        OrderNormalizationService service = createService(logger);

        List<String> lines = List.of(
                buildLine("1", "Ana", "100", "500", "10.00", "20240101"),
                buildLine("1", "Ana", "100", "501", "15.00", "20240101"),
                buildLine("1", "Ana", "101", "600", "20.00", "20240202"),
                buildLine("2", "Bruno", "200", "700", "30.00", "20240303"),
                buildLine("2", "Bruno", "200", "701", "40.00", "20240303")
        );

        List<User> users = service.normalizeOrders(lines);

        assertEquals(2, users.size());

        // User 1: 2 orders, order 100 tem 2 products, order 101 tem 1 product
        User ana = users.get(0);
        assertEquals(2, ana.getOrders().size());
        assertEquals(2, ana.getOrders().get(0).getProducts().size());
        assertEquals(new BigDecimal("25.00"), ana.getOrders().get(0).getTotal());
        assertEquals(1, ana.getOrders().get(1).getProducts().size());

        // User 2: 1 order com 2 products
        User bruno = users.get(1);
        assertEquals(1, bruno.getOrders().size());
        assertEquals(2, bruno.getOrders().get(0).getProducts().size());
        assertEquals(new BigDecimal("70.00"), bruno.getOrders().get(0).getTotal());
    }

    // ======================== AGREGAÇÃO - PRESERVAÇÃO DE ORDEM (LinkedHashMap) ========================

    @Test
    public void testInsertionOrder_UsersPreserved() {
        SpyLogger logger = new SpyLogger();
        OrderNormalizationService service = createService(logger);

        List<String> lines = List.of(
                buildLine("5", "Quinto", "100", "1", "10.00", "20240101"),
                buildLine("3", "Terceiro", "200", "2", "20.00", "20240101"),
                buildLine("1", "Primeiro", "300", "3", "30.00", "20240101"),
                buildLine("4", "Quarto", "400", "4", "40.00", "20240101"),
                buildLine("2", "Segundo", "500", "5", "50.00", "20240101")
        );

        List<User> users = service.normalizeOrders(lines);

        assertEquals(5, users.size());
        // Ordenado por user_id
        assertEquals(1, users.get(0).getUserId());
        assertEquals(2, users.get(1).getUserId());
        assertEquals(3, users.get(2).getUserId());
        assertEquals(4, users.get(3).getUserId());
        assertEquals(5, users.get(4).getUserId());
    }

    @Test
    public void testInsertionOrder_PreservedWithInterleavedUsers() {
        SpyLogger logger = new SpyLogger();
        OrderNormalizationService service = createService(logger);

        // User 2 aparece primeiro, depois User 1, depois User 2 de novo
        List<String> lines = List.of(
                buildLine("2", "Bruno", "200", "1", "10.00", "20240101"),
                buildLine("1", "Ana", "100", "2", "20.00", "20240101"),
                buildLine("2", "Bruno", "200", "3", "30.00", "20240101")
        );

        List<User> users = service.normalizeOrders(lines);

        assertEquals(2, users.size());
        // Ordenado por user_id → User 1 primeiro, depois User 2
        assertEquals(1, users.get(0).getUserId());
        assertEquals(2, users.get(1).getUserId());

        // User 2 deve ter 2 products na mesma order
        assertEquals(1, users.get(1).getOrders().size());
        assertEquals(2, users.get(1).getOrders().get(0).getProducts().size());
    }

    // ======================== RESILIÊNCIA - LINHAS INVÁLIDAS ========================

    @Test
    public void testInvalidLineInMiddle_SkippedWithoutStoppingProcessing() {
        SpyLogger logger = new SpyLogger();
        OrderNormalizationService service = createService(logger);

        List<String> lines = List.of(
                buildLine("1", "Ana", "100", "500", "10.00", "20240101"),
                "LINHA INVALIDA CURTA",
                buildLine("2", "Bruno", "200", "600", "20.00", "20240202")
        );

        List<User> users = service.normalizeOrders(lines);

        // As 2 linhas válidas devem ser processadas normalmente
        assertEquals(2, users.size());
        assertEquals(1, users.get(0).getUserId());
        assertEquals(2, users.get(1).getUserId());
    }

    @Test
    public void testMultipleInvalidLines_AllSkipped() {
        SpyLogger logger = new SpyLogger();
        OrderNormalizationService service = createService(logger);

        List<String> lines = List.of(
                "invalida1",
                buildLine("1", "Ana", "100", "500", "10.00", "20240101"),
                "invalida2",
                "invalida3",
                buildLine("2", "Bruno", "200", "600", "20.00", "20240202"),
                "invalida4"
        );

        List<User> users = service.normalizeOrders(lines);

        assertEquals(2, users.size());
    }

    @Test
    public void testAllInvalidLines_ReturnsEmptyList() {
        SpyLogger logger = new SpyLogger();
        OrderNormalizationService service = createService(logger);

        List<String> lines = List.of(
                "invalida1",
                "invalida2",
                "invalida3"
        );

        List<User> users = service.normalizeOrders(lines);

        assertTrue(users.isEmpty());
    }

    @Test
    public void testInvalidLine_DoesNotCorruptSurroundingData() {
        SpyLogger logger = new SpyLogger();
        OrderNormalizationService service = createService(logger);

        List<String> lines = List.of(
                buildLine("1", "Ana", "100", "500", "10.00", "20240101"),
                buildLine("1", "Ana", "100", "501", "15.00", "20240101"),
                "LINHA INVALIDA",
                buildLine("1", "Ana", "100", "502", "20.00", "20240101")
        );

        List<User> users = service.normalizeOrders(lines);

        assertEquals(1, users.size());
        User user = users.get(0);
        assertEquals(1, user.getOrders().size());
        // 3 products válidos na mesma order (a linha inválida foi ignorada)
        assertEquals(3, user.getOrders().get(0).getProducts().size());
        assertEquals(new BigDecimal("45.00"), user.getOrders().get(0).getTotal());
    }

    // ======================== MOCK/SPY - LOGGER ========================

    @Test
    public void testInvalidLine_LoggerErrorCalledWithLineNumber() {
        SpyLogger logger = new SpyLogger();
        OrderNormalizationService service = createService(logger);

        List<String> lines = List.of(
                buildLine("1", "Ana", "100", "500", "10.00", "20240101"),
                "LINHA INVALIDA"
        );

        service.normalizeOrders(lines);

        // Deve ter 2 mensagens de erro: a linha e os detalhes
        assertFalse(logger.errorMessages.isEmpty());
        // Primeira mensagem contém o número da linha (2) e o conteúdo
        assertTrue(logger.errorMessages.get(0).contains("linha 2"));
        assertTrue(logger.errorMessages.get(0).contains("LINHA INVALIDA"));
    }

    @Test
    public void testInvalidLine_LoggerErrorCalledWithDetails() {
        SpyLogger logger = new SpyLogger();
        OrderNormalizationService service = createService(logger);

        List<String> lines = List.of("LINHA INVALIDA");

        service.normalizeOrders(lines);

        // Deve ter exatamente 2 chamadas: "Erro ao processar linha..." e "Detalhes: ..."
        assertEquals(2, logger.errorMessages.size());
        assertTrue(logger.errorMessages.get(0).startsWith("Erro ao processar linha 1"));
        assertTrue(logger.errorMessages.get(1).startsWith("Detalhes:"));
    }

    @Test
    public void testMultipleInvalidLines_LoggerCalledForEach() {
        SpyLogger logger = new SpyLogger();
        OrderNormalizationService service = createService(logger);

        List<String> lines = List.of(
                "invalida1",
                buildLine("1", "Ana", "100", "500", "10.00", "20240101"),
                "invalida2"
        );

        service.normalizeOrders(lines);

        // 2 linhas inválidas × 2 mensagens cada = 4 mensagens de erro
        assertEquals(4, logger.errorMessages.size());
        assertTrue(logger.errorMessages.get(0).contains("linha 1"));
        assertTrue(logger.errorMessages.get(2).contains("linha 3"));
    }

    @Test
    public void testValidLines_NoErrorLogged() {
        SpyLogger logger = new SpyLogger();
        OrderNormalizationService service = createService(logger);

        List<String> lines = List.of(
                buildLine("1", "Ana", "100", "500", "10.00", "20240101"),
                buildLine("2", "Bruno", "200", "600", "20.00", "20240202")
        );

        service.normalizeOrders(lines);

        assertTrue(logger.errorMessages.isEmpty());
    }

    @Test
    public void testInvalidLineFirstPosition_LoggerShowsLine1() {
        SpyLogger logger = new SpyLogger();
        OrderNormalizationService service = createService(logger);

        List<String> lines = List.of("INVALIDA");

        service.normalizeOrders(lines);

        assertTrue(logger.errorMessages.get(0).contains("linha 1"));
    }

    @Test
    public void testInvalidLineLastPosition_LoggerShowsCorrectNumber() {
        SpyLogger logger = new SpyLogger();
        OrderNormalizationService service = createService(logger);

        List<String> lines = List.of(
                buildLine("1", "Ana", "100", "500", "10.00", "20240101"),
                buildLine("2", "Bruno", "200", "600", "20.00", "20240202"),
                buildLine("3", "Carla", "300", "700", "30.00", "20240303"),
                "INVALIDA NO FINAL"
        );

        service.normalizeOrders(lines);

        assertEquals(3, service.normalizeOrders(
                lines.subList(0, 3)).size()); // confirma que as 3 válidas funcionam

        assertTrue(logger.errorMessages.get(0).contains("linha 4"));
    }
}
