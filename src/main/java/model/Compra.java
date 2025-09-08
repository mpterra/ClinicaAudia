package model;

import java.sql.Timestamp;

public class Compra {

    private int id;
    private String fornecedor;
    private Timestamp dataCompra;
    private String usuario;

    // Construtor vazio
    public Compra() {}

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFornecedor() { return fornecedor; }
    public void setFornecedor(String fornecedor) { this.fornecedor = fornecedor; }

    public Timestamp getDataCompra() { return dataCompra; }
    public void setDataCompra(Timestamp dataCompra) { this.dataCompra = dataCompra; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
}
