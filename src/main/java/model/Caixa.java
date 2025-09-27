package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Caixa {

    // -------------------------
    // Atributos
    // -------------------------
    private int id;
    private LocalDateTime dataAbertura;
    private LocalDateTime dataFechamento;
    private BigDecimal saldoInicialDinheiro;
    private BigDecimal saldoInicialDebito;
    private BigDecimal saldoInicialCredito;
    private BigDecimal saldoInicialPix;
    private BigDecimal saldoFinalDinheiro;
    private BigDecimal saldoFinalDebito;
    private BigDecimal saldoFinalCredito;
    private BigDecimal saldoFinalPix;
    private boolean fechado;
    private String observacoes;
    private String usuario;

    // -------------------------
    // Construtores
    // -------------------------

    public Caixa() {
        // valores padrão coerentes com o banco
        this.saldoInicialDinheiro = BigDecimal.ZERO;
        this.saldoInicialDebito = BigDecimal.ZERO;
        this.saldoInicialCredito = BigDecimal.ZERO;
        this.saldoInicialPix = BigDecimal.ZERO;
        this.saldoFinalDinheiro = BigDecimal.ZERO;
        this.saldoFinalDebito = BigDecimal.ZERO;
        this.saldoFinalCredito = BigDecimal.ZERO;
        this.saldoFinalPix = BigDecimal.ZERO;
        this.fechado = false;
    }

    public Caixa(int id,
                 LocalDateTime dataAbertura,
                 LocalDateTime dataFechamento,
                 BigDecimal saldoInicialDinheiro,
                 BigDecimal saldoInicialDebito,
                 BigDecimal saldoInicialCredito,
                 BigDecimal saldoInicialPix,
                 BigDecimal saldoFinalDinheiro,
                 BigDecimal saldoFinalDebito,
                 BigDecimal saldoFinalCredito,
                 BigDecimal saldoFinalPix,
                 boolean fechado,
                 String observacoes,
                 String usuario) {
        this.id = id;
        this.dataAbertura = dataAbertura;
        this.dataFechamento = dataFechamento;
        this.saldoInicialDinheiro = saldoInicialDinheiro != null ? saldoInicialDinheiro : BigDecimal.ZERO;
        this.saldoInicialDebito = saldoInicialDebito != null ? saldoInicialDebito : BigDecimal.ZERO;
        this.saldoInicialCredito = saldoInicialCredito != null ? saldoInicialCredito : BigDecimal.ZERO;
        this.saldoInicialPix = saldoInicialPix != null ? saldoInicialPix : BigDecimal.ZERO;
        this.saldoFinalDinheiro = saldoFinalDinheiro != null ? saldoFinalDinheiro : BigDecimal.ZERO;
        this.saldoFinalDebito = saldoFinalDebito != null ? saldoFinalDebito : BigDecimal.ZERO;
        this.saldoFinalCredito = saldoFinalCredito != null ? saldoFinalCredito : BigDecimal.ZERO;
        this.saldoFinalPix = saldoFinalPix != null ? saldoFinalPix : BigDecimal.ZERO;
        this.fechado = fechado;
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

    public LocalDateTime getDataAbertura() {
        return dataAbertura;
    }

    public void setDataAbertura(LocalDateTime dataAbertura) {
        this.dataAbertura = dataAbertura;
    }

    public LocalDateTime getDataFechamento() {
        return dataFechamento;
    }

    public void setDataFechamento(LocalDateTime dataFechamento) {
        this.dataFechamento = dataFechamento;
    }

    public BigDecimal getSaldoInicialDinheiro() {
        return saldoInicialDinheiro;
    }

    public void setSaldoInicialDinheiro(BigDecimal saldoInicialDinheiro) {
        this.saldoInicialDinheiro = saldoInicialDinheiro;
    }

    public BigDecimal getSaldoInicialDebito() {
        return saldoInicialDebito;
    }

    public void setSaldoInicialDebito(BigDecimal saldoInicialDebito) {
        this.saldoInicialDebito = saldoInicialDebito;
    }

    public BigDecimal getSaldoInicialCredito() {
        return saldoInicialCredito;
    }

    public void setSaldoInicialCredito(BigDecimal saldoInicialCredito) {
        this.saldoInicialCredito = saldoInicialCredito;
    }

    public BigDecimal getSaldoInicialPix() {
        return saldoInicialPix;
    }

    public void setSaldoInicialPix(BigDecimal saldoInicialPix) {
        this.saldoInicialPix = saldoInicialPix;
    }

    public BigDecimal getSaldoFinalDinheiro() {
        return saldoFinalDinheiro;
    }

    public void setSaldoFinalDinheiro(BigDecimal saldoFinalDinheiro) {
        this.saldoFinalDinheiro = saldoFinalDinheiro;
    }

    public BigDecimal getSaldoFinalDebito() {
        return saldoFinalDebito;
    }

    public void setSaldoFinalDebito(BigDecimal saldoFinalDebito) {
        this.saldoFinalDebito = saldoFinalDebito;
    }

    public BigDecimal getSaldoFinalCredito() {
        return saldoFinalCredito;
    }

    public void setSaldoFinalCredito(BigDecimal saldoFinalCredito) {
        this.saldoFinalCredito = saldoFinalCredito;
    }

    public BigDecimal getSaldoFinalPix() {
        return saldoFinalPix;
    }

    public void setSaldoFinalPix(BigDecimal saldoFinalPix) {
        this.saldoFinalPix = saldoFinalPix;
    }

    public boolean isFechado() {
        return fechado;
    }

    public void setFechado(boolean fechado) {
        this.fechado = fechado;
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
        return "Caixa{" +
                "id=" + id +
                ", dataAbertura=" + dataAbertura +
                ", dataFechamento=" + dataFechamento +
                ", saldoInicialDinheiro=" + saldoInicialDinheiro +
                ", saldoInicialDebito=" + saldoInicialDebito +
                ", saldoInicialCredito=" + saldoInicialCredito +
                ", saldoInicialPix=" + saldoInicialPix +
                ", saldoFinalDinheiro=" + saldoFinalDinheiro +
                ", saldoFinalDebito=" + saldoFinalDebito +
                ", saldoFinalCredito=" + saldoFinalCredito +
                ", saldoFinalPix=" + saldoFinalPix +
                ", fechado=" + fechado +
                ", observacoes='" + observacoes + '\'' +
                ", usuario='" + usuario + '\'' +
                '}';
    }
}
