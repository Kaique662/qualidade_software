# Plano TDD - Alocação de salas, professores e materiais

## Objetivo e convenções

Este documento inventaria os requisitos encontrados para preparar os ciclos de TDD. Não contém implementação de testes nem código de produção. Cada regra mantém o identificador da fonte; os casos descrevem o comportamento esperado somente quando o artefato o define.

- **RF**: requisito funcional do PRD, consolidado a partir dos mesmos IDs nas personas.
- **RNF**: critério de qualidade ou operação, separado das regras funcionais. O PRD classifica suas metas numéricas como propostas iniciais ainda sujeitas a validação.
- **C-ARQ-001**: restrição local registrada na arquitetura, cuja própria fonte informa que não foi localizada nos artefatos do repositório.
- **Pendente**: a fonte não define o resultado esperado; não transformar o caso em critério pass/fail até decisão do responsável.
- **N/A**: a dimensão não se aplica ao comportamento descrito. Não significa uma regra adicional.

## Inventário de regras funcionais

Em cada linha, os cinco campos cobrem caminho feliz, limite, entrada inválida, conflito e estado proibido. Quando o requisito não define um desses resultados, isso está marcado como **Pendente**; não se presume validação, política de precedência ou transição de estado.

### Coordenadora de horários

| Origem | Caminho feliz | Limite | Entrada inválida | Conflito | Estado proibido |
| --- | --- | --- | --- | --- | --- |
| `RF-COORD-001` | Criar período com início e fim e consultá-lo depois. | Início igual ao fim ou consulta na data extrema: **Pendente** (inclusividade não definida). | Datas ausentes/invertidas: **Pendente**. | Períodos sobrepostos ou duplicados: **Pendente**. | **N/A**. |
| `RF-COORD-002` | Cadastrar e editar curso, turma, disciplina e atividade vinculados a um período. | Um vínculo por entidade e vários vínculos: **Pendente** para cardinalidade. | Entidade ou período inexistente, campos ausentes: **Pendente**. | Duplicidade de cadastro ou vínculo: **Pendente**. | Atividade sem vínculo de período não satisfaz o requisito; rejeição formal: **Pendente**. |
| `RF-COORD-003` | Associar um ou mais professores conforme regras institucionais. | Um professor (mínimo explícito) e mais de um. | Zero professores contraria “um ou mais”; resultado de gravação esperado: impedir associação vazia. | Professor indisponível ou já alocado: aplicar validação de `RF-COORD-009`; política institucional adicional: **Pendente**. | Atividade sem nenhum professor associado. |
| `RF-COORD-004` | Definir data, início, fim, duração e recorrência. | Recorrência na primeira/última data e duração mínima/máxima: **Pendente**. | Campo ausente, duração zero/negativa, fim antes do início: **Pendente** (validação não descrita). | Ocorrência recorrente coincidente com outra alocação: validar conflito; regra de expansão da recorrência: **Pendente**. | **N/A**. |
| `RF-COORD-005` | Registrar disponibilidade e indisponibilidade de professor para o período selecionado. | Intervalo nas bordas do período: **Pendente**. | Intervalo fora do período ou invertido: **Pendente**. | Disponibilidade sobreposta à indisponibilidade: precedência/resultado **Pendente**. | **N/A**. |
| `RF-COORD-006` | Selecionar sala que atenda capacidade mínima e características exigidas. | Capacidade exatamente igual ao mínimo deve ser compatível; imediatamente abaixo não atende. | Sala inexistente ou requisito sem correspondência: não selecionar sala incompatível. | Sala ocupada/bloqueada no intervalo: validar com `RF-COORD-009` e `RF-REC-014`/`016`. | Alocação em sala abaixo da capacidade mínima ou sem característica exigida. |
| `RF-COORD-007` | Associar material e informar quantidade necessária. | Quantidade exatamente disponível versus uma unidade acima: a segunda deve ser recusada por `RF-REC-015`. | Quantidade ausente, zero ou negativa: **Pendente**. | Reserva concomitante que exceda disponibilidade: impedir excesso (`RF-REC-015`). | Alocação acima da quantidade disponível. |
| `RF-COORD-008` | Pesquisar salas e materiais disponíveis para intervalo informado. | Intervalo que coincide com início/fim de uma reserva: inclusividade temporal **Pendente**; considerar buffer quando configurado (`RF-REC-016`). | Intervalo vazio/invertido: **Pendente**. | Recurso ocupado em parte do intervalo não deve ser apresentado como disponível para todo ele (`RF-REC-016`). | **N/A**. |
| `RF-COORD-009` | Salvar alocação e verificar conflitos de professor, sala e materiais. | Intervalos adjacentes e recursos no limite de quantidade: resultado temporal segue `RF-COORD-010` e quantitativo `RF-REC-015`. | Entrada conflitante deve ser identificada; consequência sobre a gravação: **Pendente**. | Colisão de cada tipo de recurso, isolada e simultânea; identificar conflito (`RF-COORD-012`). | **Pendente**: o requisito exige verificação, mas não define se toda colisão impede salvar ou admite exceção. |
| `RF-COORD-010` | Verificar intervalo sem sobreposição. | Início ou fim dentro de outro intervalo deve ser considerado sobreposição; contato exato entre endpoints: **Pendente**. | Intervalo invertido/vazio: **Pendente**. | Sobreposição parcial, total e intervalo contido em outro deve ser detectada. | **Pendente**: a regra define detecção, não a consequência de salvar. |
| `RF-COORD-011` | Considerar tempos configurados de preparação, limpeza ou deslocamento ao verificar recursos. | Buffer zero e conflito exatamente no limite configurado; igualdade/inclusão do endpoint: **Pendente**. | Valor de buffer negativo ou malformado: **Pendente**. | Alocações sem sobreposição nominal, mas conflitantes após buffer, devem ser consideradas na verificação. | Ignorar buffer configurado; consequência de detectar conflito com buffer: **Pendente**. |
| `RF-COORD-012` | Ao detectar conflito, identificar recurso, atividades e intervalos envolvidos. | Intervalos parcialmente/totalmente contidos devem mostrar intervalo conflitante correspondente; formato exato: **Pendente**. | **N/A**. | Uma ou várias colisões devem identificar cada recurso/atividade/intervalo aplicável; completude além do singular não está explicitada. | Retornar conflito sem identificar os dados requeridos. |
| `RF-COORD-013` | Para alocação inválida, apresentar alternativas válidas quando existirem. | Nenhuma alternativa disponível: não alegar que há opção; mensagem/estado: **Pendente**. | Opção que viole regras cadastradas deve ser excluída. | Alternativas também colidem com recurso/atividade: não as apresentar como válidas. | Sugerir horário/recurso que viole regra cadastrada. |
| `RF-COORD-014` | Criar e editar alocação manualmente na grade. | Grade vazia e última posição/intervalo do período: **Pendente**. | Dados incompletos: validação esperada **Pendente**; aplicar regras de alocação quando definidas. | Edição que cause colisão deve passar por `RF-COORD-009`. | **N/A**. |
| `RF-COORD-015` | Solicitar proposta automática sem publicar automaticamente alterações. | Sem atividades elegíveis ou sem resultado: comportamento **Pendente**. | Entrada incompleta para proposta: **Pendente**. | Proposta conflitante deve ser revalidada conforme `RF-COORD-009`; resolução do solver **Pendente**. | Publicar alteração automaticamente; o requisito proíbe esse efeito. |
| `RF-COORD-016` | Salvar grade como rascunho e mantê-la fora do horário oficial. | Rascunho vazio/sem alterações: **Pendente**. | Conteúdo inválido: aplicar validações definidas; possibilidade de rascunho inválido **Pendente**. | Rascunho concorrente: versão obsoleta sob `RF-COORD-017`. | Tornar rascunho visível como horário oficial. |
| `RF-COORD-017` | Salvar versão atual após consulta sem alteração concorrente. | Duas edições na mesma versão; detectar versão desatualizada e pedir revisão antes de sobrescrever. | Versão ausente/inválida: **Pendente**. | Alteração feita por outra pessoa desde a última consulta deve ser detectada. | Sobrescrever silenciosamente versão desatualizada. |
| `RF-COORD-018` | Comparar duas versões e listar inclusões, remoções e alterações de alocação. | Versões iguais: lista vazia é expectativa de comparação; representação não especificada. | Versão inexistente/inacessível: **Pendente**. | Mudanças simultâneas em uma alocação devem aparecer como alteração, não ser omitidas. | Omitir inclusão, remoção ou alteração existente. |
| `RF-COORD-019` | Submeter versão e registrar resultado e responsável pela decisão. | Aprovação e rejeição devem ter resultado e responsável registrados; datas/campos adicionais **Pendente**. | Versão inexistente ou não elegível: **Pendente**. | Decisões concorrentes para a mesma versão: precedência **Pendente**. | **Pendente**: estados autorizados para submissão e transições não definidos. |
| `RF-COORD-020` | Publicar versão aprovada e consultar versões publicadas anteriores. | Publicação sucessiva deve preservar consulta da anterior; quantidade/retenção **Pendente**. | Versão não aprovada não pode ser publicada. | Duas publicações concorrentes/vigências sobrepostas: **Pendente**. | Publicar versão não aprovada; apagar/inacessibilizar histórico publicado contraria consulta de versões anteriores. |

### Professor

| Origem | Caminho feliz | Limite | Entrada inválida | Conflito | Estado proibido |
| --- | --- | --- | --- | --- | --- |
| `RF-PROF-001` | Consultar vínculo, disciplinas, turmas e período associados ao próprio professor. | Nenhum vínculo: conteúdo esperado **Pendente**. | Identificador de outro professor/inexistente: autorização e resposta **Pendente**, observada a restrição de `RF-PROF-020`. | Vínculos alterados durante consulta: consistência **Pendente**. | Expor dados pessoais de outro professor sem autorização (`RF-PROF-020`). |
| `RF-PROF-002` | Informar intervalos disponíveis para atividades. | Intervalo na borda do período: **Pendente**. | Intervalo invertido/vazio ou fora do período: **Pendente**. | Sobreposição com indisponibilidade/restrição: tratamento **Pendente**; verificar `RF-PROF-004`. | **N/A**. |
| `RF-PROF-003` | Informar indisponibilidade recorrente com datas de início e fim. | Recorrência no primeiro/último dia: **Pendente**. | Datas ausentes ou início posterior ao fim: **Pendente**. | Recorrência coincidente com atividade/horário disponível: impacto/precedência **Pendente**. | **N/A**. |
| `RF-PROF-004` | Classificar cada horário como restrição obrigatória ou preferência negociável. | Testar cada uma das duas classes; outras classes não estão definidas. | Classe ausente/desconhecida: **Pendente**. | Preferência e restrição obrigatória incompatíveis: regra de precedência **Pendente**. | Tratar preferência como restrição obrigatória ou vice-versa sem regra definida. |
| `RF-PROF-005` | Editar/remover disponibilidade enviada enquanto coleta está aberta. | Último instante antes do fechamento: deve permitir; após fechamento, permissão **Pendente** (restrição temporal sugere testar bloqueio, mas não define exceções). | Disponibilidade inexistente ou de outro professor: resposta **Pendente**, aplicar autorização de `RF-PROF-020`. | Edição concorrente da mesma disponibilidade: **Pendente**. | Permitir edição após encerramento sem política explícita; resultado permanece **Pendente**. |
| `RF-PROF-006` | Após envio confirmado, confirmar recebimento e exibir horários registrados. | Envio de zero ou muitos intervalos: aceitação **Pendente**. | Envio rejeitado/incompleto: não afirmar recebimento confirmado; detalhamento **Pendente**. | Reenvio após timeout: consultar `RNF-PROF-003` para duplicidade/confirmar apenas após gravação. | Exibir confirmação de recebimento sem envio/gravação confirmada. |
| `RF-PROF-007` | Consultar grade proposta antes de publicação oficial. | Proposta vazia: apresentação **Pendente**. | Grade inexistente/não autorizada: resposta **Pendente**. | Proposta alterada durante consulta: versão/consistência **Pendente**. | Apresentar proposta como publicação oficial. |
| `RF-PROF-008` | Enviar comentário associado a atividade e período letivo. | Comentário mínimo/máximo ou vazio: limites **Pendente**. | Atividade/período inexistente ou sem vínculo: **Pendente**. | Múltiplos comentários concorrentes: ordenação/edição **Pendente**. | Comentário sem associação a atividade e período contraria o vínculo exigido. |
| `RF-PROF-009` | Consultar grade publicada com versão e data de publicação. | Sem versão publicada: estado exibido **Pendente**. | Versão inexistente: **Pendente**. | Publicações sucessivas: exibir a versão vigente e data correspondentes; regra de vigência detalhada **Pendente**. | Apresentar rascunho/não publicada como grade publicada. |
| `RF-PROF-010` | Visualizar a própria grade em formato diário e semanal. | Dia/semana sem atividades: apresentação **Pendente**. | Período inválido: **Pendente**. | Atividade que cruza limite entre dias/semanas ou fuso: interpretação **Pendente**, observar RNFs temporais. | Exibir grade alheia sem autorização (`RF-PROF-020`). |
| `RF-PROF-011` | Cada atividade mostra disciplina, turma, data, horários, duração, sala e local. | Campo opcional/ausente: fallback **Pendente**. | Dados ausentes/inconsistentes: **Pendente**. | Mudança de sala/horário: exibir valores da versão vigente; histórico em `RF-PROF-016`. | Omitir os dados expressamente requeridos quando disponíveis. |
| `RF-PROF-012` | Filtrar a própria grade por período, turma e intervalo de datas. | Datas nos limites do intervalo e período; inclusão das pontas **Pendente**. | Filtro de data invertido ou turma inexistente: **Pendente**. | Filtros combinados sem resultados: retorno vazio; sem regra de precedência adicional. | Retornar atividades fora dos filtros selecionados. |
| `RF-PROF-013` | Exportar em formato imprimível ou calendário quando habilitado pela instituição. | Grade vazia e maior período: **Pendente**. | Filtro/intervalo inválido: **Pendente**. | Exportação deve corresponder à própria grade filtrada; divergências e campos seguem `RNF-AUD-002`/`003` quando aplicáveis. | Disponibilizar exportação quando desabilitada; exportar grade de outra pessoa sem autorização. |
| `RF-PROF-014` | Notificar professor afetado por alteração publicada em atividade própria. | Alteração sem impacto: não deve gerar notificação para esse professor; política de duplicidade **Pendente**. | Publicação não confirmada: não notificar como alteração publicada (coerente com o gatilho explícito). | Várias alterações/afetação a vários professores: deduplicação e destinatários **Pendente**. | Notificar antes da publicação confirmada como se a alteração estivesse publicada. |
| `RF-PROF-015` | Notificação mostra valor anterior, novo valor e data de vigência. | Alteração só de horário ou só de sala: mostrar valor anterior/novo correspondente e vigência. | Campo anterior ausente: fallback **Pendente**. | Alterações sucessivas: associação entre par anterior/novo e vigência **Pendente**. | Omitir valor anterior, novo valor ou data de vigência. |
| `RF-PROF-016` | Consultar histórico de alterações das atividades da própria grade. | Atividade sem alteração: histórico vazio/estado **Pendente**. | Atividade alheia ou inexistente: autorização **Pendente**, observar `RF-PROF-020`. | Múltiplas alterações: ordenação/limite **Pendente**. | Expor histórico de grade alheia sem autorização. |
| `RF-PROF-017` | Abrir solicitação de correção/ajuste associada a atividade. | Solicitação mínima/vazia ou várias solicitações: **Pendente**. | Atividade inexistente/não associada ao professor: **Pendente**. | Duas solicitações equivalentes: duplicidade/mesclagem **Pendente**. | Solicitação sem atividade associada contraria o requisito. |
| `RF-PROF-018` | Acompanhar estado e respostas de cada solicitação enviada. | Solicitação ainda sem resposta: exibir estado; estados possíveis **Pendente**. | Solicitação inexistente/alheia: acesso **Pendente**. | Respostas/atualizações simultâneas: ordenação/precedência **Pendente**. | Omitir estado ou respostas existentes da própria solicitação. |
| `RF-PROF-019` | Registrar divergência/conflito percebido na própria grade. | Sem detalhes ou múltiplas divergências: campos/limites **Pendente**. | Atividade/grade não associada: **Pendente**. | Divergência já reportada ou incompatível com outra: deduplicação **Pendente**. | Registrar como divergência de grade sem referência a grade/atividade: **Pendente** (vínculo não especificado). |
| `RF-PROF-020` | Professor consulta/altera somente disponibilidade própria; papel institucional autorizado também pode fazê-lo. | Testar usuário próprio e cada papel autorizado existente; catálogo de papéis **Pendente**. | Usuário sem autenticação/identidade não resolvida: comportamento **Pendente**. | Tentativa de consultar/alterar disponibilidade de terceiro sem privilégio deve ser restringida. | Acesso ou alteração por professor sobre dados de outro sem autorização. |

### Gestora de espaços e materiais

| Origem | Caminho feliz | Limite | Entrada inválida | Conflito | Estado proibido |
| --- | --- | --- | --- | --- | --- |
| `RF-REC-001` | Cadastrar, consultar, editar e inativar sala/material. | Recurso sem reservas e com reservas: efeitos da inativação **Pendente**. | Campos obrigatórios inválidos: **Pendente**. | Código duplicado: observar unicidade em `RF-REC-002`; reservas durante edição: impacto **Pendente**. | Alocar recurso inativo (`RF-REC-014`). |
| `RF-REC-002` | Identificar recurso por código único, nome, tipo e localização. | Código mínimo/máximo: **Pendente**. | Identificação ausente: **Pendente**. | Repetir código deve ser impedido por unicidade explícita. | Dois recursos ativos com mesmo código identificador. |
| `RF-REC-003` | Cadastrar sala com capacidade, unidade/localização e características. | Capacidade mínima/máxima: **Pendente**. | Capacidade negativa, zero ou campos ausentes: **Pendente**. | Duas salas com características/capacidade iguais: **N/A** (não há unicidade exigida para esses atributos). | **N/A**. |
| `RF-REC-004` | Cadastrar material com total, disponível e unidade de medida. | Disponível igual ao total e zero disponível: testar; sem atividade, resultado válido não é explicitado. | Disponível maior que total, valor negativo ou unidade ausente: **Pendente**; relação total/disponível não foi definida. | Alteração de estoque enquanto há reservas: política de ajuste **Pendente**. | Exceder quantidade disponível em alocação (`RF-REC-015`). |
| `RF-REC-005` | Registrar características de compatibilidade de materiais/equipamentos. | Nenhuma/várias características: cardinalidade **Pendente**. | Característica ausente/desconhecida: **Pendente**. | Uso dessas características na validação de alocação: **Pendente**; o requisito só exige cadastrá-las. | **N/A**: não há proibição explícita de alocação de material incompatível neste RF. |
| `RF-REC-006` | Consultar e alterar estado operacional entre disponível, em uso, manutenção e inativo. | Transição entre cada estado listado; matriz de transição **Pendente**. | Estado desconhecido: conjunto de estados descrito limita valores; rejeição/resposta detalhada **Pendente**. | Alterar estado com reservas ativas: impacto e permissão **Pendente**. | Alocação de sala inativa/bloqueada é vedada por `RF-REC-014`; efeito de manutenção ou “em uso” na alocação: **Pendente**. |
| `RF-REC-007` | Pesquisar por código, nome, tipo, localização e estado. | Busca sem resultado/múltiplos resultados: retorno vazio/lista, paginação **Pendente**. | Critério inválido: **Pendente**. | Critérios combinados: semântica AND/OR **Pendente**. | **N/A**. |
| `RF-REC-008` | Consultar agenda de sala/material no intervalo solicitado. | Inclusividade das datas inicial/final: **Pendente**. | Intervalo inválido: **Pendente**. | Reserva que cruza parcialmente o intervalo deve aparecer se sobreposta; regra detalhada de endpoint em `RF-COORD-010` segue pendente. | **N/A**. |
| `RF-REC-009` | Filtrar agenda por período, localização, tipo e disponibilidade. | Cada filtro isolado e combinação; sem resultados retorna vazio. | Valor/filtro desconhecido: **Pendente**. | Combinação de filtros e disponibilidade momentânea: composição **Pendente**. | Retornar registro fora dos filtros selecionados. |
| `RF-REC-010` | Registrar manutenção com recurso, responsável, motivo, início e fim. | Manutenção mínima/máxima e períodos adjacentes: **Pendente**. | Recurso/responsável ausente, data invertida: **Pendente**. | Sobreposição com reservas/manutenções: impacto e confirmação **Pendente**, observar `RF-REC-012`. | **Pendente**: o requisito não afirma se manutenção bloqueia automaticamente alocação; relação com disponibilidade requer decisão. |
| `RF-REC-011` | Bloquear por indisponibilidade operacional com motivo e período. | Início/fim em bordas e período mínimo: **Pendente**. | Motivo/período ausente ou invertido: **Pendente**. | Reservas afetadas devem ser listadas antes de confirmar (`RF-REC-012`). | Confirmar sem apresentar impacto listado quando existem atividades afetadas. |
| `RF-REC-012` | Antes de confirmar bloqueio, listar atividades já alocadas que serão afetadas. | Nenhuma atividade afetada: lista vazia; várias atividades: listar todas (`RNF-REC-004`). | Bloqueio/intervalo inválido: **Pendente**. | Sobreposição parcial ou com buffer: inclusão exata segue `RF-REC-016`; ainda não fechada. | Confirmar bloqueio antes de exibir atividades impactadas. |
| `RF-REC-013` | Confirmar, cancelar ou alterar bloqueio, preservando histórico. | Cada ação sobre bloqueio existente; transições detalhadas **Pendente**. | Bloqueio inexistente ou ação repetida: **Pendente**. | Alteração concorrente do mesmo bloqueio: **Pendente**. | Perder histórico da operação; transição inválida não tem matriz definida. |
| `RF-REC-014` | Impedir alocação de sala inativa, bloqueada ou incompatível. | Testar cada condição isolada e em combinação; compatibilidade no limite de capacidade. | Identificador de sala inexistente: **Pendente**. | Conflito temporal aplica `RF-COORD-009`/`010` e buffers `RF-REC-016`. | Confirmar qualquer alocação da sala nas condições explicitamente proibidas. |
| `RF-REC-015` | Permitir quantidade cuja soma no intervalo não exceda disponível. | Exatamente disponível: permitir; disponível + 1: impedir. | Quantidade zero/negativa ou maior que total cadastrado: **Pendente**. | Reservas simultâneas não podem tornar soma superior à disponível (`RNF-REC-003`, `RNF-GER-013`). | Confirmar alocação que exceda disponibilidade. |
| `RF-REC-016` | Verificar disponibilidade durante todo intervalo solicitado, incluindo buffers configurados. | Disponibilidade até o limite e buffer exatamente coincidente: inclusividade **Pendente**. | Intervalo/buffer inválido: **Pendente**. | Ocupação em qualquer trecho do intervalo/buffer torna o recurso indisponível para o trecho solicitado. | Aceitar recurso indisponível em parte do intervalo considerado. |
| `RF-REC-017` | Registrar ocorrência de dano, falha ou divergência de inventário vinculada ao recurso. | Sem detalhe/mais de uma ocorrência: campos e cardinalidade **Pendente**. | Recurso inexistente ou tipo fora dos três descritos: **Pendente**. | Ocorrência com recurso já bloqueado/em manutenção: efeito **Pendente**. | Ocorrência sem vínculo a recurso contraria o requisito. |
| `RF-REC-018` | Atualizar ocorrência e registrar resolução e data de liberação do recurso. | Ocorrência resolvida com liberação imediata ou posterior: relação temporal **Pendente**. | Data de liberação inválida/anterior à ocorrência: **Pendente**. | Atualização concorrente/ocorrência já resolvida: **Pendente**. | Marcar resolução sem registrar resolução e data de liberação, ambos exigidos. |
| `RF-REC-019` | Notificar coordenação quando indisponibilidade afetar atividades alocadas. | Indisponibilidade sem atividade afetada: não se aplica o gatilho. | Destinatário/canal indisponível: retry/estado **Pendente**. | Várias atividades/alterações: deduplicação e conteúdo **Pendente**. | Notificar impacto inexistente como confirmado; enviar antes de confirmar indisponibilidade: **Pendente**. |
| `RF-REC-020` | Consultar histórico de alterações, reservas, bloqueios e ocorrências por recurso. | Recurso sem histórico e grande volume: conteúdo/paginação **Pendente**. | Recurso inexistente: **Pendente**. | Eventos simultâneos: ordenação **Pendente** (observar `RNF-AUD-006`). | Omitir categoria de histórico existente ou permitir alteração silenciosa do histórico. |

### Analista de qualidade e auditoria

| Origem | Caminho feliz | Limite | Entrada inválida | Conflito | Estado proibido |
| --- | --- | --- | --- | --- | --- |
| `RF-AUD-001` | Perfil autorizado consulta grades, recursos e alocações sem poder alterá-los. | Consulta vazia/volumosa: paginação **Pendente**. | Objeto fora do escopo de acesso: negar conforme permissão; escopo exato **Pendente**. | Tentativa simultânea de consulta e mutação: consulta não pode conceder permissão de escrita. | Perfil de auditoria alterar os dados consultados. |
| `RF-AUD-002` | Consultar versões em rascunho, aprovadas e publicadas às quais tem acesso. | Nenhuma versão em uma categoria: retorno vazio; acesso **Pendente**. | Versão inexistente/não autorizada: **Pendente**. | Versões mudam durante consulta: consistência **Pendente**. | Exibir versão fora do acesso autorizado ou confundir categoria de estado. |
| `RF-AUD-003` | Pesquisar alocações por período, turma, professor, sala e material. | Limites de data e múltiplos filtros: combinação **Pendente**. | Identificador/filtro inválido: **Pendente**. | Filtros combinados: semântica **Pendente**. | Retornar alocação fora dos critérios efetivamente selecionados. |
| `RF-AUD-004` | Exibir estados rascunho, aprovada, publicada, cancelada ou alterada. | Cada estado, inclusive histórico vazio: significado de “alterada” **Pendente**. | Estado não listado/desconhecido: **Pendente**. | Estado concorrente: consistência com versão **Pendente**. | Exibir estado incorreto ou ocultar o estado de alocação. |
| `RF-AUD-005` | Consultar resultado da validação de conflito de alocação. | Sem resultado/validação ainda não executada: estado **Pendente**. | Alocação inexistente: **Pendente**. | Resultado muda após alteração de recurso: validade temporal **Pendente**. | Apresentar resultado ausente/desatualizado como validação atual. |
| `RF-AUD-006` | Validação reprovada mostra recursos e intervalos envolvidos. | Mais de um recurso/intervalo conflitante: completude **Pendente**. | Validação aprovada: não inventar dados de conflito. | Múltiplos conflitos devem ser associados à alocação reprovada. | Omitir recurso ou intervalo envolvido em reprovação. |
| `RF-AUD-007` | Registrar tentativa rejeitada por conflito ou falta de permissão. | Repetição de tentativas: retenção/deduplicação **Pendente**. | Operação rejeitada por outro motivo: escopo deste RF não define se registra. | Falha de registro junto com rejeição: política **Pendente**; observar `RNF-GER-007`/`008`. | Rejeição por conflito/permissão sem registro. |
| `RF-AUD-008` | Pesquisar evento por período, usuário, ação e recurso afetado. | Datas inicial/final inclusivas: **Pendente**. | Filtro malformado: **Pendente**. | Filtros combinados: semântica AND/OR **Pendente**. | Retornar eventos fora dos filtros. |
| `RF-AUD-009` | Alteração auditada exibe valores anteriores e novos dos campos modificados. | Campo adicionado/removido e valor nulo: representação **Pendente**. | Evento sem mudança: **N/A**. | Alterações concorrentes: associar par correto ao evento; política detalhada **Pendente**. | Omitir valor anterior ou novo em alteração registrada. |
| `RF-AUD-010` | Cada alteração registrada identifica usuário responsável e data/hora. | Eventos simultâneos: precisão/ordenação conforme `RNF-AUD-006`. | Agente/relógio indisponível: política de gravação **Pendente**. | Alterações concorrentes devem manter agente e horário correspondentes. | Evento de alteração sem responsável ou data/hora. |
| `RF-AUD-011` | Consultar justificativa de exceção autorizada. | Justificativa vazia ou longa: limite **Pendente**. | Exceção sem autorização/justificativa: processo de autorização **Pendente**. | Justificativas múltiplas/alteradas: histórico **Pendente**. | Exceção autorizada sem justificativa consultável. |
| `RF-AUD-012` | Consultar aprovador/rejeitador da versão e instante da decisão. | Cada desfecho aprovado/rejeitado e ausência de decisão: estados adicionais **Pendente**. | Versão inexistente/não acessível: **Pendente**. | Decisões concorrentes: prevalência **Pendente**. | Decisão sem aprovador/rejeitador ou horário consultável. |
| `RF-AUD-013` | Comparar versões e identificar cada alocação incluída, removida ou alterada. | Versões iguais: nenhuma diferença; comparação sem alterações. | Versão inexistente: **Pendente**. | Alterações simultâneas em uma alocação devem constar na comparação. | Omitir inclusão, remoção ou alteração. |
| `RF-AUD-014` | Consultar histórico de bloqueios, manutenções e mudanças de estado por recurso. | Recurso sem eventos e eventos simultâneos: retorno/ordenação **Pendente**. | Recurso inexistente/não autorizado: **Pendente**. | Alteração concorrente deve preservar cada evento; ver `RF-REC-020`. | Ocultar evento existente das categorias listadas. |
| `RF-AUD-015` | Consultar notificações geradas para mudanças publicadas e estado de processamento/entrega quando disponível. | Estado ainda indisponível: não alegar entrega; representação **Pendente**. | Notificação inexistente: retorno **Pendente**. | Eventos duplicados ou entrega parcial: semântica **Pendente**. | Reportar como entregue sem estado disponível/confirmado. |
| `RF-AUD-016` | Gerar relatório de alocação com filtro por período, turma, professor, sala ou material. | Filtros sem resultados: relatório vazio; formato/campos **Pendente**. | Filtro inválido: **Pendente**. | Filtros combinados e limites: semântica **Pendente**. | Incluir alocações fora dos filtros. |
| `RF-AUD-017` | Exportar relatório/eventos em formato estruturado preservando filtros e período consultado. | Exportação vazia e grande: limites **Pendente**. | Filtro/período inválido: **Pendente**. | Exportação concorrente a alteração de origem: consistência do snapshot **Pendente**. | Perder filtros ou período na exportação; incluir campo não autorizado (`RNF-AUD-003`). |
| `RF-AUD-018` | Consultar papéis atribuídos e permissões associadas. | Usuário sem papel/vários papéis: conteúdo **Pendente**. | Usuário/papel inexistente: **Pendente**. | Papéis com permissões sobrepostas: precedência **Pendente**. | Expor/modificar permissões pela consulta somente leitura de auditoria. |
| `RF-AUD-019` | Registrar e consultar alterações em contas, papéis e permissões. | Criação/remoção e alteração sem mudança: cobertura **Pendente**. | Alteração inválida: resposta e registro **Pendente**. | Alterações concorrentes: ordenação/precedência **Pendente**. | Alteração de acesso sem evento auditável. |
| `RF-AUD-020` | Relatório identifica versão publicada da grade e respectivo período de vigência. | Sem versão publicada ou vigência aberta: formato **Pendente**. | Versão/período inválido: **Pendente**. | Publicações com vigências sobrepostas: política **Pendente**. | Omitir versão publicada ou vigência do relatório. |

## Restrição arquitetural

| Origem | Caminho feliz | Limite | Entrada inválida | Conflito | Estado proibido |
| --- | --- | --- | --- | --- | --- |
| `C-ARQ-001` (`docs/arquitetura.md`, §1) | Verificar que o job é executado diariamente ao final do dia e treina/retreina usando somente dados novos. | Sem dados novos e instante de “final do dia”: resultado/horário exato **Pendente**. | Registro novo inválido, schema inesperado ou fonte indisponível: tratamento **Pendente**. | Reexecução, concorrência de workers e reutilização do mesmo lote: **Pendente**. | Usar dados históricos não novos no treinamento contraria “somente dados novos”; critérios de elegibilidade ainda precisam de definição. |

> A própria arquitetura registra que `C-ARQ-001` foi informado na solicitação que a originou, não foi encontrado nos arquivos do repositório e não altera RF/RNF. Incluí-lo aqui para rastreabilidade do artefato, não como requisito PRD aprovado.

## Critérios não funcionais (RNF)

Os casos abaixo são oráculos de teste derivados literalmente dos critérios do PRD. Todos os valores quantitativos são **propostas iniciais a validar**, não SLAs aprovados. Aplicar o perfil de carga documentado (25 usuários simultâneos, 1.000 atividades, 100 professores, 50 salas, 200 materiais; 2 minutos de aquecimento + 10 minutos de medição; ao menos três repetições) somente onde o RNF o referencia.

### Transversais

| Origem | Verificação e resultado esperado |
| --- | --- |
| `RNF-GER-001` | Comunicação navegador/aplicação/integrações em HTTPS TLS 1.2+; HTTP redirecionado ou rejeitado. Testar TLS mínimo e requisição HTTP. |
| `RNF-GER-002` | Dados pessoais, credenciais e backups protegidos em repouso conforme infraestrutura escolhida; segredo não aparece em texto simples no repositório. Algoritmo/padrão concreto depende da infraestrutura: **Pendente**. |
| `RNF-GER-003` | Testar usuário com permissão e sem permissão em cada operação protegida; sem permissão explícita, negar consulta/criação/edição/exportação/exclusão no servidor. |
| `RNF-GER-004` | Entrada inválida é validada no servidor; erro não revela stack trace/consulta interna; análise automatizada de dependências e testes de autorização sem vulnerabilidade crítica aberta conhecida. Ferramentas/versões: **Pendente**. |
| `RNF-GER-005` | Verificar coleta/exibição apenas de dados pessoais necessários; dados reais não usados em homologação sem autorização, minimização ou anonimização adequada. Inventário/base legal: **Pendente**. |
| `RNF-GER-006` | Varredura antes da entrega não encontra senha, token, chave, cookie, dado pessoal desnecessário em logs, erros, relatórios ou exportações. |
| `RNF-GER-007` | 100% das operações críticas listadas (criação, alteração, cancelamento, aprovação, publicação, bloqueio, mudança de permissão) geram evento com agente, horário UTC, ação, objeto, resultado e motivo quando aplicável. |
| `RNF-GER-008` | Usuário comum não edita/apaga evento; tentativa não autorizada é registrada sem segredos. |
| `RNF-GER-009` | Eventos usam UTC sincronizado com fonte confiável e ID de correlação relacionável a registros técnicos. Fonte e tolerância de sincronização: **Pendente**. |
| `RNF-GER-010` | Configurar retenção/arquivamento/descarte conforme política institucional/legal e comprovar política vigente e execução. Prazo e política: **Pendente**. |
| `RNF-GER-011` | Sob perfil inicial: consultas comuns p95 ≤ 2 s, p99 ≤ 5 s e erro < 1% no período medido. |
| `RNF-GER-012` | Sob perfil inicial: validação p95 ≤ 3 s, p99 ≤ 5 s; resultado considera simultaneamente todos os recursos e intervalos associados. |
| `RNF-GER-013` | Em 100 tentativas simultâneas para mesmo recurso exclusivo/intervalo, no máximo uma operação incompatível confirmada; demais recebem conflito e não deixam gravações parciais. |
| `RNF-GER-014` | Injetar falha antes da confirmação: nenhuma alocação parcialmente gravada nem redução de material sem reserva correspondente. |
| `RNF-GER-015` | Medir fluxo de consulta externamente por mês: disponibilidade inicial ≥ 99,5%, excluídas janelas previamente comunicadas; registrar indisponibilidades/duração. |
| `RNF-GER-016` | Exercício documentado de restauração comprova RPO ≤ 15 min e RTO ≤ 4 h; compatibilidade com custo/infraestrutura ainda precisa aceite. |
| `RNF-GER-017` | Fluxos críticos WCAG 2.2 AA: teclado completo, foco visível, contraste, rótulos, erros associados aos campos e leitores de tela. |
| `RNF-GER-018` | Interface/mensagens/datas/números em pt-BR; instantes armazenados em UTC e exibidos no fuso institucional; recorrências/horários locais não deslocados incorretamente. |
| `RNF-GER-019` | Fluxos críticos funcionam nas duas versões estáveis mais recentes de Chrome, Firefox, Edge e Safari sem erro impeditivo. |
| `RNF-GER-020` | Falhas/operações lentas diagnosticáveis por logs estruturados e correlação; duração/resultado registrados sem dado pessoal ou segredo. |

### Coordenadora

| Origem | Verificação e resultado esperado |
| --- | --- |
| `RNF-COORD-001` | Grade com até 1.000 atividades abre em p95 ≤ 3 s sob carga inicial. |
| `RNF-COORD-002` | Confirmação/validação/processamento visível em até 1 s após edição; gravação respeita RNFs transversais. |
| `RNF-COORD-003` | Teste de usabilidade: ao menos 4 de 5 coordenadores identificam recurso em conflito e ação sugerida em até 60 s sem ajuda. |
| `RNF-COORD-004` | Em desconexão/falha, preservar última versão confirmada, identificar alterações ainda não salvas e nunca indicar sucesso antes da confirmação do servidor. |
| `RNF-COORD-005` | Duas pessoas editando mesma versão não sobrescrevem mudanças silenciosamente; apresentar conflito de versão antes de confirmar. |
| `RNF-COORD-006` | Comparação de grades de até 1.000 atividades em p95 ≤ 5 s sob carga inicial. |
| `RNF-COORD-007` | Criar, editar, filtrar e revisar conflitos por teclado; não depender exclusivamente de arrastar/soltar ou cor. |
| `RNF-COORD-008` | Em casos de troca de fuso, virada de dia, horário de verão aplicável e recorrência, 100% dos horários exibidos correspondem ao horário local e instante armazenado. |
| `RNF-COORD-009` | Com pelo menos cinco usuários representativos, 4/5 criam rascunho, corrigem conflito simples e submetem para aprovação em até 15 min sem roteiro passo a passo. |
| `RNF-COORD-010` | Rascunho, submetida, aprovada e publicada distinguíveis sem depender só de cor; estado/versão vigentes visíveis. |

### Professor

| Origem | Verificação e resultado esperado |
| --- | --- |
| `RNF-PROF-001` | Informar disponibilidade e consultar grade em viewport de 360 px sem rolagem horizontal para conteúdo/ações essenciais. |
| `RNF-PROF-002` | Consulta de grade pessoal sob carga inicial: p95 ≤ 2 s e p99 ≤ 5 s. |
| `RNF-PROF-003` | Repetir envio após timeout/reconexão não duplica registros nem confirma sem gravação confirmada pelo servidor. |
| `RNF-PROF-004` | 99% das alterações publicadas que afetam professor ficam disponíveis no canal interno em até 60 s; integração externa registra envio e confirmação do provedor separadamente. |
| `RNF-PROF-005` | 100% das tentativas sem privilégio de acessar disponibilidade individual alheia são negadas e registradas sem expor os dados pedidos. |
| `RNF-PROF-006` | Consulta/envio com leitor de tela e ampliação de 200%, sem perda de conteúdo/controles essenciais. |
| `RNF-PROF-007` | Em teste de usabilidade, 4/5 professores identificam horário anterior, novo horário e vigência em até 30 s. |
| `RNF-PROF-008` | Interrupção durante envio informa estado pendente ou falho; só mostra conclusão após confirmação do servidor. |
| `RNF-PROF-009` | Horários no fuso institucional mantêm mesma interpretação na tela, impressão e exportação de calendário. |
| `RNF-PROF-010` | Com cinco professores representativos, 4/5 registram disponibilidade de semana típica em até 3 min sem orientação passo a passo. |

### Gestora de espaços e materiais

| Origem | Verificação e resultado esperado |
| --- | --- |
| `RNF-REC-001` | Com até 10.000 recursos, pesquisa por código/nome/tipo/localização p95 ≤ 2 s. |
| `RNF-REC-002` | Após confirmação de bloqueio/liberação/mudança de estado, consultas refletem estado novo em até 5 s. |
| `RNF-REC-003` | Sob concorrência, soma de materiais reservados nunca excede quantidade disponível em qualquer intervalo. |
| `RNF-REC-004` | Em 100% dos testes, visualizar impacto lista todas as atividades sobrepostas antes de confirmar bloqueio. |
| `RNF-REC-005` | Bloquear/liberar/alterar estado mostra confirmação em até 1 s e persiste dentro dos limites gerais. |
| `RNF-REC-006` | 100% das alterações confirmadas de inventário, estado, manutenção e bloqueio geram auditoria com responsável, horário e motivo quando informado. |
| `RNF-REC-007` | Consulta e ocorrência em tela de 360 px, controles utilizáveis por toque e sem rolagem horizontal essencial. |
| `RNF-REC-008` | Estado/disponibilidade indicados por texto ou ícone rotulado, nunca somente por cor. |
| `RNF-REC-009` | Com cinco responsáveis representativos, 4/5 bloqueiam recurso e identificam impacto em até 2 min sem ajuda. |
| `RNF-REC-010` | Falhas em atualização/bloqueio não deixam estado parcialmente salvo; após falha, consultar estado efetivamente persistido. |

### Auditoria

| Origem | Verificação e resultado esperado |
| --- | --- |
| `RNF-AUD-001` | Com até 100.000 eventos no perfil documentado, pesquisa por usuário/ação/recurso/período p95 ≤ 3 s. |
| `RNF-AUD-002` | Exportação contém exatamente registros dos filtros; total e amostras conferem campo a campo com consulta de origem. |
| `RNF-AUD-003` | Exportação respeita permissões da interface, registra solicitante/horário e omite campos sem autorização. |
| `RNF-AUD-004` | Relatório identifica versão do sistema, período, filtros, data/hora de geração e parâmetros usados. |
| `RNF-AUD-005` | Ponta a ponta: 100% das operações críticas aceitas/rejeitadas têm evento correspondente com agente/objeto/ação/resultado sem divergência. |
| `RNF-AUD-006` | Eventos preservam UTC e conversão local sem perda de ordenação; eventos simultâneos mantêm precisão mínima de um segundo. |
| `RNF-AUD-007` | Perfil de auditoria não altera grade/inventário/permissões/eventos; 100% das tentativas são negadas. |
| `RNF-AUD-008` | Antes da avaliação final, 100% dos requisitos classificados críticos ligados a ao menos um caso de teste e evidência identificável. |
| `RNF-AUD-009` | Mesmo teste de carga, ambiente e dataset repetidos três vezes: variação p95 ≤ 10%; acima disso, investigar e reportar. |
| `RNF-AUD-010` | Varredura confirma que documentos/relatórios exportados omitem ou anonimizaram dados pessoais desnecessários e não contêm credenciais, tokens ou segredos. |

## Lacunas, ambiguidades e perguntas

### Regras sem oráculo suficiente

- Intervalos: inclusão de endpoints, início/fim iguais, fusos, recorrências, virada de dia, buffers e relação entre buffers de professor/sala/material (`RF-COORD-004`/`010`/`011`, `RF-PROF-003`, `RF-REC-016`).
- Validação de datas/períodos, formatos, campos obrigatórios, faixas numéricas, quantidade zero/negativa e relação entre quantidade total/disponível (`RF-COORD-001`/`004`/`007`, `RF-REC-003`/`004`/`010`).
- Precedência entre disponibilidade, indisponibilidade, restrição obrigatória e preferência negociável (`RF-COORD-005`, `RF-PROF-002` a `004`).
- Ciclo de vida e autorização de grades: transições permitidas, responsáveis, estados de submissão, rejeição/reenvio, vigência, concorrência de aprovação/publicação e semântica de versão (`RF-COORD-016` a `020`).
- Critério de validade, ordenação e desempate das sugestões automáticas; o RF não exige ML. `C-ARQ-001` não define entrada/saída de sucesso, dados novos elegíveis, no-op, retries, falhas, duplicidade ou replay.
- Regras de estado/transição para recurso, manutenção, bloqueio, ocorrência e liberação; impacto de alteração com reserva existente (`RF-REC-006`, `010` a `018`).
- Notificações: canal, destinatários, conteúdo completo, deduplicação, retry, confirmação de entrega e política em falha (`RF-PROF-014`/`015`, `RF-REC-019`, `RF-AUD-015`, `RNF-PROF-004`).
- Papéis institucionais, escopo de acesso, exceções autorizadas, política de retenção e campos/formatos de relatório/exportação (`RF-PROF-020`, `RF-AUD-001`/`011`/`016` a `019`, `RNF-GER-003`/`010`, `RNF-AUD-003`).
- Perfil de carga para métricas específicas, ambiente de medição, disponibilidade/plantão, fonte de horário, política de segurança e critérios institucionais de privacidade precisam de aceite (`RNF-GER-002`/`004`/`005`/`009`/`010`/`015`/`016`).
- Divergência entre p95 ≤ 2 s para consulta comum (`RNF-GER-011`) e abertura de grade p95 ≤ 3 s (`RNF-COORD-001`) quando a operação/carga for a mesma; definir precedência ou escopos distintos.
- Personas são descritas como hipóteses fictícias; amostra de participantes e roteiros de usabilidade ainda precisam ser acordados.

### Perguntas para refinamento

1. Qual a semântica dos intervalos (extremos inclusivos/exclusivos), fuso institucional, feriados e recorrências? Como calcular buffers e conflitos que apenas encostam na fronteira?
2. Quais campos são obrigatórios e quais faixas são válidas para datas, duração, capacidade, quantidade e estoque? Pode existir período com início igual ao fim?
3. Qual precedência vale quando disponibilidade e indisponibilidade se sobrepõem? Restrição obrigatória pode ser negociada por alguém?
4. Quais transições de rascunho/submetida/aprovada/publicada/cancelada são permitidas, por quais papéis, e como funciona reenvio após rejeição?
5. Como resolver concorrência em aprovação/publicação, qual versão é vigente e como tratar vigências sobrepostas?
6. Quais critérios, entradas e desempates tornam uma sugestão automática válida? A saída sempre exige revisão humana? (O RF só determina que não publica automaticamente.)
7. Para recursos, quais mudanças de estado são permitidas com reservas; manutenção/bloqueio cancelam, preservam ou exigem remanejamento das atividades?
8. Qual política para ocorrência resolvida e data de liberação, bloqueio repetido/alterado e histórico de cada ação?
9. Qual canal, público, conteúdo, retry, deduplicação e definição de “entregue” para notificações?
10. Quais papéis têm acesso a cada dado, exportação e exceção? Quais justificativas são obrigatórias e qual retenção se aplica?
11. Quais campos/formato e semântica de combinação dos filtros são esperados nos relatórios e exportações? Qual consistência de snapshot deve ser garantida?
12. RNFs numéricos são metas aprovadas? Qual perfil/ambiente e qual requisito de latência prevalece para abrir grade?
13. Para `C-ARQ-001`, qual timezone define “final do dia”; o que significa dado novo; como tratar atraso/correção, lote vazio/inválido, falha, retry, concorrência, rejeição e replay autorizado?

## Fontes consultadas e limites

- `PRD/requisitos-funcionais.md`: 80 requisitos funcionais com IDs `RF-COORD-001..020`, `RF-PROF-001..020`, `RF-REC-001..020` e `RF-AUD-001..020`.
- `PRD/requisitos-nao-funcionais.md`: 60 RNFs (`RNF-GER`, `RNF-COORD`, `RNF-PROF`, `RNF-REC`, `RNF-AUD`), perfil de carga e ressalva de que metas são propostas.
- `PRD/persona-01-coordenadora-de-horarios.md`, `persona-02-professor.md`, `persona-03-gestora-de-espacos-e-materiais.md` e `persona-04-analista-de-qualidade-e-auditoria.md`: não adicionam IDs independentes ao catálogo; declaram personas/hipóteses ilustrativas.
- `docs/arquitetura.md`: contexto, restrição `C-ARQ-001`, referências aos RF/RNF, lacunas e oito ADRs na seção “ADRs propostos para revisão”. Os ADRs são propostas, não regras aprovadas, e não foram convertidos em critérios.
- `README.md` e `CHANGELOG.md`: descrevem a aplicação Java/Spring atual, cadastro de alunos/professores e histórico de correção de teste; não definem as regras de alocação do PRD.
- Código inspecionado: entidades, serviços, controllers web/API, repositórios, migração e testes. A implementação atual cobre cadastro de estudantes/professores; não implementa o domínio de agenda, recursos, aprovação ou auditoria descrito no PRD. Foram adicionados testes para o CRUD existente, mas eles não cobrem os 80 RF nem os 60 RNF do produto de alocação.
- Busca no repositório por `AGENTS.md` e arquivos com nome de ADR não encontrou resultados. Não foi encontrado arquivo separado de regras de negócio; `docs/testes/` também não existia antes deste plano.

O código existente foi usado apenas para avaliar cobertura e diferença de escopo, não como fonte para inventar critérios. Os critérios pendentes requerem decisão dos responsáveis de produto/negócio antes de um teste afirmar comportamento não documentado.

## Matriz de cobertura TDD (estado atual)

**Nenhuma regra do domínio de alocação do PRD possui teste novo nesta etapa.** Os testes abaixo para estudantes/professores cobrem apenas o CRUD atualmente implementado; não são evidência para requisitos de agenda, disponibilidade acadêmica, recursos ou auditoria. As linhas abaixo mantêm todos os identificadores do PRD visíveis e registram a lacuna. Cenários `Pendente` não têm resultado assertável até refinamento; `N/A` não representa caso de teste.

| Regra(s) | Caso(s) do plano | Teste JUnit 5 | Status |
| --- | --- | --- | --- |
| `RF-COORD-001` a `RF-COORD-020` | Caminho feliz, limite, inválido, conflito e estado proibido, conforme aplicabilidade de cada linha do inventário. | Nenhum. | **SEM TESTE** — domínio de agenda/grade não implementado; resultados pendentes. |
| `RF-PROF-001` a `RF-PROF-020` | Caminho feliz, limite, inválido, conflito e estado proibido, conforme aplicabilidade de cada linha do inventário. | Nenhum para disponibilidade/grade; `StudentApiControllerTest`, `TeacherApiControllerTest`, `StudentControllerTest` e `TeacherControllerTest` cobrem somente CRUD de cadastro. | **SEM TESTE DO PRD** — disponibilidade acadêmica, permissões e agenda não implementadas. |
| `RF-REC-001` a `RF-REC-020` | Caminho feliz, limite, inválido, conflito e estado proibido, conforme aplicabilidade de cada linha do inventário. | Nenhum. | **SEM TESTE** — recursos/manutenção não implementados; transições pendentes. |
| `RF-AUD-001` a `RF-AUD-020` | Caminho feliz, limite, inválido, conflito e estado proibido, conforme aplicabilidade de cada linha do inventário. | Nenhum. | **SEM TESTE** — auditoria/relatórios/permissões não implementados. |
| `RNF-GER-001` a `RNF-GER-020` | Verificações de segurança, privacidade, auditoria, desempenho, recuperação e operação descritas na tabela RNF. | Nenhum. | **SEM TESTE** — superfície/infraestrutura de alocação ausente; políticas e aceite pendentes. |
| `RNF-COORD-001` a `RNF-COORD-010` | Verificações de renderização, edição, usabilidade, concorrência, acessibilidade e datas descritas na tabela RNF. | Nenhum. | **SEM TESTE** — interface de grade ausente; metas propostas. |
| `RNF-PROF-001` a `RNF-PROF-010` | Verificações de mobile, consulta, duplicidade, notificação, privacidade e usabilidade descritas na tabela RNF. | Nenhum. | **SEM TESTE** — fluxos de professor/grade ausentes; metas propostas. |
| `RNF-REC-001` a `RNF-REC-010` | Verificações de inventário, estado, bloqueio, auditoria, mobile e recuperação descritas na tabela RNF. | Nenhum. | **SEM TESTE** — fluxos de recursos ausentes; metas propostas. |
| `RNF-AUD-001` a `RNF-AUD-010` | Verificações de pesquisa, exportação, evidência, integridade temporal e proteção descritas na tabela RNF. | Nenhum. | **SEM TESTE** — consulta/exportação/auditoria ausentes; metas propostas. |
| `C-ARQ-001` | Job diário com dados novos; limite, inválidos, concorrência e estados conforme a linha da restrição arquitetural. | Nenhum. | **SEM TESTE** — pipeline/job ausente; definição de novidade e oráculos pendentes. |

### Regras do CRUD implementado (fora do PRD de alocação)

| Caso | Testes | Status |
| --- | --- | --- |
| Respostas REST de listagem, busca, criação, duplicidade, atualização, exclusão e not-found. | `StudentApiControllerTest` (13), `TeacherApiControllerTest` (11). | **PASSOU** em `./mvnw -B clean verify`. |
| Listagem e formulários web de estudante/professor, criação, edição, redirect e registro inexistente. | `StudentControllerTest` (7), `TeacherControllerTest` (7). | **PASSOU** em `./mvnw -B clean verify`. |
| Delegação de leitura, gravação, exclusão e consultas de unicidade aos repositórios. | `StudentServiceTest` (7), `TeacherServiceTest` (6). | **PASSOU** em `./mvnw -B clean verify`. |
| Campos, timestamps e atualização com relógio determinístico, sem `Thread.sleep`. | `StudentTest` (3), `TeacherTest` (3), usando `Clock.fixed`. | **PASSOU** em `./mvnw -B clean verify`. |
| Rota home retorna HTTP 200 e view `home`. | `HomeControllerTest` com `MockMvcBuilders.standaloneSetup`. | **PASSOU**; a configuração standalone não renderiza o template Thymeleaf. |

### Resultado da suíte existente (baseline)

- `./mvnw -B verify` inicialmente falhou porque o wrapper não definia `maven.multiModuleProjectDirectory`; `mvnw` foi ajustado para definir a propriedade antes de iniciar o wrapper.
- `HomeControllerTest` falhou ao renderizar o fragmento Thymeleaf, sem alterar suas asserções. Foi convertido para `MockMvcBuilders.standaloneSetup`, mantendo as verificações de HTTP 200 e view `home`; esse teste não valida o template renderizado.
- A prova de gate abaixo do mínimo foi executada com apenas `StudentTest`: JaCoCo reportou 17,1% de linhas e 0% de branches e rejeitou o build contra os limites de 80%/70%.
- `./mvnw -B clean verify` foi executado após limpar o dataset parcial: os 59 testes passaram (zero falhas/erros) e o gate foi restaurado para verde.
- Relatório final: 185/187 linhas (98,9%), 33/36 branches (91,7%), 698/703 instruções (99,3%). HTML e XML foram gerados em `target/site/jacoco/`.

**Lacunas JaCoCo observadas na execução final:**

| Arquivo/classe | Linhas/branches descobertos | Classificação | Ação |
| --- | --- | --- | --- |
| `EducationalQualityProjectApplication.java` | Linhas 10–11 (`SpringApplication.run`). | Cosmética para regras de domínio; o teste de contexto cobre inicialização do contexto, mas não o método launcher. | Sem teste adicional de cobertura; não há comportamento de negócio nessas linhas. |
| `StudentApiController.java` | Branches na linha 71 (e-mail `null`) e linha 77 (matrícula `null`). | Risco de contrato: payload parcial pode pular validações de unicidade, mas semântica de `PUT`/campos ausentes não está documentada. | Não adicionar asserção até definir se campos nulos são permitidos, rejeitados ou preservados. |
| `TeacherApiController.java` | Branch na linha 68 (e-mail `null`). | Risco de contrato: payload parcial pula validação de unicidade; semântica de campos ausentes não definida. | Não adicionar asserção até definir o contrato de atualização. |

Não há limites numéricos de regra de negócio implementada que justifiquem casos `-1/0/+1` com `@ParameterizedTest`; não inventei tais limites. Os campos de timestamp agora aceitam `Clock` nos construtores de teste e mantêm `Clock.systemDefaultZone()` como padrão de produção.

**Limite de escopo:** os testes CRUD não cobrem o domínio de alocação do PRD. As 205 ocorrências `Pendente` ainda exigem decisões de produto para virar oráculos; as 15 dimensões `N/A` não são casos executáveis. O gate JaCoCo foi provado abaixo do mínimo e voltou a passar após a restauração da suíte completa. A verificação da rota home usa setup standalone e não cobre rendering Thymeleaf.