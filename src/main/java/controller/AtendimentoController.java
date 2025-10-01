package controller;

import dao.AtendimentoDAO;
import model.Atendimento;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller para gerenciar atendimentos.
 */
public class AtendimentoController {

    private final AtendimentoDAO dao;

    public AtendimentoController() {
        this.dao = new AtendimentoDAO();
    }

    // ===========================
    // CRIAR ATENDIMENTO
    // ===========================
    public boolean criarAtendimento(Atendimento at, String usuarioLogado) throws SQLException {
        validar(at, null);
        // Força definição do statusPagamento baseado no valor ANTES de salvar
        forcarStatusPagamento(at);
        // Debug: imprime para verificar
        System.out.println("DEBUG - Criando atendimento: Valor = " + at.getValor() + ", StatusPagamento = " + at.getStatusPagamento());
        return dao.salvar(at, usuarioLogado);
    }

    // ===========================
    // ATUALIZAR ATENDIMENTO
    // ===========================
    public boolean atualizarAtendimento(Atendimento at, String usuarioLogado) throws SQLException {
        validar(at, at.getId());
        // Força definição do statusPagamento baseado no valor ANTES de salvar
        forcarStatusPagamento(at);
        // Debug: imprime para verificar
        System.out.println("DEBUG - Atualizando atendimento: Valor = " + at.getValor() + ", StatusPagamento = " + at.getStatusPagamento());
        return dao.atualizar(at, usuarioLogado);
    }

    // ===========================
    // REMOVER ATENDIMENTO
    // ===========================
    public boolean removerAtendimento(int id) throws SQLException {
        return dao.deletar(id);
    }

    // ===========================
    // BUSCAR
    // ===========================
    public Atendimento buscarPorId(int id) throws SQLException {
        return dao.buscarPorId(id);
    }

    public List<Atendimento> listarTodos() throws SQLException {
        return dao.listarTodos();
    }

    public List<Atendimento> listarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        return dao.listarPorPeriodo(inicio, fim);
    }

    // ===========================
    // VALIDAÇÕES INTERNAS
    // ===========================
    private void validar(Atendimento at, Integer idAtual) throws SQLException {
        validarDuracao(at);
        validarProfissional(at);
        validarDisponibilidade(at, idAtual);
        // Removida validação de status aqui para evitar sobrescrita; agora é forçada antes de salvar
    }

    private void validarDuracao(Atendimento at) {
        if (at.getDuracaoMin() <= 0) {
            throw new IllegalArgumentException("A duração do atendimento deve ser maior que zero.");
        }
    }

    private void validarProfissional(Atendimento at) {
        if (at.getProfissionalId() == 0) {
            throw new IllegalArgumentException("Profissional não selecionado ou inválido.");
        }
    }

    private void validarDisponibilidade(Atendimento at, Integer idAtual) throws SQLException {
        boolean disponivel = dao.isDisponivel(
                at.getProfissionalId(),
                at.getDataHora(),
                at.getDuracaoMin(),
                idAtual
        );

        if (!disponivel) {
            throw new IllegalArgumentException("Horário indisponível para o profissional.");
        }
    }

    /**
     * Força a definição do statusPagamento baseado no valor do atendimento.
     * Aplicado imediatamente antes de salvar/atualizar.
     */
    private void forcarStatusPagamento(Atendimento at) {
        if (at.getValor() == null || at.getValor().compareTo(BigDecimal.ZERO) == 0) {
            at.setStatusPagamento(Atendimento.StatusPagamento.ISENTO);
        } else {
            if (at.getStatusPagamento() == Atendimento.StatusPagamento.ISENTO) {
                at.setStatusPagamento(Atendimento.StatusPagamento.PENDENTE);
            }
        }
    }

    /**
     * Corrige status de pagamentos existentes no banco (para dados históricos).
     * Execute isso uma vez para corrigir atendimentos como ID 7.
     */
    public void corrigirStatusPagamentosExistentes() throws SQLException {
        System.out.println("DEBUG - Iniciando correção de status de pagamentos...");
        List<Atendimento> atendimentos = listarTodos();
        int corrigidos = 0;
        for (Atendimento at : atendimentos) {
            boolean precisaCorrigir = (at.getValor() == null || at.getValor().compareTo(BigDecimal.ZERO) == 0) 
                    && at.getStatusPagamento() != Atendimento.StatusPagamento.ISENTO;
            if (precisaCorrigir) {
                at.setStatusPagamento(Atendimento.StatusPagamento.ISENTO);
                atualizarAtendimento(at, "system");
                corrigidos++;
                System.out.println("DEBUG - Corrigido atendimento ID " + at.getId() + ": Valor = " + at.getValor() + ", Status = ISENTO");
            }
        }
        System.out.println("DEBUG - Correção concluída. " + corrigidos + " atendimentos corrigidos.");
    }
}