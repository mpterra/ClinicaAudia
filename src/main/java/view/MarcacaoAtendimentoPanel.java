package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import controller.AtendimentoController;
import controller.EscalaProfissionalController;
import controller.PacienteController;
import controller.ProfissionalController;
import controller.ValorAtendimentoController;
import exception.CampoObrigatorioException;
import model.Atendimento;
import model.EscalaProfissional;
import model.Paciente;
import model.Profissional;
import model.ValorAtendimento;
import util.Sessao;
import view.dialogs.EditarMarcacaoDialog;

public class MarcacaoAtendimentoPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private JTextField txtBuscaPaciente;
    private JLabel lblNomePaciente;
    private JLabel lblTelefone;
    private JLabel lblIdade;
    private JLabel lblEmail;
    private JTextArea txtObservacoes;
    private JComboBox<Profissional> cbProfissional;
    private JComboBox<Atendimento.Tipo> cbTipo;
    private JComboBox<LocalTime> cbHorario;
    private JTable tabelaAtendimentos;
    private DefaultTableModel modeloTabela;
    private TableRowSorter<DefaultTableModel> sorter;

    private LocalDate dataSelecionada;
    private JPanel painelDias;
    private JComboBox<String> cbMes;
    private JComboBox<Integer> cbAno;

    private JTextField txtBuscaProfissional;
    
    private DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final String[] meses = { "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto",
            "Setembro", "Outubro", "Novembro", "Dezembro" };

    private final Color primaryColor = new Color(30, 144, 255);
    private final Color backgroundColor = new Color(245, 245, 245);
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 18);
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 14);

    private final AtendimentoController atendimentoController = new AtendimentoController();
    private final PacienteController pacienteController = new PacienteController();
    private final ProfissionalController profissionalController = new ProfissionalController();
    private final EscalaProfissionalController escalaController = new EscalaProfissionalController();
    private final ValorAtendimentoController valorAtendimentoController = new ValorAtendimentoController();

    public MarcacaoAtendimentoPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(5, 15, 15, 15));
        setBackground(backgroundColor);

        JLabel lblTitulo = new JLabel("Marcação de Atendimento", SwingConstants.CENTER);
        lblTitulo.setFont(titleFont);
        lblTitulo.setForeground(primaryColor);
        lblTitulo.setBorder(new EmptyBorder(5, 0, 10, 0));
        add(lblTitulo, BorderLayout.NORTH);

        JPanel painelFormulario = criarPainelFormulario();
        JPanel painelTabela = criarPainelTabela();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, painelFormulario, painelTabela);
        splitPane.setResizeWeight(0.495);
        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.495));
        splitPane.setDividerSize(7);
        splitPane.setBackground(backgroundColor);

        add(splitPane, BorderLayout.CENTER);

        dataSelecionada = LocalDate.now();
        atualizarCalendario();
        carregarAtendimentos();
        carregarDadosIniciais();
    }

    // Cria o painel de formulário para agendamento
    private JPanel criarPainelFormulario() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(primaryColor, 1, true),
                        "Agendar Consulta", TitledBorder.LEFT, TitledBorder.TOP, labelFont, primaryColor),
                new EmptyBorder(10, 10, 10, 10)));
        panel.setBackground(backgroundColor);

        JPanel mainGrid = new JPanel(new GridBagLayout());
        mainGrid.setBackground(backgroundColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;

        // Row 1: Busca Paciente
        JLabel lblBuscaPaciente = new JLabel("Buscar Paciente:");
        lblBuscaPaciente.setFont(labelFont);
        txtBuscaPaciente = new JTextField(20);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        mainGrid.add(lblBuscaPaciente, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        mainGrid.add(txtBuscaPaciente, gbc);

        // Row 2: Dados Paciente e Observações
        JPanel dataSection = new JPanel(new GridBagLayout());
        dataSection.setBackground(backgroundColor);
        GridBagConstraints gbcS = new GridBagConstraints();
        gbcS.insets = new Insets(10, 10, 10, 10);
        gbcS.fill = GridBagConstraints.BOTH;
        gbcS.weightx = 0.5;
        gbcS.weighty = 1.0;

        // Seção Esquerda: Dados do Paciente
        JPanel pacientePanel = new JPanel(new GridBagLayout());
        pacientePanel.setBorder(new EmptyBorder(0, 0, 0, 5));
        pacientePanel.setBackground(backgroundColor);
        GridBagConstraints gbcP = new GridBagConstraints();
        gbcP.insets = new Insets(5, 0, 5, 0);
        gbcP.anchor = GridBagConstraints.WEST;
        gbcP.fill = GridBagConstraints.HORIZONTAL;

        lblNomePaciente = new JLabel("Nome:");
        lblNomePaciente.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblNomePaciente.setPreferredSize(new Dimension(300, 20));
        lblNomePaciente.setHorizontalAlignment(SwingConstants.LEFT);
        gbcP.gridx = 0;
        gbcP.gridy = 0;
        pacientePanel.add(lblNomePaciente, gbcP);

        lblTelefone = new JLabel("Telefone:");
        lblTelefone.setFont(labelFont);
        lblTelefone.setPreferredSize(new Dimension(300, 20));
        lblTelefone.setHorizontalAlignment(SwingConstants.LEFT);
        gbcP.gridy = 1;
        pacientePanel.add(lblTelefone, gbcP);

        lblIdade = new JLabel("Idade:");
        lblIdade.setFont(labelFont);
        lblIdade.setPreferredSize(new Dimension(300, 20));
        lblIdade.setHorizontalAlignment(SwingConstants.LEFT);
        gbcP.gridy = 2;
        pacientePanel.add(lblIdade, gbcP);

        lblEmail = new JLabel("Email:");
        lblEmail.setFont(labelFont);
        lblEmail.setPreferredSize(new Dimension(300, 20));
        lblEmail.setHorizontalAlignment(SwingConstants.LEFT);
        gbcP.gridy = 3;
        pacientePanel.add(lblEmail, gbcP);

        gbcS.gridx = 0;
        gbcS.gridy = 0;
        dataSection.add(pacientePanel, gbcS);

        // Seção Direita: Observações
        JPanel observacaoPanel = new JPanel(new BorderLayout());
        observacaoPanel.setBorder(new EmptyBorder(0, 5, 0, 0));
        observacaoPanel.setBackground(backgroundColor);

        JLabel lblObservacoes = new JLabel("Observações:");
        lblObservacoes.setFont(labelFont);
        lblObservacoes.setBorder(new EmptyBorder(0, 0, 5, 0));
        observacaoPanel.add(lblObservacoes, BorderLayout.NORTH);

        txtObservacoes = new JTextArea(6, 20);
        txtObservacoes.setLineWrap(true);
        txtObservacoes.setWrapStyleWord(true);
        JScrollPane scrollObservacoes = new JScrollPane(txtObservacoes);
        scrollObservacoes.setMinimumSize(new Dimension(300, 150));
        observacaoPanel.add(scrollObservacoes, BorderLayout.CENTER);

        gbcS.gridx = 1;
        gbcS.gridy = 0;
        dataSection.add(observacaoPanel, gbcS);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weighty = 0.3;
        mainGrid.add(dataSection, gbc);

        // Row 3: Seleções
        JPanel selecaoPanel = new JPanel(new GridBagLayout());
        selecaoPanel.setBackground(backgroundColor);
        GridBagConstraints gbcSel = new GridBagConstraints();
        gbcSel.insets = new Insets(5, 5, 5, 5);
        gbcSel.fill = GridBagConstraints.HORIZONTAL;
        gbcSel.weightx = 1.0;

        JLabel lblProf = new JLabel("Profissional:");
        lblProf.setFont(labelFont);
        gbcSel.gridx = 0;
        gbcSel.gridy = 0;
        gbcSel.weightx = 0.0;
        selecaoPanel.add(lblProf, gbcSel);
        cbProfissional = new JComboBox<>();
        cbProfissional.setPreferredSize(new Dimension(200, 30));
        cbProfissional.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbcSel.gridx = 1;
        gbcSel.weightx = 1.0;
        selecaoPanel.add(cbProfissional, gbcSel);

        JLabel lblTipo = new JLabel("Tipo:");
        lblTipo.setFont(labelFont);
        gbcSel.gridx = 2;
        gbcSel.weightx = 0.0;
        selecaoPanel.add(lblTipo, gbcSel);
        cbTipo = new JComboBox<>(Atendimento.Tipo.values());
        cbTipo.setPreferredSize(new Dimension(200, 30));
        cbTipo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbcSel.gridx = 3;
        gbcSel.weightx = 1.0;
        selecaoPanel.add(cbTipo, gbcSel);

        JLabel lblHorario = new JLabel("Horário:");
        lblHorario.setFont(labelFont);
        gbcSel.gridx = 4;
        gbcSel.weightx = 0.0;
        selecaoPanel.add(lblHorario, gbcSel);
        cbHorario = new JComboBox<>();
        cbHorario.setPreferredSize(new Dimension(200, 30));
        cbHorario.setEnabled(false);
        cbHorario.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbcSel.gridx = 5;
        gbcSel.weightx = 1.0;
        selecaoPanel.add(cbHorario, gbcSel);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.weighty = 0.0;
        mainGrid.add(selecaoPanel, gbc);

        // Row 4: Calendário
        JPanel panelDataHora = new JPanel(new BorderLayout(10, 10));
        panelDataHora.setBackground(backgroundColor);
        panelDataHora.add(criarMiniCalendario(), BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.weighty = 0.5;
        mainGrid.add(panelDataHora, gbc);

        // Row 5: Botões
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panelBotoes.setBackground(backgroundColor);
        JButton btnSalvar = new JButton("Salvar");
        btnSalvar.setBackground(primaryColor);
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setPreferredSize(new Dimension(100, 35));
        btnSalvar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JButton btnLimpar = new JButton("Limpar");
        btnLimpar.setBackground(Color.LIGHT_GRAY);
        btnLimpar.setForeground(Color.BLACK);
        btnLimpar.setPreferredSize(new Dimension(100, 35));
        btnLimpar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panelBotoes.add(btnLimpar);
        panelBotoes.add(btnSalvar);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        mainGrid.add(panelBotoes, gbc);

        btnSalvar.addActionListener(e -> salvarAtendimento());
        btnLimpar.addActionListener(e -> {
            try {
                limparCampos();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        });

        panel.add(mainGrid, BorderLayout.CENTER);

        cbProfissional.addActionListener(e -> atualizarHorarios());
        cbTipo.addActionListener(e -> atualizarHorarios());
        txtBuscaPaciente.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                try {
                    atualizarPaciente();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }

            public void removeUpdate(DocumentEvent e) {
                try {
                    atualizarPaciente();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }

            public void changedUpdate(DocumentEvent e) {
                try {
                    atualizarPaciente();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        });

        return panel;
    }

    // Atualiza os dados do paciente com base na busca
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
                    ? ChronoUnit.YEARS.between(ultimoPaciente.getDataNascimento(), LocalDate.now())
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

    // Cria o mini calendário para seleção de data
    private JPanel criarMiniCalendario() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new EmptyBorder(10, 0, 10, 0));
        panel.setBackground(backgroundColor);
        painelDias = new JPanel(new GridLayout(0, 7, 5, 5));
        painelDias.setBackground(backgroundColor);
        JPanel topo = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        topo.setBackground(backgroundColor);

        cbMes = new JComboBox<>(meses);
        cbMes.setPreferredSize(new Dimension(120, 30));
        cbMes.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cbAno = new JComboBox<>();
        cbAno.setPreferredSize(new Dimension(80, 30));
        cbAno.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        int anoAtual = LocalDate.now().getYear();
        for (int i = anoAtual - 5; i <= anoAtual + 5; i++)
            cbAno.addItem(i);
        cbMes.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        cbAno.setSelectedItem(anoAtual);

        JButton btnPrev = new JButton("<");
        btnPrev.setPreferredSize(new Dimension(40, 30));
        btnPrev.setBackground(Color.WHITE);
        btnPrev.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JButton btnNext = new JButton(">");
        btnNext.setPreferredSize(new Dimension(40, 30));
        btnNext.setBackground(Color.WHITE);
        btnNext.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnPrev.addActionListener(e -> {
            dataSelecionada = dataSelecionada.minusMonths(1);
            cbMes.setSelectedIndex(dataSelecionada.getMonthValue() - 1);
            cbAno.setSelectedItem(dataSelecionada.getYear());
            atualizarCalendario();
        });
        btnNext.addActionListener(e -> {
            dataSelecionada = dataSelecionada.plusMonths(1);
            cbMes.setSelectedIndex(dataSelecionada.getMonthValue() - 1);
            cbAno.setSelectedItem(dataSelecionada.getYear());
            atualizarCalendario();
        });
        cbMes.addActionListener(e -> atualizarCalendario());
        cbAno.addActionListener(e -> atualizarCalendario());

        topo.add(btnPrev);
        topo.add(cbMes);
        topo.add(cbAno);
        topo.add(btnNext);
        panel.add(topo, BorderLayout.NORTH);
        panel.add(painelDias, BorderLayout.CENTER);

        return panel;
    }

    // Atualiza o calendário com os dias do mês selecionado
    private void atualizarCalendario() {
        painelDias.removeAll();
        int mes = cbMes.getSelectedIndex() + 1;
        int ano = (Integer) cbAno.getSelectedItem();
        YearMonth ym = YearMonth.of(ano, mes);

        String[] diasSemana = { "Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb" };
        for (String d : diasSemana) {
            JLabel lbl = new JLabel(d, SwingConstants.CENTER);
            lbl.setFont(labelFont);
            lbl.setForeground(Color.GRAY);
            painelDias.add(lbl);
        }

        LocalDate primeira = ym.atDay(1);
        int diaSemanaInicio = primeira.getDayOfWeek().getValue() % 7;
        for (int i = 0; i < diaSemanaInicio; i++)
            painelDias.add(new JLabel(""));

        int diasNoMes = ym.lengthOfMonth();
        LocalDate hoje = LocalDate.now();
        for (int dia = 1; dia <= diasNoMes; dia++) {
            LocalDate d = ym.atDay(dia);
            JButton btn = new JButton(String.valueOf(dia));
            btn.setPreferredSize(new Dimension(40, 50));
            btn.setFont(labelFont);
            btn.setBorder(BorderFactory.createEmptyBorder());
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            if (d.equals(dataSelecionada)) {
                btn.setBackground(new Color(255, 204, 153));
                btn.setForeground(Color.BLACK);
                btn.setBorder(BorderFactory.createLineBorder(primaryColor, 2));
            } else if (d.isBefore(hoje)) {
                btn.setBackground(new Color(220, 220, 220));
                btn.setForeground(Color.BLACK);
            } else if (d.equals(hoje)) {
                btn.setBackground(new Color(144, 238, 144));
                btn.setForeground(Color.BLACK);
            } else {
                btn.setBackground(new Color(173, 216, 230));
                btn.setForeground(Color.BLACK);
            }

            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!d.equals(dataSelecionada)) {
                        btn.setBackground(primaryColor);
                        btn.setForeground(Color.WHITE);
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (d.equals(dataSelecionada)) {
                        btn.setBackground(new Color(255, 204, 153));
                        btn.setForeground(Color.BLACK);
                        btn.setBorder(BorderFactory.createLineBorder(primaryColor, 2));
                    } else if (d.isBefore(hoje)) {
                        btn.setBackground(new Color(220, 220, 220));
                        btn.setForeground(Color.BLACK);
                    } else if (d.equals(hoje)) {
                        btn.setBackground(new Color(144, 238, 144));
                        btn.setForeground(Color.BLACK);
                    } else {
                        btn.setBackground(new Color(173, 216, 230));
                        btn.setForeground(Color.BLACK);
                    }
                }
            });

            btn.addActionListener(e -> {
                dataSelecionada = d;
                atualizarHorarios();
                atualizarCalendario();
            });
            painelDias.add(btn);
        }
        painelDias.revalidate();
        painelDias.repaint();
    }

    // Cria o painel da tabela de atendimentos
    private JPanel criarPainelTabela() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(primaryColor, 1, true), "Próximos Atendimentos",
                        TitledBorder.LEFT, TitledBorder.TOP, labelFont, primaryColor),
                new EmptyBorder(10, 10, 10, 10)));
        panel.setBackground(backgroundColor);

        JPanel panelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelBusca.setBackground(backgroundColor);
        JLabel lblBuscaPacienteTabela = new JLabel("Buscar Paciente:");
        lblBuscaPacienteTabela.setFont(labelFont);
        JTextField txtBuscaPacienteTabela = new JTextField(15);
        JLabel lblBuscaProf = new JLabel("Buscar Profissional:");
        lblBuscaProf.setFont(labelFont);
        txtBuscaProfissional = new JTextField(15);
        panelBusca.add(lblBuscaPacienteTabela);
        panelBusca.add(txtBuscaPacienteTabela);
        panelBusca.add(lblBuscaProf);
        panelBusca.add(txtBuscaProfissional);
        panel.add(panelBusca, BorderLayout.NORTH);

        String[] colunas = { "Data", "Horário", "Paciente", "Profissional", "Tipo", "Situação" };
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tabelaAtendimentos = new JTable(modeloTabela) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                Color bgColor;

                try {
                    // Obtém data, horário e situação
                    String dataStr = (String) getValueAt(row, 0);
                    LocalDate dataAtendimento = LocalDate.parse(dataStr, formatoData);
                    LocalTime horaAtendimento = (LocalTime) getValueAt(row, 1);
                    LocalDateTime dtAtendimento = dataAtendimento.atTime(horaAtendimento);
                    LocalDateTime agora = LocalDateTime.now();
                    boolean isPast = dtAtendimento.isBefore(agora);
                    Atendimento.Situacao situacao = (Atendimento.Situacao) getValueAt(row, 5);

                    // Aplica cor cinza para AGENDADO passado; alterna para AGENDADO hoje/futuro; prioriza cores de outras situações
                    if (situacao == Atendimento.Situacao.AGENDADO) {
                        if (isPast) {
                            bgColor = new Color(220, 220, 220); // Cinza para AGENDADO passado
                        } else {
                            // Alterna entre azul claro e branco para AGENDADO hoje/futuro
                            bgColor = (row % 2 == 0) ? Color.decode("#AED6F1") : Color.WHITE;
                        }
                    } else {
                        // Cores específicas para outros status
                        switch (situacao) {
                            case REALIZADO -> bgColor = new Color(144, 238, 144); // Verde claro
                            case FALTOU -> bgColor = new Color(255, 255, 153); // Amarelo claro
                            case CANCELADO -> bgColor = new Color(255, 182, 193); // Rosa claro
                            default -> bgColor = backgroundColor; // Fallback
                        }
                    }
                } catch (Exception e) {
                    bgColor = backgroundColor; // Fallback
                }

                c.setBackground(bgColor);
                c.setForeground(Color.BLACK);

                // Aplica borda preta na linha selecionada
                if (isRowSelected(row)) {
                    int lastColumn = getColumnCount() - 1;
                    if (column == 0) {
                        ((JComponent) c).setBorder(BorderFactory.createMatteBorder(1, 1, 1, 0, Color.BLACK));
                    } else if (column == lastColumn) {
                        ((JComponent) c).setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, Color.BLACK));
                    } else {
                        ((JComponent) c).setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.BLACK));
                    }
                } else {
                    ((JComponent) c).setBorder(BorderFactory.createEmptyBorder());
                }

                return c;
            }
        };

        // Configurações da tabela
        tabelaAtendimentos.setShowGrid(false);
        tabelaAtendimentos.setIntercellSpacing(new Dimension(0, 0));
        tabelaAtendimentos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabelaAtendimentos.getSelectedRow() != -1) {
                    int row = tabelaAtendimentos.convertRowIndexToModel(tabelaAtendimentos.getSelectedRow());
                    String dataStr = (String) modeloTabela.getValueAt(row, 0);
                    LocalTime horario = (LocalTime) modeloTabela.getValueAt(row, 1);
                    String pacienteNome = (String) modeloTabela.getValueAt(row, 2);
                    try {
                        LocalDate data = LocalDate.parse(dataStr, formatoData);
                        Atendimento atendimento = atendimentoController.listarTodos().stream()
                                .filter(a -> a.getPaciente().getNome().equals(pacienteNome)
                                        && a.getDataHora().toLocalDateTime().toLocalDate().equals(data)
                                        && a.getDataHora().toLocalDateTime().toLocalTime().equals(horario))
                                .findFirst()
                                .orElse(null);
                        if (atendimento != null) {
                            EditarMarcacaoDialog dialog = new EditarMarcacaoDialog((Frame) SwingUtilities.getWindowAncestor(MarcacaoAtendimentoPanel.this), atendimento);
                            dialog.setVisible(true);
                            carregarAtendimentos();
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(MarcacaoAtendimentoPanel.this, "Erro ao abrir atendimento: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        tabelaAtendimentos.setFillsViewportHeight(true);
        tabelaAtendimentos.setRowHeight(25);
        tabelaAtendimentos.setFont(labelFont);
        tabelaAtendimentos.setBackground(backgroundColor);

        JTableHeader header = tabelaAtendimentos.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.setBackground(primaryColor);
        header.setForeground(Color.WHITE);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tabelaAtendimentos.getColumnCount(); i++) {
            tabelaAtendimentos.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        sorter = new TableRowSorter<>(modeloTabela);
        tabelaAtendimentos.setRowSorter(sorter);

        JScrollPane scroll = new JScrollPane(tabelaAtendimentos);
        scroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(scroll, BorderLayout.CENTER);

        txtBuscaPacienteTabela.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filtrar(txtBuscaPacienteTabela.getText(), txtBuscaProfissional.getText());
            }

            public void removeUpdate(DocumentEvent e) {
                filtrar(txtBuscaPacienteTabela.getText(), txtBuscaProfissional.getText());
            }

            public void changedUpdate(DocumentEvent e) {
                filtrar(txtBuscaPacienteTabela.getText(), txtBuscaProfissional.getText());
            }
        });

        txtBuscaProfissional.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filtrar(txtBuscaPacienteTabela.getText(), txtBuscaProfissional.getText());
            }

            public void removeUpdate(DocumentEvent e) {
                filtrar(txtBuscaPacienteTabela.getText(), txtBuscaProfissional.getText());
            }

            public void changedUpdate(DocumentEvent e) {
                filtrar(txtBuscaPacienteTabela.getText(), txtBuscaProfissional.getText());
            }
        });

        return panel;
    }

    // Filtra a tabela por paciente e profissional
    private void filtrar(String paciente, String prof) {
        RowFilter<DefaultTableModel, Object> filterPaciente = RowFilter.regexFilter("(?i)" + paciente, 2);
        RowFilter<DefaultTableModel, Object> filterProf = RowFilter.regexFilter("(?i)" + prof, 3);
        sorter.setRowFilter(RowFilter.andFilter(List.of(filterPaciente, filterProf)));
    }

    // Carrega dados iniciais como profissionais
    private void carregarDadosIniciais() {
        try {
            cbProfissional.removeAllItems();
            List<Profissional> profissionais = profissionalController.listarTodos().stream()
                    .filter(Profissional::isAtivo)
                    .collect(Collectors.toList());
            profissionais.forEach(cbProfissional::addItem);
            if (!profissionais.isEmpty()) {
                cbProfissional.setSelectedIndex(0);
            }
            atualizarPaciente();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados: " + e.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Atualiza os horários disponíveis, evitando sobreposições (exceto cancelados)
    private void atualizarHorarios() {
        cbHorario.removeAllItems();
        cbHorario.setEnabled(false);

        Profissional prof = (Profissional) cbProfissional.getSelectedItem();
        Atendimento.Tipo tipo = (Atendimento.Tipo) cbTipo.getSelectedItem();
        if (prof == null || dataSelecionada == null || tipo == null)
            return;

        int duration = (tipo == Atendimento.Tipo.AVALIACAO) ? 90 : 60;

        try {
            int diaSemana = dataSelecionada.getDayOfWeek().getValue() - 1;

            List<EscalaProfissional> escalas = escalaController.listarTodas().stream().filter(
                    e -> e.getProfissionalId() == prof.getId() && e.getDiaSemana() == diaSemana && e.isDisponivel())
                    .collect(Collectors.toList());

            // Intervalos ocupados (excluindo cancelados), usando duracaoMin armazenada
            List<Atendimento> atendimentos = atendimentoController.listarTodos().stream()
                    .filter(a -> a.getProfissional().getId() == prof.getId()
                            && a.getDataHora().toLocalDateTime().toLocalDate().equals(dataSelecionada)
                            && a.getSituacao() != Atendimento.Situacao.CANCELADO)
                    .collect(Collectors.toList());

            List<Intervalo> ocupados = new ArrayList<>();
            for (Atendimento a : atendimentos) {
                LocalTime inicio = a.getDataHora().toLocalDateTime().toLocalTime();
                LocalTime fim = inicio.plusMinutes(a.getDuracaoMin());
                ocupados.add(new Intervalo(inicio, fim));
            }

            // Gera horários possíveis sem sobreposição
            for (EscalaProfissional e : escalas) {
                LocalTime hora = e.getHoraInicio().toLocalTime();
                LocalTime fimEscala = e.getHoraFim().toLocalTime();

                while (true) {
                    LocalTime fimProposto = hora.plusMinutes(duration);
                    if (fimProposto.isAfter(fimEscala)) {
                        break;
                    }

                    boolean sobreposto = false;
                    for (Intervalo occ : ocupados) {
                        if (!(fimProposto.compareTo(occ.inicio) <= 0 || hora.compareTo(occ.fim) >= 0)) {
                            sobreposto = true;
                            break;
                        }
                    }

                    if (!sobreposto) {
                        cbHorario.addItem(hora);
                    }

                    hora = hora.plusMinutes(30);
                }
            }
            cbHorario.setEnabled(cbHorario.getItemCount() > 0);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar horários: " + ex.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Classe auxiliar para intervalos de tempo
    private static class Intervalo {
        LocalTime inicio;
        LocalTime fim;

        Intervalo(LocalTime inicio, LocalTime fim) {
            this.inicio = inicio;
            this.fim = fim;
        }
    }

    // Carrega os atendimentos na tabela, filtrando conforme regras
    private void carregarAtendimentos() {
        modeloTabela.setRowCount(0);
        try {
            List<Atendimento> atendimentos = atendimentoController.listarTodos();
            LocalDate hoje = LocalDate.now();

            for (Atendimento a : atendimentos) {
                LocalDate dataAtendimento = a.getDataHora().toLocalDateTime().toLocalDate();
                Atendimento.Situacao situacao = a.getSituacao();

                // Regra 1: Atendimentos de hoje ou futuros (exceto CANCELADO)
                // Regra 2: Atendimentos passados apenas se AGENDADO
                // Regra 3: Exclui qualquer atendimento CANCELADO
                if (situacao != Atendimento.Situacao.CANCELADO) {
                    if (!dataAtendimento.isBefore(hoje) || // Hoje ou futuro
                        (dataAtendimento.isBefore(hoje) && situacao == Atendimento.Situacao.AGENDADO)) { // Passado e AGENDADO
                        modeloTabela.addRow(new Object[] {
                                dataAtendimento.format(formatoData),
                                a.getDataHora().toLocalDateTime().toLocalTime(),
                                a.getPacienteNome(),
                                a.getProfissional().getNome(),
                                a.getTipo(),
                                situacao
                        });
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar atendimentos: " + e.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Salva o atendimento, validando campos e regras
    private void salvarAtendimento() {
        try {
            String nomePaciente = lblNomePaciente.getText().replace("Nome: ", "").trim();
            if (nomePaciente.isEmpty())
                throw new CampoObrigatorioException("Selecione um paciente!");

            Paciente p = pacienteController.listarTodos().stream()
                    .filter(pa -> pa.getNome().equals(nomePaciente))
                    .findFirst()
                    .orElseThrow(() -> new CampoObrigatorioException("Paciente não encontrado!"));

            Profissional prof = (Profissional) cbProfissional.getSelectedItem();
            LocalTime hora = (LocalTime) cbHorario.getSelectedItem();
            Atendimento.Tipo tipo = (Atendimento.Tipo) cbTipo.getSelectedItem();

            if (prof == null || hora == null || dataSelecionada == null || tipo == null)
                throw new CampoObrigatorioException("Preencha todos os campos!");

            LocalDate hoje = LocalDate.now();
            LocalTime agora = LocalTime.now();
            if (dataSelecionada.isBefore(hoje) || (dataSelecionada.equals(hoje) && hora.isBefore(agora))) {
                throw new CampoObrigatorioException("Não é possível agendar consultas em datas ou horários passados!");
            }

            // Busca o valor do atendimento
            ValorAtendimento valorAtendimento = valorAtendimentoController.buscarPorProfissionalETipo(prof.getId(), tipo);
            if (valorAtendimento == null) {
                throw new Exception("Nenhum valor cadastrado para o profissional e tipo de atendimento selecionados!");
            }
            BigDecimal valor = valorAtendimento.getValor();

            // Define a duração com base no tipo de atendimento
            int duration = (tipo == Atendimento.Tipo.AVALIACAO) ? 90 : 60;

            // Verificação de sobreposição no frontend
            List<Atendimento> atendimentos = atendimentoController.listarTodos().stream()
                    .filter(a -> a.getProfissional().getId() == prof.getId()
                            && a.getDataHora().toLocalDateTime().toLocalDate().equals(dataSelecionada)
                            && a.getSituacao() != Atendimento.Situacao.CANCELADO)
                    .collect(Collectors.toList());

            LocalDateTime inicioProposto = dataSelecionada.atTime(hora);
            LocalDateTime fimProposto = inicioProposto.plusMinutes(duration);
            for (Atendimento a : atendimentos) {
                LocalDateTime inicioExist = a.getDataHora().toLocalDateTime();
                LocalDateTime fimExist = inicioExist.plusMinutes(a.getDuracaoMin());
                if (!(fimProposto.compareTo(inicioExist) <= 0 || inicioProposto.compareTo(fimExist) >= 0)) {
                    throw new Exception("Horário Indisponível para o profissional");
                }
            }

            // Cria o atendimento
            Atendimento at = new Atendimento();
            at.setPaciente(p);
            at.setProfissional(prof);
            at.setDataHora(java.sql.Timestamp.valueOf(inicioProposto));
            at.setDuracaoMin(duration);
            at.setTipo(tipo);
            at.setSituacao(Atendimento.Situacao.AGENDADO);
            at.setUsuario(Sessao.getUsuarioLogado().getLogin());
            at.setNotas(txtObservacoes.getText());
            at.setValor(valor); // Define o valor do atendimento

            // Chama o controller para salvar
            if (atendimentoController.criarAtendimento(at, Sessao.getUsuarioLogado().getLogin())) {
                JOptionPane.showMessageDialog(this, "Atendimento salvo com sucesso!", "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
                carregarAtendimentos();
                limparCampos();
                if (cbProfissional.getItemCount() > 0) {
                    cbProfissional.setSelectedIndex(0);
                }
            } else {
                throw new Exception("Falha ao salvar o atendimento no banco de dados");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Limpa os campos do formulário
    private void limparCampos() throws SQLException {
        txtBuscaPaciente.setText("");
        cbTipo.setSelectedIndex(0);
        cbHorario.removeAllItems();
        txtObservacoes.setText("");
        atualizarPaciente();
        if (cbProfissional.getItemCount() > 0) {
            cbProfissional.setSelectedIndex(0);
        }
    }
}