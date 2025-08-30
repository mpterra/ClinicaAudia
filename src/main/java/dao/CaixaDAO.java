package dao;

import model.Caixa;
import util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CaixaDAO {

    // -------------------------
    // CRUD básico
    // -------------------------

    // Inserir novo caixa
    public void inserir(Caixa caixa) throws SQLException {
        String sql = "INSERT INTO caixa (data_abertura, saldo_inicial_dinheiro, saldo_inicial_cartao, saldo_inicial_pix, observacoes, usuario) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setTimestamp(1, Timestamp.valueOf(caixa.getDataAbertura()));
            stmt.setBigDecimal(2, caixa.getSaldoInicialDinheiro());
            stmt.setBigDecimal(3, caixa.getSaldoInicialCartao());
            stmt.setBigDecimal(4, caixa.getSaldoInicialPix());
            stmt.setString(5, caixa.getObservacoes());
            stmt.setString(6, caixa.getUsuario());

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                caixa.setId(rs.getInt(1));
            }
        }
    }

    // Atualizar observações e saldos iniciais
    public void atualizar(Caixa caixa) throws SQLException {
        String sql = "UPDATE caixa SET saldo_inicial_dinheiro = ?, saldo_inicial_cartao = ?, saldo_inicial_pix = ?, observacoes = ?, usuario = ? " +
                     "WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBigDecimal(1, caixa.getSaldoInicialDinheiro());
            stmt.setBigDecimal(2, caixa.getSaldoInicialCartao());
            stmt.setBigDecimal(3, caixa.getSaldoInicialPix());
            stmt.setString(4, caixa.getObservacoes());
            stmt.setString(5, caixa.getUsuario());
            stmt.setInt(6, caixa.getId());

            stmt.executeUpdate();
        }
    }

    // Deletar caixa
    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM caixa WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // Buscar caixa por ID
    public Caixa buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM caixa WHERE id = ?";
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

    // Listar todos os caixas
    public List<Caixa> listarTodos() throws SQLException {
        List<Caixa> lista = new ArrayList<>();
        String sql = "SELECT * FROM caixa ORDER BY data_abertura DESC";
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
    // Métodos específicos
    // -------------------------

    // Fechar caixa
    public void fecharCaixa(Caixa caixa) throws SQLException {
        String sql = "UPDATE caixa SET data_fechamento = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            LocalDateTime agora = LocalDateTime.now();
            stmt.setTimestamp(1, Timestamp.valueOf(agora));
            stmt.setInt(2, caixa.getId());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                caixa.setDataFechamento(agora);
            }
        }
    }

    // Buscar caixa aberto (último não fechado)
    public Caixa buscarCaixaAberto() throws SQLException {
        String sql = "SELECT * FROM caixa WHERE data_fechamento IS NULL ORDER BY data_abertura DESC LIMIT 1";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        }
        return null;
    }

    // -------------------------
    // Mapeamento ResultSet -> Objeto
    // -------------------------
    private Caixa mapResultSet(ResultSet rs) throws SQLException {
        Caixa caixa = new Caixa();
        caixa.setId(rs.getInt("id"));

        Timestamp tsAbertura = rs.getTimestamp("data_abertura");
        if (tsAbertura != null) caixa.setDataAbertura(tsAbertura.toLocalDateTime());

        Timestamp tsFechamento = rs.getTimestamp("data_fechamento");
        if (tsFechamento != null) caixa.setDataFechamento(tsFechamento.toLocalDateTime());

        caixa.setSaldoInicialDinheiro(rs.getBigDecimal("saldo_inicial_dinheiro"));
        caixa.setSaldoInicialCartao(rs.getBigDecimal("saldo_inicial_cartao"));
        caixa.setSaldoInicialPix(rs.getBigDecimal("saldo_inicial_pix"));
        caixa.setObservacoes(rs.getString("observacoes"));
        caixa.setUsuario(rs.getString("usuario"));

        return caixa;
    }
}
