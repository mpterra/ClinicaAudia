package controller;

import dao.CaixaDAO;
import dao.CaixaMovimentoDAO;
import model.Caixa;
import model.CaixaMovimento;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

// Controller para operações de movimentos de caixa
public class CaixaMovimentoController {

    private final CaixaMovimentoDAO movimentoDAO;
    private final CaixaDAO caixaDAO;

    public CaixaMovimentoController() {
        this.movimentoDAO = new CaixaMovimentoDAO();
        this.caixaDAO = new CaixaDAO();
    }

    // Inserir novo movimento
    public void adicionarMovimento(CaixaMovimento movimento) throws SQLException {
        if (movimento == null) throw new IllegalArgumentException("Movimento não pode ser nulo.");
        if (movimento.getCaixa() == null || movimento.getCaixa().getId() <= 0) {
            throw new IllegalArgumentException("Movimento precisa de um caixa válido.");
        }
        if (movimento.getFormaPagamento() == null) {
            throw new IllegalArgumentException("Forma de pagamento não pode ser nula.");
        }
        if (movimento.getValor() == null || movimento.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor deve ser maior que zero.");
        }
        if (movimento.getTipo() == null) {
            throw new IllegalArgumentException("Tipo de movimento não pode ser nulo.");
        }
        if (movimento.getOrigem() == null) {
            throw new IllegalArgumentException("Origem do movimento não pode ser nula.");
        }
        if (movimento.getDataHora() == null) {
            throw new IllegalArgumentException("Data e hora não podem ser nulas.");
        }
        if (movimento.getUsuario() == null || movimento.getUsuario().trim().isEmpty()) {
            throw new IllegalArgumentException("Usuário não pode ser nulo ou vazio.");
        }

        Caixa caixa = caixaDAO.buscarPorId(movimento.getCaixa().getId());
        if (caixa == null) throw new IllegalArgumentException("Caixa não encontrado.");
        if (caixa.getDataFechamento() != null) {
            throw new IllegalStateException("Não é possível adicionar movimento a um caixa fechado.");
        }

        movimentoDAO.inserir(movimento);
    }

    // Buscar movimento por ID
    public CaixaMovimento buscarPorId(int id) throws SQLException {
        if (id <= 0) return null;
        return movimentoDAO.buscarPorId(id);
    }

    // Listar todos os movimentos
    public List<CaixaMovimento> listarTodos() throws SQLException {
        return movimentoDAO.listarTodos();
    }

    // Listar movimentos por caixa
    public List<CaixaMovimento> listarMovimentosPorCaixa(int caixaId) throws SQLException {
        if (caixaId <= 0) throw new IllegalArgumentException("ID do caixa inválido.");
        Caixa caixa = caixaDAO.buscarPorId(caixaId);
        if (caixa == null) throw new IllegalArgumentException("Caixa não encontrado.");
        return movimentoDAO.listarPorCaixa(caixa);
    }

    // Calcula saldos finais por forma de pagamento
    public BigDecimal[] calcularSaldosFinais(int caixaId) throws SQLException {
        if (caixaId <= 0) throw new IllegalArgumentException("ID do caixa inválido.");
        Caixa caixa = caixaDAO.buscarPorId(caixaId);
        if (caixa == null) throw new IllegalArgumentException("Caixa não encontrado.");
        return movimentoDAO.calcularSaldosFinais(caixaId);
    }

    // Deletar movimento
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