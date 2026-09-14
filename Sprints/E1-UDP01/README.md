# Comunicação baseada em Datagramas

Este documento reúne as respostas conceptuais da tarefa, organizadas pelos critérios de aceitação.

## CA1 — Modelo de falhas e desordenação

O UDP envia cada datagrama de forma independente. Cada mensagem é tratada como uma unidade autónoma, sem manter uma sequência global entre datagramas, e por isso o protocolo não garante que as mensagens cheguem pela ordem em que foram enviadas.

A desordenação nasce porque os datagramas podem seguir caminhos diferentes na rede, podem ser entregues por rotas distintas ou podem ser tratados em buffers diferentes antes de chegar ao recetor. Assim, a aplicação pode receber, por exemplo, primeiro a mensagem 2 e depois a mensagem 1.

Exemplo de desordenação:

- Envio em sequência: 1, 2, 3
- Receção possível: 2, 1, 3

O recetor só consegue saber que uma mensagem chegou fora de ordem se conhecer a sua sequência lógica. Essa informação não vem do UDP. O UDP não inclui um número de sequência; esse número tem de ser acrescentado pela aplicação.

## CA2 — Decisões da API de datagramas

### Porto fixo no servidor e não no cliente

O servidor cria o socket em um porto conhecido e fixo, neste caso 6789. Isso permite que o cliente saiba exatamente onde enviar os datagramas. O cliente, por outro lado, cria o socket sem indicar um porto específico, porque o sistema operativo escolhe um porto temporário livre para a sua comunicação.

Se o cliente fosse executado com o servidor desligado, não haveria resposta. O cliente iria apenas tentar enviar a mensagem, mas não receberia nada e a operação ficaria pendente ou terminaria com erro de comunicação.

### Buffer de receção e comprimento dos dados

O buffer do cliente tem 1000 bytes, mas a resposta real pode ocupar muito menos. O método `getLength()` indica quantos bytes foram realmente recebidos. Se ignorarmos esse valor e usarmos `new String(reply.getData())`, o Java imprime também bytes que podem estar “restantes” no buffer, mesmo que não pertençam à mensagem atual.

Por isso, para processar corretamente a resposta, devemos usar:

- `reply.getData()` para obter o array de bytes;
- `reply.getLength()` para saber quantos bytes são válidos.

No servidor, o mesmo problema é evitado porque o datagrama de resposta é construído com o comprimento exato dos dados recebidos, usando `request.getLength()`. Assim, o reenvio não inclui bytes inúteis do buffer.

### Origem do endereço e porto na resposta

Quando o servidor recebe um datagrama, o pacote traz também a origem: o endereço IP e o porto do cliente. Esse endereço e esse porto são usados para devolver a resposta. Assim, o servidor não precisa de conhecer antecipadamente quem é o cliente; a informação vem no próprio pacote recebido.

## CA3 — Protocolo, estado e regra de decisão

A regra de decisão do servidor é:

- se o número da mensagem recebida for igual a L + 1, aceita-a e responde com echo;
- caso contrário, responde com `waitingfor,<L+1>`.

O estado mínimo que o servidor guarda é L, o número da última mensagem aceite em ordem. O valor inicial de L é 0, o que significa que ainda não foi recebida nenhuma mensagem válida em sequência; a próxima esperada é 1.

O número da sequência viaja dentro da mensagem, como parte do conteúdo do datagrama, por exemplo:

- `1,olá`
- `2,mundo`
- `3,cruel`

Isso significa que, quando o servidor lê a mensagem, tem de analisar o número e o payload. Se a mensagem estiver mal formada, por exemplo sem vírgula ou com um N que não é número, o servidor não pode encerrar nem ficar inconsistente: deve rejeitar essa mensagem, sinalizar o erro e continuar a servir pedidos.

A decisão do servidor é simples e suficiente: basta comparar cada N com L + 1. Isso é o mínimo de informação necessária para saber se a mensagem está na ordem correta. Não precisamos de guardar mais do que o último valor aceite.

## CA4 — Demonstração e interpretação

### Cenário normal

Sequência:

- 1, olá
- 2, mundo
- 3, cruel

Resposta esperada:

- ok,1
- ok,2
- ok,3

O servidor aceita todas as mensagens pela ordem correta.

### Cenário de desordenação provocada

Sequência:

- 1, olá
- 3, mundo
- 2, cruel
- 3, mundo

Resposta esperada:

- ok,1
- waitingfor,2
- ok,2
- ok,3

A mensagem que prova a recuperação do erro é a última 3, porque antes dela o servidor estava à espera do 2. Depois de receber o 2 e aceitá-lo, o servidor passa a esperar o próximo número, que agora é 3, e aceita-o sem problemas.

A desordenação teve de ser provocada porque, em localhost, os datagramas costumam chegar pela ordem em que foram enviados. Para testar a lógica de rejeição e recuperação, foi necessário forçar uma ordem diferente.

## CA5 — Limites da solução

A solução detecta mensagens fora de ordem, mas não resolve todos os problemas do UDP.

### Exemplo: mensagens duplicadas

Se o mesmo datagrama chegar duas vezes, o servidor pode aceitá-lo como se fosse uma nova mensagem, se o número de sequência coincidir com o valor esperado. Isso não é desejável em muitos sistemas, porque duplicados devem ser ignorados ou tratados de forma explícita.

Para resolver isto, seria necessário adicionar um identificador mais forte, por exemplo um número de sequência persistente e um mecanismo de cache para descartar duplicados.

### Exemplo: vários clientes em simultâneo

Se existirem vários clientes ao mesmo tempo, o estado L único do servidor deixa de fazer sentido, porque cada cliente tem a sua própria sequência de mensagens. O servidor passaria a confundir as sequências dos clientes.

A correção exigiria manter estado por cliente, por exemplo associando cada cliente ao seu endereço IP e porto e guardando a última mensagem aceite para esse cliente em particular.

### Resumo

O mecanismo implementado resolve a desordenação ao nível da aplicação, mas continua sem resolver totalmente:

- duplicação de datagramas;
- perda silenciosa de mensagens;
- múltiplos clientes concorrentes.

## Conclusão

A solução construída mostra como a aplicação pode compensar uma limitação do UDP: a ausência de garantia de ordem. A ideia central é usar um número de sequência e manter apenas o estado mínimo necessário, L, para decidir se a mensagem chega na ordem correta ou se deve pedir retransmissão. Isto é uma forma típica de resolver problemas ao nível da aplicação, sem alterar o protocolo de transporte subjacente.
