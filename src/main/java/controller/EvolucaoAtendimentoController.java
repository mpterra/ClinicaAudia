package controller;

import dao.EvolucaoAtendimentoDAO;
import model.EvolucaoAtendimento;

import java.sql.SQLException;
import java.util.List;

public class EvolucaoAtendimentoController {

    private final EvolucaoAtendimentoDAO dao;

    public EvolucaoAtendimentoController() {
        this.dao = new EvolucaoAtendimentoDAO();
    }

    public boolean criarEvolucao(EvolucaoAtendimento evo, String usuarioLogado) throws SQLException {
        return dao.salvar(evo, usuarioLogado);
    }

    public EvolucaoAtendimento buscarPorId(int id) throws SQLException {
        return dao.buscarPorId(id);
    }

    public List<EvolucaoAtendimento> listarPorAtendimento(int atendimentoId) throws SQLException {
        return dao.listarPorAtendimento(atendimentoId);
    }

    public boolean removerEvolucao(int id) throws SQLException {
        return dao.deletar(id);
    }
}
