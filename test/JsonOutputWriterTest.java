import org.junit.Test;
import org.junit.After;

import domain.Order;
import domain.Product;
import domain.User;
import io.JsonOutputWriter;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class JsonOutputWriterTest {

    private final JsonOutputWriter writer = new JsonOutputWriter();
    private Path tempFile;

    @After
    public void cleanup() throws IOException {
        if (tempFile != null && Files.exists(tempFile)) {
            Files.delete(tempFile);
        }
    }

    // ======================== HELPERS ========================

    private User createUser(int id, String name) {
        return new User(id, name);
    }

    private Order createOrder(int id, String date, BigDecimal... productValues) {
        Order order = new Order(id, date);
        for (int i = 0; i < productValues.length; i++) {
            order.addProduct(new Product(i + 1, productValues[i]));
        }
        return order;
    }

    // ======================== SERIALIZAÇÃO - LISTA VAZIA ========================

    @Test
    public void testBuildJsonString_EmptyList() {
        String json = writer.buildJsonString(Collections.emptyList());
        assertEquals("[\n\n]", json);
    }

    // ======================== SERIALIZAÇÃO - 1 USER ========================

    @Test
    public void testBuildJsonString_SingleUserSingleOrderSingleProduct() {
        User user = createUser(1, "Ana");
        Order order = createOrder(100, "2024-03-15", new BigDecimal("50.00"));
        user.addOrder(order);

        String json = writer.buildJsonString(List.of(user));

        assertTrue(json.startsWith("["));
        assertTrue(json.endsWith("]"));
        assertTrue(json.contains("\"user_id\": 1"));
        assertTrue(json.contains("\"name\": \"Ana\""));
        assertTrue(json.contains("\"order_id\": 100"));
        assertTrue(json.contains("\"total\": \"50.00\""));
        assertTrue(json.contains("\"date\": \"2024-03-15\""));
        assertTrue(json.contains("\"product_id\": 1"));
        assertTrue(json.contains("\"value\": \"50.00\""));
    }

    @Test
    public void testBuildJsonString_SingleUserMultipleProducts() {
        User user = createUser(1, "Bruno");
        Order order = createOrder(100, "2024-01-01",
                new BigDecimal("10.00"), new BigDecimal("20.00"), new BigDecimal("30.00"));
        user.addOrder(order);

        String json = writer.buildJsonString(List.of(user));

        assertTrue(json.contains("\"total\": \"60.00\""));
        assertTrue(json.contains("\"product_id\": 1"));
        assertTrue(json.contains("\"product_id\": 2"));
        assertTrue(json.contains("\"product_id\": 3"));
    }

    @Test
    public void testBuildJsonString_SingleUserMultipleOrders() {
        User user = createUser(1, "Carla");
        Order order1 = createOrder(100, "2024-01-01", new BigDecimal("10.00"));
        Order order2 = createOrder(200, "2024-02-02", new BigDecimal("20.00"));
        user.addOrder(order1);
        user.addOrder(order2);

        String json = writer.buildJsonString(List.of(user));

        assertTrue(json.contains("\"order_id\": 100"));
        assertTrue(json.contains("\"order_id\": 200"));
        assertTrue(json.contains("\"total\": \"10.00\""));
        assertTrue(json.contains("\"total\": \"20.00\""));
    }

    // ======================== SERIALIZAÇÃO - MÚLTIPLOS USERS ========================

    @Test
    public void testBuildJsonString_MultipleUsers() {
        User user1 = createUser(1, "Ana");
        user1.addOrder(createOrder(100, "2024-01-01", new BigDecimal("10.00")));

        User user2 = createUser(2, "Bruno");
        user2.addOrder(createOrder(200, "2024-02-02", new BigDecimal("20.00")));

        String json = writer.buildJsonString(List.of(user1, user2));

        assertTrue(json.contains("\"user_id\": 1"));
        assertTrue(json.contains("\"name\": \"Ana\""));
        assertTrue(json.contains("\"user_id\": 2"));
        assertTrue(json.contains("\"name\": \"Bruno\""));
        // Verifica que user1 aparece antes de user2 no JSON
        assertTrue(json.indexOf("\"user_id\": 1") < json.indexOf("\"user_id\": 2"));
    }

    @Test
    public void testBuildJsonString_MatchesExpectedFormat() {
        // Reproduz os dados do arquivo de saída real (Primeiro arquivo)
        User medeiros = createUser(2, "Medeiros");
        Order orderMed = new Order(12345, "2020-12-01");
        orderMed.addProduct(new Product(111, new BigDecimal("256.24")));
        orderMed.addProduct(new Product(122, new BigDecimal("256.24")));
        medeiros.addOrder(orderMed);

        User zarelli = createUser(1, "Zarelli");
        Order orderZar = new Order(123, "2021-12-01");
        orderZar.addProduct(new Product(111, new BigDecimal("512.24")));
        orderZar.addProduct(new Product(122, new BigDecimal("512.24")));
        zarelli.addOrder(orderZar);

        String json = writer.buildJsonString(List.of(medeiros, zarelli));

        String expected = "[\n"
                + "  {\n"
                + "    \"user_id\": 2,\n"
                + "    \"name\": \"Medeiros\",\n"
                + "    \"orders\": [\n"
                + "      {\n"
                + "        \"order_id\": 12345,\n"
                + "        \"total\": \"512.48\",\n"
                + "        \"date\": \"2020-12-01\",\n"
                + "        \"products\": [\n"
                + "          { \"product_id\": 111, \"value\": \"256.24\" },\n"
                + "          { \"product_id\": 122, \"value\": \"256.24\" }\n"
                + "        ]\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"user_id\": 1,\n"
                + "    \"name\": \"Zarelli\",\n"
                + "    \"orders\": [\n"
                + "      {\n"
                + "        \"order_id\": 123,\n"
                + "        \"total\": \"1024.48\",\n"
                + "        \"date\": \"2021-12-01\",\n"
                + "        \"products\": [\n"
                + "          { \"product_id\": 111, \"value\": \"512.24\" },\n"
                + "          { \"product_id\": 122, \"value\": \"512.24\" }\n"
                + "        ]\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "]";

        assertEquals(expected, json);
    }

    @Test
    public void testBuildJsonString_UserWithNoOrders() {
        User user = createUser(1, "Vazio");
        // Não adiciona nenhuma order

        String json = writer.buildJsonString(List.of(user));

        assertTrue(json.contains("\"user_id\": 1"));
        assertTrue(json.contains("\"name\": \"Vazio\""));
        assertTrue(json.contains("\"orders\": [\n\n    ]"));
    }

    // ======================== ESCAPE DE CARACTERES ESPECIAIS ========================

    @Test
    public void testBuildJsonString_NameWithDoubleQuotes() {
        User user = createUser(1, "Jo\"ao");
        user.addOrder(createOrder(100, "2024-01-01", new BigDecimal("10.00")));

        String json = writer.buildJsonString(List.of(user));

        assertTrue(json.contains("\"name\": \"Jo\\\"ao\""));
    }

    @Test
    public void testBuildJsonString_NameWithBackslash() {
        User user = createUser(1, "Jo\\ao");
        user.addOrder(createOrder(100, "2024-01-01", new BigDecimal("10.00")));

        String json = writer.buildJsonString(List.of(user));

        assertTrue(json.contains("\"name\": \"Jo\\\\ao\""));
    }

    @Test
    public void testBuildJsonString_NameWithNewline() {
        User user = createUser(1, "Jo\nao");
        user.addOrder(createOrder(100, "2024-01-01", new BigDecimal("10.00")));

        String json = writer.buildJsonString(List.of(user));

        assertTrue(json.contains("\"name\": \"Jo\\nao\""));
    }

    @Test
    public void testBuildJsonString_NameWithTab() {
        User user = createUser(1, "Jo\tao");
        user.addOrder(createOrder(100, "2024-01-01", new BigDecimal("10.00")));

        String json = writer.buildJsonString(List.of(user));

        assertTrue(json.contains("\"name\": \"Jo\\tao\""));
    }

    @Test
    public void testBuildJsonString_NameWithCarriageReturn() {
        User user = createUser(1, "Jo\rao");
        user.addOrder(createOrder(100, "2024-01-01", new BigDecimal("10.00")));

        String json = writer.buildJsonString(List.of(user));

        assertTrue(json.contains("\"name\": \"Jo\\rao\""));
    }

    @Test
    public void testBuildJsonString_NameWithBackspace() {
        User user = createUser(1, "Jo\bao");
        user.addOrder(createOrder(100, "2024-01-01", new BigDecimal("10.00")));

        String json = writer.buildJsonString(List.of(user));

        assertTrue(json.contains("\"name\": \"Jo\\bao\""));
    }

    @Test
    public void testBuildJsonString_NameWithFormFeed() {
        User user = createUser(1, "Jo\fao");
        user.addOrder(createOrder(100, "2024-01-01", new BigDecimal("10.00")));

        String json = writer.buildJsonString(List.of(user));

        assertTrue(json.contains("\"name\": \"Jo\\fao\""));
    }

    @Test
    public void testBuildJsonString_NameWithMultipleSpecialChars() {
        User user = createUser(1, "A\"B\\C\nD\tE");
        user.addOrder(createOrder(100, "2024-01-01", new BigDecimal("10.00")));

        String json = writer.buildJsonString(List.of(user));

        assertTrue(json.contains("\"name\": \"A\\\"B\\\\C\\nD\\tE\""));
    }

    // ======================== FORMATO NUMÉRICO - BigDecimal.toPlainString() ========================

    @Test
    public void testBuildJsonString_ValueNoScientificNotation_SmallValue() {
        User user = createUser(1, "Test");
        Order order = new Order(1, "2024-01-01");
        order.addProduct(new Product(1, new BigDecimal("0.01")));
        user.addOrder(order);

        String json = writer.buildJsonString(List.of(user));

        assertTrue(json.contains("\"value\": \"0.01\""));
        assertFalse(json.contains("E"));
    }

    @Test
    public void testBuildJsonString_ValueNoScientificNotation_LargeValue() {
        User user = createUser(1, "Test");
        Order order = new Order(1, "2024-01-01");
        order.addProduct(new Product(1, new BigDecimal("99999999.99")));
        user.addOrder(order);

        String json = writer.buildJsonString(List.of(user));

        assertTrue(json.contains("\"value\": \"99999999.99\""));
        assertFalse(json.matches(".*\\d[eE][+-]?\\d.*"));
    }

    @Test
    public void testBuildJsonString_TotalNoScientificNotation() {
        User user = createUser(1, "Test");
        Order order = new Order(1, "2024-01-01");
        order.addProduct(new Product(1, new BigDecimal("50000.50")));
        order.addProduct(new Product(2, new BigDecimal("50000.50")));
        user.addOrder(order);

        String json = writer.buildJsonString(List.of(user));

        assertTrue(json.contains("\"total\": \"100001.00\""));
    }

    @Test
    public void testBuildJsonString_ValueZero() {
        User user = createUser(1, "Test");
        Order order = new Order(1, "2024-01-01");
        order.addProduct(new Product(1, new BigDecimal("0.00")));
        user.addOrder(order);

        String json = writer.buildJsonString(List.of(user));

        assertTrue(json.contains("\"value\": \"0.00\""));
        assertTrue(json.contains("\"total\": \"0.00\""));
    }

    @Test
    public void testBuildJsonString_ValueAlwaysTwoDecimalPlaces() {
        User user = createUser(1, "Test");
        Order order = new Order(1, "2024-01-01");
        // BigDecimal com scale=2 → "10.00", não "10"
        order.addProduct(new Product(1, new BigDecimal("10.00")));
        user.addOrder(order);

        String json = writer.buildJsonString(List.of(user));

        assertTrue(json.contains("\"value\": \"10.00\""));
        assertTrue(json.contains("\"total\": \"10.00\""));
    }

    // ======================== INTEGRAÇÃO I/O - writeToFile ========================

    @Test
    public void testWriteToFile_CreatesFileWithCorrectContent() throws IOException {
        tempFile = Files.createTempFile("json-test-", ".json");

        User user = createUser(1, "Ana");
        user.addOrder(createOrder(100, "2024-01-01", new BigDecimal("25.50")));

        writer.writeToFile(tempFile.toString(), List.of(user));

        assertTrue(Files.exists(tempFile));
        String content = Files.readString(tempFile, StandardCharsets.UTF_8);
        String expected = writer.buildJsonString(List.of(user));
        assertEquals(expected, content);
    }

    @Test
    public void testWriteToFile_EmptyList() throws IOException {
        tempFile = Files.createTempFile("json-test-", ".json");

        writer.writeToFile(tempFile.toString(), Collections.emptyList());

        String content = Files.readString(tempFile, StandardCharsets.UTF_8);
        assertEquals("[\n\n]", content);
    }

    @Test
    public void testWriteToFile_MultipleUsersRoundTrip() throws IOException {
        tempFile = Files.createTempFile("json-test-", ".json");

        User user1 = createUser(1, "Ana");
        user1.addOrder(createOrder(100, "2024-01-01", new BigDecimal("10.00"), new BigDecimal("20.00")));

        User user2 = createUser(2, "Bruno");
        user2.addOrder(createOrder(200, "2024-02-02", new BigDecimal("30.00")));

        List<User> users = List.of(user1, user2);

        writer.writeToFile(tempFile.toString(), users);

        String written = Files.readString(tempFile, StandardCharsets.UTF_8);
        String expected = writer.buildJsonString(users);
        assertEquals(expected, written);
    }

    @Test
    public void testWriteToFile_Utf8Encoding() throws IOException {
        tempFile = Files.createTempFile("json-test-", ".json");

        User user = createUser(1, "João André");
        user.addOrder(createOrder(100, "2024-01-01", new BigDecimal("10.00")));

        writer.writeToFile(tempFile.toString(), List.of(user));

        String content = Files.readString(tempFile, StandardCharsets.UTF_8);
        assertTrue(content.contains("João André"));
    }

    @Test
    public void testWriteToFile_SpecialCharsEscaped() throws IOException {
        tempFile = Files.createTempFile("json-test-", ".json");

        User user = createUser(1, "Te\"st\\Name");
        user.addOrder(createOrder(100, "2024-01-01", new BigDecimal("10.00")));

        writer.writeToFile(tempFile.toString(), List.of(user));

        String content = Files.readString(tempFile, StandardCharsets.UTF_8);
        assertTrue(content.contains("Te\\\"st\\\\Name"));
    }

    // ======================== getFileExtension ========================

    @Test
    public void testGetFileExtension() {
        assertEquals(".json", writer.getFileExtension());
    }
}
