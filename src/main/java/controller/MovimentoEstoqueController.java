package controller;

import dao.MovimentoEstoqueDAO;
import model.MovimentoEstoque;
import model.Produto;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class MovimentoEstoqueController {

    private final MovimentoEstoqueDAO dao;

    public MovimentoEstoqueController() {
        this.dao = new MovimentoEstoqueDAO();
    }

    // -------------------------
    // Inserir movimento
    // -------------------------
    public void adicionarMovimento(MovimentoEstoque movimento) throws SQLException {
        if (movimento == null) throw new IllegalArgumentException("Movimento não pode ser nulo.");
        if (movimento.getProduto() == null || movimento.getProduto().getId() <= 0) {
            throw new IllegalArgumentException("Produto inválido.");
        }
        dao.insert(movimento);
    }

    // -------------------------
    // Atualizar movimento
    // -------------------------
    public void atualizarMovimento(MovimentoEstoque movimento) throws SQLException {
        if (movimento == null || movimento.getId() <= 0) {
            throw new IllegalArgumentException("Movimento inválido.");
        }
        dao.update(movimento);
    }

    // -------------------------
    // Deletar movimento
    // -------------------------
    public void deletarMovimento(int id) throws SQLException {
        MovimentoEstoque m = dao.findById(id);
        if (m == null) throw new IllegalArgumentException("Movimento não encontrado.");
        dao.delete(id);
    }

    // -------------------------
    // Consultas
    // -------------------------
    public MovimentoEstoque buscarPorId(int id) throws SQLException {
        if (id <= 0) return null;
        return dao.findById(id);
    }

    public List<MovimentoEstoque> listarTodos() throws SQLException {
        return dao.findAll();
    }

    public List<MovimentoEstoque> listarPorProduto(Produto produto) throws SQLException {
        if (produto == null || produto.getId() <= 0) return List.of();
        return dao.findByProduto(produto);
    }

    public List<MovimentoEstoque> listarPorTipo(MovimentoEstoque.TipoMovimento tipo) throws SQLException {
        if (tipo == null) return List.of();
        return dao.findByTipo(tipo);
    }

    public List<MovimentoEstoque> listarPorUsuario(String usuario) throws SQLException {
        if (usuario == null || usuario.isEmpty()) return List.of();
        return dao.findByUsuario(usuario);
    }

    public List<MovimentoEstoque> listarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        if (inicio == null || fim == null || inicio.isAfter(fim)) return List.of();
        return dao.findByDataRange(inicio, fim);
    }

    public List<MovimentoEstoque> listarPorProdutoEPeriodo(Produto produto, LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        if (produto == null || produto.getId() <= 0 || inicio == null || fim == null || inicio.isAfter(fim)) {
            return List.of();
        }
        return dao.findByProdutoAndDataRange(produto, inicio, fim);
    }
}
