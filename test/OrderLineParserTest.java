import org.junit.Test;

import parser.OrderLine;
import parser.OrderLineParser;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class OrderLineParserTest {

    private final OrderLineParser parser = new OrderLineParser();

    // Campos fixos: userId(10) + name(45) + orderId(10) + productId(10) + value(12) + date(8) = 95 chars

    /**
     * Monta uma linha de 95 caracteres no formato fixo esperado pelo parser.
     */
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

    // ======================== PARSING - LINHA VÁLIDA ========================

    @Test
    public void testParseValidLine_AllFieldsExtractedCorrectly() {
        String line = buildLine("70", "Palmer Prosacco", "753", "3", "1836.74", "20210308");

        OrderLine result = parser.parse(line);

        assertEquals(70, result.userId());
        assertEquals("Palmer Prosacco", result.name());
        assertEquals(753, result.orderId());
        assertEquals(3, result.productId());
        assertEquals(new BigDecimal("1836.74"), result.value());
        assertEquals("2021-03-08", result.date());
    }

    @Test
    public void testParseValidLine_FromRealFile() {
        // Linha real do arquivo de entrada
        String line = "0000000002                                     Medeiros00000123450000000111      256.2420201201";

        OrderLine result = parser.parse(line);

        assertEquals(2, result.userId());
        assertEquals("Medeiros", result.name());
        assertEquals(12345, result.orderId());
        assertEquals(111, result.productId());
        assertEquals(new BigDecimal("256.24"), result.value());
        assertEquals("2020-12-01", result.date());
    }

    @Test
    public void testParseValidLine_SecondRealLine() {
        String line = "0000000001                                      Zarelli00000001230000000111      512.2420211201";

        OrderLine result = parser.parse(line);

        assertEquals(1, result.userId());
        assertEquals("Zarelli", result.name());
        assertEquals(123, result.orderId());
        assertEquals(111, result.productId());
        assertEquals(new BigDecimal("512.24"), result.value());
        assertEquals("2021-12-01", result.date());
    }

    // ======================== EXTRAÇÃO INDIVIDUAL DE CAMPOS ========================

    @Test
    public void testParse_UserId() {
        String line = buildLine("99", "Nome", "1", "1", "10.00", "20240101");
        assertEquals(99, parser.parse(line).userId());
    }

    @Test
    public void testParse_UserIdLargeNumber() {
        String line = buildLine("9999999999", "Nome", "1", "1", "10.00", "20240101");
        // parseInt pode não suportar > Integer.MAX_VALUE, mas 9999999999 > int max
        // Este valor ultrapassa int, espera-se NumberFormatException encapsulada
        try {
            parser.parse(line);
            fail("Deveria lançar exceção para userId maior que Integer.MAX_VALUE");
        } catch (NumberFormatException e) {
            // esperado
        }
    }

    @Test
    public void testParse_Name() {
        String line = buildLine("1", "Ana Maria da Silva", "1", "1", "10.00", "20240101");
        assertEquals("Ana Maria da Silva", parser.parse(line).name());
    }

    @Test
    public void testParse_OrderId() {
        String line = buildLine("1", "Nome", "5678", "1", "10.00", "20240101");
        assertEquals(5678, parser.parse(line).orderId());
    }

    @Test
    public void testParse_ProductId() {
        String line = buildLine("1", "Nome", "1", "9876", "10.00", "20240101");
        assertEquals(9876, parser.parse(line).productId());
    }

    @Test
    public void testParse_Value() {
        String line = buildLine("1", "Nome", "1", "1", "1500.99", "20240101");
        assertEquals(new BigDecimal("1500.99"), parser.parse(line).value());
    }

    @Test
    public void testParse_Date() {
        String line = buildLine("1", "Nome", "1", "1", "10.00", "20261231");
        assertEquals("2026-12-31", parser.parse(line).date());
    }

    // ======================== FORMATAÇÃO - formatDecimal (via parse) ========================

    @Test
    public void testFormatDecimal_IntegerValue_ScaledToTwoDecimals() {
        String line = buildLine("1", "Nome", "1", "1", "100", "20240101");
        assertEquals(new BigDecimal("100.00"), parser.parse(line).value());
    }

    @Test
    public void testFormatDecimal_OneDecimalPlace_ScaledToTwo() {
        String line = buildLine("1", "Nome", "1", "1", "99.5", "20240101");
        assertEquals(new BigDecimal("99.50"), parser.parse(line).value());
    }

    @Test
    public void testFormatDecimal_RoundingHalfUp() {
        // 10.555 com HALF_UP → 10.56
        String line = buildLine("1", "Nome", "1", "1", "10.555", "20240101");
        assertEquals(new BigDecimal("10.56"), parser.parse(line).value());
    }

    @Test
    public void testFormatDecimal_RoundingHalfUp_ExactHalf() {
        // 10.545 com HALF_UP → 10.55 (5 arredonda pra cima)
        String line = buildLine("1", "Nome", "1", "1", "10.545", "20240101");
        assertEquals(new BigDecimal("10.55"), parser.parse(line).value());
    }

    @Test
    public void testFormatDecimal_RoundingDown() {
        // 10.554 → 10.55 (arredonda pra baixo)
        String line = buildLine("1", "Nome", "1", "1", "10.554", "20240101");
        assertEquals(new BigDecimal("10.55"), parser.parse(line).value());
    }

    @Test
    public void testFormatDecimal_ManyDecimalPlaces() {
        // 99.999999 → 100.00
        String line = buildLine("1", "Nome", "1", "1", "99.999999", "20240101");
        assertEquals(new BigDecimal("100.00"), parser.parse(line).value());
    }

    @Test
    public void testFormatDecimal_ZeroValue() {
        String line = buildLine("1", "Nome", "1", "1", "0.00", "20240101");
        assertEquals(new BigDecimal("0.00"), parser.parse(line).value());
    }

    @Test
    public void testFormatDecimal_SmallestCent() {
        String line = buildLine("1", "Nome", "1", "1", "0.01", "20240101");
        assertEquals(new BigDecimal("0.01"), parser.parse(line).value());
    }

    // ======================== FORMATAÇÃO - formatDate (via parse) ========================

    @Test
    public void testFormatDate_StandardDate() {
        String line = buildLine("1", "Nome", "1", "1", "10.00", "20240315");
        assertEquals("2024-03-15", parser.parse(line).date());
    }

    @Test
    public void testFormatDate_FirstDayOfYear() {
        String line = buildLine("1", "Nome", "1", "1", "10.00", "20240101");
        assertEquals("2024-01-01", parser.parse(line).date());
    }

    @Test
    public void testFormatDate_LastDayOfYear() {
        String line = buildLine("1", "Nome", "1", "1", "10.00", "20241231");
        assertEquals("2024-12-31", parser.parse(line).date());
    }

    @Test
    public void testFormatDate_LeapYearDay() {
        String line = buildLine("1", "Nome", "1", "1", "10.00", "20240229");
        assertEquals("2024-02-29", parser.parse(line).date());
    }

    // ======================== VALIDAÇÃO / ERRO - LINHA NULL E CURTA ========================

    @Test(expected = IllegalArgumentException.class)
    public void testParse_NullLine_ThrowsException() {
        parser.parse(null);
    }

    @Test
    public void testParse_NullLine_ExceptionMessage() {
        try {
            parser.parse(null);
            fail("Deveria lançar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Linha inválida ou incompleta", e.getMessage());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_EmptyLine_ThrowsException() {
        parser.parse("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_ShortLine_ThrowsException() {
        parser.parse("curta demais");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_Line94Chars_ThrowsException() {
        // 94 chars (1 a menos que o mínimo de 95)
        String line = "a".repeat(94);
        parser.parse(line);
    }

    @Test
    public void testParse_Line95Chars_DoesNotThrowForLength() {
        // 95 chars é o mínimo — pode falhar no parsing de campos, mas não por comprimento
        String line = buildLine("1", "Nome", "1", "1", "10.00", "20240101");
        assertEquals(95, line.length());
        // deve parsear sem lançar "Linha inválida ou incompleta"
        OrderLine result = parser.parse(line);
        assertNotNull(result);
    }

    // ======================== VALIDAÇÃO / ERRO - VALOR MONETÁRIO ========================

    @Test(expected = IllegalArgumentException.class)
    public void testParse_EmptyValue_ThrowsException() {
        // Campo value com 12 espaços → trim() → vazio → Optional.empty → exceção
        String line = padLeft("1", 10)
                + padRight("Nome", 45)
                + padLeft("1", 10)
                + padLeft("1", 10)
                + "            "  // 12 espaços
                + "20240101";
        parser.parse(line);
    }

    @Test
    public void testParse_EmptyValue_ExceptionMessage() {
        String line = padLeft("1", 10)
                + padRight("Nome", 45)
                + padLeft("1", 10)
                + padLeft("1", 10)
                + "            "
                + "20240101";
        try {
            parser.parse(line);
            fail("Deveria lançar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Valor monetário inválido"));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_InvalidValue_Letters_ThrowsException() {
        String line = buildLine("1", "Nome", "1", "1", "abc", "20240101");
        parser.parse(line);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_InvalidValue_SpecialChars_ThrowsException() {
        String line = buildLine("1", "Nome", "1", "1", "12,50", "20240101");
        parser.parse(line);
    }

    // ======================== VALIDAÇÃO / ERRO - DATA INVÁLIDA ========================

    @Test(expected = IllegalArgumentException.class)
    public void testParse_DateTooShort_ThrowsException() {
        // Fecha com 7 chars + 1 espaço pra completar 8 posições, mas trim deixa com 7
        String line = padLeft("1", 10)
                + padRight("Nome", 45)
                + padLeft("1", 10)
                + padLeft("1", 10)
                + padLeft("10.00", 12)
                + "2024010 ";  // 7 dígitos + espaço = 8 chars, trim → 7
        parser.parse(line);
    }

    @Test
    public void testParse_DateTooShort_ExceptionMessage() {
        String line = padLeft("1", 10)
                + padRight("Nome", 45)
                + padLeft("1", 10)
                + padLeft("1", 10)
                + padLeft("10.00", 12)
                + "2024010 ";
        try {
            parser.parse(line);
            fail("Deveria lançar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Data inválida"));
        }
    }

    // ======================== VALIDAÇÃO / ERRO - CAMPO NUMÉRICO NÃO-NUMÉRICO ========================

    @Test(expected = NumberFormatException.class)
    public void testParse_NonNumericUserId_ThrowsException() {
        String line = buildLine("abc", "Nome", "1", "1", "10.00", "20240101");
        parser.parse(line);
    }

    @Test(expected = NumberFormatException.class)
    public void testParse_NonNumericOrderId_ThrowsException() {
        String line = buildLine("1", "Nome", "xyz", "1", "10.00", "20240101");
        parser.parse(line);
    }

    @Test(expected = NumberFormatException.class)
    public void testParse_NonNumericProductId_ThrowsException() {
        String line = buildLine("1", "Nome", "1", "foo", "10.00", "20240101");
        parser.parse(line);
    }

    // ======================== TESTES DE BORDA ========================

    @Test
    public void testParse_FieldsWithLeadingSpaces_TrimmedCorrectly() {
        // userId com zeros à esquerda (padrão do formato)
        String line = buildLine("0000000070", "Palmer Prosacco", "0000000753", "0000000003", "1836.74", "20210308");

        OrderLine result = parser.parse(line);

        assertEquals(70, result.userId());
        assertEquals("Palmer Prosacco", result.name());
        assertEquals(753, result.orderId());
        assertEquals(3, result.productId());
    }

    @Test
    public void testParse_NameWithOnlySpaces_TrimmedToEmpty() {
        // Nome com 45 espaços → trim → ""
        String line = padLeft("1", 10)
                + " ".repeat(45)
                + padLeft("1", 10)
                + padLeft("1", 10)
                + padLeft("10.00", 12)
                + "20240101";

        OrderLine result = parser.parse(line);
        assertEquals("", result.name());
    }

    @Test
    public void testParse_NameWithTrailingSpaces() {
        String line = buildLine("1", "João", "1", "1", "10.00", "20240101");
        // padRight adiciona espaços à direita; trim deve limpar
        assertEquals("João", parser.parse(line).name());
    }

    @Test
    public void testParse_MaxLengthName() {
        // Nome que ocupa todos os 45 caracteres
        String longName = "A".repeat(45);
        String line = padLeft("1", 10)
                + longName
                + padLeft("1", 10)
                + padLeft("1", 10)
                + padLeft("10.00", 12)
                + "20240101";

        assertEquals(longName, parser.parse(line).name());
    }

    @Test
    public void testParse_ValueWithManyDecimalPlaces_RoundedCorrectly() {
        // 123.456789 → 123.46 (HALF_UP)
        String line = buildLine("1", "Nome", "1", "1", "123.456789", "20240101");
        assertEquals(new BigDecimal("123.46"), parser.parse(line).value());
    }

    @Test
    public void testParse_ValueExactlyTwoDecimals_NoRounding() {
        String line = buildLine("1", "Nome", "1", "1", "50.25", "20240101");
        assertEquals(new BigDecimal("50.25"), parser.parse(line).value());
    }

    @Test
    public void testParse_LargeValue() {
        String line = buildLine("1", "Nome", "1", "1", "999999999.99", "20240101");
        assertEquals(new BigDecimal("999999999.99"), parser.parse(line).value());
    }

    @Test
    public void testParse_MinimumUserIdAndOrderId() {
        String line = buildLine("0", "Nome", "0", "0", "0.00", "20240101");
        OrderLine result = parser.parse(line);
        assertEquals(0, result.userId());
        assertEquals(0, result.orderId());
        assertEquals(0, result.productId());
    }

    @Test
    public void testParse_LineExactly95Chars() {
        String line = buildLine("1", "A", "2", "3", "10.00", "20240101");
        assertEquals(95, line.length());
        assertNotNull(parser.parse(line));
    }

    @Test
    public void testParse_LineLongerThan95Chars_ExtraIgnored() {
        // Linha com caracteres extras após os 95 — o parser lê apenas as posições fixas
        String line = buildLine("1", "Nome", "1", "1", "10.00", "20240101") + "EXTRA_CHARS";
        assertTrue(line.length() > 95);

        OrderLine result = parser.parse(line);
        assertEquals(1, result.userId());
        assertEquals("Nome", result.name());
    }
}
