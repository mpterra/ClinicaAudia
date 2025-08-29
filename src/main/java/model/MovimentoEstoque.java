package model;

import java.time.LocalDateTime;

public class MovimentoEstoque {
    private int id;
    private Produto produto; // relacionamento com Produto
    private int quantidade;
    private TipoMovimento tipo;
    private String observacoes;
    private LocalDateTime dataHora;
    private String usuario;

    // Enum para representar os tipos de movimento
    public enum TipoMovimento {
        ENTRADA, SAIDA, AJUSTE
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public TipoMovimento getTipo() {
        return tipo;
    }

    public void setTipo(String tipoStr) {
        try {
            this.tipo = TipoMovimento.valueOf(tipoStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de movimento inválido: " + tipoStr);
        }
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    @Override
    public String toString() {
        return "MovimentoEstoque{" +
                "id=" + id +
                ", produto=" + (produto != null ? produto.getNome() : "null") +
                ", quantidade=" + quantidade +
                ", tipo=" + tipo +
                ", observacoes='" + observacoes + '\'' +
                ", dataHora=" + dataHora +
                ", usuario='" + usuario + '\'' +
                '}';
    }
}
