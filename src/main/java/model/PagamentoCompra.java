package model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class PagamentoCompra {

    private int id;
    private int compraId;
    private Timestamp dataHora;
    private Date dataVencimento;
    private BigDecimal valor;
    private MetodoPagamento metodoPagamento;
    private int parcela;
    private int totalParcelas;
    private StatusPagamento status;
    private String observacoes;
    private String usuario;

    // Enums para representar os ENUMs do banco
    public enum MetodoPagamento {
        DINHEIRO, PIX, DEBITO, CREDITO, BOLETO
    }

    public enum StatusPagamento {
        PAGO, PENDENTE
    }

    // Construtor vazio
    public PagamentoCompra() {}

    // Construtor cheio
    public PagamentoCompra(int id, int compraId, Timestamp dataHora, Date dataVencimento, BigDecimal valor,
                           MetodoPagamento metodoPagamento, int parcela, int totalParcelas,
                           StatusPagamento status, String observacoes, String usuario) {
        this.id = id;
        this.compraId = compraId;
        this.dataHora = dataHora;
        this.dataVencimento = dataVencimento;
        this.valor = valor;
        this.metodoPagamento = metodoPagamento;
        this.parcela = parcela;
        this.totalParcelas = totalParcelas;
        this.status = status;
        this.observacoes = observacoes;
        this.usuario = usuario;
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCompraId() {
        return compraId;
    }

    public void setCompraId(int compraId) {
        this.compraId = compraId;
    }

    public Timestamp getDataHora() {
        return dataHora;
    }

    public void setDataHora(Timestamp dataHora) {
        this.dataHora = dataHora;
    }

    public Date getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(Date dataVencimento) {
        this.dataVencimento = dataVencimento;
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

    public StatusPagamento getStatus() {
        return status;
    }

    public void setStatus(StatusPagamento status) {
        this.status = status;
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
}
