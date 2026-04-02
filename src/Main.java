import domain.User;
import io.FileInputReader;
import io.JsonOutputWriter;
import service.OrderNormalizationService;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            // Definir caminhos das pastas de entrada e saída
            String inputDirPath = "resources/input";
            String outputDirPath = "resources/output";

            System.out.println("========================================");
            System.out.println("   Order Parser - Desafio Hartwig");
            System.out.println("========================================");
            System.out.println();

            // Validar pastas
            File inputDir = new File(inputDirPath);
            File outputDir = new File(outputDirPath);

            if (!inputDir.exists() || !inputDir.isDirectory()) {
                System.err.println("Erro: Pasta de entrada '" + inputDirPath + "' não encontrada!");
                System.exit(1);
            }

            if (!outputDir.exists() || !outputDir.isDirectory()) {
                System.err.println("Erro: Pasta de saída '" + outputDirPath + "' não encontrada!");
                System.exit(1);
            }

            // Obter lista de arquivos .txt da pasta de entrada
            File[] txtFiles = inputDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));

            if (txtFiles == null || txtFiles.length == 0) {
                System.out.println("Nenhum arquivo .txt encontrado na pasta de entrada!");
                System.exit(0);
            }

            System.out.println("Processando " + txtFiles.length + " arquivo(s)...\n");

            int processedCount = 0;
            for (File txtFile : txtFiles) {
                try {
                    String inputFile = txtFile.getAbsolutePath();
                    String fileName = txtFile.getName();
                    String jsonFileName = fileName.substring(0, fileName.lastIndexOf(".")) + ".json";
                    String outputFile = outputDir.getAbsolutePath() + File.separator + jsonFileName;

                    System.out.println("Processando: " + fileName);

                    // Ler arquivo de entrada
                    List<String> lines = FileInputReader.readFile(inputFile);
                    System.out.println("  ✓ " + lines.size() + " linhas lidas.");

                    // Normalizar pedidos
                    List<User> users = OrderNormalizationService.normalizeOrders(lines);
                    System.out.println("  ✓ " + users.size() + " usuário(s) processado(s).");

                    // Calcular total de pedidos
                    int totalOrders = users.stream().mapToInt(u -> u.getOrders().size()).sum();
                    System.out.println("  ✓ " + totalOrders + " pedido(s) no total.");

                    // Escrever arquivo JSON de saída
                    JsonOutputWriter.writeToFile(outputFile, users);
                    System.out.println("  ✓ JSON gerado: " + jsonFileName);
                    System.out.println();

                    processedCount++;

                } catch (IOException e) {
                    System.err.println("  ✗ Erro ao processar arquivo: " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("  ✗ Erro geral: " + e.getMessage());
                }
            }

            System.out.println("========================================");
            System.out.println("   Processamento concluído!");
            System.out.println("   " + processedCount + " arquivo(s) processado(s)");
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
            System.exit(1);
        }
    }
}
