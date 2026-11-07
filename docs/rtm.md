# Matriz de rastreabilidade (RTM)

## Requisito de negócio

- R1: recurso restrito exige aprovação de um responsável.
- R2: solicitante não pode aprovar sua própria reserva.
- R3: recurso em manutenção não pode ser reservado.
- R4: a reserva segue o fluxo SOLICITADA → APROVADA → EM_USO → CONCLUIDA.
- R5: transições fora do fluxo devem ser rejeitadas.

## Rastreio para cenário executável

| Requisito | Cenário BDD | Arquivo do cenário | Implementação | Evidência |
| --- | --- | --- | --- | --- |
| R1 | aprovação de recurso restrito | `src/test/resources/features/reservation-approval-and-blocking.feature` | `ReservationWorkflowService.approve`, `ReservationJdbcRepository.transition` | `ReservationWorkflowServiceTest`, Cucumber + JUnit |
| R2 | autoaprovação bloqueada | `src/test/resources/features/reservation-approval-and-blocking.feature` | `ReservationService.approve`, `ReservationWorkflowService.approve` | `ReservationWorkflowServiceTest`, Cucumber + JUnit |
| R3 | manutenção em conflito | `src/test/resources/features/reservation-approval-and-blocking.feature` | `ResourcePlanningService.validateReservationResource`, `ReservationService.request` | `ReservationWorkflowServiceTest`, Cucumber + JUnit; PostgreSQL/Testcontainers pendente no ambiente |
| R4 | fluxo principal | `src/test/resources/features/reservation-approval-and-blocking.feature` | `ReservationWorkflowService.approve`, `startUse`, `complete` | `ReservationServiceTest`, `ReservationWorkflowServiceTest`, Cucumber + JUnit |
| R5 | transições inválidas | `src/test/resources/features/reservation-approval-and-blocking.feature` | `ReservationService.transition`, `ReservationWorkflowService` | `ReservationServiceTest`, Cucumber + JUnit |

## Observação

R1–R5 vieram da solicitação que originou o BDD, não dos requisitos formais em `PRD/`; permanecem rastreados como regras locais até validação institucional. A implementação atual conecta esse domínio a persistência JDBC/Flyway opcional e à API `/api/reservations`. Os testes unitários de domínio/serviço passaram; os testes de PostgreSQL real aplicam as migrations, mas sua execução depende de corrigir o Docker API do runner.

## Rastreabilidade para requisitos do PRD

| Requisito | Implementação relacionada | Evidência disponível | Lacuna remanescente |
| --- | --- | --- | --- |
| `RF-COORD-009`, `RF-COORD-010` | Validação compartilhada com agenda Mongo; exclusões PostgreSQL para intervalos ativos de sala e professor. | `ReservationWorkflowServiceTest`; `ConcurrentReservationIntegrationTest` aplica Flyway e executa a competição SQL. | Teste PostgreSQL não executado neste runner; gravações Mongo e SQL não compartilham transação. |
| `RF-COORD-007`, `RF-REC-015`, `RNF-REC-003` | `material_reservation`, lock transacional por material, soma de quantidade simultânea e disponibilidade agregada. | `ResourcePlanningServiceTest`, `ReservationWorkflowServiceTest`; caso concorrente em `ConcurrentReservationIntegrationTest`. | Concorrência do teste real não executada; gravação Mongo concorrente pode divergir da reserva SQL. |
| `RF-AUD-007`, `RNF-GER-007` | `reservation_audit_event` registra solicitações, decisões, transições e recusas por conflito/estado. | `ReservationWorkflowServiceTest`; persistência SQL pendente de execução com PostgreSQL local/CI. | Não há autenticação, correlação nem auditoria compartilhada das rotas Mongo. |

As regras de aprovação/manutenção R1–R5 não substituem os RF/RNF formais nem demonstram autorização segura; ver `docs/testes/bdd-aprovacao-bloqueio.md` e ADR 0002.

## Requisitos não funcionais de desempenho

| Requisito | Definição e carga | Plano | Resultado/evidência |
| --- | --- | --- | --- |
| RNF-PERF-01 | p95 da consulta de disponibilidade <= 300 ms com 50 usuários concorrentes; ver proposta e modelo de carga | `perf/reservas.jmx` | `docs/performance/resultado-01.md` (Rodada 01 inconclusiva; endpoints existem desde 2026-11-06, mas a medição permanece pendente) |
| RNF-PERF-02 | p95 da criação <= 300 ms; respostas 201/409, erro inesperado < 1%, concorrência sobre o mesmo recurso/intervalo | `perf/reservas.jmx` | `docs/performance/resultado-01.md` (sem amostras; integridade e percentis não medidos) |

Detalhes das metas propostas e do modelo de carga: `docs/performance/rnf.md`. Dados de entrada fictícios: `perf/usuarios.csv`. Nenhum resultado desta rodada deve ser interpretado como medição da aplicação em produção.
