package controller;

import dao.PagamentoCompraDAO;
import model.PagamentoCompra;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class PagamentoCompraController {

    private final PagamentoCompraDAO dao;

    public PagamentoCompraController() {
        this.dao = new PagamentoCompraDAO();
    }

    public boolean registrarPagamento(PagamentoCompra pagamento, String usuarioLogado) throws SQLException {
        if (pagamento == null) throw new IllegalArgumentException("Pagamento nulo.");
        if (pagamento.getValor() == null || pagamento.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor do pagamento inválido.");
        }
        // garante que o usuário esteja no objeto (útil para logs/retorno)
        pagamento.setUsuario(usuarioLogado);
        return dao.salvar(pagamento, usuarioLogado);
    }

    public boolean atualizarPagamento(PagamentoCompra pagamento, String usuarioLogado) throws SQLException {
        if (pagamento == null) throw new IllegalArgumentException("Pagamento nulo.");
        if (pagamento.getId() <= 0) throw new IllegalArgumentException("ID do pagamento inválido.");
        if (pagamento.getValor() == null || pagamento.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor do pagamento inválido.");
        }
        pagamento.setUsuario(usuarioLogado);
        return dao.atualizar(pagamento, usuarioLogado);
    }

    public boolean excluirPagamento(int id) throws SQLException {
        if (id <= 0) throw new IllegalArgumentException("ID do pagamento inválido.");
        return dao.deletar(id);
    }

    public PagamentoCompra buscarPorId(int id) throws SQLException {
        if (id <= 0) throw new IllegalArgumentException("ID do pagamento inválido.");
        return dao.buscarPorId(id);
    }

    public List<PagamentoCompra> listarPorCompra(int compraId) throws SQLException {
        if (compraId <= 0) throw new IllegalArgumentException("ID da compra inválido.");
        return dao.listarPorCompra(compraId);
    }

    public List<PagamentoCompra> listarTodos() throws SQLException {
        return dao.listarTodos();
    }
}
