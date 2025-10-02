package model;

import java.math.BigDecimal;

public class CompraProduto {

    private int compraId;
    private int produtoId;
    private int quantidade;
    private BigDecimal precoUnitario;
    private Integer fornecedorId; // pode ser NULL no banco

    // Construtor vazio
    public CompraProduto() {}

    // Construtor completo
    public CompraProduto(int compraId, int produtoId, int quantidade, BigDecimal precoUnitario, Integer fornecedorId) {
        this.compraId = compraId;
        this.produtoId = produtoId;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.fornecedorId = fornecedorId;
    }

    // Getters e Setters
    public int getCompraId() { return compraId; }
    public void setCompraId(int compraId) { this.compraId = compraId; }

    public int getProdutoId() { return produtoId; }
    public void setProdutoId(int produtoId) { this.produtoId = produtoId; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(BigDecimal precoUnitario) { this.precoUnitario = precoUnitario; }

    public Integer getFornecedorId() { return fornecedorId; }
    public void setFornecedorId(Integer fornecedorId) { this.fornecedorId = fornecedorId; }

    // toString (útil para debug)
    @Override
    public String toString() {
        return "CompraProduto{" +
                "compraId=" + compraId +
                ", produtoId=" + produtoId +
                ", quantidade=" + quantidade +
                ", precoUnitario=" + precoUnitario +
                ", fornecedorId=" + fornecedorId +
                '}';
    }
}
