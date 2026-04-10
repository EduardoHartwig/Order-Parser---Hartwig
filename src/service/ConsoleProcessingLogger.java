package service;

public class ConsoleProcessingLogger implements ProcessingLogger {

    @Override
    public void info(String message) {
        System.out.println(message);
    }

    @Override
    public void error(String message) {
        System.err.println(message);
    }
}
