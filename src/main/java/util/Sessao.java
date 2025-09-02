package util;

import model.Usuario;

public class Sessao {
    private static Usuario usuarioLogado;

    public static void setUsuario(Usuario usuario) {
        usuarioLogado = usuario;
    }

    public static Usuario getUsuarioLogado() {
        return usuarioLogado;
    }

    public static void limpar() {
        usuarioLogado = null;
    }
}