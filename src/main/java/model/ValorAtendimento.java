package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ValorAtendimento {

    private int id;
    private int profissionalId;
    private Tipo tipo;
    private BigDecimal valor;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    private String usuario;

    // Enum para o campo 'tipo' atualizado com novos valores
    public enum Tipo {
        AVALIACAO,
        RETORNO,
        REGULAGEM,
        EXAME,
        REUNIAO,
        PESSOAL
    }

    // Construtores
    public ValorAtendimento() {
    }

    public ValorAtendimento(int id, int profissionalId, Tipo tipo, BigDecimal valor,
                            LocalDateTime criadoEm, LocalDateTime atualizadoEm, String usuario) {
        this.id = id;
        this.profissionalId = profissionalId;
        this.tipo = tipo;
        this.valor = valor;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
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

    @Override
    public String toString() {
        return "ValorAtendimento{" +
                "id=" + id +
                ", profissionalId=" + profissionalId +
                ", tipo=" + tipo +
                ", valor=" + valor +
                ", criadoEm=" + criadoEm +
                ", atualizadoEm=" + atualizadoEm +
                ", usuario='" + usuario + '\'' +
                '}';
    }
}