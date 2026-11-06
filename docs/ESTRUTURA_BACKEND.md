# Estrutura do backend — Banco Digital

Documento de referência da estrutura atual do backend, seus principais componentes e os endpoints disponíveis.

## Visão geral

O backend é uma API REST em Java 21 com Spring Boot, organizada em camadas. O Spring Data JPA faz o acesso relacional aos dados, e o projeto está configurado para usar MySQL. A aplicação inicia na porta `8081`.

## Organização do código

```text
src/
├── main/
│   ├── java/bancodigital/api/
│   │   ├── ApiApplication.java
│   │   ├── Controller/       # Endpoints REST
│   │   ├── DTO/              # Formatos de entrada e saída
│   │   ├── Exception/        # Exceções de domínio e respostas de erro
│   │   ├── Model/            # Entidades persistidas
│   │   ├── Repository/       # Acesso a dados com Spring Data JPA
│   │   └── Service/          # Regras de negócio
│   └── resources/
│       └── application.properties
└── test/
    └── java/bancodigital/api/
        ├── ApiApplicationTests.java
        └── TransferenciaServiceTest.java
```

### Responsabilidade de cada camada

- **Controller:** recebe requisições HTTP, valida entradas e delega operações.
- **DTO:** define os dados aceitos e retornados pela API, separado das entidades.
- **Service:** concentra regras de negócio. A transferência é executada em uma transação.
- **Repository:** fornece operações de persistência e consultas específicas.
- **Model:** representa os dados gravados no banco.
- **Exception:** centraliza o tratamento de erros e padroniza respostas HTTP.

## Componentes de domínio

| Classe | Responsabilidade |
|---|---|
| `Cliente` | Dados cadastrais do cliente: nome, CPF, e-mail, senha e data de criação. |
| `Conta` | Conta vinculada por `clienteId`, com número, saldo e status. |
| `Transacao` | Registro de transferência, com contas de origem e destino, tipo, valor e data. |
| `TransferenciaService` | Valida a operação, atualiza os saldos e registra a transação. |
| `GlobalExceptionHandler` | Converte exceções em respostas HTTP com timestamp, status, erro e mensagem. |

As entidades correspondem às tabelas `clientes`, `contas` e `transacoes`. Os identificadores de cliente e conta são armazenados como campos (`clienteId`, `contaOrigemId` e `contaDestinoId`); não há relacionamentos JPA explícitos entre essas entidades no código atual.

O cadastro pela interface cria também um registro em `dados_demo_conta`. Cada nova conta recebe valores fictícios gerados para demonstração: saldo entre `R$ 1.000,00` e `R$ 9.999,99`, limite da conta entre `R$ 500,00` e `R$ 2.500,00`, limite de crédito entre `R$ 3.000,00` e `R$ 10.000,00` e fatura atual entre zero e o limite do cartão. O final do cartão é gerado com quatro dígitos e a chave Pix de demonstração é vinculada ao ID da conta. Esses valores são gravados no banco e não representam saldo ou serviços financeiros reais.

## Endpoints disponíveis

### Clientes

| Método | Caminho | Descrição | Sucesso |
|---|---|---|---|
| `GET` | `/api/v1/clientes` | Lista clientes. | `200 OK` |
| `GET` | `/api/v1/clientes/{id}` | Busca um cliente pelo ID. | `200 OK` |
| `POST` | `/api/v1/clientes` | Cadastra um cliente com validação dos campos. | `201 Created` |
| `POST` | `/api/v1/usuarios` | Cria o cliente e a conta associada em uma única operação. A senha é armazenada como hash. | `201 Created` |
| `POST` | `/api/v1/contas/{id}/encerramento` | Confirma a senha e encerra a conta sem remover o histórico. | `204 No Content` |

Exemplo de cadastro:

```http
POST /api/v1/clientes
Content-Type: application/json
```

```json
{
  "nome": "Ana Silva",
  "cpf": "12345678901",
  "email": "ana@example.com",
  "senhaHash": "valor-da-senha"
}
```

No endpoint simples `POST /api/v1/clientes`, o campo `senhaHash` é aceito na entrada e não é incluído nas respostas JSON; esse endpoint não codifica a senha. Para o cadastro completo da interface, use `POST /api/v1/usuarios`, que recebe a senha e a armazena como hash.

Para abrir uma conta pela interface, use `POST /api/v1/usuarios`. Esse endpoint recebe `nome`, `cpf`, `email`, `telefone`, `senha` e um objeto `conta` com `agencia`, `tipo` (`corrente` ou `poupanca`) e `chavePix` opcional. A senha é codificada no backend; a resposta inclui os IDs do cliente e da conta, além dos dados da conta.

### Transferências

| Método | Caminho | Descrição | Sucesso |
|---|---|---|---|
| `POST` | `/api/v1/transacoes/transferencia` | Transfere valor entre duas contas. | `201 Created` |

Exemplo:

```http
POST /api/v1/transacoes/transferencia
Content-Type: application/json
```

```json
{
  "contaOrigemId": 1,
  "contaDestinoId": 2,
  "valor": 25.50
}
```

A resposta contém `transacaoId`, IDs das contas, valor, status e data/hora. O serviço rejeita IDs ausentes, valor nulo ou não positivo, transferência para a mesma conta, contas inexistentes ou inativas e saldo insuficiente. As contas são bloqueadas em ordem determinística durante a transação para reduzir o risco de deadlock.

A resposta também inclui o nome do beneficiário e o número da conta de destino. Esses dados são gravados como snapshot na transação para preservar a identificação apresentada no comprovante.

### Encerramento de conta

O encerramento requer a senha do titular no corpo da requisição:

```json
{ "senha": "senha-do-titular" }
```

A API muda o status da conta para `INATIVA`; não apaga fisicamente a conta nem as transações, preservando o histórico.

## Tratamento de erros

O `GlobalExceptionHandler` padroniza respostas de erro com `timestamp`, `status`, `erro` e `mensagem` (e, para validação de campos, `erros`).

| Situação | Status HTTP |
|---|---:|
| JSON ou campos inválidos; argumentos inválidos | `400 Bad Request` |
| Cliente ou conta não encontrados | `404 Not Found` |
| Saldo insuficiente ou conta inativa | `422 Unprocessable Entity` |
| Violação de integridade ou dados duplicados | `409 Conflict` |
| Erro inesperado | `500 Internal Server Error` |

## Configuração e execução local

O arquivo `src/main/resources/application.properties` configura a conexão MySQL para o banco `bancodigital`, a porta `8081` e a inicialização do schema das sessões JDBC. As credenciais do banco devem ser fornecidas pelas variáveis de ambiente `DB_USERNAME` e `DB_PASSWORD`; não as grave no repositório.

Pré-requisitos: Java 21 e MySQL com o banco `bancodigital` disponível.

No Windows, na raiz do projeto:

```powershell
$env:DB_USERNAME = "seu_usuario"
$env:DB_PASSWORD = "sua_senha"
.\mvnw.cmd spring-boot:run
```

Para executar os testes:

```powershell
.\mvnw.cmd test
```

## Dependências principais

As dependências são declaradas em `pom.xml`: Spring Boot Web MVC, Spring Data JPA, validação, Spring Session JDBC, Spring Modulith e driver MySQL. O Maven Wrapper (`mvnw.cmd`) permite executar os comandos sem exigir uma instalação global do Maven.

## Testes existentes

- `ApiApplicationTests`: verifica se o contexto Spring inicia.
- `TransferenciaServiceTest`: cobre rejeição de valor nulo e saldo insuficiente, incluindo a ausência de gravações nesses casos.
