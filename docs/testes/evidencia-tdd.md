# Evidência do ciclo TDD para regras de reserva

## Cenário coberto

### BDD-EST-02 - Rejeitar transições fora da sequência principal

- Dado que uma reserva está em `SOLICITADA`
- Quando se tenta movê-la diretamente para `EM_USO`
- Então a transição não é permitida e o estado permanece `SOLICITADA`
- Dado que uma reserva está em `APROVADA`
- Quando se tenta movê-la diretamente para `CONCLUÍDA`
- Então a transição não é permitida e o estado permanece `APROVADA`

## Status da implementação

A regra já estava implementada no domínio em `ReservationService.transition(...)`:

- `startUse()` aceita apenas transições de `APROVADA -> EM_USO`
- `complete()` aceita apenas transições de `EM_USO -> CONCLUIDA`
- qualquer salto entre estados não altera o valor atual de `status`

Em outras palavras, o cenário foi convertido em teste de regressão para garantir que a política de sequência continue preservada.

## Comando de verificação

```bash
./mvnw -B test -Dtest=com.example.educationalqualityproject.reservation.ReservationServiceTest
```

## Resultado verificado

- Testes executados: 5
- Falhas: 0
- Erros: 0
- Status: BUILD SUCCESS

## Histórico relevante

- `test(red): BDD-EST-01 reservation lifecycle`
- `feat(green): BDD-EST-01 reservation lifecycle`
- `refactor: centralize reservation state transitions`
- `test: add invalid transition coverage`

A última alteração adiciona o teste de regressão para `BDD-EST-02` sem exigir correção de produção, porque a regra de transição proibida já estava correta no serviço de domínio.
