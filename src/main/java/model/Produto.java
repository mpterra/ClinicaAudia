package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Produto {
	private int id;
	private TipoProduto tipoProduto;
	private String nome;
	private String codigoSerial;
	private String descricao;
	private BigDecimal preco;
	private int estoque;
	private LocalDateTime criadoEm;
	private LocalDateTime atualizadoEm;
	private String usuario;

	// Construtores
	public Produto() {
	}

	public Produto(int id, TipoProduto tipoProduto, String nome, String codigoSerial, String descricao,
			BigDecimal preco, int estoque, LocalDateTime criadoEm, LocalDateTime atualizadoEm, String usuario) {
		this.id = id;
		this.tipoProduto = tipoProduto;
		this.nome = nome;
		this.codigoSerial = codigoSerial;
		this.descricao = descricao;
		this.preco = preco;
		this.estoque = estoque;
		this.criadoEm = criadoEm;
		this.atualizadoEm = atualizadoEm;
		this.usuario = usuario;
	}

	public Produto(int id, TipoProduto tipoProduto, String nome, String codigoSerial, String descricao,
			BigDecimal preco, int estoque, String usuario) {
		this.id = id;
		this.tipoProduto = tipoProduto;
		this.nome = nome;
		this.codigoSerial = codigoSerial;
		this.descricao = descricao;
		this.preco = preco;
		this.estoque = estoque;
		this.usuario = usuario;
	}

	// Getters e Setters
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public TipoProduto getTipoProduto() {
		return tipoProduto;
	}

	public void setTipoProduto(TipoProduto tipoProduto) {
		this.tipoProduto = tipoProduto;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getCodigoSerial() {
		return codigoSerial;
	}

	public void setCodigoSerial(String codigoSerial) {
		this.codigoSerial = codigoSerial;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public BigDecimal getPreco() {
		return preco;
	}

	public void setPreco(BigDecimal preco) {
		this.preco = preco;
	}

	public int getEstoque() {
		return estoque;
	}

	public void setEstoque(int estoque) {
		this.estoque = estoque;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

	public void setCriadoEm(LocalDateTime criadoEm) {
		this.criadoEm = criadoEm;
	}

	public LocalDateTime getAtualizadoEm() {
		return atualizadoEm;
	}

	public void setAtualizadoEm(LocalDateTime atualizadoEm) {
		this.atualizadoEm = atualizadoEm;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	// toString
	@Override
	public String toString() {
		return "Produto{" + "id=" + id + ", tipoProduto=" + (tipoProduto != null ? tipoProduto.getNome() : "null")
				+ ", nome='" + nome + '\'' + ", codigoSerial='" + codigoSerial + '\'' + ", descricao='" + descricao
				+ '\'' + ", preco=" + preco + ", estoque=" + estoque + ", criadoEm=" + criadoEm + ", atualizadoEm="
				+ atualizadoEm + ", usuario='" + usuario + '\'' + '}';
	}
}
