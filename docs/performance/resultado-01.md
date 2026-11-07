# Resultado de desempenho - Rodada 01

> Atualização em 2026-11-06: esta evidência registra uma rodada histórica anterior à API de reservas. O código agora contém `/api/reservations` e PostgreSQL/Flyway opcionais, mas nenhum novo JTL foi produzido; a decisão de desempenho continua inconclusiva.

## Decisão: inconclusivo

A rodada não foi executada. Não existe `perf/resultados/rodada-01.jtl` nem `perf/relatorio-01/index.html`; portanto, não há amostras para calcular percentis, vazão ou taxa de erro. Nenhuma afirmação de desempenho é feita sobre produção ou sobre a aplicação em execução.

## Ambiente

| Item | Valor observado |
| --- | --- |
| Máquina/contêiner | Linux x86_64, kernel `6.8.0-1064-azure`; workspace em contêiner Ubuntu 24.04.5 |
| Recursos visíveis | 2 CPUs lógicas, 7.8 GiB RAM |
| Java disponível | OpenJDK 25.0.4.1 (Microsoft build) |
| Versão declarada da aplicação | `0.0.1-SNAPSHOT` (POM); aplicação não estava em execução |
| Commit HEAD | `5059b3ac13b89856afa66cb7c550dd66ca373a83` |
| Estado do checkout | Worktree com alterações locais; o plano e a documentação da rodada não estão commitados |
| JMeter | Não instalado (`jmeter` não encontrado no PATH) |
| Alvo local | `127.0.0.1:8080` recusou conexão durante a verificação |
| Banco local | Não foi possível consultar: `psql`/`pg_isready` ausentes e aplicação/endpoints de reserva indisponíveis |

## Carga aplicada

Nenhuma carga foi aplicada: 0 usuários iniciados, sem ramp-up e sem duração de medição. O modelo proposto em `docs/performance/rnf.md` (50 usuários, ramp-up de 30 s, 300 s, mistura de 60% disponibilidade e 40% criação) não foi executado. O JMeter também não está instalado e o código atual não implementa login, consulta de disponibilidade nem criação de reserva; executar o plano contra este checkout não produziria uma medição funcional.

## Resultados por operação

Não há arquivo JTL. As métricas abaixo são indisponíveis, não zero.

| Operação | Amostras | p50 | p95 | p99 | Vazão | Erros inesperados | HTTP 201 | HTTP 409 esperado |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Login | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A |
| Consulta de disponibilidade | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A |
| Criação de reserva | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A |

Como não houve amostras, não é possível separar `409` esperado de erro real nem estimar a taxa de erro.

## Integridade no banco

Não confirmada. Nenhuma consulta local ao banco foi executada, pois não havia serviço/endpoints de reserva disponíveis e o cliente PostgreSQL não está instalado. Assim, não há evidência para afirmar que existe no máximo uma reserva aceita para o recurso e intervalo disputados. A migration SQL com exclusões `gist` não substitui a consulta a uma base inicializada nem comprova que foi aplicada.

## Comparação com os RNFs propostos

| Critério | Meta proposta | Resultado | Avaliação |
| --- | --- | --- | --- |
| Consulta de disponibilidade | p95 <= 300 ms, 50 usuários | N/A, sem execução | Inconclusivo |
| Criação de reserva | p95 <= 300 ms, 50 usuários | N/A, sem execução | Inconclusivo |
| Respostas de criação | Apenas 201 ou 409; nunca 500 | N/A, sem execução | Inconclusivo |
| Taxa de erro inesperado | < 1%, excluindo 409 esperado | N/A, sem execução | Inconclusivo |
| Integridade | No máximo 1 aceita para recurso/intervalo | Não consultado | Inconclusivo |

Nenhum valor foi arredondado ou inferido a partir de dados ausentes. Não há comparação quantitativa válida com as metas.

## Hipótese de gargalo

A hipótese sustentada pelos dados da rodada é que o gargalo estava na **prontidão do ambiente de medição**, não em uma operação de runtime: JMeter ausente, conexão local em `127.0.0.1:8080` recusada e endpoints ainda não implementados naquele checkout. A implementação posterior não altera a ausência de amostras nem permite inferir um gargalo de desempenho.

## Próximo experimento

O contrato local foi aprovado e o harness PostgreSQL está implementado em `com.example.performance.PerformanceApplication`. O guia atualizado em `docs/performance/rnf.md` descreve a inicialização e o encerramento locais. Este documento continua registrando uma rodada não executada; nenhum resultado foi acrescentado.

1. Disponibilizar JMeter 5.6.3 no runner e validar que Docker Compose, PostgreSQL, aplicação e dados fictícios estão locais.
2. Executar o comando definido para a rodada: `jmeter -n -t perf/reservas.jmx -l perf/resultados/rodada-01.jtl -e -o perf/relatorio-01`.
3. Calcular p50/p95/p99 e vazão por label/operação a partir do JTL, contando `409` como resultado funcional e não como erro; contar falha de transporte, timeout e status diferente de `201`/`409` na criação como erro.
4. Consultar diretamente o banco local após a carga e confirmar que a mesma sala/intervalo possui no máximo uma reserva aceita.
5. Registrar a versão executada, o commit exato, a máquina e a configuração de carga; repetir em uma nova rodada se o grupo validar essa necessidade.

Manter todos os testes estritamente locais. Os números de uma execução local não caracterizam produção.
