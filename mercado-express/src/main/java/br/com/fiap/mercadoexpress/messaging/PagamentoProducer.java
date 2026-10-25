package br.com.fiap.mercadoexpress.messaging;

import br.com.fiap.mercadoexpress.dto.PagamentoRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Produz a mensagem: para publicar basta informar a Exchange e a Routing Key (aula 17). */
@Slf4j
@Component
@RequiredArgsConstructor
public class PagamentoProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbit.request-exchange}") private String exchange;
    @Value("${app.rabbit.request-routing-key}") private String routingKey;

    public void enviar(PagamentoRequest request) {
        log.info("[RABBIT] Enviando pagamento do pedido {} -> {} / {}", request.getPedidoId(), exchange, routingKey);
        rabbitTemplate.convertAndSend(exchange, routingKey, request);
    }
}
