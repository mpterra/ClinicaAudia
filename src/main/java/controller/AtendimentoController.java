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
        validar(at, null);
        return dao.salvar(at, usuarioLogado);
    }

    // ===========================
    // ATUALIZAR ATENDIMENTO
    // ===========================
    public boolean atualizarAtendimento(Atendimento at, String usuarioLogado) throws SQLException {
        validar(at, at.getId());
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
    // VALIDAÇÕES INTERNAS
    // ===========================
    private void validar(Atendimento at, Integer idAtual) throws SQLException {
        validarDuracao(at);
        validarProfissional(at);
        validarDisponibilidade(at, idAtual);
    }

    private void validarDuracao(Atendimento at) {
        if (at.getDuracaoMin() <= 0) {
            throw new IllegalArgumentException("A duração do atendimento deve ser maior que zero.");
        }
    }

    private void validarProfissional(Atendimento at) {
        Profissional prof = at.getProfissional();
        if (prof == null || prof.getId() == 0) {
            throw new IllegalArgumentException("Profissional não selecionado ou inválido.");
        }
    }

    private void validarDisponibilidade(Atendimento at, Integer idAtual) throws SQLException {
        boolean disponivel = dao.isDisponivel(
                at.getProfissional().getId(),
                at.getDataHora(),
                at.getDuracaoMin(),
                idAtual
        );

        if (!disponivel) {
            throw new IllegalArgumentException("Horário indisponível para o profissional.");
        }
    }
}
