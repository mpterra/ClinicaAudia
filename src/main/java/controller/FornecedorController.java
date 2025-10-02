package controller;

import dao.FornecedorDAO;
import model.Fornecedor;

import java.sql.SQLException;
import java.util.List;

/**
 * Controlador para gerenciar operacoes relacionadas a fornecedores.
 * Responsavel por intermediar a comunicacao entre a camada de visao (view) e a camada de acesso a dados (DAO).
 */
public class FornecedorController {

    private final FornecedorDAO fornecedorDAO;

    /**
     * Construtor que inicializa o FornecedorDAO.
     */
    public FornecedorController() {
        this.fornecedorDAO = new FornecedorDAO();
    }

    /**
     * Salva um novo fornecedor no banco de dados.
     *
     * @param fornecedor O objeto Fornecedor a ser salvo.
     * @return true se o fornecedor foi salvo com sucesso, false caso contrario.
     * @throws SQLException Se ocorrer um erro ao acessar o banco de dados.
     */
    public boolean salvar(Fornecedor fornecedor) throws SQLException {
        return fornecedorDAO.salvar(fornecedor);
    }

    /**
     * Busca um fornecedor pelo seu ID.
     *
     * @param id O ID do fornecedor a ser buscado.
     * @return O objeto Fornecedor correspondente ou null se nao encontrado.
     * @throws SQLException Se ocorrer um erro ao acessar o banco de dados.
     */
    public Fornecedor buscarPorId(int id) throws SQLException {
        return fornecedorDAO.buscarPorId(id);
    }

    /**
     * Lista todos os fornecedores cadastrados, ordenados por nome.
     *
     * @return Uma lista de objetos Fornecedor ou uma lista vazia se nenhum for encontrado.
     * @throws SQLException Se ocorrer um erro ao acessar o banco de dados.
     */
    public List<Fornecedor> listarTodos() throws SQLException {
        return fornecedorDAO.listarTodos();
    }

    /**
     * Atualiza os dados de um fornecedor existente.
     *
     * @param fornecedor O objeto Fornecedor com os dados atualizados.
     * @return true se o fornecedor foi atualizado com sucesso, false caso contrario.
     * @throws SQLException Se ocorrer um erro ao acessar o banco de dados.
     */
    public boolean atualizar(Fornecedor fornecedor) throws SQLException {
        return fornecedorDAO.atualizar(fornecedor);
    }

    /**
     * Deleta um fornecedor pelo seu ID.
     *
     * @param id O ID do fornecedor a ser deletado.
     * @return true se o fornecedor foi deletado com sucesso, false caso contrario.
     * @throws SQLException Se ocorrer um erro ao acessar o banco de dados.
     */
    public boolean deletar(int id) throws SQLException {
        return fornecedorDAO.deletar(id);
    }

    /**
     * Busca o fornecedor associado à última compra de um produto.
     *
     * @param produtoId O ID do produto para o qual buscar o fornecedor.
     * @return O objeto Fornecedor da última compra ou null se não houver compras associadas.
     * @throws SQLException Se ocorrer um erro ao acessar o banco de dados.
     */
    public Fornecedor buscarFornecedorUltimaCompra(int produtoId) throws SQLException {
        return fornecedorDAO.buscarFornecedorUltimaCompra(produtoId);
    }
}