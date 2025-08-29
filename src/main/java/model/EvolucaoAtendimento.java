package model;

import java.time.LocalDateTime;

public class EvolucaoAtendimento {

    // -------------------------
    // Atributos
    // -------------------------
    private int id;
    private Atendimento atendimento;
    private String notas;
    private String arquivo;
    private LocalDateTime criadoEm;
    private String usuario;

    // -------------------------
    // Construtores
    // -------------------------

    // Construtor vazio
    public EvolucaoAtendimento() {}

    // Construtor completo
    public EvolucaoAtendimento(int id, Atendimento atendimento, String notas, String arquivo,
                               LocalDateTime criadoEm, String usuario) {
        this.id = id;
        this.atendimento = atendimento;
        this.notas = notas;
        this.arquivo = arquivo;
        this.criadoEm = criadoEm;
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

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public String getArquivo() {
        return arquivo;
    }

    public void setArquivo(String arquivo) {
        this.arquivo = arquivo;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
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
        return "EvolucaoAtendimento{" +
                "id=" + id +
                ", atendimentoId=" + (atendimento != null ? atendimento.getId() : "null") +
                ", notas='" + notas + '\'' +
                ", arquivo='" + arquivo + '\'' +
                ", criadoEm=" + criadoEm +
                ", usuario='" + usuario + '\'' +
                '}';
    }
}
