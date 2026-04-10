import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.FileInputReader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.*;

public class FileInputReaderTest {

    private FileInputReader reader;
    private Path tempFile;

    @Before
    public void setUp() {
        reader = new FileInputReader();
    }

    @After
    public void tearDown() throws IOException {
        if (tempFile != null && Files.exists(tempFile)) {
            Files.delete(tempFile);
        }
    }

    private Path writeTempFile(String content) throws IOException {
        tempFile = Files.createTempFile("reader-test-", ".txt");
        Files.writeString(tempFile, content, StandardCharsets.UTF_8);
        return tempFile;
    }

    private Path writeTempFileBytes(byte[] bytes) throws IOException {
        tempFile = Files.createTempFile("reader-test-", ".txt");
        Files.write(tempFile, bytes);
        return tempFile;
    }

    // ======================== INTEGRAÇÃO I/O - LEITURA BÁSICA ========================

    @Test
    public void testReadFile_SingleLine() throws IOException {
        Path file = writeTempFile("linha1");

        List<String> lines = reader.readFile(file.toString());

        assertEquals(1, lines.size());
        assertEquals("linha1", lines.get(0));
    }

    @Test
    public void testReadFile_MultipleLines() throws IOException {
        Path file = writeTempFile("linha1\nlinha2\nlinha3");

        List<String> lines = reader.readFile(file.toString());

        assertEquals(3, lines.size());
        assertEquals("linha1", lines.get(0));
        assertEquals("linha2", lines.get(1));
        assertEquals("linha3", lines.get(2));
    }

    @Test
    public void testReadFile_EmptyFile_ReturnsEmptyList() throws IOException {
        Path file = writeTempFile("");

        List<String> lines = reader.readFile(file.toString());

        assertTrue(lines.isEmpty());
    }

    @Test
    public void testReadFile_ReturnsCorrectLineCount() throws IOException {
        Path file = writeTempFile("a\nb\nc\nd\ne");

        List<String> lines = reader.readFile(file.toString());

        assertEquals(5, lines.size());
    }

    // ======================== FILTRAGEM - BOM ========================

    @Test
    public void testReadFile_RemovesBOM() throws IOException {
        // BOM UTF-8: EF BB BF seguido de "linha1"
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] content = "linha1\nlinha2".getBytes(StandardCharsets.UTF_8);
        byte[] full = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, full, 0, bom.length);
        System.arraycopy(content, 0, full, bom.length, content.length);

        Path file = writeTempFileBytes(full);

        List<String> lines = reader.readFile(file.toString());

        assertEquals(2, lines.size());
        assertEquals("linha1", lines.get(0));
        assertFalse(lines.get(0).startsWith("\uFEFF"));
    }

    @Test
    public void testReadFile_BOMOnlyOnFirstLine() throws IOException {
        // BOM no início, segunda linha normal
        String content = "\uFEFFprimeira\nsegunda";
        Path file = writeTempFile(content);

        List<String> lines = reader.readFile(file.toString());

        assertEquals("primeira", lines.get(0));
        assertEquals("segunda", lines.get(1));
    }

    @Test
    public void testReadFile_NoBOM_NoStripping() throws IOException {
        Path file = writeTempFile("normal\nsem bom");

        List<String> lines = reader.readFile(file.toString());

        assertEquals("normal", lines.get(0));
        assertEquals("sem bom", lines.get(1));
    }

    // ======================== FILTRAGEM - LINHAS VAZIAS ========================

    @Test
    public void testReadFile_SkipsEmptyLines() throws IOException {
        Path file = writeTempFile("linha1\n\nlinha2\n\n\nlinha3");

        List<String> lines = reader.readFile(file.toString());

        assertEquals(3, lines.size());
        assertEquals("linha1", lines.get(0));
        assertEquals("linha2", lines.get(1));
        assertEquals("linha3", lines.get(2));
    }

    @Test
    public void testReadFile_SkipsWhitespaceOnlyLines() throws IOException {
        Path file = writeTempFile("linha1\n   \nlinha2\n\t\nlinha3");

        List<String> lines = reader.readFile(file.toString());

        assertEquals(3, lines.size());
    }

    @Test
    public void testReadFile_AllEmptyLines_ReturnsEmpty() throws IOException {
        Path file = writeTempFile("\n\n\n   \n\t\n");

        List<String> lines = reader.readFile(file.toString());

        assertTrue(lines.isEmpty());
    }

    // ======================== FILTRAGEM - LINHAS COM PIPE ========================

    @Test
    public void testReadFile_SkipsLinesWithPipe() throws IOException {
        Path file = writeTempFile("dados\ncabeçalho|separador\nmais dados");

        List<String> lines = reader.readFile(file.toString());

        assertEquals(2, lines.size());
        assertEquals("dados", lines.get(0));
        assertEquals("mais dados", lines.get(1));
    }

    @Test
    public void testReadFile_SkipsMultiplePipeLines() throws IOException {
        Path file = writeTempFile("col1|col2|col3\n---|---|---\ndados1\ndados2");

        List<String> lines = reader.readFile(file.toString());

        assertEquals(2, lines.size());
        assertEquals("dados1", lines.get(0));
        assertEquals("dados2", lines.get(1));
    }

    @Test
    public void testReadFile_PipeAtStartOrEnd() throws IOException {
        Path file = writeTempFile("|inicio\nfim|\nmiddle|middle\ndados");

        List<String> lines = reader.readFile(file.toString());

        assertEquals(1, lines.size());
        assertEquals("dados", lines.get(0));
    }

    @Test
    public void testReadFile_AllPipeLines_ReturnsEmpty() throws IOException {
        Path file = writeTempFile("a|b\nc|d\ne|f");

        List<String> lines = reader.readFile(file.toString());

        assertTrue(lines.isEmpty());
    }

    // ======================== FILTRAGEM - COMBINAÇÃO ========================

    @Test
    public void testReadFile_BOMAndEmptyAndPipeTogether() throws IOException {
        String content = "\uFEFFdados_ok\n\ncabeçalho|sep\n   \nlinha_valida\n|pipe\n";
        Path file = writeTempFile(content);

        List<String> lines = reader.readFile(file.toString());

        assertEquals(2, lines.size());
        assertEquals("dados_ok", lines.get(0));
        assertEquals("linha_valida", lines.get(1));
    }

    // ======================== ENCODING UTF-8 ========================

    @Test
    public void testReadFile_Utf8AccentedCharacters() throws IOException {
        Path file = writeTempFile("João\nMária\nAndré\nCésar");

        List<String> lines = reader.readFile(file.toString());

        assertEquals(4, lines.size());
        assertEquals("João", lines.get(0));
        assertEquals("Mária", lines.get(1));
        assertEquals("André", lines.get(2));
        assertEquals("César", lines.get(3));
    }

    @Test
    public void testReadFile_Utf8SpecialCharacters() throws IOException {
        Path file = writeTempFile("São Paulo\nÇédilha\nÜmlaut\nÑoño");

        List<String> lines = reader.readFile(file.toString());

        assertEquals(4, lines.size());
        assertEquals("São Paulo", lines.get(0));
        assertEquals("Çédilha", lines.get(1));
        assertEquals("Ümlaut", lines.get(2));
        assertEquals("Ñoño", lines.get(3));
    }

    @Test
    public void testReadFile_Utf8WithBOM() throws IOException {
        String content = "\uFEFFJoão André";
        Path file = writeTempFile(content);

        List<String> lines = reader.readFile(file.toString());

        assertEquals(1, lines.size());
        assertEquals("João André", lines.get(0));
    }

    // ======================== ERRO - ARQUIVO INEXISTENTE ========================

    @Test(expected = IOException.class)
    public void testReadFile_NonExistentFile_ThrowsIOException() throws IOException {
        reader.readFile("caminho/que/nao/existe.txt");
    }

    @Test
    public void testReadFile_NonExistentFile_ExceptionType() {
        try {
            reader.readFile("arquivo_inexistente.txt");
            fail("Deveria lançar IOException");
        } catch (IOException e) {
            assertNotNull(e.getMessage());
        }
    }
}
