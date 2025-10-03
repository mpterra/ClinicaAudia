package controller;

import dao.EmprestimoProdutoDAO;
import model.EmprestimoProduto;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class EmprestimoProdutoController {

    private final EmprestimoProdutoDAO dao;

    public EmprestimoProdutoController(Connection conn) {
        this.dao = new EmprestimoProdutoDAO(conn);
    }

    // ============================
    // CREATE
    // ============================
    public void adicionar(EmprestimoProduto e) throws SQLException {
        if (e.getProdutoId() <= 0 || e.getPacienteId() <= 0 || e.getProfissionalId() <= 0) {
            throw new IllegalArgumentException("Produto, Paciente e Profissional são obrigatórios.");
        }
        if (e.getDataEmprestimo() == null) {
            e.setDataEmprestimo(LocalDateTime.now());
        }
        dao.inserir(e);
    }

    // ============================
    // UPDATE
    // ============================
    public void atualizar(EmprestimoProduto e) throws SQLException {
        if (e.getId() <= 0) {
            throw new IllegalArgumentException("ID inválido para atualização.");
        }
        dao.atualizar(e);
    }

    // ============================
    // DELETE
    // ============================
    public void remover(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID inválido para exclusão.");
        }
        dao.deletar(id);
    }

    // ============================
    // READ
    // ============================
    public EmprestimoProduto buscarPorId(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID inválido para busca.");
        }
        return dao.buscarPorId(id);
    }

    public List<EmprestimoProduto> listarTodos() throws SQLException {
        return dao.listarTodos();
    }

    // ============================
    // OPERAÇÕES ESPECIAIS
    // ============================
    public void marcarDevolucao(int id) throws SQLException {
        EmprestimoProduto e = dao.buscarPorId(id);
        if (e == null) {
            throw new IllegalArgumentException("Empréstimo não encontrado.");
        }
        e.setDevolvido(true);
        e.setDataDevolucao(LocalDateTime.now());
        dao.atualizar(e);
    }
}
