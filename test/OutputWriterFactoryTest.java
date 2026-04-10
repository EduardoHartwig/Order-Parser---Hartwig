import org.junit.Test;

import io.JsonOutputWriter;
import io.OutputWriter;
import io.OutputWriterFactory;

import static org.junit.Assert.*;

public class OutputWriterFactoryTest {

    // ======================== getDefault ========================

    @Test
    public void testGetDefault_ReturnsJsonOutputWriter() {
        OutputWriter writer = OutputWriterFactory.getDefault();
        assertTrue(writer instanceof JsonOutputWriter);
    }

    @Test
    public void testGetDefault_ExtensionIsJson() {
        OutputWriter writer = OutputWriterFactory.getDefault();
        assertEquals(".json", writer.getFileExtension());
    }

    // ======================== getWriter ========================

    @Test
    public void testGetWriter_Json_ReturnsInstance() {
        OutputWriter writer = OutputWriterFactory.getWriter(".json");
        assertNotNull(writer);
        assertTrue(writer instanceof JsonOutputWriter);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetWriter_UnsupportedExtension_ThrowsException() {
        OutputWriterFactory.getWriter(".xml");
    }

    @Test
    public void testGetWriter_UnsupportedExtension_ExceptionMessage() {
        try {
            OutputWriterFactory.getWriter(".csv");
            fail("Deveria lançar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Formato de saida nao suportado"));
            assertTrue(e.getMessage().contains(".csv"));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetWriter_NullExtension_ThrowsException() {
        OutputWriterFactory.getWriter(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetWriter_EmptyExtension_ThrowsException() {
        OutputWriterFactory.getWriter("");
    }
}
