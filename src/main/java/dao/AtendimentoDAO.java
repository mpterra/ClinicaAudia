package dao;

import model.Atendimento;
import model.Paciente;
import model.Profissional;
import model.EmpresaParceira;
import util.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AtendimentoDAO {

    // ============================
    // CREATE
    // ============================
    public boolean salvar(Atendimento at, String usuarioLogado) throws SQLException {
        String sql = """
            INSERT INTO atendimento 
            (paciente_id, profissional_id, empresa_parceira_id, data_hora, duracao_min, tipo, situacao, notas, valor, status_pagamento, usuario)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, at.getPacienteId());
            stmt.setInt(2, at.getProfissionalId());
            stmt.setInt(3, at.getEmpresaParceiraId());
            stmt.setTimestamp(4, at.getDataHora());
            stmt.setInt(5, at.getDuracaoMin());
            stmt.setString(6, at.getTipo().name());
            stmt.setString(7, at.getSituacao().name());
            stmt.setString(8, at.getNotas());
            stmt.setBigDecimal(9, at.getValor());
            stmt.setString(10, at.getStatusPagamento().name());
            stmt.setString(11, usuarioLogado);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Falha ao inserir Atendimento.");

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) at.setId(rs.getInt(1));
            }
        }
        return true;
    }

    // ============================
    // READ
    // ============================
    public Atendimento buscarPorId(int id) throws SQLException {
        String sql = """
            SELECT a.*, 
                   p.nome AS pacienteNome, 
                   pr.nome AS profissionalNome,
                   ep.nome AS empresaNome
            FROM atendimento a
            JOIN paciente p ON a.paciente_id = p.id
            JOIN profissional pr ON a.profissional_id = pr.id
            LEFT JOIN empresa_parceira ep ON a.empresa_parceira_id = ep.id
            WHERE a.id = ?
            """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Atendimento> listarTodos() throws SQLException {
        List<Atendimento> lista = new ArrayList<>();
        String sql = """
            SELECT a.*, 
                   p.nome AS pacienteNome, 
                   pr.nome AS profissionalNome,
                   ep.nome AS empresaNome
            FROM atendimento a
            JOIN paciente p ON a.paciente_id = p.id
            JOIN profissional pr ON a.profissional_id = pr.id
            LEFT JOIN empresa_parceira ep ON a.empresa_parceira_id = ep.id
            ORDER BY a.data_hora
            """;

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) lista.add(mapRow(rs));
        }
        return lista;
    }

    public List<Atendimento> listarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        List<Atendimento> lista = new ArrayList<>();
        String sql = """
            SELECT a.*, 
                   p.nome AS pacienteNome, 
                   pr.nome AS profissionalNome,
                   ep.nome AS empresaNome
            FROM atendimento a
            JOIN paciente p ON a.paciente_id = p.id
            JOIN profissional pr ON a.profissional_id = pr.id
            LEFT JOIN empresa_parceira ep ON a.empresa_parceira_id = ep.id
            WHERE a.data_hora BETWEEN ? AND ?
            ORDER BY a.data_hora
            """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(inicio));
            stmt.setTimestamp(2, Timestamp.valueOf(fim));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapRow(rs));
            }
        }
        return lista;
    }

    // ============================
    // UPDATE
    // ============================
    public boolean atualizar(Atendimento at, String usuarioLogado) throws SQLException {
        String sql = """
            UPDATE atendimento SET 
                paciente_id = ?, profissional_id = ?, empresa_parceira_id = ?, data_hora = ?, duracao_min = ?, 
                tipo = ?, situacao = ?, notas = ?, valor = ?, status_pagamento = ?, 
                usuario = ?, atualizado_em = CURRENT_TIMESTAMP
            WHERE id = ?
            """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, at.getPacienteId());
            stmt.setInt(2, at.getProfissionalId());
            stmt.setInt(3, at.getEmpresaParceiraId());
            stmt.setTimestamp(4, at.getDataHora());
            stmt.setInt(5, at.getDuracaoMin());
            stmt.setString(6, at.getTipo().name());
            stmt.setString(7, at.getSituacao().name());
            stmt.setString(8, at.getNotas());
            stmt.setBigDecimal(9, at.getValor());
            stmt.setString(10, at.getStatusPagamento().name());
            stmt.setString(11, usuarioLogado);
            stmt.setInt(12, at.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    // ============================
    // DELETE
    // ============================
    public boolean deletar(int id) throws SQLException {
        String sql = "DELETE FROM atendimento WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    // ============================
    // VERIFICAR DISPONIBILIDADE
    // ============================
    public boolean isDisponivel(int profissionalId, Timestamp dataHora, int duracaoMin, Integer idAtual) throws SQLException {
        LocalDateTime inicio = dataHora.toLocalDateTime();
        LocalDateTime fim = inicio.plusMinutes(duracaoMin);

        String sql = """
            SELECT COUNT(*) FROM atendimento
            WHERE profissional_id = ?
              AND situacao != 'CANCELADO'
              AND (? IS NULL OR id != ?)
              AND (
                (? >= data_hora AND ? < DATE_ADD(data_hora, INTERVAL duracao_min MINUTE))
                OR
                (? > data_hora AND ? <= DATE_ADD(data_hora, INTERVAL duracao_min MINUTE))
                OR
                (data_hora >= ? AND data_hora < ?)
              )
            """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, profissionalId);
            if (idAtual != null) {
                stmt.setInt(2, idAtual);
                stmt.setInt(3, idAtual);
            } else {
                stmt.setNull(2, Types.INTEGER);
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.setTimestamp(4, dataHora);
            stmt.setTimestamp(5, Timestamp.valueOf(inicio));
            stmt.setTimestamp(6, Timestamp.valueOf(fim));
            stmt.setTimestamp(7, Timestamp.valueOf(fim));
            stmt.setTimestamp(8, dataHora);
            stmt.setTimestamp(9, Timestamp.valueOf(fim));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) == 0;
            }
        }
        return true;
    }

    // ============================
    // MAPEAMENTO
    // ============================
    private Atendimento mapRow(ResultSet rs) throws SQLException {
        Atendimento at = new Atendimento();
        at.setId(rs.getInt("id"));
        at.setPacienteId(rs.getInt("paciente_id"));
        at.setProfissionalId(rs.getInt("profissional_id"));
        at.setEmpresaParceiraId(rs.getInt("empresa_parceira_id"));
        at.setDataHora(rs.getTimestamp("data_hora"));
        at.setDuracaoMin(rs.getInt("duracao_min"));
        at.setTipo(Atendimento.Tipo.valueOf(rs.getString("tipo")));
        at.setSituacao(Atendimento.Situacao.valueOf(rs.getString("situacao")));
        at.setNotas(rs.getString("notas"));
        at.setValor(rs.getBigDecimal("valor"));
        at.setStatusPagamento(Atendimento.StatusPagamento.valueOf(rs.getString("status_pagamento")));
        at.setCriadoEm(rs.getTimestamp("criado_em"));
        at.setAtualizadoEm(rs.getTimestamp("atualizado_em"));
        at.setUsuario(rs.getString("usuario"));

        // Mapear Paciente
        Paciente p = new Paciente();
        p.setId(at.getPacienteId());
        p.setNome(rs.getString("pacienteNome"));
        at.setPaciente(p);

        // Mapear Profissional
        Profissional pr = new Profissional();
        pr.setId(at.getProfissionalId());
        pr.setNome(rs.getString("profissionalNome"));
        at.setProfissional(pr);

        // Mapear Empresa Parceira (opcional)
        int empresaId = rs.getInt("empresa_parceira_id");
        if (!rs.wasNull()) {
            EmpresaParceira ep = new EmpresaParceira();
            ep.setId(empresaId);
            ep.setNome(rs.getString("empresaNome"));
            at.setEmpresaParceira(ep);
        }

        return at;
    }
}