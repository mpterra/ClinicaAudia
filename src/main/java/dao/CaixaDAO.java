package dao;

import model.Caixa;
import util.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// DAO para operações na tabela caixa
public class CaixaDAO {

    // -------------------------
    // CRUD básico
    // -------------------------

    // Inserir novo caixa
    public boolean inserir(Caixa caixa) throws SQLException {
        String sql = "INSERT INTO caixa (data_abertura, saldo_inicial_dinheiro, saldo_inicial_debito, saldo_inicial_credito, saldo_inicial_pix, observacoes, usuario) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setTimestamp(1, Timestamp.valueOf(caixa.getDataAbertura()));
            stmt.setBigDecimal(2, caixa.getSaldoInicialDinheiro());
            stmt.setBigDecimal(3, caixa.getSaldoInicialDebito());
            stmt.setBigDecimal(4, caixa.getSaldoInicialCredito());
            stmt.setBigDecimal(4, caixa.getSaldoInicialPix());
            stmt.setString(5, caixa.getObservacoes());
            stmt.setString(6, caixa.getUsuario());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    caixa.setId(rs.getInt(1));
                }
                return true; // Sucesso na inserção
            }
            return false; // Falha na inserção
        }
    }

    // Atualizar observações e saldos iniciais
    public boolean atualizar(Caixa caixa) throws SQLException {
        // Verifica se o caixa está fechado
        Caixa existente = buscarPorId(caixa.getId());
        if (existente == null) {
            throw new SQLException("Caixa não encontrado.");
        }
        if (existente.getDataFechamento() != null) {
            throw new SQLException("Não é possível atualizar um caixa já fechado.");
        }

        String sql = "UPDATE caixa SET saldo_inicial_dinheiro = ?, saldo_inicial_debito = ?, saldo_inicial_credito = ?, saldo_inicial_pix = ?, observacoes = ?, usuario = ? " +
                     "WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBigDecimal(1, caixa.getSaldoInicialDinheiro());
            stmt.setBigDecimal(2, caixa.getSaldoInicialDebito());
            stmt.setBigDecimal(3, caixa.getSaldoInicialCredito());
            stmt.setBigDecimal(3, caixa.getSaldoInicialPix());
            stmt.setString(4, caixa.getObservacoes());
            stmt.setString(5, caixa.getUsuario());
            stmt.setInt(6, caixa.getId());

            return stmt.executeUpdate() > 0; // Retorna true se atualizado com sucesso
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
    public boolean fecharCaixa(Caixa caixa) throws SQLException {
        String sql = "UPDATE caixa SET data_fechamento = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            LocalDateTime agora = LocalDateTime.now();
            stmt.setTimestamp(1, Timestamp.valueOf(agora));
            stmt.setInt(2, caixa.getId());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                caixa.setDataFechamento(agora);
                return true; // Sucesso no fechamento
            }
            return false; // Falha no fechamento
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
        caixa.setSaldoInicialDebito(rs.getBigDecimal("saldo_inicial_debito"));
        caixa.setSaldoInicialCredito(rs.getBigDecimal("saldo_inicial_credito"));
        caixa.setSaldoInicialPix(rs.getBigDecimal("saldo_inicial_pix"));
        caixa.setObservacoes(rs.getString("observacoes"));
        caixa.setUsuario(rs.getString("usuario"));

        return caixa;
    }
}