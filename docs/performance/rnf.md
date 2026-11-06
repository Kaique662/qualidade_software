# Proposta de requisito não funcional: desempenho de reservas

**Status:** o contrato HTTP do experimento local foi aprovado. As metas abaixo continuam propostas e não são resultados medidos.

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

## Contrato HTTP aprovado para o experimento local

O plano `perf/reservas.jmx` usa estes endpoints, disponíveis somente na aplicação dedicada de desempenho:

| Operação | Contrato |
| --- | --- |
| Login | `POST /api/auth/login` com `{"username":"...","password":"..."}`; retorna `200` e `{"token":"..."}`. Credenciais inválidas retornam `401`. |
| Autorização | Enviar `Authorization: Bearer <token>` nos endpoints de disponibilidade e reserva. Os tokens são aleatórios, mantidos apenas em memória e revogados quando a aplicação reinicia. |
| Disponibilidade | `GET /api/resources/{resourceId}/availability?startsAt=...&endsAt=...`; retorna `200` com `resourceId`, intervalo e `available`. |
| Criar reserva | `POST /api/reservations` com `resourceId`, `teacherId`, `startsAt` e `endsAt`; retorna `201` se sala e professor forem aceitos, `409` em conflito de intervalo. |

As reservas do experimento são persistidas em PostgreSQL local. Restrições de exclusão `gist` em intervalos semiabertos `[início, fim)` garantem que requisições concorrentes não aceitem sobreposição da mesma sala ou do mesmo professor. A inserção na sala e na agenda do professor é transacional.

Esta aplicação é um harness educacional/local, não autenticação ou API de reserva pronta para produção. Usa apenas as contas fictícias de `perf/usuarios.csv`, não conecta ao MongoDB e se recusa a iniciar se o PostgreSQL não estiver em loopback. A aplicação também escuta apenas em `127.0.0.1`.

## Preparação e execução manual

Requisitos: Docker Compose, Java 17+ e JMeter 5.6.3. Nenhuma carga deve ser executada até que o grupo valide a preparação e confirme que o destino continua local.

1. Defina `PERF_DB_PASSWORD` no terminal como um valor local aleatório; não o salve no repositório. O usuário do banco padrão é `perfuser`.
2. Inicie o PostgreSQL descartável: `docker compose -f compose.perf.yaml up -d --wait`.
3. Na mesma sessão de terminal, com `PERF_DB_PASSWORD` ainda definido, inicie o harness:

   ```bash
   SERVER_PORT=8081 ./mvnw -Pperformance spring-boot:run \
     -Dspring-boot.run.profiles=performance
   ```

4. Antes de carregar, confirme a porta local e que o servidor responde: `curl -i http://127.0.0.1:8081/api/auth/login` (um `405` para GET confirma que a rota existe; não envie carga por esse método).
5. Com JMeter 5.6.3 instalado e a preparação validada, crie o diretório `perf/resultados` e execute o plano apenas no alvo padrão loopback:

   ```bash
   jmeter -n -t perf/reservas.jmx \
     -Jport=8081 \
     -l perf/resultados/rodada-01.jtl \
     -e -o perf/relatorio-01
   ```

6. Consulte a integridade no PostgreSQL local. Para o mesmo recurso e intervalo do plano, deve existir no máximo uma reserva aceita:

   ```sql
   SELECT count(*)
   FROM room_reservation
   WHERE resource_id = 'room-perf-01'
     AND status = 'ACEITA'
     AND time_range && tstzrange(
       '2027-01-15T09:00:00Z'::timestamptz,
       '2027-01-15T10:00:00Z'::timestamptz,
       '[)'
     );
   ```

7. Encerre a aplicação e o PostgreSQL local com `docker compose -f compose.perf.yaml down`. O JTL e o relatório são resultados locais; não devem ser versionados.

Não sobrescreva `host` ou `port` do JMeter para apontar a produção, homologação ou qualquer serviço compartilhado. Não reutilize credenciais reais.
