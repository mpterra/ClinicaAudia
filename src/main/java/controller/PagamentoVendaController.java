package controller;

import dao.PagamentoVendaDAO;
import model.PagamentoVenda;
import model.Venda;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PagamentoVendaController {

    private final PagamentoVendaDAO dao = new PagamentoVendaDAO();

    // -----------------------------
    // Inserir pagamento
    // -----------------------------
    public void inserir(PagamentoVenda pagamento) throws SQLException {
        dao.insert(pagamento);
    }

    // -----------------------------
    // Atualizar pagamento
    // -----------------------------
    public void atualizar(PagamentoVenda pagamento) throws SQLException {
        dao.update(pagamento);
    }

    // -----------------------------
    // Deletar pagamento
    // -----------------------------
    public void deletar(int id) throws SQLException {
        dao.delete(id);
    }

    // -----------------------------
    // Buscar por ID
    // -----------------------------
    public PagamentoVenda buscarPorId(int id) throws SQLException {
        return dao.findById(id);
    }

    // -----------------------------
    // Buscar por venda
    // -----------------------------
    public List<PagamentoVenda> listarPorVenda(Venda venda) throws SQLException {
        return dao.findByVenda(venda);
    }

    // -----------------------------
    // Listar todos
    // -----------------------------
    public List<PagamentoVenda> listarTodos() throws SQLException {
        return dao.findAll();
    }

    // -----------------------------
    // Buscar por faixa de datas (data_hora)
    // -----------------------------
    public List<PagamentoVenda> listarPorFaixaData(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        List<PagamentoVenda> todas = dao.findAll();
        List<PagamentoVenda> filtradas = new ArrayList<>();
        for (PagamentoVenda p : todas) {
            if (p.getDataHora() != null &&
                (p.getDataHora().isEqual(inicio) || p.getDataHora().isAfter(inicio)) &&
                (p.getDataHora().isEqual(fim) || p.getDataHora().isBefore(fim))) {
                filtradas.add(p);
            }
        }
        return filtradas;
    }

    // -----------------------------
    // Buscar por método de pagamento
    // -----------------------------
    public List<PagamentoVenda> listarPorMetodo(PagamentoVenda.MetodoPagamento metodo) throws SQLException {
        List<PagamentoVenda> todas = dao.findAll();
        List<PagamentoVenda> filtradas = new ArrayList<>();
        for (PagamentoVenda p : todas) {
            if (p.getMetodoPagamento() == metodo) {
                filtradas.add(p);
            }
        }
        return filtradas;
    }

    // -----------------------------
    // Buscar por venda + faixa de datas
    // -----------------------------
    public List<PagamentoVenda> listarPorVendaEData(Venda venda, LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        List<PagamentoVenda> todas = dao.findByVenda(venda);
        List<PagamentoVenda> filtradas = new ArrayList<>();
        for (PagamentoVenda p : todas) {
            if (p.getDataHora() != null &&
                (p.getDataHora().isEqual(inicio) || p.getDataHora().isAfter(inicio)) &&
                (p.getDataHora().isEqual(fim) || p.getDataHora().isBefore(fim))) {
                filtradas.add(p);
            }
        }
        return filtradas;
    }

    // -----------------------------
    // Buscar por venda + método
    // -----------------------------
    public List<PagamentoVenda> listarPorVendaEMetodo(Venda venda, PagamentoVenda.MetodoPagamento metodo) throws SQLException {
        List<PagamentoVenda> todas = dao.findByVenda(venda);
        List<PagamentoVenda> filtradas = new ArrayList<>();
        for (PagamentoVenda p : todas) {
            if (p.getMetodoPagamento() == metodo) {
                filtradas.add(p);
            }
        }
        return filtradas;
    }

    // -----------------------------
    // Buscar por venda + método + faixa de datas
    // -----------------------------
    public List<PagamentoVenda> listarPorVendaMetodoEData(Venda venda, PagamentoVenda.MetodoPagamento metodo, LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        List<PagamentoVenda> todas = dao.findByVenda(venda);
        List<PagamentoVenda> filtradas = new ArrayList<>();
        for (PagamentoVenda p : todas) {
            if (p.getMetodoPagamento() == metodo &&
                p.getDataHora() != null &&
                (p.getDataHora().isEqual(inicio) || p.getDataHora().isAfter(inicio)) &&
                (p.getDataHora().isEqual(fim) || p.getDataHora().isBefore(fim))) {
                filtradas.add(p);
            }
        }
        return filtradas;
    }
}
