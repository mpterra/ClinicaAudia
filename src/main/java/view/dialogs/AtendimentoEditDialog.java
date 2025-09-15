package view.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import controller.AtendimentoController;
import controller.EscalaProfissionalController;
import controller.PacienteController;
import controller.ProfissionalController;
import exception.CampoObrigatorioException;
import model.Atendimento;
import model.EscalaProfissional;
import model.Paciente;
import model.Profissional;
import util.Sessao;

public class AtendimentoEditDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final Atendimento atendimento;
    private final AtendimentoController atendimentoController = new AtendimentoController();
    private final PacienteController pacienteController = new PacienteController();
    private final ProfissionalController profissionalController = new ProfissionalController();
    private final EscalaProfissionalController escalaController = new EscalaProfissionalController();

    private JTextField txtBuscaPaciente;
    private JLabel lblNomePaciente;
    private JLabel lblTelefone;
    private JLabel lblIdade;
    private JLabel lblEmail;
    private JComboBox<Profissional> cbProfissional;
    private JComboBox<Atendimento.Tipo> cbTipo;
    private JComboBox<Atendimento.Situacao> cbSituacao;
    private JComboBox<LocalTime> cbHorario;
    private JTextArea txtObservacoes;
    private JComboBox<String> cbData;
    private final DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Color primaryColor = new Color(30, 144, 255);
    private final Color backgroundColor = new Color(245, 245, 245);
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 14);
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 18);

    public AtendimentoEditDialog(Frame parent, Atendimento atendimento) {
        super(parent, "Editar Atendimento", true);
        this.atendimento = atendimento;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        setBackground(backgroundColor);

        initComponents();
        preencherCampos();
        carregarDadosIniciais();
    }

    private void initComponents() {
        // Painel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(backgroundColor);

        // Título
        JLabel lblTitulo = new JLabel("Editar Atendimento", SwingConstants.CENTER);
        lblTitulo.setFont(titleFont);
        lblTitulo.setForeground(primaryColor);
        lblTitulo.setBorder(new EmptyBorder(0, 0, 10, 0));
        add(lblTitulo, BorderLayout.NORTH);

        // Formulário
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(backgroundColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Busca Paciente
        JLabel lblBuscaPaciente = new JLabel("Buscar Paciente:");
        lblBuscaPaciente.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(lblBuscaPaciente, gbc);

        txtBuscaPaciente = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        formPanel.add(txtBuscaPaciente, gbc);

        // Dados Paciente
        lblNomePaciente = new JLabel("Nome:");
        lblNomePaciente.setFont(new Font("SansSerif", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        formPanel.add(lblNomePaciente, gbc);

        lblTelefone = new JLabel("Telefone:");
        lblTelefone.setFont(labelFont);
        gbc.gridy = 2;
        formPanel.add(lblTelefone, gbc);

        lblIdade = new JLabel("Idade:");
        lblIdade.setFont(labelFont);
        gbc.gridy = 3;
        formPanel.add(lblIdade, gbc);

        lblEmail = new JLabel("Email:");
        lblEmail.setFont(labelFont);
        gbc.gridy = 4;
        formPanel.add(lblEmail, gbc);

        // Profissional, Tipo, Situação, Horário, Data
        JLabel lblProfissional = new JLabel("Profissional:");
        lblProfissional.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        formPanel.add(lblProfissional, gbc);

        cbProfissional = new JComboBox<>();
        cbProfissional.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        formPanel.add(cbProfissional, gbc);

        JLabel lblTipo = new JLabel("Tipo:");
        lblTipo.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        formPanel.add(lblTipo, gbc);

        cbTipo = new JComboBox<>(Atendimento.Tipo.values());
        cbTipo.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        formPanel.add(cbTipo, gbc);

        JLabel lblSituacao = new JLabel("Situação:");
        lblSituacao.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 1;
        formPanel.add(lblSituacao, gbc);

        cbSituacao = new JComboBox<>(Atendimento.Situacao.values());
        cbSituacao.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        formPanel.add(cbSituacao, gbc);

        JLabel lblData = new JLabel("Data:");
        lblData.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 1;
        formPanel.add(lblData, gbc);

        cbData = new JComboBox<>();
        cbData.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        formPanel.add(cbData, gbc);

        JLabel lblHorario = new JLabel("Horário:");
        lblHorario.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 1;
        formPanel.add(lblHorario, gbc);

        cbHorario = new JComboBox<>();
        cbHorario.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        formPanel.add(cbHorario, gbc);

        // Observações
        JLabel lblObservacoes = new JLabel("Observações:");
        lblObservacoes.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.gridwidth = 1;
        formPanel.add(lblObservacoes, gbc);

        txtObservacoes = new JTextArea(4, 20);
        txtObservacoes.setLineWrap(true);
        txtObservacoes.setWrapStyleWord(true);
        JScrollPane scrollObservacoes = new JScrollPane(txtObservacoes);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        formPanel.add(scrollObservacoes, gbc);

        // Botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(backgroundColor);

        JButton btnSalvar = new JButton("Salvar");
        btnSalvar.setBackground(primaryColor);
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setPreferredSize(new Dimension(100, 35));

        JButton btnExcluir = new JButton("Excluir");
        btnExcluir.setBackground(new Color(255, 99, 71));
        btnExcluir.setForeground(Color.WHITE);
        btnExcluir.setPreferredSize(new Dimension(100, 35));

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setBackground(Color.LIGHT_GRAY);
        btnCancelar.setForeground(Color.BLACK);
        btnCancelar.setPreferredSize(new Dimension(100, 35));

        buttonPanel.add(btnCancelar);
        buttonPanel.add(btnExcluir);
        buttonPanel.add(btnSalvar);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);

        // Ações dos botões
        btnSalvar.addActionListener(e -> salvar());
        btnExcluir.addActionListener(e -> excluir());
        btnCancelar.addActionListener(e -> dispose());

        // Listener para busca de paciente
        txtBuscaPaciente.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                try {
                    atualizarPaciente();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(AtendimentoEditDialog.this, "Erro ao buscar paciente: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                try {
                    atualizarPaciente();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(AtendimentoEditDialog.this, "Erro ao buscar paciente: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                try {
                    atualizarPaciente();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(AtendimentoEditDialog.this, "Erro ao buscar paciente: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Listener para mudança de profissional
        cbProfissional.addActionListener(e -> atualizarHorarios());
        cbData.addActionListener(e -> atualizarHorarios());
    }

    private void preencherCampos() {
        txtBuscaPaciente.setText(atendimento.getPaciente().getNome());
        try {
            atualizarPaciente(); // Chama a atualização do paciente ao abrir
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados do paciente: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
        lblNomePaciente.setText("Nome: " + atendimento.getPaciente().getNome());
        lblTelefone.setText("Telefone: " + (atendimento.getPaciente().getTelefone() != null ? atendimento.getPaciente().getTelefone() : "N/A"));
        long idade = atendimento.getPaciente().getDataNascimento() != null
                ? java.time.temporal.ChronoUnit.YEARS.between(atendimento.getPaciente().getDataNascimento(), LocalDate.now())
                : 0;
        lblIdade.setText("Idade: " + idade);
        lblEmail.setText("Email: " + (atendimento.getPaciente().getEmail() != null ? atendimento.getPaciente().getEmail() : "N/A"));
        cbProfissional.setSelectedItem(atendimento.getProfissional());
        cbTipo.setSelectedItem(atendimento.getTipo());
        cbSituacao.setSelectedItem(atendimento.getSituacao());
        String dataStr = atendimento.getDataHora().toLocalDateTime().toLocalDate().format(formatoData);
        cbData.addItem(dataStr);
        cbData.setSelectedItem(dataStr); // Define o item inicial corretamente
        cbHorario.addItem(atendimento.getDataHora().toLocalDateTime().toLocalTime());
        cbHorario.setSelectedItem(atendimento.getDataHora().toLocalDateTime().toLocalTime()); // Define o horário inicial
        txtObservacoes.setText(atendimento.getNotas());
    }

    private void carregarDadosIniciais() {
        try {
            // Carregar profissionais
            cbProfissional.removeAllItems();
            profissionalController.listarTodos().stream()
                    .filter(Profissional::isAtivo)
                    .forEach(cbProfissional::addItem);

            // Carregar datas próximas (hoje e futuro)
            cbData.removeAllItems(); // Limpa antes de preencher
            LocalDate hoje = LocalDate.now();
            for (int i = 0; i < 30; i++) {
                LocalDate data = hoje.plusDays(i);
                String dataFormatted = data.format(formatoData);
                cbData.addItem(dataFormatted);
            }
            cbData.setSelectedItem(atendimento.getDataHora().toLocalDateTime().toLocalDate().format(formatoData)); // Define a data inicial

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void atualizarPaciente() throws SQLException {
        String busca = txtBuscaPaciente.getText().toLowerCase();
        Paciente ultimoPaciente = null;

        for (Paciente p : pacienteController.listarTodos()) {
            if (p.getNome().toLowerCase().contains(busca)) {
                if (ultimoPaciente == null || p.getId() > ultimoPaciente.getId()) {
                    ultimoPaciente = p;
                }
            }
        }

        if (ultimoPaciente != null) {
            lblNomePaciente.setText("Nome: " + ultimoPaciente.getNome());
            lblTelefone.setText("Telefone: " + (ultimoPaciente.getTelefone() != null ? ultimoPaciente.getTelefone() : "N/A"));
            long idade = ultimoPaciente.getDataNascimento() != null
                    ? java.time.temporal.ChronoUnit.YEARS.between(ultimoPaciente.getDataNascimento(), LocalDate.now())
                    : 0;
            lblIdade.setText("Idade: " + idade);
            lblEmail.setText("Email: " + (ultimoPaciente.getEmail() != null ? ultimoPaciente.getEmail() : "N/A"));
        } else {
            lblNomePaciente.setText("Nome:");
            lblTelefone.setText("Telefone:");
            lblIdade.setText("Idade:");
            lblEmail.setText("Email:");
        }
    }

    private void atualizarHorarios() {
        cbHorario.removeAllItems();
        cbHorario.setEnabled(false);

        Profissional prof = (Profissional) cbProfissional.getSelectedItem();
        String dataStr = (String) cbData.getSelectedItem();
        if (prof == null || dataStr == null) return;

        try {
            LocalDate data = LocalDate.parse(dataStr, formatoData);
            int diaSemana = data.getDayOfWeek().getValue() - 1;

            List<EscalaProfissional> escalas = escalaController.listarTodas().stream()
                    .filter(e -> e.getProfissionalId() == prof.getId() && e.getDiaSemana() == diaSemana && e.isDisponivel())
                    .collect(Collectors.toList());

            List<LocalTime> ocupados = atendimentoController.listarTodos().stream()
                    .filter(a -> a.getProfissional().getId() == prof.getId()
                            && a.getDataHora().toLocalDateTime().toLocalDate().equals(data)
                            && a.getId() != atendimento.getId()) // Exclui o próprio atendimento
                    .map(a -> a.getDataHora().toLocalDateTime().toLocalTime())
                    .collect(Collectors.toList());

            for (EscalaProfissional e : escalas) {
                LocalTime hora = e.getHoraInicio().toLocalTime();
                LocalTime fim = e.getHoraFim().toLocalTime();
                while (!hora.isAfter(fim.minusMinutes(30))) {
                    if (!ocupados.contains(hora)) {
                        cbHorario.addItem(hora);
                    }
                    hora = hora.plusMinutes(30);
                }
            }
            // Tenta selecionar o horário original se ainda estiver disponível
            LocalTime horarioOriginal = atendimento.getDataHora().toLocalDateTime().toLocalTime();
            if (cbHorario.getItemCount() > 0) {
                cbHorario.setSelectedItem(horarioOriginal);
                cbHorario.setEnabled(true);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar horários: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void salvar() {
        try {
            String nomePaciente = lblNomePaciente.getText().replace("Nome: ", "").trim();
            if (nomePaciente.isEmpty()) {
                throw new CampoObrigatorioException("Selecione um paciente!");
            }

            Paciente p = pacienteController.listarTodos().stream()
                    .filter(pa -> pa.getNome().equals(nomePaciente))
                    .findFirst()
                    .orElseThrow(() -> new CampoObrigatorioException("Paciente não encontrado!"));

            Profissional prof = (Profissional) cbProfissional.getSelectedItem();
            Atendimento.Tipo tipo = (Atendimento.Tipo) cbTipo.getSelectedItem();
            Atendimento.Situacao situacao = (Atendimento.Situacao) cbSituacao.getSelectedItem();
            LocalTime hora = (LocalTime) cbHorario.getSelectedItem();
            String dataStr = (String) cbData.getSelectedItem();

            if (prof == null || tipo == null || situacao == null || hora == null || dataStr == null) {
                throw new CampoObrigatorioException("Preencha todos os campos!");
            }

            LocalDate data = LocalDate.parse(dataStr, formatoData);
            LocalDate hoje = LocalDate.now();
            LocalTime agora = LocalTime.now();
            if (data.isBefore(hoje) || (data.equals(hoje) && hora.isBefore(agora))) {
                throw new CampoObrigatorioException("Não é possível agendar consultas em datas ou horários passados!");
            }

            atendimento.setPaciente(p);
            atendimento.setProfissional(prof);
            atendimento.setDataHora(Timestamp.valueOf(data.atTime(hora)));
            atendimento.setTipo(tipo);
            atendimento.setSituacao(situacao);
            atendimento.setNotas(txtObservacoes.getText());
            atendimento.setUsuario(Sessao.getUsuarioLogado().getLogin());

            if (atendimentoController.atualizarAtendimento(atendimento, Sessao.getUsuarioLogado().getLogin())) {
                JOptionPane.showMessageDialog(this, "Atendimento atualizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluir() {
        int confirm = JOptionPane.showConfirmDialog(this, "Deseja realmente excluir este atendimento?", "Confirmação", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (atendimentoController.removerAtendimento(atendimento.getId())) {
                    JOptionPane.showMessageDialog(this, "Atendimento excluído com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir atendimento: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}