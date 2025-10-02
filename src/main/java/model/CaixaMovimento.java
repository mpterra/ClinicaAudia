package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CaixaMovimento {

    // -------------------------
    // Enums
    // -------------------------
    public enum TipoMovimento {ENTRADA, SAIDA}
    public enum OrigemMovimento {PAGAMENTO_ATENDIMENTO, PAGAMENTO_VENDA, DESPESA, AJUSTE, OUTRO, PAGAMENTO_COMPRA}
    public enum FormaPagamento {DINHEIRO, PIX, DEBITO, CREDITO, BOLETO}

    // -------------------------
    // Atributos
    // -------------------------
    private int id;
    private Caixa caixa;
    private TipoMovimento tipo;
    private OrigemMovimento origem;
    private PagamentoAtendimento pagamentoAtendimento; // pode ser null
    private PagamentoVenda pagamentoVenda; // pode ser null
    private PagamentoCompra pagamentoCompra; // pode ser null
    private FormaPagamento formaPagamento;
    private BigDecimal valor;
    private String descricao;
    private LocalDateTime dataHora;
    private String usuario;

    // -------------------------
    // Construtores
    // -------------------------

    // Construtor vazio
    public CaixaMovimento() {}

    // Construtor completo
    public CaixaMovimento(int id, Caixa caixa, TipoMovimento tipo, OrigemMovimento origem,
                          PagamentoAtendimento pagamentoAtendimento, PagamentoVenda pagamentoVenda,
                          PagamentoCompra pagamentoCompra, FormaPagamento formaPagamento,
                          BigDecimal valor, String descricao, LocalDateTime dataHora, String usuario) {
        this.id = id;
        this.caixa = caixa;
        this.tipo = tipo;
        this.origem = origem;
        this.pagamentoAtendimento = pagamentoAtendimento;
        this.pagamentoVenda = pagamentoVenda;
        this.pagamentoCompra = pagamentoCompra;
        this.formaPagamento = formaPagamento;
        this.valor = valor;
        this.descricao = descricao;
        this.dataHora = dataHora;
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

    public Caixa getCaixa() {
        return caixa;
    }

    public void setCaixa(Caixa caixa) {
        this.caixa = caixa;
    }

    public TipoMovimento getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimento tipo) {
        this.tipo = tipo;
    }

    public OrigemMovimento getOrigem() {
        return origem;
    }

    public void setOrigem(OrigemMovimento origem) {
        this.origem = origem;
    }

    public PagamentoAtendimento getPagamentoAtendimento() {
        return pagamentoAtendimento;
    }

    public void setPagamentoAtendimento(PagamentoAtendimento pagamentoAtendimento) {
        this.pagamentoAtendimento = pagamentoAtendimento;
    }

    public PagamentoVenda getPagamentoVenda() {
        return pagamentoVenda;
    }

    public void setPagamentoVenda(PagamentoVenda pagamentoVenda) {
        this.pagamentoVenda = pagamentoVenda;
    }

    public PagamentoCompra getPagamentoCompra() {
        return pagamentoCompra;
    }

    public void setPagamentoCompra(PagamentoCompra pagamentoCompra) {
        this.pagamentoCompra = pagamentoCompra;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(FormaPagamento formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
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
        return "CaixaMovimento{" +
                "id=" + id +
                ", caixaId=" + (caixa != null ? caixa.getId() : "null") +
                ", tipo=" + tipo +
                ", origem=" + origem +
                ", pagamentoAtendimentoId=" + (pagamentoAtendimento != null ? pagamentoAtendimento.getId() : "null") +
                ", pagamentoVendaId=" + (pagamentoVenda != null ? pagamentoVenda.getId() : "null") +
                ", pagamentoCompraId=" + (pagamentoCompra != null ? pagamentoCompra.getId() : "null") +
                ", formaPagamento=" + formaPagamento +
                ", valor=" + valor +
                ", descricao='" + descricao + '\'' +
                ", dataHora=" + dataHora +
                ", usuario='" + usuario + '\'' +
                '}';
    }
}