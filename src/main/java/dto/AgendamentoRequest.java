// Pacote: src/main/java/dto/AgendamentoRequest.java
package dto;

import model.Atendimento;
import model.EmpresaParceira;
import model.Paciente;
import model.Profissional;

import java.time.LocalDateTime;

/**
 * DTO para encapsular os dados de requisição de agendamento vindos da UI.
 */
public class AgendamentoRequest {
    private Paciente paciente;
    private Profissional profissional;
    private EmpresaParceira empresaParceira;
    private LocalDateTime dataHora;
    private Atendimento.Tipo tipo;
    private String observacoes;
    private String usuarioLogin;

    // Construtor
    public AgendamentoRequest(Paciente paciente, Profissional profissional, EmpresaParceira empresaParceira,
                              LocalDateTime dataHora, Atendimento.Tipo tipo, String observacoes, String usuarioLogin) {
        this.paciente = paciente;
        this.profissional = profissional;
        this.empresaParceira = empresaParceira;
        this.dataHora = dataHora;
        this.tipo = tipo;
        this.observacoes = observacoes;
        this.usuarioLogin = usuarioLogin;
    }

    // Getters
    public Paciente getPaciente() {
        return paciente;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public EmpresaParceira getEmpresaParceira() {
        return empresaParceira;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public Atendimento.Tipo getTipo() {
        return tipo;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public String getUsuarioLogin() {
        return usuarioLogin;
    }
}