package dao;

import model.Endereco;
import model.Profissional;
import util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProfissionalDAO {

    // -----------------------------
    // CRUD Básico
    // -----------------------------

    public boolean salvar(Profissional prof) throws SQLException {
        String sql = "INSERT INTO profissional (nome, sexo, cpf, email, telefone, data_nascimento, tipo, endereco_id, ativo, usuario, criado_em) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, prof.getNome());
            stmt.setString(2, prof.getSexo());
            stmt.setString(3, prof.getCpf());
            stmt.setString(4, prof.getEmail());
            stmt.setString(5, prof.getTelefone());
            stmt.setDate(6, prof.getDataNascimento() != null ? Date.valueOf(prof.getDataNascimento()) : null);
            stmt.setString(7, prof.getTipo());
            if (prof.getEndereco() != null) {
                stmt.setInt(8, prof.getEndereco().getId());
            } else {
                stmt.setNull(8, Types.INTEGER);
            }
            stmt.setBoolean(9, prof.isAtivo());
            stmt.setString(10, prof.getUsuario());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) return false;

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    prof.setId(rs.getInt(1));
                    return true;
                }
            }
        }
        return false;
    }

    public void atualizar(Profissional prof) throws SQLException {
        String sql = "UPDATE profissional SET nome=?, sexo=?, cpf=?, email=?, telefone=?, data_nascimento=?, tipo=?, endereco_id=?, ativo=?, usuario=?, atualizado_em=CURRENT_TIMESTAMP WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, prof.getNome());
            stmt.setString(2, prof.getSexo());
            stmt.setString(3, prof.getCpf());
            stmt.setString(4, prof.getEmail());
            stmt.setString(5, prof.getTelefone());
            stmt.setDate(6, prof.getDataNascimento() != null ? Date.valueOf(prof.getDataNascimento()) : null);
            stmt.setString(7, prof.getTipo());
            if (prof.getEndereco() != null) {
                stmt.setInt(8, prof.getEndereco().getId());
            } else {
                stmt.setNull(8, Types.INTEGER);
            }
            stmt.setBoolean(9, prof.isAtivo());
            stmt.setString(10, prof.getUsuario());
            stmt.setInt(11, prof.getId());

            stmt.executeUpdate();
        }
    }

    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM profissional WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public Profissional buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM profissional WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearProfissional(rs);
                }
            }
        }
        return null;
    }

    // -----------------------------
    // Buscas simples
    // -----------------------------

    public List<Profissional> buscarPorNome(String nome) throws SQLException {
        String sql = "SELECT * FROM profissional WHERE nome LIKE ?";
        List<Profissional> lista = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + nome + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearProfissional(rs));
                }
            }
        }
        return lista;
    }

    public List<Profissional> buscarPorTipo(String tipo) throws SQLException {
        String sql = "SELECT * FROM profissional WHERE tipo=?";
        List<Profissional> lista = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tipo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearProfissional(rs));
                }
            }
        }
        return lista;
    }

    public List<Profissional> buscarPorAtivo(boolean ativo) throws SQLException {
        String sql = "SELECT * FROM profissional WHERE ativo=?";
        List<Profissional> lista = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, ativo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearProfissional(rs));
                }
            }
        }
        return lista;
    }

    // -----------------------------
    // Busca avançada (filtros opcionais)
    // -----------------------------
    public List<Profissional> buscarAvancado(String nome, String tipo, Boolean ativo) throws SQLException {
        List<Profissional> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM profissional WHERE 1=1");

        if (nome != null && !nome.isEmpty())
            sql.append(" AND nome LIKE ?");
        if (tipo != null && !tipo.isEmpty())
            sql.append(" AND tipo=?");
        if (ativo != null)
            sql.append(" AND ativo=?");

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int index = 1;
            if (nome != null && !nome.isEmpty())
                stmt.setString(index++, "%" + nome + "%");
            if (tipo != null && !tipo.isEmpty())
                stmt.setString(index++, tipo);
            if (ativo != null)
                stmt.setBoolean(index++, ativo);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearProfissional(rs));
                }
            }
        }
        return lista;
    }

    // -----------------------------
    // Método auxiliar para mapear ResultSet
    // -----------------------------
    private Profissional mapearProfissional(ResultSet rs) throws SQLException {
        Profissional p = new Profissional();
        p.setId(rs.getInt("id"));
        p.setNome(rs.getString("nome"));
        p.setSexo(rs.getString("sexo"));
        p.setCpf(rs.getString("cpf"));
        p.setEmail(rs.getString("email"));
        p.setTelefone(rs.getString("telefone"));
        p.setDataNascimento(rs.getDate("data_nascimento") != null ? rs.getDate("data_nascimento").toLocalDate() : null);
        p.setTipo(rs.getString("tipo"));

        int enderecoId = rs.getInt("endereco_id");
        if (!rs.wasNull()) {
            Endereco e = new Endereco();
            e.setId(enderecoId);
            p.setEndereco(e);
        }

        p.setAtivo(rs.getBoolean("ativo"));
        p.setUsuario(rs.getString("usuario"));

        return p;
    }

    public List<Profissional> listarTodos() throws SQLException {
        String sql = "SELECT * FROM profissional";
        List<Profissional> lista = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearProfissional(rs));
            }
        }
        return lista;
    }
}
