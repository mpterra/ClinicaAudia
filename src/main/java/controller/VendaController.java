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

    public boolean registrarVenda(Venda venda, String usuarioLogado) throws SQLException {
        if (venda.getValorTotal().doubleValue() < 0) {
            throw new IllegalArgumentException("Valor total não pode ser negativo.");
        }
        return dao.salvar(venda, usuarioLogado);
    }

    public Venda buscarPorId(int id) throws SQLException {
        return dao.buscarPorId(id);
    }

    public List<Venda> listarTodos() throws SQLException {
        return dao.listarTodos();
    }

    public boolean removerVenda(int id) throws SQLException {
        return dao.deletar(id);
    }
}
