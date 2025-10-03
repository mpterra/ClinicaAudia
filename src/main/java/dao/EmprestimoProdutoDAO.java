package dao;

import model.EmprestimoProduto;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EmprestimoProdutoDAO {

    private final Connection conn;

    public EmprestimoProdutoDAO(Connection conn) {
        this.conn = conn;
    }

    // ============================
    // INSERIR
    // ============================
    public void inserir(EmprestimoProduto e) throws SQLException {
        String sql = "INSERT INTO emprestimo_produto " +
                "(produto_id, paciente_id, profissional_id, data_emprestimo, data_devolucao, devolvido, observacoes, usuario) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, e.getProdutoId());
            stmt.setInt(2, e.getPacienteId());
            stmt.setInt(3, e.getProfissionalId());
            stmt.setTimestamp(4, e.getDataEmprestimo() != null ? Timestamp.valueOf(e.getDataEmprestimo()) : null);
            stmt.setTimestamp(5, e.getDataDevolucao() != null ? Timestamp.valueOf(e.getDataDevolucao()) : null);
            stmt.setBoolean(6, e.isDevolvido());
            stmt.setString(7, e.getObservacoes());
            stmt.setString(8, e.getUsuario());

            stmt.executeUpdate();

            // pega o ID gerado
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    e.setId(rs.getInt(1));
                }
            }
        }
    }

    // ============================
    // ATUALIZAR
    // ============================
    public void atualizar(EmprestimoProduto e) throws SQLException {
        String sql = "UPDATE emprestimo_produto SET " +
                "produto_id = ?, paciente_id = ?, profissional_id = ?, data_emprestimo = ?, " +
                "data_devolucao = ?, devolvido = ?, observacoes = ?, usuario = ? " +
                "WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, e.getProdutoId());
            stmt.setInt(2, e.getPacienteId());
            stmt.setInt(3, e.getProfissionalId());
            stmt.setTimestamp(4, e.getDataEmprestimo() != null ? Timestamp.valueOf(e.getDataEmprestimo()) : null);
            stmt.setTimestamp(5, e.getDataDevolucao() != null ? Timestamp.valueOf(e.getDataDevolucao()) : null);
            stmt.setBoolean(6, e.isDevolvido());
            stmt.setString(7, e.getObservacoes());
            stmt.setString(8, e.getUsuario());
            stmt.setInt(9, e.getId());

            stmt.executeUpdate();
        }
    }

    // ============================
    // DELETAR
    // ============================
    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM emprestimo_produto WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // ============================
    // BUSCAR POR ID
    // ============================
    public EmprestimoProduto buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM emprestimo_produto WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    // ============================
    // LISTAR TODOS
    // ============================
    public List<EmprestimoProduto> listarTodos() throws SQLException {
        List<EmprestimoProduto> lista = new ArrayList<>();
        String sql = "SELECT * FROM emprestimo_produto ORDER BY data_emprestimo DESC";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    // ============================
    // MAPEAR RESULTSET -> OBJETO
    // ============================
    private EmprestimoProduto mapear(ResultSet rs) throws SQLException {
        EmprestimoProduto e = new EmprestimoProduto();
        e.setId(rs.getInt("id"));
        e.setProdutoId(rs.getInt("produto_id"));
        e.setPacienteId(rs.getInt("paciente_id"));
        e.setProfissionalId(rs.getInt("profissional_id"));

        Timestamp tsEmprestimo = rs.getTimestamp("data_emprestimo");
        if (tsEmprestimo != null) {
            e.setDataEmprestimo(tsEmprestimo.toLocalDateTime());
        }

        Timestamp tsDevolucao = rs.getTimestamp("data_devolucao");
        if (tsDevolucao != null) {
            e.setDataDevolucao(tsDevolucao.toLocalDateTime());
        }

        e.setDevolvido(rs.getBoolean("devolvido"));
        e.setObservacoes(rs.getString("observacoes"));
        e.setUsuario(rs.getString("usuario"));

        return e;
    }
}
