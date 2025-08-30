package controller;

import dao.VendaProdutoDAO;
import model.VendaProduto;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class VendaProdutoController {

    private final VendaProdutoDAO dao;

    public VendaProdutoController() {
        this.dao = new VendaProdutoDAO();
    }

    // ============================
    // CREATE
    // ============================
    public VendaProduto salvar(VendaProduto vp) throws SQLException {
        return dao.salvar(vp);
    }

    // ============================
    // READ / LISTAR
    // ============================
    public List<VendaProduto> listarTodos() throws SQLException {
        return dao.listarTodos();
    }

    public List<VendaProduto> listarPorVenda(int vendaId) throws SQLException {
        return dao.listarPorVenda(vendaId);
    }

    public List<VendaProduto> listarPorProduto(int produtoId) throws SQLException {
        return dao.listarPorProduto(produtoId);
    }

    public List<VendaProduto> listarPorData(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        return dao.listarPorData(inicio, fim);
    }

    public List<VendaProduto> listarPorPaciente(int pacienteId) throws SQLException {
        return dao.listarPorPaciente(pacienteId);
    }

    public List<VendaProduto> listarPorPacienteEData(int pacienteId, LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        return dao.listarPorPacienteEData(pacienteId, inicio, fim);
    }

    // ============================
    // TOTAL / QUANTIDADE
    // ============================
    public BigDecimal totalVendidoPorPaciente(int pacienteId) throws SQLException {
        return dao.totalVendidoPorPaciente(pacienteId);
    }

    public BigDecimal totalVendidoPorProduto(int produtoId) throws SQLException {
        return dao.totalVendidoPorProduto(produtoId);
    }

    public int quantidadeVendidaPorProduto(int produtoId) throws SQLException {
        return dao.quantidadeVendidaPorProduto(produtoId);
    }

    public BigDecimal totalVendidoPorData(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        return dao.totalVendidoPorData(inicio, fim);
    }

    public BigDecimal totalVendidoPorPacienteEData(int pacienteId, LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        return dao.totalVendidoPorPacienteEData(pacienteId, inicio, fim);
    }

    // ============================
    // DELETE
    // ============================
    public boolean deletar(int vendaId, int produtoId) throws SQLException {
        return dao.deletar(vendaId, produtoId);
    }
}
