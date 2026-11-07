# Projeto de Qualidade Educacional

Este é um projeto educacional em Java usando Spring Boot e Maven para ensinar conceitos de qualidade aos alunos. O projeto implementa um sistema simples de cadastro de alunos e professores com uma interface web básica.

## Funcionalidades

- Cadastro e gerenciamento de alunos
- Cadastro e gerenciamento de professores
- Interface web simples usando Thymeleaf e Bootstrap
- Banco de dados MongoDB para armazenamento de documentos

## Tecnologias Utilizadas

- Java 17
- Spring Boot 3.2.0
- Maven
- Spring Data MongoDB
- Thymeleaf
- Bootstrap
- Banco de Dados MongoDB

## Conexão ao Banco de Dados

Este projeto utiliza um banco de dados MongoDB hospedado remotamente para fins educacionais:
```
mongodb://mongo:XdnCdkmKwzJiKHiryTDzdgRGOxzNQYtA@crossover.proxy.rlwy.net:24258
```

## Começando

### Pré-requisitos

- Java 17 ou superior
- Maven 3.6.0 ou superior

### Instalação do Maven

#### No Windows:
1. Baixe o Maven do site oficial: https://maven.apache.org/download.cgi
2. Extraia o arquivo ZIP para um diretório como `C:\apache-maven-3.9.5`
3. Adicione a variável de ambiente `MAVEN_HOME` apontando para o diretório do Maven
4. Adicione `%MAVEN_HOME%\bin` ao seu PATH
5. Verifique a instalação com o comando:
```cmd
mvn -version
```

#### No Linux (Ubuntu/Debian):
```bash
sudo apt update
sudo apt install maven
```

#### No Linux (CentOS/RHEL/Fedora):
```bash
sudo yum install maven
# ou para Fedora:
sudo dnf install maven
```

### Executando a Aplicação

1. Clone o repositório
2. Navegue até o diretório do projeto
3. Execute a aplicação usando Maven:

```bash
./mvnw spring-boot:run
```

Ou construa e execute o JAR:

```bash
./mvnw clean package
java -jar target/educational-quality-project-0.0.1-SNAPSHOT.jar
```

### Comandos úteis do Maven

- Para compilar o projeto: `mvn compile`
- Para executar os testes: `mvn test`
- Para empacotar o projeto: `mvn package`
- Para limpar os arquivos gerados: `mvn clean`
- Para executar a aplicação: `mvn spring-boot:run`

### Acessando a Aplicação

Antes de iniciar, configure uma senha local para cada perfil. Os nomes de usuário padrão são `admin`, `professor` e `gestora`:

```bash
export APP_ADMIN_PASSWORD='admin2'
export APP_PROFESSOR_PASSWORD='prof2'
export APP_MANAGER_PASSWORD='mama2'
./mvnw spring-boot:run
```

Opcionalmente, os nomes de usuário podem ser definidos com `APP_ADMIN_USERNAME`, `APP_PROFESSOR_USERNAME` e `APP_MANAGER_USERNAME`. As senhas não têm valor padrão e não devem ser salvas no repositório. Este login local de demonstração não substitui um provedor de identidade para produção.

Depois de iniciar a aplicação, acesse http://localhost:8080/login. O perfil é escolhido pelas credenciais usadas:
- `admin`: painel com cadastro de estudantes e professores
- `professor`: painel baseado na persona 02
- `gestora`: painel de espaços e materiais da persona 03

As telas de estudantes e recursos precisam de uma conexão MongoDB disponível, configurada por `SPRING_DATA_MONGODB_URI` ou em `application.properties`.

Rotas diretas das áreas:
- Página de alunos: http://localhost:8080/students
- Página de professores: http://localhost:8080/teachers

### API Endpoints

A aplicação também disponibiliza endpoints RESTful para integração com outras aplicações:

#### Estudantes
- `GET /api/students` - Listar todos os estudantes
- `GET /api/students/{id}` - Obter detalhes de um estudante específico
- `POST /api/students` - Criar um novo estudante
- `PUT /api/students/{id}` - Atualizar informações de um estudante
- `DELETE /api/students/{id}` - Excluir um estudante

#### Professores
- `GET /api/teachers` - Listar todos os professores
- `GET /api/teachers/{id}` - Obter detalhes de um professor específico
- `POST /api/teachers` - Criar um novo professor
- `PUT /api/teachers/{id}` - Atualizar informações de um professor
- `DELETE /api/teachers/{id}` - Excluir um professor

#### Planejamento acadêmico
Os recursos abaixo oferecem operações `GET` (listar e consultar por ID), `POST` (criar), `PUT` (editar) e `DELETE` (excluir):
- `/api/academic-periods` - Períodos letivos com datas de início e fim
- `/api/courses` - Cursos
- `/api/classes` - Turmas vinculadas a um curso e período letivo
- `/api/subjects` - Disciplinas vinculadas a um curso
- `/api/schedules` - Grades vinculadas a um período, com atividades e professores

Uma atividade de grade deve referenciar curso, turma, disciplina e ao menos um professor existente. As datas da atividade precisam estar dentro do período letivo e o fim deve ser posterior ao início. Ao criar ou editar uma grade, o sistema registra uma versão com cópia das atividades.

#### Reservas de recursos
O módulo de reservas usa PostgreSQL e é ativado somente quando `reservations.datasource.url` estiver configurada. O Flyway aplica as migrations em `src/main/resources/db/migration`; configure também `reservations.datasource.username` e `reservations.datasource.password` conforme o ambiente. Sem essa configuração, a API de reservas fica desativada e as demais telas/rotas continuam usando a configuração MongoDB existente.

- `POST /api/reservations` - Criar solicitação. Corpo: `resourceId`, `resourceType` (`ROOM`, `PROFESSOR` ou `MATERIAL`), `requesterId`, `startsAt`, `endsAt`, `quantity` (opcional, padrão 1) e `academicPeriodId` (obrigatório para professor). Use instantes ISO-8601 com offset, por exemplo `2026-03-01T10:00:00Z`.
- `GET /api/reservations?resourceId=...&status=...&startsBefore=...&endsAfter=...` - Consultar reservas com filtros opcionais.
- `GET /api/reservations/{id}` - Consultar uma reserva.
- `POST /api/reservations/{id}/approve?actorId=...&role=RESPONSAVEL` - Aprovar; recurso restrito não pode ser aprovado pelo próprio solicitante.
- `POST /api/reservations/{id}/start?actorId=...` e `POST /api/reservations/{id}/complete?actorId=...` - Avançar `APROVADA → EM_USO → CONCLUIDA`.
- `GET /api/reservations/{id}/history` - Consultar eventos da reserva, incluindo solicitações e transições rejeitadas.

Salas e professores usam exclusão GiST no PostgreSQL; materiais usam lock transacional por recurso e cálculo do pico de quantidade em intervalos semiabertos, retornando `409` para conflito ou excesso. O fluxo valida também estado, bloqueios, buffers e agenda atual no MongoDB. Isso não cria transação distribuída com as gravações de grade/alocação Mongo: reservas SQL e alocações acadêmicas ainda podem divergir sob gravações concorrentes entre os dois fluxos.

Os parâmetros `requesterId`, `actorId` e `role` ainda são informados pelo cliente porque o projeto não possui autenticação/autorização; portanto, o módulo não deve ser exposto como seguro em rede compartilhada. Rejeição/cancelamento/não comparecimento e notificações não foram implementados porque a matriz de transição e o canal ainda estão em aberto nos registros de requisitos.

#### Versões, aprovação e publicação de grades
- `GET /api/schedules/{id}/versions` - Consultar as versões e suas atividades.
- `GET /api/schedules/{id}/versions/compare?fromVersion=1&toVersion=2` - Comparar inclusões, remoções e alterações de atividades.
- `PUT /api/schedules/{id}?expectedVersion=1&actor=coordenacao` - Salvar nova versão. `expectedVersion` é obrigatório; divergência retorna `409` e ausência retorna `428`.
- `POST /api/schedules/{id}/submit-for-approval?actor=coordenacao` - Submeter um rascunho.
- `POST /api/schedules/{id}/approve?actor=direcao` ou `POST /api/schedules/{id}/reject?actor=direcao&justification=...` - Registrar decisão; rejeição exige justificativa.
- `POST /api/schedules/{id}/publish?actor=direcao` - Publicar somente uma versão aprovada.
- `GET /api/schedules/{id}/published` - Consultar a versão publicada; grades publicadas não podem ser excluídas.
- `POST /api/schedules/{id}/adjustment-requests` - Abrir solicitação com `requester`, `description` e `activityId` opcional. `GET` na mesma rota consulta solicitações; `PATCH /api/schedules/{id}/adjustment-requests/{requestId}?actor=coordenacao&decision=ACEITA&response=...` registra a resposta.
- `GET /api/schedules/{id}/notifications?recipient=teacher-1` e `PATCH /api/schedules/{id}/notifications/{notificationId}/read?recipient=teacher-1` - Consultar e marcar notificações como lidas.
- `GET /api/schedules/{id}/audit` - Consultar eventos de criação, revisão, aprovação, publicação e solicitações de ajuste.
- `GET /api/schedule-reports` - Gerar relatório JSON; filtros opcionais: `academicPeriodId`, `status`, `teacherId`, `classId` e `roomResourceId`.

Os estados do fluxo são `RASCUNHO`, `PENDENTE_APROVACAO`, `APROVADA`, `REJEITADA` e `PUBLICADA`. Edições e alocações de sala/material são permitidas somente em rascunho; cada alteração cria uma nova versão. As versões, auditoria, solicitações e notificações são armazenadas no próprio documento MongoDB da grade. Notificações são registros consultáveis, sem integração de envio por e-mail ou push. O projeto ainda não implementa autenticação/autorização: `actor` e `recipient` são identificadores informados na requisição e não identidades verificadas, portanto não representam controle de acesso seguro.

#### Disponibilidade, recursos e alocação
- `GET/POST /api/teachers/{teacherId}/availability` - Consultar ou cadastrar disponibilidade/indisponibilidade por período letivo.
- `PUT/DELETE /api/teachers/{teacherId}/availability/{availabilityId}` - Editar ou remover enquanto a coleta do período estiver aberta. Disponibilidade recorrente usa `recurringWeekly`, `validFrom`, `validUntil`, `weekdays`, `dailyStartsAt` e `dailyEndsAt`.
- `GET /api/resources` - Pesquisar por `code`, `name`, `type` (`ROOM` ou `MATERIAL`), `location`, `state`, `startsAt` e `endsAt`. Para materiais, a resposta inclui `availableQuantity` calculada para o intervalo quando as datas são informadas.
- `GET/POST /api/resources`, `GET/PUT/DELETE /api/resources/{id}` - Consultar, cadastrar, editar ou inativar recurso. `DELETE` altera o estado para `INACTIVE`.
- `PATCH /api/resources/{id}/state?state=AVAILABLE|IN_USE|MAINTENANCE|INACTIVE` - Alterar o estado operacional.
- `GET /api/resources/{resourceId}/agenda?startsAt=...&endsAt=...` - Consultar alocações no intervalo.
- `GET /api/resources/{resourceId}/block-impact?startsAt=...&endsAt=...` - Listar alocações afetadas antes de solicitar bloqueio.
- `POST /api/resource-blocks` - Criar bloqueio pendente com motivo, responsável e intervalo; a resposta inclui as alocações afetadas.
- `GET/PUT /api/resource-blocks/{id}`, `POST /api/resource-blocks/{id}/confirm?actor=...` e `POST /api/resource-blocks/{id}/cancel?actor=...` - Consultar, editar, confirmar ou cancelar mantendo histórico.
- `PUT /api/schedules/{scheduleId}/activities/{activityId}/allocation?expectedVersion=1&actor=coordenacao` - Associar uma sala e quantidades de materiais à atividade; `DELETE` na mesma rota remove a alocação. Ambas exigem a versão esperada e geram nova versão.

A alocação verifica capacidade e características da sala, professores conflitantes ou indisponíveis, bloqueios confirmados, buffers e quantidade concorrente de materiais. As comparações de intervalo usam início inclusivo e fim exclusivo. A verificação e a gravação não são uma transação distribuída: concorrência simultânea entre requisições ainda requer uma garantia de persistência apropriada. Os horários usam `LocalDateTime`; o fuso institucional ainda precisa ser definido para atender integralmente aos RNFs temporais.

## Estrutura do Projeto

- `entity/` - Entidades MongoDB para estudantes, professores e planejamento acadêmico
- `repository/` - Interfaces da camada de acesso a dados
- `service/` - Camada de lógica de negócios
- `controller/` - Controladores web
- `templates/` - Templates Thymeleaf com Bootstrap

## Valor Educacional

Este projeto demonstra:
- Operações CRUD básicas
- Arquitetura MVC
- Fundamentos do Spring Boot
- Uso do Spring Data MongoDB
- Injeção de dependência
- Design de API RESTful
- Integração com banco de dados NoSQL
- Template frontend com Thymeleaf