package controller;

import dao.EnderecoDAO;
import model.Endereco;

import java.sql.SQLException;
import java.util.List;

public class EnderecoController {

	private final EnderecoDAO dao;

	public EnderecoController() {
		this.dao = new EnderecoDAO();
	}

	// -------------------------
	// Inserir endereço
	// -------------------------
	public void adicionarEndereco(Endereco endereco) throws SQLException {
		if (endereco == null)
			throw new IllegalArgumentException("Endereço não pode ser nulo.");
		if (endereco.getCep() == null || endereco.getCep().isEmpty()) {
			throw new IllegalArgumentException("CEP é obrigatório.");
		}
		if (endereco.getRua() == null || endereco.getRua().isEmpty()) {
			throw new IllegalArgumentException("Rua é obrigatória.");
		}
		if (endereco.getCidade() == null || endereco.getCidade().isEmpty()) {
			throw new IllegalArgumentException("Cidade é obrigatória.");
		}
		dao.insert(endereco);
	}

	// -------------------------
	// Atualizar endereço
	// -------------------------
	public void atualizarEndereco(Endereco endereco) throws SQLException {
		if (endereco == null || endereco.getId() <= 0) {
			throw new IllegalArgumentException("Endereço inválido.");
		}
		dao.update(endereco);
	}

	// -------------------------
	// Deletar endereço
	// -------------------------
	public void deletarEndereco(int id) throws SQLException {
		Endereco endereco = dao.findById(id);
		if (endereco == null)
			throw new IllegalArgumentException("Endereço não encontrado.");
		dao.delete(id);
	}

	// -------------------------
	// Consultas
	// -------------------------
	public Endereco buscarPorId(int id) throws SQLException {
		if (id <= 0)
			return null;
		return dao.findById(id);
	}

	public List<Endereco> listarPorCep(String cep) throws SQLException {
		if (cep == null || cep.isEmpty())
			return List.of();
		return dao.findByCep(cep);
	}

	public List<Endereco> listarPorCidade(String cidade) throws SQLException {
		if (cidade == null || cidade.isEmpty())
			return List.of();
		return dao.findByCidade(cidade);
	}

	public List<Endereco> listarPorBairro(String bairro) throws SQLException {
		if (bairro == null || bairro.isEmpty())
			return List.of();
		return dao.findByBairro(bairro);
	}
}
