# Persona 03: Renata Costa Nascimento, gestora de espaços e materiais

## Contexto do documento

Esta persona representa a pessoa responsável por salas, laboratórios e materiais compartilhados. É um perfil fictício para orientar a descoberta e a avaliação do produto proposto. Nome, idade e situações são ilustrativos, não representam pesquisa já realizada. Validar com as equipes responsáveis pelos espaços e inventário.

## Perfil

- **Idade ilustrativa:** 48 anos.
- **Cargo:** Gestora de espaços, equipamentos audiovisuais e materiais acadêmicos.
- **Experiência:** 15 anos em operações acadêmicas; conhece limitações físicas, condições e histórico de manutenção dos recursos.
- **Contexto de trabalho:** Coordena reservas, configuração de salas, empréstimos, inventário e indisponibilidades para manutenção.
- **Dispositivos e ambiente:** Computador para administrar inventário; celular durante inspeções e movimentação de equipamentos.
- **Familiaridade digital:** Intermediária; costuma trabalhar com inventários, agendas e planilhas.
- **Condições de uso:** Precisa registrar ocorrências em campo e pesquisar por código, nome, localização, tipo ou disponibilidade.

## Frase representativa

> “O sistema precisa conhecer a condição real do recurso, não apenas mostrar um horário vazio.”

## Objetivos

- Manter inventário confiável de salas e materiais, com capacidade, localização, características e condição.
- Bloquear recursos em manutenção ou indisponíveis por razões operacionais.
- Confirmar que cada atividade terá os recursos necessários durante todo o intervalo.
- Evitar dupla reserva, incompatibilidade e falta de tempo para preparação ou transporte.
- Planejar manutenção vendo as alocações que serão afetadas.
- Localizar rapidamente quem reservou um recurso, para qual atividade e em qual período.

## Jornada ponta a ponta

1. Cadastra ou atualiza salas e materiais com identificadores, localização, capacidade, características, quantidade e estado operacional.
2. Registra restrições de uso, acessibilidade, manutenção, montagem e deslocamento, quando aplicáveis.
3. Revisa solicitações e confirma compatibilidade entre atividade e recurso.
4. Reserva, libera ou bloqueia recursos, registrando motivo e período de vigência.
5. Consulta a agenda por recurso antes de autorizar manutenção ou movimentação.
6. Registra falha, dano, indisponibilidade ou divergência de inventário e analisa as alocações afetadas.
7. Acompanha a resolução, libera o recurso e preserva o histórico da ocorrência.

## Dores e riscos

- Uma sala livre pode ser inadequada por capacidade, acessibilidade ou configuração.
- Recursos em unidades diferentes podem não ser transportáveis entre atividades consecutivas.
- Equipamentos em quantidade limitada podem ser alocados simultaneamente em excesso.
- Inventário desatualizado leva a reservas impossíveis de cumprir.
- Bloqueios sem motivo, prazo ou responsável podem retirar recursos da grade indefinidamente.
- Uma indisponibilidade registrada após a publicação pode deixar uma atividade sem alternativa.
- Uma mudança de estado do recurso pode afetar várias atividades, exigindo comunicação e remanejamento.

## Necessidades do produto

- Cadastro validado e estado operacional separado da mera existência no inventário.
- Regras de compatibilidade entre atividade, sala e material, incluindo capacidade, características e quantidade.
- Agenda por recurso e visão de impacto antes de confirmar um bloqueio.
- Bloqueios com responsável, motivo, início, fim e revisão ou encerramento.
- Verificação de disponibilidade durante todo o intervalo, considerando buffers e deslocamento configurados.
- Histórico de reservas, manutenções, alterações, bloqueios e responsáveis.
- Pesquisa por identificador, nome, local, tipo e estado.
- Permissões distintas para consultar, editar inventário, bloquear e liberar recursos.
- Alertas sobre alocações afetadas quando um recurso é marcado como indisponível.

## Critérios de sucesso

- O sistema não confirma alocação que ultrapasse capacidade ou use recurso incompatível.
- Manutenção planejada mostra as atividades afetadas antes de confirmar o bloqueio.
- É possível identificar condição, localização e próxima utilização de cada recurso.
- Recursos com quantidade limitada não são reservados além da disponibilidade.
- Bloqueios registram motivo, período, responsável e histórico de resolução.

## Evidências para avaliação

- Testes de capacidade, características obrigatórias, quantidade, intervalos, buffers, localização e bloqueios.
- Testes de consistência para duas solicitações concorrentes pelo mesmo recurso.
- Teste de impacto confirmando que uma indisponibilidade não remove ou altera alocações silenciosamente.
- Relatório de inventário e reservas incompatíveis, com dados de teste rastreáveis.
- Trilha de auditoria para criação, edição, bloqueio e liberação, com motivo e valores anteriores/novos.
- Testes de autorização para operações de inventário e administração de recursos.
- Teste de desempenho para consultas de agenda por recurso em volume representativo, com carga e percentis documentados.

## Hipóteses a validar

- Quais características de sala são obrigatórias para cada tipo de atividade?
- Materiais são unidades individualmente identificadas ou itens fungíveis contabilizados por quantidade?
- Quais recursos exigem tempo de montagem, limpeza, preparação ou deslocamento?
- Quem pode registrar manutenção, bloquear recurso e liberar uma reserva?
- Como inventário e condição real são atualizados e com que frequência?