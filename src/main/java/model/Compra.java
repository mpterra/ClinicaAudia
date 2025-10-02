package model;

import java.sql.Timestamp;

public class Compra {

    private int id;
    private Timestamp dataCompra;
    private String usuario;

    // Construtor vazio
    public Compra() {}

    // Construtor completo
    public Compra(int id, Timestamp dataCompra, String usuario) {
        this.id = id;
        this.dataCompra = dataCompra;
        this.usuario = usuario;
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

    // toString (opcional, útil para debug)
    @Override
    public String toString() {
        return "Compra{" +
                "id=" + id +
                ", dataCompra=" + dataCompra +
                ", usuario='" + usuario + '\'' +
                '}';
    }
}
