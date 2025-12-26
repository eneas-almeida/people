# Estrutura de Logger - People Microservice

Esta estrutura de logging foi desenvolvida para integração com Datadog e fornece logs estruturados em formato JSON.

## Componentes

### 1. Logger
Classe centralizada para logging que automaticamente adiciona contexto do Datadog (trace_id, span_id).

### 2. LogContext
Gerenciador de contexto para adicionar informações customizadas aos logs usando MDC (Mapped Diagnostic Context).

### 3. RequestContext
Gerenciador de UUID da request que propaga o Request ID através de toda a aplicação e chamadas externas.

### 4. GrpcLoggingInterceptor
Interceptor que adiciona logs automáticos para todas as chamadas gRPC e gerencia o UUID da request.

## Como Usar

### Uso Básico

```java
import org.people.infrastructure.logging.Logger;

public class MyService {
    private static final Logger logger = Logger.getLogger(MyService.class);

    public void myMethod() {
        logger.info("Mensagem simples");
        logger.debug("Debug com parâmetros: {}, {}", param1, param2);
        logger.error("Erro ocorreu", exception);
    }
}
```

### Uso com Contexto Customizado

```java
import org.people.infrastructure.logging.Logger;
import org.people.infrastructure.logging.LogContext;
import java.util.Map;

public class MyService {
    private static final Logger logger = Logger.getLogger(MyService.class);

    public void processUser(String userId) {
        // Adicionar contexto individualmente
        LogContext.setUserId(userId);
        LogContext.setMethod("processUser");

        logger.info("Processando usuário");

        // Ou adicionar múltiplos campos de uma vez
        Map<String, String> context = Map.of(
            "user_id", userId,
            "operation", "create",
            "source", "api"
        );
        logger.info("Operação realizada", context);

        // Limpar contexto quando necessário
        LogContext.clear();
    }
}
```

### Campos de Contexto Predefinidos

```java
LogContext.setUserId("user123");
LogContext.setRequestId("req-456");
LogContext.setCorrelationId("corr-789");
LogContext.setMethod("getUserById");
LogContext.setEndpoint("/api/users");
LogContext.setDuration(125L); // em milissegundos
LogContext.setStatusCode("OK");
LogContext.setError("NOT_FOUND");
LogContext.setErrorMessage("User not found");
```

### Campos Personalizados

```java
LogContext.add("custom_field", "custom_value");

Map<String, String> customContext = Map.of(
    "tenant_id", "tenant123",
    "region", "us-east-1",
    "version", "v2"
);
LogContext.addAll(customContext);
```

### Uso do RequestContext (UUID da Request)

O `RequestContext` gerencia automaticamente um UUID único para cada request que é propagado através de toda a aplicação.

```java
import org.people.infrastructure.logging.RequestContext;
import org.people.infrastructure.logging.Logger;

public class MyUseCase {
    private static final Logger logger = Logger.getLogger(MyUseCase.class);

    public void execute(Integer id) {
        // Obter o UUID da request atual (gerado automaticamente pelo interceptor)
        String requestId = RequestContext.getRequestId();

        logger.info("Processing request - id: {}, requestId: {}", id, requestId);

        // O requestId será automaticamente incluído em todos os logs
        // e propagado para chamadas HTTP externas via headers
    }
}
```

**Características do RequestContext:**
- UUID gerado automaticamente pelo `GrpcLoggingInterceptor` para cada chamada gRPC
- Se a chamada gRPC incluir header `X-Request-ID`, esse valor será usado (permite propagação entre serviços)
- RequestID é automaticamente adicionado aos headers `X-Request-ID` em todas as chamadas HTTP externas
- Suporte a `X-Correlation-ID` para rastreamento distribuído
- ThreadLocal para isolamento entre requests concorrentes

**Propagação Automática:**
- Logs: Todos os logs incluem automaticamente o `request_id`
- gRPC: Headers `x-request-id` e `x-correlation-id` são extraídos e propagados
- HTTP: Headers `X-Request-ID` e `X-Correlation-ID` são adicionados automaticamente (veja `TypiCodeClientConfig`)

**Exemplo de Rastreamento Distribuído:**
```
Cliente → [X-Request-ID: abc-123] → People Service → [X-Request-ID: abc-123] → API Externa
```

## Integração com Datadog

### Formato JSON
Todos os logs são emitidos em formato JSON estruturado, incluindo:
- timestamp
- level (INFO, DEBUG, WARN, ERROR)
- message
- logger (nome da classe)
- thread
- service (nome do microserviço)
- dd.trace_id (Datadog trace ID)
- dd.span_id (Datadog span ID)
- Todos os campos adicionados via LogContext

### Exemplo de Log JSON

```json
{
  "timestamp": "2025-12-16T10:30:45.123Z",
  "level": "INFO",
  "message": "Processando usuário",
  "logger": "org.people.usecase.GetPeopleUseCase",
  "thread": "grpc-default-executor-1",
  "service": "people",
  "env": "production",
  "dd.trace_id": "1234567890",
  "dd.span_id": "9876543210",
  "user_id": "user123",
  "request_id": "req-456",
  "method": "getPeople",
  "duration_ms": "125"
}
```

### Executar com Datadog Agent

Para integração completa com Datadog, execute a aplicação com o Java Agent:

```bash
java -javaagent:/path/to/dd-java-agent.jar \
     -Ddd.service=people \
     -Ddd.env=production \
     -Ddd.version=1.0.0 \
     -jar people-0.0.1-SNAPSHOT.jar
```

### Variáveis de Ambiente Datadog

```bash
DD_SERVICE=people
DD_ENV=production
DD_VERSION=1.0.0
DD_AGENT_HOST=localhost
DD_TRACE_AGENT_PORT=8126
DD_LOGS_INJECTION=true
ENVIRONMENT=production
```

## Profiles

A configuração de logging suporta diferentes profiles:

- **local/dev**: Logs síncronos no console
- **staging/prod**: Logs assíncronos (melhor performance)

Configurar via:
```bash
SPRING_PROFILE=prod
```

## Níveis de Log

Configurados em `application.yml`:
- Root: INFO
- org.people: DEBUG
- io.grpc: INFO
- net.devh: INFO

## Interceptor gRPC

O `GrpcLoggingInterceptor` automaticamente adiciona logs para:
- Início da requisição
- Conclusão (sucesso ou falha)
- Cancelamento
- Duração da chamada
- Status code

Não é necessário configurar manualmente, o interceptor é registrado automaticamente via `@GrpcGlobalServerInterceptor`.

## Boas Práticas

1. **Sempre limpar o contexto**: Use `LogContext.clear()` após operações que adicionam contexto
2. **Usar níveis apropriados**:
   - DEBUG: Informações detalhadas para debug
   - INFO: Eventos importantes do sistema
   - WARN: Situações inesperadas mas recuperáveis
   - ERROR: Erros que impedem operações
3. **Incluir contexto relevante**: Adicione IDs, operações, e metadados que ajudem na investigação
4. **Não logar informações sensíveis**: Evite logar senhas, tokens, PII sem necessidade
5. **Usar parâmetros em vez de concatenação**: Use `logger.info("User {}", userId)` em vez de `logger.info("User " + userId)`

<hr />

<div>
  <sub>Conteúdo criado por <a href="https://github.com/eneas-almeida">Enéas Almeida</a></sub>
</div>