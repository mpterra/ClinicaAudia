package view.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

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
    private final ProfissionalController profissionalController = new ProfissionalController();
    private final EscalaProfissionalController escalaController = new EscalaProfissionalController();

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
        setSize(700, 600); // Aumentado para 700x600
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
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(backgroundColor);

        // Título
        JLabel lblTitulo = new JLabel("Editar Atendimento", SwingConstants.CENTER);
        lblTitulo.setFont(titleFont);
        lblTitulo.setForeground(primaryColor);
        lblTitulo.setBorder(new EmptyBorder(0, 0, 15, 0));
        add(lblTitulo, BorderLayout.NORTH);

        // Formulário
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(backgroundColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;

        // Dados do Paciente (estáticos e centralizados)
        JPanel pacientePanel = new JPanel(new GridBagLayout());
        pacientePanel.setBackground(backgroundColor);
        pacientePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(primaryColor), "Dados do Paciente",
                        TitledBorder.CENTER, TitledBorder.TOP, labelFont, primaryColor),
                new EmptyBorder(10, 10, 10, 10)));
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
        gbc.gridwidth = 3;
        formPanel.add(pacientePanel, gbc);

        // Profissional, Tipo, Situação, Horário, Data
        JLabel lblProfissional = new JLabel("Profissional:");
        lblProfissional.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(lblProfissional, gbc);

        cbProfissional = new JComboBox<>();
        cbProfissional.setPreferredSize(new Dimension(250, 30));
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        formPanel.add(cbProfissional, gbc);

        JLabel lblTipo = new JLabel("Tipo:");
        lblTipo.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        formPanel.add(lblTipo, gbc);

        cbTipo = new JComboBox<>(Atendimento.Tipo.values());
        cbTipo.setPreferredSize(new Dimension(250, 30));
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        formPanel.add(cbTipo, gbc);

        JLabel lblSituacao = new JLabel("Situação:");
        lblSituacao.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        formPanel.add(lblSituacao, gbc);

        cbSituacao = new JComboBox<>(Atendimento.Situacao.values());
        cbSituacao.setPreferredSize(new Dimension(250, 30));
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        formPanel.add(cbSituacao, gbc);

        JLabel lblData = new JLabel("Data:");
        lblData.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        formPanel.add(lblData, gbc);

        cbData = new JComboBox<>();
        cbData.setPreferredSize(new Dimension(250, 30));
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        formPanel.add(cbData, gbc);

        JLabel lblHorario = new JLabel("Horário:");
        lblHorario.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        formPanel.add(lblHorario, gbc);

        cbHorario = new JComboBox<>();
        cbHorario.setPreferredSize(new Dimension(250, 30));
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        formPanel.add(cbHorario, gbc);

        // Observações (maior)
        JLabel lblObservacoes = new JLabel("Observações:");
        lblObservacoes.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        formPanel.add(lblObservacoes, gbc);

        txtObservacoes = new JTextArea(6, 25); // Aumentado para 6 linhas e 25 colunas
        txtObservacoes.setLineWrap(true);
        txtObservacoes.setWrapStyleWord(true);
        JScrollPane scrollObservacoes = new JScrollPane(txtObservacoes);
        scrollObservacoes.setPreferredSize(new Dimension(400, 150)); // Aumentado o tamanho
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

        // Listener para mudança de profissional
        cbProfissional.addActionListener(e -> atualizarHorarios());
        cbData.addActionListener(e -> atualizarHorarios());
    }

    private void preencherCampos() {
        Paciente paciente = atendimento.getPaciente();
        lblNomePaciente.setText("Nome: " + paciente.getNome());
        lblTelefone.setText("Telefone: " + (paciente.getTelefone() != null ? paciente.getTelefone() : "N/A"));
        long idade = paciente.getDataNascimento() != null
                ? java.time.temporal.ChronoUnit.YEARS.between(paciente.getDataNascimento(), LocalDate.now())
                : 0;
        lblIdade.setText("Idade: " + idade);
        lblEmail.setText("Email: " + (paciente.getEmail() != null ? paciente.getEmail() : "N/A"));
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