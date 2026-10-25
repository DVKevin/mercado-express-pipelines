package br.com.fiap.mercadoexpress;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableRabbit
@SpringBootApplication
public class MercadoExpressApplication {
    public static void main(String[] args) {
        SpringApplication.run(MercadoExpressApplication.class, args);
    }
}
