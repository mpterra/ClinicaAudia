package controller;

import dao.CaixaDAO;
import model.Caixa;

import java.sql.SQLException;
import java.util.List;

public class CaixaController {

    private final CaixaDAO caixaDAO;

    public CaixaController() {
        this.caixaDAO = new CaixaDAO();
    }

    // -------------------------
    // CRUD inteligente
    // -------------------------

    public void abrirCaixa(Caixa caixa) throws SQLException {
        if (caixa == null) throw new IllegalArgumentException("Caixa não pode ser nulo.");

        // Regra: não pode abrir novo caixa se já houver um aberto
        Caixa aberto = caixaDAO.buscarCaixaAberto();
        if (aberto != null) {
            throw new IllegalStateException("Já existe um caixa aberto (ID: " + aberto.getId() + "). Feche-o antes de abrir outro.");
        }

        caixaDAO.inserir(caixa);
    }

    public void atualizarCaixa(Caixa caixa) throws SQLException {
        if (caixa == null || caixa.getId() <= 0) throw new IllegalArgumentException("Caixa inválido.");

        // Regra: só pode atualizar caixa aberto
        Caixa existente = caixaDAO.buscarPorId(caixa.getId());
        if (existente == null) throw new IllegalArgumentException("Caixa não encontrado.");
        if (existente.getDataFechamento() != null) {
            throw new IllegalStateException("Não é possível atualizar um caixa já fechado.");
        }

        caixaDAO.atualizar(caixa);
    }

    public void fecharCaixa(Caixa caixa) throws SQLException {
        if (caixa == null || caixa.getId() <= 0) throw new IllegalArgumentException("Caixa inválido.");

        Caixa existente = caixaDAO.buscarPorId(caixa.getId());
        if (existente == null) throw new IllegalArgumentException("Caixa não encontrado.");
        if (existente.getDataFechamento() != null) {
            throw new IllegalStateException("Caixa já está fechado.");
        }

        caixaDAO.fecharCaixa(caixa);
    }

    public void deletarCaixa(int id) throws SQLException {
        Caixa existente = caixaDAO.buscarPorId(id);
        if (existente == null) throw new IllegalArgumentException("Caixa não encontrado.");
        if (existente.getDataFechamento() != null) {
            throw new IllegalStateException("Não é permitido deletar caixas já fechados.");
        }

        caixaDAO.deletar(id);
    }

    // -------------------------
    // Consultas
    // -------------------------
    public Caixa buscarPorId(int id) throws SQLException {
        if (id <= 0) return null;
        return caixaDAO.buscarPorId(id);
    }

    public List<Caixa> listarTodos() throws SQLException {
        return caixaDAO.listarTodos();
    }

    public Caixa buscarCaixaAberto() throws SQLException {
        return caixaDAO.buscarCaixaAberto();
    }
}
