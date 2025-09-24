package controller;

import dao.ProdutoDAO;
import model.Produto;

import java.sql.SQLException;
import java.util.List;
import java.math.BigDecimal;

public class ProdutoController {

    private final ProdutoDAO dao;

    public ProdutoController() {
        this.dao = new ProdutoDAO();
    }

    // ============================
    // CREATE
    // ============================
    public boolean criarProduto(Produto produto, String usuarioLogado) throws SQLException {
        validarProduto(produto);
        return dao.salvar(produto, usuarioLogado);
    }

    // ============================
    // UPDATE
    // ============================
    public boolean atualizarProduto(Produto produto, String usuarioLogado) throws SQLException {
        if (produto.getId() <= 0) {
            throw new IllegalArgumentException("ID do produto inválido para atualização.");
        }
        validarProduto(produto);
        return dao.atualizar(produto, usuarioLogado);
    }

    // ============================
    // DELETE
    // ============================
    public boolean removerProduto(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID inválido para remoção.");
        }
        return dao.deletar(id);
    }

    // ============================
    // READ
    // ============================
    public Produto buscarPorId(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID inválido para busca.");
        }
        return dao.buscarPorId(id);
    }

    public List<Produto> listarTodos() throws SQLException {
        return dao.listarTodos();
    }

    // ============================
    // VALIDATION
    // ============================
    private void validarProduto(Produto produto) {
        if (produto == null) {
            throw new IllegalArgumentException("Produto não pode ser nulo.");
        }
        if (produto.getNome() == null || produto.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome do produto é obrigatório.");
        }
        if (produto.getTipoProdutoId() <= 0) {
            throw new IllegalArgumentException("Tipo de produto inválido.");
        }
        if (produto.getCodigoSerial() == null || produto.getCodigoSerial().isBlank()) {
            throw new IllegalArgumentException("Código serial é obrigatório.");
        }
        if (produto.getGarantiaMeses() < 0) {
            throw new IllegalArgumentException("Garantia em meses não pode ser negativa.");
        }
        if (produto.getPrecoVenda() == null || produto.getPrecoVenda().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Preço de venda deve ser maior ou igual a zero.");
        }
        if (produto.getPrecoCusto() == null || produto.getPrecoCusto().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Preço de custo deve ser maior ou igual a zero.");
        }
    }
}