package br.com.fiap.mercadoexpress.service;

import br.com.fiap.mercadoexpress.dto.PagamentoRequest;
import br.com.fiap.mercadoexpress.messaging.PagamentoProducer;
import br.com.fiap.mercadoexpress.model.Pedido;
import br.com.fiap.mercadoexpress.model.StatusPedido;
import br.com.fiap.mercadoexpress.model.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Finalização da compra: grava o pedido (transação própria, já commitada) e só depois
 * publica no RabbitMQ — assim a resposta do banco nunca chega antes do pedido existir no BD.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompraService {

    private final PedidoService pedidoService;
    private final PagamentoProducer pagamentoProducer;

    public Pedido finalizar(Usuario usuario, Map<Long, Integer> itens, String contaBanco) {
        Pedido pedido = pedidoService.criarPedido(usuario, itens, contaBanco);

        PagamentoRequest req = PagamentoRequest.builder()
                .pedidoId(pedido.getId())
                .clienteNome(usuario.getNome())
                .clienteEmail(usuario.getEmail())
                .contaBanco(pedido.getContaBanco())
                .valor(pedido.getValorTotal())
                .dataHora(LocalDateTime.now().toString())
                .build();
        try {
            pagamentoProducer.enviar(req);
        } catch (AmqpException e) {
            log.error("Falha ao publicar pagamento do pedido {}: {}", pedido.getId(), e.getMessage());
            return pedidoService.recusar(pedido.getId(), null,
                            "Serviço de pagamento indisponível (RabbitMQ fora do ar)", StatusPedido.ERRO_ENVIO)
                    .orElse(pedido);
        }
        return pedido;
    }
}
