package dao;

import model.DocumentoPaciente;
import model.Paciente;
import util.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DocumentoPacienteDAO {

    // -------------------------
    // Inserir novo documento
    // -------------------------
    public void inserir(DocumentoPaciente doc) throws SQLException {
        String sql = "INSERT INTO documento_paciente " +
                "(paciente_id, nome_arquivo, caminho_arquivo, tipo_arquivo, usuario) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, doc.getPaciente().getId());
            stmt.setString(2, doc.getNomeArquivo());
            stmt.setString(3, doc.getCaminhoArquivo());
            stmt.setString(4, doc.getTipoArquivo());
            stmt.setString(5, doc.getUsuario());

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                doc.setId(rs.getInt(1));
            }
        }
    }

    // -------------------------
    // Buscar documento por ID
    // -------------------------
    public DocumentoPaciente buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM documento_paciente WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        }
        return null;
    }

    // -------------------------
    // Listar todos os documentos
    // -------------------------
    public List<DocumentoPaciente> listarTodos() throws SQLException {
        List<DocumentoPaciente> lista = new ArrayList<>();
        String sql = "SELECT * FROM documento_paciente ORDER BY criado_em DESC";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(mapResultSet(rs));
            }
        }
        return lista;
    }

    // -------------------------
    // Listar documentos por paciente
    // -------------------------
    public List<DocumentoPaciente> listarPorPaciente(Paciente paciente) throws SQLException {
        List<DocumentoPaciente> lista = new ArrayList<>();
        String sql = "SELECT * FROM documento_paciente WHERE paciente_id = ? ORDER BY criado_em DESC";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, paciente.getId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(mapResultSet(rs));
            }
        }
        return lista;
    }

    // -------------------------
    // Deletar documento
    // -------------------------
    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM documento_paciente WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // -------------------------
    // Mapear ResultSet para objeto
    // -------------------------
    private DocumentoPaciente mapResultSet(ResultSet rs) throws SQLException {
        DocumentoPaciente doc = new DocumentoPaciente();
        doc.setId(rs.getInt("id"));

        int pacienteId = rs.getInt("paciente_id");
        if (!rs.wasNull()) {
            Paciente paciente = new Paciente();
            paciente.setId(pacienteId);
            doc.setPaciente(paciente);
        }

        doc.setNomeArquivo(rs.getString("nome_arquivo"));
        doc.setCaminhoArquivo(rs.getString("caminho_arquivo"));
        doc.setTipoArquivo(rs.getString("tipo_arquivo"));

        Timestamp ts = rs.getTimestamp("criado_em");
        if (ts != null) doc.getClass().getDeclaredFields(); // atenção: você vai precisar de um setter se quiser setar criadoEm

        doc.setUsuario(rs.getString("usuario"));
        return doc;
    }
}
