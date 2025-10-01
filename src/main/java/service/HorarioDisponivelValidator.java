package service;

import controller.AtendimentoController;
import controller.EscalaProfissionalController;
import model.Atendimento;
import model.EscalaProfissional;
import model.Profissional;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço para validar e listar horários disponíveis, verificando escalas e conflitos.
 */
public class HorarioDisponivelValidator {
    private final EscalaProfissionalController escalaController;
    private final AtendimentoController atendimentoController;

    // Construtor
    public HorarioDisponivelValidator(EscalaProfissionalController escalaController, AtendimentoController atendimentoController) {
        this.escalaController = escalaController;
        this.atendimentoController = atendimentoController;
    }

    /**
     * Lista horários disponíveis para um profissional em uma data e tipo específicos.
     * @param prof Profissional selecionado.
     * @param data Data selecionada.
     * @param tipo Tipo de atendimento.
     * @return Lista de horários disponíveis.
     * @throws SQLException se houver erro ao acessar dados.
     */
    public List<LocalTime> listarHorariosDisponiveis(Profissional prof, LocalDate data, Atendimento.Tipo tipo) throws SQLException {
        List<LocalTime> horarios = new ArrayList<>();
        int duracao = (tipo == Atendimento.Tipo.AVALIACAO) ? 90 : 60;
        int diaSemana = data.getDayOfWeek().getValue() - 1;

        // Filtra escalas do profissional para o dia da semana
        List<EscalaProfissional> escalas = escalaController.listarTodas().stream()
                .filter(e -> e.getProfissionalId() == prof.getId() && e.getDiaSemana() == diaSemana && e.isDisponivel())
                .toList();

        // Lista atendimentos existentes do profissional na data
        List<Atendimento> atendimentos = atendimentoController.listarTodos().stream()
                .filter(a -> a.getProfissional().getId() == prof.getId()
                        && a.getDataHora().toLocalDateTime().toLocalDate().equals(data)
                        && a.getSituacao() != Atendimento.Situacao.CANCELADO)
                .toList();

        // Monta intervalos ocupados
        List<Intervalo> ocupados = new ArrayList<>();
        for (Atendimento a : atendimentos) {
            LocalTime inicio = a.getDataHora().toLocalDateTime().toLocalTime();
            LocalTime fim = inicio.plusMinutes(a.getDuracaoMin());
            ocupados.add(new Intervalo(inicio, fim));
        }

        // Itera sobre escalas e calcula horários disponíveis
        for (EscalaProfissional e : escalas) {
            LocalTime horaInicio = e.getHoraInicio().toLocalTime();
            LocalTime fimEscala = e.getHoraFim().toLocalTime();

            // Calcula quantidade de intervalos de 30 minutos
            long minutosTotais = ChronoUnit.MINUTES.between(horaInicio, fimEscala);
            int intervalos = (int) (minutosTotais / 30);

            // Itera sobre intervalos de 30 minutos
            for (int i = 0; i <= intervalos; i++) {
                LocalTime hora = horaInicio.plusMinutes(i * 30);
                LocalTime fimProposto = hora.plusMinutes(duracao);

                // Verifica se o intervalo excede a escala
                if (fimProposto.isAfter(fimEscala)) {
                    continue;
                }

                // Verifica se há sobreposição com atendimentos existentes
                boolean sobreposto = ocupados.stream()
                        .anyMatch(occ -> !(fimProposto.compareTo(occ.inicio) <= 0 || hora.compareTo(occ.fim) >= 0));

                // Adiciona horário se não houver conflito
                if (!sobreposto) {
                    horarios.add(hora);
                }
            }
        }

        return horarios;
    }

    /**
     * Valida se há conflito de horário para o proposto.
     * @param inicioProposto Início proposto.
     * @param duracao Duração em minutos.
     * @param atendimentos Lista de atendimentos existentes.
     * @throws Exception se houver conflito.
     */
    public void validarConflito(LocalDateTime inicioProposto, int duracao, List<Atendimento> atendimentos) throws Exception {
        LocalDateTime fimProposto = inicioProposto.plusMinutes(duracao);
        for (Atendimento a : atendimentos) {
            LocalDateTime inicioExist = a.getDataHora().toLocalDateTime();
            LocalDateTime fimExist = inicioExist.plusMinutes(a.getDuracaoMin());
            if (!(fimProposto.compareTo(inicioExist) <= 0 || inicioProposto.compareTo(fimExist) >= 0)) {
                throw new Exception("Horário Indisponível para o profissional");
            }
        }
    }

    // Classe auxiliar interna
    private static class Intervalo {
        LocalTime inicio;
        LocalTime fim;

        Intervalo(LocalTime inicio, LocalTime fim) {
            this.inicio = inicio;
            this.fim = fim;
        }
    }
}