package controller;

import dao.ProfissionalDAO;
import model.Profissional;

import java.sql.SQLException;
import java.util.List;

public class ProfissionalController {

    private final ProfissionalDAO dao;

    public ProfissionalController() {
        this.dao = new ProfissionalDAO();
    }

    // -----------------------------
    // CRUD
    // -----------------------------

    public boolean salvar(Profissional prof) throws SQLException {
        return dao.salvar(prof);
    }

    public void atualizar(Profissional prof) throws SQLException {
        dao.atualizar(prof);
    }

    public void deletar(int id) throws SQLException {
        dao.deletar(id);
    }

    public Profissional buscarPorId(int id) throws SQLException {
        return dao.buscarPorId(id);
    }

    // -----------------------------
    // Buscas simples
    // -----------------------------

    public List<Profissional> buscarPorNome(String nome) throws SQLException {
        return dao.buscarPorNome(nome);
    }

    public List<Profissional> buscarPorTipo(String tipo) throws SQLException {
        return dao.buscarPorTipo(tipo);
    }

    public List<Profissional> buscarPorAtivo(boolean ativo) throws SQLException {
        return dao.buscarPorAtivo(ativo);
    }

    // -----------------------------
    // Busca avançada
    // -----------------------------
    public List<Profissional> buscarAvancado(String nome, String tipo, Boolean ativo) throws SQLException {
        return dao.buscarAvancado(nome, tipo, ativo);
    }

    public List<Profissional> listarTodos() throws SQLException {
        return dao.listarTodos();
    }

	public List<Profissional> listarTodosOrdenadoPorIdDesc() {
		return dao.listarTodosOrdenadoPorIdDesc();
	}
}
