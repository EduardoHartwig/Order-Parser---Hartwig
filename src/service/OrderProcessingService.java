package service;

import domain.User;
import io.InputReader;
import io.OutputWriter;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class OrderProcessingService {

    private final InputReader inputReader;
    private final NormalizationService normalizationService;
    private final OutputWriter outputWriter;
    private final ProcessingLogger logger;

    public OrderProcessingService(InputReader inputReader, NormalizationService normalizationService, OutputWriter outputWriter, ProcessingLogger logger) {
        this.inputReader = inputReader;
        this.normalizationService = normalizationService;
        this.outputWriter = outputWriter;
        this.logger = logger;
    }

    public int processFiles(File[] txtFiles, String outputDirPath) {
        int processedCount = 0;

        for (File txtFile : txtFiles) {
            try {
                processFile(txtFile, outputDirPath);
                processedCount++;
            } catch (IOException e) {
                logger.error("  \u2717 Erro ao processar arquivo: " + e.getMessage());
            } catch (Exception e) {
                logger.error("  \u2717 Erro geral: " + e.getMessage());
            }
        }

        return processedCount;
    }

    private void processFile(File txtFile, String outputDirPath) throws IOException {
        String inputFile = txtFile.getAbsolutePath();
        String fileName = txtFile.getName();
        String baseName = fileName.substring(0, fileName.lastIndexOf("."));
        String outputFileName = baseName + " - Arquivo de saída" + outputWriter.getFileExtension();
        String outputFile = new File(outputDirPath).getAbsolutePath() + File.separator + outputFileName;

        logger.info("Processando: " + fileName);

        List<String> lines = inputReader.readFile(inputFile);
        logger.info("  \u2713 " + lines.size() + " linhas lidas.");

        List<User> users = normalizationService.normalizeOrders(lines);
        logger.info("  \u2713 " + users.size() + " usuario(s) processado(s).");

        int totalOrders = users.stream().mapToInt(u -> u.getOrders().size()).sum();
        logger.info("  \u2713 " + totalOrders + " pedido(s) no total.");

        outputWriter.writeToFile(outputFile, users);
        logger.info("  \u2713 Arquivo gerado: " + outputFileName);
        logger.info("");

    }
}
