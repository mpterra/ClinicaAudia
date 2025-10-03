package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Orcamento {

	private int id;
	private Integer pacienteId; // opcional
	private Integer profissionalId; // opcional
	private Integer atendimentoId; // opcional
	private Timestamp dataHora;
	private BigDecimal valorTotal;
	private String observacoes;
	private String usuario;

	public Orcamento() {
		this.valorTotal = BigDecimal.ZERO;
	}

	// Getters e Setters
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Integer getPacienteId() {
		return pacienteId;
	}

	public void setPacienteId(Integer pacienteId) {
		this.pacienteId = pacienteId;
	}

	public Integer getProfissionalId() {
		return profissionalId;
	}

	public void setProfissionalId(Integer profissionalId) {
		this.profissionalId = profissionalId;
	}

	public Integer getAtendimentoId() {
		return atendimentoId;
	}

	public void setAtendimentoId(Integer atendimentoId) {
		this.atendimentoId = atendimentoId;
	}

	public Timestamp getDataHora() {
		return dataHora;
	}

	public void setDataHora(Timestamp dataHora) {
		this.dataHora = dataHora;
	}

	public BigDecimal getValorTotal() {
		return valorTotal;
	}

	public void setValorTotal(BigDecimal valorTotal) {
		this.valorTotal = valorTotal;
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
