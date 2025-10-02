package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Despesa {

    // =========================
    // ENUMs correspondentes à tabela
    // =========================
    public enum Categoria {
        PESSOAL,        // salários, encargos, pró-labore
        OPERACIONAL,    // aluguel, luz, água, internet
        ADMINISTRATIVA, // material escritório, sistemas
        VARIAVEL,       // motoboy, pequenas compras
        IMPOSTOS,       // tributos, taxas
        OUTROS          // algo que não se encaixe
    }

    public enum FormaPagamento {
        DINHEIRO, 
        DEBITO, 
        CREDITO, 
        PIX, 
        BOLETO
    }

    public enum Status {
        PENDENTE, 
        PAGO, 
        CANCELADO
    }

    // =========================
    // Atributos
    // =========================
    private int id;
    private String descricao;
    private Categoria categoria;
    private BigDecimal valor;
    private FormaPagamento formaPagamento;
    private LocalDate dataVencimento;
    private LocalDate dataPagamento; // pode ser null
    private Status status = Status.PENDENTE; // default conforme tabela
    private String usuario; // quem lançou
    private LocalDateTime dataHora; // registro de auditoria (CURRENT_TIMESTAMP)

    // =========================
    // Construtores
    // =========================
    public Despesa() {
    }

    public Despesa(int id, String descricao, Categoria categoria, BigDecimal valor, 
                   FormaPagamento formaPagamento, LocalDate dataVencimento, 
                   LocalDate dataPagamento, Status status, String usuario, 
                   LocalDateTime dataHora) {
        this.id = id;
        this.descricao = descricao;
        this.categoria = categoria;
        this.valor = valor;
        this.formaPagamento = formaPagamento;
        this.dataVencimento = dataVencimento;
        this.dataPagamento = dataPagamento;
        this.status = status;
        this.usuario = usuario;
        this.dataHora = dataHora;
    }

    // =========================
    // Getters e Setters
    // =========================
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(FormaPagamento formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(LocalDate dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(LocalDate dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
}
