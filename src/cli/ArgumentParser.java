package cli;

import java.io.File;

public class ArgumentParser {

    private static final String DEFAULT_INPUT_DIR = "resources/input";
    private static final String DEFAULT_OUTPUT_DIR = "resources/output";

    private final String outputDirPath;
    private final File[] txtFiles;

    private ArgumentParser(String outputDirPath, File[] txtFiles) {
        this.outputDirPath = outputDirPath;
        this.txtFiles = txtFiles;
    }

    public String getOutputDirPath() {
        return outputDirPath;
    }

    public File[] getTxtFiles() {
        return txtFiles;
    }

    public static ArgumentParser parse(String[] args) {
        if (args.length > 0) {
            return parseFromArgs(args[0]);
        }
        return parseDefaults();
    }

    private static ArgumentParser parseFromArgs(String inputPath) {
        File inputFile = new File(inputPath);

        if (!inputFile.exists()) {
            throw new IllegalArgumentException("Caminho '" + inputPath + "' não encontrado!");
        }

        if (inputFile.isFile() && inputFile.getName().toLowerCase().endsWith(".txt")) {
            return new ArgumentParser(inputFile.getParent(), new File[]{inputFile});
        }

        if (inputFile.isDirectory()) {
            File[] files = inputFile.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
            return new ArgumentParser(inputPath, (files != null) ? files : new File[0]);
        }

        throw new IllegalArgumentException("O arquivo deve ter extensão .txt!");
    }

    private static ArgumentParser parseDefaults() {
        File inputDir = new File(DEFAULT_INPUT_DIR);
        File outputDir = new File(DEFAULT_OUTPUT_DIR);

        if (!inputDir.exists() || !inputDir.isDirectory()) {
            throw new IllegalArgumentException("Pasta de entrada '" + DEFAULT_INPUT_DIR + "' não encontrada!");
        }

        if (!outputDir.exists() || !outputDir.isDirectory()) {
            throw new IllegalArgumentException("Pasta de saída '" + DEFAULT_OUTPUT_DIR + "' não encontrada!");
        }

        File[] files = inputDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        return new ArgumentParser(DEFAULT_OUTPUT_DIR, (files != null) ? files : new File[0]);
    }
}
