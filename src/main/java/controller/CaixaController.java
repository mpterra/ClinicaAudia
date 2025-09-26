package controller;

import dao.CaixaDAO;
import model.Caixa;

import java.sql.SQLException;
import java.util.List;

// Controller para lógica de negócio de caixas
public class CaixaController {

    private final CaixaDAO caixaDAO;

    public CaixaController() {
        this.caixaDAO = new CaixaDAO();
    }

    // -------------------------
    // CRUD inteligente
    // -------------------------

    // Abre um novo caixa
    public boolean abrirCaixa(Caixa caixa) throws SQLException {
        if (caixa == null) throw new IllegalArgumentException("Caixa não pode ser nulo.");

        // Regra: não pode abrir novo caixa se já houver um aberto
        if (existeCaixaAberto()) {
            throw new IllegalStateException("Já existe um caixa aberto. Feche-o antes de abrir outro.");
        }

        return caixaDAO.inserir(caixa); // Retorna true se inserção bem-sucedida
    }

    // Atualiza um caixa
    public void atualizarCaixa(Caixa caixa) throws SQLException {
        if (caixa == null || caixa.getId() <= 0) throw new IllegalArgumentException("Caixa inválido.");

        // Regra: só pode atualizar caixa aberto
        Caixa existente = caixaDAO.buscarPorId(caixa.getId());
        if (existente == null) throw new IllegalArgumentException("Caixa não encontrado.");
        if (existente.getDataFechamento() != null) {
            throw new IllegalStateException("Não é possível atualizar um caixa já fechado.");
        }

        caixaDAO.atualizar(caixa);
    }

    // Fecha o caixa
    public boolean fecharCaixa(Caixa caixa) throws SQLException {
        if (caixa == null || caixa.getId() <= 0) throw new IllegalArgumentException("Caixa inválido.");

        Caixa existente = caixaDAO.buscarPorId(caixa.getId());
        if (existente == null) throw new IllegalArgumentException("Caixa não encontrado.");
        if (existente.getDataFechamento() != null) {
            throw new IllegalStateException("Caixa já está fechado.");
        }

        return caixaDAO.fecharCaixa(caixa); // Retorna true se fechamento bem-sucedido
    }

    // Deleta um caixa
    public void deletarCaixa(int id) throws SQLException {
        Caixa existente = caixaDAO.buscarPorId(id);
        if (existente == null) throw new IllegalArgumentException("Caixa não encontrado.");
        if (existente.getDataFechamento() != null) {
            throw new IllegalStateException("Não é permitido deletar caixas já fechados.");
        }

        caixaDAO.deletar(id);
    }

    // -------------------------
    // Consultas
    // -------------------------

    // Busca caixa por ID
    public Caixa buscarPorId(int id) throws SQLException {
        if (id <= 0) return null;
        return caixaDAO.buscarPorId(id);
    }

    // Lista todos os caixas
    public List<Caixa> listarTodos() throws SQLException {
        return caixaDAO.listarTodos();
    }

    // Verifica se existe um caixa aberto
    public boolean existeCaixaAberto() throws SQLException {
        return caixaDAO.buscarCaixaAberto() != null;
    }

    // Obtém o caixa aberto
    public Caixa getCaixaAberto() throws SQLException {
        return caixaDAO.buscarCaixaAberto();
    }
}