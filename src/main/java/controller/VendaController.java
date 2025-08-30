package controller;

import dao.VendaDAO;
import model.Venda;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class VendaController {

    private final VendaDAO dao;

    public VendaController() {
        this.dao = new VendaDAO();
    }

    // ============================
    // CREATE
    // ============================
    public Venda salvar(Venda venda) throws SQLException {
        return dao.salvar(venda);
    }

    // ============================
    // READ
    // ============================
    public Venda buscarPorId(int id) throws SQLException {
        return dao.buscarPorId(id);
    }

    public List<Venda> listarTodos() throws SQLException {
        return dao.listarTodos();
    }

    public List<Venda> buscarPorUsuario(String usuario) throws SQLException {
        return dao.buscarPorUsuario(usuario);
    }

    public List<Venda> listarPorPaciente(int pacienteId) throws SQLException {
        return dao.listarPorPaciente(pacienteId);
    }

    public List<Venda> listarPorData(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        return dao.listarPorData(inicio, fim);
    }

    public List<Venda> listarPorPacienteEData(int pacienteId, LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        return dao.listarPorPacienteEData(pacienteId, inicio, fim);
    }

    // ============================
    // UPDATE
    // ============================
    public boolean atualizar(Venda venda) throws SQLException {
        return dao.atualizar(venda);
    }

    // ============================
    // DELETE
    // ============================
    public boolean deletar(int id) throws SQLException {
        return dao.deletar(id);
    }
}
