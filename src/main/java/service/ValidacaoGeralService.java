// Pacote: src/main/java/service/ValidacaoGeralService.java
package service;

import dto.AgendamentoRequest;
import exception.CampoObrigatorioException;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Serviço utilitário para validações gerais.
 */
public class ValidacaoGeralService {

    /**
     * Valida campos obrigatórios do request.
     * @param request DTO de agendamento.
     * @throws CampoObrigatorioException se faltar algo.
     */
    public static void validarCamposObrigatorios(AgendamentoRequest request) throws CampoObrigatorioException {
        if (request.getPaciente() == null) {
            throw new CampoObrigatorioException("Selecione um paciente!");
        }
        if (request.getProfissional() == null || request.getDataHora() == null || request.getTipo() == null) {
            throw new CampoObrigatorioException("Preencha todos os campos!");
        }
        if (request.getEmpresaParceira() != null && request.getEmpresaParceira().getId() <= 0) {
            throw new CampoObrigatorioException("Empresa parceira selecionada é inválida!");
        }
    }

    /**
     * Valida se a data/hora é futura.
     * @param dataHora Data e hora proposta.
     * @throws CampoObrigatorioException se for passada.
     */
    public static void validarDataFutura(LocalDateTime dataHora) throws CampoObrigatorioException {
        LocalDateTime agora = LocalDateTime.now();
        if (dataHora.isBefore(agora)) {
            throw new CampoObrigatorioException("Não é possível agendar consultas em datas ou horários passados!");
        }
    }
}