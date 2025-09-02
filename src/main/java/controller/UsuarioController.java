package controller;

import dao.UsuarioDAO;
import exception.LoginDuplicadoException;
import model.Usuario;
import util.Sessao;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;

public class UsuarioController {

    private final UsuarioDAO dao;

    public UsuarioController() {
        this.dao = new UsuarioDAO();
    }

    // ============================
    // CREATE
    // ============================
    public boolean salvar(Usuario usuario) throws SQLException, LoginDuplicadoException {
        // Converter senha em hash antes de salvar
        String hash = gerarHash(usuario.getSenha());
        usuario.setSenha(hash);

        String sessao = Sessao.getUsuarioLogado().getLogin();
        return dao.salvar(usuario, sessao);
    }

    // ============================
    // READ
    // ============================
    public Usuario buscarPorLogin(String login) throws SQLException {
        return dao.buscarPorLogin(login);
    }

    public List<Usuario> listarTodos() throws SQLException {
        return dao.listarTodos();
    }

    // ============================
    // UPDATE
    // ============================
    public boolean atualizar(Usuario usuario) throws SQLException, LoginDuplicadoException {
        // Converter senha em hash antes de atualizar
        String hash = gerarHash(usuario.getSenha());
        usuario.setSenha(hash);
        return dao.atualizar(usuario);
    }

    public boolean atualizarStatus(int id, int ativo, String usuarioLogado) throws SQLException {
        return dao.atualizarStatus(id, ativo, usuarioLogado);
    }

    // ============================
    // DELETE
    // ============================
    public boolean deletar(int id) throws SQLException {
        return dao.deletar(id);
    }

    // ============================
    // LOGIN
    // ============================
    public Usuario login(String login, String senhaDigitada) throws SQLException {
        Usuario usuario = dao.buscarPorLogin(login);
        if (usuario == null) {
            return null; // Usuário não existe
        }

        String hashDigitado = gerarHash(senhaDigitada);
        if (hashDigitado.equals(usuario.getSenha())) {
            return usuario; // Login bem-sucedido
        } else {
            return null; // Senha incorreta
        }
    }

    // ============================
    // UTIL: Geração de Hash
    // ============================
    private String gerarHash(String senha) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(senha.getBytes());

            // Converter para String hexadecimal
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao gerar hash da senha", e);
        }
    }
}
