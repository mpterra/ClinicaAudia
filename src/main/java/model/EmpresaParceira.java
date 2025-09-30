package model;

import java.time.LocalDateTime;

public class EmpresaParceira {
    private int id;
    private String nome;
    private String cnpj;
    private String telefone;
    private String email;
    private Endereco endereco; // Referência ao objeto Endereco
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    private String usuario;

    // Construtor vazio
    public EmpresaParceira() {
    }

    // Construtor completo (sem datas de auditoria)
    public EmpresaParceira(int id, String nome, String cnpj, String telefone, String email, Endereco endereco, String usuario) {
        this.id = id;
        this.nome = nome;
        this.cnpj = cnpj;
        this.telefone = telefone;
        this.email = email;
        this.endereco = endereco;
        this.usuario = usuario;
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    // Para debug/log
    @Override
    public String toString() {
        return nome;
    }
}