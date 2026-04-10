package io;

import java.util.HashMap;
import java.util.Map;

public class OutputWriterFactory {

    private static final Map<String, OutputWriter> WRITERS = new HashMap<>();

    static {
        register(new JsonOutputWriter());
    }

    public static void register(OutputWriter writer) {
        WRITERS.put(writer.getFileExtension(), writer);
    }

    public static OutputWriter getWriter(String extension) {
        OutputWriter writer = WRITERS.get(extension);
        if (writer == null) {
            throw new IllegalArgumentException("Formato de saida nao suportado: " + extension);
        }
        return writer;
    }

    public static OutputWriter getDefault() {
        return getWriter(".json");
    }
}
