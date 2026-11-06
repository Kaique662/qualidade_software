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

Uma atividade de grade deve referenciar curso, turma, disciplina e ao menos um professor existente. As datas da atividade precisam estar dentro do período letivo e o fim deve ser posterior ao início. Grades são salvas como `RASCUNHO`; aprovação, publicação, recorrência e detecção de conflitos ainda não fazem parte desta implementação.

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
- `PUT /api/schedules/{scheduleId}/activities/{activityId}/allocation` - Associar uma sala e quantidades de materiais à atividade; `DELETE` na mesma rota remove a alocação.

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