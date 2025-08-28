package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe utilitária para conexão com o banco MySQL "audia".
 * Segue padrão Singleton opcional para reutilização da conexão.
 */
public class Database {

    // Configurações de conexão
    private static final String URL = "jdbc:mysql://localhost:3306/audia?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";     // seu usuário MySQL
    private static final String PASSWORD = "Eng%3571";     // sua senha MySQL

    // Construtor privado para evitar instâncias
    private Database() {
    	
    }

    /**
     * Retorna uma conexão nova com o banco.
     * @return Connection
     * @throws SQLException em caso de falha na conexão
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Testa a conexão com o banco.
     */
    public static void testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Conexão com MySQL realizada com sucesso!");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao conectar ao MySQL: " + e.getMessage());
        }
    }

    // Para uso rápido: exemplo de main para teste
    public static void main(String[] args) {
        testConnection();
    }
}
