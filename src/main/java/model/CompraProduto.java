package model;

import java.math.BigDecimal;

public class CompraProduto {

    private int compraId;
    private int produtoId;
    private int quantidade;
    private BigDecimal precoUnitario;

    // Construtor vazio
    public CompraProduto() {}

    // Getters e Setters
    public int getCompraId() { return compraId; }
    public void setCompraId(int compraId) { this.compraId = compraId; }

    public int getProdutoId() { return produtoId; }
    public void setProdutoId(int produtoId) { this.produtoId = produtoId; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(BigDecimal precoUnitario) { this.precoUnitario = precoUnitario; }
}
