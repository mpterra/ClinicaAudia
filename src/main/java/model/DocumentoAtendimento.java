package model;

import java.sql.Timestamp;

public class DocumentoAtendimento {

    private int id;
    private int atendimentoId;
    private String nomeArquivo;
    private String caminhoArquivo;
    private String tipoArquivo;
    private Timestamp criadoEm;
    private String usuario;
    private Atendimento atendimento;

    // Construtor vazio
    public DocumentoAtendimento() {}

    // Construtor completo (sem criadoEm, preenchido pelo banco)
    public DocumentoAtendimento(int id, int atendimentoId, String nomeArquivo, String caminhoArquivo, 
                              String tipoArquivo, String usuario) {
        this.id = id;
        this.atendimentoId = atendimentoId;
        this.nomeArquivo = nomeArquivo;
        this.caminhoArquivo = caminhoArquivo;
        this.tipoArquivo = tipoArquivo;
        this.usuario = usuario;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAtendimentoId() { return atendimentoId; }
    public void setAtendimentoId(int atendimentoId) { this.atendimentoId = atendimentoId; }

    public String getNomeArquivo() { return nomeArquivo; }
    public void setNomeArquivo(String nomeArquivo) { this.nomeArquivo = nomeArquivo; }

    public String getCaminhoArquivo() { return caminhoArquivo; }
    public void setCaminhoArquivo(String caminhoArquivo) { this.caminhoArquivo = caminhoArquivo; }

    public String getTipoArquivo() { return tipoArquivo; }
    public void setTipoArquivo(String tipoArquivo) { this.tipoArquivo = tipoArquivo; }

    public Timestamp getCriadoEm() { return criadoEm; }
    public void setCriadoEm(Timestamp criadoEm) { this.criadoEm = criadoEm; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public Atendimento getAtendimento() { return atendimento; }
    public void setAtendimento(Atendimento atendimento) { 
        this.atendimento = atendimento;
        if (atendimento != null) {
            this.atendimentoId = atendimento.getId();
        }
    }

    @Override
    public String toString() {
        return nomeArquivo;
    }
}