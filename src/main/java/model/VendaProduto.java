package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class VendaProduto {

    // -------------------------
    // Atributos
    // -------------------------
    private Venda venda;
    private Produto produto;
    private int quantidade;
    private BigDecimal precoUnitario;
    private LocalDateTime dataVenda;
    private int garantiaMeses;
    private LocalDate fimGarantia;

    // -------------------------
    // Construtores
    // -------------------------

    // Construtor vazio
    public VendaProduto() {}

    // Construtor completo
    public VendaProduto(Venda venda, Produto produto, int quantidade, BigDecimal precoUnitario,
                        LocalDateTime dataVenda, int garantiaMeses, LocalDate fimGarantia) {
        this.venda = venda;
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.dataVenda = dataVenda;
        this.garantiaMeses = garantiaMeses;
        this.fimGarantia = fimGarantia;
    }

    // -------------------------
    // Getters e Setters
    // -------------------------
    public Venda getVenda() {
        return venda;
    }

    public void setVenda(Venda venda) {
        this.venda = venda;
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

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(BigDecimal precoUnitario) {
        this.precoUnitario = precoUnitario;
    }

    public LocalDateTime getDataVenda() {
        return dataVenda;
    }

    public void setDataVenda(LocalDateTime dataVenda) {
        this.dataVenda = dataVenda;
    }

    public int getGarantiaMeses() {
        return garantiaMeses;
    }

    public void setGarantiaMeses(int garantiaMeses) {
        this.garantiaMeses = garantiaMeses;
    }

    public LocalDate getFimGarantia() {
        return fimGarantia;
    }

    public void setFimGarantia(LocalDate fimGarantia) {
        this.fimGarantia = fimGarantia;
    }

    // -------------------------
    // toString
    // -------------------------
    @Override
    public String toString() {
        return "VendaProduto{" +
                "vendaId=" + (venda != null ? venda.getId() : "null") +
                ", produtoId=" + (produto != null ? produto.getId() : "null") +
                ", quantidade=" + quantidade +
                ", precoUnitario=" + precoUnitario +
                ", dataVenda=" + dataVenda +
                ", garantiaMeses=" + garantiaMeses +
                ", fimGarantia=" + fimGarantia +
                '}';
    }
}
