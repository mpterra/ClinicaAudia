package dao;

import model.EmprestimoProduto;
import util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmprestimoProdutoDAO {

    // -----------------------------
    // INSERIR
    // -----------------------------
    public boolean inserir(EmprestimoProduto e) throws SQLException {
        String sql = "INSERT INTO emprestimo_produto " +
                "(produto_id, codigo_serial, paciente_id, profissional_id, data_emprestimo, data_devolucao, devolvido, observacoes, usuario) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, e.getProdutoId());
            stmt.setString(2, e.getCodigoSerial()); // novo campo
            stmt.setInt(3, e.getPacienteId());
            stmt.setInt(4, e.getProfissionalId());

            if (e.getDataEmprestimo() != null) {
                stmt.setTimestamp(5, Timestamp.valueOf(e.getDataEmprestimo()));
            } else {
                stmt.setNull(5, Types.TIMESTAMP);
            }

            if (e.getDataDevolucao() != null) {
                stmt.setTimestamp(6, Timestamp.valueOf(e.getDataDevolucao()));
            } else {
                stmt.setNull(6, Types.TIMESTAMP);
            }

            stmt.setBoolean(7, e.isDevolvido());
            stmt.setString(8, e.getObservacoes());
            stmt.setString(9, e.getUsuario());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) return false;

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    e.setId(rs.getInt(1));
                    return true;
                }
            }
        }
        return false;
    }

    // -----------------------------
    // ATUALIZAR
    // -----------------------------
    public void atualizar(EmprestimoProduto e) throws SQLException {
        String sql = "UPDATE emprestimo_produto SET " +
                "produto_id=?, codigo_serial=?, paciente_id=?, profissional_id=?, data_emprestimo=?, data_devolucao=?, " +
                "devolvido=?, observacoes=?, usuario=? WHERE id=?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, e.getProdutoId());
            stmt.setString(2, e.getCodigoSerial());
            stmt.setInt(3, e.getPacienteId());
            stmt.setInt(4, e.getProfissionalId());

            if (e.getDataEmprestimo() != null) {
                stmt.setTimestamp(5, Timestamp.valueOf(e.getDataEmprestimo()));
            } else {
                stmt.setNull(5, Types.TIMESTAMP);
            }

            if (e.getDataDevolucao() != null) {
                stmt.setTimestamp(6, Timestamp.valueOf(e.getDataDevolucao()));
            } else {
                stmt.setNull(6, Types.TIMESTAMP);
            }

            stmt.setBoolean(7, e.isDevolvido());
            stmt.setString(8, e.getObservacoes());
            stmt.setString(9, e.getUsuario());
            stmt.setInt(10, e.getId());

            stmt.executeUpdate();
        }
    }

    // -----------------------------
    // DELETAR
    // -----------------------------
    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM emprestimo_produto WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // -----------------------------
    // BUSCAR POR ID
    // -----------------------------
    public EmprestimoProduto buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM emprestimo_produto WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    // -----------------------------
    // LISTAR TODOS
    // -----------------------------
    public List<EmprestimoProduto> listarTodos() throws SQLException {
        List<EmprestimoProduto> lista = new ArrayList<>();
        String sql = "SELECT * FROM emprestimo_produto ORDER BY data_emprestimo DESC";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    // -----------------------------
    // MAPEAR RESULTSET -> OBJETO
    // -----------------------------
    private EmprestimoProduto mapear(ResultSet rs) throws SQLException {
        EmprestimoProduto e = new EmprestimoProduto();
        e.setId(rs.getInt("id"));
        e.setProdutoId(rs.getInt("produto_id"));
        e.setCodigoSerial(rs.getString("codigo_serial")); // novo campo
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
