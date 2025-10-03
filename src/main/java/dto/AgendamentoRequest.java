package dto;

import model.Atendimento;
import model.EmpresaParceira;
import model.Paciente;
import model.Profissional;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO para encapsular os dados de requisição de agendamento vindos da interface do usuário.
 * Representa os dados necessários para criar um novo agendamento no sistema.
 */
public class AgendamentoRequest {
    private final Paciente paciente;
    private final Profissional profissional;
    private final EmpresaParceira empresaParceira;
    private final LocalDateTime dataHora;
    private final Atendimento.Tipo tipo;
    private final String observacoes;
    private final String usuarioLogin;
    private final Atendimento.StatusPagamento statusPagamento;

    /**
     * Construtor privado para uso com o Builder.
     */
    private AgendamentoRequest(Builder builder) {
        this.paciente = Objects.requireNonNull(builder.paciente, "Paciente não pode ser nulo");
        this.profissional = Objects.requireNonNull(builder.profissional, "Profissional não pode ser nulo");
        this.empresaParceira = builder.empresaParceira; // Pode ser null
        this.dataHora = Objects.requireNonNull(builder.dataHora, "Data e hora não podem ser nulos");
        this.tipo = Objects.requireNonNull(builder.tipo, "Tipo de atendimento não pode ser nulo");
        this.observacoes = builder.observacoes != null ? builder.observacoes : "";
        this.usuarioLogin = Objects.requireNonNull(builder.usuarioLogin, "Usuário logado não pode ser nulo");
        this.statusPagamento = builder.statusPagamento != null ? builder.statusPagamento : Atendimento.StatusPagamento.PENDENTE;
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

    public Atendimento.StatusPagamento getStatusPagamento() {
        return statusPagamento;
    }

    @Override
    public String toString() {
        return "AgendamentoRequest{" +
               "paciente=" + paciente.getNome() +
               ", profissional=" + profissional.getNome() +
               ", empresaParceira=" + (empresaParceira != null ? empresaParceira.getNome() : "Nenhuma") +
               ", dataHora=" + dataHora +
               ", tipo=" + tipo +
               ", observacoes='" + observacoes + '\'' +
               ", usuarioLogin='" + usuarioLogin + '\'' +
               ", statusPagamento=" + statusPagamento +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgendamentoRequest that = (AgendamentoRequest) o;
        return Objects.equals(paciente, that.paciente) &&
               Objects.equals(profissional, that.profissional) &&
               Objects.equals(empresaParceira, that.empresaParceira) &&
               Objects.equals(dataHora, that.dataHora) &&
               tipo == that.tipo &&
               Objects.equals(observacoes, that.observacoes) &&
               Objects.equals(usuarioLogin, that.usuarioLogin) &&
               statusPagamento == that.statusPagamento;
    }

    @Override
    public int hashCode() {
        return Objects.hash(paciente, profissional, empresaParceira, dataHora, tipo, observacoes, usuarioLogin, statusPagamento);
    }

    /**
     * Builder para facilitar a construção de objetos AgendamentoRequest.
     */
    public static class Builder {
        private Paciente paciente;
        private Profissional profissional;
        private EmpresaParceira empresaParceira;
        private LocalDateTime dataHora;
        private Atendimento.Tipo tipo;
        private String observacoes;
        private String usuarioLogin;
        private Atendimento.StatusPagamento statusPagamento;

        public Builder withPaciente(Paciente paciente) {
            this.paciente = paciente;
            return this;
        }

        public Builder withProfissional(Profissional profissional) {
            this.profissional = profissional;
            return this;
        }

        public Builder withEmpresaParceira(EmpresaParceira empresaParceira) {
            this.empresaParceira = empresaParceira;
            return this;
        }

        public Builder withDataHora(LocalDateTime dataHora) {
            this.dataHora = dataHora;
            return this;
        }

        public Builder withTipo(Atendimento.Tipo tipo) {
            this.tipo = tipo;
            return this;
        }

        public Builder withObservacoes(String observacoes) {
            this.observacoes = observacoes;
            return this;
        }

        public Builder withUsuarioLogin(String usuarioLogin) {
            this.usuarioLogin = usuarioLogin;
            return this;
        }

        public Builder withStatusPagamento(Atendimento.StatusPagamento statusPagamento) {
            this.statusPagamento = statusPagamento;
            return this;
        }

        public AgendamentoRequest build() {
            return new AgendamentoRequest(this);
        }
    }
}