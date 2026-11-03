# BDD - Aprovação, bloqueio e estados de reserva

## Escopo e rastreabilidade

Este documento transforma em cenários as quatro regras descritas na solicitação desta tarefa:

1. Recurso restrito exige aprovação de um `Responsável`.
2. `Solicitante` não aprova a própria reserva.
3. Recurso em manutenção não pode ser reservado.
4. A reserva segue `SOLICITADA → APROVADA → EM_USO → CONCLUÍDA`, com alternativas rejeitada, cancelada e não compareceu.

**Limite de fonte:** `docs/prd.md` e `docs/rtm.md` não foram encontrados. Foram procurados com `find` em `docs/` e no repositório; as buscas textuais por aprovação, Solicitante, Responsável, manutenção, estados e reserva cobriram `PRD/`, `docs/` e `src/`. Existem documentos de requisitos em `PRD/`, mas eles descrevem outro escopo (alocação acadêmica) e não formalizam estas quatro regras nem seus contratos. O código Java encontrado implementa estudante/professor; não há entidades ou serviços de reserva/manutenção. A arquitetura menciona que a política de manutenção e o fluxo de ocorrência precisam de detalhamento (`docs/arquitetura.md`, seção 24).

Assim, os cenários abaixo usam as quatro regras conforme enunciadas pelo solicitante, não como regras confirmadas pelo PRD/RTM do repositório. Resultados de borda não definidos estão marcados **Em aberto** e não devem virar expectativa de teste até decisão do responsável pelo produto.

## Cenários

### BDD-APR-01 - Aprovação de recurso restrito

**Dado** que uma reserva foi solicitada por um `Solicitante` para um recurso classificado como restrito  
**Quando** a reserva é submetida  
**Então** ela permanece em `SOLICITADA` até decisão de um `Responsável`  
**E** não pode passar a `APROVADA` por uma ação do Solicitante.

**Dado** que a reserva restrita está em `SOLICITADA`  
**Quando** um `Responsável` distinto do Solicitante aprova a reserva  
**Então** a reserva passa a `APROVADA`.

O caminho de recusa de aprovação é coberto por BDD-APR-02. A regra não define o catálogo de papéis nem como um recurso é marcado como restrito; isso precisa de contrato de domínio/autorização.

### BDD-APR-02 - Impedir autoaprovação

**Dado** que uma reserva está em `SOLICITADA` e registra o usuário A como Solicitante  
**Quando** o mesmo usuário A tenta aprová-la  
**Então** a aprovação é recusada  
**E** a reserva não muda para `APROVADA`.

**Dado** que a mesma reserva continua em `SOLICITADA`  
**Quando** um `Responsável` diferente do Solicitante a aprova  
**Então** a reserva pode passar para `APROVADA`, desde que os demais critérios de aprovação sejam satisfeitos.

O texto da regra não define o formato da recusa (erro de domínio ou resposta HTTP/status/corpo); esses detalhes ficam para o contrato da API.

### BDD-MAN-01 - Reserva durante manutenção

**Dado** que um recurso possui um período de manutenção  
**E** o intervalo solicitado se sobrepõe ao período de manutenção  
**Quando** o Solicitante tenta criar a reserva  
**Então** a reserva não é criada nem aprovada para esse recurso.

#### Exemplos de fronteira do período

Considere `M` como o início da manutenção e `F` como o fim. Os exemplos usam horários ilustrativos; não definem fuso nem precisão de armazenamento.

| Caso | Intervalo da reserva relativo à manutenção | Resultado especificado | Estado do caso |
| --- | --- | --- | --- |
| Termina exatamente no início | Reserva termina em `M`. | O PRD/RTM não foi encontrado e a regra fornecida não define se igualdade conta como sobreposição. | **Em aberto:** permitir ou rejeitar? A fronteira é inclusiva/exclusiva? |
| Começa exatamente no fim | Reserva começa em `F`. | O PRD/RTM não foi encontrado e a regra fornecida não define se igualdade conta como sobreposição. | **Em aberto:** permitir ou rejeitar? A fronteira é inclusiva/exclusiva? |
| Cruza o início por um segundo | Reserva começa antes de `M` e termina em `M + 1 segundo`. | Há interseção com o período de manutenção; pela regra “recurso em manutenção não pode ser reservado”, a solicitação deve ser rejeitada. | **Definido pela regra fornecida**, sujeito a confirmar a precisão temporal usada pelo sistema. |

**Perguntas abertas para BDD-MAN-01**

- Os períodos são intervalos fechados, abertos ou semiabertos? Em particular, `fim da reserva = início da manutenção` e `início da reserva = fim da manutenção` são conflitos?
- A aplicação usa segundos, milissegundos ou outra precisão? Qual fuso deve ser aplicado ao comparar os períodos?
- Se existir buffer de preparação/limpeza, ele também participa do conflito com manutenção? A fonte encontrada não resolve essa interação.

### BDD-EST-01 - Caminho principal de estados

**Dado** que uma reserva está em `SOLICITADA`  
**Quando** um `Responsável` aprova a reserva  
**Então** ela passa para `APROVADA`.

**Dado** que uma reserva está em `APROVADA`  
**Quando** o recurso é colocado em uso para essa reserva  
**Então** ela passa para `EM_USO`.

**Dado** que uma reserva está em `EM_USO`  
**Quando** o uso é encerrado  
**Então** ela passa para `CONCLUÍDA`.

### BDD-EST-02 - Rejeitar transições fora da sequência principal

**Dado** que uma reserva está em `SOLICITADA`  
**Quando** se tenta movê-la diretamente para `EM_USO`  
**Então** a transição não é permitida e o estado permanece `SOLICITADA`.

**Dado** que uma reserva está em `APROVADA`  
**Quando** se tenta movê-la diretamente para `CONCLUÍDA`  
**Então** a transição não é permitida e o estado permanece `APROVADA`.

As duas transições pulam um estado da sequência expressamente listada. A consequência externa da recusa (HTTP status/corpo) não está especificada.

### BDD-EST-03 - Resultados alternativos

**Dado** que uma reserva pode terminar como rejeitada, cancelada ou não compareceu  
**Quando** o ator autorizado aplica um desses resultados  
**Então** o resultado correspondente deve ser registrado, sem avançar a reserva pela sequência principal.

**Em aberto:** não há informação suficiente para afirmar quando cada resultado alternativo pode partir de `SOLICITADA`, `APROVADA` ou `EM_USO`, quem pode aplicá-lo, se “não compareceu” é um estado persistido ou um motivo de encerramento, ou se esses resultados são terminais. Não testar combinações específicas antes de aprovar a máquina de estados completa.

## Perguntas abertas consolidadas

1. Qual documento é a fonte oficial para estas regras? `docs/prd.md` e `docs/rtm.md` não existem no checkout, e os requisitos localizados em `PRD/` não detalham o fluxo de reserva descrito nesta tarefa.
2. Quais papéis e permissões definem `Solicitante` e `Responsável`? Como esses papéis se relacionam com a identidade do usuário da API?
3. Como o recurso é classificado como restrito e quais campos/estados de recurso são aceitos?
4. As extremidades de manutenção são inclusivas? Qual fuso e precisão temporal definem a comparação?
5. Quais são todas as transições permitidas para rejeitada, cancelada e não compareceu, e quem pode executar cada uma?
6. Quais respostas de API representam aprovação negada, conflito com manutenção e transição de estado inválida?

## Destino dos cenários de teste

Os tipos abaixo são uma proposta de alocação de teste, não uma afirmação de que existam classes/endpoints no código atual. Os contratos ausentes precisam ser definidos antes de fixar status HTTP, nomes de rotas ou estados de origem das alternativas.

### Testes de unidade

- **BDD-APR-01:** política de aprovação para recurso restrito e autorização do papel Responsável, com regra de estado `SOLICITADA → APROVADA`.
- **BDD-APR-02:** comparação entre usuário Solicitante e ator que aprova; asserção de que autoaprovação não altera o estado.
- **BDD-MAN-01:** decisão de conflito entre intervalo da reserva e manutenção; casos de fronteira ficam pendentes até a resposta das perguntas abertas.
- **BDD-EST-01:** cada transição válida do caminho principal.
- **BDD-EST-02:** exemplos de transições que pulam estados, confirmando rejeição e preservação do estado anterior.
- **BDD-EST-03:** não é testável como conjunto de transições até definir estados de origem, atores e terminalidade das alternativas.

### Testes de API

- **BDD-APR-01:** submissão de reserva restrita e tentativa de aprovação por Responsável autorizado; conferir estado e resposta observáveis.
- **BDD-APR-02:** chamada de autoaprovação recusada e estado mantido; chamada por Responsável distinto pode aprovar.
- **BDD-MAN-01:** criação sobreposta a manutenção rejeitada; executar os dois casos de igualdade como testes bloqueados/pendentes até decisão explícita das fronteiras; cruzamento por um segundo rejeitado.
- **BDD-EST-01:** chamadas de API para cada transição válida e verificação do estado persistido.
- **BDD-EST-02:** chamadas para transições proibidas não alteram o estado persistido.
- **BDD-EST-03:** casos de rejeição/cancelamento/não comparecimento somente após a matriz de transição e permissões ser aprovada.

Nenhum endpoint, status HTTP, papel adicional, janela temporal ou transição alternativa foi presumido neste documento.