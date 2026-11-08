# Jornadas de interface e cobertura de rotas

Atualizado em 2026-11-08. Este mapa distingue telas implementadas de contratos REST sem interface; a existência de uma rota não significa que ela faça parte de uma jornada visual.

## Administrador / coordenadora

Tela: `/admin/planning`.

Rotas conectadas: `GET/POST /api/academic-periods`, `GET/POST /api/courses`, `GET/POST /api/classes`, `GET/POST /api/subjects`, `GET /api/teachers`, `GET /api/resources`, `GET/POST /api/schedules`, `PUT /api/schedules/{id}/activities/{activityId}/allocation`, `GET /api/schedules/{id}/versions`, `GET /api/schedules/{id}/versions/compare`, submissão/aprovação/rejeição/publicação, `GET /api/schedules/{id}/audit`, decisões de solicitações de ajuste e `GET /api/schedule-reports` com exportação JSON.

Sem interface nesta jornada: edição e exclusão de período/curso/turma/disciplina (`PUT/DELETE` das coleções), edição e exclusão de grade (`PUT/DELETE /api/schedules/{id}`), remoção de alocação (`DELETE .../allocation`), consulta direta da versão publicada (`GET /api/schedules/{id}/published`) e `GET /api/schedules/{id}/notifications`. A área atual cria grades e encaminha decisões, mas não permite editar uma grade já salva.

## Professor

Tela: `/professor`. O perfil precisa do vínculo `APP_PROFESSOR_TEACHER_ID`; sem ele a interface não carrega nem grava dados pessoais. O valor deve ser o ID do mesmo cadastro usado em `/api/teachers/{teacherId}`.

Rotas conectadas: `GET/POST/PUT/DELETE /api/teachers/{teacherId}/availability`, `GET /api/teachers/{teacherId}/schedules`, `GET/POST /api/schedules/{id}/adjustment-requests`, `GET /api/schedules/{id}/notifications` e `PATCH .../notifications/{notificationId}/read`. A leitura da grade retorna apenas as atividades do professor; submissão/aprovação/publicação não estão disponíveis nesta jornada.

Sem interface ou contrato: comentários independentes sobre atividades (RF-PROF-008), registro dedicado de divergência (RF-PROF-019), exportação de calendário ICS (há impressão do navegador, não exportação de calendário) e entrega externa de notificações. A grade e os ajustes dependem de a coordenação já ter criado/submetido/publicado versões.

## Gestora de espaços e materiais

Tela: `/gestora`.

Rotas conectadas: `GET/POST/PUT /api/resources`, `PATCH /api/resources/{id}/state`, `GET /api/resources/{resourceId}/agenda`, `GET /api/resources/{resourceId}/block-impact`, `GET/POST /api/resource-blocks`, `POST /api/resource-blocks/{id}/confirm` e `POST /api/resource-blocks/{id}/cancel`. A confirmação consulta e apresenta as atividades afetadas imediatamente antes da decisão; o histórico fica visível por bloqueio.

Sem interface nesta jornada: reatribuir/editar um bloqueio (`PUT /api/resource-blocks/{id}`), consulta individual (`GET /api/resource-blocks/{id}`) e reservas SQL (`/api/reservations`, incluindo aprovação). Não há fluxo visual para ocorrências de dano/falha nem para registrar resolução/liberação (RF-REC-017/018). Inativação pode ser feita pelo seletor de estado; o `DELETE /api/resources/{id}` não é chamado pela tela.

## Analista de auditoria

Não há perfil de auditoria ou workspace correspondente. O relatório JSON e a auditoria de uma grade estão conectados apenas ao workspace administrativo. Faltam pesquisa global de eventos por agente/ação/período/recurso, consulta somente leitura de inventário e permissões, exportação estruturada de auditoria e auditoria unificada das rotas de recursos/reservas (RF-AUD-001, 007/008, 014/017/018/019).

## Limites compartilhados

- A tela de reservas SQL ainda não existe; as rotas `/api/reservations` são REST apenas.
- O login professor é uma conta local de demonstração, ligada a um único ID via configuração; não substitui gestão de identidade institucional.
- A interface não efetua entrega de e-mail/push nem exibe estado de entrega de provedor.
- As rotas de grade/alocação persistem no MongoDB e o módulo de reservas usa PostgreSQL; as telas não ocultam que são fluxos distintos e sem transação distribuída.