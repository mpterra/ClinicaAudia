package controller;

import dao.CompraProdutoDAO;
import model.CompraProduto;

import java.sql.SQLException;
import java.util.List;

public class CompraProdutoController {

    private final CompraProdutoDAO dao;

    public CompraProdutoController() {
        this.dao = new CompraProdutoDAO();
    }

    public boolean adicionarProdutoCompra(CompraProduto cp) throws SQLException {
        if (cp.getQuantidade() <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        }
        if (cp.getPrecoUnitario().doubleValue() <= 0) {
            throw new IllegalArgumentException("Preço unitário deve ser maior que zero.");
        }
        return dao.salvar(cp);
    }

    public List<CompraProduto> listarPorCompra(int compraId) throws SQLException {
        return dao.listarPorCompra(compraId);
    }

    public List<CompraProduto> listarPorProduto(int produtoId) throws SQLException {
        return dao.listarPorProduto(produtoId);
    }

    public boolean removerProdutoCompra(int compraId, int produtoId) throws SQLException {
        return dao.deletar(compraId, produtoId);
    }
}
