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
    
    // ======================== TESTES DE SUCESSO - NORMALIZATION SERVICE ========================

    @Test
    public void testNormalizeOrdersEmptyList() {
        List<String> lines = new ArrayList<>();
        
        List<User> users = OrderNormalizationService.normalizeOrders(lines);
        
        assertEquals(0, users.size());
    }

}

