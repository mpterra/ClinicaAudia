package util;

import java.math.BigDecimal;

public class FiltroProduto {
    private String nome;
    private Integer tipoProdutoId;
    private BigDecimal precoMin;
    private BigDecimal precoMax;
    private Integer estoqueMin;
    private Integer estoqueMax;

    public FiltroProduto() {}

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Integer getTipoProdutoId() { return tipoProdutoId; }
    public void setTipoProdutoId(Integer tipoProdutoId) { this.tipoProdutoId = tipoProdutoId; }

    public BigDecimal getPrecoMin() { return precoMin; }
    public void setPrecoMin(BigDecimal precoMin) { this.precoMin = precoMin; }

    public BigDecimal getPrecoMax() { return precoMax; }
    public void setPrecoMax(BigDecimal precoMax) { this.precoMax = precoMax; }

    public Integer getEstoqueMin() { return estoqueMin; }
    public void setEstoqueMin(Integer estoqueMin) { this.estoqueMin = estoqueMin; }

    public Integer getEstoqueMax() { return estoqueMax; }
    public void setEstoqueMax(Integer estoqueMax) { this.estoqueMax = estoqueMax; }
}
