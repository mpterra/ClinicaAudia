package model;

import java.sql.Time;
import java.sql.Timestamp;

public class EscalaProfissional {

    private int id;
    private int profissionalId;
    private int diaSemana;      // 1 = segunda ... 7 = domingo
    private Time horaInicio;
    private Time horaFim;
    private boolean disponivel; // true = disponível, false = indisponível
    private Timestamp criadoEm;
    private Timestamp atualizadoEm;
    private String usuario;

    // Construtor vazio
    public EscalaProfissional() {
        this.disponivel = true; // padrão da tabela
    }

    // Construtor com todos os campos
    public EscalaProfissional(int id, int profissionalId, int diaSemana, Time horaInicio, Time horaFim,
                              boolean disponivel, Timestamp criadoEm, Timestamp atualizadoEm, String usuario) {
        this.id = id;
        this.profissionalId = profissionalId;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.disponivel = disponivel;
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

    public int getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(int diaSemana) {
        this.diaSemana = diaSemana;
    }

    public Time getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(Time horaInicio) {
        this.horaInicio = horaInicio;
    }

    public Time getHoraFim() {
        return horaFim;
    }

    public void setHoraFim(Time horaFim) {
        this.horaFim = horaFim;
    }

    public boolean isDisponivel() {
        return disponivel;
    }

    public void setDisponivel(boolean disponivel) {
        this.disponivel = disponivel;
    }

    public Timestamp getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Timestamp criadoEm) {
        this.criadoEm = criadoEm;
    }

    public Timestamp getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(Timestamp atualizadoEm) {
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
        return "EscalaProfissional{" +
                "id=" + id +
                ", profissionalId=" + profissionalId +
                ", diaSemana=" + diaSemana +
                ", horaInicio=" + horaInicio +
                ", horaFim=" + horaFim +
                ", disponivel=" + disponivel +
                ", criadoEm=" + criadoEm +
                ", atualizadoEm=" + atualizadoEm +
                ", usuario='" + usuario + '\'' +
                '}';
    }
}
