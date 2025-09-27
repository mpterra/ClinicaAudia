package controller;

import dao.DespesaDAO;
import model.Despesa;
import model.Despesa.Categoria;
import model.Despesa.FormaPagamento;
import model.Despesa.Status;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class DespesaController {

    private final DespesaDAO dao;

    public DespesaController() {
        this.dao = new DespesaDAO();
    }

    // ============================
    // CREATE
    // ============================
    public boolean adicionarDespesa(Despesa despesa, String usuarioLogado) throws SQLException {
        if (despesa.getDescricao() == null || despesa.getDescricao().isEmpty())
            throw new IllegalArgumentException("Descrição é obrigatória.");

        if (despesa.getValor() == null || despesa.getValor().doubleValue() <= 0)
            throw new IllegalArgumentException("Valor deve ser maior que zero.");

        return dao.salvar(despesa, usuarioLogado);
    }

    // ============================
    // READ
    // ============================
    public Despesa buscarDespesa(int id) throws SQLException {
        return dao.buscarPorId(id);
    }

    public List<Despesa> listarTodas() throws SQLException {
        return dao.listarTodos();
    }

    public List<Despesa> listarPorFiltros(
            Categoria categoria,
            Status status,
            FormaPagamento formaPagamento,
            LocalDate inicio,
            LocalDate fim
    ) throws SQLException {
        return dao.listarPorFiltros(categoria, status, formaPagamento, inicio, fim);
    }

    // ============================
    // UPDATE
    // ============================
    public boolean atualizarDespesa(Despesa despesa, String usuarioLogado) throws SQLException {
        if (despesa.getId() <= 0)
            throw new IllegalArgumentException("Despesa inválida.");

        return dao.atualizar(despesa, usuarioLogado);
    }

    // ============================
    // DELETE
    // ============================
    public boolean removerDespesa(int id) throws SQLException {
        if (id <= 0)
            throw new IllegalArgumentException("ID inválido.");
        return dao.deletar(id);
    }

    // ============================
    // MÉTODOS AUXILIARES
    // ============================
    public boolean marcarComoPago(int id, LocalDate dataPagamento, String usuarioLogado) throws SQLException {
        Despesa despesa = dao.buscarPorId(id);
        if (despesa == null) return false;

        despesa.setStatus(Status.PAGO);
        despesa.setDataPagamento(dataPagamento);
        return dao.atualizar(despesa, usuarioLogado);
    }

    public boolean marcarComoPendente(int id, String usuarioLogado) throws SQLException {
        Despesa despesa = dao.buscarPorId(id);
        if (despesa == null) return false;

        despesa.setStatus(Status.PENDENTE);
        despesa.setDataPagamento(null);
        return dao.atualizar(despesa, usuarioLogado);
    }
}
