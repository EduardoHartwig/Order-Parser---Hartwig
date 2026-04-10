package app;

import cli.ArgumentParser;
import io.FileInputReader;
import io.OutputWriterFactory;
import parser.OrderLineParser;
import service.ConsoleProcessingLogger;
import service.ProcessingLogger;
import service.OrderNormalizationService;
import service.OrderProcessingService;

import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class Main {

    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));

            System.out.println("========================================");
            System.out.println("   Order Parser - Desafio Hartwig");
            System.out.println("========================================");
            System.out.println();

            ArgumentParser parsedArgs = ArgumentParser.parse(args);
            File[] txtFiles = parsedArgs.getTxtFiles();

            if (txtFiles.length == 0) {
                System.out.println("Nenhum arquivo .txt encontrado na pasta de entrada!");
                return;
            }

            ProcessingLogger logger = new ConsoleProcessingLogger();

            OrderProcessingService processingService = new OrderProcessingService(
                    new FileInputReader(),
                    new OrderNormalizationService(new OrderLineParser(), logger),
                    OutputWriterFactory.getDefault(),
                    logger
            );

            System.out.println("Processando " + txtFiles.length + " arquivo(s)...\n");

            int processedCount = processingService.processFiles(txtFiles, parsedArgs.getOutputDirPath());

            System.out.println("========================================");
            System.out.println("   Processamento concluido!");
            System.out.println("   " + processedCount + " arquivo(s) processado(s)");
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
            System.exit(1);
        }
    }
}
