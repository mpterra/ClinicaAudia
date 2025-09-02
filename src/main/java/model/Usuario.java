package model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Usuario {

	private int id;
	private String login;
	private String senha;
	private String tipo;
	private boolean status;
	private LocalDateTime criadoEm;
	private LocalDateTime atualizadoEm;
	private Integer profissionalId;
	private String usuario;

	public enum TipoUsuario {
		ADMIN, FONOAUDIOLOGO, SECRETARIA, FINANCEIRO
	}

	// Construtor vazio
	public Usuario() {
	}

	// Construtor completo
	public Usuario(int id, String login, String senha, String tipo, LocalDateTime criadoEm,
			LocalDateTime atualizadoEm) {
		this.id = id;
		this.login = login;
		this.senha = senha;
		this.tipo = tipo;
	}

	public Usuario(String login, String senha) {
		this.login = login;
		this.senha = senha;
	}

	// Getters e Setters
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipoStr) {
		try {
			this.tipo = TipoUsuario.valueOf(tipoStr.toUpperCase()).name(); // .name() retorna String
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Tipo inválido: " + tipoStr);
		}
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public boolean isAtivo() {
		return status;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}
	
	public void setCriadoEm(Timestamp timestamp) {
		if (timestamp != null) {
			this.criadoEm = timestamp.toLocalDateTime();
		}
	}

	public LocalDateTime getAtualizadoEm() {
		return atualizadoEm;
	}

	public Integer getProfissionalId() {
		return profissionalId;
	}

	public void setProfissionalId(Integer profissionalId) {
		this.profissionalId = profissionalId;
	}
	
	public String getUsuario() {
	    return usuario;
	}

	public void setUsuario(String usuario) {
	    this.usuario = usuario;
	}

	// Para debug/log
	@Override
	public String toString() {
		return "Usuario{" + "id=" + id + ", login='" + login + '\'' + ", tipo='" + tipo + '\'' + ", criadoEm="
				+ criadoEm + ", atualizadoEm=" + atualizadoEm + '}';
	}

}
