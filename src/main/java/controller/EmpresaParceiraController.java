package controller;

import dao.EmpresaParceiraDAO;
import model.EmpresaParceira;

import java.sql.SQLException;
import java.util.List;

public class EmpresaParceiraController {

    private final EmpresaParceiraDAO dao;

    public EmpresaParceiraController() {
        this.dao = new EmpresaParceiraDAO();
    }

    // -----------------------------
    // CRUD
    // -----------------------------

    // Salva uma nova empresa parceira
    public boolean salvar(EmpresaParceira empresa) throws SQLException {
        return dao.salvar(empresa);
    }

    // Atualiza uma empresa parceira existente
    public void atualizar(EmpresaParceira empresa) throws SQLException {
        dao.atualizar(empresa);
    }

    // Deleta uma empresa parceira pelo ID
    public void deletar(int id) throws SQLException {
        dao.deletar(id);
    }

    // Busca uma empresa parceira pelo ID
    public EmpresaParceira buscarPorId(int id) throws SQLException {
        return dao.buscarPorId(id);
    }

    // -----------------------------
    // Buscas simples
    // -----------------------------

    // Busca empresas por nome (parcial)
    public List<EmpresaParceira> buscarPorNome(String nome) throws SQLException {
        return dao.buscarPorNome(nome);
    }

    // Busca empresas por CNPJ
    public List<EmpresaParceira> buscarPorCnpj(String cnpj) throws SQLException {
        return dao.buscarPorCnpj(cnpj);
    }

    // Lista todas as empresas parceiras
    public List<EmpresaParceira> listarTodos() throws SQLException {
        return dao.listarTodos();
    }

    // Lista todas as empresas ordenadas por ID (descendente)
    public List<EmpresaParceira> listarTodosOrdenadoPorIdDesc() throws SQLException {
        return dao.listarTodosOrdenadoPorIdDesc();
    }

    // -----------------------------
    // Busca avançada
    // -----------------------------

    // Busca avançada com filtros opcionais
    public List<EmpresaParceira> buscarAvancado(String nome, String cnpj) throws SQLException {
        return dao.buscarAvancado(nome, cnpj);
    }
}