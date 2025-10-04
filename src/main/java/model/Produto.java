package model;

import java.sql.Timestamp;
import java.math.BigDecimal;

public class Produto {

    private int id;
    private int tipoProdutoId;
    private String nome;
    private String codigoSerial;
    private String descricao;
    private int garantiaMeses;
    private BigDecimal precoVenda;
    private BigDecimal precoCusto;
    private Timestamp criadoEm;
    private Timestamp atualizadoEm;
    private String usuario;

    public Produto() {}

    public Produto(int id, int tipoProdutoId, String nome, String codigoSerial, String descricao,
                   int garantiaMeses, BigDecimal precoVenda, BigDecimal precoCusto,
                   Timestamp criadoEm, Timestamp atualizadoEm, String usuario) {
        this.id = id;
        this.tipoProdutoId = tipoProdutoId;
        this.nome = nome;
        this.codigoSerial = codigoSerial;
        this.descricao = descricao;
        this.garantiaMeses = garantiaMeses;
        this.precoVenda = precoVenda;
        this.precoCusto = precoCusto;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
        this.usuario = usuario;
    }

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

    public int getGarantiaMeses() { return garantiaMeses; }
    public void setGarantiaMeses(int garantiaMeses) { this.garantiaMeses = garantiaMeses; }

    public BigDecimal getPrecoVenda() { return precoVenda; }
    public void setPrecoVenda(BigDecimal precoVenda) { this.precoVenda = precoVenda; }

    public BigDecimal getPrecoCusto() { return precoCusto; }
    public void setPrecoCusto(BigDecimal precoCusto) { this.precoCusto = precoCusto; }

    public Timestamp getCriadoEm() { return criadoEm; }
    public void setCriadoEm(Timestamp criadoEm) { this.criadoEm = criadoEm; }

    public Timestamp getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(Timestamp atualizadoEm) { this.atualizadoEm = atualizadoEm; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    // toString para exibir nome do produto no JComboBox
    @Override
    public String toString() {
        return nome;
    }
}