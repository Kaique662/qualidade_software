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
| R1 | aprovação de recurso restrito | `src/test/resources/features/reservation-approval-and-blocking.feature` | `ReservationService.approve` | Cucumber + JUnit |
| R2 | autoaprovação bloqueada | `src/test/resources/features/reservation-approval-and-blocking.feature` | `ReservationService.canApproveRestrictedReservationExcludingRequester` | Cucumber + JUnit |
| R3 | manutenção em conflito | `src/test/resources/features/reservation-approval-and-blocking.feature` | `ReservationService.request(..., startsAt, endsAt)` | Cucumber + JUnit |
| R4 | fluxo principal | `src/test/resources/features/reservation-approval-and-blocking.feature` | `ReservationService.approve`, `startUse`, `complete` | Cucumber + JUnit |
| R5 | transições inválidas | `src/test/resources/features/reservation-approval-and-blocking.feature` | `ReservationService.transition` | Cucumber + JUnit |

## Observação

A implementação atual usa o domínio de reserva em memória do serviço de teste como referência de comportamento para a especificação executável. O documento de cenários original em `docs/testes/bdd-aprovacao-bloqueio.md` foi mantido como fonte de requisitos e a execução foi convertida em BDD com Cucumber/JUnit.

## Requisitos não funcionais de desempenho

| Requisito | Definição e carga | Plano | Resultado/evidência |
| --- | --- | --- | --- |
| RNF-PERF-01 | p95 da consulta de disponibilidade <= 300 ms com 50 usuários concorrentes; ver proposta e modelo de carga | `perf/reservas.jmx` | `docs/performance/resultado-01.md` (Rodada 01 inconclusiva: sem JTL e sem aplicação local disponível) |
| RNF-PERF-02 | p95 da criação <= 300 ms; respostas 201/409, erro inesperado < 1%, concorrência sobre o mesmo recurso/intervalo | `perf/reservas.jmx` | `docs/performance/resultado-01.md` (sem amostras; separação 409/erro e integridade de banco não verificadas) |

Detalhes das metas propostas e do modelo de carga: `docs/performance/rnf.md`. Dados de entrada fictícios: `perf/usuarios.csv`. Nenhum resultado desta rodada deve ser interpretado como medição da aplicação em produção.
