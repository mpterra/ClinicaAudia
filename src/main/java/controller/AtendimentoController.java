package controller;

import dao.AtendimentoDAO;
import model.Atendimento;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class AtendimentoController {

    private final AtendimentoDAO dao;

    public AtendimentoController() {
        this.dao = new AtendimentoDAO();
    }

    public boolean criarAtendimento(Atendimento at, String usuarioLogado) throws SQLException {
        if (at.getDuracaoMin() <= 0) {
            throw new IllegalArgumentException("Duração deve ser maior que zero.");
        }
        return dao.salvar(at, usuarioLogado);
    }

    public boolean atualizarAtendimento(Atendimento at, String usuarioLogado) throws SQLException {
        if (at.getDuracaoMin() <= 0) {
            throw new IllegalArgumentException("Duração deve ser maior que zero.");
        }
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
}
