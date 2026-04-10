import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import service.ConsoleProcessingLogger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.*;

public class ConsoleProcessingLoggerTest {

    private final ConsoleProcessingLogger logger = new ConsoleProcessingLogger();

    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;
    private PrintStream originalOut;
    private PrintStream originalErr;

    @Before
    public void setUp() {
        originalOut = System.out;
        originalErr = System.err;
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @After
    public void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    // ======================== info → System.out ========================

    @Test
    public void testInfo_WritesToSystemOut() {
        logger.info("mensagem de info");
        assertTrue(outContent.toString().contains("mensagem de info"));
    }

    @Test
    public void testInfo_DoesNotWriteToSystemErr() {
        logger.info("apenas out");
        assertEquals("", errContent.toString());
    }

    @Test
    public void testInfo_AppendsNewline() {
        logger.info("teste");
        assertTrue(outContent.toString().endsWith(System.lineSeparator()));
    }

    // ======================== error → System.err ========================

    @Test
    public void testError_WritesToSystemErr() {
        logger.error("mensagem de erro");
        assertTrue(errContent.toString().contains("mensagem de erro"));
    }

    @Test
    public void testError_DoesNotWriteToSystemOut() {
        logger.error("apenas err");
        assertEquals("", outContent.toString());
    }

    @Test
    public void testError_AppendsNewline() {
        logger.error("teste");
        assertTrue(errContent.toString().endsWith(System.lineSeparator()));
    }
}
