# Documentação gRPC - People Service

Guia completo sobre a implementação de gRPC no People Service, incluindo configuração, Protocol Buffers, geração de código e integração com Spring Boot.

## 📋 Índice

- [Introdução ao gRPC](#introdução-ao-grpc)
- [Dependências Maven](#dependências-maven)
- [Protocol Buffers](#protocol-buffers)
- [Estrutura de Arquivos](#estrutura-de-arquivos)
- [Implementação do Servidor](#implementação-do-servidor)
- [Configuração Spring Boot](#configuração-spring-boot)
- [Logging e Interceptors](#logging-e-interceptors)
- [Tratamento de Erros](#tratamento-de-erros)
- [Testando o Serviço](#testando-o-serviço)

---

## Introdução ao gRPC

**gRPC** (gRPC Remote Procedure Call) é um framework RPC moderno e de alto desempenho desenvolvido pelo Google que pode rodar em qualquer ambiente.

### Principais Características

- **Protocol Buffers**: Serialização binária eficiente
- **HTTP/2**: Multiplexação de requisições, streaming bidirecional
- **Contratos fortemente tipados**: Definição clara de APIs via `.proto`
- **Geração automática de código**: Stubs client e server
- **Multi-linguagem**: Suporte para diversas linguagens de programação

### Quando Usar gRPC

✅ **Use gRPC quando:**
- Comunicação entre microserviços
- Necessita de baixa latência e alto throughput
- Precisa de contratos fortemente tipados
- Streaming de dados (unidirecional ou bidirecional)
- Comunicação polyglot (múltiplas linguagens)

❌ **Evite gRPC quando:**
- Clientes web diretos (use gRPC-Web ou REST)
- APIs públicas para consumo externo
- Depuração visual é crítica (ferramentas REST são mais maduras)

---

## Dependências Maven

### POM.xml Completo

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.3</version>
        <relativePath/>
    </parent>

    <groupId>org</groupId>
    <artifactId>people</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>people</name>
    <description>People gRPC Service</description>

    <properties>
        <java.version>21</java.version>
        <grpc.version>1.59.0</grpc.version>
        <protobuf.version>3.24.4</protobuf.version>
        <reactor-grpc.version>1.2.4</reactor-grpc.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>

        <!-- gRPC Spring Boot Starter -->
        <!-- Facilita integração do gRPC com Spring Boot -->
        <dependency>
            <groupId>net.devh</groupId>
            <artifactId>grpc-server-spring-boot-starter</artifactId>
            <version>2.15.0.RELEASE</version>
        </dependency>

        <!-- gRPC Core -->
        <!-- Biblioteca principal do gRPC -->
        <dependency>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-netty-shaded</artifactId>
            <version>${grpc.version}</version>
        </dependency>

        <!-- gRPC Protocol Buffers -->
        <!-- Suporte para Protocol Buffers -->
        <dependency>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-protobuf</artifactId>
            <version>${grpc.version}</version>
        </dependency>

        <!-- gRPC Stub -->
        <!-- Classes geradas para client e server stubs -->
        <dependency>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-stub</artifactId>
            <version>${grpc.version}</version>
        </dependency>

        <!-- Reactor gRPC -->
        <!-- Integração do gRPC com Project Reactor -->
        <dependency>
            <groupId>com.salesforce.servicelibs</groupId>
            <artifactId>reactor-grpc-stub</artifactId>
            <version>${reactor-grpc.version}</version>
        </dependency>

        <!-- Protocol Buffers Java -->
        <!-- Runtime do Protocol Buffers -->
        <dependency>
            <groupId>com.google.protobuf</groupId>
            <artifactId>protobuf-java</artifactId>
            <version>${protobuf.version}</version>
        </dependency>

        <!-- Anotações javax -->
        <!-- Necessário para @Generated nas classes geradas -->
        <dependency>
            <groupId>javax.annotation</groupId>
            <artifactId>javax.annotation-api</artifactId>
            <version>1.3.2</version>
        </dependency>
    </dependencies>

    <build>
        <extensions>
            <!-- OS Maven Plugin -->
            <!-- Detecta o sistema operacional para compilação específica -->
            <extension>
                <groupId>kr.motd.maven</groupId>
                <artifactId>os-maven-plugin</artifactId>
                <version>1.7.1</version>
            </extension>
        </extensions>

        <plugins>
            <!-- Protobuf Maven Plugin -->
            <!-- Compila arquivos .proto e gera código Java -->
            <plugin>
                <groupId>org.xolstice.maven.plugins</groupId>
                <artifactId>protobuf-maven-plugin</artifactId>
                <version>0.6.1</version>
                <configuration>
                    <!-- Localização do compilador protoc -->
                    <protocArtifact>
                        com.google.protobuf:protoc:${protobuf.version}:exe:${os.detected.classifier}
                    </protocArtifact>

                    <!-- Plugin do gRPC -->
                    <pluginId>grpc-java</pluginId>
                    <pluginArtifact>
                        io.grpc:protoc-gen-grpc-java:${grpc.version}:exe:${os.detected.classifier}
                    </pluginArtifact>

                    <!-- Diretório dos arquivos .proto -->
                    <protoSourceRoot>${project.basedir}/src/main/proto</protoSourceRoot>

                    <!-- Diretório de saída do código gerado -->
                    <outputDirectory>${project.build.directory}/generated-sources/protobuf/java</outputDirectory>
                    <clearOutputDirectory>false</clearOutputDirectory>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <!-- Compila os arquivos .proto -->
                            <goal>compile</goal>
                            <!-- Gera código gRPC -->
                            <goal>compile-custom</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

### Descrição das Dependências Principais

| Dependência | Propósito | Versão |
|------------|-----------|--------|
| `grpc-server-spring-boot-starter` | Integração gRPC + Spring Boot | 2.15.0 |
| `grpc-netty-shaded` | Servidor gRPC com Netty (sem conflitos) | 1.59.0 |
| `grpc-protobuf` | Suporte Protocol Buffers | 1.59.0 |
| `grpc-stub` | Stubs gerados para client/server | 1.59.0 |
| `reactor-grpc-stub` | Integração com Project Reactor | 1.2.4 |
| `protobuf-java` | Runtime Protocol Buffers | 3.24.4 |

### Plugin Protobuf Maven

O plugin `protobuf-maven-plugin` é responsável por:

1. **Compilar arquivos `.proto`** → Gera classes Java
2. **Gerar código gRPC** → Cria stubs e service definitions
3. **Integração com build** → Executa automaticamente em `mvn compile`

**Comando de compilação:**
```bash
mvn clean compile
```

**Saída gerada em:**
```
target/generated-sources/protobuf/java/
└── com/people/grpc/
    ├── ServiceProto.java          # Mensagens Protocol Buffers
    └── PeopleServiceGrpc.java     # Service stubs e interface
```

---

## Protocol Buffers

### Arquivo person.proto

**Localização:** `src/main/proto/person.proto`

```protobuf
syntax = "proto3";

// Configurações Java
option java_package = "com.people.grpc";
option java_outer_classname = "ServiceProto";
option java_multiple_files = false;

// Namespace do pacote
package grpcservice;

// Definição do serviço gRPC
service PeopleService {
  // RPC para buscar pessoa por ID
  rpc GetPeople (PeopleRequestGrpc) returns (PeopleResponseGrpc);

  // RPC para listar todas as pessoas
  rpc ListPeople (ListPeopleRequestGrpc) returns (ListPeopleResponseGrpc);
}

// === Mensagens de Request ===

message PeopleRequestGrpc {
  int32 id = 1;  // ID da pessoa
}

message ListPeopleRequestGrpc {
  // Vazio - lista todos
}

// === Mensagens de Response ===

message PeopleResponseGrpc {
  int32 id = 1;       // ID da pessoa
  string name = 2;     // Nome completo
  string email = 3;    // Email
}

message ListPeopleResponseGrpc {
  repeated PeopleResponseGrpc people = 1;  // Lista de pessoas
}
```

### Tipos de Dados Protocol Buffers

| Proto Type | Java Type | Descrição |
|------------|-----------|-----------|
| `int32` | `int` | Inteiro 32 bits |
| `int64` | `long` | Inteiro 64 bits |
| `string` | `String` | String UTF-8 |
| `bool` | `boolean` | Booleano |
| `bytes` | `ByteString` | Bytes arbitrários |
| `repeated` | `List<T>` | Lista repetida |
| `map<K,V>` | `Map<K,V>` | Mapa chave-valor |

### Opções Java

```protobuf
// Pacote Java das classes geradas
option java_package = "com.people.grpc";

// Nome da classe outer que contém todas as mensagens
option java_outer_classname = "ServiceProto";

// Se true, cada mensagem é uma classe separada
option java_multiple_files = false;

// Otimização de geração de código
option optimize_for = SPEED;  // SPEED, CODE_SIZE, LITE_RUNTIME
```

### Exemplo de Classe Gerada

```java
// Gerado automaticamente em ServiceProto.java
public final class ServiceProto {

  // Mensagem PeopleRequestGrpc
  public static final class PeopleRequestGrpc
      extends com.google.protobuf.GeneratedMessageV3 {

    private int id_;

    public int getId() {
      return id_;
    }

    public static Builder newBuilder() {
      return new Builder();
    }

    public static final class Builder {
      public Builder setId(int value) {
        id_ = value;
        return this;
      }

      public PeopleRequestGrpc build() {
        return new PeopleRequestGrpc(this);
      }
    }
  }
}
```

---

## Estrutura de Arquivos

```
people/
├── src/main/
│   ├── proto/
│   │   └── person.proto                    # Definição Protocol Buffers
│   │
│   ├── java/org/people/
│   │   └── infrastructure/
│   │       ├── entrypoint/grpc/
│   │       │   └── PeopleGrpcServiceImpl.java    # Implementação do serviço
│   │       │
│   │       ├── exception/
│   │       │   └── GlobalGrpcExceptionHandler.java  # Handler de exceções
│   │       │
│   │       └── logging/
│   │           └── GrpcLoggingInterceptor.java      # Interceptor de logs
│   │
│   └── resources/
│       └── application.yml                 # Configuração gRPC
│
└── target/
    └── generated-sources/
        └── protobuf/java/
            └── com/people/grpc/
                ├── ServiceProto.java       # Mensagens geradas
                └── PeopleServiceGrpc.java  # Service stubs gerados
```

---

## Implementação do Servidor

### PeopleGrpcServiceImpl.java

**Localização:** `infrastructure/entrypoint/grpc/PeopleGrpcServiceImpl.java`

```java
package org.people.infrastructure.entrypoint.grpc;

import com.people.grpc.PeopleServiceGrpc;
import com.people.grpc.ServiceProto.*;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.people.application.dto.PeopleResponse;
import org.people.application.usecase.GetPeopleUseCaseImpl;
import org.people.application.usecase.ListPeopleUseCaseImpl;
import org.people.infrastructure.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementação do serviço gRPC PeopleService.
 *
 * Anotação @GrpcService registra automaticamente o serviço no servidor gRPC.
 */
@GrpcService
public class PeopleGrpcServiceImpl extends PeopleServiceGrpc.PeopleServiceImplBase {

    private static final Logger logger = Logger.getLogger(PeopleGrpcServiceImpl.class);

    @Autowired
    private GetPeopleUseCaseImpl getPeopleUseCase;

    @Autowired
    private ListPeopleUseCaseImpl listPeopleUseCase;

    /**
     * RPC GetPeople - Busca uma pessoa por ID
     *
     * @param request Contém o ID da pessoa
     * @param responseObserver Observer para enviar a resposta
     */
    @Override
    public void getPeople(PeopleRequestGrpc request,
                         StreamObserver<PeopleResponseGrpc> responseObserver) {

        logger.info("gRPC GetPeople called - id: {}", request.getId());

        // Chama o use case que retorna Mono<PeopleResponse>
        Mono<PeopleResponse> peopleMono = getPeopleUseCase.execute(request.getId());

        // Subscreve no Mono e processa a resposta
        peopleMono.subscribe(
            people -> {
                // Sucesso - converte para gRPC response
                PeopleResponseGrpc grpcResponse = PeopleResponseGrpc.newBuilder()
                        .setId(people.getId())
                        .setName(people.getName())
                        .setEmail(people.getEmail())
                        .build();

                // Envia a resposta
                responseObserver.onNext(grpcResponse);

                // Completa o stream
                responseObserver.onCompleted();

                logger.info("gRPC GetPeople completed successfully - id: {}", request.getId());
            },
            error -> {
                // Erro - propaga para o GlobalGrpcExceptionHandler
                logger.error("gRPC GetPeople error - id: {}", request.getId(), error);
                responseObserver.onError(error);
            }
        );
    }

    /**
     * RPC ListPeople - Lista todas as pessoas
     *
     * @param request Request vazio
     * @param responseObserver Observer para enviar a resposta
     */
    @Override
    public void listPeople(ListPeopleRequestGrpc request,
                          StreamObserver<ListPeopleResponseGrpc> responseObserver) {

        logger.info("gRPC ListPeople called");

        // Chama o use case que retorna Flux<PeopleResponse>
        Flux<PeopleResponse> peopleFlux = listPeopleUseCase.execute();

        // Coleta todos os elementos do Flux em uma lista
        Mono<List<PeopleResponse>> peopleListMono = peopleFlux.collectList();

        // Subscreve e processa
        peopleListMono.subscribe(
            peopleList -> {
                // Converte cada PeopleResponse para PeopleResponseGrpc
                List<PeopleResponseGrpc> grpcPeopleList = peopleList.stream()
                        .map(people -> PeopleResponseGrpc.newBuilder()
                                .setId(people.getId())
                                .setName(people.getName())
                                .setEmail(people.getEmail())
                                .build())
                        .collect(Collectors.toList());

                // Constrói a resposta com a lista
                ListPeopleResponseGrpc grpcResponse = ListPeopleResponseGrpc.newBuilder()
                        .addAllPeople(grpcPeopleList)
                        .build();

                // Envia a resposta
                responseObserver.onNext(grpcResponse);

                // Completa o stream
                responseObserver.onCompleted();

                logger.info("gRPC ListPeople completed successfully - count: {}",
                           grpcPeopleList.size());
            },
            error -> {
                // Erro
                logger.error("gRPC ListPeople error", error);
                responseObserver.onError(error);
            }
        );
    }
}
```

### Conceitos Importantes

#### 1. @GrpcService

```java
@GrpcService
public class PeopleGrpcServiceImpl extends PeopleServiceGrpc.PeopleServiceImplBase
```

- Anotação do `grpc-server-spring-boot-starter`
- Registra automaticamente o serviço no servidor gRPC
- Equivalente a `@Service` do Spring, mas específico para gRPC

#### 2. StreamObserver

```java
public void getPeople(PeopleRequestGrpc request,
                     StreamObserver<PeopleResponseGrpc> responseObserver)
```

- Interface para enviar respostas assíncronas
- **`onNext(T value)`** - Envia um valor
- **`onError(Throwable t)`** - Envia um erro
- **`onCompleted()`** - Marca stream como completo

#### 3. Integração com Reactor

```java
Mono<PeopleResponse> peopleMono = getPeopleUseCase.execute(request.getId());

peopleMono.subscribe(
    people -> {
        // Sucesso
        responseObserver.onNext(grpcResponse);
        responseObserver.onCompleted();
    },
    error -> {
        // Erro
        responseObserver.onError(error);
    }
);
```

- Usa `subscribe()` para conectar Reactor com gRPC
- Lambda de sucesso: converte e envia resposta
- Lambda de erro: propaga exceção

---

## Configuração Spring Boot

### application.yml

```yaml
spring:
  application:
    name: people

grpc:
  server:
    # Porta do servidor gRPC
    port: 9090

    # Endereço de bind (0.0.0.0 = todas as interfaces)
    address: 0.0.0.0

    # Tamanho máximo de mensagem (4MB)
    max-inbound-message-size: 4194304

    # Keep alive
    keep-alive-time: 30s
    keep-alive-timeout: 5s
    permit-keep-alive-time: 5m

    # Segurança (opcional)
    # security:
    #   enabled: true
    #   certificateChain: file:certificates/server.crt
    #   privateKey: file:certificates/server.key

logging:
  level:
    # Log do gRPC
    io.grpc: INFO

    # Log do grpc-spring-boot-starter
    net.devh: INFO

    # Log da aplicação
    org.people: DEBUG
```

### Propriedades Importantes

| Propriedade | Padrão | Descrição |
|------------|--------|-----------|
| `grpc.server.port` | 9090 | Porta do servidor gRPC |
| `grpc.server.address` | * | Endereço de bind |
| `grpc.server.max-inbound-message-size` | 4MB | Tamanho máximo da mensagem |
| `grpc.server.keep-alive-time` | - | Tempo de keep-alive |
| `grpc.server.security.enabled` | false | Habilita TLS/SSL |

---

## Logging e Interceptors

### GrpcLoggingInterceptor.java

**Localização:** `infrastructure/logging/GrpcLoggingInterceptor.java`

```java
package org.people.infrastructure.logging;

import io.grpc.*;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;

/**
 * Interceptor global para logging de requisições gRPC.
 *
 * Anotação @GrpcGlobalServerInterceptor aplica automaticamente
 * o interceptor a todos os serviços gRPC.
 */
@GrpcGlobalServerInterceptor
public class GrpcLoggingInterceptor implements ServerInterceptor {

    private static final Logger logger = Logger.getLogger(GrpcLoggingInterceptor.class);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        // Extrai informações da chamada
        String methodName = call.getMethodDescriptor().getFullMethodName();
        long startTime = System.currentTimeMillis();

        // Gera ou extrai Request ID
        String requestId = headers.get(Metadata.Key.of("x-request-id",
                                                       Metadata.ASCII_STRING_MARSHALLER));
        if (requestId == null) {
            requestId = java.util.UUID.randomUUID().toString();
        }

        // Armazena no contexto
        RequestContext.setRequestId(requestId);
        LogContext.add("method", methodName);
        LogContext.add("request_id", requestId);

        logger.info("gRPC Request started - method: {}, requestId: {}",
                   methodName, requestId);

        // Wrapper do ServerCall para interceptar a resposta
        ServerCall<ReqT, RespT> wrappedCall = new ForwardingServerCall.SimpleForwardingServerCall<>(call) {

            @Override
            public void close(Status status, Metadata trailers) {
                long duration = System.currentTimeMillis() - startTime;

                LogContext.setDuration(duration);
                LogContext.setStatusCode(status.getCode().name());

                if (status.isOk()) {
                    logger.info("gRPC Request completed - method: {}, duration: {}ms, status: OK",
                               methodName, duration);
                } else {
                    logger.error("gRPC Request failed - method: {}, duration: {}ms, status: {}, error: {}",
                                methodName, duration, status.getCode(), status.getDescription());
                }

                // Limpa o contexto
                LogContext.clear();
                RequestContext.clear();

                super.close(status, trailers);
            }
        };

        return next.startCall(wrappedCall, headers);
    }
}
```

### Como Funciona

1. **Interceptação**: Captura todas as chamadas gRPC antes de chegarem ao serviço
2. **Logging inicial**: Registra método, request ID e timestamp
3. **Wrapper**: Envolve o `ServerCall` para interceptar o fechamento
4. **Logging final**: Registra duração, status e possíveis erros
5. **Cleanup**: Limpa contextos de log e request

---

## Tratamento de Erros

### GlobalGrpcExceptionHandler.java

**Localização:** `infrastructure/exception/GlobalGrpcExceptionHandler.java`

```java
package org.people.infrastructure.exception;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import org.people.domain.exception.PeopleNotFoundException;
import org.people.domain.exception.ValidationException;
import org.people.domain.exception.BusinessRuleException;
import org.people.infrastructure.logging.Logger;

/**
 * Handler global de exceções para serviços gRPC.
 *
 * Converte exceções de domínio em status codes gRPC apropriados.
 */
@GrpcAdvice
public class GlobalGrpcExceptionHandler {

    private static final Logger logger = Logger.getLogger(GlobalGrpcExceptionHandler.class);

    /**
     * Trata exceção de recurso não encontrado.
     * Retorna status NOT_FOUND.
     */
    @GrpcExceptionHandler(PeopleNotFoundException.class)
    public StatusRuntimeException handleNotFound(PeopleNotFoundException ex) {
        logger.warn("People not found: {}", ex.getMessage());

        return Status.NOT_FOUND
                .withDescription(ex.getMessage())
                .withCause(ex)
                .asRuntimeException();
    }

    /**
     * Trata exceção de validação.
     * Retorna status INVALID_ARGUMENT.
     */
    @GrpcExceptionHandler(ValidationException.class)
    public StatusRuntimeException handleValidation(ValidationException ex) {
        logger.warn("Validation error: {}", ex.getMessage());

        return Status.INVALID_ARGUMENT
                .withDescription(ex.getMessage())
                .withCause(ex)
                .asRuntimeException();
    }

    /**
     * Trata exceção de regra de negócio.
     * Retorna status FAILED_PRECONDITION.
     */
    @GrpcExceptionHandler(BusinessRuleException.class)
    public StatusRuntimeException handleBusinessRule(BusinessRuleException ex) {
        logger.warn("Business rule violation: {}", ex.getMessage());

        return Status.FAILED_PRECONDITION
                .withDescription(ex.getMessage())
                .withCause(ex)
                .asRuntimeException();
    }

    /**
     * Trata exceção de serviço externo.
     * Retorna status UNAVAILABLE.
     */
    @GrpcExceptionHandler(ExternalServiceException.class)
    public StatusRuntimeException handleExternalService(ExternalServiceException ex) {
        logger.error("External service error: {}", ex.getMessage(), ex);

        return Status.UNAVAILABLE
                .withDescription("External service temporarily unavailable: " + ex.getMessage())
                .withCause(ex)
                .asRuntimeException();
    }

    /**
     * Trata exceções genéricas.
     * Retorna status INTERNAL.
     */
    @GrpcExceptionHandler(Exception.class)
    public StatusRuntimeException handleGeneric(Exception ex) {
        logger.error("Unexpected error", ex);

        return Status.INTERNAL
                .withDescription("Internal server error")
                .withCause(ex)
                .asRuntimeException();
    }
}
```

### Mapeamento de Status Codes

| Exception | gRPC Status | HTTP Equiv. | Descrição |
|-----------|-------------|-------------|-----------|
| `PeopleNotFoundException` | `NOT_FOUND` | 404 | Recurso não encontrado |
| `ValidationException` | `INVALID_ARGUMENT` | 400 | Validação falhou |
| `BusinessRuleException` | `FAILED_PRECONDITION` | 412 | Regra de negócio violada |
| `ExternalServiceException` | `UNAVAILABLE` | 503 | Serviço externo indisponível |
| `Exception` | `INTERNAL` | 500 | Erro interno não esperado |

---

## Testando o Serviço

### Com grpcurl

#### Instalar grpcurl

```bash
# Linux/Mac com Go
go install github.com/fullstorydev/grpcurl/cmd/grpcurl@latest

# Mac com Homebrew
brew install grpcurl

# Windows com Chocolatey
choco install grpcurl
```

#### Listar Serviços

```bash
grpcurl -plaintext localhost:9090 list
```

**Saída:**
```
grpcservice.PeopleService
```

#### Descrever Serviço

```bash
grpcurl -plaintext localhost:9090 describe grpcservice.PeopleService
```

**Saída:**
```
grpcservice.PeopleService is a service:
service PeopleService {
  rpc GetPeople ( .grpcservice.PeopleRequestGrpc ) returns ( .grpcservice.PeopleResponseGrpc );
  rpc ListPeople ( .grpcservice.ListPeopleRequestGrpc ) returns ( .grpcservice.ListPeopleResponseGrpc );
}
```

#### Chamar GetPeople

```bash
grpcurl -plaintext -d '{"id": 1}' localhost:9090 grpcservice.PeopleService/GetPeople
```

**Resposta:**
```json
{
  "id": 1,
  "name": "Emily Johnson",
  "email": "emily.johnson@x.dummyjson.com"
}
```

#### Chamar ListPeople

```bash
grpcurl -plaintext localhost:9090 grpcservice.PeopleService/ListPeople
```

**Resposta:**
```json
{
  "people": [
    {
      "id": 1,
      "name": "Emily Johnson",
      "email": "emily.johnson@x.dummyjson.com"
    },
    {
      "id": 2,
      "name": "Michael Williams",
      "email": "michael.williams@x.dummyjson.com"
    }
    ...
  ]
}
```

#### Testar Erro (ID inválido)

```bash
grpcurl -plaintext -d '{"id": 99999}' localhost:9090 grpcservice.PeopleService/GetPeople
```

**Resposta:**
```
ERROR:
  Code: NotFound
  Message: People not found - id: 99999
```

### Com Cliente Java

```java
// Criar channel
ManagedChannel channel = ManagedChannelBuilder
        .forAddress("localhost", 9090)
        .usePlaintext()
        .build();

// Criar stub
PeopleServiceGrpc.PeopleServiceBlockingStub stub =
        PeopleServiceGrpc.newBlockingStub(channel);

// Chamar GetPeople
PeopleRequestGrpc request = PeopleRequestGrpc.newBuilder()
        .setId(1)
        .build();

PeopleResponseGrpc response = stub.getPeople(request);

System.out.println("ID: " + response.getId());
System.out.println("Name: " + response.getName());
System.out.println("Email: " + response.getEmail());

// Fechar channel
channel.shutdown();
```

---

## Resumo de Pacotes Importantes

### Pacotes gRPC Core

```java
// Principais classes do gRPC
import io.grpc.Status;              // Status codes gRPC
import io.grpc.StatusRuntimeException;  // Exceção com status
import io.grpc.ServerInterceptor;    // Interface de interceptor
import io.grpc.Metadata;            // Headers/metadados
import io.grpc.stub.StreamObserver;  // Observer para respostas assíncronas
```

### Pacotes Spring Boot gRPC

```java
// Anotações do grpc-spring-boot-starter
import net.devh.boot.grpc.server.service.GrpcService;  // Marca serviço gRPC
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;  // Interceptor global
import net.devh.boot.grpc.server.advice.GrpcAdvice;   // Handler de exceções
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;  // Mapeia exceções
```

### Pacotes Gerados

```java
// Gerados automaticamente pelo protoc
import com.people.grpc.ServiceProto.*;  // Mensagens Protocol Buffers
import com.people.grpc.PeopleServiceGrpc;  // Service stubs
```

---

## Boas Práticas

### 1. Versionamento de API

```protobuf
// Adicione versão no pacote
package grpcservice.v1;

// Ou no nome do serviço
service PeopleServiceV1 {
  // ...
}
```

### 2. Campos Opcionais

```protobuf
// Use wrapper types para campos opcionais
import "google/protobuf/wrappers.proto";

message PeopleRequestGrpc {
  int32 id = 1;
  google.protobuf.StringValue filter = 2;  // Opcional
}
```

### 3. Paginação

```protobuf
message ListPeopleRequestGrpc {
  int32 page_size = 1;
  string page_token = 2;
}

message ListPeopleResponseGrpc {
  repeated PeopleResponseGrpc people = 1;
  string next_page_token = 2;
}
```

### 4. Timeouts

```java
// Cliente com timeout
PeopleServiceGrpc.PeopleServiceBlockingStub stub =
    PeopleServiceGrpc.newBlockingStub(channel)
        .withDeadlineAfter(5, TimeUnit.SECONDS);
```

### 5. Health Checks

```yaml
# application.yml
grpc:
  server:
    health:
      enabled: true
```

```bash
# Testar health
grpcurl -plaintext localhost:9090 grpc.health.v1.Health/Check
```

---

**Criado por:** People Service Team
**Última atualização:** Dezembro 2024
**Versão do gRPC:** 1.59.0
