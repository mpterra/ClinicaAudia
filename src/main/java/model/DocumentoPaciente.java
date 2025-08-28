package model;

import java.time.LocalDateTime;

public class DocumentoPaciente {

    private int id;
    private Paciente paciente; // referência ao objeto Paciente
    private String nomeArquivo;
    private String caminhoArquivo;
    private String tipoArquivo;
    private LocalDateTime criadoEm;
    private String usuario;

    // Construtor vazio
    public DocumentoPaciente() {}

    // Construtor completo (sem datas, pois o banco preenche)
    public DocumentoPaciente(int id, Paciente paciente, String nomeArquivo,
                             String caminhoArquivo, String tipoArquivo, String usuario) {
        this.id = id;
        this.paciente = paciente;
        this.nomeArquivo = nomeArquivo;
        this.caminhoArquivo = caminhoArquivo;
        this.tipoArquivo = tipoArquivo;
        this.usuario = usuario;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    public String getNomeArquivo() { return nomeArquivo; }
    public void setNomeArquivo(String nomeArquivo) { this.nomeArquivo = nomeArquivo; }

    public String getCaminhoArquivo() { return caminhoArquivo; }
    public void setCaminhoArquivo(String caminhoArquivo) { this.caminhoArquivo = caminhoArquivo; }

    public String getTipoArquivo() { return tipoArquivo; }
    public void setTipoArquivo(String tipoArquivo) { this.tipoArquivo = tipoArquivo; }

    public LocalDateTime getCriadoEm() { return criadoEm; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    @Override
    public String toString() {
        return "DocumentoPaciente{" +
                "id=" + id +
                ", paciente=" + (paciente != null ? paciente.getId() : null) +
                ", nomeArquivo='" + nomeArquivo + '\'' +
                ", caminhoArquivo='" + caminhoArquivo + '\'' +
                ", tipoArquivo='" + tipoArquivo + '\'' +
                ", criadoEm=" + criadoEm +
                ", usuario='" + usuario + '\'' +
                '}';
    }
}
