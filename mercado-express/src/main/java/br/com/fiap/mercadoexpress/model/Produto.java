package br.com.fiap.mercadoexpress.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "TDS_Sec_MVC_TB_Mercado")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Informe o nome")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nome;

    @NotBlank(message = "Informe a categoria")
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String categoria;

    @Size(max = 255)
    private String descricao;

    @NotNull(message = "Informe o preço de custo")
    @DecimalMin(value = "0.00", message = "Preço de custo não pode ser negativo")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precoCusto;

    @NotNull(message = "Informe o preço de venda")
    @DecimalMin(value = "0.01", message = "Preço de venda deve ser maior que zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precoVenda;

    @NotNull(message = "Informe o estoque")
    @Min(value = 0, message = "Estoque não pode ser negativo")
    @Column(nullable = false)
    private Integer estoque;

    @NotBlank(message = "Informe a unidade")
    @Size(max = 10)
    @Column(nullable = false, length = 10)
    private String unidade; // kg, un, bandeja...

    /** N Produtos -> 1 Fornecedor (FK fornecedor_id). */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;
}
