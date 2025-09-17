package dao;

import model.DocumentoAtendimento;
import model.Atendimento;
import util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DocumentoAtendimentoDAO {

    // Inserir documento
    public boolean insert(DocumentoAtendimento doc, String usuarioLogado) throws SQLException {
        String sql = "INSERT INTO documento_atendimento (atendimento_id, nome_arquivo, caminho_arquivo, tipo_arquivo, usuario) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, doc.getAtendimentoId());
            stmt.setString(2, doc.getNomeArquivo());
            stmt.setString(3, doc.getCaminhoArquivo());
            stmt.setString(4, doc.getTipoArquivo());
            stmt.setString(5, usuarioLogado);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) return false;

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    doc.setId(rs.getInt(1));
                    return true;
                }
            }
        }
        return false;
    }

    // Atualizar documento
    public boolean update(DocumentoAtendimento doc, String usuarioLogado) throws SQLException {
        String sql = "UPDATE documento_atendimento SET atendimento_id = ?, nome_arquivo = ?, caminho_arquivo = ?, tipo_arquivo = ?, usuario = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, doc.getAtendimentoId());
            stmt.setString(2, doc.getNomeArquivo());
            stmt.setString(3, doc.getCaminhoArquivo());
            stmt.setString(4, doc.getTipoArquivo());
            stmt.setString(5, usuarioLogado);
            stmt.setInt(6, doc.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    // Deletar documento
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM documento_atendimento WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    // Buscar por ID
    public DocumentoAtendimento findById(int id) throws SQLException {
        String sql = """
            SELECT da.*, a.data_hora, a.paciente_id, a.profissional_id 
            FROM documento_atendimento da 
            JOIN atendimento a ON da.atendimento_id = a.id 
            WHERE da.id = ?
            """;
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    // Listar por atendimento
    public List<DocumentoAtendimento> findByAtendimentoId(int atendimentoId) throws SQLException {
        String sql = """
            SELECT da.*, a.data_hora, a.paciente_id, a.profissional_id 
            FROM documento_atendimento da 
            JOIN atendimento a ON da.atendimento_id = a.id 
            WHERE da.atendimento_id = ?
            ORDER BY da.criado_em
            """;
        List<DocumentoAtendimento> lista = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, atendimentoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRow(rs));
                }
            }
        }
        return lista;
    }

    // Listar todos
    public List<DocumentoAtendimento> findAll() throws SQLException {
        String sql = """
            SELECT da.*, a.data_hora, a.paciente_id, a.profissional_id 
            FROM documento_atendimento da 
            JOIN atendimento a ON da.atendimento_id = a.id 
            ORDER BY da.criado_em
            """;
        List<DocumentoAtendimento> lista = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                lista.add(mapRow(rs));
            }
        }
        return lista;
    }

    // Mapear ResultSet para DocumentoAtendimento
    private DocumentoAtendimento mapRow(ResultSet rs) throws SQLException {
        DocumentoAtendimento doc = new DocumentoAtendimento();
        doc.setId(rs.getInt("id"));
        doc.setAtendimentoId(rs.getInt("atendimento_id"));
        doc.setNomeArquivo(rs.getString("nome_arquivo"));
        doc.setCaminhoArquivo(rs.getString("caminho_arquivo"));
        doc.setTipoArquivo(rs.getString("tipo_arquivo"));
        doc.setCriadoEm(rs.getTimestamp("criado_em"));
        doc.setUsuario(rs.getString("usuario"));

        Atendimento atendimento = new Atendimento();
        atendimento.setId(rs.getInt("atendimento_id"));
        atendimento.setDataHora(rs.getTimestamp("data_hora"));
        atendimento.setPacienteId(rs.getInt("paciente_id"));
        atendimento.setProfissionalId(rs.getInt("profissional_id"));
        doc.setAtendimento(atendimento);

        return doc;
    }
}