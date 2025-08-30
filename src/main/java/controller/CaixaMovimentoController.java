package controller;

import dao.CaixaDAO;
import dao.CaixaMovimentoDAO;
import model.Caixa;
import model.CaixaMovimento;

import java.sql.SQLException;
import java.util.List;

public class CaixaMovimentoController {

    private final CaixaMovimentoDAO movimentoDAO;
    private final CaixaDAO caixaDAO;

    public CaixaMovimentoController() {
        this.movimentoDAO = new CaixaMovimentoDAO();
        this.caixaDAO = new CaixaDAO();
    }

    // -------------------------
    // Inserir novo movimento
    // -------------------------
    public void adicionarMovimento(CaixaMovimento movimento) throws SQLException {
        if (movimento == null) throw new IllegalArgumentException("Movimento não pode ser nulo.");
        if (movimento.getCaixa() == null || movimento.getCaixa().getId() <= 0) {
            throw new IllegalArgumentException("Movimento precisa de um caixa válido.");
        }

        Caixa caixa = caixaDAO.buscarPorId(movimento.getCaixa().getId());
        if (caixa == null) throw new IllegalArgumentException("Caixa não encontrado.");
        if (caixa.getDataFechamento() != null) {
            throw new IllegalStateException("Não é possível adicionar movimento a um caixa fechado.");
        }

        movimentoDAO.inserir(movimento);
    }

    // -------------------------
    // Consultas
    // -------------------------
    public CaixaMovimento buscarPorId(int id) throws SQLException {
        if (id <= 0) return null;
        return movimentoDAO.buscarPorId(id);
    }

    public List<CaixaMovimento> listarTodos() throws SQLException {
        return movimentoDAO.listarTodos();
    }

    public List<CaixaMovimento> listarPorCaixa(Caixa caixa) throws SQLException {
        if (caixa == null || caixa.getId() <= 0) throw new IllegalArgumentException("Caixa inválido.");
        return movimentoDAO.listarPorCaixa(caixa);
    }

    // -------------------------
    // Deletar movimento
    // -------------------------
    public void deletarMovimento(int id) throws SQLException {
        CaixaMovimento movimento = movimentoDAO.buscarPorId(id);
        if (movimento == null) throw new IllegalArgumentException("Movimento não encontrado.");
        if (movimento.getCaixa() != null) {
            Caixa caixa = caixaDAO.buscarPorId(movimento.getCaixa().getId());
            if (caixa != null && caixa.getDataFechamento() != null) {
                throw new IllegalStateException("Não é permitido deletar movimentos de caixas fechados.");
            }
        }

        movimentoDAO.deletar(id);
    }
}
