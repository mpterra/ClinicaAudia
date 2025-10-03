package model;

import java.time.LocalDateTime;

public class EmprestimoProduto {

    private int id;
    private int produtoId;
    private int pacienteId;
    private int profissionalId;
    private LocalDateTime dataEmprestimo;
    private LocalDateTime dataDevolucao;
    private boolean devolvido;
    private String observacoes;
    private String usuario;

    // ============================
    // Construtores
    // ============================
    public EmprestimoProduto() {
    }

    public EmprestimoProduto(int id, int produtoId, int pacienteId, int profissionalId,
                             LocalDateTime dataEmprestimo, LocalDateTime dataDevolucao,
                             boolean devolvido, String observacoes, String usuario) {
        this.id = id;
        this.produtoId = produtoId;
        this.pacienteId = pacienteId;
        this.profissionalId = profissionalId;
        this.dataEmprestimo = dataEmprestimo;
        this.dataDevolucao = dataDevolucao;
        this.devolvido = devolvido;
        this.observacoes = observacoes;
        this.usuario = usuario;
    }

    // ============================
    // Getters e Setters
    // ============================
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(int produtoId) {
        this.produtoId = produtoId;
    }

    public int getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(int pacienteId) {
        this.pacienteId = pacienteId;
    }

    public int getProfissionalId() {
        return profissionalId;
    }

    public void setProfissionalId(int profissionalId) {
        this.profissionalId = profissionalId;
    }

    public LocalDateTime getDataEmprestimo() {
        return dataEmprestimo;
    }

    public void setDataEmprestimo(LocalDateTime dataEmprestimo) {
        this.dataEmprestimo = dataEmprestimo;
    }

    public LocalDateTime getDataDevolucao() {
        return dataDevolucao;
    }

    public void setDataDevolucao(LocalDateTime dataDevolucao) {
        this.dataDevolucao = dataDevolucao;
    }

    public boolean isDevolvido() {
        return devolvido;
    }

    public void setDevolvido(boolean devolvido) {
        this.devolvido = devolvido;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    // ============================
    // toString()
    // ============================
    @Override
    public String toString() {
        return "EmprestimoProduto{" +
                "id=" + id +
                ", produtoId=" + produtoId +
                ", pacienteId=" + pacienteId +
                ", profissionalId=" + profissionalId +
                ", dataEmprestimo=" + dataEmprestimo +
                ", dataDevolucao=" + dataDevolucao +
                ", devolvido=" + devolvido +
                ", observacoes='" + observacoes + '\'' +
                ", usuario='" + usuario + '\'' +
                '}';
    }
}
