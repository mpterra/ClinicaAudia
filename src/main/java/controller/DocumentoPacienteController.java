package controller;

import dao.DocumentoPacienteDAO;
import model.DocumentoPaciente;
import model.Paciente;

import java.sql.SQLException;
import java.util.List;

public class DocumentoPacienteController {

    private final DocumentoPacienteDAO dao;

    public DocumentoPacienteController() {
        this.dao = new DocumentoPacienteDAO();
    }

    // -------------------------
    // Inserir documento
    // -------------------------
    public void adicionarDocumento(DocumentoPaciente doc) throws SQLException {
        if (doc == null) throw new IllegalArgumentException("Documento não pode ser nulo.");
        if (doc.getPaciente() == null || doc.getPaciente().getId() <= 0) {
            throw new IllegalArgumentException("Documento precisa de um paciente válido.");
        }
        if (doc.getNomeArquivo() == null || doc.getNomeArquivo().isEmpty()) {
            throw new IllegalArgumentException("Nome do arquivo não pode ser vazio.");
        }
        dao.inserir(doc);
    }

    // -------------------------
    // Consultas
    // -------------------------
    public DocumentoPaciente buscarPorId(int id) throws SQLException {
        if (id <= 0) return null;
        return dao.buscarPorId(id);
    }

    public List<DocumentoPaciente> listarTodos() throws SQLException {
        return dao.listarTodos();
    }

    public List<DocumentoPaciente> listarPorPaciente(Paciente paciente) throws SQLException {
        if (paciente == null || paciente.getId() <= 0) {
            throw new IllegalArgumentException("Paciente inválido.");
        }
        return dao.listarPorPaciente(paciente);
    }

    // -------------------------
    // Deletar documento
    // -------------------------
    public void deletarDocumento(int id) throws SQLException {
        DocumentoPaciente doc = dao.buscarPorId(id);
        if (doc == null) throw new IllegalArgumentException("Documento não encontrado.");
        dao.deletar(id);
    }
}
