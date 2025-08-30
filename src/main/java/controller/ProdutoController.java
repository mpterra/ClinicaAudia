package controller;

import dao.ProdutoDAO;
import model.Produto;
import util.FiltroProduto;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class ProdutoController {

    private final ProdutoDAO dao = new ProdutoDAO();

    // -------------------------
    // CRUD Básico
    // -------------------------

    public void inserir(Produto produto) throws SQLException {
        dao.inserir(produto);
    }

    public void atualizar(Produto produto) throws SQLException {
        dao.atualizar(produto);
    }

    public void deletar(int id) throws SQLException {
        dao.deletar(id);
    }

    public Produto buscarPorId(int id) throws SQLException {
        return dao.buscarPorId(id);
    }

    public List<Produto> listarTodos() throws SQLException {
        return dao.listarTodos();
    }

    // -------------------------
    // Buscas simples
    // -------------------------

    public List<Produto> buscarPorNome(String nome) throws SQLException {
        return dao.buscarPorNome(nome);
    }

    public Produto buscarPorCodigoSerial(String codigoSerial) throws SQLException {
        return dao.buscarPorCodigoSerial(codigoSerial);
    }

    public List<Produto> buscarPorTipo(int tipoProdutoId) throws SQLException {
        return dao.buscarPorTipo(tipoProdutoId);
    }

    public List<Produto> buscarPorFaixaPreco(BigDecimal precoMin, BigDecimal precoMax) throws SQLException {
        return dao.buscarPorFaixaPreco(precoMin, precoMax);
    }

    public List<Produto> buscarPorEstoqueMinimo(int minimo) throws SQLException {
        return dao.buscarPorEstoqueMinimo(minimo);
    }

    // -------------------------
    // Busca avançada com filtros
    // -------------------------
    public List<Produto> buscarComFiltro(FiltroProduto filtro) throws SQLException {
        return dao.buscarProdutosAvancado(
                filtro.getNome(),
                filtro.getTipoProdutoId(),
                filtro.getPrecoMin(),
                filtro.getPrecoMax(),
                filtro.getEstoqueMin(),
                filtro.getEstoqueMax()
        );
    }
}
