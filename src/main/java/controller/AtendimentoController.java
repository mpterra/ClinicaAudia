package controller;

import dao.AtendimentoDAO;
import model.Atendimento;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class AtendimentoController {

    private final AtendimentoDAO atendimentoDAO;

    public AtendimentoController() {
        this.atendimentoDAO = new AtendimentoDAO();
    }

    // -------------------------
    // CREATE
    // -------------------------
    public void criarAtendimento(Atendimento atendimento) throws SQLException {
        // Aqui você pode colocar validações de negócio, exemplo:
        if (atendimento.getDataHora() == null) {
            throw new IllegalArgumentException("Data/Hora do atendimento não pode ser nula.");
        }
        atendimentoDAO.inserir(atendimento);
    }

    // -------------------------
    // UPDATE
    // -------------------------
    public void atualizarAtendimento(Atendimento atendimento) throws SQLException {
        if (atendimento.getId() <= 0) {
            throw new IllegalArgumentException("ID inválido para atualização.");
        }
        atendimentoDAO.atualizar(atendimento);
    }

    // -------------------------
    // DELETE
    // -------------------------
    public void removerAtendimento(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID inválido para remoção.");
        }
        atendimentoDAO.deletar(id);
    }

    // -------------------------
    // READ
    // -------------------------
    public Atendimento buscarPorId(int id) throws SQLException {
        if (id <= 0) return null;
        return atendimentoDAO.buscarPorId(id);
    }

    public List<Atendimento> listarPorPaciente(int pacienteId) throws SQLException {
        if (pacienteId <= 0) throw new IllegalArgumentException("ID do paciente inválido.");
        return atendimentoDAO.listarPorPaciente(pacienteId);
    }

    public List<Atendimento> listarPorProfissional(int profissionalId) throws SQLException {
        if (profissionalId <= 0) throw new IllegalArgumentException("ID do profissional inválido.");
        return atendimentoDAO.listarPorProfissional(profissionalId);
    }

    public List<Atendimento> listarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        if (inicio == null || fim == null) throw new IllegalArgumentException("Período inválido.");
        return atendimentoDAO.listarPorPeriodo(inicio, fim);
    }

    public List<Atendimento> listarPorSituacao(Atendimento.Situacao situacao) throws SQLException {
        if (situacao == null) throw new IllegalArgumentException("Situação inválida.");
        return atendimentoDAO.listarPorSituacao(situacao);
    }

}
