package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PagamentoAtendimento {

    private int id;
    private Atendimento atendimento;            // FK -> atendimento_id
    private LocalDateTime dataHora;             // preenchido pelo banco (DEFAULT CURRENT_TIMESTAMP)
    private BigDecimal valor;
    private MetodoPagamento metodoPagamento;    // ENUM do banco: DINHEIRO, PIX, CARTAO
    private String observacoes;
    private String usuario;

    public enum MetodoPagamento {
        DINHEIRO, PIX, CARTAO
    }

    // Construtor vazio
    public PagamentoAtendimento() {}

    // Construtor completo (sem dataHora, pois o banco preenche)
    public PagamentoAtendimento(int id,
                                Atendimento atendimento,
                                BigDecimal valor,
                                MetodoPagamento metodoPagamento,
                                String observacoes,
                                String usuario) {
        this.id = id;
        this.atendimento = atendimento;
        this.valor = valor;
        this.metodoPagamento = metodoPagamento;
        this.observacoes = observacoes;
        this.usuario = usuario;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Atendimento getAtendimento() { return atendimento; }
    public void setAtendimento(Atendimento atendimento) { this.atendimento = atendimento; }

    public LocalDateTime getDataHora() { return dataHora; } // só leitura

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public MetodoPagamento getMetodoPagamento() { return metodoPagamento; }
    public void setMetodoPagamento(MetodoPagamento metodoPagamento) { this.metodoPagamento = metodoPagamento; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    @Override
    public String toString() {
        return "PagamentoAtendimento{" +
                "id=" + id +
                ", atendimento=" + (atendimento != null ? atendimento.getId() : null) +
                ", dataHora=" + dataHora +
                ", valor=" + valor +
                ", metodoPagamento=" + metodoPagamento +
                ", observacoes='" + observacoes + '\'' +
                ", usuario='" + usuario + '\'' +
                '}';
    }
}
