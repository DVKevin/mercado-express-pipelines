package br.com.fiap.mercadoexpress.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Fornecedor dos produtos do Mercado Express.
 * Relacionamento: 1 Fornecedor -> N Produtos (FK fornecedor_id em TDS_Sec_MVC_TB_Mercado).
 */
@Entity
@Table(name = "TB_FORNECEDOR")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Fornecedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Informe o nome do fornecedor")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nome;

    @NotBlank(message = "Informe o CNPJ")
    @Pattern(regexp = "\\d{14}", message = "CNPJ deve ter 14 dígitos (somente números)")
    @Column(nullable = false, unique = true, length = 14)
    private String cnpj;

    @jakarta.validation.constraints.Email(message = "E-mail inválido")
    @Size(max = 120)
    @Column(length = 120)
    private String email;

    @Size(max = 20)
    @Column(length = 20)
    private String telefone;

    @NotBlank(message = "Informe a cidade")
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String cidade;
}
