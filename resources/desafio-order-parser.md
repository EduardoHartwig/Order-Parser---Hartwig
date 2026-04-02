# Desafio Técnico - Order Parser

## Descrição
Temos uma demanda para integrar dois sistemas. O sistema legado possui um arquivo de pedidos desnormalizado, e precisamos transformá-lo em um arquivo JSON normalizado. Para isso, é necessário atender aos requisitos descritos abaixo.

## Objetivo do desafio
Desenvolva um sistema, na linguagem de sua preferência, que receba um arquivo ou diretório (a forma de recebimento fica a seu critério) e processe os dados para o novo formato JSON.

Não é necessário utilizar frameworks — utilize apenas os recursos nativos da linguagem.

Assim que iniciar o desenvolvimento, crie um repositório com o código e compartilhe o link conosco, informando também quando concluir o desafio.

## Arquivos de entrada
O arquivo do sistema legado possui uma estrutura em que cada linha representa uma parte de um pedido.

Os dados são padronizados por tamanho fixo, conforme a tabela abaixo:

| Campo | Tamanho | Tipo |
|---|---:|---|
| id usuário | 10 | Numérico |
| nome | 45 | Texto |
| id pedido | 10 | Numérico |
| id produto | 10 | Numérico |
| valor do produto | 12 | Decimal |
| data compra | 8 | Numérico (formato: yyyymmdd) |

## Observações
- Todos os campos numéricos são completados com 0 à esquerda.
- Campos textuais são completados com espaços à esquerda.

## Saída esperada (JSON normalizado)

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
  },
  {
    "user_id": 2,
    "name": "Medeiros",
    "orders": [
      {
        "order_id": 12345,
        "total": "512.48",
        "date": "2020-12-01",
        "products": [
          { "product_id": 111, "value": "256.24" },
          { "product_id": 122, "value": "256.24" }
        ]
      }
    ]
  }
]
```

## Palavras-chave
- Testes
- Lógica
- Simplicidade
- Linguagem (não estamos falando de framework)
