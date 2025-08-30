package controller;

import dao.PagamentoAtendimentoDAO;
import model.PagamentoAtendimento;
import model.Atendimento;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class PagamentoAtendimentoController {

    private final PagamentoAtendimentoDAO pagamentoDAO;

    public PagamentoAtendimentoController() {
        this.pagamentoDAO = new PagamentoAtendimentoDAO();
    }

    // -----------------------------
    // Salvar pagamento (insert ou update)
    // -----------------------------
    public void salvarPagamento(PagamentoAtendimento pagamento) throws SQLException {
        validarPagamento(pagamento);
        if (pagamento.getId() == 0) {
            pagamentoDAO.insert(pagamento);
        } else {
            pagamentoDAO.update(pagamento);
        }
    }

    // -----------------------------
    // Deletar pagamento
    // -----------------------------
    public void deletarPagamento(int id) throws SQLException {
        pagamentoDAO.delete(id);
    }

    // -----------------------------
    // Buscar pagamento por ID
    // -----------------------------
    public PagamentoAtendimento buscarPorId(int id) throws SQLException {
        return pagamentoDAO.findById(id);
    }

    // -----------------------------
    // Buscar pagamentos de um atendimento específico
    // -----------------------------
    public List<PagamentoAtendimento> buscarPorAtendimento(Atendimento atendimento) throws SQLException {
        if (atendimento == null || atendimento.getId() <= 0) {
            throw new IllegalArgumentException("Atendimento inválido.");
        }
        return pagamentoDAO.findByAtendimento(atendimento);
    }

    // -----------------------------
    // Listar todos os pagamentos
    // -----------------------------
    public List<PagamentoAtendimento> listarTodos() throws SQLException {
        return pagamentoDAO.findAll();
    }

    // -----------------------------
    // Validações básicas
    // -----------------------------
    private void validarPagamento(PagamentoAtendimento pagamento) {
        if (pagamento.getAtendimento() == null || pagamento.getAtendimento().getId() <= 0) {
            throw new IllegalArgumentException("Atendimento obrigatório para o pagamento.");
        }
        if (pagamento.getValor() == null || pagamento.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor do pagamento deve ser maior que zero.");
        }
        if (pagamento.getMetodoPagamento() == null) {
            throw new IllegalArgumentException("Método de pagamento obrigatório.");
        }
        if (pagamento.getUsuario() == null || pagamento.getUsuario().isBlank()) {
            throw new IllegalArgumentException("Usuário é obrigatório.");
        }
    }
}
