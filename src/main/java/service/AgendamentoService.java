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

/**
 * Serviço para orquestrar a criação e atualização de agendamentos, chamando validações e cálculos necessários.
 */
public class AgendamentoService {
    private final AtendimentoController atendimentoController;
    private final HorarioDisponivelValidator horarioValidator;
    private final ValorAtendimentoCalculator valorCalculator;
    private final ValidacaoGeralService validacaoService;

    // Construtor com injeção de dependências
    public AgendamentoService(AtendimentoController atendimentoController, HorarioDisponivelValidator horarioValidator,
                              ValorAtendimentoCalculator valorCalculator, ValidacaoGeralService validacaoService) {
        this.atendimentoController = atendimentoController;
        this.horarioValidator = horarioValidator;
        this.valorCalculator = valorCalculator;
        this.validacaoService = validacaoService;
    }

    /**
     * Cria um novo agendamento após validações e cálculos.
     * @param request DTO com dados do agendamento.
     * @return true se salvo com sucesso.
     * @throws Exception se houver erro de validação ou salvamento.
     */
    public boolean criarAgendamento(AgendamentoRequest request) throws Exception {
        validacaoService.validarCamposObrigatorios(request);
        validacaoService.validarDataFutura(request.getDataHora());

        Profissional prof = request.getProfissional();
        Atendimento.Tipo tipo = request.getTipo();
        EmpresaParceira empresa = request.getEmpresaParceira();

        // Verifica conflitos de horário
        List<Atendimento> atendimentos = atendimentoController.listarTodos().stream()
                .filter(a -> a.getProfissional().getId() == prof.getId()
                        && a.getDataHora().toLocalDateTime().toLocalDate().equals(request.getDataHora().toLocalDate())
                        && a.getSituacao() != Atendimento.Situacao.CANCELADO)
                .toList();
        int duracao = (tipo == Atendimento.Tipo.AVALIACAO) ? 90 : 60;
        horarioValidator.validarConflito(request.getDataHora(), duracao, atendimentos);

        // Calcula valor
        BigDecimal valor = valorCalculator.calcularValor(prof, tipo, empresa);

        // Cria o atendimento
        Atendimento at = new Atendimento();
        at.setPaciente(request.getPaciente());
        at.setProfissional(prof);
        at.setEmpresaParceira(empresa);
        at.setDataHora(java.sql.Timestamp.valueOf(request.getDataHora()));
        at.setDuracaoMin(duracao);
        at.setTipo(tipo);
        at.setSituacao(Atendimento.Situacao.AGENDADO);
        at.setUsuario(request.getUsuarioLogin());
        at.setNotas(request.getObservacoes());
        at.setValor(valor);

        return atendimentoController.criarAtendimento(at, request.getUsuarioLogin());
    }

    // Método para atualização (para uso no dialog de edição, se necessário)
    public void atualizarAgendamento(Atendimento atendimento, AgendamentoRequest request) throws SQLException {
        // Implementação similar à criação, ajustando para update
        // ... (adapte conforme necessário)
    }
}