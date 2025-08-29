package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Venda {
    private int id;
    private Atendimento atendimento; // pode ser null
    private LocalDateTime dataHora;
    private BigDecimal valorTotal;
    private String usuario;

    // -------------------------
    // Construtores
    // -------------------------

    // Construtor vazio
    public Venda() {}

    // Construtor completo
    public Venda(int id, Atendimento atendimento, LocalDateTime dataHora, BigDecimal valorTotal, String usuario) {
        this.id = id;
        this.atendimento = atendimento;
        this.dataHora = dataHora;
        this.valorTotal = valorTotal;
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

    public Atendimento getAtendimento() {
        return atendimento;
    }

    public void setAtendimento(Atendimento atendimento) {
        this.atendimento = atendimento;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
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
        return "Venda{" +
                "id=" + id +
                ", atendimento=" + (atendimento != null ? atendimento.getId() : "null") +
                ", dataHora=" + dataHora +
                ", valorTotal=" + valorTotal +
                ", usuario='" + usuario + '\'' +
                '}';
    }
}
