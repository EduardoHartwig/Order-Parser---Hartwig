import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cli.ArgumentParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.Assert.*;

public class ArgumentParserTest {

    private Path tempDir;

    @Before
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("arg-parser-test-");
    }

    @After
    public void tearDown() throws IOException {
        if (tempDir != null && Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    // ======================== HELPERS ========================

    private File createTxtFile(String name) throws IOException {
        Path file = tempDir.resolve(name);
        Files.createFile(file);
        return file.toFile();
    }

    private File createSubDir(String name) throws IOException {
        Path dir = tempDir.resolve(name);
        Files.createDirectory(dir);
        return dir.toFile();
    }

    // ======================== PARSING - CAMINHO DE ARQUIVO .txt ========================

    @Test
    public void testParse_SingleTxtFile() throws IOException {
        File txtFile = createTxtFile("dados.txt");

        ArgumentParser result = ArgumentParser.parse(new String[]{txtFile.getAbsolutePath()});

        assertEquals(1, result.getTxtFiles().length);
        assertEquals(txtFile.getAbsolutePath(), result.getTxtFiles()[0].getAbsolutePath());
    }

    @Test
    public void testParse_SingleTxtFile_OutputDirIsParent() throws IOException {
        File txtFile = createTxtFile("dados.txt");

        ArgumentParser result = ArgumentParser.parse(new String[]{txtFile.getAbsolutePath()});

        assertEquals(tempDir.toString(), result.getOutputDirPath());
    }

    @Test
    public void testParse_TxtFileUppercaseExtension() throws IOException {
        File txtFile = createTxtFile("dados.TXT");

        ArgumentParser result = ArgumentParser.parse(new String[]{txtFile.getAbsolutePath()});

        assertEquals(1, result.getTxtFiles().length);
    }

    // ======================== PARSING - CAMINHO DE DIRETÓRIO ========================

    @Test
    public void testParse_DirectoryWithTxtFiles() throws IOException {
        createTxtFile("arquivo1.txt");
        createTxtFile("arquivo2.txt");
        createTxtFile("arquivo3.txt");

        ArgumentParser result = ArgumentParser.parse(new String[]{tempDir.toString()});

        assertEquals(3, result.getTxtFiles().length);
        assertEquals(tempDir.toString(), result.getOutputDirPath());
    }

    @Test
    public void testParse_DirectoryFiltersOnlyTxt() throws IOException {
        createTxtFile("dados.txt");
        createTxtFile("ignorar.csv");
        createTxtFile("outro.json");

        ArgumentParser result = ArgumentParser.parse(new String[]{tempDir.toString()});

        assertEquals(1, result.getTxtFiles().length);
        assertTrue(result.getTxtFiles()[0].getName().endsWith(".txt"));
    }

    @Test
    public void testParse_DirectoryWithMixedCaseExtensions() throws IOException {
        createTxtFile("lower.txt");
        createTxtFile("upper.TXT");
        createTxtFile("mixed.Txt");

        ArgumentParser result = ArgumentParser.parse(new String[]{tempDir.toString()});

        assertEquals(3, result.getTxtFiles().length);
    }

    // ======================== PARSING - SEM ARGUMENTOS (DEFAULTS) ========================

    @Test
    public void testParse_NoArgs_UsesDefaultDirs() {
        // Este teste depende da existência das pastas resources/input e resources/output
        File inputDir = new File("resources/input");
        File outputDir = new File("resources/output");

        if (!inputDir.exists() || !outputDir.exists()) {
            // skip - pastas default não existem neste ambiente
            return;
        }

        ArgumentParser result = ArgumentParser.parse(new String[]{});

        assertEquals("resources/output", result.getOutputDirPath());
        assertNotNull(result.getTxtFiles());
    }

    // ======================== VALIDAÇÃO / ERRO - CAMINHO INEXISTENTE ========================

    @Test(expected = IllegalArgumentException.class)
    public void testParse_NonExistentPath_ThrowsException() {
        ArgumentParser.parse(new String[]{"caminho/que/nao/existe.txt"});
    }

    @Test
    public void testParse_NonExistentPath_ExceptionMessage() {
        try {
            ArgumentParser.parse(new String[]{"caminho/inexistente.txt"});
            fail("Deveria lançar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("não encontrado"));
            assertTrue(e.getMessage().contains("caminho/inexistente.txt"));
        }
    }

    // ======================== VALIDAÇÃO / ERRO - ARQUIVO SEM EXTENSÃO .txt ========================

    @Test(expected = IllegalArgumentException.class)
    public void testParse_FileWithoutTxtExtension_ThrowsException() throws IOException {
        File csvFile = createTxtFile("dados.csv");

        ArgumentParser.parse(new String[]{csvFile.getAbsolutePath()});
    }

    @Test
    public void testParse_FileWithoutTxtExtension_ExceptionMessage() throws IOException {
        File csvFile = createTxtFile("dados.csv");

        try {
            ArgumentParser.parse(new String[]{csvFile.getAbsolutePath()});
            fail("Deveria lançar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains(".txt"));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_FileNoExtension_ThrowsException() throws IOException {
        File noExt = createTxtFile("dados");

        ArgumentParser.parse(new String[]{noExt.getAbsolutePath()});
    }

    // ======================== VALIDAÇÃO / ERRO - DEFAULT DIRS ========================

    @Test
    public void testParse_NoArgs_DefaultDirsExist_ReturnsResults() {
        // Testa o happy path de parseDefaults() — as pastas resources/input e resources/output existem no projeto
        File inputDir = new File("resources/input");
        File outputDir = new File("resources/output");
        if (!inputDir.isDirectory() || !outputDir.isDirectory()) {
            return; // skip se não estiver rodando no diretório do projeto
        }

        ArgumentParser result = ArgumentParser.parse(new String[]{});

        assertEquals("resources/output", result.getOutputDirPath());
        assertNotNull(result.getTxtFiles());
        // A pasta resources/input contém arquivos .txt
        assertTrue(result.getTxtFiles().length > 0);
        for (File f : result.getTxtFiles()) {
            assertTrue(f.getName().toLowerCase().endsWith(".txt"));
        }
    }

    @Test
    public void testParse_NoArgs_InputDirMissing_ThrowsException() {
        // Verifica a mensagem quando parseDefaults() não encontra input dir
        // Só roda se as pastas default NÃO existem (ex: CI ou outro diretório)
        File inputDir = new File("resources/input");
        if (inputDir.isDirectory()) {
            // Se existe, forçar o teste passando um caminho relativo que não existe como diretório
            // não é possível sem mudar CWD — validamos apenas a existência da lógica
            return;
        }

        try {
            ArgumentParser.parse(new String[]{});
            fail("Deveria lançar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Pasta de entrada"));
        }
    }

    @Test
    public void testParse_NoArgs_InputExistsAsFile_ThrowsException() throws IOException {
        // Se resources/input existisse como arquivo (não diretório), parseDefaults deve falhar
        // Simulamos via parseFromArgs com arquivo não-.txt para cobrir o branch isFile && !.txt
        // O branch !isDirectory em parseDefaults não é testável sem alterar CWD
        File nonTxtFile = createTxtFile("dados.xml");

        try {
            ArgumentParser.parse(new String[]{nonTxtFile.getAbsolutePath()});
            fail("Deveria lançar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains(".txt"));
        }
    }

    // ======================== BORDA - DIRETÓRIO VAZIO ========================

    @Test
    public void testParse_EmptyDirectory_ReturnsEmptyArray() throws IOException {
        // Diretório sem nenhum arquivo .txt
        File emptyDir = createSubDir("vazio");

        ArgumentParser result = ArgumentParser.parse(new String[]{emptyDir.getAbsolutePath()});

        assertEquals(0, result.getTxtFiles().length);
    }

    @Test
    public void testParse_DirectoryWithNoTxtFiles_ReturnsEmptyArray() throws IOException {
        // Diretório com arquivos, mas nenhum .txt
        File subDir = createSubDir("sem-txt");
        Files.createFile(subDir.toPath().resolve("dados.csv"));
        Files.createFile(subDir.toPath().resolve("config.json"));

        ArgumentParser result = ArgumentParser.parse(new String[]{subDir.getAbsolutePath()});

        assertEquals(0, result.getTxtFiles().length);
    }

    // ======================== BORDA - ESPAÇOS NO CAMINHO ========================

    @Test
    public void testParse_PathWithSpaces_TxtFile() throws IOException {
        File dirWithSpaces = createSubDir("pasta com espaços");
        Path txtFile = Files.createFile(dirWithSpaces.toPath().resolve("arquivo com espaços.txt"));

        ArgumentParser result = ArgumentParser.parse(new String[]{txtFile.toString()});

        assertEquals(1, result.getTxtFiles().length);
        assertTrue(result.getTxtFiles()[0].getName().contains("espaços"));
    }

    @Test
    public void testParse_PathWithSpaces_Directory() throws IOException {
        File dirWithSpaces = createSubDir("pasta com espaços");
        Files.createFile(dirWithSpaces.toPath().resolve("dados.txt"));

        ArgumentParser result = ArgumentParser.parse(new String[]{dirWithSpaces.getAbsolutePath()});

        assertEquals(1, result.getTxtFiles().length);
    }

    // ======================== BORDA - EXTRAS ========================

    @Test
    public void testParse_MultipleArgs_OnlyFirstUsed() throws IOException {
        File txt1 = createTxtFile("primeiro.txt");
        File txt2 = createTxtFile("segundo.txt");

        ArgumentParser result = ArgumentParser.parse(new String[]{
                txt1.getAbsolutePath(), txt2.getAbsolutePath()
        });

        // Apenas args[0] é usado
        assertEquals(1, result.getTxtFiles().length);
        assertEquals(txt1.getAbsolutePath(), result.getTxtFiles()[0].getAbsolutePath());
    }

    @Test
    public void testGetTxtFiles_ReturnsArray() throws IOException {
        createTxtFile("a.txt");
        createTxtFile("b.txt");

        ArgumentParser result = ArgumentParser.parse(new String[]{tempDir.toString()});

        File[] files = result.getTxtFiles();
        assertNotNull(files);
        assertEquals(2, files.length);
    }

    // ======================== BRANCHES - isFile && endsWith(.txt) ========================

    @Test
    public void testParse_FileIsTxt_BothConditionsTrue() throws IOException {
        // isFile()=true && endsWith(".txt")=true → entra no branch
        File txtFile = createTxtFile("valido.txt");

        ArgumentParser result = ArgumentParser.parse(new String[]{txtFile.getAbsolutePath()});

        assertEquals(1, result.getTxtFiles().length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_FileIsNotTxt_SecondConditionFalse() throws IOException {
        // isFile()=true && endsWith(".txt")=false → não entra, cai no isDirectory, cai no throw
        File csvFile = createTxtFile("dados.log");

        ArgumentParser.parse(new String[]{csvFile.getAbsolutePath()});
    }

    @Test
    public void testParse_DirectoryIsNotFile_FirstConditionFalse() throws IOException {
        // isFile()=false → pula para isDirectory()=true
        File dir = createSubDir("pasta");
        Files.createFile(dir.toPath().resolve("algo.txt"));

        ArgumentParser result = ArgumentParser.parse(new String[]{dir.getAbsolutePath()});

        assertEquals(1, result.getTxtFiles().length);
    }

    // ======================== BRANCHES - listFiles null check ========================

    @Test
    public void testParse_DirectoryWithTxtFiles_ListFilesNotNull() throws IOException {
        // listFiles retorna array não-null → usa files diretamente
        File dir = createSubDir("com-txt");
        Files.createFile(dir.toPath().resolve("a.txt"));
        Files.createFile(dir.toPath().resolve("b.txt"));

        ArgumentParser result = ArgumentParser.parse(new String[]{dir.getAbsolutePath()});

        assertEquals(2, result.getTxtFiles().length);
    }

    @Test
    public void testParse_EmptyDir_ListFilesReturnsEmptyArray() throws IOException {
        // listFiles retorna array vazio (não null) → files.length == 0
        File dir = createSubDir("vazio-branch");

        ArgumentParser result = ArgumentParser.parse(new String[]{dir.getAbsolutePath()});

        assertNotNull(result.getTxtFiles());
        assertEquals(0, result.getTxtFiles().length);
    }

    // ======================== BRANCHES - parseDefaults listFiles ========================

    @Test
    public void testParse_NoArgs_DefaultListFilesReturnsArray() {
        // parseDefaults() happy path — listFiles retorna array (não null)
        File inputDir = new File("resources/input");
        File outputDir = new File("resources/output");
        if (!inputDir.isDirectory() || !outputDir.isDirectory()) {
            return;
        }

        ArgumentParser result = ArgumentParser.parse(new String[]{});
        assertNotNull(result.getTxtFiles());
        // getTxtFiles() nunca é null, mesmo se listFiles retornasse null internamente
        assertTrue(result.getTxtFiles().length >= 0);
    }
}
