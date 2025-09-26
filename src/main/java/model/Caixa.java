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
	private String observacoes;
	private String usuario;

	// -------------------------
	// Construtores
	// -------------------------

	// Construtor vazio
	public Caixa() {
	}

	// Construtor completo
	public Caixa(int id, LocalDateTime dataAbertura, LocalDateTime dataFechamento,
			BigDecimal saldoInicialDinheiro, BigDecimal saldoInicialDebito, BigDecimal saldoInicialCredito,
			BigDecimal saldoInicialPix, String observacoes, String usuario) {
		this.id = id;
		this.dataAbertura = dataAbertura;
		this.dataFechamento = dataFechamento;
		this.saldoInicialDinheiro = saldoInicialDinheiro;
		this.saldoInicialDebito = saldoInicialDebito;
		this.saldoInicialCredito = saldoInicialCredito;
		this.saldoInicialPix = saldoInicialPix;
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
		return "Caixa{" + "id=" + id + ", dataAbertura=" + dataAbertura + ", dataFechamento=" + dataFechamento
				+ ", saldoInicialDinheiro=" + saldoInicialDinheiro + ", saldoInicialDebito=" + saldoInicialDebito
				+ ", saldoInicialCredito=" + saldoInicialCredito + ", saldoInicialPix=" + saldoInicialPix
				+ ", observacoes='" + observacoes + '\'' + ", usuario='" + usuario + '\'' + '}';
	}
}
