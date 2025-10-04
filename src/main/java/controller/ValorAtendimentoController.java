package controller;

import dao.ValorAtendimentoDAO;
import model.ValorAtendimento;

import java.sql.SQLException;
import java.util.List;

public class ValorAtendimentoController {

    private final ValorAtendimentoDAO valorAtendimentoDAO;

    public ValorAtendimentoController() {
        this.valorAtendimentoDAO = new ValorAtendimentoDAO();
    }

    // ============================
    // CREATE
    // ============================
    public boolean salvar(ValorAtendimento valorAtendimento, String usuarioLogado) throws SQLException {
        if (valorAtendimento == null) {
            throw new IllegalArgumentException("ValorAtendimento não pode ser nulo.");
        }
        return valorAtendimentoDAO.salvar(valorAtendimento, usuarioLogado);
    }

    // ============================
    // READ
    // ============================
    public ValorAtendimento buscarPorId(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID inválido.");
        }
        return valorAtendimentoDAO.buscarPorId(id);
    }

    public List<ValorAtendimento> listarTodos() throws SQLException {
        return valorAtendimentoDAO.listarTodos();
    }

    public List<ValorAtendimento> listarPorProfissional(int profissionalId) throws SQLException {
        if (profissionalId <= 0) {
            throw new IllegalArgumentException("ProfissionalId inválido.");
        }
        return valorAtendimentoDAO.listarPorProfissional(profissionalId);
    }

    public ValorAtendimento buscarPorProfissionalETipo(int profissionalId, ValorAtendimento.Tipo tipo) throws SQLException {
        if (profissionalId <= 0) {
            throw new IllegalArgumentException("ProfissionalId inválido.");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo inválido.");
        }
        return valorAtendimentoDAO.buscarPorProfissionalETipo(profissionalId, tipo);
    }

    // ============================
    // UPDATE
    // ============================
    public boolean atualizar(ValorAtendimento valorAtendimento, String usuarioLogado) throws SQLException {
        if (valorAtendimento == null || valorAtendimento.getId() <= 0) {
            throw new IllegalArgumentException("ValorAtendimento inválido para atualização.");
        }
        return valorAtendimentoDAO.atualizar(valorAtendimento, usuarioLogado);
    }

    // ============================
    // DELETE
    // ============================
    public boolean deletar(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID inválido para exclusão.");
        }
        return valorAtendimentoDAO.deletar(id);
    }
}