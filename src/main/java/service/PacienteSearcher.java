// Pacote: src/main/java/service/PacienteSearcher.java
package service;

import controller.PacienteController;
import model.Paciente;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Serviço para buscar e formatar dados de pacientes.
 */
public class PacienteSearcher {
    private final PacienteController pacienteController;

    // Construtor
    public PacienteSearcher(PacienteController pacienteController) {
        this.pacienteController = pacienteController;
    }

    /**
     * Busca o paciente mais recente por nome parcial.
     * @param busca Texto de busca.
     * @return Paciente encontrado ou null.
     * @throws SQLException se houver erro ao listar.
     */
    public Paciente buscarPacientePorNome(String busca) throws SQLException {
        String buscaLower = busca.toLowerCase();
        Paciente ultimoPaciente = null;

        List<Paciente> pacientes = pacienteController.listarTodos();
        for (Paciente p : pacientes) {
            if (p.getNome().toLowerCase().contains(buscaLower)) {
                if (ultimoPaciente == null || p.getId() > ultimoPaciente.getId()) {
                    ultimoPaciente = p;
                }
            }
        }
        return ultimoPaciente;
    }

    /**
     * Calcula a idade do paciente.
     * @param paciente Paciente.
     * @return Idade em anos.
     */
    public long calcularIdade(Paciente paciente) {
        return paciente.getDataNascimento() != null
                ? ChronoUnit.YEARS.between(paciente.getDataNascimento(), LocalDate.now())
                : 0;
    }
}