package model;

import java.sql.Timestamp;

public class MovimentoEstoque {

    public enum Tipo {
        ENTRADA, SAIDA, AJUSTE
    }

    private int id;
    private int produtoId;
    private int quantidade;
    private Tipo tipo;
    private String observacoes;
    private Timestamp dataHora;
    private String usuario;

    // Construtor vazio
    public MovimentoEstoque() {}

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProdutoId() { return produtoId; }
    public void setProdutoId(int produtoId) { this.produtoId = produtoId; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public Tipo getTipo() { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public Timestamp getDataHora() { return dataHora; }
    public void setDataHora(Timestamp dataHora) { this.dataHora = dataHora; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
}
