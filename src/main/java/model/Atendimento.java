package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Atendimento {

	public enum Tipo {
		AVALIACAO, RETORNO, REGULAGEM, EXAME, REUNIAO, PESSOAL
	}

	public enum Situacao {
		AGENDADO, REALIZADO, FALTOU, CANCELADO
	}

	private int id;
	private int pacienteId;
	private String pacienteNome; // Adicionado para facilitar exibição
	private int profissionalId;
	private String profissionalNome; // Adicionado para facilitar exibição
	private Timestamp dataHora;
	private int duracaoMin;
	private Tipo tipo;
	private Situacao situacao;
	private String notas;
	private BigDecimal valor;
	private Timestamp criadoEm;
	private Timestamp atualizadoEm;
	private String usuario;

	// Construtor vazio
	public Atendimento() {
		this.duracaoMin = 30;
		this.situacao = Situacao.AGENDADO;
		this.valor = BigDecimal.ZERO;
	}

	// Getters e Setters
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPacienteId() {
		return pacienteId;
	}

	public void setPacienteId(int pacienteId) {
		this.pacienteId = pacienteId;
	}

	public String getPacienteNome() {
		return pacienteNome;
	}

	public void setPacienteNome(String pacienteNome) {
		this.pacienteNome = pacienteNome;
	}

	public int getProfissionalId() {
		return profissionalId;
	}

	public void setProfissionalId(int profissionalId) {
		this.profissionalId = profissionalId;
	}
	
	public String getProfissionalNome() {
		return profissionalNome;
	}
	
	public void setProfissionalNome(String profissionalNome) {
		this.profissionalNome = profissionalNome;
	}

	public Timestamp getDataHora() {
		return dataHora;
	}

	public void setDataHora(Timestamp dataHora) {
		this.dataHora = dataHora;
	}

	public int getDuracaoMin() {
		return duracaoMin;
	}

	public void setDuracaoMin(int duracaoMin) {
		this.duracaoMin = duracaoMin;
	}

	public Tipo getTipo() {
		return tipo;
	}

	public void setTipo(Tipo tipo) {
		this.tipo = tipo;
	}

	public Situacao getSituacao() {
		return situacao;
	}

	public void setSituacao(Situacao situacao) {
		this.situacao = situacao;
	}

	public String getNotas() {
		return notas;
	}

	public void setNotas(String notas) {
		this.notas = notas;
	}

	public BigDecimal getValor() {
		return valor;
	}

	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}

	public Timestamp getCriadoEm() {
		return criadoEm;
	}

	public void setCriadoEm(Timestamp criadoEm) {
		this.criadoEm = criadoEm;
	}

	public Timestamp getAtualizadoEm() {
		return atualizadoEm;
	}

	public void setAtualizadoEm(Timestamp atualizadoEm) {
		this.atualizadoEm = atualizadoEm;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}
}
