package model;

import java.sql.Timestamp;

public class EvolucaoAtendimento {

    private int id;
    private int atendimentoId;
    private String notas;
    private String arquivo;
    private Timestamp criadoEm;
    private String usuario;

    // Construtor vazio
    public EvolucaoAtendimento() {}

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAtendimentoId() { return atendimentoId; }
    public void setAtendimentoId(int atendimentoId) { this.atendimentoId = atendimentoId; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public String getArquivo() { return arquivo; }
    public void setArquivo(String arquivo) { this.arquivo = arquivo; }

    public Timestamp getCriadoEm() { return criadoEm; }
    public void setCriadoEm(Timestamp criadoEm) { this.criadoEm = criadoEm; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
}
