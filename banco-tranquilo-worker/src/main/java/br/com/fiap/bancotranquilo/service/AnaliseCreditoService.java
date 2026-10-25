package br.com.fiap.bancotranquilo.service;

import br.com.fiap.bancotranquilo.dto.PagamentoRequest;
import br.com.fiap.bancotranquilo.dto.PagamentoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Regras (simuladas) do Banco Tranquilo para aprovar ou recusar um pagamento. */
@Service
public class AnaliseCreditoService {

    private final BigDecimal limitePorCompra;
    private final Set<String> contasBloqueadas;

    public AnaliseCreditoService(@Value("${banco.limite-por-compra}") BigDecimal limitePorCompra,
                                 @Value("${banco.contas-bloqueadas:}") String contasBloqueadas) {
        this.limitePorCompra = limitePorCompra;
        this.contasBloqueadas = Arrays.stream(contasBloqueadas.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
    }

    public PagamentoResponse analisar(PagamentoRequest req) {
        String codigo = "BT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String agora = LocalDateTime.now().toString();

        String motivoRecusa = null;
        if (req.getPedidoId() == null) motivoRecusa = "Requisição sem pedidoId";
        else if (req.getValor() == null || req.getValor().signum() <= 0) motivoRecusa = "Valor inválido";
        else if (req.getContaBanco() == null || req.getContaBanco().isBlank()) motivoRecusa = "Conta não informada";
        else if (contasBloqueadas.contains(req.getContaBanco().trim())) motivoRecusa = "Conta bloqueada";
        else if (req.getValor().compareTo(limitePorCompra) > 0)
            motivoRecusa = "Valor acima do limite por compra (R$ " + limitePorCompra + ")";

        if (motivoRecusa != null) {
            return PagamentoResponse.builder().pedidoId(req.getPedidoId()).aprovado(false)
                    .codigoTransacao(codigo).mensagem("Pagamento recusado: " + motivoRecusa).processadoEm(agora).build();
        }
        return PagamentoResponse.builder().pedidoId(req.getPedidoId()).aprovado(true)
                .codigoTransacao(codigo).mensagem("Pagamento aprovado pelo Banco Tranquilo").processadoEm(agora).build();
    }
}
