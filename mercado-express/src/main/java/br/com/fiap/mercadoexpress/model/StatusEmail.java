package br.com.fiap.mercadoexpress.model;

public enum StatusEmail {
    SENT,       // enviado pelo SMTP
    ERROR,      // falhou no SMTP
    SIMULATED   // SMTP não configurado: registrado só no BD
}
