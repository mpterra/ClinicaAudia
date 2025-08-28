package model;

import java.time.LocalDateTime;

public class Profissional {

    private int id;
    private String nome;
    private String cpf;
    private String email;
    private String telefone;
    private TipoProfissional tipo; // enum
    private Endereco endereco; // referência ao objeto Endereco
    private boolean ativo; // true = ativo, false = inativo
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    private String usuario;

    // Enum interno para tipo
    public enum TipoProfissional {
    	FONOAUDIOLOGA,SECRETARIA
    }

    // Construtor vazio
    public Profissional() {}

    // Construtor completo (sem datas)
    public Profissional(int id, String nome, String cpf, String email, String telefone,
                        TipoProfissional tipo, Endereco endereco, boolean ativo, String usuario) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.telefone = telefone;
        this.tipo = tipo;
        this.endereco = endereco;
        this.ativo = ativo;
        this.usuario = usuario;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public TipoProfissional getTipo() { return tipo; }
    public void setTipo(TipoProfissional tipo) { this.tipo = tipo; }

    public Endereco getEndereco() { return endereco; }
    public void setEndereco(Endereco endereco) { this.endereco = endereco; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    // Para debug/log
    @Override
    public String toString() {
        return "Profissional{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", cpf='" + cpf + '\'' +
                ", email='" + email + '\'' +
                ", telefone='" + telefone + '\'' +
                ", tipo=" + tipo +
                ", endereco=" + (endereco != null ? endereco.getId() : null) +
                ", ativo=" + ativo +
                ", criadoEm=" + criadoEm +
                ", atualizadoEm=" + atualizadoEm +
                ", usuario='" + usuario + '\'' +
                '}';
    }
}
