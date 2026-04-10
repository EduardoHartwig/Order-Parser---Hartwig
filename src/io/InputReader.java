package io;

import java.io.IOException;
import java.util.List;

public interface InputReader {

    List<String> readFile(String filePath) throws IOException;
}
