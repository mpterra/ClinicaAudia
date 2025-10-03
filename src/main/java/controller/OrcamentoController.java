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

	// ============================
	// CREATE
	// ============================
	public void criarOrcamento(Orcamento orcamento, String usuarioLogado) throws SQLException {
		validarOrcamento(orcamento);
		dao.salvar(orcamento, usuarioLogado);
	}

	// ============================
	// READ
	// ============================
	public Orcamento buscarPorId(int id) throws SQLException {
		if (id <= 0)
			throw new IllegalArgumentException("ID inválido.");
		return dao.buscarPorId(id);
	}

	public List<Orcamento> listarTodos() throws SQLException {
		return dao.listarTodos();
	}

	// ============================
	// UPDATE
	// ============================
	public void atualizarOrcamento(Orcamento orcamento, String usuarioLogado) throws SQLException {
		validarOrcamento(orcamento);
		boolean atualizado = dao.atualizar(orcamento, usuarioLogado);
		if (!atualizado) {
			throw new SQLException("Orçamento não encontrado para atualização.");
		}
	}

	// ============================
	// DELETE
	// ============================
	public void removerOrcamento(int id) throws SQLException {
		if (id <= 0)
			throw new IllegalArgumentException("ID inválido.");
		boolean removido = dao.deletar(id);
		if (!removido) {
			throw new SQLException("Orçamento não encontrado para remoção.");
		}
	}

	// ============================
	// VALIDAÇÃO
	// ============================
	private void validarOrcamento(Orcamento orcamento) {
		if (orcamento == null) {
			throw new IllegalArgumentException("Orçamento não pode ser nulo.");
		}
		if (orcamento.getValorTotal() == null || orcamento.getValorTotal().doubleValue() < 0) {
			throw new IllegalArgumentException("Valor total não pode ser nulo ou negativo.");
		}
	}
}
