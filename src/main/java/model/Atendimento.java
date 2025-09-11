package model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;

public class Atendimento {

    public enum Tipo {
        AVALIACAO, RETORNO, REGULAGEM, EXAME, REUNIAO, PESSOAL
    }

    public enum Situacao {
        AGENDADO, REALIZADO, FALTOU, CANCELADO
    }

    private int id;
    private int pacienteId;
    private int profissionalId;
    private Timestamp dataHora;
    private int duracaoMin;
    private Tipo tipo;
    private Situacao situacao;
    private String notas;
    private BigDecimal valor;
    private Timestamp criadoEm;
    private Timestamp atualizadoEm;
    private String usuario;

    // Objetos relacionados
    private Paciente paciente;
    private Profissional profissional;

    // Construtor vazio
    public Atendimento() {
        this.duracaoMin = 30;
        this.situacao = Situacao.AGENDADO;
        this.valor = BigDecimal.ZERO;
    }

    // =============================
    // Getters e Setters
    // =============================
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPacienteId() { return pacienteId; }
    public void setPacienteId(int pacienteId) { this.pacienteId = pacienteId; }

    public int getProfissionalId() { return profissionalId; }
    public void setProfissionalId(int profissionalId) { this.profissionalId = profissionalId; }

    public Timestamp getDataHora() { return dataHora; }
    public void setDataHora(Timestamp dataHora) { this.dataHora = dataHora; }

    public int getDuracaoMin() { return duracaoMin; }
    public void setDuracaoMin(int duracaoMin) { this.duracaoMin = duracaoMin; }

    public Tipo getTipo() { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }

    public Situacao getSituacao() { return situacao; }
    public void setSituacao(Situacao situacao) { this.situacao = situacao; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public Timestamp getCriadoEm() { return criadoEm; }
    public void setCriadoEm(Timestamp criadoEm) { this.criadoEm = criadoEm; }

    public Timestamp getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(Timestamp atualizadoEm) { this.atualizadoEm = atualizadoEm; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    // =============================
    // Getters e Setters dos objetos
    // =============================
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { 
        this.paciente = paciente;
        if (paciente != null) {
            this.pacienteId = paciente.getId();
        }
    }

    public Profissional getProfissional() { return profissional; }
    public void setProfissional(Profissional profissional) { 
        this.profissional = profissional;
        if (profissional != null) {
            this.profissionalId = profissional.getId();
        }
    }

    // =============================
    // Métodos utilitários
    // =============================
    /** Retorna a data como LocalDate */
    public LocalDate getData() {
        if (dataHora != null) return dataHora.toLocalDateTime().toLocalDate();
        return null;
    }

    /** Retorna a hora como LocalTime */
    public LocalTime getHora() {
        if (dataHora != null) return dataHora.toLocalDateTime().toLocalTime();
        return null;
    }

    /** Define data e hora separadamente */
    public void setDataHora(LocalDate data, LocalTime hora) {
        if (data != null && hora != null) {
            this.dataHora = Timestamp.valueOf(data.atTime(hora));
        } else {
            this.dataHora = null;
        }
    }

    /** Retorna o nome do paciente */
    public String getPacienteNome() {
        return (paciente != null) ? paciente.getNome() : null;
    }

    /** Retorna o nome do profissional */
    public String getProfissionalNome() {
        return (profissional != null) ? profissional.getNome() : null;
    }
}
