package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Atendimento {

    private int id;
    private Paciente paciente;
    private Profissional profissional;
    private LocalDateTime dataHora;
    private int duracaoMin;
    private TipoAtendimento tipo;
    private Situacao situacao;
    private String notas;
    private BigDecimal valor;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    private String usuario;

    // Enums internos
    public enum TipoAtendimento {
        AVALIACAO, RETORNO, REGULAGEM, EXAME, REUNIAO, PESSOAL
    }

    public enum Situacao {
        AGENDADO, REALIZADO, FALTOU, CANCELADO
    }

    // Construtor vazio
    public Atendimento() {}

    // Construtor completo (sem datas)
    public Atendimento(int id, Paciente paciente, Profissional profissional, LocalDateTime dataHora,
                       int duracaoMin, TipoAtendimento tipo, Situacao situacao, String notas,
                       BigDecimal valor, String usuario) {
        this.id = id;
        this.paciente = paciente;
        this.profissional = profissional;
        this.dataHora = dataHora;
        this.duracaoMin = duracaoMin;
        this.tipo = tipo;
        this.situacao = situacao;
        this.notas = notas;
        this.valor = valor;
        this.usuario = usuario;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    public Profissional getProfissional() { return profissional; }
    public void setProfissional(Profissional profissional) { this.profissional = profissional; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public int getDuracaoMin() { return duracaoMin; }
    public void setDuracaoMin(int duracaoMin) { this.duracaoMin = duracaoMin; }

    public TipoAtendimento getTipo() { return tipo; }
    public void setTipo(TipoAtendimento tipo) { this.tipo = tipo; }

    public Situacao getSituacao() { return situacao; }
    public void setSituacao(Situacao situacao) { this.situacao = situacao; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    @Override
    public String toString() {
        return "Atendimento{" +
                "id=" + id +
                ", paciente=" + (paciente != null ? paciente.getId() : null) +
                ", profissional=" + (profissional != null ? profissional.getId() : null) +
                ", dataHora=" + dataHora +
                ", duracaoMin=" + duracaoMin +
                ", tipo=" + tipo +
                ", situacao=" + situacao +
                ", notas='" + notas + '\'' +
                ", valor=" + valor +
                ", criadoEm=" + criadoEm +
                ", atualizadoEm=" + atualizadoEm +
                ", usuario='" + usuario + '\'' +
                '}';
    }
}
