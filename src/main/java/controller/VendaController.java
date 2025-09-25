package controller;

import dao.VendaDAO;
import model.Venda;

import java.sql.SQLException;
import java.util.List;

public class VendaController {

    private final VendaDAO dao;

    public VendaController() {
        this.dao = new VendaDAO();
    }

    // ============================
    // CREATE
    // ============================
    public boolean registrarVenda(Venda venda, String usuarioLogado) throws SQLException {
        validarVenda(venda);
        return dao.salvar(venda, usuarioLogado);
    }

    // ============================
    // READ
    // ============================
    public Venda buscarPorId(int id) throws SQLException {
        return dao.buscarPorId(id);
    }

    public List<Venda> listarTodos() throws SQLException {
        return dao.listarTodos();
    }

    // ============================
    // UPDATE
    // ============================
    public boolean atualizarVenda(Venda venda, String usuarioLogado) throws SQLException {
        validarVenda(venda);
        return dao.atualizar(venda, usuarioLogado); // método que vamos implementar no DAO
    }

    // ============================
    // DELETE
    // ============================
    public boolean removerVenda(int id) throws SQLException {
        return dao.deletar(id);
    }

    // ============================
    // Validação de regras de negócio
    // ============================
    private void validarVenda(Venda venda) {
        if (venda == null) {
            throw new IllegalArgumentException("Venda não pode ser nula.");
        }
        if (venda.getValorTotal() == null) {
            throw new IllegalArgumentException("Valor total não pode ser nulo.");
        }
        if (venda.getValorTotal().doubleValue() < 0) {
            throw new IllegalArgumentException("Valor total não pode ser negativo.");
        }
    }
}
