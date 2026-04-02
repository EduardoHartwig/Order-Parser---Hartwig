# Order Parser - Desafio Hartwig

Aplicação Java que lê arquivos de texto com dados de pedidos em formato de largura fixa, normaliza as informações agrupando por usuário e pedido, e gera arquivos JSON estruturados como saída.

## Estrutura do Projeto

```
src/
├── Main.java                          # Ponto de entrada da aplicação
├── domain/
│   ├── User.java                      # Modelo de usuário
│   ├── Order.java                     # Modelo de pedido
│   └── Product.java                   # Modelo de produto
├── io/
│   ├── FileInputReader.java           # Leitura dos arquivos .txt de entrada
│   └── JsonOutputWriter.java          # Escrita dos arquivos .json de saída
├── parser/
│   └── OrderLineParser.java           # Parser de cada linha do arquivo (largura fixa)
└── service/
    └── OrderNormalizationService.java # Agrupamento e normalização dos dados
test/
└── OrderParserTest.java               # Testes unitários (JUnit 4)
resources/
└── input/                             # Pasta padrão dos arquivos de entrada
```

## Formato do Arquivo de Entrada

Cada linha do arquivo `.txt` segue um formato de largura fixa com os seguintes campos:

| Campo      | Tamanho | Descrição                    |
|------------|---------|------------------------------|
| userId     | 10      | ID do usuário                |
| userName   | 45      | Nome do usuário              |
| orderId    | 10      | ID do pedido                 |
| productId  | 10      | ID do produto                |
| value      | 12      | Valor do produto             |
| date       | 8       | Data do pedido (`YYYYMMDD`)  |

Exemplo de linha:

```
0000000001                                      Zarelli00000001230000000111      512.2420211201
```

- Linhas em branco e linhas contendo `|` (cabeçalhos) são ignoradas automaticamente.

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

- Java 11 ou superior

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
java -jar parser.jar "C:\Users\Eduardo\Documents\pedidos\arquivo1.txt"
```

**Passando um diretório** (processa todos os `.txt` dentro dele):

```powershell
java -jar parser.jar "C:\Users\Eduardo\Documents\pedidos"
```

**Modo padrão** (sem argumentos):

```powershell
java -jar parser.jar
```

> Quando executado sem argumentos, a aplicação lê os arquivos `.txt` da pasta `resources/input/` e gera os JSONs em `resources/output/`. Você pode colocar seus arquivos de entrada diretamente na pasta `resources/input/` caso prefira usar esse modo.

O arquivo JSON de saída será gerado no mesmo diretório do arquivo de entrada (quando passado por argumento) ou em `resources/output/` (modo padrão), com o nome `<nome do arquivo> - Arquivo de saída.json`.

## Testes

O projeto utiliza **JUnit 4** para testes unitários cobrindo:

- Criação e comportamento dos modelos (`Product`, `Order`, `User`)
- Cálculo de totais dos pedidos
- Parsing de linhas em formato de largura fixa
- Formatação de datas e valores decimais
- Normalização e agrupamento de múltiplos usuários e pedidos
- Preservação da ordem de inserção dos usuários
- Cenários com listas vazias e dados complexos
