# 🎓 Projeto de Qualidade Educacional

**Sistema web de gestão acadêmica e recursos educacionais**, desenvolvido em Java com Spring Boot. Reúne cadastros, planejamento de aulas, revisão de grades, disponibilidade docente, manutenção de salas e materiais, além de uma API REST para integração e testes de qualidade de software.

> **Para começar rapidamente:** abra o projeto no GitHub Codespaces, configure o **Java 17** e a conexão com o **MongoDB Atlas** e execute `./mvnw spring-boot:run`.

## 📑 Índice

- [Visão geral](#-visão-geral)
- [Tecnologias](#-tecnologias)
- [Início rápido no GitHub Codespaces](#-início-rápido-no-github-codespaces)
- [Acesso para avaliação do professor](#-acesso-para-avaliação-do-professor)
- [Acesso ao sistema e perfis](#-acesso-ao-sistema-e-perfis)
- [Execução local e comandos úteis](#-execução-local-e-comandos-úteis)
- [Testes automatizados](#-testes-automatizados)
- [Documentação da API](#-documentação-da-api)
- [Estrutura do projeto](#-estrutura-do-projeto)
- [Problemas frequentes](#-problemas-frequentes)
- [Limitações e melhorias futuras](#-limitações-e-melhorias-futuras)

## ✨ Visão geral

| Área | Funcionalidades |
| --- | --- |
| **Cadastros** | Gerenciamento de estudantes e professores |
| **Planejamento acadêmico** | Períodos letivos, cursos, turmas, disciplinas, grades e atividades |
| **Fluxo de grades** | Rascunhos, versões, comparação, revisão, aprovação, publicação e auditoria |
| **Professores** | Disponibilidade, agenda, solicitações de ajuste e consulta de notificações |
| **Gestão de recursos** | Salas, materiais, disponibilidade, bloqueios e identificação de aulas impactadas |
| **Remanejamento** | Solicitação de troca de sala para revisão; **a aprovação não altera a sala automaticamente** |
| **Reservas opcionais** | API de reservas com PostgreSQL, Flyway e regras de concorrência |

A interface utiliza **Thymeleaf** e **Bootstrap**. Os dados acadêmicos são armazenados no **MongoDB**. O módulo adicional de reservas, quando habilitado, utiliza **PostgreSQL**.

## 🛠 Tecnologias

| Camada | Tecnologia |
| --- | --- |
| Linguagem | Java 17 |
| Backend | Spring Boot 3.2.0 |
| Build | Maven / Maven Wrapper (`mvnw`) |
| Persistência principal | Spring Data MongoDB / MongoDB Atlas |
| Interface web | Thymeleaf e Bootstrap |
| Reservas opcionais | PostgreSQL e Flyway |
| Testes de integração | JUnit, Testcontainers e Docker |

## 🚀 Início rápido no GitHub Codespaces

Este roteiro é recomendado para **quem acabou de criar um Codespace ou está acessando o projeto por outra conta**.

### 1. Abra o repositório

1. Acesse **[Kaique662/qualidade_software](https://github.com/Kaique662/qualidade_software)**.
2. Clique em **Code → Codespaces → Create codespace on main**.
3. Aguarde a abertura do VS Code e abra o terminal.
4. Confira a branch atual:

   ```bash
   git branch --show-current
   ```

   Se as alterações necessárias estiverem na branch `ci/github-actions-maven-verify` e ela estiver disponível no remoto, use:

   ```bash
   git fetch origin
   git switch ci/github-actions-maven-verify
   ```

> **Atenção:** um Codespace novo só terá as alterações que já foram enviadas ao repositório remoto. Arquivos não commitados em outro Codespace não são transferidos automaticamente.

### 2. Instale e selecione o Java 17

Cole este bloco inteiro no terminal:

```bash
sudo apt-get update
sudo apt-get install -y openjdk-17-jdk

export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"

java -version
```

O comando `java -version` deverá apresentar **Java 17**. As variáveis definidas com `export` valem para a sessão atual do terminal.

### 3. Configure o MongoDB Atlas

1. Acesse **[MongoDB Atlas](https://cloud.mongodb.com/)**.
2. Abra o projeto e selecione o cluster correspondente.
3. Clique em **Connect → Drivers** e copie a URI de conexão.
4. Substitua o usuário e a senha pelos dados do **Database Access**.
5. Especifique o banco `qualidade_software` após `.net/`.

**Modelo de URI** (sem credenciais reais):

```text
mongodb+srv://Teste:123456d7asd234a567ADSD345627312DSAD@projetin.hy8qs1c.mongodb.net/qualidade_software?retryWrites=true&w=majority
```

No terminal, cole este bloco:

```bash
read -r -s -p "Cole a URI completa do MongoDB Atlas: " MONGODB_URI
echo
export MONGODB_URI
export SPRING_DATA_MONGODB_URI="$MONGODB_URI"
```

Quando aparecer a solicitação, cole **apenas a URI completa**, sem aspas, e pressione **Enter**. A senha não será mostrada na tela.

> 🔒 **Segurança:** nunca inclua uma URI com senha no README, no código, em commits ou em mensagens públicas. Para uso recorrente, configure `MONGODB_URI` em **GitHub → Settings → Codespaces → Secrets**, autorize o repositório e reinicie o ambiente. O valor também pode ser utilizado como `SPRING_DATA_MONGODB_URI`. Se a senha tiver caracteres especiais, aplique a codificação adequada para URIs.

**Verifique as variáveis sem exibir credenciais:**

```bash
python3 - <<'PY'
import os
uri = os.environ.get("SPRING_DATA_MONGODB_URI", "")
print("URI configurada:", bool(uri))
print("Formato válido:", uri.startswith(("mongodb://", "mongodb+srv://")))
print("Banco informado:", "/qualidade_software" in uri)
PY
```

O esperado é `True` nas três verificações. Isso **não garante** que o cluster esteja acessível ou que as credenciais sejam válidas.

Sem URI configurada, a aplicação pode tentar usar `mongodb://localhost:27017/qualidade_software` — endereço que não corresponde ao Atlas.

### 4. Inicie a aplicação

No mesmo terminal em que configurou as variáveis:

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

Quando o servidor iniciar corretamente:

- No **Codespaces**, abra **Ports → 8080 → Open in Browser**.
- Em execução **local**, acesse `http://localhost:8080/login`.

Para encerrar, pressione **Ctrl + C**. Se abrir outro terminal, talvez seja necessário configurar novamente `JAVA_HOME` e as variáveis do MongoDB.

## 🧑‍🏫 Acesso para avaliação do professor

Para executar e avaliar o sistema, o professor precisará de acesso ao repositório, de um Codespace com **Java 17** e da conexão com o **MongoDB Atlas**. O passo a passo completo está na seção [Início rápido no GitHub Codespaces](#-início-rápido-no-github-codespaces).

**Informações do banco de dados:**

| Campo | Valor |
| --- | --- |
| Serviço | MongoDB Atlas |
| Cluster | `projetin.hy8qs1c.mongodb.net` |
| Banco | `qualidade_software` |
| Usuário de banco | `Admin` |
| Senha | **Fornecida ao professor por canal privado** |

> **Importante:** o usuário `Admin` acima pertence ao **MongoDB Atlas**; ele não é necessariamente o mesmo login `admin` da aplicação web. Por segurança, a senha do banco **não deve ser publicada no README nem em commits**. Para a avaliação, compartilhe a senha atual com o professor em mensagem privada, preferencialmente usando um usuário exclusivo, com permissões limitadas e acesso temporário. Se alguma senha já tiver sido exposta, altere-a no Atlas antes da entrega.

Depois de receber a senha, o professor deve montar a URI no formato:

```text
mongodb+srv://Admin:SENHA_RECEBIDA@projetin.hy8qs1c.mongodb.net/qualidade_software?retryWrites=true&w=majority
```

No **terminal do Codespaces**, configure a URI sem exibir a senha nem gravá-la no histórico de comandos:

```bash
read -r -s -p "Cole a URI completa do MongoDB Atlas: " MONGODB_URI
echo
export MONGODB_URI
export SPRING_DATA_MONGODB_URI="$MONGODB_URI"
./mvnw spring-boot:run
```

Se a conexão falhar, confira o usuário e a senha em **Database Access**, as permissões de rede em **Network Access** e se o cluster está ativo. Para testar sem iniciar a interface, use `./mvnw test`; os testes de integração com Testcontainers exigem Docker.

---

## 👥 Acesso ao sistema e perfis

O projeto apresenta três perfis demonstrativos:

| Perfil | Área de uso |
| --- | --- |
| **Administrador** (`admin`) | Cadastros e planejamento acadêmico |
| **Professor** (`professor`) | Disponibilidade, agenda e solicitações |
| **Gestora** (`gestora`) | Salas, materiais, manutenção e análise de impacto |

Para definir **senhas próprias de demonstração** no ambiente local, configure antes de iniciar:

```bash
export APP_ADMIN_PASSWORD='SENHA_LOCAL_ADMIN'
export APP_PROFESSOR_PASSWORD='SENHA_LOCAL_PROFESSOR'
export APP_MANAGER_PASSWORD='SENHA_LOCAL_GESTORA'
# Opcional: vincule o login demonstrativo a um professor existente.
export APP_PROFESSOR_TEACHER_ID='ID_DO_PROFESSOR_NO_MONGODB'

./mvnw spring-boot:run
```

Os nomes de usuário também podem ser alterados com `APP_ADMIN_USERNAME`, `APP_PROFESSOR_USERNAME` e `APP_MANAGER_USERNAME`.

No painel administrativo, é possível cadastrar um professor com **e-mail e senha de pelo menos 8 caracteres**. A senha é armazenada como hash no MongoDB, e o professor pode alterá-la em **Minha conta**. Ao editar um professor, deixe a senha vazia para preservá-la. O cadastro vincula a conta à disponibilidade, à agenda e às solicitações.

> **Observação:** os logins demonstrativos por variáveis de ambiente são destinados ao uso local. O controle de sessão e os acessos da interface não substituem um mecanismo de identidade e autorização adequado para produção.

## 💻 Execução local e comandos úteis

### Clonar e acessar o repositório

```bash
git clone https://github.com/Kaique662/qualidade_software.git
cd qualidade_software
```

Após configurar Java e MongoDB, inicie com `./mvnw spring-boot:run` no Linux/macOS ou `mvnw.cmd spring-boot:run` no Windows.

**Não é necessário instalar Maven separadamente quando o Maven Wrapper está disponível.** Caso prefira uma instalação global, consulte a [documentação oficial do Maven](https://maven.apache.org/install.html).

### Comandos mais usados

| Objetivo | Comando |
| --- | --- |
| Iniciar a aplicação | `./mvnw spring-boot:run` |
| Compilar | `./mvnw compile` |
| Executar testes | `./mvnw test` |
| Criar o pacote | `./mvnw package` |
| Limpar arquivos gerados | `./mvnw clean` |
| Gerar JAR com build completo | `./mvnw clean package` |

Para executar o JAR após o empacotamento:

```bash
java -jar target/educational-quality-project-0.0.1-SNAPSHOT.jar
```

Rotas de interface utilizadas no projeto:

- Login: `/login`
- Cadastro de estudantes: `/students`
- Cadastro de professores: `/teachers`
- Planejamento acadêmico: `/admin/planning`
- Área do professor: `/professor`
- Gestão de recursos: `/gestora`

## 🧪 Testes automatizados

Execute a suíte completa com:

```bash
./mvnw test
```

Os testes de integração de reservas utilizam **PostgreSQL via Testcontainers**, que requer Docker funcionando no ambiente:

```bash
docker info
./mvnw -Dtest=ConcurrentReservationIntegrationTest test
```

Um resultado bem-sucedido apresenta **`BUILD SUCCESS`** e **zero falhas e erros** nos testes. A quantidade total de testes pode variar conforme a versão do projeto.

## 📚 Documentação da API

A aplicação disponibiliza endpoints REST. As operações podem depender do perfil e das regras de acesso configuradas.

### Estudantes e professores

| Método | Endpoint | Finalidade |
| --- | --- | --- |
| `GET` | `/api/students` | Listar estudantes |
| `GET` | `/api/students/{id}` | Consultar estudante |
| `POST` | `/api/students` | Cadastrar estudante |
| `PUT` | `/api/students/{id}` | Atualizar estudante |
| `DELETE` | `/api/students/{id}` | Excluir estudante |
| `GET` | `/api/teachers` | Listar professores |
| `GET` | `/api/teachers/{id}` | Consultar professor |
| `POST` | `/api/teachers` | Cadastrar professor |
| `PUT` | `/api/teachers/{id}` | Atualizar professor |
| `DELETE` | `/api/teachers/{id}` | Excluir professor |

### Planejamento acadêmico

Os recursos abaixo possuem operações de consulta (`GET`), criação (`POST`), edição (`PUT`) e exclusão (`DELETE`), conforme as regras de cada recurso.

| Recurso | Descrição |
| --- | --- |
| `/api/academic-periods` | Períodos letivos e datas de início e fim |
| `/api/courses` | Cursos |
| `/api/classes` | Turmas associadas a curso e período |
| `/api/subjects` | Disciplinas associadas a curso |
| `/api/schedules` | Grades, atividades e professores |

As atividades devem referenciar curso, turma, disciplina e pelo menos um professor existente. O horário precisa estar dentro do período letivo, com fim posterior ao início. Versões das atividades são registradas ao criar ou editar grades.

### Versões, revisão e publicação de grades

| Método | Endpoint | Finalidade |
| --- | --- | --- |
| `GET` | `/api/schedules/{id}/versions` | Consultar versões |
| `GET` | `/api/schedules/{id}/versions/compare?fromVersion=1&toVersion=2` | Comparar versões |
| `PUT` | `/api/schedules/{id}?expectedVersion=1&actor=coordenacao` | Salvar versão com controle de concorrência |
| `POST` | `/api/schedules/{id}/submit-for-approval?actor=coordenacao` | Enviar rascunho para aprovação |
| `POST` | `/api/schedules/{id}/approve?actor=direcao` | Aprovar grade |
| `POST` | `/api/schedules/{id}/reject?actor=direcao&justification=...` | Rejeitar com justificativa |
| `POST` | `/api/schedules/{id}/publish?actor=direcao` | Publicar versão aprovada |
| `GET` | `/api/schedules/{id}/published` | Consultar versão publicada |
| `GET` | `/api/schedules/{id}/audit` | Consultar auditoria |
| `GET` | `/api/schedule-reports` | Consultar relatório JSON |

Em atualizações com `expectedVersion`, uma divergência de versão retorna **409**, e a ausência do parâmetro retorna **428**. Os estados de uma grade são `RASCUNHO`, `PENDENTE_APROVACAO`, `APROVADA`, `REJEITADA` e `PUBLICADA`. As grades publicadas não podem ser excluídas; alterações e alocações de salas/materiais ocorrem na fase de rascunho.

**Solicitações e notificações:**

| Método | Endpoint | Finalidade |
| --- | --- | --- |
| `GET/POST` | `/api/schedules/{id}/adjustment-requests` | Listar ou abrir solicitações de ajuste |
| `PATCH` | `/api/schedules/{id}/adjustment-requests/{requestId}?actor=coordenacao&decision=ACEITA&response=...` | Aceitar ou rejeitar solicitação |
| `GET` | `/api/schedules/{id}/notifications?recipient=teacher-1` | Consultar notificações |
| `PATCH` | `/api/schedules/{id}/notifications/{notificationId}/read?recipient=teacher-1` | Marcar como lida |

O corpo de uma solicitação de ajuste recebe `requester`, `description` e `activityId` (opcional). Versões, auditoria, solicitações e notificações são armazenadas no documento MongoDB da grade. As notificações são **registros consultáveis**, sem envio automático por e-mail ou push.

### Disponibilidade docente, recursos e bloqueios

| Método | Endpoint | Finalidade |
| --- | --- | --- |
| `GET/POST` | `/api/teachers/{teacherId}/availability` | Consultar ou cadastrar disponibilidade |
| `PUT/DELETE` | `/api/teachers/{teacherId}/availability/{availabilityId}` | Alterar/remover disponibilidade |
| `GET/POST` | `/api/resources` | Pesquisar ou cadastrar recursos |
| `GET/PUT/DELETE` | `/api/resources/{id}` | Consultar, editar ou inativar recurso |
| `PATCH` | `/api/resources/{id}/state?state=AVAILABLE` | Atualizar estado operacional |
| `GET` | `/api/resources/{resourceId}/agenda?startsAt=...&endsAt=...` | Consultar agenda do recurso |
| `GET` | `/api/resources/{resourceId}/block-impact?startsAt=...&endsAt=...` | Identificar aulas afetadas |
| `POST` | `/api/resource-blocks` | Criar bloqueio pendente |
| `GET/PUT` | `/api/resource-blocks/{id}` | Consultar ou editar bloqueio |
| `POST` | `/api/resource-blocks/{id}/confirm?actor=...` | Confirmar bloqueio |
| `POST` | `/api/resource-blocks/{id}/cancel?actor=...` | Cancelar bloqueio |
| `PUT/DELETE` | `/api/schedules/{scheduleId}/activities/{activityId}/allocation?expectedVersion=1&actor=coordenacao` | Associar ou remover alocação |

A disponibilidade pode ser semanal e recorrente, usando `recurringWeekly`, `validFrom`, `validUntil`, `weekdays`, `dailyStartsAt` e `dailyEndsAt`. Os recursos podem ser dos tipos `ROOM` ou `MATERIAL`, com filtros por código, nome, localização, estado e período. Para materiais, a consulta de período pode apresentar `availableQuantity`.

As alocações verificam capacidade e características da sala, disponibilidade docente, bloqueios confirmados, buffers e quantidades concorrentes de materiais. Os intervalos usam **início inclusivo e fim exclusivo**.

### Reservas de recursos — módulo opcional (PostgreSQL)

O módulo é ativado quando a propriedade `reservations.datasource.url` está configurada, juntamente com `reservations.datasource.username` e `reservations.datasource.password`. O **Flyway** aplica as migrations em `src/main/resources/db/migration`. Sem essa configuração, a API de reservas permanece desativada e as telas que usam MongoDB continuam independentes desse módulo.

| Método | Endpoint | Finalidade |
| --- | --- | --- |
| `POST` | `/api/reservations` | Criar solicitação de reserva |
| `GET` | `/api/reservations?resourceId=...&status=...&startsBefore=...&endsAfter=...` | Listar com filtros |
| `GET` | `/api/reservations/{id}` | Consultar reserva |
| `POST` | `/api/reservations/{id}/approve?actorId=...&role=RESPONSAVEL` | Aprovar reserva |
| `POST` | `/api/reservations/{id}/start?actorId=...` | Marcar início de uso |
| `POST` | `/api/reservations/{id}/complete?actorId=...` | Concluir uso |
| `GET` | `/api/reservations/{id}/history` | Consultar histórico |

Para criar uma reserva, o corpo utiliza `resourceId`, `resourceType` (`ROOM`, `PROFESSOR` ou `MATERIAL`), `requesterId`, `startsAt`, `endsAt`, `quantity` (opcional; padrão 1) e `academicPeriodId` (obrigatório para professor). Use datas **ISO-8601 com offset**, por exemplo `2026-03-01T10:00:00Z`.

Os recursos de sala e professor usam restrições de exclusão **GiST** no PostgreSQL. Os materiais utilizam travamento transacional por recurso e análise do pico de quantidade. Conflitos retornam **HTTP 409**. O fluxo de reservas considera ainda bloqueios, buffers e informações de agenda do MongoDB.

**Limites do módulo opcional:** não há transação distribuída entre PostgreSQL e MongoDB. Informações como `requesterId`, `actorId` e `role` podem ser informadas pelo cliente e não devem ser tratadas, isoladamente, como comprovação segura de identidade. O módulo não deve ser exposto como produção sem revisão de segurança. Rejeição, cancelamento, não comparecimento e notificações deste módulo ainda dependem de requisitos adicionais.

## 🗂 Estrutura do projeto

```text
src/
├── main/
│   ├── java/                 # Controllers, services, repositories e entidades
│   └── resources/
│       ├── templates/        # Telas HTML / Thymeleaf
│       ├── static/           # JavaScript, CSS e outros recursos
│       └── db/migration/     # Migrações Flyway do módulo de reservas
└── test/                     # Testes automatizados
```

Organização lógica do backend:

- `entity/` — entidades persistidas no MongoDB.
- `repository/` — interfaces de acesso a dados.
- `service/` — regras de negócio.
- `controller/` — endpoints HTTP e controllers web.
- `templates/` — páginas renderizadas com Thymeleaf.

Consulte também [**docs/interface-jornadas.md**](docs/interface-jornadas.md) para o mapa de jornadas, telas e rotas.

## 🩺 Problemas frequentes

| Mensagem / situação | O que verificar |
| --- | --- |
| `Connection refused localhost:27017` | URI do Atlas não foi aplicada; a aplicação tenta o MongoDB local |
| `The connection string is invalid` | A URI deve começar com `mongodb://` ou `mongodb+srv://`, sem aspas coladas ao valor |
| `Authentication failed` | Usuário e senha do **Database Access**; caracteres especiais na senha |
| `Database name must not be empty` | Presença de `/qualidade_software` na URI |
| `Timed out` ao conectar no Atlas | Cluster ativo e permissão de rede em **Network Access** |
| `Port 8080 already in use` | Outra instância do Spring Boot está executando |
| Docker/Testcontainers falhou | Docker instalado, daemon acessível e configuração de ambiente |
| Java diferente de 17 | Confira `java -version`, `JAVA_HOME` e `PATH` |
| Página do Codespaces não abre | Verifique **Ports → 8080** e o status do servidor |

> **Importante:** `BUILD SUCCESS` no comando `spring-boot:run` não substitui a confirmação de que a aplicação realmente iniciou. Verifique os logs do Spring Boot e teste a tela de login.

## 🔭 Limitações e melhorias futuras

- **Remanejamento de salas:** a Gestora pode solicitar a troca e o pedido pode ser analisado; a decisão registrada **não executa automaticamente** a transferência da aula para a sala sugerida. Uma evolução é validar disponibilidade e aplicar a troca após aprovação.
- **Notificações:** são consultadas no sistema; não há envio automático por e-mail ou push nas rotinas de grades descritas.
- **Concorrência entre bancos:** os fluxos que envolvem PostgreSQL e MongoDB não constituem uma transação distribuída, podendo exigir mecanismos adicionais de consistência.
- **Segurança:** o acesso por perfis e sessão da interface não deve ser confundido com autorização robusta de produção em todas as APIs; campos `actor`/`recipient` e parâmetros semelhantes exigem validação adequada.
- **Datas e fusos:** as grades utilizam `LocalDateTime`; a definição explícita de um fuso institucional continua relevante.

## 🎯 Valor educacional

O projeto permite praticar **arquitetura MVC, CRUD, APIs REST, Spring Boot, injeção de dependência, persistência NoSQL, versionamento, testes automatizados, validação de regras de negócio e concorrência de reservas**.

---

**Projeto acadêmico — Qualidade de Software.**
