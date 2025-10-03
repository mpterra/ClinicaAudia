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

    // Registra um novo pagamento
    public boolean registrarPagamento(PagamentoCompra pagamento, String usuarioLogado) throws SQLException {
        if (pagamento == null) throw new IllegalArgumentException("Pagamento nulo.");
        if (pagamento.getValor() == null || pagamento.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor do pagamento inválido.");
        }
        pagamento.setUsuario(usuarioLogado);
        return dao.salvar(pagamento, usuarioLogado);
    }

    // Atualiza um pagamento existente
    public boolean atualizarPagamento(PagamentoCompra pagamento, String usuarioLogado) throws SQLException {
        if (pagamento == null) throw new IllegalArgumentException("Pagamento nulo.");
        if (pagamento.getId() <= 0) throw new IllegalArgumentException("ID do pagamento inválido.");
        if (pagamento.getValor() == null || pagamento.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor do pagamento inválido.");
        }
        pagamento.setUsuario(usuarioLogado);
        return dao.atualizar(pagamento, usuarioLogado);
    }

    // Exclui um pagamento por ID
    public boolean excluirPagamento(int id) throws SQLException {
        if (id <= 0) throw new IllegalArgumentException("ID do pagamento inválido.");
        return dao.deletar(id);
    }

    // Busca um pagamento por ID
    public PagamentoCompra buscarPorId(int id) throws SQLException {
        if (id <= 0) throw new IllegalArgumentException("ID do pagamento inválido.");
        return dao.buscarPorId(id);
    }

    // Lista pagamentos por compra
    public List<PagamentoCompra> listarPorCompra(int compraId) throws SQLException {
        if (compraId <= 0) throw new IllegalArgumentException("ID da compra inválido.");
        return dao.listarPorCompra(compraId);
    }

    // Lista todos os pagamentos
    public List<PagamentoCompra> listarTodos() throws SQLException {
        return dao.listarTodos();
    }

    // Lista pagamentos pendentes
    public List<PagamentoCompra> listarPagamentosPendentes() throws SQLException {
        return dao.listarPagamentosPendentes();
    }

    // Lista pagamentos pendentes de boleto
    public List<PagamentoCompra> listarPagamentosPendentesBoleto() throws SQLException {
        return dao.listarPagamentosPendentesBoleto();
    }

    // Cancela todos os pagamentos associados a uma compra
    public boolean cancelarPagamentosPorCompra(int compraId, String usuarioLogado) throws SQLException {
        if (compraId <= 0) {
            throw new IllegalArgumentException("ID da compra inválido.");
        }
        if (usuarioLogado == null || usuarioLogado.isBlank()) {
            throw new IllegalArgumentException("Usuário é obrigatório.");
        }
        List<PagamentoCompra> pagamentos = listarPorCompra(compraId);
        boolean sucesso = true;
        for (PagamentoCompra pagamento : pagamentos) {
            if (!dao.deletar(pagamento.getId())) {
                sucesso = false;
            }
        }
        return sucesso;
    }
}