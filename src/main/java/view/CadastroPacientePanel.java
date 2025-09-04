package view;

import controller.PacienteController;
import controller.EnderecoController;
import model.Paciente;
import model.Endereco;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CadastroPacientePanel extends JPanel {

    private JTextField txtNome, txtCpf, txtTelefone, txtEmail;
    private JTextField txtLogradouro, txtNumero, txtComplemento, txtBairro, txtCidade, txtEstado, txtCep;
    private JButton btnSalvar, btnCancelar;

    private PacienteController pacienteController;
    private EnderecoController enderecoController;

    private String usuarioLogado; // <- usuário da sessão

    public CadastroPacientePanel(String usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
        this.pacienteController = new PacienteController();
        this.enderecoController = new EnderecoController();

        setLayout(new BorderLayout(10, 10));

        // ==========================
        // PAINEL PRINCIPAL
        // ==========================
        JPanel painelPrincipal = new JPanel();
        painelPrincipal.setLayout(new BoxLayout(painelPrincipal, BoxLayout.Y_AXIS));
        add(painelPrincipal, BorderLayout.CENTER);

        // ==========================
        // PAINEL PACIENTE
        // ==========================
        JPanel painelPaciente = new JPanel(new GridBagLayout());
        painelPaciente.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                "Dados do Paciente",
                TitledBorder.LEFT,
                TitledBorder.TOP
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Nome
        gbc.gridx = 0; gbc.gridy = 0;
        painelPaciente.add(new JLabel("Nome:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 3;
        txtNome = new JTextField(20);
        painelPaciente.add(txtNome, gbc);

        // CPF
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        painelPaciente.add(new JLabel("CPF:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        txtCpf = new JTextField(15);
        painelPaciente.add(txtCpf, gbc);

        // Telefone
        gbc.gridx = 2; gbc.gridy = 1;
        painelPaciente.add(new JLabel("Telefone:"), gbc);
        gbc.gridx = 3; gbc.gridy = 1;
        txtTelefone = new JTextField(15);
        painelPaciente.add(txtTelefone, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 2;
        painelPaciente.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 3;
        txtEmail = new JTextField(20);
        painelPaciente.add(txtEmail, gbc);

        painelPrincipal.add(painelPaciente);

        // ==========================
        // PAINEL ENDEREÇO
        // ==========================
        JPanel painelEndereco = new JPanel(new GridBagLayout());
        painelEndereco.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                "Endereço",
                TitledBorder.LEFT,
                TitledBorder.TOP
        ));
        gbc.gridwidth = 1;

        // Logradouro
        gbc.gridx = 0; gbc.gridy = 0;
        painelEndereco.add(new JLabel("Logradouro:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 3;
        txtLogradouro = new JTextField(20);
        painelEndereco.add(txtLogradouro, gbc);

        // Número
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        painelEndereco.add(new JLabel("Número:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        txtNumero = new JTextField(5);
        painelEndereco.add(txtNumero, gbc);

        // Complemento
        gbc.gridx = 2; gbc.gridy = 1;
        painelEndereco.add(new JLabel("Complemento:"), gbc);
        gbc.gridx = 3; gbc.gridy = 1;
        txtComplemento = new JTextField(10);
        painelEndereco.add(txtComplemento, gbc);

        // Bairro
        gbc.gridx = 0; gbc.gridy = 2;
        painelEndereco.add(new JLabel("Bairro:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        txtBairro = new JTextField(15);
        painelEndereco.add(txtBairro, gbc);

        // Cidade
        gbc.gridx = 2; gbc.gridy = 2;
        painelEndereco.add(new JLabel("Cidade:"), gbc);
        gbc.gridx = 3; gbc.gridy = 2;
        txtCidade = new JTextField(15);
        painelEndereco.add(txtCidade, gbc);

        // Estado
        gbc.gridx = 0; gbc.gridy = 3;
        painelEndereco.add(new JLabel("Estado:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        txtEstado = new JTextField(5);
        painelEndereco.add(txtEstado, gbc);

        // CEP
        gbc.gridx = 2; gbc.gridy = 3;
        painelEndereco.add(new JLabel("CEP:"), gbc);
        gbc.gridx = 3; gbc.gridy = 3;
        txtCep = new JTextField(10);
        painelEndereco.add(txtCep, gbc);

        painelPrincipal.add(painelEndereco);

        // ==========================
        // BOTÕES
        // ==========================
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSalvar = new JButton("Salvar");
        btnCancelar = new JButton("Cancelar");
        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnCancelar);
        add(painelBotoes, BorderLayout.SOUTH);

        // ==========================
        // AÇÕES
        // ==========================
        btnSalvar.addActionListener(e -> salvarPaciente());
        btnCancelar.addActionListener(e -> limparCampos());
    }

    private void salvarPaciente() {
        try {
            String nome = txtNome.getText().trim();
            String cpf = txtCpf.getText().trim();
            String telefone = txtTelefone.getText().trim();
            String email = txtEmail.getText().trim();
            LocalDate dataNascimento = LocalDate.now(); // aqui você pode ajustar para pegar de um JDatePicker se quiser

            // Criar objeto Endereco
            Endereco endereco = new Endereco();
            endereco.setRua(txtLogradouro.getText().trim());
            endereco.setNumero(txtNumero.getText().trim());
            endereco.setComplemento(txtComplemento.getText().trim());
            endereco.setBairro(txtBairro.getText().trim());
            endereco.setCidade(txtCidade.getText().trim());
            endereco.setEstado(txtEstado.getText().trim());
            endereco.setCep(txtCep.getText().trim());

            // Salvar Endereço primeiro
            enderecoController.adicionarEndereco(endereco);

            // Criar Paciente, agora passando o usuário logado
            Paciente paciente = new Paciente(
                    0, nome, cpf, telefone, email, dataNascimento, endereco, usuarioLogado
            );

            pacienteController.salvarPaciente(paciente);

            JOptionPane.showMessageDialog(this, "Paciente salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            limparCampos();

        } catch (IllegalArgumentException | SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao salvar paciente: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limparCampos() {
        txtNome.setText("");
        txtCpf.setText("");
        txtTelefone.setText("");
        txtEmail.setText("");
        txtLogradouro.setText("");
        txtNumero.setText("");
        txtComplemento.setText("");
        txtBairro.setText("");
        txtCidade.setText("");
        txtEstado.setText("");
        txtCep.setText("");
    }
}
