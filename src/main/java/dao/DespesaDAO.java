package dao;

import model.Despesa;
import model.Despesa.Categoria;
import model.Despesa.FormaPagamento;
import model.Despesa.Status;
import util.Database;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DespesaDAO {

    // ============================
    // CREATE
    // ============================
    public boolean salvar(Despesa despesa, String usuarioLogado) throws SQLException {
        String sql = """
            INSERT INTO despesa 
            (descricao, categoria, recorrente, valor, forma_pagamento, data_vencimento, data_pagamento, status, usuario)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preencherStatement(stmt, despesa, usuarioLogado, false);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir despesa.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    despesa.setId(rs.getInt(1));
                }
            }
            return true;
        }
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
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Despesa> listarTodos() throws SQLException {
        return listarPorFiltros(null, null, null, null, null, null);
    }

    // ============================
    // FILTROS AVANÇADOS
    // ============================
    public List<Despesa> listarPorFiltros(
            Categoria categoria,
            Status status,
            FormaPagamento formaPagamento,
            Boolean recorrente,
            LocalDate inicio,
            LocalDate fim
    ) throws SQLException {

        StringBuilder sql = new StringBuilder("SELECT * FROM despesa WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (categoria != null) {
            sql.append(" AND categoria = ?");
            params.add(categoria.name());
        }
        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status.name());
        }
        if (formaPagamento != null) {
            sql.append(" AND forma_pagamento = ?");
            params.add(formaPagamento.name());
        }
        if (recorrente != null) {
            sql.append(" AND recorrente = ?");
            params.add(recorrente ? 1 : 0);
        }
        if (inicio != null) {
            sql.append(" AND data_vencimento >= ?");
            params.add(Date.valueOf(inicio));
        }
        if (fim != null) {
            sql.append(" AND data_vencimento <= ?");
            params.add(Date.valueOf(fim));
        }
        sql.append(" ORDER BY data_vencimento");

        List<Despesa> lista = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRow(rs));
                }
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
                descricao = ?, categoria = ?, recorrente = ?, valor = ?, forma_pagamento = ?, 
                data_vencimento = ?, data_pagamento = ?, status = ?, usuario = ?, dataHora = CURRENT_TIMESTAMP
            WHERE id = ?
            """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            preencherStatement(stmt, despesa, usuarioLogado, true);
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
        d.setRecorrente(rs.getInt("recorrente") == 1);
        d.setValor(rs.getBigDecimal("valor"));
        d.setFormaPagamento(FormaPagamento.valueOf(rs.getString("forma_pagamento")));
        d.setDataVencimento(rs.getDate("data_vencimento").toLocalDate());

        Date dataPagamento = rs.getDate("data_pagamento");
        if (dataPagamento != null) {
            d.setDataPagamento(dataPagamento.toLocalDate());
        }

        d.setStatus(Status.valueOf(rs.getString("status")));
        d.setUsuario(rs.getString("usuario"));

        Timestamp ts = rs.getTimestamp("dataHora");
        if (ts != null) {
            d.setDataHora(ts.toLocalDateTime());
        }
        return d;
    }

    // ============================
    // AUXILIAR: preencher statement
    // ============================
    private void preencherStatement(PreparedStatement stmt, Despesa despesa, String usuarioLogado, boolean isUpdate) throws SQLException {
        stmt.setString(1, despesa.getDescricao());
        stmt.setString(2, despesa.getCategoria().name());
        stmt.setInt(3, despesa.isRecorrente() ? 1 : 0);
        stmt.setBigDecimal(4, despesa.getValor());
        stmt.setString(5, despesa.getFormaPagamento().name());
        stmt.setDate(6, Date.valueOf(despesa.getDataVencimento()));

        if (despesa.getDataPagamento() != null) {
            stmt.setDate(7, Date.valueOf(despesa.getDataPagamento()));
        } else {
            stmt.setNull(7, Types.DATE);
        }

        stmt.setString(8, despesa.getStatus().name());
        stmt.setString(9, usuarioLogado);

        if (isUpdate) {
            stmt.setInt(10, despesa.getId());
        }
    }
}
