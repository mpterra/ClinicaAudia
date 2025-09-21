package view.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import controller.AtendimentoController;
import controller.EscalaProfissionalController;
import controller.ProfissionalController;
import controller.PacienteController;
import exception.CampoObrigatorioException;
import model.Atendimento;
import model.EscalaProfissional;
import model.Paciente;
import model.Profissional;
import util.Sessao;

public class EditarMarcacaoDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private Atendimento atendimento;
    private final AtendimentoController atendimentoController = new AtendimentoController();
    private final ProfissionalController profissionalController = new ProfissionalController();
    private final EscalaProfissionalController escalaController = new EscalaProfissionalController();
    private final PacienteController pacienteController = new PacienteController();

    private JLabel lblNomePaciente;
    private JLabel lblTelefone;
    private JLabel lblIdade;
    private JLabel lblEmail;
    private JComboBox<Profissional> cbProfissional;
    private JComboBox<Atendimento.Tipo> cbTipo;
    private JComboBox<Atendimento.Situacao> cbSituacao;
    private JComboBox<LocalTime> cbHorario;
    private JTextPane txtObservacoes; // Changed to JTextPane for HTML support
    private JComboBox<String> cbData;
    private final DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Color primaryColor = new Color(30, 144, 255);
    private final Color backgroundColor = new Color(245, 245, 245);
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 14);
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 18);

    public EditarMarcacaoDialog(Frame parent, Atendimento atendimento) {
        super(parent, "Editar Atendimento", true);
        this.atendimento = atendimento;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(700, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        setBackground(backgroundColor);

        initComponents();

        try {
            carregarDadosIniciais();

            try {
                Atendimento full = atendimentoController.buscarPorId(this.atendimento.getId());
                if (full != null) this.atendimento = full;
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Aviso: falha ao recarregar atendimento: " + ex.getMessage(),
                        "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados iniciais: " + e.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }

        preencherCampos();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(backgroundColor);

        JLabel lblTitulo = new JLabel("Editar Atendimento", SwingConstants.CENTER);
        lblTitulo.setFont(titleFont);
        lblTitulo.setForeground(primaryColor);
        lblTitulo.setBorder(new EmptyBorder(0, 0, 20, 0));
        add(lblTitulo, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(backgroundColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Painel dados do paciente
        JPanel pacientePanel = new JPanel(new GridBagLayout());
        pacientePanel.setBackground(backgroundColor);
        pacientePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(primaryColor), "Dados do Paciente",
                        TitledBorder.CENTER, TitledBorder.TOP, labelFont, primaryColor),
                new EmptyBorder(15, 15, 15, 15)));
        GridBagConstraints gbcP = new GridBagConstraints();
        gbcP.insets = new Insets(5, 0, 5, 0);
        gbcP.anchor = GridBagConstraints.CENTER;

        lblNomePaciente = new JLabel();
        lblNomePaciente.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblNomePaciente.setHorizontalAlignment(SwingConstants.CENTER);
        gbcP.gridx = 0;
        gbcP.gridy = 0;
        pacientePanel.add(lblNomePaciente, gbcP);

        lblTelefone = new JLabel();
        lblTelefone.setFont(labelFont);
        lblTelefone.setHorizontalAlignment(SwingConstants.CENTER);
        gbcP.gridy = 1;
        pacientePanel.add(lblTelefone, gbcP);

        lblIdade = new JLabel();
        lblIdade.setFont(labelFont);
        lblIdade.setHorizontalAlignment(SwingConstants.CENTER);
        gbcP.gridy = 2;
        pacientePanel.add(lblIdade, gbcP);

        lblEmail = new JLabel();
        lblEmail.setFont(labelFont);
        lblEmail.setHorizontalAlignment(SwingConstants.CENTER);
        gbcP.gridy = 3;
        pacientePanel.add(lblEmail, gbcP);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        formPanel.add(pacientePanel, gbc);

        // Profissional
        JLabel lblProfissional = new JLabel("Profissional:");
        lblProfissional.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 15, 5, 5);
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(lblProfissional, gbc);

        cbProfissional = new JComboBox<>();
        cbProfissional.setPreferredSize(new Dimension(200, 25));
        cbProfissional.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Hand cursor for combo
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 15);
        formPanel.add(cbProfissional, gbc);

        // Linha dupla: Tipo | Situação
        JLabel lblTipo = new JLabel("Tipo:");
        lblTipo.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 15, 5, 5);
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(lblTipo, gbc);

        cbTipo = new JComboBox<>(Atendimento.Tipo.values());
        cbTipo.setPreferredSize(new Dimension(150, 25));
        cbTipo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Hand cursor for combo
        gbc.gridx = 1;
        gbc.insets = new Insets(5, 5, 5, 15);
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(cbTipo, gbc);

        JLabel lblSituacao = new JLabel("Situação:");
        lblSituacao.setFont(labelFont);
        gbc.gridx = 2;
        gbc.insets = new Insets(5, 15, 5, 5);
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(lblSituacao, gbc);

        cbSituacao = new JComboBox<>(Atendimento.Situacao.values());
        cbSituacao.setPreferredSize(new Dimension(150, 25));
        cbSituacao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Hand cursor for combo
        gbc.gridx = 3;
        gbc.insets = new Insets(5, 5, 5, 15);
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(cbSituacao, gbc);

        // Linha dupla: Data | Horário
        JLabel lblData = new JLabel("Data:");
        lblData.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = new Insets(5, 15, 5, 5);
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(lblData, gbc);

        cbData = new JComboBox<>();
        cbData.setPreferredSize(new Dimension(150, 25));
        cbData.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Hand cursor for combo
        gbc.gridx = 1;
        gbc.insets = new Insets(5, 5, 5, 15);
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(cbData, gbc);

        JLabel lblHorario = new JLabel("Horário:");
        lblHorario.setFont(labelFont);
        gbc.gridx = 2;
        gbc.insets = new Insets(5, 15, 5, 5);
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(lblHorario, gbc);

        cbHorario = new JComboBox<>();
        cbHorario.setPreferredSize(new Dimension(150, 25));
        cbHorario.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Hand cursor for combo
        gbc.gridx = 3;
        gbc.insets = new Insets(5, 5, 5, 15);
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(cbHorario, gbc);

        // Observações
        JLabel lblObservacoes = new JLabel("Observações:");
        lblObservacoes.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 1.0;
        formPanel.add(lblObservacoes, gbc);

        txtObservacoes = new JTextPane(); // Changed to JTextPane
        txtObservacoes.setContentType("text/html"); // Set content type to HTML
        txtObservacoes.setEditorKit(new HTMLEditorKit()); // Enable HTML rendering
        txtObservacoes.setText("<html></html>"); // Initialize with empty HTML
        JScrollPane scrollObservacoes = new JScrollPane(txtObservacoes);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(scrollObservacoes, gbc);

        // Botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(backgroundColor);

        JButton btnSalvar = new JButton("Salvar");
        btnSalvar.setBackground(primaryColor);
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setPreferredSize(new Dimension(100, 35));
        btnSalvar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Hand cursor for button

        JButton btnExcluir = new JButton("Excluir");
        btnExcluir.setBackground(new Color(255, 99, 71));
        btnExcluir.setForeground(Color.WHITE);
        btnExcluir.setPreferredSize(new Dimension(100, 35));
        btnExcluir.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Hand cursor for button

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setBackground(Color.LIGHT_GRAY);
        btnCancelar.setForeground(Color.BLACK);
        btnCancelar.setPreferredSize(new Dimension(100, 35));
        btnCancelar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Hand cursor for button

        buttonPanel.add(btnCancelar);
        buttonPanel.add(btnExcluir);
        buttonPanel.add(btnSalvar);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);

        btnSalvar.addActionListener(e -> salvar());
        btnExcluir.addActionListener(e -> excluir());
        btnCancelar.addActionListener(e -> dispose());

        cbProfissional.addActionListener(e -> atualizarHorarios());
        cbData.addActionListener(e -> atualizarHorarios());
    }

    private void preencherCampos() {
        try {
            Paciente paciente = atendimento.getPaciente();
            if (paciente != null && paciente.getId() > 0) {
                try {
                    paciente = pacienteController.buscarPorId(paciente.getId());
                } catch (SQLException ignored) {}
            }

            lblNomePaciente.setText("Nome: " + (paciente != null && paciente.getNome() != null ? paciente.getNome() : "Não informado"));
            lblTelefone.setText("Telefone: " + (paciente != null && paciente.getTelefone() != null ? paciente.getTelefone() : "Não informado"));
            long idade = paciente != null && paciente.getDataNascimento() != null
                    ? java.time.temporal.ChronoUnit.YEARS.between(paciente.getDataNascimento(), LocalDate.now())
                    : 0;
            lblIdade.setText("Idade: " + (idade > 0 ? idade : "Não informada"));
            lblEmail.setText("Email: " + (paciente != null && paciente.getEmail() != null ? paciente.getEmail() : "Não informado"));
        } catch (Exception e) {
            lblNomePaciente.setText("Nome: Não informado");
            lblTelefone.setText("Telefone: Não informado");
            lblIdade.setText("Idade: Não informada");
            lblEmail.setText("Email: Não informado");
        }

        if (atendimento.getProfissional() != null) {
            selectProfissionalById(atendimento.getProfissional().getId());
        } else {
            cbProfissional.setSelectedIndex(-1);
        }

        cbTipo.setSelectedItem(atendimento.getTipo());
        cbSituacao.setSelectedItem(atendimento.getSituacao());

        String dataStr = atendimento.getDataHora().toLocalDateTime().toLocalDate().format(formatoData);
        boolean found = false;
        for (int i = 0; i < cbData.getItemCount(); i++) {
            if (cbData.getItemAt(i).equals(dataStr)) { found = true; break; }
        }
        if (!found) cbData.insertItemAt(dataStr, 0);
        cbData.setSelectedItem(dataStr);

        atualizarHorarios();
        txtObservacoes.setText(atendimento.getNotas() != null ? "<html>" + atendimento.getNotas() + "</html>" : "<html></html>"); // Set HTML content
    }

    private void carregarDadosIniciais() throws SQLException {
        cbProfissional.removeAllItems();
        profissionalController.listarTodos().stream().filter(Profissional::isAtivo).forEach(cbProfissional::addItem);

        cbData.removeAllItems();
        LocalDate hoje = LocalDate.now();
        for (int i = 0; i < 30; i++) {
            LocalDate data = hoje.plusDays(i);
            cbData.addItem(data.format(formatoData));
        }

        if (atendimento != null && atendimento.getDataHora() != null) {
            String dataAt = atendimento.getDataHora().toLocalDateTime().toLocalDate().format(formatoData);
            boolean achou = false;
            for (int i = 0; i < cbData.getItemCount(); i++) {
                if (cbData.getItemAt(i).equals(dataAt)) { achou = true; break; }
            }
            if (!achou) cbData.insertItemAt(dataAt, 0);
            cbData.setSelectedItem(dataAt);
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
                            && a.getId() != atendimento.getId())
                    .map(a -> a.getDataHora().toLocalDateTime().toLocalTime())
                    .collect(Collectors.toList());

            for (EscalaProfissional e : escalas) {
                LocalTime hora = e.getHoraInicio().toLocalTime();
                LocalTime fim = e.getHoraFim().toLocalTime();
                while (!hora.isAfter(fim.minusMinutes(30))) {
                    if (!ocupados.contains(hora)) cbHorario.addItem(hora);
                    hora = hora.plusMinutes(30);
                }
            }

            LocalTime horarioOriginal = atendimento.getDataHora().toLocalDateTime().toLocalTime();
            if (cbHorario.getItemCount() > 0) {
                if (cbHorario.getItemAt(0).equals(horarioOriginal)) cbHorario.setSelectedItem(horarioOriginal);
                else cbHorario.setSelectedIndex(0);
                cbHorario.setEnabled(true);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar horários: " + ex.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void salvar() {
        try {
            Profissional prof = (Profissional) cbProfissional.getSelectedItem();
            Atendimento.Tipo tipo = (Atendimento.Tipo) cbTipo.getSelectedItem();
            Atendimento.Situacao situacao = (Atendimento.Situacao) cbSituacao.getSelectedItem();
            LocalTime hora = (LocalTime) cbHorario.getSelectedItem();
            String dataStr = (String) cbData.getSelectedItem();

            if (prof == null || tipo == null || situacao == null || hora == null || dataStr == null) {
                throw new CampoObrigatorioException("Preencha todos os campos!");
            }

            LocalDate data = LocalDate.parse(dataStr, formatoData);
            
            // Captura data/hora original para validação condicional em edições
            LocalDateTime originalDataHora = atendimento.getDataHora().toLocalDateTime();
            LocalDate originalData = originalDataHora.toLocalDate();
            LocalTime originalHora = originalDataHora.toLocalTime();
            
            // Validação só aplica se data/hora mudou; permite edições em atendimentos passados sem alterar data/hora
            boolean dataHoraMudou = !data.equals(originalData) || !hora.equals(originalHora);
            if (dataHoraMudou) {
                LocalDate hoje = LocalDate.now();
                LocalTime agora = LocalTime.now();
                if (data.isBefore(hoje) || (data.equals(hoje) && hora.isBefore(agora))) {
                    throw new CampoObrigatorioException("Não é possível agendar consultas em datas ou horários passados!");
                }
            }

            atendimento.setProfissional(prof);
            atendimento.setDataHora(Timestamp.valueOf(data.atTime(hora)));
            atendimento.setTipo(tipo);
            atendimento.setSituacao(situacao);
            atendimento.setNotas(txtObservacoes.getText().replaceAll("<html>|</html>", "")); // Strip HTML tags for saving

            if (atendimentoController.atualizarAtendimento(atendimento, Sessao.getUsuarioLogado().getLogin())) {
                JOptionPane.showMessageDialog(this, "Atendimento atualizado com sucesso!", "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluir() {
        int confirm = JOptionPane.showConfirmDialog(this, "Deseja realmente excluir este atendimento?", "Confirmação",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (atendimentoController.removerAtendimento(atendimento.getId())) {
                    JOptionPane.showMessageDialog(this, "Atendimento excluído com sucesso!", "Sucesso",
                            JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir atendimento: " + e.getMessage(), "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void selectProfissionalById(int profId) {
        for (int i = 0; i < cbProfissional.getItemCount(); i++) {
            Profissional p = cbProfissional.getItemAt(i);
            if (p != null && p.getId() == profId) {
                cbProfissional.setSelectedIndex(i);
                return;
            }
        }
        cbProfissional.setSelectedIndex(-1);
    }
}