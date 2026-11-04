# ADR 0001: usar `EXCLUDE USING gist` com `tstzrange`

- Status: Aceito
- Data: 2026-11-04

## Contexto
A regra de negócio exige que a mesma sala ou o mesmo professor não tenha intervalos sobrepostos para a mesma janela de tempo. Em concorrência, duas transações simultâneas podem tentar inserir o mesmo intervalo e causar duplicidade caso a verificação seja feita apenas na aplicação.

## Decisão
Usaremos uma exclusão de intervalos no PostgreSQL com `EXCLUDE USING gist` sobre `tstzrange`, usando a chave de negócio (`resource_id` ou `professor_id`) e o intervalo de tempo.

## Motivo
- O banco garante a atomicidade da validação e da inserção.
- A concorrência é tratada no nível do armazenamento, sem depender de race conditions na aplicação.
- A consulta `&&` em `gist` detecta sobreposição de intervalos em uma forma nativa e eficiente.
- Isso é mais robusto do que validar somente na camada de aplicação ou usar uma abordagem transacional frágil.

## Consequências
- Tentativas de sobreposição passam a falhar com violação da exclusão, produzindo status 409 na camada de serviço/API.
- O banco mantém uma única linha válida por recurso e intervalo, preservando a integridade da agenda.
- O esquema passa a refletir uma regra de negócio de domínio e não apenas uma validação de aplicação.
