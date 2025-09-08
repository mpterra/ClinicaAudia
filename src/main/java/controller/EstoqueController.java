package controller;

import dao.EstoqueDAO;
import model.Estoque;

import java.sql.SQLException;
import java.util.List;

public class EstoqueController {

    private final EstoqueDAO dao;

    public EstoqueController() {
        this.dao = new EstoqueDAO();
    }

    public boolean salvarOuAtualizarEstoque(Estoque estoque, String usuarioLogado) throws SQLException {
        if (estoque.getQuantidade() < 0) {
            throw new IllegalArgumentException("Quantidade não pode ser negativa.");
        }
        if (estoque.getEstoqueMinimo() < 0) {
            throw new IllegalArgumentException("Estoque mínimo não pode ser negativo.");
        }
        return dao.salvarOuAtualizar(estoque, usuarioLogado);
    }

    public Estoque buscarPorProdutoId(int produtoId) throws SQLException {
        return dao.buscarPorProdutoId(produtoId);
    }

    public List<Estoque> listarTodos() throws SQLException {
        return dao.listarTodos();
    }

    public boolean removerEstoque(int produtoId) throws SQLException {
        return dao.deletar(produtoId);
    }
}
