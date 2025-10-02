package controller;

import dao.PagamentoVendaDAO;
import model.PagamentoVenda;
import model.Venda;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PagamentoVendaController {

    private final PagamentoVendaDAO dao = new PagamentoVendaDAO();

    // -----------------------------
    // CRUD básico
    // -----------------------------
    public void inserir(PagamentoVenda pagamento) throws SQLException {
        dao.insert(pagamento);
    }

    public void atualizar(PagamentoVenda pagamento) throws SQLException {
        dao.update(pagamento);
    }

    public void deletar(int id) throws SQLException {
        dao.delete(id);
    }

    public PagamentoVenda buscarPorId(int id) throws SQLException {
        return dao.findById(id);
    }

    public List<PagamentoVenda> listarTodos() throws SQLException {
        return dao.findAll();
    }

    public List<PagamentoVenda> listarPorVenda(Venda venda) throws SQLException {
        return dao.findByVenda(venda);
    }

    // -----------------------------
    // Predicados genéricos
    // -----------------------------
    private List<PagamentoVenda> filtrar(List<PagamentoVenda> lista, Predicate<PagamentoVenda> filtro) {
        return lista.stream().filter(filtro).collect(Collectors.toList());
    }

    private Predicate<PagamentoVenda> faixaDataHora(LocalDateTime inicio, LocalDateTime fim) {
        return p -> p.getDataHora() != null &&
                    (p.getDataHora().isEqual(inicio) || p.getDataHora().isAfter(inicio)) &&
                    (p.getDataHora().isEqual(fim) || p.getDataHora().isBefore(fim));
    }

    private Predicate<PagamentoVenda> faixaDataVencimento(LocalDate inicio, LocalDate fim) {
        return p -> p.getDataVencimento() != null &&
                    (!p.getDataVencimento().isBefore(inicio)) &&
                    (!p.getDataVencimento().isAfter(fim));
    }

    private Predicate<PagamentoVenda> metodo(PagamentoVenda.MetodoPagamento m) {
        return p -> p.getMetodoPagamento() == m;
    }

    private Predicate<PagamentoVenda> venda(Venda v) {
        return p -> p.getVenda() != null && p.getVenda().getId() == v.getId();
    }

    // -----------------------------
    // Consultas avançadas
    // -----------------------------
    public List<PagamentoVenda> listarPorFaixaDataHora(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        return filtrar(dao.findAll(), faixaDataHora(inicio, fim));
    }

    public List<PagamentoVenda> listarPorFaixaDataVencimento(LocalDate inicio, LocalDate fim) throws SQLException {
        return filtrar(dao.findAll(), faixaDataVencimento(inicio, fim));
    }

    public List<PagamentoVenda> listarPorMetodo(PagamentoVenda.MetodoPagamento m) throws SQLException {
        return filtrar(dao.findAll(), metodo(m));
    }

    public List<PagamentoVenda> listarPorVendaEDataHora(Venda v, LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        return filtrar(dao.findByVenda(v), faixaDataHora(inicio, fim));
    }

    public List<PagamentoVenda> listarPorVendaEMetodo(Venda v, PagamentoVenda.MetodoPagamento m) throws SQLException {
        return filtrar(dao.findByVenda(v), metodo(m));
    }

    public List<PagamentoVenda> listarPorVendaEDataVencimento(Venda v, LocalDate inicio, LocalDate fim) throws SQLException {
        return filtrar(dao.findByVenda(v), faixaDataVencimento(inicio, fim));
    }

    public List<PagamentoVenda> listarPorVendaMetodoEDataHora(Venda v, PagamentoVenda.MetodoPagamento m, LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        return filtrar(dao.findByVenda(v), metodo(m).and(faixaDataHora(inicio, fim)));
    }

    public List<PagamentoVenda> listarPorVendaMetodoEDataVencimento(Venda v, PagamentoVenda.MetodoPagamento m, LocalDate inicio, LocalDate fim) throws SQLException {
        return filtrar(dao.findByVenda(v), metodo(m).and(faixaDataVencimento(inicio, fim)));
    }

    public List<PagamentoVenda> listarPorVendaDataHoraEVencimento(Venda v, LocalDateTime inicioHora, LocalDateTime fimHora,
                                                                   LocalDate inicioVenc, LocalDate fimVenc) throws SQLException {
        return filtrar(dao.findByVenda(v),
                faixaDataHora(inicioHora, fimHora).and(faixaDataVencimento(inicioVenc, fimVenc)));
    }

    public List<PagamentoVenda> listarPorVendaMetodoDataHoraEVencimento(Venda v, PagamentoVenda.MetodoPagamento m,
                                                                        LocalDateTime inicioHora, LocalDateTime fimHora,
                                                                        LocalDate inicioVenc, LocalDate fimVenc) throws SQLException {
        return filtrar(dao.findByVenda(v),
                venda(v)
                .and(metodo(m))
                .and(faixaDataHora(inicioHora, fimHora))
                .and(faixaDataVencimento(inicioVenc, fimVenc)));
    }
}
