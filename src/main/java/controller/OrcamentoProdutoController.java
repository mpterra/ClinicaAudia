package controller;

import dao.OrcamentoProdutoDAO;
import model.OrcamentoProduto;

import java.sql.SQLException;
import java.util.List;

public class OrcamentoProdutoController {

    private final OrcamentoProdutoDAO dao;

    public OrcamentoProdutoController() {
        this.dao = new OrcamentoProdutoDAO();
    }

    public boolean adicionarProdutoOrcamento(OrcamentoProduto op) throws SQLException {
        if (op.getQuantidade() <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        }
        if (op.getPrecoUnitario().doubleValue() <= 0) {
            throw new IllegalArgumentException("Preço unitário deve ser maior que zero.");
        }
        return dao.salvar(op);
    }

    public List<OrcamentoProduto> listarPorOrcamento(int orcamentoId) throws SQLException {
        return dao.listarPorOrcamento(orcamentoId);
    }

    public List<OrcamentoProduto> listarPorProduto(int produtoId) throws SQLException {
        return dao.listarPorProduto(produtoId);
    }

    public boolean removerProdutoOrcamento(int orcamentoId, int produtoId) throws SQLException {
        return dao.deletar(orcamentoId, produtoId);
    }
}
