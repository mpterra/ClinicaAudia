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

    public boolean criarCompra(Compra compra, String usuarioLogado) throws SQLException {
        if (compra.getFornecedor() == null || compra.getFornecedor().isBlank()) {
            throw new IllegalArgumentException("Fornecedor é obrigatório.");
        }
        return dao.salvar(compra, usuarioLogado);
    }

    public Compra buscarPorId(int id) throws SQLException {
        return dao.buscarPorId(id);
    }

    public List<Compra> listarTodos() throws SQLException {
        return dao.listarTodos();
    }

    public boolean removerCompra(int id) throws SQLException {
        return dao.deletar(id);
    }
}
