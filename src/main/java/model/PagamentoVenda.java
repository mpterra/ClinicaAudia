package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PagamentoVenda {

    // -------------------------
    // Atributos
    // -------------------------
    private int id;
    private Venda venda;
    private LocalDateTime dataHora;
    private BigDecimal valor;
    private MetodoPagamento metodoPagamento;
    private int parcela;
    private int totalParcelas;
    private String observacoes;
    private String usuario;

    // -------------------------
    // Enum para método de pagamento
    // -------------------------
    public enum MetodoPagamento {DINHEIRO, PIX, CARTAO, BOLETO}

    // -------------------------
    // Construtores
    // -------------------------
    
    // Construtor vazio
    public PagamentoVenda() {}

    // Construtor completo
    public PagamentoVenda(int id, Venda venda, LocalDateTime dataHora, BigDecimal valor,
                          MetodoPagamento metodoPagamento, int parcela, int totalParcelas,
                          String observacoes, String usuario) {
        this.id = id;
        this.venda = venda;
        this.dataHora = dataHora;
        this.valor = valor;
        this.metodoPagamento = metodoPagamento;
        this.parcela = parcela;
        this.totalParcelas = totalParcelas;
        this.observacoes = observacoes;
        this.usuario = usuario;
    }

    // -------------------------
    // Getters e Setters
    // -------------------------
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Venda getVenda() {
        return venda;
    }

    public void setVenda(Venda venda) {
        this.venda = venda;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public MetodoPagamento getMetodoPagamento() {
        return metodoPagamento;
    }

    public void setMetodoPagamento(MetodoPagamento metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
    }

    public int getParcela() {
        return parcela;
    }

    public void setParcela(int parcela) {
        this.parcela = parcela;
    }

    public int getTotalParcelas() {
        return totalParcelas;
    }

    public void setTotalParcelas(int totalParcelas) {
        this.totalParcelas = totalParcelas;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    // -------------------------
    // toString
    // -------------------------
    @Override
    public String toString() {
        return "PagamentoVenda{" +
                "id=" + id +
                ", vendaId=" + (venda != null ? venda.getId() : "null") +
                ", dataHora=" + dataHora +
                ", valor=" + valor +
                ", metodoPagamento=" + metodoPagamento +
                ", parcela=" + parcela +
                ", totalParcelas=" + totalParcelas +
                ", observacoes='" + observacoes + '\'' +
                ", usuario='" + usuario + '\'' +
                '}';
    }
}
