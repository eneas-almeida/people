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
│   - PeopleServiceGrpcImpl               │
└─────────────┬───────────────────────────┘
              │
┌─────────────▼───────────────────────────┐
│       Application Layer                 │
│   - PeopleService (interface)           │
│   - PeopleServiceImpl                   │
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
│   - RepositoryConfig                    │
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
   - Services (`PeopleService`, `PeopleServiceImpl`)
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
│   │   │   │   └── service/
│   │   │   │       ├── PeopleService.java          # Interface do serviço
│   │   │   │       └── PeopleServiceImpl.java      # Implementação do serviço
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
│   │   │       │   └── RepositoryConfig.java       # Config do repository
│   │   │       │
│   │   │       ├── entrypoint/grpc/
│   │   │       │   └── PeopleServiceGrpcImpl.java
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
  active-datasource: TYPICODE  # Options: TYPICODE, DUMMY
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

### Verificar Execução

Após iniciar a aplicação, você verá logs indicando que o servidor gRPC está rodando:

```
gRPC Server started, listening on address: *, port: 9090
```

## 🔌 API gRPC

### Endpoints Disponíveis

#### 1. GetPeople
Busca uma pessoa específica por ID.

**Exemplo de Uso com grpcurl:**
```bash
grpcurl -plaintext -d '{"id": 1}' localhost:9090 grpcservice.PeopleService/GetPeople
```

**Resposta Esperada:**
```json
{
  "id": 1,
  "name": "Leanne Graham",
  "email": "Sincere@april.biz"
}
```

#### 2. ListPeople
Lista todas as pessoas disponíveis.

**Exemplo de Uso com grpcurl:**
```bash
grpcurl -plaintext localhost:9090 grpcservice.PeopleService/ListPeople
```

### Testando com grpcurl

```bash
# Listar serviços disponíveis
grpcurl -plaintext localhost:9090 list

# Descrever um serviço
grpcurl -plaintext localhost:9090 describe grpcservice.PeopleService
```

## 🌐 APIs Externas Suportadas

### DummyJSON (Fonte: DUMMY)
- **URL Base:** `https://dummyjson.com`
- **Usuários disponíveis:** ~200
- **Documentação:** https://dummyjson.com/docs/users

### JSONPlaceholder (Fonte: TYPICODE)
- **URL Base:** `https://jsonplaceholder.typicode.com`
- **Usuários disponíveis:** 10
- **Documentação:** https://jsonplaceholder.typicode.com/guide/

## 🔧 Detalhes Técnicos

### Padrão Service

O projeto utiliza **PeopleService** como camada de aplicação:

```java
@Service
@RequiredArgsConstructor
public class PeopleServiceImpl implements PeopleService {

    private final PeopleRepository peopleRepository;

    @Override
    public Mono<PeopleResponse> getById(Integer id) {
        return peopleRepository.findById(id);
    }

    @Override
    public Flux<PeopleResponse> listAll() {
        return peopleRepository.findAll();
    }
}
```

### Padrão Strategy

O projeto utiliza o **padrão Strategy** via `PeopleRepositoryImpl` para permitir troca dinâmica entre diferentes APIs externas:

```java
@RequiredArgsConstructor
public class PeopleRepositoryImpl implements PeopleRepository {

    private final Map<DataSource, PeopleClient> clientStrategies;
    private final DataSource activeDataSource;

    @Override
    public Mono<PeopleResponse> findById(Integer id) {
        return getActiveClient().getPeopleById(id);
    }

    private PeopleClient getActiveClient() {
        return clientStrategies.get(activeDataSource);
    }
}
```

### Inversão de Dependência

O gRPC Service injeta a interface do serviço:

```java
@GrpcService
@RequiredArgsConstructor
public class PeopleServiceGrpcImpl extends ReactorPeopleServiceGrpc.PeopleServiceImplBase {

    private final PeopleService peopleService;  // Interface!

    @Override
    public Mono<PeopleResponseGrpc> getPeople(Mono<PeopleRequestGrpc> request) {
        return request.flatMap(req -> peopleService.getById(req.getId()))
            .map(people -> PeopleResponseGrpc.newBuilder()
                .setId(people.getId())
                .setName(people.getName())
                .setEmail(people.getEmail())
                .build());
    }
}
```

### MapStruct

O MapStruct é utilizado para conversão type-safe:

```java
@Mapper(
    componentModel = "spring",
    implementationName = "DummyMapperImpl"
)
public interface DummyMapper {

    @Mapping(target = "name",
        expression = "java(response.firstName() + \" \" + response.lastName())")
    PeopleResponse toPeopleResponse(DummyResponse response);
}
```

### Lombok

Construtores são gerados automaticamente via Lombok:

```java
@Service
@RequiredArgsConstructor  // Gera construtor com campos final
public class PeopleServiceImpl implements PeopleService {
    private final PeopleRepository peopleRepository;
}
```

## 📦 Build e Deploy

### Gerar Artefato de Produção

```bash
mvn clean package -DskipTests
```

### Docker (Exemplo)

```dockerfile
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY target/people-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 9090

ENV ACTIVE_DATASOURCE=TYPICODE
ENV SPRING_PROFILE=prod

ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Build e Run:**
```bash
docker build -t people-service:latest .
docker run -p 9090:9090 people-service:latest
```

## 📝 Licença

Este projeto é um exemplo educacional e está disponível para uso livre.

---

**Desenvolvido com Java 21 + Spring Boot + gRPC + WebFlux + MapStruct + Lombok**
