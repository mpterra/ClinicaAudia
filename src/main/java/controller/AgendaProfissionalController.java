package controller;

import dao.AgendaProfissionalDAO;
import model.AgendaProfissional;
import util.Database;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class AgendaProfissionalController {

    private final AgendaProfissionalDAO dao;

    public AgendaProfissionalController() {
        this.dao = new AgendaProfissionalDAO();
    }

    // ============================
    // INSERIR AGENDA
    // ============================
    public AgendaProfissional criarAgenda(AgendaProfissional agenda) throws SQLException {
        // validação simples: horários
        if (agenda.getDataHoraInicio().isAfter(agenda.getDataHoraFim())) {
            throw new IllegalArgumentException("Horário de início não pode ser depois do horário de fim");
        }
        dao.inserir(agenda);
        return agenda;
    }

    // ============================
    // ATUALIZAR AGENDA
    // ============================
    public void atualizarAgenda(AgendaProfissional agenda) throws SQLException {
        if (agenda.getDataHoraInicio().isAfter(agenda.getDataHoraFim())) {
            throw new IllegalArgumentException("Horário de início não pode ser depois do horário de fim");
        }
        dao.atualizar(agenda);
    }

    // ============================
    // DELETAR AGENDA
    // ============================
    public void removerAgenda(int id) throws SQLException {
        dao.deletar(id);
    }

    // ============================
    // BUSCAS
    // ============================

    public AgendaProfissional buscarPorId(int id) throws SQLException {
        return dao.buscarPorId(id);
    }

    public List<AgendaProfissional> listarPorProfissional(int profissionalId) throws SQLException {
        return dao.listarPorProfissional(profissionalId);
    }

    public List<AgendaProfissional> listarDisponiveis(int profissionalId) throws SQLException {
        return dao.listarDisponiveis(profissionalId);
    }

    public List<AgendaProfissional> listarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        return dao.listarPorPeriodo(inicio, fim);
    }

    public List<AgendaProfissional> listarPorProfissionalEPeriodo(int profissionalId, LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        return dao.listarPorProfissionalEPeriodo(profissionalId, inicio, fim);
    }

}
