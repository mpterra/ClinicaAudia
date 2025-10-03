package controller;

import dao.DespesaDAO;
import model.Despesa;
import model.Despesa.Categoria;
import model.Despesa.FormaPagamento;
import model.Despesa.Status;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class DespesaController {

    private final DespesaDAO dao = new DespesaDAO();

    // ============================
    // CREATE
    // ============================
    public boolean adicionar(Despesa despesa, String usuarioLogado) throws SQLException {
        validarDespesa(despesa, false);
        return dao.salvar(despesa, usuarioLogado);
    }

    // ============================
    // READ
    // ============================
    public Despesa buscar(int id) throws SQLException {
        if (id <= 0) throw new IllegalArgumentException("ID inválido.");
        return dao.buscarPorId(id);
    }

    public List<Despesa> listarTodas() throws SQLException {
        return dao.listarTodos();
    }

    public List<Despesa> listarPorFiltros(
            Categoria categoria,
            Status status,
            FormaPagamento formaPagamento,
            Boolean recorrente,
            LocalDate inicio,
            LocalDate fim
    ) throws SQLException {
        return dao.listarPorFiltros(categoria, status, formaPagamento, recorrente, inicio, fim);
    }

    // ============================
    // UPDATE
    // ============================
    public boolean atualizar(Despesa despesa, String usuarioLogado) throws SQLException {
        validarDespesa(despesa, true);
        return dao.atualizar(despesa, usuarioLogado);
    }

    // ============================
    // DELETE
    // ============================
    public boolean remover(int id) throws SQLException {
        if (id <= 0) throw new IllegalArgumentException("ID inválido.");
        return dao.deletar(id);
    }

    // ============================
    // MÉTODOS AUXILIARES
    // ============================
    public boolean marcarComoPago(int id, LocalDate dataPagamento, String usuarioLogado) throws SQLException {
        return alterarStatus(id, Status.PAGO, dataPagamento, usuarioLogado);
    }

    public boolean marcarComoPendente(int id, String usuarioLogado) throws SQLException {
        return alterarStatus(id, Status.PENDENTE, null, usuarioLogado);
    }

    private boolean alterarStatus(int id, Status novoStatus, LocalDate dataPagamento, String usuarioLogado) throws SQLException {
        Despesa despesa = dao.buscarPorId(id);
        if (despesa == null) return false;

        despesa.setStatus(novoStatus);
        despesa.setDataPagamento(dataPagamento);
        return dao.atualizar(despesa, usuarioLogado);
    }

    // ============================
    // VALIDAÇÃO
    // ============================
    private void validarDespesa(Despesa despesa, boolean exigeId) {
        if (despesa == null)
            throw new IllegalArgumentException("Despesa não pode ser nula.");

        if (exigeId && despesa.getId() <= 0)
            throw new IllegalArgumentException("Despesa inválida para atualização.");

        if (despesa.getDescricao() == null || despesa.getDescricao().isBlank())
            throw new IllegalArgumentException("Descrição é obrigatória.");

        if (despesa.getCategoria() == null)
            throw new IllegalArgumentException("Categoria é obrigatória.");

        if (despesa.getFormaPagamento() == null)
            throw new IllegalArgumentException("Forma de pagamento é obrigatória.");

        if (despesa.getValor() == null || despesa.getValor().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Valor deve ser maior que zero.");

        if (despesa.getDataVencimento() == null)
            throw new IllegalArgumentException("Data de vencimento é obrigatória.");

        if (despesa.getUsuario() == null || despesa.getUsuario().isBlank())
            throw new IllegalArgumentException("Usuário é obrigatório.");
    }
}
