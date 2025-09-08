package controller;

import dao.OrcamentoDAO;
import model.Orcamento;

import java.sql.SQLException;
import java.util.List;

public class OrcamentoController {

    private final OrcamentoDAO dao;

    public OrcamentoController() {
        this.dao = new OrcamentoDAO();
    }

    public boolean criarOrcamento(Orcamento orcamento, String usuarioLogado) throws SQLException {
        if (orcamento.getValorTotal().doubleValue() < 0) {
            throw new IllegalArgumentException("Valor total não pode ser negativo.");
        }
        return dao.salvar(orcamento, usuarioLogado);
    }

    public Orcamento buscarPorId(int id) throws SQLException {
        return dao.buscarPorId(id);
    }

    public List<Orcamento> listarTodos() throws SQLException {
        return dao.listarTodos();
    }

    public boolean removerOrcamento(int id) throws SQLException {
        return dao.deletar(id);
    }
}
