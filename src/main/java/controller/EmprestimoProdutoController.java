package controller;

import dao.EmprestimoProdutoDAO;
import model.EmprestimoProduto;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class EmprestimoProdutoController {

    private final EmprestimoProdutoDAO dao;

    public EmprestimoProdutoController() {
        this.dao = new EmprestimoProdutoDAO();
    }

    // ============================
    // CREATE
    // ============================
    public boolean adicionar(EmprestimoProduto e) throws SQLException {
        validarCamposObrigatorios(e, true);

        if (e.getDataEmprestimo() == null) {
            e.setDataEmprestimo(LocalDateTime.now());
        }

        return dao.inserir(e);
    }

    // ============================
    // UPDATE
    // ============================
    public void atualizar(EmprestimoProduto e) throws SQLException {
        if (e.getId() <= 0) {
            throw new IllegalArgumentException("ID inválido para atualização.");
        }

        validarCamposObrigatorios(e, false);

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

    // ============================
    // VALIDAÇÃO
    // ============================
    private void validarCamposObrigatorios(EmprestimoProduto e, boolean isNovo) {
        if (e.getProdutoId() <= 0) {
            throw new IllegalArgumentException("Produto é obrigatório.");
        }
        if (e.getPacienteId() <= 0) {
            throw new IllegalArgumentException("Paciente é obrigatório.");
        }
        if (e.getProfissionalId() <= 0) {
            throw new IllegalArgumentException("Profissional é obrigatório.");
        }
        if (e.getCodigoSerial() == null || e.getCodigoSerial().isBlank()) {
            throw new IllegalArgumentException("Código serial é obrigatório.");
        }

        // Para novos registros, devolvido deve iniciar como falso
        if (isNovo) {
            e.setDevolvido(false);
        }
    }
}
