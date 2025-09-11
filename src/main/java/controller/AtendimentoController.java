package controller;

import dao.AtendimentoDAO;
import model.Atendimento;
import model.Paciente;
import model.Profissional;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AtendimentoController {

    private final AtendimentoDAO dao;

    public AtendimentoController() {
        this.dao = new AtendimentoDAO();
    }

    public boolean criarAtendimento(Atendimento at, String usuarioLogado) throws SQLException {
        validarDuracao(at);
        validarDisponibilidade(at);
        return dao.salvar(at, usuarioLogado);
    }

    public boolean atualizarAtendimento(Atendimento at, String usuarioLogado) throws SQLException {
        validarDuracao(at);
        validarDisponibilidade(at);
        return dao.atualizar(at, usuarioLogado);
    }

    public boolean removerAtendimento(int id) throws SQLException {
        return dao.deletar(id);
    }

    public Atendimento buscarPorId(int id) throws SQLException {
        return dao.buscarPorId(id);
    }

    public List<Atendimento> listarTodos() throws SQLException {
        return dao.listarTodos();
    }

    public List<Atendimento> listarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        return dao.listarPorPeriodo(inicio, fim);
    }

    private void validarDuracao(Atendimento at) {
        if (at.getDuracaoMin() <= 0) {
            throw new IllegalArgumentException("Duração deve ser maior que zero.");
        }
    }

    private void validarDisponibilidade(Atendimento at) throws SQLException {
        Profissional prof = at.getProfissional();
        if (prof == null) {
            throw new IllegalArgumentException("Profissional não selecionado.");
        }

        boolean disponivel = dao.isDisponivel(
                prof.getId(),
                at.getDataHora(),
                at.getDuracaoMin()
        );

        if (!disponivel) {
            throw new IllegalArgumentException("Horário indisponível para o profissional.");
        }
    }
}