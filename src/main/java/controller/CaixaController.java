package controller;

import dao.CaixaDAO;
import model.Caixa;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class CaixaController {

    private final CaixaDAO caixaDAO;

    public CaixaController() {
        this.caixaDAO = new CaixaDAO();
    }

    // -------------------------
    // Ações principais
    // -------------------------

    // Abre novo caixa
    public boolean abrirCaixa(Caixa caixa) throws SQLException {
        validarCaixaAbertura(caixa);
        if (existeCaixaAberto()) {
            throw new IllegalStateException("Já existe um caixa aberto. Feche-o antes de abrir outro.");
        }
        return caixaDAO.inserir(caixa);
    }

    // Atualiza caixa aberto
    public void atualizarCaixa(Caixa caixa) throws SQLException {
        validarCaixaAbertura(caixa);
        if (caixa.getId() <= 0) {
            throw new IllegalArgumentException("ID de caixa inválido.");
        }

        Caixa existente = caixaDAO.buscarPorId(caixa.getId());
        if (existente == null) throw new IllegalArgumentException("Caixa não encontrado.");
        if (existente.isFechado()) {
            throw new IllegalStateException("Não é possível atualizar um caixa já fechado.");
        }
        caixaDAO.atualizar(caixa);
    }

    // Fecha caixa
    public boolean fecharCaixa(Caixa caixa) throws SQLException {
        if (caixa == null || caixa.getId() <= 0) throw new IllegalArgumentException("Caixa inválido.");

        Caixa existente = caixaDAO.buscarPorId(caixa.getId());
        if (existente == null) throw new IllegalArgumentException("Caixa não encontrado.");
        if (existente.isFechado()) {
            throw new IllegalStateException("Caixa já está fechado.");
        }

        validarCaixaFechamento(caixa);
        return caixaDAO.fecharCaixa(caixa);
    }

    // Deleta caixa (somente aberto)
    public void deletarCaixa(int id) throws SQLException {
        Caixa existente = caixaDAO.buscarPorId(id);
        if (existente == null) throw new IllegalArgumentException("Caixa não encontrado.");
        if (existente.isFechado()) {
            throw new IllegalStateException("Não é permitido deletar caixas já fechados.");
        }
        caixaDAO.deletar(id);
    }

    // -------------------------
    // Consultas
    // -------------------------
    public Caixa buscarPorId(int id) throws SQLException {
        return (id > 0) ? caixaDAO.buscarPorId(id) : null;
    }

    public List<Caixa> listarTodos() throws SQLException {
        return caixaDAO.listarTodos();
    }

    public boolean existeCaixaAberto() throws SQLException {
        return caixaDAO.buscarCaixaAberto() != null;
    }

    public Caixa getCaixaAberto() throws SQLException {
        return caixaDAO.buscarCaixaAberto();
    }

    // -------------------------
    // Validações privadas
    // -------------------------

    // Valida saldos e dados obrigatórios na abertura
    private void validarCaixaAbertura(Caixa caixa) {
        if (caixa == null) throw new IllegalArgumentException("Caixa não pode ser nulo.");
        if (caixa.getDataAbertura() == null) throw new IllegalArgumentException("Data de abertura não pode ser nula.");
        validarSaldoPositivo(caixa.getSaldoInicialDinheiro(), "Saldo inicial em dinheiro");
        validarSaldoPositivo(caixa.getSaldoInicialDebito(), "Saldo inicial em cartão débito");
        validarSaldoPositivo(caixa.getSaldoInicialCredito(), "Saldo inicial em cartão crédito");
        validarSaldoPositivo(caixa.getSaldoInicialPix(), "Saldo inicial em PIX");
        if (caixa.getUsuario() == null || caixa.getUsuario().trim().isEmpty()) {
            throw new IllegalArgumentException("Usuário não pode ser nulo ou vazio.");
        }
    }

    // Valida saldos finais no fechamento
    private void validarCaixaFechamento(Caixa caixa) {
        validarSaldoPositivo(caixa.getSaldoFinalDinheiro(), "Saldo final em dinheiro");
        validarSaldoPositivo(caixa.getSaldoFinalDebito(), "Saldo final em cartão débito");
        validarSaldoPositivo(caixa.getSaldoFinalCredito(), "Saldo final em cartão crédito");
        validarSaldoPositivo(caixa.getSaldoFinalPix(), "Saldo final em PIX");
    }

    // Validação genérica de BigDecimal
    private void validarSaldoPositivo(BigDecimal valor, String campo) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(campo + " deve ser não negativo.");
        }
    }
}
