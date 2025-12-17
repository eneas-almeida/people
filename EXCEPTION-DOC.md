# Estrutura de Exceptions - People Microservice

Estrutura completa e organizada de tratamento de exceptions para o microserviço People, com integração automática ao gRPC.

## Arquitetura

```
PeopleException (abstract)
├── Domain Exceptions (org.people.domain.exception)
│   ├── PeopleNotFoundException
│   ├── BusinessRuleException
│   └── ValidationException
│
└── Infrastructure Exceptions (org.people.infrastructure.exception)
    ├── ExternalServiceException
    └── InternalServerException
```

## Componentes

### 1. **PeopleException** (Base)
Exception abstrata base para todas as exceptions do domínio.

**Características:**
- Código de erro customizado
- Detalhes adicionais opcionais
- Status code abstrato (implementado por subclasses)
- Suporte a causa (Throwable)

### 2. **Exceptions de Domínio**

#### **PeopleNotFoundException**
- **Quando usar**: Recurso não encontrado (pessoa não existe)
- **Status gRPC**: `NOT_FOUND`
- **Código**: `PEOPLE_NOT_FOUND`

```java
// Exemplos de uso
throw new PeopleNotFoundException("Person not found");
throw new PeopleNotFoundException(123); // Por ID
throw new PeopleNotFoundException("Person not found", "Additional details");
```

#### **BusinessRuleException**
- **Quando usar**: Violação de regra de negócio
- **Status gRPC**: `FAILED_PRECONDITION`
- **Código**: `BUSINESS_RULE_VIOLATION`

```java
// Exemplos de uso
throw new BusinessRuleException("Cannot delete person with active contracts");
throw new BusinessRuleException("Age must be 18+", "MIN_AGE_VIOLATION");
throw new BusinessRuleException("Invalid operation", "CUSTOM_CODE", "Details here");
```

#### **ValidationException**
- **Quando usar**: Validação de entrada/dados
- **Status gRPC**: `INVALID_ARGUMENT`
- **Código**: `VALIDATION_ERROR`
- **Extra**: Suporta mapa de erros por campo

```java
// Exemplo simples
throw new ValidationException("Invalid input");

// Com campo específico
throw new ValidationException("Name is required", "name", "cannot be empty");

// Com múltiplos campos
Map<String, String> errors = Map.of(
    "name", "cannot be empty",
    "email", "invalid format",
    "age", "must be positive"
);
throw new ValidationException("Validation failed", errors);
```

### 3. **Exceptions de Infraestrutura**

#### **ExternalServiceException**
- **Quando usar**: Erro ao chamar serviço externo (API, HTTP)
- **Status gRPC**: `UNAVAILABLE`
- **Código**: `EXTERNAL_SERVICE_ERROR`
- **Extra**: Nome do serviço e status HTTP

```java
// Exemplos de uso
throw new ExternalServiceException("API timeout", "TypiCode API");
throw new ExternalServiceException("Service unavailable", "TypiCode API", 503);
throw new ExternalServiceException("Connection failed", "TypiCode API", cause);
```

#### **InternalServerException**
- **Quando usar**: Erro interno do servidor
- **Status gRPC**: `INTERNAL`
- **Código**: `INTERNAL_SERVER_ERROR`

```java
// Exemplos de uso
throw new InternalServerException("Database connection failed");
throw new InternalServerException("Unexpected error", cause);
throw new InternalServerException("Custom error", "CUSTOM_CODE", cause);
```

## GlobalGrpcExceptionHandler

Handler global que intercepta todas as exceptions e as converte automaticamente em Status gRPC apropriados.

### Como Funciona

1. Exception é lançada em qualquer parte do código
2. `@GrpcAdvice` intercepta a exception
3. Método `@GrpcExceptionHandler` específico trata a exception
4. Status gRPC é retornado ao cliente
5. Log apropriado é registrado

### Mapeamento Exception → gRPC Status

| Exception | gRPC Status | Código HTTP Equiv. |
|-----------|-------------|-------------------|
| PeopleNotFoundException | NOT_FOUND | 404 |
| ValidationException | INVALID_ARGUMENT | 400 |
| BusinessRuleException | FAILED_PRECONDITION | 412 |
| ExternalServiceException | UNAVAILABLE | 503 |
| InternalServerException | INTERNAL | 500 |
| Exception (genérica) | INTERNAL | 500 |

## Uso Prático

### Em Use Cases

```java
public class GetPeopleUseCase {
    private static final Logger logger = Logger.getLogger(GetPeopleUseCase.class);
    private final PeopleRepository repository;

    public People execute(Integer id) {
        // Validação
        if (id == null || id <= 0) {
            throw new ValidationException("Invalid ID", "id", "must be positive");
        }

        // Busca
        People people = repository.findById(id)
            .orElseThrow(() -> new PeopleNotFoundException(id));

        // Regra de negócio
        if (people.isInactive()) {
            throw new BusinessRuleException("Cannot access inactive person");
        }

        return people;
    }
}
```

### Em Adapters (Infraestrutura)

```java
public class TypiCodePeopleClientImpl {
    private Mono<Throwable> handleServerError(Integer id, ClientResponse response) {
        return response.bodyToMono(String.class)
            .defaultIfEmpty("No body")
            .flatMap(body -> {
                String errorMsg = String.format("Server error - id: %d, status: %s",
                        id, response.statusCode());
                logger.error(errorMsg);
                return Mono.error(new ExternalServiceException(
                    errorMsg, "TypiCode API", response.statusCode().value()
                ));
            });
    }
}
```

### Em Controllers/Services gRPC

```java
@GrpcService
public class PeopleGrpcService {
    private final GetPeopleUseCase getPeopleUseCase;

    @Override
    public void getPerson(GetPersonRequest request, StreamObserver<PersonResponse> responseObserver) {
        try {
            // O handler vai interceptar qualquer exception automaticamente
            People people = getPeopleUseCase.execute(request.getId());

            PersonResponse response = mapToResponse(people);
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            // Opcional: você pode deixar o GlobalGrpcExceptionHandler tratar
            // ou tratar localmente se precisar
            responseObserver.onError(e);
        }
    }
}
```

## Logging Integrado

Todas as exceptions são automaticamente logadas pelo `GlobalGrpcExceptionHandler`:

**PeopleNotFoundException**:
```json
{
  "level": "WARN",
  "message": "People not found - code: PEOPLE_NOT_FOUND, message: Person with id 123 not found"
}
```

**ValidationException**:
```json
{
  "level": "WARN",
  "message": "Validation error - code: VALIDATION_ERROR, errors: {name: cannot be empty, email: invalid format}",
  "validation_errors": "name: cannot be empty, email: invalid format"
}
```

**ExternalServiceException**:
```json
{
  "level": "ERROR",
  "message": "External service error - service: TypiCode API, code: EXTERNAL_SERVICE_ERROR, httpStatus: 503",
  "external_service": "TypiCode API",
  "http_status_code": "503"
}
```

**InternalServerException**:
```json
{
  "level": "ERROR",
  "message": "Internal server error - code: INTERNAL_SERVER_ERROR",
  "error": "InternalServerException",
  "error_message": "Database connection failed"
}
```

## Resposta ao Cliente gRPC

Quando uma exception é lançada, o cliente gRPC recebe:

```protobuf
status {
  code: 5  // NOT_FOUND, INVALID_ARGUMENT, etc.
  message: "Person with id 123 not found"
  details: [] // Pode conter metadados adicionais
}
```

## Boas Práticas

1. **Use a exception mais específica possível**
   - ✅ `throw new PeopleNotFoundException(id)`
   - ❌ `throw new RuntimeException("Not found")`

2. **Sempre inclua mensagem descritiva**
   - ✅ `throw new ValidationException("Email is required", "email", "cannot be empty")`
   - ❌ `throw new ValidationException("Error")`

3. **Preserve a causa original**
   ```java
   try {
       externalApi.call();
   } catch (IOException e) {
       throw new ExternalServiceException("API call failed", "External API", e);
   }
   ```

4. **Não capture exceptions do domínio sem necessidade**
   ```java
   // ❌ Ruim
   try {
       useCase.execute(id);
   } catch (PeopleNotFoundException e) {
       throw new InternalServerException(e.getMessage());
   }

   // ✅ Bom - deixe propagar
   useCase.execute(id);
   ```

5. **Use códigos customizados quando apropriado**
   ```java
   throw new BusinessRuleException(
       "Cannot delete person with active contracts",
       "HAS_ACTIVE_CONTRACTS",
       "Person has 3 active contracts"
   );
   ```

## Integração com Datadog

Todas as exceptions são logadas com contexto completo e aparecem no Datadog com:
- Request ID
- Trace ID / Span ID
- Código do erro
- Stack trace
- Metadados adicionais (service name, HTTP status, etc.)

Isso permite rastrear e correlacionar erros facilmente no Datadog APM.