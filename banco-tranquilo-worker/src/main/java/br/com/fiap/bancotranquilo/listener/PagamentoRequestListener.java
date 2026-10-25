package br.com.fiap.bancotranquilo.listener;

import br.com.fiap.bancotranquilo.dto.PagamentoRequest;
import br.com.fiap.bancotranquilo.dto.PagamentoResponse;
import br.com.fiap.bancotranquilo.service.AnaliseCreditoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Consome pagamento-request-queue, analisa e publica a resposta:
 *  aprovado -> pagamento-response-sucesso-exchange
 *  recusado -> pagamento-response-erro-exchange
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PagamentoRequestListener {

    private final AnaliseCreditoService analiseCreditoService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbit.sucesso-exchange}") private String sucessoExchange;
    @Value("${app.rabbit.sucesso-routing-key}") private String sucessoRoutingKey;
    @Value("${app.rabbit.erro-exchange}") private String erroExchange;
    @Value("${app.rabbit.erro-routing-key}") private String erroRoutingKey;
    @Value("${banco.tempo-processamento-ms:0}") private long tempoProcessamento;

    @RabbitListener(queues = "${app.rabbit.request-queue}")
    public void processar(PagamentoRequest req) throws InterruptedException {
        log.info("[BANCO TRANQUILO] Requisição recebida: {}", req);
        if (tempoProcessamento > 0) Thread.sleep(tempoProcessamento); // simula processamento

        PagamentoResponse resp = analiseCreditoService.analisar(req);
        if (resp.isAprovado()) {
            rabbitTemplate.convertAndSend(sucessoExchange, sucessoRoutingKey, resp);
            log.info("[BANCO TRANQUILO] APROVADO pedido {} ({}) -> {}", resp.getPedidoId(), resp.getCodigoTransacao(), sucessoExchange);
        } else {
            rabbitTemplate.convertAndSend(erroExchange, erroRoutingKey, resp);
            log.info("[BANCO TRANQUILO] RECUSADO pedido {}: {} -> {}", resp.getPedidoId(), resp.getMensagem(), erroExchange);
        }
    }
}
