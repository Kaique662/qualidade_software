# Persona 02: Marcos Vinícius Lima, professor

## Contexto do documento

Esta persona representa o professor que informa sua disponibilidade e consulta a grade acadêmica. É um perfil fictício para orientar a descoberta e a avaliação do produto proposto de alocação sem conflitos. Nome, idade e situações são ilustrativos, não resultados de entrevistas. Validar as hipóteses com professores reais antes de usá-las como evidência de pesquisa.

## Perfil

- **Idade ilustrativa:** 36 anos.
- **Cargo:** Professor de graduação com aulas em mais de uma turma.
- **Experiência:** 9 anos de docência; participa da revisão dos horários a cada período.
- **Contexto de trabalho:** Divide o tempo entre aulas, preparação, atendimento a alunos e atividades em outra unidade ou instituição.
- **Dispositivos e ambiente:** Consulta informações principalmente pelo celular; usa computador para informar disponibilidade e planejar o semestre.
- **Familiaridade digital:** Intermediária; espera um fluxo direto e não quer aprender um sistema administrativo complexo.
- **Condições de uso:** Pode acessar a grade em deslocamento ou com conexão instável. Precisa de interface responsiva, texto legível e navegação por teclado.

## Frase representativa

> “Quero informar meus horários possíveis uma vez e confiar que a grade publicada é a que vale.”

## Objetivos

- Informar disponibilidade, indisponibilidade e preferências com antecedência.
- Consultar sua grade por dia, semana e período letivo.
- Identificar rapidamente alterações desde a última consulta.
- Receber aviso quando uma alteração afetar suas aulas.
- Conferir sala, local, duração e recursos necessários para cada atividade.
- Solicitar correção de informação incorreta e acompanhar a resposta.

## Jornada ponta a ponta

1. Acessa o período letivo correto e confere seus dados de vínculo e disciplinas.
2. Informa horários disponíveis e indisponíveis, incluindo recorrência e validade das restrições.
3. Distingue impedimentos obrigatórios de preferências negociáveis.
4. Consulta a grade proposta e aponta incompatibilidades ou informações incorretas.
5. Recebe a publicação da grade e verifica horários, salas e recursos das aulas.
6. Consulta alterações posteriores, identificando versão, data de atualização e vigência.
7. Abre uma solicitação de ajuste ou contesta uma mudança e acompanha seu estado e histórico.

## Dores e riscos

- Horários divulgados por canais diferentes podem divergir.
- Preferências podem ser interpretadas como impedimentos ou restrições rígidas podem ser ignoradas.
- Alterações sem aviso podem causar ausência em aula ou deslocamento desnecessário.
- Dados pessoais de disponibilidade não devem ser visíveis para qualquer usuário.
- Uma interface somente para desktop é difícil de usar em deslocamento.
- Sem indicação de versão e vigência, não sabe qual horário é oficial.
- Uma conexão instável pode dificultar o envio e deixar dúvida sobre o recebimento da disponibilidade.

## Necessidades do produto

- Formulário simples para informar disponibilidade, com distinção entre restrições obrigatórias e preferências.
- Confirmação de salvamento e tratamento claro de falhas de conexão ou envio.
- Calendário responsivo com período letivo, fuso horário, versão e estado de publicação visíveis.
- Notificação de alteração que explique o impacto sem expor dados de terceiros.
- Controle de acesso para que cada professor consulte somente os dados autorizados para seu papel.
- Canal para solicitar correção, com protocolo ou identificador, estado e histórico do atendimento.
- Apresentação clara dos estados: proposta, aprovada e publicada.
- Navegação por teclado, textos legíveis e mensagens de erro compreensíveis.

## Critérios de sucesso

- Consegue informar disponibilidade sem treinamento e confirmar que ela foi recebida.
- Consulta a grade vigente pelo celular e entende se é uma versão publicada.
- Recebe aviso de mudança que afeta suas aulas e identifica o que mudou.
- Não consegue acessar dados de disponibilidade de outro professor sem autorização.
- Consegue enviar uma solicitação e acompanhar seu estado sem recorrer a canais paralelos.

## Evidências para avaliação

- Testes funcionais para disponibilidade, recorrência, preferências, estados de publicação e solicitações.
- Testes de autorização negativos: um professor não consulta nem altera informações de outro sem permissão.
- Testes responsivos em tamanhos móveis e validação de navegação por teclado e mensagens de acessibilidade.
- Testes de falha e repetição de envio que verifiquem ausência de registros duplicados ou confirmação enganosa.
- Evidência de que notificações são emitidas para mudanças relevantes sem revelar dados restritos.
- Registro relacionando versão publicada, alteração e notificação, com status de entrega quando disponível.
- Verificação de que logs não guardam dados pessoais desnecessários, senhas ou tokens.

## Hipóteses a validar

- Quais dados de disponibilidade podem ser vistos por coordenação, pelo próprio professor e por auditoria?
- Quais canais de notificação são acessíveis e preferidos pelos professores?
- Quanto tempo de antecedência é necessário para considerar uma mudança aceitável?
- Professores precisam exportar ou sincronizar a grade com calendários externos?
- Quais recursos de acessibilidade são prioritários para os usuários reais?