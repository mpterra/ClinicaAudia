package model;

import java.time.LocalDateTime;

public class AgendaProfissional {

    // -------------------------
    // Atributos
    // -------------------------
    private int id;
    private Profissional profissional;
    private LocalDateTime dataHoraInicio;
    private LocalDateTime dataHoraFim;
    private boolean disponivel;
    private String usuario;

    // -------------------------
    // Construtores
    // -------------------------

    // Construtor vazio
    public AgendaProfissional() {}

    // Construtor completo
    public AgendaProfissional(int id, Profissional profissional, LocalDateTime dataHoraInicio,
                              LocalDateTime dataHoraFim, boolean disponivel, String usuario) {
        this.id = id;
        this.profissional = profissional;
        this.dataHoraInicio = dataHoraInicio;
        this.dataHoraFim = dataHoraFim;
        this.disponivel = disponivel;
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

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public LocalDateTime getDataHoraInicio() {
        return dataHoraInicio;
    }

    public void setDataHoraInicio(LocalDateTime dataHoraInicio) {
        this.dataHoraInicio = dataHoraInicio;
    }

    public LocalDateTime getDataHoraFim() {
        return dataHoraFim;
    }

    public void setDataHoraFim(LocalDateTime dataHoraFim) {
        this.dataHoraFim = dataHoraFim;
    }

    public boolean isDisponivel() {
        return disponivel;
    }

    public void setDisponivel(boolean disponivel) {
        this.disponivel = disponivel;
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
        return "AgendaProfissional{" +
                "id=" + id +
                ", profissionalId=" + (profissional != null ? profissional.getId() : "null") +
                ", dataHoraInicio=" + dataHoraInicio +
                ", dataHoraFim=" + dataHoraFim +
                ", disponivel=" + disponivel +
                ", usuario='" + usuario + '\'' +
                '}';
    }
}
