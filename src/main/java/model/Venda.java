package model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

public class Venda {

    private int id;
    private Integer atendimentoId;  // pode ser null
    private Integer pacienteId;     // pode ser null
    private Integer orcamentoId;    // pode ser null
    private Timestamp dataHora;
    private BigDecimal valorTotal;
    private String usuario;

    // Construtor vazio
    public Venda() {
        this.valorTotal = BigDecimal.ZERO;
        this.dataHora = new Timestamp(System.currentTimeMillis());
    }

    // Construtor completo
    public Venda(int id, Integer atendimentoId, Integer pacienteId, Integer orcamentoId,
                 Timestamp dataHora, BigDecimal valorTotal, String usuario) {
        this.id = id;
        this.atendimentoId = atendimentoId;
        this.pacienteId = pacienteId;
        this.orcamentoId = orcamentoId;
        this.dataHora = dataHora != null ? dataHora : new Timestamp(System.currentTimeMillis());
        this.valorTotal = valorTotal != null ? valorTotal : BigDecimal.ZERO;
        this.usuario = usuario;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getAtendimentoId() { return atendimentoId; }
    public void setAtendimentoId(Integer atendimentoId) { this.atendimentoId = atendimentoId; }

    public Integer getPacienteId() { return pacienteId; }
    public void setPacienteId(Integer pacienteId) { this.pacienteId = pacienteId; }

    public Integer getOrcamentoId() { return orcamentoId; }
    public void setOrcamentoId(Integer orcamentoId) { this.orcamentoId = orcamentoId; }

    public Timestamp getDataHora() { return dataHora; }
    public void setDataHora(Timestamp dataHora) { this.dataHora = dataHora; }

    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { 
        this.valorTotal = valorTotal != null ? valorTotal : BigDecimal.ZERO;
    }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    // Métodos utilitários
    @Override
    public String toString() {
        return "Venda{" +
                "id=" + id +
                ", atendimentoId=" + atendimentoId +
                ", pacienteId=" + pacienteId +
                ", orcamentoId=" + orcamentoId +
                ", dataHora=" + dataHora +
                ", valorTotal=" + valorTotal +
                ", usuario='" + usuario + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Venda)) return false;
        Venda venda = (Venda) o;
        return id == venda.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
