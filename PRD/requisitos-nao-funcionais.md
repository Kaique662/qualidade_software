# Requisitos Não Funcionais (RNF)

## Objetivo e escopo

Este documento reúne requisitos não funcionais para o sistema proposto de alocação de salas, professores e materiais. Os RNFs descrevem atributos de qualidade e restrições de operação, complementando os comportamentos funcionais de [requisitos-funcionais.md](requisitos-funcionais.md). Estão organizados em requisitos transversais e requisitos específicos para cada persona.

Os perfis são hipóteses ilustrativas, não resultados de pesquisa com usuários. As metas numéricas abaixo são propostas iniciais para orientar testes e avaliação; devem ser confirmadas com a instituição, o volume real de dados e a infraestrutura de implantação antes de serem tratadas como critérios contratuais.

## Perfil de carga inicial para avaliação

Quando um RNF mencionar a carga de referência, usar inicialmente o cenário abaixo em ambiente de homologação e documentar qualquer alteração:

- 25 usuários simultâneos.
- 1.000 atividades no período letivo consultado.
- 100 professores, 50 salas e 200 registros de materiais.
- Teste de carga contínua por 10 minutos após aquecimento de 2 minutos.
- Registrar ambiente, versão da aplicação, configuração do banco, dataset, concorrência, p50, p95, p99 e taxa de erro.
- Repetir cada medição ao menos três vezes; reportar resultados individuais e não apenas a média.

## RNFs transversais

### Segurança e privacidade

1. **RNF-GER-001 - Proteção em trânsito:** Toda comunicação entre navegador, aplicação e serviços integrados deve usar HTTPS com TLS 1.2 ou superior; requisições HTTP devem ser redirecionadas ou rejeitadas.
2. **RNF-GER-002 - Proteção de dados armazenados:** Dados pessoais, credenciais e cópias de segurança devem ter proteção em repouso adequada à infraestrutura escolhida; segredos não podem ser armazenados em texto simples no repositório.
3. **RNF-GER-003 - Acesso mínimo necessário:** Autorização deve ser verificada no servidor em cada operação protegida; por padrão, um usuário sem permissão explícita não pode consultar, criar, editar, exportar ou excluir o objeto.
4. **RNF-GER-004 - Resistência a falhas de segurança comuns:** Entradas devem ser validadas no servidor, erros não podem expor stack traces ou consultas internas, e a versão entregue deve passar por análise automatizada de dependências e por testes de autorização sem vulnerabilidades críticas abertas conhecidas.
5. **RNF-GER-005 - Privacidade:** A aplicação deve coletar e exibir somente dados pessoais necessários ao fluxo; dados reais não devem ser usados em homologação sem autorização, minimização ou anonimização adequada.
6. **RNF-GER-006 - Segredos em logs:** Senhas, tokens, chaves, cookies de sessão e dados pessoais desnecessários não podem aparecer em logs, mensagens de erro, relatórios ou arquivos exportados; verificar com varredura automatizada antes da entrega.

### Rastreabilidade e auditoria

7. **RNF-GER-007 - Cobertura de auditoria:** 100% das operações críticas de criação, alteração, cancelamento, aprovação, publicação, bloqueio e mudança de permissão devem gerar evento de auditoria com agente, data/hora UTC, ação, objeto, resultado e motivo quando aplicável.
8. **RNF-GER-008 - Integridade da auditoria:** Usuários comuns não podem editar ou apagar eventos de auditoria; tentativas de acesso ou alteração não autorizadas também devem ser registradas sem incluir segredos.
9. **RNF-GER-009 - Relógio e correlação:** Eventos devem usar horário UTC sincronizado com fonte confiável e identificador de correlação para relacionar uma operação aos registros técnicos correspondentes.
10. **RNF-GER-010 - Retenção configurável:** O prazo de retenção, arquivamento e descarte de dados e auditoria deve ser configurado conforme a política institucional e requisitos legais aplicáveis; o sistema deve permitir comprovar a política vigente e sua execução.

### Desempenho, confiabilidade e recuperação

11. **RNF-GER-011 - Desempenho de consulta:** Sob o perfil de carga inicial, consultas comuns devem apresentar p95 de até 2 segundos e p99 de até 5 segundos, com taxa de erro inferior a 1% durante o período medido.
12. **RNF-GER-012 - Desempenho de validação:** Sob o perfil de carga inicial, a validação de uma alocação deve apresentar p95 de até 3 segundos e p99 de até 5 segundos; o resultado deve considerar simultaneamente todos os recursos e intervalos associados.
13. **RNF-GER-013 - Consistência sob concorrência:** Em teste com 100 tentativas simultâneas de reservar o mesmo recurso exclusivo no mesmo intervalo, no máximo uma operação incompatível pode ser confirmada; demais tentativas devem receber resposta de conflito sem deixar gravações parciais.
14. **RNF-GER-014 - Integridade transacional:** Uma falha antes da confirmação de uma operação não pode deixar alocação parcialmente gravada nem reduzir a disponibilidade de material sem a reserva correspondente.
15. **RNF-GER-015 - Disponibilidade:** A meta inicial de disponibilidade mensal é de 99,5%, excluindo janelas de manutenção comunicadas previamente. Medir a partir de verificações externas do fluxo de consulta e registrar indisponibilidades e duração.
16. **RNF-GER-016 - Recuperação:** A meta inicial após incidente é RPO de até 15 minutos e RTO de até 4 horas. Demonstrar por exercício documentado de restauração; confirmar se as metas são compatíveis com infraestrutura e custos.

### Usabilidade, acessibilidade e compatibilidade

17. **RNF-GER-017 - Acessibilidade:** Fluxos críticos devem atender WCAG 2.2 nível AA, incluindo uso completo por teclado, foco visível, contraste, rótulos acessíveis, mensagens de erro associadas aos campos e suporte a leitores de tela.
18. **RNF-GER-018 - Idioma e horário:** Interface, mensagens, datas e números devem usar pt-BR; armazenar instantes em UTC e exibi-los no fuso horário configurado para a instituição, sem deslocar incorretamente recorrências ou horários locais.
19. **RNF-GER-019 - Navegadores suportados:** Os fluxos críticos devem funcionar nas duas versões estáveis mais recentes de Chrome, Firefox, Edge e Safari, sem erros que impeçam consulta, cadastro, alocação ou auditoria.
20. **RNF-GER-020 - Observabilidade:** Falhas e operações lentas devem ser diagnosticáveis por logs estruturados e identificador de correlação; registrar duração e resultado sem expor dados pessoais ou segredos.

## RNFs da coordenadora de horários

1. **RNF-COORD-001 - Renderização da grade:** Com a carga inicial, a abertura da grade com até 1.000 atividades deve concluir em p95 de até 3 segundos.
2. **RNF-COORD-002 - Resposta de edição:** Após uma ação de edição, a interface deve exibir confirmação, validação ou estado de processamento em até 1 segundo; a conclusão da gravação deve respeitar os limites de desempenho transversais.
3. **RNF-COORD-003 - Conflitos compreensíveis:** Em teste de usabilidade, pelo menos 4 de 5 coordenadores devem identificar o recurso conflitante e a ação corretiva sugerida em até 60 segundos, sem ajuda do moderador.
4. **RNF-COORD-004 - Proteção contra perda de rascunho:** Em cenários de desconexão ou falha de servidor, o sistema deve preservar a última versão confirmada e deixar explícito se alterações mais recentes ainda não foram salvas; não pode indicar sucesso antes da confirmação do servidor.
5. **RNF-COORD-005 - Edição concorrente segura:** Duas pessoas editando a mesma versão não podem sobrescrever alterações uma da outra silenciosamente; o conflito de versão deve ser apresentado antes da confirmação.
6. **RNF-COORD-006 - Comparação de versões:** A comparação de duas grades de até 1.000 atividades deve apresentar o resultado em p95 de até 5 segundos no perfil de carga inicial.
7. **RNF-COORD-007 - Acessibilidade da grade:** Criar, editar, filtrar e revisar conflitos deve ser possível por teclado, sem depender exclusivamente de arrastar e soltar ou de cor para identificar estados.
8. **RNF-COORD-008 - Consistência de datas:** Em uma suíte com mudanças de fuso, virada de dia, horário de verão aplicável e recorrências, 100% dos horários exibidos devem corresponder ao horário local configurado e ao instante armazenado.
9. **RNF-COORD-009 - Usabilidade do fluxo principal:** Em teste com pelo menos cinco usuários representativos, 4 de 5 devem conseguir criar um rascunho, corrigir um conflito simples e submetê-lo para aprovação em até 15 minutos, sem instrução passo a passo.
10. **RNF-COORD-010 - Estado da publicação:** Rascunho, versão submetida, aprovada e publicada devem ser distinguíveis sem depender somente de cor; estado e versão vigentes devem permanecer visíveis na tela de planejamento.

## RNFs do professor

1. **RNF-PROF-001 - Uso em dispositivos móveis:** Os fluxos de informar disponibilidade e consultar a grade devem funcionar a partir de 360 px de largura, sem rolagem horizontal da página para acessar conteúdo ou ações essenciais.
2. **RNF-PROF-002 - Consulta da grade:** Sob a carga inicial, a consulta da grade pessoal deve atender p95 de até 2 segundos e p99 de até 5 segundos.
3. **RNF-PROF-003 - Envio sem duplicidade:** Repetir o envio de disponibilidade após timeout ou reconexão não pode criar registros duplicados nem exibir confirmação se o servidor não confirmou a gravação.
4. **RNF-PROF-004 - Notificação tempestiva:** Para 99% das alterações publicadas que afetem um professor, a notificação deve ser disponibilizada no canal interno em até 60 segundos; integrações externas devem registrar separadamente envio e confirmação do provedor.
5. **RNF-PROF-005 - Privacidade da disponibilidade:** Em testes de autorização, 100% das tentativas de um professor acessar disponibilidade individual de outro sem privilégio devem ser negadas e registradas sem expor os dados solicitados.
6. **RNF-PROF-006 - Acessibilidade móvel:** Consulta e envio devem funcionar com leitor de tela e ampliação de texto a 200%, sem perda de conteúdo ou de controles essenciais.
7. **RNF-PROF-007 - Legibilidade de alterações:** Em teste de usabilidade, 4 de 5 professores devem identificar o horário anterior, o novo horário e a data de vigência de uma alteração em até 30 segundos.
8. **RNF-PROF-008 - Resiliência de conexão:** Com conexão interrompida durante o envio, a interface deve informar que a operação está pendente ou falhou; somente após confirmação do servidor deve apresentá-la como concluída.
9. **RNF-PROF-009 - Consistência temporal:** Datas e horários da grade devem ser apresentados no fuso da instituição e manter a mesma interpretação entre tela, impressão e calendário exportado.
10. **RNF-PROF-010 - Aprendizado do fluxo:** Em teste com cinco professores representativos, 4 de 5 devem conseguir registrar disponibilidade para uma semana típica em até 3 minutos, sem orientação passo a passo.

## RNFs da gestora de espaços e materiais

1. **RNF-REC-001 - Pesquisa de inventário:** Com até 10.000 registros de recursos, pesquisa por código, nome, tipo e localização deve apresentar p95 de até 2 segundos.
2. **RNF-REC-002 - Atualização de disponibilidade:** Após confirmação de bloqueio, liberação ou mudança de estado, as consultas subsequentes devem refletir o novo estado em até 5 segundos.
3. **RNF-REC-003 - Consistência de reserva:** Testes concorrentes não podem permitir que a soma de materiais reservados exceda a quantidade disponível em qualquer intervalo.
4. **RNF-REC-004 - Integridade de bloqueio:** Em 100% dos casos de teste, a visualização de impacto deve listar todas as atividades sobrepostas ao período do bloqueio antes de sua confirmação.
5. **RNF-REC-005 - Confirmação de operação:** Bloquear, liberar ou alterar a condição de um recurso deve exibir confirmação em até 1 segundo e concluir a persistência dentro dos limites gerais de desempenho.
6. **RNF-REC-006 - Registro rastreável:** 100% das alterações confirmadas de inventário, estado, manutenção e bloqueio devem gerar evento de auditoria com responsável, horário e motivo, quando informado.
7. **RNF-REC-007 - Uso em campo:** As operações essenciais de consulta e registro de ocorrência devem funcionar em tela de 360 px de largura, com controles utilizáveis por toque e sem rolagem horizontal essencial.
8. **RNF-REC-008 - Acessibilidade de estado:** Estado de recurso e disponibilidade devem ser indicados por texto ou ícone com rótulo, nunca apenas por cor.
9. **RNF-REC-009 - Usabilidade do bloqueio:** Em teste com cinco responsáveis por espaços, 4 de 5 devem conseguir bloquear um recurso e identificar as atividades impactadas em até 2 minutos, sem ajuda.
10. **RNF-REC-010 - Recuperação após falha:** Falhas durante atualização de inventário ou bloqueio não podem deixar estado parcialmente salvo; após falha, o sistema deve permitir consultar o estado efetivamente persistido.

## RNFs do analista de qualidade e auditoria

1. **RNF-AUD-001 - Consulta de auditoria:** Pesquisa por usuário, ação, recurso e intervalo de datas deve apresentar p95 de até 3 segundos para até 100.000 eventos no perfil de carga documentado.
2. **RNF-AUD-002 - Exportação íntegra:** Relatórios exportados devem conter exatamente os registros correspondentes aos filtros selecionados; validar total e amostras campo a campo contra a consulta de origem.
3. **RNF-AUD-003 - Proteção de exportação:** Exportações devem respeitar as mesmas permissões da interface, registrar solicitante e horário e não incluir campos sem autorização.
4. **RNF-AUD-004 - Evidência reprodutível:** Cada relatório de avaliação deve identificar versão do sistema, período consultado, filtros, data/hora de geração e parâmetros usados.
5. **RNF-AUD-005 - Completude de auditoria:** Em testes de ponta a ponta, 100% das operações críticas aceitas e rejeitadas devem ter evento correspondente, sem divergência de agente, objeto, ação ou resultado.
6. **RNF-AUD-006 - Precisão temporal:** Eventos devem preservar horário UTC e exibir conversão local sem perda de ordenação; validar eventos simultâneos com precisão mínima de um segundo.
7. **RNF-AUD-007 - Imutabilidade para o auditor:** O perfil de auditoria deve ser somente leitura para os dados avaliados; 100% das tentativas de alterar grade, inventário, permissões ou eventos devem ser negadas.
8. **RNF-AUD-008 - Rastreabilidade de avaliação:** 100% dos requisitos classificados como críticos devem estar ligados a pelo menos um caso de teste e a uma evidência identificável antes da avaliação final.
9. **RNF-AUD-009 - Repetibilidade de desempenho:** Executando o mesmo teste de carga três vezes no mesmo ambiente e dataset, a variação do p95 não deve exceder 10%; qualquer variação maior deve ser investigada e reportada.
10. **RNF-AUD-010 - Proteção de evidências:** Documentos e relatórios exportados devem omitir ou anonimizar dados pessoais não necessários e não podem conter credenciais, tokens ou segredos, verificados por varredura automatizada.

## Como validar e manter os RNFs

1. Confirmar as metas propostas com usuários, responsáveis pela infraestrutura e avaliadores do projeto.
2. Converter cada RNF aprovado em cenário de teste, método de medição, ambiente e resultado esperado.
3. Versionar resultados de carga, acessibilidade, segurança e recuperação junto à versão da aplicação avaliada.
4. Registrar desvios, limitações e riscos aceitos; não declarar conformidade com base em testes não executados.
5. Revisar os RNFs quando o volume de dados, os papéis, a infraestrutura ou as políticas institucionais mudarem.