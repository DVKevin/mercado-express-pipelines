package br.com.fiap.mercadoexpress.repository;

import br.com.fiap.mercadoexpress.model.Pedido;
import br.com.fiap.mercadoexpress.model.StatusPedido;
import br.com.fiap.mercadoexpress.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByUsuarioOrderByCriadoEmDesc(Usuario usuario);
    List<Pedido> findAllByOrderByCriadoEmDesc();
    List<Pedido> findByStatus(StatusPedido status);
}
