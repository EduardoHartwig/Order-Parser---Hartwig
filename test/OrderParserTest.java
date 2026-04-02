import org.junit.Before;
import org.junit.Test;

import domain.Order;
import domain.Product;
import domain.User;
import parser.OrderLineParser;
import service.OrderNormalizationService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class OrderParserTest {

    // ======================== TESTES DE SUCESSO - PRODUCT ========================
    
    @Test
    public void testProductCreation() {
        Product product = new Product(123, "50.00");
        assertEquals(123, product.getProductId());
        assertEquals("50.00", product.getValue());
    }

    @Test
    public void testProductWithDifferentValues() {
        Product product1 = new Product(1, "10.50");
        Product product2 = new Product(2, "99.99");
        Product product3 = new Product(3, "0.01");

        assertEquals("10.50", product1.getValue());
        assertEquals("99.99", product2.getValue());
        assertEquals("0.01", product3.getValue());
    }

    // ======================== TESTES DE SUCESSO - ORDER ========================
    
    @Test
    public void testOrderCreation() {
        Order order = new Order(456, "2024-03-15");
        assertEquals(456, order.getOrderId());
        assertEquals("2024-03-15", order.getDate());
        assertTrue(order.getProducts().isEmpty());
    }

    @Test
    public void testOrderAddProduct() {
        Order order = new Order(1, "2024-03-15");
        Product product = new Product(100, "25.50");
        
        order.addProduct(product);
        
        assertEquals(1, order.getProducts().size());
        assertEquals(product, order.getProducts().get(0));
    }

    @Test
    public void testOrderAddMultipleProducts() {
        Order order = new Order(1, "2024-03-15");
        Product product1 = new Product(100, "10.00");
        Product product2 = new Product(101, "20.00");
        Product product3 = new Product(102, "15.00");
        
        order.addProduct(product1);
        order.addProduct(product2);
        order.addProduct(product3);
        
        assertEquals(3, order.getProducts().size());
    }

    @Test
    public void testOrderTotalWithSingleProduct() {
        Order order = new Order(1, "2024-03-15");
        order.addProduct(new Product(100, "50.00"));
        
        assertEquals("50.00", order.getTotal());
    }

    @Test
    public void testOrderTotalWithMultipleProducts() {
        Order order = new Order(1, "2024-03-15");
        order.addProduct(new Product(100, "10.50"));
        order.addProduct(new Product(101, "20.30"));
        order.addProduct(new Product(102, "15.20"));
        
        assertEquals("46.00", order.getTotal());
    }

    @Test
    public void testOrderTotalWithZeroValues() {
        Order order = new Order(1, "2024-03-15");
        order.addProduct(new Product(100, "0.00"));
        order.addProduct(new Product(101, "0.00"));
        
        assertEquals("0.00", order.getTotal());
    }

    @Test
    public void testOrderTotalEmptyOrder() {
        Order order = new Order(1, "2024-03-15");
        assertEquals("0.00", order.getTotal());
    }

    // ======================== TESTES DE SUCESSO - USER ========================
    
    @Test
    public void testUserCreation() {
        User user = new User(789, "João Silva");
        assertEquals(789, user.getUserId());
        assertEquals("João Silva", user.getName());
        assertTrue(user.getOrders().isEmpty());
    }

    @Test
    public void testUserAddOrder() {
        User user = new User(1, "Maria Santos");
        Order order = new Order(100, "2024-03-15");
        
        user.addOrder(order);
        
        assertEquals(1, user.getOrders().size());
        assertEquals(order, user.getOrders().get(0));
    }

    @Test
    public void testUserAddMultipleOrders() {
        User user = new User(1, "Pedro Costa");
        Order order1 = new Order(100, "2024-03-15");
        Order order2 = new Order(101, "2024-03-16");
        Order order3 = new Order(102, "2024-03-17");
        
        user.addOrder(order1);
        user.addOrder(order2);
        user.addOrder(order3);
        
        assertEquals(3, user.getOrders().size());
    }

    @Test
    public void testUserWithMultipleOrdersAndProducts() {
        User user = new User(1, "Ana Oliveira");
        
        Order order1 = new Order(100, "2024-03-15");
        order1.addProduct(new Product(1001, "50.00"));
        order1.addProduct(new Product(1002, "30.00"));
        
        Order order2 = new Order(101, "2024-03-16");
        order2.addProduct(new Product(1003, "100.00"));
        
        user.addOrder(order1);
        user.addOrder(order2);
        
        assertEquals(2, user.getOrders().size());
        assertEquals("80.00", order1.getTotal());
        assertEquals("100.00", order2.getTotal());
    }

    // ======================== TESTES DE SUCESSO - PARSER ========================
    
    @Test
    public void testOrderLineParserValidLine() {
        // Linha com 95 caracteres (10+45+10+10+12+8)
        String line = "1         João Silva                    100       1001      50.00       20240315";
        OrderLineParser.OrderLine result = OrderLineParser.parse(line);
        
        assertEquals(1, result.userId);
        assertEquals("João Silva", result.name);
        assertEquals(100, result.orderId);
        assertEquals(1001, result.productId);
        assertEquals("50.00", result.value);
        assertEquals("2024-03-15", result.date);
    }

    @Test
    public void testOrderLineParserWithSpaces() {
        String line = "   5    João da Silva Santos Ferreira   200       2001      125.50      20240410";
        OrderLineParser.OrderLine result = OrderLineParser.parse(line);
        
        assertEquals(5, result.userId);
        assertEquals("João da Silva Santos Ferreira", result.name);
        assertEquals(200, result.orderId);
        assertEquals(2001, result.productId);
        assertEquals("125.50", result.value);
        assertEquals("2024-04-10", result.date);
    }

    @Test
    public void testOrderLineParserDateFormat() {
        String line = "1         Maria                       100       1001      50.00       20240515";
        OrderLineParser.OrderLine result = OrderLineParser.parse(line);
        
        assertEquals("2024-05-15", result.date);
    }

    @Test
    public void testOrderLineParserDecimalFormat() {
        String line = "1         Test User                    100       1001      99.99       20240315";
        OrderLineParser.OrderLine result = OrderLineParser.parse(line);
        
        assertEquals("99.99", result.value);
    }

    @Test
    public void testOrderLineParserSmallValue() {
        String line = "1         Test User                    100       1001      0.01        20240315";
        OrderLineParser.OrderLine result = OrderLineParser.parse(line);
        
        assertEquals("0.01", result.value);
    }

    @Test
    public void testOrderLineParserZeroValue() {
        String line = "1         Test User                    100       1001      0.00        20240315";
        OrderLineParser.OrderLine result = OrderLineParser.parse(line);
        
        assertEquals("0.00", result.value);
    }

    // ======================== TESTES DE SUCESSO - NORMALIZATION SERVICE ========================
    
    @Test
    public void testNormalizeOrdersSingleLine() {
        List<String> lines = Arrays.asList(
            "1         João Silva                    100       1001      50.00       20240315"
        );
        
        List<User> users = OrderNormalizationService.normalizeOrders(lines);
        
        assertEquals(1, users.size());
        User user = users.get(0);
        assertEquals(1, user.getUserId());
        assertEquals("João Silva", user.getName());
        assertEquals(1, user.getOrders().size());
    }

    @Test
    public void testNormalizeOrdersMultipleLinesMultipleProducts() {
        List<String> lines = Arrays.asList(
            "1         João Silva                    100       1001      50.00       20240315",
            "1         João Silva                    100       1002      30.00       20240315"
        );
        
        List<User> users = OrderNormalizationService.normalizeOrders(lines);
        
        assertEquals(1, users.size());
        User user = users.get(0);
        assertEquals(1, user.getOrders().size());
        Order order = user.getOrders().get(0);
        assertEquals(2, order.getProducts().size());
        assertEquals("80.00", order.getTotal());
    }

    @Test
    public void testNormalizeOrdersMultipleUsers() {
        List<String> lines = Arrays.asList(
            "1         João Silva                    100       1001      50.00       20240315",
            "2         Maria Santos                  101       2001      100.00      20240316"
        );
        
        List<User> users = OrderNormalizationService.normalizeOrders(lines);
        
        assertEquals(2, users.size());
        assertEquals("João Silva", users.get(0).getName());
        assertEquals("Maria Santos", users.get(1).getName());
    }

    @Test
    public void testNormalizeOrdersMultipleUsersMultipleOrders() {
        List<String> lines = Arrays.asList(
            "1         João Silva                    100       1001      50.00       20240315",
            "1         João Silva                    101       1002      30.00       20240316",
            "2         Maria Santos                  102       2001      100.00      20240315",
            "2         Maria Santos                  102       2002      50.00       20240315"
        );
        
        List<User> users = OrderNormalizationService.normalizeOrders(lines);
        
        assertEquals(2, users.size());
        
        User user1 = users.get(0);
        assertEquals(1, user1.getUserId());
        assertEquals(2, user1.getOrders().size());
        assertEquals("50.00", user1.getOrders().get(0).getTotal());
        assertEquals("30.00", user1.getOrders().get(1).getTotal());
        
        User user2 = users.get(1);
        assertEquals(2, user2.getUserId());
        assertEquals(1, user2.getOrders().size());
        assertEquals("150.00", user2.getOrders().get(0).getTotal());
    }

    @Test
    public void testNormalizeOrdersPreservesUserOrder() {
        List<String> lines = Arrays.asList(
            "3         User Three                    100       1001      50.00       20240315",
            "1         User One                      101       2001      100.00      20240316",
            "2         User Two                      102       3001      75.00       20240317",
            "1         User One                      101       2002      50.00       20240316"
        );
        
        List<User> users = OrderNormalizationService.normalizeOrders(lines);
        
        assertEquals(3, users.size());
        assertEquals(3, users.get(0).getUserId());
        assertEquals(1, users.get(1).getUserId());
        assertEquals(2, users.get(2).getUserId());
    }

    @Test
    public void testNormalizeOrdersEmptyList() {
        List<String> lines = new ArrayList<>();
        
        List<User> users = OrderNormalizationService.normalizeOrders(lines);
        
        assertEquals(0, users.size());
    }

    @Test
    public void testNormalizeOrdersWithComplexData() {
        List<String> lines = Arrays.asList(
            "1         João Silva da Silva           100       1001      150.50      20240315",
            "1         João Silva da Silva           100       1002      250.75      20240315",
            "1         João Silva da Silva           101       1003      99.99       20240316",
            "2         Maria da Conceição Santos     102       2001      500.00      20240315",
            "2         Maria da Conceição Santos     102       2002      249.50      20240315"
        );
        
        List<User> users = OrderNormalizationService.normalizeOrders(lines);
        
        assertEquals(2, users.size());
        
        User user1 = users.get(0);
        assertEquals("João Silva da Silva", user1.getName());
        assertEquals(2, user1.getOrders().size());
        assertEquals("401.25", user1.getOrders().get(0).getTotal());
        assertEquals("99.99", user1.getOrders().get(1).getTotal());
        
        User user2 = users.get(1);
        assertEquals("Maria da Conceição Santos", user2.getName());
        assertEquals(1, user2.getOrders().size());
        assertEquals("749.50", user2.getOrders().get(0).getTotal());
    }

}

