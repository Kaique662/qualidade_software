# Persona 04: Diego Ferreira Alves, analista de qualidade e auditoria

## Contexto do documento

Esta persona representa quem verifica requisitos, segurança, rastreabilidade e desempenho do sistema de alocação acadêmica. É um perfil fictício para orientar a avaliação. Nome, idade e situações são ilustrativos, não representam pesquisa já realizada. Validar as necessidades com a equipe de qualidade e com os responsáveis pela avaliação institucional.

## Perfil

- **Idade ilustrativa:** 31 anos.
- **Cargo:** Analista de qualidade de software, com responsabilidade compartilhada por evidências de auditoria e conformidade.
- **Experiência:** 7 anos em testes de sistemas administrativos; familiaridade com APIs, logs, métricas e controle de acesso.
- **Contexto de trabalho:** Avalia versões antes de demonstrações e entregas; precisa relacionar requisitos, riscos, casos de teste, defeitos e resultados.
- **Dispositivos e ambiente:** Computador com ferramentas de teste e documentação; executa validações em ambiente controlado de homologação.
- **Familiaridade digital:** Avançada tecnicamente, mas precisa apresentar evidências compreensíveis também para avaliadores não técnicos.
- **Condições de uso:** Valoriza documentação pesquisável, resultados reproduzíveis e identificadores consistentes.

## Frase representativa

> “Não basta dizer que não há conflitos; preciso demonstrar como isso foi verificado e o que acontece quando algo falha.”

## Objetivos

- Confirmar que as regras de alocação estão especificadas e cobertas por testes reproduzíveis.
- Demonstrar que pessoas não autorizadas não conseguem consultar ou alterar dados protegidos.
- Rastrear cada requisito crítico até cenários, execução, evidências e defeitos relacionados.
- Medir desempenho sob carga definida, reportando ambiente, dados, método e resultados.
- Identificar falhas de validação, concorrência, tratamento de erro, logs e exposição de dados.
- Apresentar resultados objetivos, limitações e riscos residuais sem afirmar qualidade absoluta com base em uma suíte verde.

## Jornada ponta a ponta

1. Revisa requisitos e identifica regras de negócio, atributos de qualidade, riscos e critérios de aceitação.
2. Relaciona requisitos a cenários, dados de entrada, resultados esperados e evidências a coletar.
3. Executa testes unitários, integração, API, autorização e interface em ambiente controlado.
4. Reproduz casos-limite: sobreposição parcial, intervalos adjacentes, concorrência e alteração de disponibilidade durante a gravação.
5. Executa testes de desempenho com volume, perfil de acesso, duração e limites documentados.
6. Verifica logs e auditoria quanto a completude, integridade, controle de acesso e ausência de segredos.
7. Registra defeitos com passos para reprodução, severidade, requisito relacionado e evidências.
8. Reexecuta testes após correções e publica relatório versionado com resultados e riscos remanescentes.

## Dores e riscos

- Requisitos como “rápido”, “seguro” e “sem conflitos” não são critérios de aprovação mensuráveis.
- Testes sem ambiente, dados e passos registrados não podem ser reproduzidos.
- Uma suíte aprovada pode não exercitar concorrência, autorização ou fronteiras de intervalo.
- Logs podem omitir quem realizou uma mudança ou armazenar dados sensíveis em excesso.
- Cobertura de código isolada não prova que as regras de negócio estão corretas.
- Medição de desempenho sem carga e ambiente definidos não permite comparação.
- Sem vínculo entre requisito, teste, defeito e evidência, a avaliação é difícil de auditar.

## Necessidades para avaliação

- Requisitos identificáveis, priorizados e com critérios de aceitação verificáveis.
- Matriz requisito -> risco -> cenário de teste -> execução -> evidência -> defeito.
- Contas de teste e papéis documentados, com casos positivos e negativos de autorização.
- Registro de auditoria com agente, data/hora, ação, objeto afetado, resultado e motivo quando aplicável.
- Controle de acesso e política definida para retenção e consulta de auditoria.
- Relatório de desempenho com dataset, concorrência, taxa de requisições, duração, ambiente, p50, p95, p99 e taxa de erro.
- Relatórios versionados com identificador do build/commit, data, ambiente e resultado.
- Separação entre logs operacionais, auditoria e telemetria; não registrar senhas, tokens ou dados pessoais desnecessários.
- Erros apresentados ao usuário não devem expor stack traces, consultas, segredos ou dados de terceiros.

## Critérios de sucesso

- Cada requisito crítico tem critério verificável e evidência associada.
- Casos de conflito e autorização podem ser repetidos com resultados determinísticos.
- Alterações administrativas relevantes podem ser atribuídas a um agente e reconstruídas pelo histórico.
- Outra pessoa consegue repetir uma medição de desempenho usando a metodologia documentada.
- O relatório diferencia testes executados, aprovados, reprovados, não executados e riscos aceitos.
- Dados usados em demonstrações e relatórios não expõem informações pessoais ou segredos.

## Evidências para avaliação

- Matriz de rastreabilidade atualizada, incluindo exemplos de ponta a ponta.
- Relatórios automatizados de teste ligados à versão avaliada.
- Testes de autenticação, autorização, validação no servidor, erros e proteção dos logs.
- Testes de concorrência demonstrando que apenas uma alocação válida é confirmada quando duas solicitações disputam o mesmo recurso e horário.
- Relatório de desempenho com ambiente, dataset, carga, duração, percentis e taxa de erro.
- Como metas iniciais para validação, considerar p95 inferior a 2 segundos para consultas comuns e inferior a 3 segundos para validar uma alocação, sempre sob carga acordada e documentada. Essas metas não são compromissos aprovados até serem validadas com usuários e infraestrutura representativos.
- Amostras anonimizadas de auditoria com autoria, horário, alteração, resultado e justificativa, sem revelar segredos.
- Registro de riscos residuais e limitações dos testes executados.

## Hipóteses a validar

- Quais evidências e formato de relatório são exigidos pelos avaliadores do projeto?
- Qual carga de usuários e volume de alocações é representativo para a instituição?
- Qual política institucional rege retenção, acesso e descarte de registros de auditoria?
- Quais dados podem ser usados em homologação e demonstrações sem risco de exposição?
- Quais limiares de desempenho são aceitáveis para cada fluxo e sob qual infraestrutura?