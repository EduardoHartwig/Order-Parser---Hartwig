package io;

import domain.User;

import java.io.IOException;
import java.util.List;

public sealed interface OutputWriter permits JsonOutputWriter {

    void writeToFile(String filePath, List<User> users) throws IOException;

    String getFileExtension();
}
