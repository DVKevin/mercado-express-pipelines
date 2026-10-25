package br.com.fiap.mercadoexpress.model;

public enum StatusPedido {
    AGUARDANDO_PAGAMENTO, // enviado ao Banco Tranquilo, aguardando resposta
    APROVADO,             // banco confirmou -> e-mail enviado
    RECUSADO,             // banco recusou
    ERRO_ENVIO            // não foi possível publicar no RabbitMQ
}
