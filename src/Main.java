import domain.User;
import io.FileInputReader;
import io.JsonOutputWriter;
import service.OrderNormalizationService;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.List;

public class Main {

    public static String removeAccents(String text) {
        if (text == null) return null;
        
        String nfkdForm = Normalizer.normalize(text, Normalizer.Form.NFKD);
        
        return nfkdForm.replaceAll("[^\\p{ASCII}]", "");
    }
    
    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
            
            System.out.println("========================================");
            System.out.println("   Order Parser - Desafio Hartwig");
            System.out.println("========================================");
            System.out.println();

            String inputPath;
            String outputDirPath;
            File[] txtFiles;

            if (args.length > 0) {
                inputPath = args[0];
                File inputFile = new File(inputPath);

                if (!inputFile.exists()) {
                    System.err.println(removeAccents("Erro: Caminho '" + inputPath + "' não encontrado!"));
                    System.exit(1);
                }

                if (inputFile.isFile() && inputFile.getName().toLowerCase().endsWith(".txt")) {
                    outputDirPath = inputFile.getParent();
                    txtFiles = new File[]{inputFile};
                }
                else if (inputFile.isDirectory()) {
                    outputDirPath = inputPath;
                    File[] files = inputFile.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
                    txtFiles = (files != null) ? files : new File[0];
                }
                else {
                    System.err.println(removeAccents("Erro: O arquivo deve ter extensão .txt!"));
                    System.exit(1);
                    return;
                }
            }
            else {
                String inputDirPath = "resources/input";
                outputDirPath = "resources/output";

                File inputDir = new File(inputDirPath);
                File outputDir = new File(outputDirPath);

                if (!inputDir.exists() || !inputDir.isDirectory()) {
                    System.err.println(removeAccents("Erro: Pasta de entrada '" + inputDirPath + "' não encontrada!"));
                    System.exit(1);
                }

                if (!outputDir.exists() || !outputDir.isDirectory()) {
                    System.err.println(removeAccents("Erro: Pasta de saida '" + outputDirPath + "' não encontrada!"));
                    System.exit(1);
                }

                File[] files = inputDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
                txtFiles = (files != null) ? files : new File[0];
            }

            if (txtFiles.length == 0) {
                System.out.println(removeAccents("Nenhum arquivo .txt encontrado na pasta de entrada!"));
                System.exit(0);
            }

            System.out.println(removeAccents("Processando " + txtFiles.length + " arquivo(s)...\n"));

            int processedCount = 0;
            for (File txtFile : txtFiles) {
                try {
                    String inputFile = txtFile.getAbsolutePath();
                    String fileName = txtFile.getName();
                    String jsonFileName = fileName.substring(0, fileName.lastIndexOf(".")) + " - Arquivo de saída.json";
                    File outputDir = new File(outputDirPath);
                    String outputFile = outputDir.getAbsolutePath() + File.separator + jsonFileName;

                    System.out.println(removeAccents("Processando: " + fileName));

                    List<String> lines = FileInputReader.readFile(inputFile);
                    System.out.println(removeAccents("  ✓ " + lines.size() + " linhas lidas."));

                    List<User> users = OrderNormalizationService.normalizeOrders(lines);
                    System.out.println(removeAccents("  ✓ " + users.size() + " usuario(s) processado(s)."));

                    int totalOrders = users.stream().mapToInt(u -> u.getOrders().size()).sum();
                    System.out.println(removeAccents("  ✓ " + totalOrders + " pedido(s) no total."));

                    JsonOutputWriter.writeToFile(outputFile, users);
                    System.out.println(removeAccents("  ✓ JSON gerado: " + jsonFileName));
                    System.out.println();

                    processedCount++;

                } catch (IOException e) {
                    System.err.println(removeAccents("  ✗ Erro ao processar arquivo: " + e.getMessage()));
                } catch (Exception e) {
                    System.err.println(removeAccents("  ✗ Erro geral: " + e.getMessage()));
                }
            }

            System.out.println("========================================");
            System.out.println(removeAccents("   Processamento concluido!"));
            System.out.println(removeAccents("   " + processedCount + " arquivo(s) processado(s)"));
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println(removeAccents("Erro: " + e.getMessage()));
            System.exit(1);
        }
    }
}
