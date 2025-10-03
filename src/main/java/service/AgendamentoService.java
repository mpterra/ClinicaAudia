package service;

import controller.AtendimentoController;
import dto.AgendamentoRequest;
import exception.CampoObrigatorioException;
import model.Atendimento;
import model.EmpresaParceira;
import model.Profissional;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Serviço responsável por orquestrar a criação e atualização de agendamentos,
 * realizando validações, cálculos e interações com o banco de dados.
 */
public class AgendamentoService {
    private final AtendimentoController atendimentoController;
    private final HorarioDisponivelValidator horarioValidator;
    private final ValorAtendimentoCalculator valorCalculator;
    private final ValidacaoGeralService validacaoService;

    /**
     * Construtor com injeção de dependências.
     *
     * @param atendimentoController Controlador para operações de persistência de atendimentos.
     * @param horarioValidator Validador de horários disponíveis.
     * @param valorCalculator Calculador de valores de atendimento.
     * @param validacaoService Serviço de validações gerais.
     */
    public AgendamentoService(AtendimentoController atendimentoController,
                             HorarioDisponivelValidator horarioValidator,
                             ValorAtendimentoCalculator valorCalculator,
                             ValidacaoGeralService validacaoService) {
        this.atendimentoController = Objects.requireNonNull(atendimentoController, "AtendimentoController não pode ser nulo");
        this.horarioValidator = Objects.requireNonNull(horarioValidator, "HorarioDisponivelValidator não pode ser nulo");
        this.valorCalculator = Objects.requireNonNull(valorCalculator, "ValorAtendimentoCalculator não pode ser nulo");
        this.validacaoService = Objects.requireNonNull(validacaoService, "ValidacaoGeralService não pode ser nulo");
    }

    /**
     * Cria um novo agendamento após validações e cálculos.
     *
     * @param request DTO com os dados do agendamento.
     * @return true se o agendamento for salvo com sucesso.
     * @throws SQLException se houver erro no acesso ao banco de dados.
     * @throws CampoObrigatorioException se campos obrigatórios estiverem inválidos.
     * @throws IllegalArgumentException se houver erro nas validações de horário ou valor.
     */
    public boolean criarAgendamento(AgendamentoRequest request) throws SQLException, CampoObrigatorioException {
        validarRequest(request);
        int duracao = calcularDuracao(request.getTipo());
        validarHorario(request, duracao, true);
        BigDecimal valor = calcularValor(request);
        Atendimento atendimento = criarAtendimento(request, duracao, valor);
        return atendimentoController.criarAtendimento(atendimento, request.getUsuarioLogin());
    }

    /**
     * Atualiza um agendamento existente após validações e cálculos.
     *
     * @param atendimento Atendimento a ser atualizado.
     * @param request DTO com os novos dados do agendamento.
     * @throws SQLException se houver erro no acesso ao banco de dados.
     * @throws CampoObrigatorioException se campos obrigatórios estiverem inválidos.
     * @throws IllegalArgumentException se houver erro nas validações de horário ou valor.
     */
    public void atualizarAgendamento(Atendimento atendimento, AgendamentoRequest request)
            throws SQLException, CampoObrigatorioException {
        Objects.requireNonNull(atendimento, "Atendimento não pode ser nulo");
        validarRequest(request);
        int duracao = calcularDuracao(request.getTipo());
        validarHorario(request, duracao, false, atendimento.getId());
        BigDecimal valor = calcularValor(request);
        atualizarAtendimento(atendimento, request, duracao, valor);
        atendimentoController.atualizarAtendimento(atendimento, request.getUsuarioLogin());
    }

    /**
     * Valida os campos obrigatórios e a data do agendamento.
     *
     * @param request DTO com os dados do agendamento.
     * @throws CampoObrigatorioException se campos obrigatórios estiverem inválidos.
     */
    private void validarRequest(AgendamentoRequest request) throws CampoObrigatorioException {
        validacaoService.validarCamposObrigatorios(request);
        validacaoService.validarDataFutura(request.getDataHora());
    }

    /**
     * Calcula a duração do atendimento com base no tipo.
     *
     * @param tipo Tipo de atendimento.
     * @return Duração em minutos (90 para AVALIACAO, 60 para outros).
     */
    private int calcularDuracao(Atendimento.Tipo tipo) {
        return tipo == Atendimento.Tipo.AVALIACAO ? 90 : 60;
    }

    /**
     * Valida se o horário está disponível, considerando conflitos de agendamento.
     *
     * @param request DTO com os dados do agendamento.
     * @param duracao Duração do atendimento.
     * @param isNovo Indica se é um novo agendamento (true) ou atualização (false).
     * @param atendimentoId ID do atendimento (usado em atualizações para excluir o próprio).
     * @throws SQLException se houver erro ao listar atendimentos.
     * @throws IllegalArgumentException se houver conflito de horário.
     */
    private void validarHorario(AgendamentoRequest request, int duracao, boolean isNovo, int... atendimentoId)
            throws SQLException {
        List<Atendimento> atendimentos = atendimentoController.listarTodos().stream()
                .filter(a -> a.getProfissional().getId() == request.getProfissional().getId()
                        && a.getDataHora().toLocalDateTime().toLocalDate().equals(request.getDataHora().toLocalDate())
                        && a.getSituacao() != Atendimento.Situacao.CANCELADO
                        && (isNovo || a.getId() != atendimentoId[0]))
                .toList();
        try {
            horarioValidator.validarConflito(request.getDataHora(), duracao, atendimentos);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Calcula o valor do atendimento com base no profissional, tipo e empresa.
     *
     * @param request DTO com os dados do agendamento.
     * @return Valor calculado do atendimento.
     * @throws IllegalArgumentException se houver erro no cálculo.
     */
    private BigDecimal calcularValor(AgendamentoRequest request) {
        try {
            return valorCalculator.calcularValor(request.getProfissional(), request.getTipo(), request.getEmpresaParceira());
        } catch (Exception e) {
            throw new IllegalArgumentException("Erro ao calcular valor: " + e.getMessage(), e);
        }
    }

    /**
     * Cria um objeto Atendimento com os dados do request.
     *
     * @param request DTO com os dados do agendamento.
     * @param duracao Duração do atendimento.
     * @param valor Valor calculado do atendimento.
     * @return Objeto Atendimento configurado.
     */
    private Atendimento criarAtendimento(AgendamentoRequest request, int duracao, BigDecimal valor) {
        Atendimento atendimento = new Atendimento();
        atendimento.setPaciente(request.getPaciente());
        atendimento.setProfissional(request.getProfissional());
        atendimento.setEmpresaParceira(request.getEmpresaParceira());
        atendimento.setDataHora(java.sql.Timestamp.valueOf(request.getDataHora()));
        atendimento.setDuracaoMin(duracao);
        atendimento.setTipo(request.getTipo());
        atendimento.setSituacao(Atendimento.Situacao.AGENDADO);
        atendimento.setNotas(request.getObservacoes());
        atendimento.setValor(valor);
        atendimento.setStatusPagamento(request.getStatusPagamento() != null 
            ? request.getStatusPagamento() 
            : Atendimento.StatusPagamento.PENDENTE);
        atendimento.setUsuario(request.getUsuarioLogin());
        return atendimento;
    }

    /**
     * Atualiza os campos de um atendimento existente com os dados do request.
     *
     * @param atendimento Atendimento a ser atualizado.
     * @param request DTO com os novos dados do agendamento.
     * @param duracao Duração do atendimento.
     * @param valor Valor calculado do atendimento.
     */
    private void atualizarAtendimento(Atendimento atendimento, AgendamentoRequest request, int duracao, BigDecimal valor) {
        atendimento.setPaciente(request.getPaciente());
        atendimento.setProfissional(request.getProfissional());
        atendimento.setEmpresaParceira(request.getEmpresaParceira());
        atendimento.setDataHora(java.sql.Timestamp.valueOf(request.getDataHora()));
        atendimento.setDuracaoMin(duracao);
        atendimento.setTipo(request.getTipo());
        atendimento.setNotas(request.getObservacoes());
        atendimento.setValor(valor);
        atendimento.setStatusPagamento(request.getStatusPagamento() != null 
            ? request.getStatusPagamento() 
            : Atendimento.StatusPagamento.PENDENTE);
    }
}