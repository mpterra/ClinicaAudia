package dao;

import model.EmpresaParceira;
import model.Endereco;
import util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmpresaParceiraDAO {

    // -----------------------------
    // CRUD Básico
    // -----------------------------

    // Salva uma nova empresa parceira no banco
    public boolean salvar(EmpresaParceira empresa) throws SQLException {
        String sql = "INSERT INTO empresa_parceira (nome, cnpj, telefone, email, id_endereco, usuario, criado_em) " +
                     "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, empresa.getNome());
            stmt.setString(2, empresa.getCnpj());
            stmt.setString(3, empresa.getTelefone());
            stmt.setString(4, empresa.getEmail());
            if (empresa.getEndereco() != null) {
                stmt.setInt(5, empresa.getEndereco().getId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            stmt.setString(6, empresa.getUsuario());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    empresa.setId(rs.getInt(1));
                    return true;
                }
            }
        }
        return false;
    }

    // Atualiza uma empresa parceira existente
    public void atualizar(EmpresaParceira empresa) throws SQLException {
        String sql = "UPDATE empresa_parceira SET nome=?, cnpj=?, telefone=?, email=?, id_endereco=?, usuario=?, atualizado_em=CURRENT_TIMESTAMP WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, empresa.getNome());
            stmt.setString(2, empresa.getCnpj());
            stmt.setString(3, empresa.getTelefone());
            stmt.setString(4, empresa.getEmail());
            if (empresa.getEndereco() != null) {
                stmt.setInt(5, empresa.getEndereco().getId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            stmt.setString(6, empresa.getUsuario());
            stmt.setInt(7, empresa.getId());

            stmt.executeUpdate();
        }
    }

    // Deleta uma empresa parceira pelo ID
    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM empresa_parceira WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // Busca uma empresa parceira pelo ID
    public EmpresaParceira buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM empresa_parceira WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearEmpresaParceira(rs);
                }
            }
        }
        return null;
    }

    // -----------------------------
    // Buscas simples
    // -----------------------------

    // Busca empresas por nome (parcial)
    public List<EmpresaParceira> buscarPorNome(String nome) throws SQLException {
        String sql = "SELECT * FROM empresa_parceira WHERE nome LIKE ?";
        List<EmpresaParceira> lista = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + nome + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearEmpresaParceira(rs));
                }
            }
        }
        return lista;
    }

    // Busca empresas por CNPJ
    public List<EmpresaParceira> buscarPorCnpj(String cnpj) throws SQLException {
        String sql = "SELECT * FROM empresa_parceira WHERE cnpj=?";
        List<EmpresaParceira> lista = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cnpj);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearEmpresaParceira(rs));
                }
            }
        }
        return lista;
    }

    // Lista todas as empresas parceiras
    public List<EmpresaParceira> listarTodos() throws SQLException {
        String sql = "SELECT * FROM empresa_parceira";
        List<EmpresaParceira> lista = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearEmpresaParceira(rs));
            }
        }
        return lista;
    }

    // Lista todas as empresas ordenadas por ID (descendente)
    public List<EmpresaParceira> listarTodosOrdenadoPorIdDesc() throws SQLException {
        String sql = "SELECT * FROM empresa_parceira ORDER BY id DESC";
        List<EmpresaParceira> lista = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearEmpresaParceira(rs));
            }
        }
        return lista;
    }

    // -----------------------------
    // Busca avançada
    // -----------------------------

    // Busca avançada com filtros opcionais
    public List<EmpresaParceira> buscarAvancado(String nome, String cnpj) throws SQLException {
        List<EmpresaParceira> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM empresa_parceira WHERE 1=1");

        if (nome != null && !nome.isEmpty()) {
            sql.append(" AND nome LIKE ?");
        }
        if (cnpj != null && !cnpj.isEmpty()) {
            sql.append(" AND cnpj=?");
        }

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int index = 1;
            if (nome != null && !nome.isEmpty()) {
                stmt.setString(index++, "%" + nome + "%");
            }
            if (cnpj != null && !cnpj.isEmpty()) {
                stmt.setString(index++, cnpj);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearEmpresaParceira(rs));
                }
            }
        }
        return lista;
    }

    // -----------------------------
    // Método auxiliar para mapear ResultSet
    // -----------------------------

    private EmpresaParceira mapearEmpresaParceira(ResultSet rs) throws SQLException {
        EmpresaParceira empresa = new EmpresaParceira();
        empresa.setId(rs.getInt("id"));
        empresa.setNome(rs.getString("nome"));
        empresa.setCnpj(rs.getString("cnpj"));
        empresa.setTelefone(rs.getString("telefone"));
        empresa.setEmail(rs.getString("email"));

        int enderecoId = rs.getInt("id_endereco");
        if (!rs.wasNull()) {
            Endereco e = new Endereco();
            e.setId(enderecoId);
            empresa.setEndereco(e);
        }

        empresa.setCriadoEm(rs.getTimestamp("criado_em") != null ? 
                            rs.getTimestamp("criado_em").toLocalDateTime() : null);
        empresa.setAtualizadoEm(rs.getTimestamp("atualizado_em") != null ? 
                                rs.getTimestamp("atualizado_em").toLocalDateTime() : null);
        empresa.setUsuario(rs.getString("usuario"));

        return empresa;
    }
}