package service;

import controller.AtendimentoController;
import model.Atendimento;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço para filtrar e listar atendimentos para a tabela.
 */
public class AtendimentoFilter {
    private final AtendimentoController atendimentoController;
    private final DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Construtor
    public AtendimentoFilter(AtendimentoController atendimentoController) {
        this.atendimentoController = atendimentoController;
    }

    /**
     * Lista atendimentos filtrados por regras: não-cancelados, incluindo agendados passados.
     * @param dataAtual Data atual para filtro.
     * @param paciente Filtro por nome de paciente (opcional).
     * @param profissional Filtro por nome de profissional (opcional).
     * @return Lista de objetos para a tabela.
     * @throws SQLException se houver erro ao listar.
     */
    public List<Object[]> listarAtendimentosFiltrados(LocalDate dataAtual, String paciente, String profissional) throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        List<Atendimento> atendimentos = atendimentoController.listarTodos();

        for (Atendimento a : atendimentos) {
            LocalDate dataAtendimento = a.getDataHora().toLocalDateTime().toLocalDate();
            Atendimento.Situacao situacao = a.getSituacao();

            if (situacao != Atendimento.Situacao.CANCELADO &&
                    (!dataAtendimento.isBefore(dataAtual) || (dataAtendimento.isBefore(dataAtual) && situacao == Atendimento.Situacao.AGENDADO))) {

                String pacienteNome = a.getPaciente().getNome();
                String profNome = a.getProfissional().getNome();

                if ((paciente == null || pacienteNome.toLowerCase().contains(paciente.toLowerCase())) &&
                    (profissional == null || profNome.toLowerCase().contains(profissional.toLowerCase()))) {

                    String empresaNome = a.getEmpresaParceira() != null ? a.getEmpresaParceira().getNome() : "Nenhuma";
                    
                    // Ajuste para exibição: se valor for zero, exibe ISENTO independentemente do status salvo
                    Atendimento.StatusPagamento statusExibicao = a.getValor().equals(BigDecimal.ZERO) ? Atendimento.StatusPagamento.ISENTO : a.getStatusPagamento();

                    rows.add(new Object[] {
                            dataAtendimento.format(formatoData),
                            a.getDataHora().toLocalDateTime().toLocalTime(),
                            pacienteNome,
                            profNome,
                            empresaNome,
                            a.getTipo(),
                            situacao,
                            statusExibicao
                    });
                }
            }
        }
        return rows;
    }
}