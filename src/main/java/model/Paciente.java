package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Paciente {

	private int id;
	private String nome;
	private String sexo;
	private String cpf;
	private String telefone;
	private String email;
	private LocalDate dataNascimento;
	private Endereco endereco; // referência ao objeto Endereco
	private LocalDateTime criadoEm;
	private LocalDateTime atualizadoEm;
	private String usuario; // usuário que cadastrou ou alterou

	// Construtor vazio
	public Paciente() {
	}

	// Construtor completo (sem datas, pois o banco preenche)
	public Paciente(int id, String nome, String sexo, String cpf, String telefone, String email,
			LocalDate dataNascimento, Endereco endereco, String usuario) {
		this.id = id;
		this.nome = nome;
		this.sexo = sexo;
		this.cpf = cpf;
		this.telefone = telefone;
		this.email = email;
		this.dataNascimento = dataNascimento;
		this.endereco = endereco;
		this.usuario = usuario;
	}

	// Getters e Setters
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getSexo() {
		return sexo;
	}

	public void setSexo(String sexo) {
		this.sexo = sexo;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public LocalDate getDataNascimento() {
		return dataNascimento;
	}

	public void setDataNascimento(LocalDate dataNascimento) {
		this.dataNascimento = dataNascimento;
	}

	public Endereco getEndereco() {
		return endereco;
	}

	public void setEndereco(Endereco endereco) {
		this.endereco = endereco;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

	public LocalDateTime getAtualizadoEm() {
		return atualizadoEm;
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
		return nome;
	}

	public String getEnderecoCompleto() {
		if (endereco != null) {
			return endereco.getRua() + ", " + endereco.getNumero()
					+ (endereco.getComplemento() != null && !endereco.getComplemento().isEmpty()
							? " - " + endereco.getComplemento()
							: "")
					+ ", " + endereco.getBairro() + ", " + endereco.getCidade() + " - " + endereco.getEstado()
					+ ", CEP: " + endereco.getCep();
		}
		return "";
	}
}
