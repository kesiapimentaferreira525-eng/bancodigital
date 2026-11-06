# Banco Digital — aplicação de demonstração

Aplicação de demonstração bancária com interface Angular, API REST em Java/Spring Boot e persistência MySQL. Permite cadastrar clientes e contas, consultar os dados de demonstração, fazer transferências entre contas cadastradas e encerrar uma conta sem apagar seu histórico.

> **Importante:** este projeto é apenas demonstrativo. Saldos, limites, faturas, cartões e chaves Pix são fictícios. Não há integração com instituições financeiras, pagamentos ou Pix real. Não use dados financeiros ou credenciais reais.

## Projetos

O sistema é composto por dois diretórios que podem ser mantidos em repositórios separados:

| Diretório | Responsabilidade | Tecnologias |
|---|---|---|
| `bancodigital` | API, regras de negócio e acesso ao banco | Java 21, Spring Boot, Spring Data JPA, MySQL |
| `bancodigital-front` | Cadastro, painel, transferências e encerramento | Angular 18, TypeScript |

O navegador não acessa o MySQL diretamente. O Angular chama a API; a API valida as operações e usa Spring Data JPA para ler e gravar no banco.

```text
Angular (localhost:4200)
        │ /api/* — proxy de desenvolvimento
        ▼
Spring Boot (localhost:8081)
        │ JDBC / Spring Data JPA
        ▼
MySQL (localhost:3306, banco bancodigital)
```

## Funcionalidades

### Cadastro e abertura de conta

O formulário coleta nome, CPF, e-mail, telefone, senha, agência, tipo de conta e uma chave Pix opcional. A interface formata CPF e telefone e valida os campos antes do envio; o backend também valida a requisição e rejeita CPF ou e-mail já cadastrados. Após o cadastro, o backend cria o cliente, a conta e os dados de demonstração em uma única transação. A senha é armazenada como hash PBKDF2, nunca como texto puro.

O cadastro bem-sucedido abre o painel da conta criada. O painel exibe agência, número e ID da conta, saldo fictício, limite da conta, dados fictícios do cartão, fatura e chave Pix.

Cada novo cadastro recebe valores de demonstração gerados dentro destas faixas:

| Campo | Faixa |
|---|---:|
| Saldo da conta | R$ 1.000,00 a R$ 9.999,99 |
| Limite da conta | R$ 500,00 a R$ 2.500,00 |
| Limite de crédito | R$ 3.000,00 a R$ 10.000,00 |
| Fatura atual | R$ 0,00 até o limite de crédito |
| Cartão | Quatro dígitos finais fictícios |
| Chave Pix demonstrativa | `demo-<id-da-conta>@bancodigital.local` |

Não é gerado nem armazenado número completo de cartão ou CVV. Os valores ficam registrados no MySQL e não representam dinheiro ou crédito reais.

### Transferências de demonstração

A transferência exige o **ID interno** das contas de origem e destino — não o número da conta — e um valor maior que zero. O backend valida que as contas existem, estão ativas, são diferentes e que há saldo suficiente. Em uma transação, atualiza os dois saldos e registra a operação.

O comprovante apresenta o beneficiário e o número da conta de destino. Nome e número são salvos como snapshot na transação, preservando a identificação usada no comprovante.

### Sair e encerrar conta

- **Sair:** retorna à tela de cadastro e limpa o painel atual no navegador. Não altera nem remove registros no MySQL; a conta continua ativa.
- **Encerrar conta:** solicita a senha, valida-a e muda o status da conta para `INATIVA`. A conta e o histórico são mantidos; novas operações são bloqueadas.

O projeto ainda não tem fluxo de autenticação/login para retornar a uma conta existente. Depois de sair, é possível iniciar um novo cadastro; os dados anteriores continuam no banco. CPF e e-mail já cadastrados não podem ser reutilizados.

## Persistência no MySQL

Crie o banco antes de iniciar a API:

```sql
CREATE DATABASE bancodigital
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

O Hibernate está configurado com `spring.jpa.hibernate.ddl-auto=update` e cria/atualiza as tabelas mapeadas ao iniciar a aplicação. As entidades principais são:

| Tabela | Conteúdo |
|---|---|
| `clientes` | Dados cadastrais e hash da senha |
| `contas` | Agência, número, tipo, chave Pix, saldo, limite e status |
| `dados_demo_conta` | Limite de crédito, fatura, final do cartão e chave Pix de demonstração |
| `transacoes` | Origem, destino, beneficiário, valor e data/hora |

Os IDs de cliente/conta são armazenados nos modelos como campos simples; não há relacionamentos JPA explícitos entre essas entidades. As tabelas de sessão JDBC, se inicializadas pelo Spring, são distintas das tabelas dos dados bancários.

As credenciais do MySQL são lidas de `DB_USERNAME` e `DB_PASSWORD`. Não coloque valores reais dessas variáveis no código, neste README ou em arquivos versionados.

## Executar localmente no Windows

### Pré-requisitos

- Java 21
- MySQL em execução e banco `bancodigital` criado
- Node.js 20 LTS e npm

### 1. Iniciar a API

Abra um terminal PowerShell na pasta `bancodigital`:

```powershell
$env:DB_USERNAME = "seu_usuario_mysql"
$env:DB_PASSWORD = "sua_senha_mysql"
.\mvnw.cmd spring-boot:run
```

A API fica em `http://localhost:8081`. No IntelliJ, configure as mesmas variáveis `DB_USERNAME` e `DB_PASSWORD` em **Run/Debug Configurations → Environment variables**.

Execute apenas uma instância do backend por vez: se a porta `8081` já estiver ocupada, pare a execução anterior no IntelliJ ou no terminal antes de iniciar outra.

### 2. Iniciar a interface

Em outro terminal, na pasta `bancodigital-front`:

```powershell
npm install
npm start
```

Abra `http://localhost:4200`. O arquivo `proxy.conf.json` encaminha chamadas `/api` para `http://localhost:8081`; por isso, o backend precisa estar ativo. A interface e a API devem ser iniciadas em terminais separados.

## Endpoints da API

Todos os caminhos abaixo usam o prefixo `http://localhost:8081`.

| Método | Caminho | Descrição |
|---|---|---|
| `POST` | `/api/v1/usuarios` | Cria cliente, conta e dados de demonstração; retorna os dados para abrir o painel. |
| `GET` | `/api/v1/clientes` | Lista clientes cadastrados. |
| `GET` | `/api/v1/clientes/{id}` | Busca um cliente pelo ID. |
| `POST` | `/api/v1/transacoes/transferencia` | Valida e registra uma transferência. |
| `POST` | `/api/v1/contas/{id}/encerramento` | Encerra logicamente a conta após validar a senha. |
| `POST` | `/api/v1/clientes` | Endpoint simples/legado de cadastro de cliente; não é usado pelo formulário Angular. |

Exemplo do corpo para abrir uma conta em `/api/v1/usuarios`:

```json
{
  "nome": "Cliente de Demonstração",
  "cpf": "12345678909",
  "email": "cliente@example.test",
  "telefone": "11999999999",
  "senha": "SenhaDemo#2026",
  "conta": {
    "agencia": "0001",
    "tipo": "corrente",
    "chavePix": null
  }
}
```

Use dados fictícios e únicos ao testar; CPF e e-mail duplicados são rejeitados. O endpoint simples `POST /api/v1/clientes` não codifica a senha recebida e não deve ser usado para cadastro normal.

### Respostas de erro

Os erros da API são normalizados pelo backend. Em geral, campos inválidos retornam `400`, registros não encontrados retornam `404`, dados duplicados retornam `409`, e saldo insuficiente ou conta inativa retornam `422`. A resposta inclui uma mensagem adequada para a interface.

## Testes e build

Na pasta do backend:

```powershell
.\mvnw.cmd test
```

Na pasta do frontend:

```powershell
npm run build
```

Os testes do backend cobrem inicialização da aplicação, cadastro, transferência e encerramento de conta.

## Estrutura principal

```text
bancodigital/
├── src/main/java/bancodigital/api/
│   ├── Controller/   # Endpoints REST
│   ├── DTO/          # Contratos de entrada e saída
│   ├── Exception/    # Erros e respostas padronizadas
│   ├── Model/        # Entidades persistidas
│   ├── Repository/   # Repositórios Spring Data JPA
│   └── Service/      # Regras de negócio e transações
├── src/main/resources/application.properties
└── src/test/java/    # Testes do backend

bancodigital-front/
├── src/app/cadastro/       # Formulário de cadastro
├── src/app/core/           # Serviços HTTP da API
├── src/app/transferencia/  # Painel, transferência e encerramento
└── proxy.conf.json         # Proxy /api para o backend local
```

O documento [Estrutura do backend](docs/ESTRUTURA_BACKEND.md) contém detalhes adicionais sobre camadas, regras e respostas de erro.

## Limitações e segurança

Este código é um protótipo didático, não um sistema bancário de produção. Ainda não há autenticação/login para usuários. Os endpoints de clientes não possuem controle de acesso e podem expor dados cadastrais; não publique a API na internet nem use dados reais. O encerramento é lógico, e não há opção de apagar permanentemente clientes ou contas pela interface.