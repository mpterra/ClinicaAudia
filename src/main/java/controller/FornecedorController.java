package controller;

import dao.FornecedorDAO;
import model.Fornecedor;

import java.sql.SQLException;
import java.util.List;

public class FornecedorController {

    private final FornecedorDAO fornecedorDAO;

    public FornecedorController() {
        this.fornecedorDAO = new FornecedorDAO();
    }

    // -------------------------
    // CREATE
    // -------------------------
    public boolean salvarFornecedor(Fornecedor fornecedor) {
        try {
            return fornecedorDAO.salvar(fornecedor);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // -------------------------
    // READ by ID
    // -------------------------
    public Fornecedor buscarFornecedorPorId(int id) {
        try {
            return fornecedorDAO.buscarPorId(id);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // -------------------------
    // READ all
    // -------------------------
    public List<Fornecedor> listarFornecedores() {
        try {
            return fornecedorDAO.listarTodos();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // -------------------------
    // UPDATE
    // -------------------------
    public boolean atualizarFornecedor(Fornecedor fornecedor) {
        try {
            return fornecedorDAO.atualizar(fornecedor);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // -------------------------
    // DELETE
    // -------------------------
    public boolean deletarFornecedor(int id) {
        try {
            return fornecedorDAO.deletar(id);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
