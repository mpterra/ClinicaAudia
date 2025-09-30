package controller;

import dao.ValorAtendimentoEmpresaDAO;
import model.ValorAtendimentoEmpresa;

import java.sql.SQLException;
import java.util.List;

public class ValorAtendimentoEmpresaController {

    private final ValorAtendimentoEmpresaDAO dao;

    public ValorAtendimentoEmpresaController() {
        this.dao = new ValorAtendimentoEmpresaDAO();
    }

    // Salva ou atualiza um valor de atendimento por empresa
    public boolean salvar(ValorAtendimentoEmpresa valor) throws SQLException {
        return dao.salvar(valor);
    }

    // Deleta um valor de atendimento por empresa
    public void deletar(int id) throws SQLException {
        dao.deletar(id);
    }

    // Busca um valor de atendimento por empresa pelo ID
    public ValorAtendimentoEmpresa buscarPorId(int id) throws SQLException {
        return dao.buscarPorId(id);
    }

    // Lista todos os valores de atendimento por empresa
    public List<ValorAtendimentoEmpresa> listarTodos() throws SQLException {
        return dao.listarTodos();
    }

    // Busca valores por profissional e empresa
    public List<ValorAtendimentoEmpresa> buscarPorProfissionalEEmpresa(int profissionalId, int empresaParceiraId) throws SQLException {
        return dao.buscarPorProfissionalEEmpresa(profissionalId, empresaParceiraId);
    }
}