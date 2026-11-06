# Proposta de requisito não funcional: desempenho de reservas

**Status: proposta para validação pelo grupo.** Estes valores são metas iniciais, não critérios aprovados nem resultados medidos.

## RNF-PERF-01: consulta de disponibilidade

Com 50 usuários virtuais simultâneos, o percentil 95 (p95) do tempo de resposta de uma consulta de disponibilidade deve ser de até 300 ms, medido no cliente JMeter em rede local, sem incluir inicialização da aplicação.

## RNF-PERF-02: criação de reserva

Com 50 usuários virtuais simultâneos disputando o mesmo recurso e intervalo, o p95 da criação de reserva deve ser de até 300 ms. Respostas HTTP `201` e `409` são resultados funcionais aceitos. Nenhuma criação pode responder `500` ou outro status não previsto.

A taxa de erro inesperado deve ser inferior a 1% das amostras. Para este cálculo, `409` é uma resposta funcional esperada e não conta como erro; falhas de transporte, timeouts e respostas diferentes de `201`/`409` contam como erro. Relatar também separadamente as contagens de `201` e `409`.

## Modelo de carga proposto

| Parâmetro | Proposta |
| --- | --- |
| Usuários virtuais | 50 |
| Ramp-up | 30 segundos, aproximadamente 1,7 usuários/segundo |
| Duração sustentada | 5 minutos após a inicialização do grupo |
| Mistura | 60% consultas de disponibilidade e 40% tentativas de criação |
| Contenção | Todos os usuários usam o mesmo identificador de sala e o mesmo intervalo |
| Identidades | 50 usuários fictícios carregados de `perf/usuarios.csv` |
| Ambiente | Aplicação executada localmente em `127.0.0.1`; não executar contra ambiente compartilhado |
| Repetições | Uma rodada identificada por arquivo JTL; registrar alterações de ambiente entre rodadas |

## Pré-requisitos para medir

A API de reservas (`/api/reservations`) e a persistência PostgreSQL opcional foram adicionadas em 2026-11-06. O plano JMeter em `perf/reservas.jmx` foi escrito antes desse fluxo e seus caminhos/contratos ainda precisam ser comparados com a API atual; não executá-lo sem essa revisão. Para uma rodada, habilitar `reservations.datasource.url`, aplicar Flyway, preparar localmente sala, professor, material, intervalo e identidades fictícias, e adaptar a autenticação simulada, pois autenticação real ainda não existe.

A carga não deve ser executada contra produção, homologação ou qualquer serviço compartilhado. O JTL bruto e o relatório HTML são artefatos locais; não devem ser versionados.
