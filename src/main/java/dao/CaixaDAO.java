package dao;

import model.Caixa;
import util.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CaixaDAO {

    // -------------------------
    // CRUD básico
    // -------------------------

    // Inserir novo caixa (apenas abertura)
    public boolean inserir(Caixa caixa) throws SQLException {
        String sql = """
                INSERT INTO caixa 
                (data_abertura, saldo_inicial_dinheiro, saldo_inicial_debito, saldo_inicial_credito, saldo_inicial_pix, observacoes, usuario, fechado) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setTimestamp(1, Timestamp.valueOf(caixa.getDataAbertura()));
            stmt.setBigDecimal(2, caixa.getSaldoInicialDinheiro());
            stmt.setBigDecimal(3, caixa.getSaldoInicialDebito());
            stmt.setBigDecimal(4, caixa.getSaldoInicialCredito());
            stmt.setBigDecimal(5, caixa.getSaldoInicialPix());
            stmt.setString(6, caixa.getObservacoes());
            stmt.setString(7, caixa.getUsuario());
            stmt.setBoolean(8, caixa.isFechado());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    caixa.setId(rs.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    // Atualizar informações de um caixa aberto
    public boolean atualizar(Caixa caixa) throws SQLException {
        Caixa existente = buscarPorId(caixa.getId());
        if (existente == null) {
            throw new SQLException("Caixa não encontrado.");
        }
        if (existente.isFechado()) {
            throw new SQLException("Não é possível atualizar um caixa já fechado.");
        }

        String sql = """
                UPDATE caixa 
                SET saldo_inicial_dinheiro = ?, saldo_inicial_debito = ?, saldo_inicial_credito = ?, saldo_inicial_pix = ?, 
                    observacoes = ?, usuario = ? 
                WHERE id = ?
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBigDecimal(1, caixa.getSaldoInicialDinheiro());
            stmt.setBigDecimal(2, caixa.getSaldoInicialDebito());
            stmt.setBigDecimal(3, caixa.getSaldoInicialCredito());
            stmt.setBigDecimal(4, caixa.getSaldoInicialPix());
            stmt.setString(5, caixa.getObservacoes());
            stmt.setString(6, caixa.getUsuario());
            stmt.setInt(7, caixa.getId());

            return stmt.executeUpdate() > 0;
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

    // Buscar por ID
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

    // Listar todos
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

    // Fechar caixa (salva saldo final + data fechamento + flag fechado)
    public boolean fecharCaixa(Caixa caixa) throws SQLException {
        String sql = """
                UPDATE caixa 
                SET data_fechamento = ?, 
                    saldo_final_dinheiro = ?, saldo_final_debito = ?, saldo_final_credito = ?, saldo_final_pix = ?, 
                    fechado = 1 
                WHERE id = ?
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            LocalDateTime agora = LocalDateTime.now();

            stmt.setTimestamp(1, Timestamp.valueOf(agora));
            stmt.setBigDecimal(2, caixa.getSaldoFinalDinheiro());
            stmt.setBigDecimal(3, caixa.getSaldoFinalDebito());
            stmt.setBigDecimal(4, caixa.getSaldoFinalCredito());
            stmt.setBigDecimal(5, caixa.getSaldoFinalPix());
            stmt.setInt(6, caixa.getId());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                caixa.setDataFechamento(agora);
                caixa.setFechado(true);
                return true;
            }
            return false;
        }
    }

    // Buscar caixa aberto (último não fechado)
    public Caixa buscarCaixaAberto() throws SQLException {
        String sql = "SELECT * FROM caixa WHERE fechado = 0 ORDER BY data_abertura DESC LIMIT 1";
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
        caixa.setSaldoInicialDebito(rs.getBigDecimal("saldo_inicial_debito"));
        caixa.setSaldoInicialCredito(rs.getBigDecimal("saldo_inicial_credito"));
        caixa.setSaldoInicialPix(rs.getBigDecimal("saldo_inicial_pix"));

        caixa.setSaldoFinalDinheiro(rs.getBigDecimal("saldo_final_dinheiro"));
        caixa.setSaldoFinalDebito(rs.getBigDecimal("saldo_final_debito"));
        caixa.setSaldoFinalCredito(rs.getBigDecimal("saldo_final_credito"));
        caixa.setSaldoFinalPix(rs.getBigDecimal("saldo_final_pix"));

        caixa.setFechado(rs.getBoolean("fechado"));
        caixa.setObservacoes(rs.getString("observacoes"));
        caixa.setUsuario(rs.getString("usuario"));

        return caixa;
    }
}
