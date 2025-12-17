# Configuração do Datadog para People Microservice

Este documento descreve como configurar o Datadog para monitoramento completo do microserviço People.

## Pré-requisitos

1. Conta no Datadog
2. Datadog Agent instalado e rodando
3. API Key do Datadog

## Instalação do Datadog Agent

### Docker (Recomendado para desenvolvimento)

```bash
docker run -d \
  --name datadog-agent \
  -e DD_API_KEY=<YOUR_DATADOG_API_KEY> \
  -e DD_SITE="datadoghq.com" \
  -e DD_LOGS_ENABLED=true \
  -e DD_LOGS_CONFIG_CONTAINER_COLLECT_ALL=true \
  -e DD_APM_ENABLED=true \
  -e DD_APM_NON_LOCAL_TRAFFIC=true \
  -v /var/run/docker.sock:/var/run/docker.sock:ro \
  -v /proc/:/host/proc/:ro \
  -v /sys/fs/cgroup/:/host/sys/fs/cgroup:ro \
  -v /opt/datadog-agent/run:/opt/datadog-agent/run:rw \
  -p 8126:8126/tcp \
  -p 8125:8125/udp \
  gcr.io/datadoghq/agent:latest
```

### Linux/MacOS

```bash
DD_API_KEY=<YOUR_DATADOG_API_KEY> \
DD_SITE="datadoghq.com" \
bash -c "$(curl -L https://s3.amazonaws.com/dd-agent/scripts/install_script_agent7.sh)"
```

### Windows

```powershell
$env:DD_API_KEY="<YOUR_DATADOG_API_KEY>"
$env:DD_SITE="datadoghq.com"
. { iwr -useb https://s3.amazonaws.com/dd-agent/scripts/install_script_agent7.ps1 } | iex; install
```

## Download do Java Tracer

```bash
# Baixar o Datadog Java Agent
wget -O dd-java-agent.jar 'https://dtdg.co/latest-java-tracer'

# Ou usando curl
curl -Lo dd-java-agent.jar 'https://dtdg.co/latest-java-tracer'
```

## Configuração da Aplicação

### 1. Variáveis de Ambiente

Crie um arquivo `.env` na raiz do projeto:

```bash
# Datadog Configuration
DD_SERVICE=people
DD_ENV=production
DD_VERSION=1.0.0
DD_AGENT_HOST=localhost
DD_TRACE_AGENT_PORT=8126
DD_LOGS_INJECTION=true
DD_TRACE_SAMPLE_RATE=1.0
DD_PROFILING_ENABLED=true

# Application Environment
ENVIRONMENT=production
SPRING_PROFILE=prod
```

### 2. Executar a Aplicação com Datadog

#### Usando Java diretamente

```bash
java -javaagent:dd-java-agent.jar \
     -Ddd.service=people \
     -Ddd.env=production \
     -Ddd.version=1.0.0 \
     -Ddd.logs.injection=true \
     -Ddd.trace.sample.rate=1.0 \
     -Ddd.profiling.enabled=true \
     -jar target/people-0.0.1-SNAPSHOT.jar
```

#### Usando Maven

Adicione ao `pom.xml`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <jvmArguments>
                    -javaagent:dd-java-agent.jar
                    -Ddd.service=people
                    -Ddd.env=${DD_ENV}
                    -Ddd.version=${DD_VERSION}
                    -Ddd.logs.injection=true
                </jvmArguments>
            </configuration>
        </plugin>
    </plugins>
</build>
```

Depois execute:

```bash
mvn spring-boot:run
```

#### Docker Compose

```yaml
version: '3.8'

services:
  datadog-agent:
    image: gcr.io/datadoghq/agent:latest
    container_name: datadog-agent
    environment:
      - DD_API_KEY=${DD_API_KEY}
      - DD_SITE=datadoghq.com
      - DD_LOGS_ENABLED=true
      - DD_APM_ENABLED=true
      - DD_APM_NON_LOCAL_TRAFFIC=true
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock:ro
      - /proc/:/host/proc/:ro
      - /sys/fs/cgroup/:/host/sys/fs/cgroup:ro
    ports:
      - "8126:8126"
      - "8125:8125/udp"

  people-service:
    build: .
    container_name: people-service
    environment:
      - DD_SERVICE=people
      - DD_ENV=production
      - DD_VERSION=1.0.0
      - DD_AGENT_HOST=datadog-agent
      - DD_LOGS_INJECTION=true
      - ENVIRONMENT=production
      - SPRING_PROFILE=prod
    volumes:
      - ./dd-java-agent.jar:/dd-java-agent.jar
    command: >
      java -javaagent:/dd-java-agent.jar
      -jar /app/people-0.0.1-SNAPSHOT.jar
    depends_on:
      - datadog-agent
    ports:
      - "9090:9090"
```

### 3. Dockerfile com Datadog

```dockerfile
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiar o Datadog Java Agent
COPY dd-java-agent.jar /dd-java-agent.jar

# Copiar o JAR da aplicação
COPY target/people-0.0.1-SNAPSHOT.jar /app/people.jar

# Expor porta gRPC
EXPOSE 9090

# Executar com Datadog Agent
ENTRYPOINT ["java", "-javaagent:/dd-java-agent.jar", "-jar", "/app/people.jar"]
```

## Configuração de Logs no Datadog

### 1. Configurar Pipeline de Logs

No Datadog, vá para:
1. Logs → Configuration → Pipelines
2. Crie um novo pipeline para o serviço "people"
3. Adicione os seguintes processors:

#### Grok Parser

```
rule %{data:timestamp} %{notSpace:level} %{data:message}
```

#### Date Remapper
- Source: `timestamp`

#### Status Remapper
- Source: `level`

#### Service Remapper
- Source: `service`

### 2. Criar Facets Customizadas

Crie facets para os seguintes campos:
- `dd.trace_id`
- `dd.span_id`
- `request_id`
- `user_id`
- `method`
- `duration_ms`
- `status_code`
- `error`

## Dashboards e Alertas

### Dashboard de Métricas

Crie um dashboard com:
- Taxa de requisições por minuto
- Latência p50, p95, p99
- Taxa de erros
- Throughput do gRPC
- Uso de CPU e memória

### Alertas Recomendados

1. **Taxa de Erro Alta**
   - Condição: Error rate > 5% por 5 minutos
   - Notificar: Equipe de desenvolvimento

2. **Latência Alta**
   - Condição: p95 latency > 1000ms por 10 minutos
   - Notificar: Equipe de SRE

3. **Serviço Down**
   - Condição: Nenhuma métrica recebida por 5 minutos
   - Notificar: On-call engineer

## APM (Application Performance Monitoring)

### Traces

O Datadog automaticamente irá coletar:
- Traces de chamadas gRPC
- Traces de chamadas HTTP (WebClient)
- Correlação entre logs e traces via `dd.trace_id`

### Service Map

O Service Map mostrará:
- People service
- Dependências externas (typicode API)
- Latência entre serviços
- Taxa de erros

## Profiling

Com profiling habilitado, você terá acesso a:
- CPU profiling
- Heap profiling
- Análise de threads
- Hotspots de código

## Verificação

Após configurar, verifique:

1. **Logs estão chegando**
   ```
   Logs → Live Tail → Filtrar por service:people
   ```

2. **Traces estão sendo coletados**
   ```
   APM → Services → people
   ```

3. **Métricas estão disponíveis**
   ```
   Metrics → Explorer → Filtrar por service:people
   ```

## Troubleshooting

### Logs não aparecem no Datadog

1. Verificar se o Agent está rodando:
   ```bash
   docker ps | grep datadog
   # ou
   sudo systemctl status datadog-agent
   ```

2. Verificar logs do Agent:
   ```bash
   docker logs datadog-agent
   # ou
   sudo journalctl -u datadog-agent
   ```

3. Verificar conectividade:
   ```bash
   curl -v http://localhost:8126/info
   ```

### Traces não aparecem

1. Verificar se o Java Agent está sendo carregado:
   - Procure por mensagens de inicialização do Datadog nos logs da aplicação
   - Deve ver: `Datadog Tracer vX.X.X`

2. Verificar variáveis de ambiente:
   ```bash
   echo $DD_SERVICE
   echo $DD_ENV
   echo $DD_AGENT_HOST
   ```

3. Verificar porta do Agent:
   ```bash
   netstat -an | grep 8126
   ```

## Custos

Para otimizar custos do Datadog:

1. **Ajustar taxa de amostragem**
   ```
   DD_TRACE_SAMPLE_RATE=0.5  # 50% das traces
   ```

2. **Filtrar logs desnecessários** no logback-spring.xml

3. **Usar retention policies** no Datadog

4. **Desabilitar profiling em ambientes não-produtivos**
   ```
   DD_PROFILING_ENABLED=false
   ```

## Referências

- [Datadog Java Tracer](https://docs.datadoghq.com/tracing/setup_overview/setup/java/)
- [Datadog Logging](https://docs.datadoghq.com/logs/log_collection/java/)
- [Datadog APM](https://docs.datadoghq.com/tracing/)
- [Datadog Profiling](https://docs.datadoghq.com/profiler/)