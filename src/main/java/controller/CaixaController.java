package controller;

import dao.CaixaDAO;
import model.Caixa;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

// Controller para operações de caixas
public class CaixaController {

    private final CaixaDAO caixaDAO;

    public CaixaController() {
        this.caixaDAO = new CaixaDAO();
    }

    // Abre novo caixa
    public boolean abrirCaixa(Caixa caixa) throws SQLException {
        if (caixa == null) throw new IllegalArgumentException("Caixa não pode ser nulo.");
        if (caixa.getDataAbertura() == null) throw new IllegalArgumentException("Data de abertura não pode ser nula.");
        if (caixa.getSaldoInicialDinheiro() == null || caixa.getSaldoInicialDinheiro().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Saldo inicial em dinheiro deve ser não negativo.");
        }
        if (caixa.getSaldoInicialDebito() == null || caixa.getSaldoInicialDebito().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Saldo inicial em cartão débito deve ser não negativo.");
        }
        if (caixa.getSaldoInicialCredito() == null || caixa.getSaldoInicialCredito().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Saldo inicial em cartão crédito deve ser não negativo.");
        }
        if (caixa.getSaldoInicialPix() == null || caixa.getSaldoInicialPix().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Saldo inicial em PIX deve ser não negativo.");
        }
        if (caixa.getUsuario() == null || caixa.getUsuario().trim().isEmpty()) {
            throw new IllegalArgumentException("Usuário não pode ser nulo ou vazio.");
        }
        if (existeCaixaAberto()) {
            throw new IllegalStateException("Já existe um caixa aberto. Feche-o antes de abrir outro.");
        }
        return caixaDAO.inserir(caixa);
    }

    // Atualiza caixa existente
    public void atualizarCaixa(Caixa caixa) throws SQLException {
        if (caixa == null || caixa.getId() <= 0) throw new IllegalArgumentException("Caixa inválido.");
        if (caixa.getSaldoInicialDinheiro() == null || caixa.getSaldoInicialDinheiro().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Saldo inicial em dinheiro deve ser não negativo.");
        }
        if (caixa.getSaldoInicialDebito() == null || caixa.getSaldoInicialDebito().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Saldo inicial em cartão débito deve ser não negativo.");
        }
        if (caixa.getSaldoInicialCredito() == null || caixa.getSaldoInicialCredito().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Saldo inicial em cartão crédito deve ser não negativo.");
        }
        if (caixa.getSaldoInicialPix() == null || caixa.getSaldoInicialPix().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Saldo inicial em PIX deve ser não negativo.");
        }
        if (caixa.getUsuario() == null || caixa.getUsuario().trim().isEmpty()) {
            throw new IllegalArgumentException("Usuário não pode ser nulo ou vazio.");
        }
        Caixa existente = caixaDAO.buscarPorId(caixa.getId());
        if (existente == null) throw new IllegalArgumentException("Caixa não encontrado.");
        if (existente.getDataFechamento() != null) {
            throw new IllegalStateException("Não é possível atualizar um caixa já fechado.");
        }
        caixaDAO.atualizar(caixa);
    }

    // Fecha caixa
    public boolean fecharCaixa(Caixa caixa) throws SQLException {
        if (caixa == null || caixa.getId() <= 0) throw new IllegalArgumentException("Caixa inválido.");
        Caixa existente = caixaDAO.buscarPorId(caixa.getId());
        if (existente == null) throw new IllegalArgumentException("Caixa não encontrado.");
        if (existente.getDataFechamento() != null) {
            throw new IllegalStateException("Caixa já está fechado.");
        }
        return caixaDAO.fecharCaixa(caixa);
    }

    // Deleta caixa
    public void deletarCaixa(int id) throws SQLException {
        Caixa existente = caixaDAO.buscarPorId(id);
        if (existente == null) throw new IllegalArgumentException("Caixa não encontrado.");
        if (existente.getDataFechamento() != null) {
            throw new IllegalStateException("Não é permitido deletar caixas já fechados.");
        }
        caixaDAO.deletar(id);
    }

    // Busca caixa por ID
    public Caixa buscarPorId(int id) throws SQLException {
        if (id <= 0) return null;
        return caixaDAO.buscarPorId(id);
    }

    // Lista todos os caixas
    public List<Caixa> listarTodos() throws SQLException {
        return caixaDAO.listarTodos();
    }

    // Verifica se existe caixa aberto
    public boolean existeCaixaAberto() throws SQLException {
        return caixaDAO.buscarCaixaAberto() != null;
    }

    // Obtém caixa aberto
    public Caixa getCaixaAberto() throws SQLException {
        return caixaDAO.buscarCaixaAberto();
    }
}