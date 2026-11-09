# Persona 01: Ana Paula Ribeiro, coordenadora de horários

## Contexto do documento

Esta persona representa a pessoa responsável por planejar e publicar horários acadêmicos em uma aplicação que aloca salas, professores e materiais sem conflitos. É um perfil fictício para orientar a descoberta e a avaliação do produto. Nome, idade e situações são ilustrativos; não representam pesquisa já realizada. Validar as hipóteses com entrevistas e observação antes de tratá-las como evidência sobre usuários reais.

## Perfil

- **Idade ilustrativa:** 42 anos.
- **Cargo:** Coordenadora acadêmica responsável pela montagem dos horários.
- **Experiência:** 12 anos em gestão acadêmica; 6 anos organizando grades.
- **Instituição:** Escola ou faculdade de médio porte, com vários cursos, turmas e períodos letivos.
- **Responsabilidades:** Consolidar demandas, compatibilizar disponibilidade, resolver conflitos, coordenar aprovações e publicar horários.
- **Dispositivos e ambiente:** Computador no escritório, planilhas e sistemas administrativos; celular para acompanhar mensagens quando está fora.
- **Familiaridade digital:** Intermediária. Domina planilhas, mas não quer depender de fórmulas ou configurações técnicas.
- **Condições de uso:** Trabalha com prazos e interrupções. Precisa retomar o planejamento sem perder alterações e identificar erros sem interpretar mensagens técnicas.

## Frase representativa

> “Preciso fechar uma grade que funcione para todos e conseguir explicar por que cada decisão foi tomada.”

## Objetivos

- Montar, revisar e publicar a grade do período dentro do prazo.
- Considerar simultaneamente disponibilidade de professores, salas adequadas e quantidade de materiais necessários.
- Detectar conflitos antes da publicação e entender quais recursos e intervalos estão envolvidos.
- Comparar alternativas sem perder a versão vigente.
- Fazer ajustes emergenciais com o menor impacto possível e identificar quem será afetado.
- Explicar à direção como uma alocação ou exceção foi decidida e aprovada.

## Jornada ponta a ponta

1. Seleciona o período letivo e importa ou cadastra turmas, atividades, professores, salas e materiais.
2. Confere dados incompletos, capacidades, indisponibilidades, restrições e recursos obrigatórios.
3. Define ou revisa duração, recorrência, intervalos, prioridades e necessidades de cada atividade.
4. Solicita sugestões de alocação ou atribui recursos manualmente.
5. Analisa conflitos e alternativas; corrige dados ou negocia mudanças com professores e responsáveis pelos espaços.
6. Salva versões de trabalho, compara alterações e resolve conflitos remanescentes.
7. Submete a grade para aprovação e registra justificativas para exceções permitidas pela política institucional.
8. Publica a versão aprovada e consulta seu histórico.
9. Diante de uma mudança posterior, revisa o impacto, registra o motivo, confirma a alteração e comunica as pessoas afetadas.

## Dores e riscos

- Planilhas paralelas permitem alterações concorrentes e deixam dúvidas sobre qual versão é oficial.
- Uma sala pode estar livre, mas ser inadequada por capacidade, acessibilidade, laboratório ou equipamento.
- A disponibilidade de um professor pode mudar durante o planejamento.
- Um material compartilhado pode ser requisitado por mais de uma turma no mesmo intervalo.
- Recorrências, durações diferentes, intervalos adjacentes e edições simultâneas podem esconder conflitos.
- Uma mudança tardia pode afetar aulas já divulgadas e passar despercebida por parte do público.
- Erros genéricos não indicam o que corrigir e aumentam o retrabalho.

## Necessidades do produto

- Visão consolidada e filtrável por período, turma, professor, sala e material.
- Validação de conflito ao alocar, salvar e publicar, considerando o intervalo completo da atividade.
- Regras explícitas para sobreposição, recorrência, fuso horário e buffers de preparação ou deslocamento, quando aplicáveis.
- Mensagem acionável para cada conflito: recurso, atividades envolvidas, horários e possíveis correções.
- Rascunho, aprovação e publicação claramente diferenciados.
- Histórico de versões com autor, data/hora, motivo e valores anteriores e novos.
- Proteção contra sobrescrita concorrente; erro no salvamento não pode deixar alterações parciais silenciosas.
- Permissões por papel e aprovação para publicação ou exceções, conforme política institucional.
- Antes de confirmar uma alteração publicada, mostrar os impactos conhecidos e permitir revisar quem será afetado.

## Critérios de sucesso

- Consegue preparar e revisar uma grade sem manter uma planilha paralela como fonte oficial.
- Conflitos conhecidos de professor, sala ou material são bloqueados ou claramente sinalizados antes da publicação.
- Uma exceção autorizada tem responsável, justificativa e registro consultável.
- Uma mudança publicada mantém histórico e permite identificar os afetados.
- Consegue entender e resolver uma mensagem de validação sem suporte técnico.

## Evidências para avaliação

- Testes unitários e de integração para sobreposição parcial, intervalos adjacentes, duração, recorrência, indisponibilidade e alterações concorrentes.
- Testes de aceitação para os fluxos de rascunho, aprovação e publicação, incluindo cenários negativos.
- Teste concorrente que demonstre que duas solicitações não confirmam alocações incompatíveis para o mesmo recurso.
- Amostra de trilha de auditoria contendo agente, horário, ação, objeto, resultado e justificativa, sem segredos ou dados desnecessários.
- Teste de falha de gravação que confirme ausência de estado parcialmente atualizado.
- Relatório de desempenho com volume de atividades e recursos, concorrência, taxa de erro e latências p50/p95/p99.

## Hipóteses a validar

- A coordenação prefere sugestões automáticas, edição manual ou uma combinação das duas?
- Quem pode aprovar a grade e autorizar exceções? Quais justificativas são exigidas?
- Qual é o volume real de turmas, atividades, salas e materiais por período?
- Quais canais e prazos são esperados para comunicar alterações?
- Quais metas de latência são aceitáveis no ambiente real da instituição?