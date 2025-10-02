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

    // ============================
    // CREATE
    // ============================
    public boolean adicionarProdutoCompra(CompraProduto cp) throws SQLException {
        if (cp == null) {
            throw new IllegalArgumentException("Objeto CompraProduto não pode ser nulo.");
        }
        if (cp.getQuantidade() <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        }
        if (cp.getPrecoUnitario() == null || cp.getPrecoUnitario().doubleValue() <= 0) {
            throw new IllegalArgumentException("Preço unitário deve ser maior que zero.");
        }
        // fornecedorId pode ser nulo (campo opcional)
        return dao.salvar(cp);
    }

    // ============================
    // READ
    // ============================
    public List<CompraProduto> listarPorCompra(int compraId) throws SQLException {
        if (compraId <= 0) {
            throw new IllegalArgumentException("ID da compra inválido.");
        }
        return dao.listarPorCompra(compraId);
    }

    public List<CompraProduto> listarPorProduto(int produtoId) throws SQLException {
        if (produtoId <= 0) {
            throw new IllegalArgumentException("ID do produto inválido.");
        }
        return dao.listarPorProduto(produtoId);
    }

    // ============================
    // DELETE
    // ============================
    public boolean removerProdutoCompra(int compraId, int produtoId) throws SQLException {
        if (compraId <= 0 || produtoId <= 0) {
            throw new IllegalArgumentException("IDs de compra e produto devem ser válidos.");
        }
        return dao.deletar(compraId, produtoId);
    }
}
