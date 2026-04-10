# Order Parser - Desafio Hartwig

Aplicação Java que lê arquivos de texto com dados de pedidos em formato de largura fixa, normaliza as informações agrupando por usuário e pedido, e gera arquivos JSON estruturados como saída.

## Estrutura do Projeto

```
src/
├── app/
│   └── Main.java                          # Ponto de entrada da aplicação
├── cli/
│   └── ArgumentParser.java                # Parser de argumentos da linha de comando
├── domain/
│   ├── User.java                          # Modelo de usuário
│   ├── Order.java                         # Modelo de pedido
│   └── Product.java                       # Modelo de produto
├── io/
│   ├── InputReader.java                   # Interface selada de leitura de entrada
│   ├── FileInputReader.java               # Implementação: leitura de arquivos .txt
│   ├── OutputWriter.java                  # Interface selada de escrita de saída
│   ├── JsonOutputWriter.java              # Implementação: escrita de arquivos .json
│   └── OutputWriterFactory.java           # Factory para registro e criação de writers
├── parser/
│   ├── LineParser.java                    # Interface selada de parsing de linha
│   ├── OrderLine.java                     # Record imutável com dados de uma linha
│   └── OrderLineParser.java              # Implementação: parsing de largura fixa
└── service/
    ├── ProcessingLogger.java              # Interface de logging do processamento
    ├── ConsoleProcessingLogger.java       # Implementação: logging no console
    ├── NormalizationService.java          # Interface de normalização
    ├── OrderNormalizationService.java     # Implementação: agrupamento dos dados
    └── OrderProcessingService.java        # Orquestrador do fluxo de processamento
test/
├── ArgumentParserTest.java                # Testes do parser de argumentos
├── ConsoleProcessingLoggerTest.java       # Testes do logger
├── FileInputReaderTest.java               # Testes de leitura de arquivos
├── JsonOutputWriterTest.java              # Testes de escrita JSON
├── OrderLineParserTest.java               # Testes de parsing de linhas
├── OrderNormalizationServiceTest.java     # Testes de normalização
├── OrderParserTest.java                   # Testes dos modelos de domínio
├── OrderProcessingServiceTest.java        # Testes do serviço de processamento
└── OutputWriterFactoryTest.java           # Testes da factory de writers
resources/
├── input/                                 # Pasta padrão dos arquivos de entrada
└── output/                                # Pasta padrão dos arquivos de saída
```

## Arquitetura

O projeto segue uma arquitetura em camadas com os seguintes padrões de design:

- **Sealed Interfaces** — `InputReader`, `OutputWriter`, `LineParser` e `NormalizationService` são interfaces seladas, restringindo as implementações a tipos conhecidos em tempo de compilação.
- **Factory Pattern** — `OutputWriterFactory` permite registrar e resolver writers de saída por extensão de arquivo (ex: `.json`), facilitando a adição de novos formatos.
- **Strategy Pattern** — As interfaces de I/O e parsing permitem trocar implementações sem alterar o fluxo principal.
- **Records** — `OrderLine` é um record Java para representação imutável dos dados de cada linha.
- **Injeção de dependências via construtor** — Os serviços recebem suas dependências pelo construtor, facilitando testes e substituição de componentes.
- **Coleções não modificáveis** — Todos os getters de domínio retornam listas não modificáveis.
- **BigDecimal** — Valores monetários utilizam `BigDecimal` com precisão de 2 casas decimais e arredondamento `HALF_UP`.

## Formato do Arquivo de Entrada

Cada linha do arquivo `.txt` segue um formato de largura fixa com os seguintes campos:

| Campo      | Tamanho | Posição | Descrição                    |
|------------|---------|---------|------------------------------|
| userId     | 10      | 0-9     | ID do usuário                |
| userName   | 45      | 10-54   | Nome do usuário              |
| orderId    | 10      | 55-64   | ID do pedido                 |
| productId  | 10      | 65-74   | ID do produto                |
| value      | 12      | 75-86   | Valor do produto             |
| date       | 8       | 87-94   | Data do pedido (`YYYYMMDD`)  |

Exemplo de linha:

```
0000000001                                      Zarelli00000001230000000111      512.2420211201
```

- Linhas em branco e linhas contendo `|` (cabeçalhos) são ignoradas automaticamente.
- O BOM UTF-8 é removido automaticamente.
- Linhas inválidas são registradas no log, mas não interrompem o processamento.

## Formato da Saída (JSON)

O arquivo JSON gerado agrupa os dados por usuário, com seus pedidos e respectivos produtos:

```json
[
  {
    "user_id": 1,
    "name": "Zarelli",
    "orders": [
      {
        "order_id": 123,
        "total": "1024.48",
        "date": "2021-12-01",
        "products": [
          { "product_id": 111, "value": "512.24" },
          { "product_id": 122, "value": "512.24" }
        ]
      }
    ]
  }
]
```

## Como Executar

### Pré-requisitos

- Java 17 ou superior

### Compilação (PowerShell)

```powershell
javac -d out (Get-ChildItem -Recurse -Filter *.java -Path src).FullName
```

### Gerar o JAR

```powershell
jar cfm parser.jar MANIFEST.MF -C out .
```

### Execução

**Passando um arquivo específico:**

```powershell
java -jar parser.jar "C:\caminho\para\arquivo.txt"
```

**Passando um diretório** (processa todos os `.txt` dentro dele):

```powershell
java -jar parser.jar "C:\caminho\para\diretorio"
```

**Modo padrão** (sem argumentos):

```powershell
java -jar parser.jar
```

> Quando executado sem argumentos, a aplicação lê os arquivos `.txt` da pasta `resources/input/` e gera os JSONs em `resources/output/`. Você pode colocar seus arquivos de entrada diretamente na pasta `resources/input/` caso prefira usar esse modo.

O arquivo JSON de saída será gerado no mesmo diretório do arquivo de entrada (quando passado por argumento) ou em `resources/output/` (modo padrão), com o nome `<nome do arquivo> - Arquivo de saída.json`.

## Testes

O projeto utiliza **JUnit 4** para testes unitários com **8 classes de teste** cobrindo:

| Classe de Teste                    | Cobertura                                                   |
|------------------------------------|-------------------------------------------------------------|
| `ArgumentParserTest`               | Parsing de argumentos: arquivo, diretório, padrão e erros   |
| `OrderLineParserTest`              | Parsing de largura fixa, formatação de datas e decimais     |
| `OrderNormalizationServiceTest`    | Agrupamento por usuário, deduplicação de pedidos, logging   |
| `FileInputReaderTest`              | Leitura UTF-8, remoção de BOM, filtragem de linhas          |
| `JsonOutputWriterTest`             | Serialização JSON, escaping, cálculo de totais              |
| `OutputWriterFactoryTest`          | Registro de writers, resolução por extensão, tratamento de erros |
| `ConsoleProcessingLoggerTest`      | Roteamento de mensagens para stdout/stderr                  |
| `OrderProcessingServiceTest`       | Orquestração do fluxo completo, tratamento de erros         |