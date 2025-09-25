package controller;

import dao.VendaProdutoDAO;
import model.VendaProduto;

import java.sql.SQLException;
import java.util.List;

public class VendaProdutoController {

    private final VendaProdutoDAO dao;

    public VendaProdutoController() {
        this.dao = new VendaProdutoDAO();
    }

    public boolean adicionarProdutoVenda(VendaProduto vp) throws SQLException {
        if (vp.getQuantidade() <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        }
        if (vp.getPrecoUnitario().doubleValue() <= 0) {
            throw new IllegalArgumentException("Preço unitário deve ser maior que zero.");
        }
        return dao.salvar(vp);
    }

    public List<VendaProduto> listarPorVenda(int vendaId) throws SQLException {
        return dao.listarPorVenda(vendaId);
    }

    public List<VendaProduto> listarPorProduto(int produtoId) throws SQLException {
        return dao.listarPorProduto(produtoId);
    }

    public boolean removerProdutoVenda(int vendaId, int produtoId) throws SQLException {
        return dao.deletar(vendaId, produtoId);
    }

	public boolean serialExiste(String codigoSerial) {
		return dao.serialExiste(codigoSerial);
	}
}
