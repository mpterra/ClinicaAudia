package dao;

import model.Caixa;
import model.CaixaMovimento;
import model.PagamentoAtendimento;
import model.PagamentoVenda;
import util.Database;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// DAO para operações na tabela caixa_movimento
public class CaixaMovimentoDAO {

    // -------------------------
    // Inserir novo movimento
    // -------------------------
    public boolean inserir(CaixaMovimento movimento) throws SQLException {
        String sql = "INSERT INTO caixa_movimento " +
                "(caixa_id, tipo, origem, pagamento_atendimento_id, pagamento_venda_id, forma_pagamento, valor, descricao, data_hora, usuario) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setObject(1, movimento.getCaixa() != null ? movimento.getCaixa().getId() : null, Types.INTEGER);
            stmt.setString(2, movimento.getTipo().name());
            stmt.setString(3, movimento.getOrigem().name());
            stmt.setObject(4, movimento.getPagamentoAtendimento() != null ? movimento.getPagamentoAtendimento().getId() : null, Types.INTEGER);
            stmt.setObject(5, movimento.getPagamentoVenda() != null ? movimento.getPagamentoVenda().getId() : null, Types.INTEGER);
            stmt.setString(6, movimento.getFormaPagamento().name());
            stmt.setBigDecimal(7, movimento.getValor());
            stmt.setString(8, movimento.getDescricao());
            stmt.setTimestamp(9, Timestamp.valueOf(movimento.getDataHora()));
            stmt.setString(10, movimento.getUsuario());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    movimento.setId(rs.getInt(1));
                }
                return true; // Sucesso na inserção
            }
            return false; // Falha na inserção
        }
    }

    // -------------------------
    // Buscar movimento por ID
    // -------------------------
    public CaixaMovimento buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM caixa_movimento WHERE id = ?";
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
    // Listar todos os movimentos
    // -------------------------
    public List<CaixaMovimento> listarTodos() throws SQLException {
        List<CaixaMovimento> lista = new ArrayList<>();
        String sql = "SELECT * FROM caixa_movimento ORDER BY data_hora DESC";
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
    // Listar movimentos por caixa
    // -------------------------
    public List<CaixaMovimento> listarPorCaixa(Caixa caixa) throws SQLException {
        List<CaixaMovimento> lista = new ArrayList<>();
        String sql = "SELECT * FROM caixa_movimento WHERE caixa_id = ? ORDER BY data_hora DESC";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, caixa.getId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(mapResultSet(rs));
            }
        }
        return lista;
    }

    // -------------------------
    // Deletar movimento
    // -------------------------
    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM caixa_movimento WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // -------------------------
    // Calcular saldos finais por forma de pagamento
    // -------------------------
    public BigDecimal[] calcularSaldosFinais(int caixaId) throws SQLException {
        String sql = "SELECT forma_pagamento, SUM(CASE WHEN tipo = 'ENTRADA' THEN valor ELSE -valor END) AS saldo " +
                     "FROM caixa_movimento WHERE caixa_id = ? GROUP BY forma_pagamento";
        BigDecimal saldoDinheiro = BigDecimal.ZERO;
        BigDecimal saldoCartao = BigDecimal.ZERO;
        BigDecimal saldoPix = BigDecimal.ZERO;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, caixaId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String forma = rs.getString("forma_pagamento");
                BigDecimal saldo = rs.getBigDecimal("saldo") != null ? rs.getBigDecimal("saldo") : BigDecimal.ZERO;
                switch (forma) {
                    case "DINHEIRO":
                        saldoDinheiro = saldo;
                        break;
                    case "CARTAO":
                        saldoCartao = saldo;
                        break;
                    case "PIX":
                        saldoPix = saldo;
                        break;
                    default:
                        break;
                }
            }
        }

        // Adicionar saldos iniciais do caixa
        CaixaDAO caixaDAO = new CaixaDAO();
        Caixa caixa = caixaDAO.buscarPorId(caixaId);
        if (caixa != null) {
            saldoDinheiro = saldoDinheiro.add(caixa.getSaldoInicialDinheiro());
            saldoCartao = saldoCartao.add(caixa.getSaldoInicialCartao());
            saldoPix = saldoPix.add(caixa.getSaldoInicialPix());
        }

        return new BigDecimal[]{saldoDinheiro, saldoCartao, saldoPix};
    }

    // -------------------------
    // Mapeamento ResultSet -> Objeto
    // -------------------------
    private CaixaMovimento mapResultSet(ResultSet rs) throws SQLException {
        CaixaMovimento movimento = new CaixaMovimento();
        movimento.setId(rs.getInt("id"));

        int caixaId = rs.getInt("caixa_id");
        if (!rs.wasNull()) {
            Caixa caixa = new Caixa();
            caixa.setId(caixaId);
            movimento.setCaixa(caixa);
        }

        movimento.setTipo(CaixaMovimento.TipoMovimento.valueOf(rs.getString("tipo")));
        movimento.setOrigem(CaixaMovimento.OrigemMovimento.valueOf(rs.getString("origem")));

        int pagAtId = rs.getInt("pagamento_atendimento_id");
        if (!rs.wasNull()) {
            PagamentoAtendimento pa = new PagamentoAtendimento();
            pa.setId(pagAtId);
            movimento.setPagamentoAtendimento(pa);
        }

        int pagVendaId = rs.getInt("pagamento_venda_id");
        if (!rs.wasNull()) {
            PagamentoVenda pv = new PagamentoVenda();
            pv.setId(pagVendaId);
            movimento.setPagamentoVenda(pv);
        }

        movimento.setFormaPagamento(CaixaMovimento.FormaPagamento.valueOf(rs.getString("forma_pagamento")));
        movimento.setValor(rs.getBigDecimal("valor"));
        movimento.setDescricao(rs.getString("descricao"));

        Timestamp ts = rs.getTimestamp("data_hora");
        if (ts != null) movimento.setDataHora(ts.toLocalDateTime());

        movimento.setUsuario(rs.getString("usuario"));

        return movimento;
    }
}