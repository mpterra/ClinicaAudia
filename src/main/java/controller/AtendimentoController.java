package controller;

import dao.AtendimentoDAO;
import model.Atendimento;
import model.Profissional;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class AtendimentoController {

    private final AtendimentoDAO dao;

    public AtendimentoController() {
        this.dao = new AtendimentoDAO();
    }

    // ===========================
    // CRIAR ATENDIMENTO
    // ===========================
    public boolean criarAtendimento(Atendimento at, String usuarioLogado) throws SQLException {
        validarDuracao(at);
        validarDisponibilidade(at, null); // null = novo atendimento
        return dao.salvar(at, usuarioLogado);
    }

    // ===========================
    // ATUALIZAR ATENDIMENTO
    // ===========================
    public boolean atualizarAtendimento(Atendimento at, String usuarioLogado) throws SQLException {
        validarDuracao(at);
        validarDisponibilidade(at, at.getId()); // Passa ID atual para ignorar na verificação
        return dao.atualizar(at, usuarioLogado);
    }

    // ===========================
    // REMOVER ATENDIMENTO
    // ===========================
    public boolean removerAtendimento(int id) throws SQLException {
        return dao.deletar(id);
    }

    // ===========================
    // BUSCAR
    // ===========================
    public Atendimento buscarPorId(int id) throws SQLException {
        return dao.buscarPorId(id);
    }

    public List<Atendimento> listarTodos() throws SQLException {
        return dao.listarTodos();
    }

    public List<Atendimento> listarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        return dao.listarPorPeriodo(inicio, fim);
    }

    // ===========================
    // VALIDAÇÕES
    // ===========================
    private void validarDuracao(Atendimento at) {
        if (at.getDuracaoMin() <= 0) {
            throw new IllegalArgumentException("Duração deve ser maior que zero.");
        }
    }

    private void validarDisponibilidade(Atendimento at, Integer idAtual) throws SQLException {
        Profissional prof = at.getProfissional();
        if (prof == null) {
            throw new IllegalArgumentException("Profissional não selecionado.");
        }

        boolean disponivel = dao.isDisponivel(
                prof.getId(),
                at.getDataHora(),
                at.getDuracaoMin(),
                idAtual // Se for atualização, passa o ID atual
        );

        if (!disponivel) {
            throw new IllegalArgumentException("Horário indisponível para o profissional.");
        }
    }
}
