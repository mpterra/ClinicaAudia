package controller;

import dao.EvolucaoAtendimentoDAO;
import model.EvolucaoAtendimento;
import model.Atendimento;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class EvolucaoAtendimentoController {

    private final EvolucaoAtendimentoDAO dao;

    public EvolucaoAtendimentoController() {
        this.dao = new EvolucaoAtendimentoDAO();
    }

    // -------------------------
    // Inserir evolução
    // -------------------------
    public void adicionarEvolucao(EvolucaoAtendimento evolucao) throws SQLException {
        if (evolucao == null) throw new IllegalArgumentException("Evolução não pode ser nula.");
        if (evolucao.getAtendimento() == null || evolucao.getAtendimento().getId() <= 0) {
            throw new IllegalArgumentException("Atendimento inválido.");
        }
        dao.insert(evolucao);
    }

    // -------------------------
    // Atualizar evolução
    // -------------------------
    public void atualizarEvolucao(EvolucaoAtendimento evolucao) throws SQLException {
        if (evolucao == null || evolucao.getId() <= 0) {
            throw new IllegalArgumentException("Evolução inválida.");
        }
        dao.update(evolucao);
    }

    // -------------------------
    // Deletar evolução
    // -------------------------
    public void deletarEvolucao(int id) throws SQLException {
        EvolucaoAtendimento e = dao.findById(id);
        if (e == null) throw new IllegalArgumentException("Evolução não encontrada.");
        dao.delete(id);
    }

    // -------------------------
    // Consultas
    // -------------------------
    public EvolucaoAtendimento buscarPorId(int id) throws SQLException {
        if (id <= 0) return null;
        return dao.findById(id);
    }

    public List<EvolucaoAtendimento> listarPorAtendimento(Atendimento atendimento) throws SQLException {
        if (atendimento == null || atendimento.getId() <= 0) return List.of();
        return dao.findByAtendimento(atendimento);
    }

    public List<EvolucaoAtendimento> listarPorUsuario(String usuario) throws SQLException {
        if (usuario == null || usuario.isEmpty()) return List.of();
        return dao.findByUsuario(usuario);
    }

    public List<EvolucaoAtendimento> listarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        if (inicio == null || fim == null || inicio.isAfter(fim)) return List.of();
        return dao.findByDateRange(inicio, fim);
    }
}
