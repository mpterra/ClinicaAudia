package model;

import java.sql.Timestamp;

public class Produto {

    private int id;
    private int tipoProdutoId;
    private String nome;
    private String codigoSerial;
    private String descricao;
    private Timestamp criadoEm;
    private Timestamp atualizadoEm;
    private String usuario;

    // Construtor vazio
    public Produto() {}

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTipoProdutoId() { return tipoProdutoId; }
    public void setTipoProdutoId(int tipoProdutoId) { this.tipoProdutoId = tipoProdutoId; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCodigoSerial() { return codigoSerial; }
    public void setCodigoSerial(String codigoSerial) { this.codigoSerial = codigoSerial; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Timestamp getCriadoEm() { return criadoEm; }
    public void setCriadoEm(Timestamp criadoEm) { this.criadoEm = criadoEm; }

    public Timestamp getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(Timestamp atualizadoEm) { this.atualizadoEm = atualizadoEm; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
}
