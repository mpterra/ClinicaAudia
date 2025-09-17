package controller;

import dao.DocumentoAtendimentoDAO;
import model.DocumentoAtendimento;

import java.sql.SQLException;
import java.util.List;

public class DocumentoAtendimentoController {

    private final DocumentoAtendimentoDAO dao;

    public DocumentoAtendimentoController() {
        this.dao = new DocumentoAtendimentoDAO();
    }

    // Criar documento
    public boolean criar(DocumentoAtendimento doc, String usuarioLogado) throws SQLException {
        validarDocumento(doc);
        return dao.insert(doc, usuarioLogado);
    }

    // Atualizar documento
    public boolean atualizar(DocumentoAtendimento doc, String usuarioLogado) throws SQLException {
        validarDocumento(doc);
        return dao.update(doc, usuarioLogado);
    }

    // Deletar documento
    public boolean deletar(int id) throws SQLException {
        return dao.delete(id);
    }

    // Buscar por ID
    public DocumentoAtendimento buscarPorId(int id) throws SQLException {
        return dao.findById(id);
    }

    // Listar por atendimento
    public List<DocumentoAtendimento> listarPorAtendimentoId(int atendimentoId) throws SQLException {
        return dao.findByAtendimentoId(atendimentoId);
    }

    // Listar todos
    public List<DocumentoAtendimento> listarTodos() throws SQLException {
        return dao.findAll();
    }

    // Validação
    private void validarDocumento(DocumentoAtendimento doc) {
        if (doc.getAtendimentoId() <= 0) {
            throw new IllegalArgumentException("Atendimento inválido.");
        }
        if (doc.getNomeArquivo() == null || doc.getNomeArquivo().isBlank()) {
            throw new IllegalArgumentException("Nome do arquivo é obrigatório.");
        }
        if (doc.getCaminhoArquivo() == null || doc.getCaminhoArquivo().isBlank()) {
            throw new IllegalArgumentException("Caminho do arquivo é obrigatório.");
        }
        if (doc.getTipoArquivo() == null || doc.getTipoArquivo().isBlank()) {
            throw new IllegalArgumentException("Tipo de arquivo é obrigatório.");
        }
    }
}