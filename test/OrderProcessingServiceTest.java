import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import domain.User;
import io.JsonOutputWriter;
import io.InputReader;
import service.NormalizationService;
import service.OrderProcessingService;
import service.ProcessingLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.*;

public class OrderProcessingServiceTest {

    private Path tempDir;

    @Before
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("processing-test-");
    }

    @After
    public void tearDown() throws IOException {
        if (tempDir != null && Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    // ======================== STUBS / FAKES ========================

    private static class StubInputReader implements InputReader {
        private final List<String> lines;
        private IOException exceptionToThrow;
        int callCount = 0;

        StubInputReader(List<String> lines) {
            this.lines = lines;
        }

        StubInputReader(IOException exceptionToThrow) {
            this.lines = null;
            this.exceptionToThrow = exceptionToThrow;
        }

        @Override
        public List<String> readFile(String filePath) throws IOException {
            callCount++;
            if (exceptionToThrow != null) {
                throw exceptionToThrow;
            }
            return lines;
        }
    }

    private static class StubNormalizationService implements NormalizationService {
        private final List<User> users;
        int callCount = 0;

        StubNormalizationService(List<User> users) {
            this.users = users;
        }

        @Override
        public List<User> normalizeOrders(List<String> lines) {
            callCount++;
            return users;
        }
    }

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

    /**
     * InputReader que lança IOException apenas para caminhos específicos.
     */
    private static class SelectiveInputReader implements InputReader {
        private final List<String> failPaths;
        private final List<String> lines;
        int callCount = 0;

        SelectiveInputReader(List<String> lines, List<String> failPaths) {
            this.lines = lines;
            this.failPaths = failPaths;
        }

        @Override
        public List<String> readFile(String filePath) throws IOException {
            callCount++;
            for (String failPath : failPaths) {
                if (filePath.contains(failPath)) {
                    throw new IOException("Erro simulado: " + filePath);
                }
            }
            return lines;
        }
    }

    // ======================== HELPERS ========================

    private File createTxtFile(String name) throws IOException {
        Path file = tempDir.resolve(name);
        Files.createFile(file);
        return file.toFile();
    }

    private OrderProcessingService createService(InputReader reader, NormalizationService normalizer, SpyLogger logger) {
        return new OrderProcessingService(reader, normalizer, new JsonOutputWriter(), logger);
    }

    // ======================== TESTES DE ORQUESTRAÇÃO ========================

    @Test
    public void testProcessFiles_CallsReadNormalizeWriteInSequence() throws IOException {
        File txtFile = createTxtFile("dados.txt");

        User user = new User(1, "Ana");
        List<String> lines = List.of("line1", "line2");

        StubInputReader reader = new StubInputReader(lines);
        StubNormalizationService normalizer = new StubNormalizationService(List.of(user));
        SpyLogger logger = new SpyLogger();

        OrderProcessingService service = createService(reader, normalizer, logger);
        service.processFiles(new File[]{txtFile}, tempDir.toString());

        assertEquals(1, reader.callCount);
        assertEquals(1, normalizer.callCount);
        // Logger deve ter registrado: Processando, linhas lidas, usuarios, pedidos, arquivo gerado, linha vazia
        assertTrue(logger.infoMessages.stream().anyMatch(m -> m.contains("Processando")));
        assertTrue(logger.infoMessages.stream().anyMatch(m -> m.contains("linhas lidas")));
        assertTrue(logger.infoMessages.stream().anyMatch(m -> m.contains("usuario(s)")));
        assertTrue(logger.infoMessages.stream().anyMatch(m -> m.contains("pedido(s)")));
        assertTrue(logger.infoMessages.stream().anyMatch(m -> m.contains("Arquivo gerado")));
    }

    @Test
    public void testProcessFiles_MultipleFiles_EachProcessedIndependently() throws IOException {
        File file1 = createTxtFile("a.txt");
        File file2 = createTxtFile("b.txt");
        File file3 = createTxtFile("c.txt");

        StubInputReader reader = new StubInputReader(Collections.emptyList());
        StubNormalizationService normalizer = new StubNormalizationService(Collections.emptyList());
        SpyLogger logger = new SpyLogger();

        OrderProcessingService service = createService(reader, normalizer, logger);
        service.processFiles(new File[]{file1, file2, file3}, tempDir.toString());

        assertEquals(3, reader.callCount);
        assertEquals(3, normalizer.callCount);
    }

    // ======================== TESTES DE CONTAGEM ========================

    @Test
    public void testProcessFiles_AllSuccess_ReturnsCorrectCount() throws IOException {
        File file1 = createTxtFile("a.txt");
        File file2 = createTxtFile("b.txt");

        StubInputReader reader = new StubInputReader(Collections.emptyList());
        StubNormalizationService normalizer = new StubNormalizationService(Collections.emptyList());
        SpyLogger logger = new SpyLogger();

        OrderProcessingService service = createService(reader, normalizer, logger);
        int count = service.processFiles(new File[]{file1, file2}, tempDir.toString());

        assertEquals(2, count);
    }

    @Test
    public void testProcessFiles_AllFail_ReturnsZero() throws IOException {
        File file1 = createTxtFile("a.txt");
        File file2 = createTxtFile("b.txt");

        StubInputReader reader = new StubInputReader(new IOException("falha"));
        StubNormalizationService normalizer = new StubNormalizationService(Collections.emptyList());
        SpyLogger logger = new SpyLogger();

        OrderProcessingService service = createService(reader, normalizer, logger);
        int count = service.processFiles(new File[]{file1, file2}, tempDir.toString());

        assertEquals(0, count);
    }

    @Test
    public void testProcessFiles_SomeFail_ReturnsOnlySuccessCount() throws IOException {
        File fileOk = createTxtFile("ok.txt");
        File fileFail = createTxtFile("fail.txt");

        SelectiveInputReader reader = new SelectiveInputReader(
                Collections.emptyList(), List.of("fail.txt"));
        StubNormalizationService normalizer = new StubNormalizationService(Collections.emptyList());
        SpyLogger logger = new SpyLogger();

        OrderProcessingService service = createService(reader, normalizer, logger);
        int count = service.processFiles(new File[]{fileOk, fileFail}, tempDir.toString());

        assertEquals(1, count);
    }

    @Test
    public void testProcessFiles_EmptyArray_ReturnsZero() {
        StubInputReader reader = new StubInputReader(Collections.emptyList());
        StubNormalizationService normalizer = new StubNormalizationService(Collections.emptyList());
        SpyLogger logger = new SpyLogger();

        OrderProcessingService service = createService(reader, normalizer, logger);
        int count = service.processFiles(new File[]{}, tempDir.toString());

        assertEquals(0, count);
        assertTrue(logger.infoMessages.isEmpty());
        assertTrue(logger.errorMessages.isEmpty());
    }

    // ======================== TESTES DE RESILIÊNCIA ========================

    @Test
    public void testProcessFiles_IOExceptionOnOneFile_OthersStillProcessed() throws IOException {
        File file1 = createTxtFile("primeiro.txt");
        File fileBad = createTxtFile("falha.txt");
        File file3 = createTxtFile("terceiro.txt");

        SelectiveInputReader reader = new SelectiveInputReader(
                Collections.emptyList(), List.of("falha.txt"));
        StubNormalizationService normalizer = new StubNormalizationService(Collections.emptyList());
        SpyLogger logger = new SpyLogger();

        OrderProcessingService service = createService(reader, normalizer, logger);
        int count = service.processFiles(new File[]{file1, fileBad, file3}, tempDir.toString());

        assertEquals(2, count);
        assertEquals(3, reader.callCount);
    }

    @Test
    public void testProcessFiles_IOException_LogsError() throws IOException {
        File file = createTxtFile("falha.txt");

        StubInputReader reader = new StubInputReader(new IOException("disco cheio"));
        StubNormalizationService normalizer = new StubNormalizationService(Collections.emptyList());
        SpyLogger logger = new SpyLogger();

        OrderProcessingService service = createService(reader, normalizer, logger);
        service.processFiles(new File[]{file}, tempDir.toString());

        assertFalse(logger.errorMessages.isEmpty());
        assertTrue(logger.errorMessages.get(0).contains("Erro ao processar arquivo"));
        assertTrue(logger.errorMessages.get(0).contains("disco cheio"));
    }

    @Test
    public void testProcessFiles_GenericException_LogsGeneralError() throws IOException {
        File file = createTxtFile("dados.txt");

        // NormalizationService que lança RuntimeException
        NormalizationService badNormalizer = lines -> {
            throw new RuntimeException("erro inesperado");
        };

        StubInputReader reader = new StubInputReader(Collections.emptyList());
        SpyLogger logger = new SpyLogger();

        OrderProcessingService service = new OrderProcessingService(reader, badNormalizer, new JsonOutputWriter(), logger);
        int count = service.processFiles(new File[]{file}, tempDir.toString());

        assertEquals(0, count);
        assertFalse(logger.errorMessages.isEmpty());
        assertTrue(logger.errorMessages.get(0).contains("Erro geral"));
        assertTrue(logger.errorMessages.get(0).contains("erro inesperado"));
    }

    @Test
    public void testProcessFiles_FailureInMiddle_CountIsCorrect() throws IOException {
        File f1 = createTxtFile("a.txt");
        File f2 = createTxtFile("b.txt");
        File f3 = createTxtFile("c.txt");
        File f4 = createTxtFile("d.txt");

        SelectiveInputReader reader = new SelectiveInputReader(
                Collections.emptyList(), List.of("b.txt", "c.txt"));
        StubNormalizationService normalizer = new StubNormalizationService(Collections.emptyList());
        SpyLogger logger = new SpyLogger();

        OrderProcessingService service = createService(reader, normalizer, logger);
        int count = service.processFiles(new File[]{f1, f2, f3, f4}, tempDir.toString());

        assertEquals(2, count);
        assertEquals(2, logger.errorMessages.size());
    }

    // ======================== TESTES DE NOME DO ARQUIVO DE SAÍDA ========================

    @Test
    public void testProcessFiles_OutputFileNameFormat() throws IOException {
        File txtFile = createTxtFile("Primeiro arquivo.txt");

        StubInputReader reader = new StubInputReader(Collections.emptyList());
        StubNormalizationService normalizer = new StubNormalizationService(Collections.emptyList());
        SpyLogger logger = new SpyLogger();

        OrderProcessingService service = createService(reader, normalizer, logger);
        service.processFiles(new File[]{txtFile}, tempDir.toString());

        String expectedName = "Primeiro arquivo - Arquivo de saída.json";
        assertTrue(logger.infoMessages.stream()
                .anyMatch(m -> m.contains(expectedName)));
    }

    @Test
    public void testProcessFiles_OutputFileCreatedOnDisk() throws IOException {
        File txtFile = createTxtFile("dados.txt");

        StubInputReader reader = new StubInputReader(Collections.emptyList());
        StubNormalizationService normalizer = new StubNormalizationService(Collections.emptyList());
        SpyLogger logger = new SpyLogger();

        OrderProcessingService service = createService(reader, normalizer, logger);
        service.processFiles(new File[]{txtFile}, tempDir.toString());

        Path expectedOutput = tempDir.resolve("dados - Arquivo de saída.json");
        assertTrue(Files.exists(expectedOutput));
    }

    @Test
    public void testProcessFiles_OutputFileContent() throws IOException {
        File txtFile = createTxtFile("test.txt");

        User user = new User(1, "Ana");
        StubInputReader reader = new StubInputReader(Collections.emptyList());
        StubNormalizationService normalizer = new StubNormalizationService(List.of(user));
        SpyLogger logger = new SpyLogger();

        OrderProcessingService service = createService(reader, normalizer, logger);
        service.processFiles(new File[]{txtFile}, tempDir.toString());

        Path output = tempDir.resolve("test - Arquivo de saída.json");
        assertTrue(Files.exists(output));
        String content = Files.readString(output);
        assertTrue(content.contains("\"user_id\": 1"));
        assertTrue(content.contains("\"name\": \"Ana\""));
    }

    @Test
    public void testProcessFiles_LogsCorrectLineCount() throws IOException {
        File txtFile = createTxtFile("dados.txt");

        StubInputReader reader = new StubInputReader(List.of("l1", "l2", "l3"));
        StubNormalizationService normalizer = new StubNormalizationService(Collections.emptyList());
        SpyLogger logger = new SpyLogger();

        OrderProcessingService service = createService(reader, normalizer, logger);
        service.processFiles(new File[]{txtFile}, tempDir.toString());

        assertTrue(logger.infoMessages.stream().anyMatch(m -> m.contains("3 linhas lidas")));
    }

    @Test
    public void testProcessFiles_LogsCorrectUserCount() throws IOException {
        File txtFile = createTxtFile("dados.txt");

        User u1 = new User(1, "Ana");
        User u2 = new User(2, "Bruno");
        StubInputReader reader = new StubInputReader(Collections.emptyList());
        StubNormalizationService normalizer = new StubNormalizationService(List.of(u1, u2));
        SpyLogger logger = new SpyLogger();

        OrderProcessingService service = createService(reader, normalizer, logger);
        service.processFiles(new File[]{txtFile}, tempDir.toString());

        assertTrue(logger.infoMessages.stream().anyMatch(m -> m.contains("2 usuario(s)")));
    }
}
