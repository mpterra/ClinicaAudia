package dao;

import model.ValorAtendimentoEmpresa;
import util.Database;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ValorAtendimentoEmpresaDAO {

    // Salva ou atualiza um valor de atendimento por empresa
    public boolean salvar(ValorAtendimentoEmpresa valor) throws SQLException {
        String sql = "INSERT INTO valor_atendimento_empresa (profissional_id, empresa_parceira_id, tipo, valor, usuario, criado_em) " +
                     "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP) " +
                     "ON DUPLICATE KEY UPDATE valor = ?, atualizado_em = CURRENT_TIMESTAMP, usuario = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, valor.getProfissionalId());
            stmt.setInt(2, valor.getEmpresaParceiraId());
            stmt.setString(3, valor.getTipo().name());
            stmt.setBigDecimal(4, valor.getValor());
            stmt.setString(5, valor.getUsuario());
            stmt.setBigDecimal(6, valor.getValor());
            stmt.setString(7, valor.getUsuario());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0 && valor.getId() == 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        valor.setId(rs.getInt(1));
                    }
                }
            }
            return affectedRows > 0;
        }
    }

    // Deleta um valor de atendimento por empresa
    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM valor_atendimento_empresa WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // Busca um valor de atendimento por empresa pelo ID
    public ValorAtendimentoEmpresa buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM valor_atendimento_empresa WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearValor(rs);
                }
            }
        }
        return null;
    }

    // Lista todos os valores de atendimento por empresa
    public List<ValorAtendimentoEmpresa> listarTodos() throws SQLException {
        String sql = "SELECT * FROM valor_atendimento_empresa";
        List<ValorAtendimentoEmpresa> lista = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearValor(rs));
            }
        }
        return lista;
    }

    // Busca valores por profissional e empresa
    public List<ValorAtendimentoEmpresa> buscarPorProfissionalEEmpresa(int profissionalId, int empresaParceiraId) throws SQLException {
        String sql = "SELECT * FROM valor_atendimento_empresa WHERE profissional_id = ? AND empresa_parceira_id = ?";
        List<ValorAtendimentoEmpresa> lista = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, profissionalId);
            stmt.setInt(2, empresaParceiraId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearValor(rs));
                }
            }
        }
        return lista;
    }

    // Método auxiliar para mapear ResultSet
    private ValorAtendimentoEmpresa mapearValor(ResultSet rs) throws SQLException {
        ValorAtendimentoEmpresa valor = new ValorAtendimentoEmpresa();
        valor.setId(rs.getInt("id"));
        valor.setProfissionalId(rs.getInt("profissional_id"));
        valor.setEmpresaParceiraId(rs.getInt("empresa_parceira_id"));
        valor.setTipo(ValorAtendimentoEmpresa.Tipo.valueOf(rs.getString("tipo")));
        valor.setValor(rs.getBigDecimal("valor"));
        valor.setCriadoEm(rs.getTimestamp("criado_em") != null ? rs.getTimestamp("criado_em").toLocalDateTime() : null);
        valor.setAtualizadoEm(rs.getTimestamp("atualizado_em") != null ? rs.getTimestamp("atualizado_em").toLocalDateTime() : null);
        valor.setUsuario(rs.getString("usuario"));
        return valor;
    }
}