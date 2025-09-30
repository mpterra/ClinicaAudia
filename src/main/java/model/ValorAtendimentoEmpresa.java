package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ValorAtendimentoEmpresa {

    private int id;
    private int profissionalId;
    private int empresaParceiraId;
    private Tipo tipo;
    private BigDecimal valor;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    private String usuario;

    // Enum para tipo de atendimento, mapeando os valores da tabela
    public enum Tipo {
        AVALIACAO, RETORNO, REGULAGEM, EXAME
    }

    // Construtor vazio
    public ValorAtendimentoEmpresa() {
    }

    // Construtor principal
    public ValorAtendimentoEmpresa(int id, int profissionalId, int empresaParceiraId, Tipo tipo, BigDecimal valor, String usuario) {
        this.id = id;
        this.profissionalId = profissionalId;
        this.empresaParceiraId = empresaParceiraId;
        this.tipo = tipo;
        this.valor = valor;
        this.usuario = usuario;
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProfissionalId() {
        return profissionalId;
    }

    public void setProfissionalId(int profissionalId) {
        this.profissionalId = profissionalId;
    }

    public int getEmpresaParceiraId() {
        return empresaParceiraId;
    }

    public void setEmpresaParceiraId(int empresaParceiraId) {
        this.empresaParceiraId = empresaParceiraId;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
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

    // Para exibição em JComboBox ou tabela
    @Override
    public String toString() {
        return tipo + " - R$ " + (valor != null ? String.format("%.2f", valor) : "0.00");
    }
}