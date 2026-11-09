# ADR 0002: conectar reservas ao PostgreSQL e controlar quantidade de materiais

- Status: Aceito para o módulo de reservas implementado
- Data: 2026-11-06

## Contexto

O ADR 0001 definiu exclusão GiST para sobreposições de salas e professores, mas a migration V2 não era executada pela aplicação, não havia API/repositório de reservas e materiais não tinham persistência SQL ou controle concorrente. O PRD exige ausência de conflitos de professor/sala/material (`RF-COORD-009`, `RF-COORD-010`, `RF-REC-015`), consistência sob concorrência (`RNF-GER-013`, `RNF-REC-003`) e registro de tentativas recusadas (`RF-AUD-007`). As regras de aprovação/manutenção nos documentos BDD são hipóteses locais ainda não formalizadas pelo PRD.

O planejamento acadêmico existente mantém recursos, bloqueios, disponibilidade e grades no MongoDB. Migrar todo esse domínio para PostgreSQL não faz parte deste ADR.

## Decisão

- Adicionar Flyway/JDBC e executar as migrations quando `reservations.datasource.url` estiver configurada; preservar a inicialização do restante da aplicação sem essa propriedade.
- Usar `/api/reservations` e `ReservationWorkflowService` como fluxo de escrita no PostgreSQL; persistir os dados comuns em `reservation` e o intervalo do recurso em `room_reservation`, `teacher_agenda` ou `material_reservation` na mesma transação.
- Manter exclusões GiST parciais para estados ativos de sala/professor. Para material, serializar por `resource_id` com advisory transaction lock, calcular o pico concorrente em intervalos semiabertos e rejeitar excesso antes do insert.
- Registrar solicitações, transições e tentativas rejeitadas em `reservation_audit_event`; mapear conflitos de exclusão e quantidade para HTTP 409.
- Consultar o planejamento MongoDB antes da escrita para respeitar estado do recurso, bloqueios, buffers, disponibilidade e alocações já cadastradas.
- Não acrescentar cancelamento/rejeição/não comparecimento nem presumir autorização real: atores e papéis ainda não têm integração com identidade/autorização do projeto.

## Consequências e limites

- A aplicação aplica migrations e o teste Testcontainers usa as mesmas migrations de produção, evitando duplicar o DDL no teste.
- O controle SQL garante concorrência entre solicitações que passam por `/api/reservations`; ele não cria transação distribuída com as rotas de grade/alocação Mongo. Essas rotas podem divergir e precisam ser unificadas ou sincronizadas antes de declarar cobertura integral de `RNF-GER-014`/`RNF-REC-003` entre todos os fluxos.
- A ativação é opcional para não exigir PostgreSQL de quem usa somente as funções existentes; o deploy que habilitar reservas deve fornecer URL, credenciais e PostgreSQL com `btree_gist`.
- `actorId`, `requesterId` e `role` vêm da requisição; sem autenticação/autorização não são prova de identidade nem atendem `RNF-GER-003`.
- A execução da migration e do teste de concorrência deve ser confirmada em ambiente PostgreSQL real; no runner atual, Testcontainers é bloqueado por incompatibilidade do Docker API.

## Alternativas consideradas

1. Manter reserva em memória e testar apenas regras: rejeitada, pois não liga migration nem assegura concorrência entre requisições.
2. Fazer reserva somente em MongoDB: rejeitada para este fluxo, pois não usaria a exclusão GiST aprovada no ADR 0001.
3. Migrar grades e recursos integralmente para PostgreSQL: não selecionada; excede a lacuna e exige plano de migração do domínio existente.