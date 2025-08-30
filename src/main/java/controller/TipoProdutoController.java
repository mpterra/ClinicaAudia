package controller;

import dao.TipoProdutoDAO;
import model.TipoProduto;

import java.sql.SQLException;
import java.util.List;

public class TipoProdutoController {

    private final TipoProdutoDAO dao;

    public TipoProdutoController() {
        this.dao = new TipoProdutoDAO();
    }

    // -----------------------------
    // CREATE
    // -----------------------------
    public TipoProduto salvar(TipoProduto tipoProduto) throws SQLException {
        return dao.salvar(tipoProduto);
    }

    // -----------------------------
    // READ
    // -----------------------------
    public TipoProduto buscarPorId(int id) throws SQLException {
        return dao.buscarPorId(id);
    }

    public List<TipoProduto> listarTodos() throws SQLException {
        return dao.listarTodos();
    }

    public List<TipoProduto> buscarPorNomeLike(String termo) throws SQLException {
        return dao.buscarPorNomeLike(termo);
    }

    // -----------------------------
    // UPDATE
    // -----------------------------
    public boolean atualizar(TipoProduto tipoProduto) throws SQLException {
        return dao.atualizar(tipoProduto);
    }

    // -----------------------------
    // DELETE
    // -----------------------------
    public boolean deletar(int id) throws SQLException {
        return dao.deletar(id);
    }
}
