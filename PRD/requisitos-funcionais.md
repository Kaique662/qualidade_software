# Requisitos Funcionais (RF)

## Objetivo

Este documento reúne os requisitos funcionais associados às quatro personas do sistema proposto para alocação de salas, professores e materiais sem conflitos de horário. Os requisitos descrevem comportamentos observáveis do sistema e devem ser refinados com critérios de aceitação e testes.

Os IDs são únicos e foram mantidos nos arquivos das personas para facilitar rastreabilidade. Os perfis e cenários são hipóteses ilustrativas, não resultados de pesquisa com usuários.

## Coordenadora de horários

1. **RF-COORD-001:** O sistema deve permitir criar e consultar períodos letivos com datas de início e fim.
2. **RF-COORD-002:** O sistema deve permitir cadastrar e editar cursos, turmas, disciplinas e atividades vinculadas a um período letivo.
3. **RF-COORD-003:** O sistema deve permitir associar um ou mais professores a cada atividade, conforme as regras institucionais.
4. **RF-COORD-004:** O sistema deve permitir definir data, horário inicial, horário final, duração e recorrência de uma atividade.
5. **RF-COORD-005:** O sistema deve permitir registrar disponibilidade e indisponibilidade de professores para o período selecionado.
6. **RF-COORD-006:** O sistema deve permitir selecionar uma sala compatível com capacidade mínima e características exigidas pela atividade.
7. **RF-COORD-007:** O sistema deve permitir associar materiais a uma atividade e informar a quantidade necessária.
8. **RF-COORD-008:** O sistema deve permitir pesquisar salas e materiais disponíveis para um intervalo específico.
9. **RF-COORD-009:** Ao salvar uma alocação, o sistema deve verificar conflitos de horário para professor, sala e materiais.
10. **RF-COORD-010:** A verificação de conflito deve considerar qualquer sobreposição entre os intervalos, inclusive quando um intervalo começa ou termina dentro de outro.
11. **RF-COORD-011:** O sistema deve considerar os tempos de preparação, limpeza ou deslocamento configurados para os recursos.
12. **RF-COORD-012:** Quando houver conflito, o sistema deve identificar o recurso, as atividades envolvidas e os intervalos conflitantes.
13. **RF-COORD-013:** Para uma alocação inválida, o sistema deve apresentar opções de horários ou recursos que atendam às regras cadastradas, quando houver.
14. **RF-COORD-014:** O sistema deve permitir criar e editar alocações manualmente em uma grade de horários.
15. **RF-COORD-015:** O sistema deve permitir solicitar uma proposta de alocação automática sem publicar alterações automaticamente.
16. **RF-COORD-016:** O sistema deve permitir salvar uma grade como rascunho sem torná-la visível como horário oficial.
17. **RF-COORD-017:** Ao salvar uma grade alterada por outra pessoa desde a última consulta, o sistema deve detectar a versão desatualizada e solicitar revisão antes de sobrescrever.
18. **RF-COORD-018:** O sistema deve permitir comparar duas versões de uma grade e listar inclusões, remoções e alterações de alocação.
19. **RF-COORD-019:** O sistema deve permitir submeter uma versão para aprovação e registrar o resultado e o responsável pela decisão.
20. **RF-COORD-020:** O sistema deve permitir publicar somente uma versão aprovada e manter as versões publicadas anteriores consultáveis.

## Professor

1. **RF-PROF-001:** O sistema deve permitir que o professor consulte seu vínculo, disciplinas, turmas e período letivo associados.
2. **RF-PROF-002:** O sistema deve permitir informar os intervalos em que está disponível para atividades acadêmicas.
3. **RF-PROF-003:** O sistema deve permitir informar indisponibilidades recorrentes e suas datas de início e fim.
4. **RF-PROF-004:** O sistema deve permitir classificar cada horário informado como restrição obrigatória ou preferência negociável.
5. **RF-PROF-005:** O sistema deve permitir editar ou remover uma disponibilidade enviada enquanto o período de coleta estiver aberto.
6. **RF-PROF-006:** Após o envio de disponibilidade, o sistema deve confirmar o recebimento e exibir os horários registrados.
7. **RF-PROF-007:** O sistema deve permitir que o professor consulte sua grade proposta antes da publicação oficial.
8. **RF-PROF-008:** O sistema deve permitir enviar comentários sobre uma grade proposta, associados à atividade e ao período letivo.
9. **RF-PROF-009:** O sistema deve permitir consultar a grade publicada e indicar sua versão e data de publicação.
10. **RF-PROF-010:** O sistema deve permitir visualizar a própria grade em formato diário e semanal.
11. **RF-PROF-011:** Para cada atividade, o sistema deve exibir disciplina, turma, data, horários, duração, sala e local.
12. **RF-PROF-012:** O sistema deve permitir filtrar a grade própria por período letivo, turma e intervalo de datas.
13. **RF-PROF-013:** O sistema deve permitir exportar a própria grade em formato imprimível ou de calendário, quando habilitado pela instituição.
14. **RF-PROF-014:** O sistema deve notificar o professor quando uma alteração publicada afetar uma de suas atividades.
15. **RF-PROF-015:** A notificação de alteração deve apresentar o horário ou sala anterior, o novo valor e a data de vigência.
16. **RF-PROF-016:** O sistema deve permitir consultar o histórico de alterações das atividades da própria grade.
17. **RF-PROF-017:** O sistema deve permitir abrir uma solicitação de correção ou ajuste associada a uma atividade.
18. **RF-PROF-018:** O sistema deve permitir acompanhar o estado e as respostas de cada solicitação enviada pelo professor.
19. **RF-PROF-019:** O sistema deve permitir que o professor registre uma divergência ou conflito percebido em sua grade.
20. **RF-PROF-020:** O sistema deve restringir a consulta e a alteração de disponibilidades pessoais ao próprio professor e aos papéis institucionais autorizados.

## Gestora de espaços e materiais

1. **RF-REC-001:** O sistema deve permitir cadastrar, consultar, editar e inativar salas e materiais.
2. **RF-REC-002:** O sistema deve permitir identificar cada recurso por código único, nome, tipo e localização.
3. **RF-REC-003:** Para salas, o sistema deve permitir cadastrar capacidade, unidade/localização e características disponíveis.
4. **RF-REC-004:** Para materiais, o sistema deve permitir cadastrar quantidade total, quantidade disponível e unidade de medida.
5. **RF-REC-005:** O sistema deve permitir registrar características de compatibilidade dos materiais e equipamentos.
6. **RF-REC-006:** O sistema deve permitir consultar e alterar o estado operacional do recurso, incluindo disponível, em uso, em manutenção e inativo.
7. **RF-REC-007:** O sistema deve permitir pesquisar recursos por código, nome, tipo, localização e estado operacional.
8. **RF-REC-008:** O sistema deve permitir consultar a agenda de alocações de uma sala ou material em um intervalo de datas.
9. **RF-REC-009:** O sistema deve permitir filtrar a agenda de recursos por período, localização, tipo e disponibilidade.
10. **RF-REC-010:** O sistema deve permitir registrar períodos de manutenção com recurso, responsável, motivo, início e fim.
11. **RF-REC-011:** O sistema deve permitir bloquear um recurso por indisponibilidade operacional, informando motivo e período.
12. **RF-REC-012:** Antes de confirmar um bloqueio, o sistema deve listar as atividades já alocadas que serão afetadas.
13. **RF-REC-013:** O sistema deve permitir confirmar, cancelar ou alterar um bloqueio, mantendo o histórico da operação.
14. **RF-REC-014:** O sistema deve impedir a alocação de sala inativa, bloqueada ou incompatível com os requisitos cadastrados da atividade.
15. **RF-REC-015:** O sistema deve impedir que a quantidade de materiais alocada no mesmo intervalo exceda a quantidade disponível.
16. **RF-REC-016:** O sistema deve verificar a disponibilidade do recurso durante todo o intervalo solicitado, incluindo buffers configurados.
17. **RF-REC-017:** O sistema deve permitir registrar ocorrências de dano, falha ou divergência de inventário vinculadas ao recurso.
18. **RF-REC-018:** O sistema deve permitir atualizar a ocorrência e registrar sua resolução e a data de liberação do recurso.
19. **RF-REC-019:** O sistema deve notificar a coordenação quando uma indisponibilidade afetar atividades alocadas.
20. **RF-REC-020:** O sistema deve manter histórico consultável de alterações, reservas, bloqueios e ocorrências por recurso.

## Analista de qualidade e auditoria

1. **RF-AUD-001:** O sistema deve permitir que o perfil de auditoria consulte grades, recursos e alocações sem permissão para alterá-los.
2. **RF-AUD-002:** O sistema deve permitir consultar as versões de grade em rascunho, aprovadas e publicadas às quais o perfil tem acesso.
3. **RF-AUD-003:** O sistema deve permitir pesquisar alocações por período letivo, turma, professor, sala e material.
4. **RF-AUD-004:** O sistema deve exibir o estado de cada alocação, incluindo rascunho, aprovada, publicada, cancelada ou alterada.
5. **RF-AUD-005:** O sistema deve permitir consultar o resultado da validação de conflitos de uma alocação.
6. **RF-AUD-006:** Para uma validação reprovada, o sistema deve exibir os recursos e intervalos envolvidos no conflito.
7. **RF-AUD-007:** O sistema deve registrar tentativas de operações rejeitadas por conflito ou falta de permissão.
8. **RF-AUD-008:** O sistema deve permitir pesquisar eventos de auditoria por intervalo de datas, usuário, tipo de ação e recurso afetado.
9. **RF-AUD-009:** O sistema deve exibir, para alterações auditadas, os valores anteriores e novos dos campos modificados.
10. **RF-AUD-010:** O sistema deve identificar o usuário responsável e a data/hora de cada alteração registrada.
11. **RF-AUD-011:** O sistema deve registrar e permitir consultar justificativas de exceções autorizadas.
12. **RF-AUD-012:** O sistema deve permitir consultar quem aprovou ou rejeitou uma versão da grade e quando a decisão ocorreu.
13. **RF-AUD-013:** O sistema deve permitir comparar versões de uma grade e identificar cada alocação incluída, removida ou alterada.
14. **RF-AUD-014:** O sistema deve permitir consultar o histórico de bloqueios, manutenções e alterações de estado de cada recurso.
15. **RF-AUD-015:** O sistema deve permitir consultar notificações geradas para mudanças publicadas e seus estados de processamento ou entrega, quando disponíveis.
16. **RF-AUD-016:** O sistema deve permitir gerar relatórios de alocação filtrados por período, turma, professor, sala ou material.
17. **RF-AUD-017:** O sistema deve permitir exportar relatórios e eventos de auditoria em formato estruturado, preservando filtros e período consultado.
18. **RF-AUD-018:** O sistema deve permitir consultar os papéis atribuídos aos usuários e as permissões associadas a cada papel.
19. **RF-AUD-019:** O sistema deve registrar e permitir consultar alterações em contas, papéis e permissões de acesso.
20. **RF-AUD-020:** O sistema deve identificar em relatórios a versão publicada da grade e o período de vigência correspondente.