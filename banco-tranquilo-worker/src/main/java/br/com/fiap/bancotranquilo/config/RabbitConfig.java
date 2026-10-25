package br.com.fiap.bancotranquilo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declara as 3 exchanges, 3 queues e os bindings (routing keys) da aula 17.
 * O RabbitAdmin do Spring cria tudo automaticamente no broker ao conectar
 * (se já existirem com a mesma configuração, nada muda).
 */
@Configuration
public class RabbitConfig {

    @Value("${app.rabbit.request-exchange}") private String requestExchange;
    @Value("${app.rabbit.request-queue}") private String requestQueue;
    @Value("${app.rabbit.request-routing-key}") private String requestRoutingKey;
    @Value("${app.rabbit.sucesso-exchange}") private String sucessoExchange;
    @Value("${app.rabbit.sucesso-queue}") private String sucessoQueue;
    @Value("${app.rabbit.sucesso-routing-key}") private String sucessoRoutingKey;
    @Value("${app.rabbit.erro-exchange}") private String erroExchange;
    @Value("${app.rabbit.erro-queue}") private String erroQueue;
    @Value("${app.rabbit.erro-routing-key}") private String erroRoutingKey;

    // ---------- Exchanges ----------
    @Bean DirectExchange pagamentoRequestExchange() { return new DirectExchange(requestExchange); }
    @Bean DirectExchange pagamentoSucessoExchange() { return new DirectExchange(sucessoExchange); }
    @Bean DirectExchange pagamentoErroExchange()    { return new DirectExchange(erroExchange); }

    // ---------- Queues ----------
    @Bean Queue pagamentoRequestQueue() { return QueueBuilder.durable(requestQueue).build(); }
    @Bean Queue pagamentoSucessoQueue() { return QueueBuilder.durable(sucessoQueue).build(); }
    @Bean Queue pagamentoErroQueue()    { return QueueBuilder.durable(erroQueue).build(); }

    // ---------- Bindings (routing keys) ----------
    @Bean Binding requestBinding() {
        return BindingBuilder.bind(pagamentoRequestQueue()).to(pagamentoRequestExchange()).with(requestRoutingKey);
    }
    @Bean Binding sucessoBinding() {
        return BindingBuilder.bind(pagamentoSucessoQueue()).to(pagamentoSucessoExchange()).with(sucessoRoutingKey);
    }
    @Bean Binding erroBinding() {
        return BindingBuilder.bind(pagamentoErroQueue()).to(pagamentoErroExchange()).with(erroRoutingKey);
    }

    /**
     * Mensagens trafegam em JSON. "alwaysConvertToInferredType" faz o Spring usar o tipo do
     * parâmetro do @RabbitListener, então o JSON pode vir de outro projeto (worker) ou ser
     * publicado manualmente pelo dashboard do RabbitMQ.
     */
    @Bean
    MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        converter.setAlwaysConvertToInferredType(true);
        return converter;
    }
}
