package model;

import java.sql.Timestamp;

public class Compra {

    private int id;
    private Timestamp dataCompra;
    private String usuario;
    private boolean cancelada; // Novo campo para indicar se a compra foi cancelada

    // Construtor vazio
    public Compra() {
        this.cancelada = false; // Inicializa como não cancelada por padrão
    }

    // Construtor completo
    public Compra(int id, Timestamp dataCompra, String usuario, boolean cancelada) {
        this.id = id;
        this.dataCompra = dataCompra;
        this.usuario = usuario;
        this.cancelada = cancelada;
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Timestamp getDataCompra() {
        return dataCompra;
    }

    public void setDataCompra(Timestamp dataCompra) {
        this.dataCompra = dataCompra;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public boolean isCancelada() {
        return cancelada;
    }

    public void setCancelada(boolean cancelada) {
        this.cancelada = cancelada;
    }

    // toString (atualizado para incluir o campo cancelada)
    @Override
    public String toString() {
        return "Compra{" +
                "id=" + id +
                ", dataCompra=" + dataCompra +
                ", usuario='" + usuario + '\'' +
                ", cancelada=" + cancelada +
                '}';
    }
}