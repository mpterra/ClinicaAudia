package controller;

import dao.VendaProdutoDAO;
import model.VendaProduto;

import java.sql.SQLException;
import java.util.List;

// Controlador para operações com itens de venda de produtos
public class VendaProdutoController {

    private final VendaProdutoDAO dao;

    public VendaProdutoController() {
        this.dao = new VendaProdutoDAO();
    }

    // Adiciona um produto à venda com validações
    public boolean adicionarProdutoVenda(VendaProduto vp) throws SQLException {
        if (vp.getQuantidade() <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        }
        if (vp.getPrecoUnitario() == null || vp.getPrecoUnitario().doubleValue() <= 0) {
            throw new IllegalArgumentException("Preço unitário deve ser maior que zero.");
        }
        if (vp.getDesconto() == null || vp.getDesconto().doubleValue() < 0 || vp.getDesconto().compareTo(vp.getPrecoUnitario()) > 0) {
            throw new IllegalArgumentException("Desconto deve ser entre 0 e o preço unitário.");
        }
        return dao.salvar(vp);
    }

    // Lista produtos associados a uma venda
    public List<VendaProduto> listarPorVenda(int vendaId) throws SQLException {
        return dao.listarPorVenda(vendaId);
    }

    // Lista vendas associadas a um produto
    public List<VendaProduto> listarPorProduto(int produtoId) throws SQLException {
        return dao.listarPorProduto(produtoId);
    }

    // Remove um produto de uma venda
    public boolean removerProdutoVenda(int vendaId, int produtoId) throws SQLException {
        return dao.deletar(vendaId, produtoId);
    }

    // Verifica se um código serial já existe
    public boolean serialExiste(String codigoSerial) throws SQLException {
        if (codigoSerial == null || codigoSerial.trim().isEmpty()) {
            return false; // Código serial nulo ou vazio não é considerado existente
        }
        return dao.serialExiste(codigoSerial);
    }
}