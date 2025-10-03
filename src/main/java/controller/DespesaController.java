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

    /**
     * Verifica se a despesa é a última recorrente não paga da série.
     * @param despesa Despesa a ser verificada
     * @return true se for a última recorrente não paga, false caso contrário
     * @throws SQLException Se houver erro no acesso ao banco de dados
     */
    public boolean isUltimaDespesaRecorrente(Despesa despesa) throws SQLException {
        if (!despesa.isRecorrente()) return false;
        List<Despesa> despesasRecorrentes = listarPorFiltros(
            despesa.getCategoria(),
            Despesa.Status.PENDENTE,
            despesa.getFormaPagamento(),
            true,
            despesa.getDataVencimento(),
            null
        );
        return despesasRecorrentes.stream()
            .noneMatch(d -> (d.getDescricao().startsWith(despesa.getDescricao()) || d.getDescricao().equals(despesa.getDescricao()))
                && d.getId() != despesa.getId());
    }

    // ============================
    // UPDATE
    // ============================
    public boolean atualizar(Despesa despesa, String usuarioLogado) throws SQLException {
        validarDespesa(despesa, true);
        return dao.atualizar(despesa, usuarioLogado);
    }

    public void atualizarRecorrentes(Despesa despesa, String usuarioLogado, LocalDate dataVencimento) throws SQLException {
        List<Despesa> despesasRecorrentes = listarPorFiltros(
            despesa.getCategoria(),
            Despesa.Status.PENDENTE,
            despesa.getFormaPagamento(),
            true,
            dataVencimento,
            null
        );

        for (Despesa d : despesasRecorrentes) {
            if (d.getDescricao().startsWith(despesa.getDescricao()) || d.getDescricao().equals(despesa.getDescricao())) {
                d.setCategoria(despesa.getCategoria());
                d.setDescricao(despesa.getDescricao());
                d.setValor(despesa.getValor());
                d.setFormaPagamento(despesa.getFormaPagamento());
                d.setDataVencimento(d.getDataVencimento());
                d.setDataPagamento(despesa.getDataPagamento());
                d.setStatus(despesa.getStatus());
                d.setRecorrente(despesa.isRecorrente());
                atualizar(d, usuarioLogado);
            }
        }
    }

    // ============================
    // DELETE
    // ============================
    public boolean remover(int id) throws SQLException {
        if (id <= 0) throw new IllegalArgumentException("ID inválido.");
        return dao.deletar(id);
    }

    public void removerRecorrentes(Despesa despesa, LocalDate dataVencimento) throws SQLException {
        List<Despesa> despesasRecorrentes = listarPorFiltros(
            despesa.getCategoria(),
            Despesa.Status.PENDENTE,
            despesa.getFormaPagamento(),
            true,
            dataVencimento,
            null
        );

        for (Despesa d : despesasRecorrentes) {
            if (d.getDescricao().startsWith(despesa.getDescricao()) || d.getDescricao().equals(despesa.getDescricao())) {
                remover(d.getId());
            }
        }
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