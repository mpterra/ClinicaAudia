package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class OrcamentoProduto {

    private int orcamentoId;
    private int produtoId;
    private int quantidade = 1;
    private BigDecimal precoUnitario;
    private Timestamp dataRegistro;

    public OrcamentoProduto() {}

    // Getters e Setters
    public int getOrcamentoId() { return orcamentoId; }
    public void setOrcamentoId(int orcamentoId) { this.orcamentoId = orcamentoId; }

    public int getProdutoId() { return produtoId; }
    public void setProdutoId(int produtoId) { this.produtoId = produtoId; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(BigDecimal precoUnitario) { this.precoUnitario = precoUnitario; }

    public Timestamp getDataRegistro() { return dataRegistro; }
    public void setDataRegistro(Timestamp dataRegistro) { this.dataRegistro = dataRegistro; }
}
