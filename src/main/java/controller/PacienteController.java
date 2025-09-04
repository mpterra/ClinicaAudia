package controller;

import dao.PacienteDAO;
import dao.EnderecoDAO;
import model.Paciente;
import model.Endereco;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class PacienteController {

    private final PacienteDAO pacienteDAO;
    private final EnderecoDAO enderecoDAO;

    public PacienteController() {
        this.pacienteDAO = new PacienteDAO();
        this.enderecoDAO = new EnderecoDAO();
    }

    // -----------------------------
    // CRUD - Inserir paciente
    // -----------------------------
    public boolean salvarPaciente(Paciente paciente) throws SQLException {
        if (paciente == null) throw new IllegalArgumentException("Paciente inválido.");

        // 1️⃣ Salvar endereço primeiro se existir e ainda não tiver ID
        Endereco endereco = paciente.getEndereco();
        if (endereco != null && endereco.getId() <= 0) {
            if (endereco.getCep() == null || endereco.getCep().isBlank() ||
                endereco.getRua() == null || endereco.getRua().isBlank() ||
                endereco.getCidade() == null || endereco.getCidade().isBlank()) {
                throw new IllegalArgumentException("Endereço incompleto.");
            }
            enderecoDAO.insert(endereco); // ⚡ já seta o ID no objeto
        }

        // 2️⃣ Validar paciente
        validarPaciente(paciente);

        // 3️⃣ Salvar ou atualizar paciente
        if (paciente.getId() == 0) {
            pacienteDAO.insert(paciente);
        } else {
            pacienteDAO.update(paciente);
        }

        return true;
    }

    // -----------------------------
    // Deletar paciente
    // -----------------------------
    public void deletarPaciente(int id) throws SQLException {
        pacienteDAO.delete(id);
    }

    // -----------------------------
    // Buscar paciente por ID
    // -----------------------------
    public Paciente buscarPorId(int id) throws SQLException {
        Paciente paciente = pacienteDAO.findById(id);
        if (paciente != null && paciente.getEndereco() != null) {
            Endereco enderecoCompleto = enderecoDAO.findById(paciente.getEndereco().getId());
            paciente.setEndereco(enderecoCompleto);
        }
        return paciente;
    }

    // -----------------------------
    // Listar todos os pacientes
    // -----------------------------
    public List<Paciente> listarTodos() throws SQLException {
        List<Paciente> pacientes = pacienteDAO.findAll();
        for (Paciente p : pacientes) {
            if (p.getEndereco() != null) {
                p.setEndereco(enderecoDAO.findById(p.getEndereco().getId()));
            }
        }
        return pacientes;
    }

    // -----------------------------
    // Consultas específicas
    // -----------------------------
    public List<Paciente> buscarPorNome(String nome) throws SQLException {
        return pacienteDAO.findByNome(nome);
    }

    public Paciente buscarPorCPF(String cpf) throws SQLException {
        return pacienteDAO.findByCPF(cpf);
    }

    public List<Paciente> buscarPorTelefoneParcial(String telefoneParcial) throws SQLException {
        return pacienteDAO.findByTelefoneParcial(telefoneParcial);
    }

    public List<Paciente> buscarPorCidade(String cidade) throws SQLException {
        return pacienteDAO.findByCidade(cidade);
    }

    public List<Paciente> buscarPorFaixaIdade(int idadeMin, int idadeMax) throws SQLException {
        if (idadeMin < 0 || idadeMax < 0 || idadeMax < idadeMin) {
            throw new IllegalArgumentException("Faixa de idade inválida.");
        }
        return pacienteDAO.findByFaixaIdade(idadeMin, idadeMax);
    }

    // -----------------------------
    // Validações básicas
    // -----------------------------
    private void validarPaciente(Paciente paciente) {
        if (paciente.getNome() == null || paciente.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome do paciente é obrigatório.");
        }
        if (paciente.getCpf() == null || paciente.getCpf().isBlank()) {
            throw new IllegalArgumentException("CPF é obrigatório.");
        }
        if (paciente.getDataNascimento() == null || paciente.getDataNascimento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de nascimento inválida.");
        }
        // Endereço já foi salvo acima, então não precisa validar ID aqui
    }
}
