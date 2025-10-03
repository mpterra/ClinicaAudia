package controller;

import dao.EstoqueDAO;
import model.Estoque;
import model.MovimentoEstoque;

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

    /**
     * Reduz o estoque de um produto e registra o movimento de saída.
     *
     * @param produtoId ID do produto.
     * @param quantidade Quantidade a reduzir.
     * @param observacoes Observações do movimento.
     * @param usuarioLogado Usuário responsável.
     * @throws SQLException Em caso de erro no banco.
     */
    public void reduzirEstoque(int produtoId, int quantidade, String observacoes, String usuarioLogado) throws SQLException {
        Estoque estoque = buscarPorProdutoId(produtoId);
        if (estoque == null) {
            throw new IllegalArgumentException("Estoque não encontrado para o produto.");
        }
        if (estoque.getQuantidade() < quantidade) {
            throw new IllegalArgumentException("Estoque insuficiente.");
        }
        estoque.setQuantidade(estoque.getQuantidade() - quantidade);
        salvarOuAtualizarEstoque(estoque, usuarioLogado);

        MovimentoEstoque mov = new MovimentoEstoque();
        mov.setProdutoId(produtoId);
        mov.setQuantidade(quantidade);
        mov.setTipo(MovimentoEstoque.Tipo.SAIDA);
        mov.setObservacoes(observacoes);
        new MovimentoEstoqueController().registrarMovimento(mov, usuarioLogado);
    }

    /**
     * Incrementa o estoque de um produto e registra o movimento de entrada.
     *
     * @param produtoId ID do produto.
     * @param quantidade Quantidade a incrementar.
     * @param observacoes Observações do movimento.
     * @param usuarioLogado Usuário responsável.
     * @throws SQLException Em caso de erro no banco.
     */
    public void incrementarEstoque(int produtoId, int quantidade, String observacoes, String usuarioLogado) throws SQLException {
        Estoque estoque = buscarPorProdutoId(produtoId);
        if (estoque == null) {
            throw new IllegalArgumentException("Estoque não encontrado para o produto.");
        }
        estoque.setQuantidade(estoque.getQuantidade() + quantidade);
        salvarOuAtualizarEstoque(estoque, usuarioLogado);

        MovimentoEstoque mov = new MovimentoEstoque();
        mov.setProdutoId(produtoId);
        mov.setQuantidade(quantidade);
        mov.setTipo(MovimentoEstoque.Tipo.ENTRADA);
        mov.setObservacoes(observacoes);
        new MovimentoEstoqueController().registrarMovimento(mov, usuarioLogado);
    }
}