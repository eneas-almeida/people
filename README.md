# People Service

Serviço de gerenciamento de usuários baseado em gRPC que consome dados de APIs públicas (DummyJSON ou JSONPlaceholder). O projeto foi desenvolvido utilizando Spring Boot e segue princípios de Clean Architecture com suporte a múltiplas fontes de dados.

## 📋 Índice

- [Visão Geral](#-visão-geral)
- [Arquitetura](#-arquitetura)
- [Tecnologias](#-tecnologias)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Pré-requisitos](#-pré-requisitos)
- [Configuração](#-configuração)
- [Como Executar](#-como-executar)
- [API gRPC](#-api-grpc)
- [APIs Externas Suportadas](#-apis-externas-suportadas)
- [Detalhes Técnicos](#-detalhes-técnicos)
- [Build e Deploy](#-build-e-deploy)

## 🎯 Visão Geral

O **People Service** é um microserviço que expõe uma API gRPC para consulta de informações de usuários. O serviço atua como um intermediário entre clientes gRPC e APIs REST públicas (DummyJSON ou JSONPlaceholder), aplicando os conceitos de Clean Architecture e o padrão Strategy para permitir troca fácil entre diferentes fontes de dados.

### Funcionalidades Principais

- **Buscar pessoa por ID**: Retorna informações detalhadas de uma pessoa específica
- **Listar todas as pessoas**: Retorna uma lista com todas as pessoas disponíveis
- **Múltiplas fontes de dados**: Suporte a DummyJSON e JSONPlaceholder via padrão Strategy
- **Comunicação reativa**: Utiliza WebFlux para chamadas HTTP não-bloqueantes
- **Interface gRPC**: API de alto desempenho para comunicação entre serviços
- **Logging estruturado**: Sistema de logs com correlation IDs e contexto de requisição
- **Tratamento robusto de erros**: Hierarquia de exceções customizadas e retry com backoff

## 🏗 Arquitetura

O projeto segue os princípios da **Clean Architecture**, organizando o código em camadas bem definidas:

```
┌─────────────────────────────────────────┐
│      Entrypoint (gRPC Service)          │
│   - PeopleGrpcServiceImpl               │
└─────────────┬───────────────────────────┘
              │
┌─────────────▼───────────────────────────┐
│       Application Layer                 │
│   - GetPeopleUseCaseImpl                │
│   - ListPeopleUseCaseImpl               │
└─────────────┬───────────────────────────┘
              │
┌─────────────▼───────────────────────────┐
│       Domain Layer                      │
│   - People (Entity)                     │
│   - PeopleClient (Interface)            │
│   - PeopleRepository (Interface)        │
│   - DataSource (Enum)                   │
└─────────────┬───────────────────────────┘
              │
┌─────────────▼───────────────────────────┐
│      Infrastructure Layer               │
│   Repository:                           │
│   - PeopleRepositoryImpl (Strategy)     │
│                                         │
│   Clients:                              │
│   - DummyClientImpl                     │
│   - TypiCodeClientImpl                  │
│                                         │
│   Configs:                              │
│   - DummyClientConfig                   │
│   - TypiCodeClientConfig                │
└─────────────────────────────────────────┘
```

### Camadas

1. **Domain** (`org.people.domain`)
   - Contém as entidades de negócio (`People`)
   - Interfaces de cliente (`PeopleClient`) e repositório (`PeopleRepository`)
   - Exceções de negócio (`PeopleException`, `PeopleNotFoundException`, etc.)
   - Enums (`DataSource`)
   - Livre de dependências externas

2. **Application** (`org.people.application`)
   - Implementa a lógica de negócio da aplicação
   - DTOs de aplicação (`PeopleResponse`)
   - Use Cases (`GetPeopleUseCaseImpl`, `ListPeopleUseCaseImpl`)
   - Orquestra as interações entre domain e infrastructure

3. **Infrastructure** (`org.people.infrastructure`)
   - **Clients**: Implementações concretas dos clientes de API
     - `dummy/`: Cliente para DummyJSON API
     - `typicode/`: Cliente para JSONPlaceholder API
   - **Repository**: Implementação do padrão Strategy (`PeopleRepositoryImpl`)
   - **Entrypoints**: Pontos de entrada da aplicação (gRPC)
   - **Config**: Configurações e beans do Spring
   - **Exception**: Exceções de infraestrutura
   - **Logging**: Sistema de logging estruturado

## 🚀 Tecnologias

### Core
- **Java 21**: Versão LTS mais recente com recursos modernos
- **Spring Boot 3.3.3**: Framework principal para desenvolvimento
- **Maven**: Gerenciamento de dependências e build

### Comunicação
- **gRPC 1.59.0**: Framework RPC de alto desempenho
- **Protocol Buffers 3.24.4**: Serialização de dados
- **Spring WebFlux**: Cliente HTTP reativo e não-bloqueante
- **Reactor gRPC 1.2.4**: gRPC Reativo

### Utilitários
- **Lombok**: Redução de boilerplate code
- **MapStruct 1.5.5**: Mapeamento automático entre objetos
- **Logstash Logback Encoder 7.4**: Logging estruturado em JSON
- **Datadog Trace API 1.30.1**: Observabilidade e tracing distribuído

### Programação Reativa
- **Project Reactor**: Implementação do Reactive Streams
  - `Mono<T>`: Para operações que retornam 0 ou 1 elemento
  - `Flux<T>`: Para operações que retornam 0 a N elementos

## 📁 Estrutura do Projeto

```
people/
├── src/
│   ├── main/
│   │   ├── java/org/people/
│   │   │   ├── PeopleApplication.java
│   │   │   │
│   │   │   ├── domain/
│   │   │   │   ├── client/
│   │   │   │   │   └── PeopleClient.java           # Interface do cliente
│   │   │   │   ├── entity/
│   │   │   │   │   └── People.java                 # Entidade de domínio
│   │   │   │   ├── enums/
│   │   │   │   │   └── DataSource.java             # Enum de fontes de dados
│   │   │   │   ├── exception/
│   │   │   │   │   ├── PeopleException.java
│   │   │   │   │   ├── BusinessRuleException.java
│   │   │   │   │   ├── ValidationException.java
│   │   │   │   │   └── PeopleNotFoundException.java
│   │   │   │   └── repository/
│   │   │   │       └── PeopleRepository.java       # Interface do repositório
│   │   │   │
│   │   │   ├── application/
│   │   │   │   ├── dto/
│   │   │   │   │   └── PeopleResponse.java         # DTO de resposta
│   │   │   │   └── usecase/
│   │   │   │       ├── GetPeopleUseCaseImpl.java
│   │   │   │       └── ListPeopleUseCaseImpl.java
│   │   │   │
│   │   │   └── infrastructure/
│   │   │       ├── client/
│   │   │       │   ├── dummy/
│   │   │       │   │   ├── DummyClientImpl.java
│   │   │       │   │   ├── DummyMapper.java
│   │   │       │   │   ├── DummyResponse.java
│   │   │       │   │   └── DummyListResponse.java
│   │   │       │   └── typicode/
│   │   │       │       ├── TypiCodeClientImpl.java
│   │   │       │       ├── TypiCodeMapper.java
│   │   │       │       └── TypiCodeResponse.java
│   │   │       │
│   │   │       ├── repository/
│   │   │       │   └── PeopleRepositoryImpl.java   # Padrão Strategy
│   │   │       │
│   │   │       ├── config/
│   │   │       │   ├── client/
│   │   │       │   │   ├── DummyClientConfig.java
│   │   │       │   │   └── TypiCodeClientConfig.java
│   │   │       │   └── usecase/
│   │   │       │       └── UseCaseConfig.java
│   │   │       │
│   │   │       ├── entrypoint/grpc/
│   │   │       │   └── PeopleGrpcServiceImpl.java
│   │   │       │
│   │   │       ├── exception/
│   │   │       │   ├── GlobalGrpcExceptionHandler.java
│   │   │       │   ├── ExternalServiceException.java
│   │   │       │   └── InternalServerException.java
│   │   │       │
│   │   │       └── logging/
│   │   │           ├── Logger.java
│   │   │           ├── LogContext.java
│   │   │           ├── RequestContext.java
│   │   │           └── GrpcLoggingInterceptor.java
│   │   │
│   │   ├── proto/
│   │   │   └── person.proto                        # Definição do serviço gRPC
│   │   │
│   │   └── resources/
│   │       └── application.yml                     # Configurações da aplicação
│   │
│   └── test/
│       └── java/org/people/
│           └── PeopleApplicationTests.java
│
├── target/                                         # Arquivos compilados
├── pom.xml                                         # Configuração Maven
├── mvnw                                            # Maven Wrapper (Unix)
└── mvnw.cmd                                        # Maven Wrapper (Windows)
```

## 📋 Pré-requisitos

- **Java Development Kit (JDK) 21** ou superior
- **Maven 3.6+** (ou utilize o Maven Wrapper incluído no projeto)
- **Git** (para clonar o repositório)
- Conexão com a internet (para acessar as APIs externas)

### Verificar Instalações

```bash
# Verificar versão do Java
java -version

# Verificar versão do Maven
mvn -version
```

## ⚙ Configuração

### Arquivo application.yml

```yaml
spring:
  application:
    name: people
  profiles:
    active: ${SPRING_PROFILE:local}

grpc:
  server:
    port: 9090

client:
  active-datasource: DUMMY  # Options: TYPICODE, DUMMY
  typicode:
    base-url: https://jsonplaceholder.typicode.com
  dummy:
    base-url: https://dummyjson.com

logging:
  level:
    root: INFO
    org.people: DEBUG
    io.grpc: INFO
    net.devh: INFO
```

### Seleção de Fonte de Dados

A aplicação suporta duas APIs externas. Para alterar a fonte de dados, modifique a propriedade `client.active-datasource`:

```yaml
# Para usar DummyJSON
client:
  active-datasource: DUMMY

# Para usar JSONPlaceholder
client:
  active-datasource: TYPICODE
```

Ou defina via variável de ambiente:

```bash
export ACTIVE_DATASOURCE=DUMMY
```

### Customização de URLs

Para alterar as URLs base das APIs externas:

```yaml
client:
  typicode:
    base-url: https://sua-api-typicode.exemplo.com
  dummy:
    base-url: https://sua-api-dummy.exemplo.com
```

## 🏃 Como Executar

### Usando Maven Wrapper (Recomendado)

#### Windows
```cmd
# Limpar e compilar o projeto
.\mvnw.cmd clean install

# Executar a aplicação
.\mvnw.cmd spring-boot:run
```

#### Unix/Linux/MacOS
```bash
# Limpar e compilar o projeto
./mvnw clean install

# Executar a aplicação
./mvnw spring-boot:run
```

### Usando Maven Local

```bash
# Limpar e compilar
mvn clean install

# Executar
mvn spring-boot:run
```

### Executando o JAR Gerado

```bash
# Gerar o JAR
mvn clean package

# Executar o JAR
java -jar target/people-0.0.1-SNAPSHOT.jar
```

### Executar com Configuração Customizada

```bash
java -jar \
  -Dclient.active-datasource=DUMMY \
  -Dlogging.level.org.people=DEBUG \
  target/people-0.0.1-SNAPSHOT.jar
```

### Verificar Execução

Após iniciar a aplicação, você verá logs indicando que o servidor gRPC está rodando:

```
gRPC Server started, listening on address: *, port: 9090
```

## 🔌 API gRPC

### Definição do Serviço (Protocol Buffers)

O serviço gRPC está definido em `src/main/proto/person.proto`:

```protobuf
syntax = "proto3";

option java_package = "com.people.grpc";
option java_outer_classname = "ServiceProto";

package grpcservice;

service PeopleService {
  rpc GetPeople (PeopleRequestGrpc) returns (PeopleResponseGrpc);
  rpc ListPeople (ListPeopleRequestGrpc) returns (ListPeopleResponseGrpc);
}

message PeopleRequestGrpc {
  int32 id = 1;
}

message ListPeopleRequestGrpc {}

message PeopleResponseGrpc {
  int32 id = 1;
  string name = 2;
  string email = 3;
}

message ListPeopleResponseGrpc {
  repeated PeopleResponseGrpc people = 1;
}
```

### Endpoints Disponíveis

#### 1. GetPeople
Busca uma pessoa específica por ID.

**Request:**
```protobuf
message PeopleRequestGrpc {
  int32 id = 1;
}
```

**Response:**
```protobuf
message PeopleResponseGrpc {
  int32 id = 1;
  string name = 2;
  string email = 3;
}
```

**Exemplo de Uso com grpcurl:**
```bash
grpcurl -plaintext -d '{"id": 1}' localhost:9090 grpcservice.PeopleService/GetPeople
```

**Resposta Esperada (DummyJSON):**
```json
{
  "id": 1,
  "name": "Emily Johnson",
  "email": "emily.johnson@x.dummyjson.com"
}
```

**Resposta Esperada (JSONPlaceholder):**
```json
{
  "id": 1,
  "name": "Leanne Graham",
  "email": "Sincere@april.biz"
}
```

#### 2. ListPeople
Lista todas as pessoas disponíveis.

**Request:**
```protobuf
message ListPeopleRequestGrpc {}
```

**Response:**
```protobuf
message ListPeopleResponseGrpc {
  repeated PeopleResponseGrpc people = 1;
}
```

**Exemplo de Uso com grpcurl:**
```bash
grpcurl -plaintext localhost:9090 grpcservice.PeopleService/ListPeople
```

### Testando com grpcurl

Para testar os endpoints, você pode usar a ferramenta [grpcurl](https://github.com/fullstorydev/grpcurl):

```bash
# Instalar grpcurl (exemplo para Linux/Mac)
go install github.com/fullstorydev/grpcurl/cmd/grpcurl@latest

# Listar serviços disponíveis
grpcurl -plaintext localhost:9090 list

# Descrever um serviço
grpcurl -plaintext localhost:9090 describe grpcservice.PeopleService

# Listar métodos do serviço
grpcurl -plaintext localhost:9090 list grpcservice.PeopleService
```

### Tratamento de Erros

O serviço retorna status codes gRPC padrão:

- **OK (0)**: Operação bem-sucedida
- **NOT_FOUND (5)**: Pessoa não encontrada
- **INVALID_ARGUMENT (3)**: Argumentos inválidos (validação)
- **FAILED_PRECONDITION (9)**: Violação de regra de negócio
- **UNAVAILABLE (14)**: Serviço externo indisponível
- **INTERNAL (13)**: Erro interno não esperado

## 🌐 APIs Externas Suportadas

### DummyJSON (Fonte: DUMMY)

**URL Base:** `https://dummyjson.com`

**Endpoints utilizados:**
- `GET /users/{id}` - Buscar usuário por ID
- `GET /users` - Listar todos os usuários

**Estrutura de resposta:**
```json
{
  "id": 1,
  "firstName": "Emily",
  "lastName": "Johnson",
  "email": "emily.johnson@x.dummyjson.com"
}
```

**Características:**
- API gratuita com dados realistas
- Maior quantidade de usuários disponíveis (~200)
- Campos separados (firstName, lastName) - combinados pelo mapper

**Documentação:** https://dummyjson.com/docs/users

### JSONPlaceholder (Fonte: TYPICODE)

**URL Base:** `https://jsonplaceholder.typicode.com`

**Endpoints utilizados:**
- `GET /users/{id}` - Buscar usuário por ID
- `GET /users` - Listar todos os usuários

**Estrutura de resposta:**
```json
{
  "id": 1,
  "name": "Leanne Graham",
  "email": "Sincere@april.biz",
  "username": "Bret",
  "address": {...},
  "phone": "1-770-736-8031 x56442",
  "website": "hildegard.org",
  "company": {...}
}
```

**Características:**
- API gratuita e pública para testes
- 10 usuários disponíveis
- Campo name já unificado
- Dados adicionais (endereço, telefone, etc.)

**Documentação:** https://jsonplaceholder.typicode.com/guide/

### Trocar de API

Altere no `application.yml`:

```yaml
client:
  active-datasource: DUMMY  # ou TYPICODE
```

Ou via variável de ambiente:

```bash
export ACTIVE_DATASOURCE=TYPICODE
java -jar target/people-0.0.1-SNAPSHOT.jar
```

## 🔧 Detalhes Técnicos

### Padrão Strategy

O projeto utiliza o **padrão Strategy** via `PeopleRepositoryImpl` para permitir troca dinâmica entre diferentes APIs externas:

```java
@Component
public class PeopleRepositoryImpl implements PeopleRepository {

    private final Map<DataSource, PeopleClient> clientStrategies;
    private final DataSource activeDataSource;

    public PeopleRepositoryImpl(
            Map<DataSource, PeopleClient> clientStrategies,
            DataSource activeDataSource) {
        this.clientStrategies = clientStrategies;
        this.activeDataSource = activeDataSource;
    }

    @Override
    public Mono<PeopleResponse> findById(Integer id) {
        return getActiveClient().findById(id);
    }

    private PeopleClient getActiveClient() {
        return clientStrategies.get(activeDataSource);
    }
}
```

**Benefícios:**
- Fácil adição de novas APIs
- Troca de fonte de dados sem alterar código
- Testabilidade aumentada
- Baixo acoplamento

### Programação Reativa

O projeto utiliza **Project Reactor** para operações assíncronas e não-bloqueantes:

```java
// Exemplo de uso de Mono
public Mono<PeopleResponse> findById(Integer id) {
    return webClient
        .get()
        .uri("/users/{id}", id)
        .retrieve()
        .bodyToMono(DummyResponse.class)
        .map(response -> mapper.toPeopleResponse(response));
}

// Exemplo de uso de Flux
public Flux<PeopleResponse> listAll() {
    return webClient
        .get()
        .uri("/users")
        .retrieve()
        .bodyToMono(DummyListResponse.class)
        .flatMapMany(response -> Flux.fromIterable(response.users()))
        .map(user -> mapper.toPeopleResponse(user));
}
```

**Vantagens:**
- Maior eficiência no uso de recursos
- Melhor escalabilidade
- Não bloqueia threads durante operações I/O
- Suporte a backpressure

### Logging Estruturado

O serviço implementa logging estruturado com:

**Logger customizado:**
```java
private static final Logger logger = Logger.getLogger(TypiCodeClientImpl.class);

logger.info("Fetching people by id from external API - id: {}, requestId: {}",
    id, requestId);
```

**Contexto de log:**
```java
LogContext.add("people_id", String.valueOf(id));
LogContext.add("operation", "findById");
```

**Interceptor gRPC:**
- Adiciona correlation IDs automaticamente
- Registra duração de requisições
- Captura erros e status codes

**Formato de saída:**
- JSON estruturado via Logstash Logback Encoder
- Compatível com ELK Stack, Datadog, etc.
- Request IDs para rastreabilidade

### Retry e Resiliência

O cliente TypiCode implementa retry com backoff exponencial:

```java
.retryWhen(Retry.backoff(2, Duration.ofMillis(100))
    .filter(this::isRetryableException)
    .doBeforeRetry(retrySignal ->
        logger.warn("Retrying request - attempt: {}",
            retrySignal.totalRetries() + 1)))
```

**Política de retry:**
- Apenas para erros 5xx (servidor)
- Não faz retry para 404 (not found)
- Backoff exponencial: 100ms, 200ms
- Máximo de 2 tentativas

### MapStruct

O MapStruct é utilizado para conversão type-safe entre DTOs e entidades:

```java
@Mapper(
    componentModel = "spring",
    implementationName = "DummyMapperImpl"
)
public interface DummyMapper {

    @Mapping(target = "name",
        expression = "java(response.firstName() + \" \" + response.lastName())")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "id", source = "id")
    PeopleResponse toPeopleResponse(DummyResponse response);
}
```

**Benefícios:**
- Geração de código em tempo de compilação
- Type-safe
- Alto desempenho (sem reflexão)
- Mapeamentos customizados via expressions

### Hierarquia de Exceções

```
PeopleException (Domain - Base)
├── BusinessRuleException
├── ValidationException
└── PeopleNotFoundException

Infrastructure Exceptions
├── ExternalServiceException
└── InternalServerException
```

**GlobalGrpcExceptionHandler:**
- Intercepta exceções dos serviços gRPC
- Converte para status codes apropriados
- Adiciona mensagens de erro estruturadas
- Registra erros no sistema de logging

### WebClient Configuração

Cada API externa possui sua própria configuração de WebClient:

```java
@Configuration
public class DummyClientConfig {

    @Bean
    @Qualifier("dummyWebClient")
    public WebClient dummyWebClient(
            WebClient.Builder builder,
            @Value("${client.dummy.base-url}") String baseUrl) {

        ExchangeStrategies strategies = ExchangeStrategies.builder()
            .codecs(c -> c.defaultCodecs()
                .maxInMemorySize(5242880))
            .build();

        return builder
            .baseUrl(baseUrl)
            .exchangeStrategies(strategies)
            .filter(addRequestIdHeader())
            .filter(logRequest())
            .filter(logResponse())
            .build();
    }
}
```

**Características:**
- Non-blocking I/O
- Filtros customizados (headers, logging)
- Estratégias de codec configuráveis
- Buffer size configurável

## 📦 Build e Deploy

### Gerar Artefato de Produção

```bash
# Compilar sem executar testes
mvn clean package -DskipTests

# Compilar com testes
mvn clean package
```

O JAR executável será gerado em: `target/people-0.0.1-SNAPSHOT.jar`

### Executar em Produção

```bash
java -jar \
  -Dspring.profiles.active=prod \
  -Dclient.active-datasource=DUMMY \
  -Dlogging.level.org.people=INFO \
  target/people-0.0.1-SNAPSHOT.jar
```

### Docker (Exemplo de Dockerfile)

```dockerfile
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/people-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 9090

# Variáveis de ambiente
ENV ACTIVE_DATASOURCE=DUMMY
ENV SPRING_PROFILE=prod

ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Build e Run:**
```bash
# Build da imagem
docker build -t people-service:latest .

# Executar container
docker run -p 9090:9090 \
  -e ACTIVE_DATASOURCE=TYPICODE \
  people-service:latest
```

### Docker Compose (Exemplo)

```yaml
version: '3.8'

services:
  people-service:
    build: .
    ports:
      - "9090:9090"
    environment:
      - ACTIVE_DATASOURCE=DUMMY
      - SPRING_PROFILE=prod
      - LOGGING_LEVEL_ORG_PEOPLE=INFO
    healthcheck:
      test: ["CMD", "grpcurl", "-plaintext", "localhost:9090", "list"]
      interval: 30s
      timeout: 10s
      retries: 3
```

## 🧪 Testes

### Executar Todos os Testes

```bash
# Com Maven Wrapper
./mvnw test

# Com Maven local
mvn test
```

### Executar Testes com Cobertura

```bash
mvn clean test jacoco:report
```

O relatório de cobertura estará disponível em: `target/site/jacoco/index.html`

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/MinhaFeature`)
3. Commit suas mudanças (`git commit -m 'Adiciona MinhaFeature'`)
4. Push para a branch (`git push origin feature/MinhaFeature`)
5. Abra um Pull Request

### Padrões de Código

- Seguir convenções Java padrão
- Manter Clean Architecture
- Adicionar testes para novas funcionalidades
- Documentar código complexo
- Usar logging estruturado

### Adicionando Nova API Externa

Para adicionar uma nova fonte de dados:

1. Criar novo valor no enum `DataSource`
2. Criar implementação de `PeopleClient` no pacote adequado
3. Criar mapper MapStruct para conversão de DTOs
4. Criar configuração de WebClient
5. Registrar no `UseCaseConfig`
6. Adicionar configurações no `application.yml`

## 📝 Licença

Este projeto é um exemplo educacional e está disponível para uso livre.

## 📧 Contato

Para dúvidas ou sugestões, abra uma issue no repositório.

---

**Desenvolvido com Java 21 + Spring Boot + gRPC + WebFlux**
