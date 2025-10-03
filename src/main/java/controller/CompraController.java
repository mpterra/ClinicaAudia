package controller;

import dao.CompraDAO;
import model.Compra;

import java.sql.SQLException;
import java.util.List;

public class CompraController {

    private final CompraDAO dao;

    public CompraController() {
        this.dao = new CompraDAO();
    }

    // ============================
    // CREATE
    // ============================
    // Cria uma nova compra
    public boolean criarCompra(Compra compra, String usuarioLogado) throws SQLException {
        if (usuarioLogado == null || usuarioLogado.isBlank()) {
            throw new IllegalArgumentException("Usuário é obrigatório.");
        }
        return dao.salvar(compra, usuarioLogado);
    }

    // ============================
    // READ
    // ============================
    // Busca uma compra por ID
    public Compra buscarPorId(int id) throws SQLException {
        return dao.buscarPorId(id);
    }

    // Lista todas as compras
    public List<Compra> listarTodos() throws SQLException {
        return dao.listarTodos();
    }

    // ============================
    // DELETE
    // ============================
    // Remove uma compra por ID
    public boolean removerCompra(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID da compra inválido.");
        }
        return dao.deletar(id);
    }

    // Cancela uma compra, marcando-a como inativa ou registrando o cancelamento
    public boolean cancelarCompra(int compraId, String usuarioLogado) throws SQLException {
        if (compraId <= 0) {
            throw new IllegalArgumentException("ID da compra inválido.");
        }
        if (usuarioLogado == null || usuarioLogado.isBlank()) {
            throw new IllegalArgumentException("Usuário é obrigatório.");
        }
        Compra compra = dao.buscarPorId(compraId);
        if (compra == null) {
            throw new SQLException("Compra não encontrada.");
        }
        return dao.cancelar(compraId, usuarioLogado); // Assume que o DAO tem um método cancelar
    }
}