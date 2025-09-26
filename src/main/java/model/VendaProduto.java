package model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class VendaProduto {

    private int vendaId;
    private int produtoId;
    private int quantidade = 1;
    private BigDecimal precoUnitario;
    private Timestamp dataVenda;
    private int garantiaMeses = 0;
    private Date fimGarantia;
    private String codigoSerial;

    public VendaProduto() {}

    // Getters e Setters
    public int getVendaId() { return vendaId; }
    public void setVendaId(int vendaId) { this.vendaId = vendaId; }

    public int getProdutoId() { return produtoId; }
    public void setProdutoId(int produtoId) { this.produtoId = produtoId; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(BigDecimal precoUnitario) { this.precoUnitario = precoUnitario; }

    public Timestamp getDataVenda() { return dataVenda; }
    public void setDataVenda(Timestamp dataVenda) { this.dataVenda = dataVenda; }

    public int getGarantiaMeses() { return garantiaMeses; }
    public void setGarantiaMeses(int garantiaMeses) { this.garantiaMeses = garantiaMeses; }

    public Date getFimGarantia() { return fimGarantia; }
    public void setFimGarantia(Date fimGarantia) { this.fimGarantia = fimGarantia; }
    
    public String getCogidoSerial() { return codigoSerial; }
    public void setCogidoSerial(String cogidoSerial) { this.codigoSerial = cogidoSerial; }
}
