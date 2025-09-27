package dao;

import model.Despesa;
import model.Despesa.Categoria;
import model.Despesa.FormaPagamento;
import model.Despesa.Status;
import util.Database;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DespesaDAO {

    // ============================
    // CREATE
    // ============================
    public boolean salvar(Despesa despesa, String usuarioLogado) throws SQLException {
        String sql = """
            INSERT INTO despesa 
            (descricao, categoria, valor, forma_pagamento, data_vencimento, data_pagamento, status, usuario)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, despesa.getDescricao());
            stmt.setString(2, despesa.getCategoria().name());
            stmt.setBigDecimal(3, despesa.getValor());
            stmt.setString(4, despesa.getFormaPagamento().name());
            stmt.setDate(5, Date.valueOf(despesa.getDataVencimento()));
            if (despesa.getDataPagamento() != null) {
                stmt.setDate(6, Date.valueOf(despesa.getDataPagamento()));
            } else {
                stmt.setNull(6, Types.DATE);
            }
            stmt.setString(7, despesa.getStatus().name());
            stmt.setString(8, usuarioLogado);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Falha ao inserir Despesa.");

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) despesa.setId(rs.getInt(1));
            }
        }
        return true;
    }

    // ============================
    // READ
    // ============================
    public Despesa buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM despesa WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Despesa> listarTodos() throws SQLException {
        return listarPorFiltros(null, null, null, null, null);
    }

    // ============================
    // FILTROS AVANÇADOS
    // ============================
    public List<Despesa> listarPorFiltros(
            Categoria categoria,
            Status status,
            FormaPagamento formaPagamento,
            LocalDate inicio,
            LocalDate fim
    ) throws SQLException {

        List<Despesa> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM despesa WHERE 1=1");

        if (categoria != null) sql.append(" AND categoria = ?");
        if (status != null) sql.append(" AND status = ?");
        if (formaPagamento != null) sql.append(" AND forma_pagamento = ?");
        if (inicio != null) sql.append(" AND data_vencimento >= ?");
        if (fim != null) sql.append(" AND data_vencimento <= ?");
        sql.append(" ORDER BY data_vencimento");

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int index = 1;
            if (categoria != null) stmt.setString(index++, categoria.name());
            if (status != null) stmt.setString(index++, status.name());
            if (formaPagamento != null) stmt.setString(index++, formaPagamento.name());
            if (inicio != null) stmt.setDate(index++, Date.valueOf(inicio));
            if (fim != null) stmt.setDate(index++, Date.valueOf(fim));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapRow(rs));
            }
        }
        return lista;
    }

    // ============================
    // UPDATE
    // ============================
    public boolean atualizar(Despesa despesa, String usuarioLogado) throws SQLException {
        String sql = """
            UPDATE despesa SET
                descricao = ?, categoria = ?, valor = ?, forma_pagamento = ?, 
                data_vencimento = ?, data_pagamento = ?, status = ?, usuario = ?, dataHora = CURRENT_TIMESTAMP
            WHERE id = ?
            """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, despesa.getDescricao());
            stmt.setString(2, despesa.getCategoria().name());
            stmt.setBigDecimal(3, despesa.getValor());
            stmt.setString(4, despesa.getFormaPagamento().name());
            stmt.setDate(5, Date.valueOf(despesa.getDataVencimento()));
            if (despesa.getDataPagamento() != null) {
                stmt.setDate(6, Date.valueOf(despesa.getDataPagamento()));
            } else {
                stmt.setNull(6, Types.DATE);
            }
            stmt.setString(7, despesa.getStatus().name());
            stmt.setString(8, usuarioLogado);
            stmt.setInt(9, despesa.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    // ============================
    // DELETE
    // ============================
    public boolean deletar(int id) throws SQLException {
        String sql = "DELETE FROM despesa WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    // ============================
    // MAPEAMENTO
    // ============================
    private Despesa mapRow(ResultSet rs) throws SQLException {
        Despesa d = new Despesa();
        d.setId(rs.getInt("id"));
        d.setDescricao(rs.getString("descricao"));
        d.setCategoria(Categoria.valueOf(rs.getString("categoria")));
        d.setValor(rs.getBigDecimal("valor"));
        d.setFormaPagamento(FormaPagamento.valueOf(rs.getString("forma_pagamento")));
        d.setDataVencimento(rs.getDate("data_vencimento").toLocalDate());
        Date dataPagamento = rs.getDate("data_pagamento");
        if (dataPagamento != null) d.setDataPagamento(dataPagamento.toLocalDate());
        d.setStatus(Status.valueOf(rs.getString("status")));
        d.setUsuario(rs.getString("usuario"));
        Timestamp ts = rs.getTimestamp("dataHora");
        if (ts != null) d.setDataHora(ts.toLocalDateTime());
        return d;
    }
}
