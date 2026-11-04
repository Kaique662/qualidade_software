# Defeito: ambiente de execução do Testcontainers não suporta o Docker API requerido

## Resumo
O teste de integração com PostgreSQL real foi escrito com Testcontainers, mas a execução falhou antes da lógica de negócio por incompatibilidade do ambiente do runner com o Docker API do contêiner.

## Passos para reprodução
1. Executar o comando:
   `cd /workspaces/qualidade_software && ./mvnw -B -Dtest=ConcurrentReservationIntegrationTest test`
2. O suporte do Testcontainers tenta iniciar o container `postgres:16-alpine`.
3. O ambiente responde com erro de infraestrutura:
   `Could not find a valid Docker environment.`
4. A mensagem adicional mostra:
   `client version 1.32 is too old. Minimum supported API version is 1.40, please upgrade your client to a newer version`

## Evidência observada
- Comando executado em 2026-11-04.
- Saída final do Maven registra falha de infraestrutura, não falha funcional da regra de exclusão.
- O erro foi obtido antes da execução do teste, no bootstrap do container PostgreSQL.

## Impacto
- Não foi possível validar a regra de concorrência em ambiente local nesta sessão.
- A correção estrutural do banco foi aplicada para garantir a exclusão de sobreposição quando o ambiente Docker estiver disponível.

## Ação corretiva
- Resolver a compatibilidade do daemon/cliente Docker do ambiente.
- Reexecutar a suíte com PostgreSQL real após a atualização do cliente/daemon.
