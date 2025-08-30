package controller;

import dao.UsuarioDAO;
import model.Usuario;

import java.sql.SQLException;
import java.util.List;

public class UsuarioController {

    private final UsuarioDAO dao;

    public UsuarioController() {
        this.dao = new UsuarioDAO();
    }

    // ============================
    // CREATE
    // ============================
    public Usuario salvar(Usuario usuario) throws SQLException {
        return dao.salvar(usuario);
    }

    // ============================
    // READ
    // ============================
    public Usuario buscarPorId(int id) throws SQLException {
        return dao.buscarPorId(id);
    }

    public Usuario buscarPorLogin(String login) throws SQLException {
        return dao.buscarPorLogin(login);
    }

    public List<Usuario> listarTodos() throws SQLException {
        return dao.listarTodos();
    }

    // ============================
    // UPDATE
    // ============================
    public boolean atualizar(Usuario usuario) throws SQLException {
        return dao.atualizar(usuario);
    }

    // ============================
    // DELETE
    // ============================
    public boolean deletar(int id) throws SQLException {
        return dao.deletar(id);
    }
}
