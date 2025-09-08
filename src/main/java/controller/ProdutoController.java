package controller;

import dao.ProdutoDAO;
import model.Produto;

import java.sql.SQLException;
import java.util.List;

public class ProdutoController {

    private final ProdutoDAO dao;

    public ProdutoController() {
        this.dao = new ProdutoDAO();
    }

    public boolean criarProduto(Produto produto, String usuarioLogado) throws SQLException {
        if (produto.getNome() == null || produto.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome do produto é obrigatório.");
        }
        return dao.salvar(produto, usuarioLogado);
    }

    public boolean atualizarProduto(Produto produto, String usuarioLogado) throws SQLException {
        if (produto.getNome() == null || produto.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome do produto é obrigatório.");
        }
        return dao.atualizar(produto, usuarioLogado);
    }

    public boolean removerProduto(int id) throws SQLException {
        return dao.deletar(id);
    }

    public Produto buscarPorId(int id) throws SQLException {
        return dao.buscarPorId(id);
    }

    public List<Produto> listarTodos() throws SQLException {
        return dao.listarTodos();
    }
}
