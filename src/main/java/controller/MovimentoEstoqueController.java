package controller;

import dao.MovimentoEstoqueDAO;
import model.MovimentoEstoque;

import java.sql.SQLException;
import java.util.List;

public class MovimentoEstoqueController {

    private final MovimentoEstoqueDAO dao;

    public MovimentoEstoqueController() {
        this.dao = new MovimentoEstoqueDAO();
    }

    public boolean registrarMovimento(MovimentoEstoque mov, String usuarioLogado) throws SQLException {
        if (mov.getQuantidade() <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        }
        return dao.salvar(mov, usuarioLogado);
    }

    public MovimentoEstoque buscarPorId(int id) throws SQLException {
        return dao.buscarPorId(id);
    }

    public List<MovimentoEstoque> listarTodos() throws SQLException {
        return dao.listarTodos();
    }

    public List<MovimentoEstoque> listarPorProduto(int produtoId) throws SQLException {
        return dao.listarPorProduto(produtoId);
    }

    public boolean removerMovimento(int id) throws SQLException {
        return dao.deletar(id);
    }
}
