package view;

import controller.AtendimentoController;
import controller.EmpresaParceiraController;
import controller.EscalaProfissionalController;
import controller.PacienteController;
import controller.ProfissionalController;
import controller.ValorAtendimentoController;
import controller.ValorAtendimentoEmpresaController;
import dto.AgendamentoRequest;
import model.Atendimento;
import model.EmpresaParceira;
import model.Paciente;
import model.Profissional;
import service.AgendamentoService;
import service.AtendimentoFilter;
import service.HorarioDisponivelValidator;
import service.PacienteSearcher;
import service.ValidacaoGeralService;
import service.ValorAtendimentoCalculator;
import util.Sessao;
import view.dialogs.EditarMarcacaoDialog;
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
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Painel para marcação de atendimentos, com abas para agendamento e tabela.
 */
public class MarcacaoAtendimentoPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private JTextField txtBuscaPaciente;
    private JLabel lblNomePaciente;
    private JLabel lblTelefone;
    private JLabel lblIdade;
    private JLabel lblEmail;
    private JLabel lblEndereco;
    private JTextArea txtObservacoes;
    private JComboBox<Profissional> cbProfissional;
    private JComboBox<Atendimento.Tipo> cbTipo;
    private JComboBox<LocalTime> cbHorario;
    private JComboBox<EmpresaParceira> cbEmpresaParceira;
    private JTable tabelaAtendimentos;
    private DefaultTableModel modeloTabela;
    private TableRowSorter<DefaultTableModel> sorter;
    private LocalDate dataSelecionada;
    private JPanel painelDias;
    private JComboBox<String> cbMes;
    private JComboBox<Integer> cbAno;
    private JTextField txtBuscaProfissional;
    private final DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final String[] meses = { "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto",
            "Setembro", "Outubro", "Novembro", "Dezembro" };
    private final Color primaryColor = new Color(30, 144, 255);
    private final Color backgroundColor = new Color(245, 245, 245);
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 18);
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 14);
    private final Font tabFont = new Font("SansSerif", Font.BOLD, 16); // Fonte maior para abas
    // Injeção de dependências: controllers e serviços
    private final AtendimentoController atendimentoController = new AtendimentoController();
    private final PacienteController pacienteController = new PacienteController();
    private final ProfissionalController profissionalController = new ProfissionalController();
    private final EscalaProfissionalController escalaController = new EscalaProfissionalController();
    private final ValorAtendimentoController valorAtendimentoController = new ValorAtendimentoController();
    private final EmpresaParceiraController empresaParceiraController = new EmpresaParceiraController();
    private final ValorAtendimentoEmpresaController valorAtendimentoEmpresaController = new ValorAtendimentoEmpresaController();
    private final PacienteSearcher pacienteSearcher = new PacienteSearcher(pacienteController);
    private final HorarioDisponivelValidator horarioValidator = new HorarioDisponivelValidator(escalaController, atendimentoController);
    private final ValorAtendimentoCalculator valorCalculator = new ValorAtendimentoCalculator(valorAtendimentoController, valorAtendimentoEmpresaController);
    private final AgendamentoService agendamentoService = new AgendamentoService(atendimentoController, horarioValidator, valorCalculator, new ValidacaoGeralService());
    private final AtendimentoFilter atendimentoFilter = new AtendimentoFilter(atendimentoController);

    /**
     * Construtor do painel, inicializa a interface gráfica com abas.
     */
    public MarcacaoAtendimentoPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(5, 15, 15, 15));
        setBackground(backgroundColor);
        // Título do painel
        JLabel lblTitulo = new JLabel("Marcação de Atendimento", SwingConstants.CENTER);
        lblTitulo.setFont(titleFont);
        lblTitulo.setForeground(primaryColor);
        lblTitulo.setBorder(new EmptyBorder(5, 0, 10, 0));
        add(lblTitulo, BorderLayout.NORTH);
        // Abas
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(backgroundColor);
        tabbedPane.setFont(tabFont); // Define fonte maior e negrito para abas
        tabbedPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Cursor de mão para abas
        tabbedPane.addTab("Agendamento", criarPainelFormulario());
        tabbedPane.addTab("Atendimentos", criarPainelTabela());
        add(tabbedPane, BorderLayout.CENTER);
        // Inicializa data selecionada e carrega dados
        dataSelecionada = LocalDate.now();
        atualizarCalendario();
        carregarAtendimentos();
        carregarDadosIniciais();
    }

    /**
     * Cria o painel de formulário para agendamento.
     * @return JPanel com os componentes do formulário.
     */
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
        // Row 2: Dados Paciente
        JPanel pacientePanel = new JPanel(new GridBagLayout());
        pacientePanel.setBackground(backgroundColor);
        GridBagConstraints gbcP = new GridBagConstraints();
        gbcP.insets = new Insets(5, 0, 5, 0);
        gbcP.anchor = GridBagConstraints.WEST;
        gbcP.fill = GridBagConstraints.HORIZONTAL;
        lblNomePaciente = new JLabel("Nome:");
        lblNomePaciente.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblNomePaciente.setPreferredSize(new Dimension(400, 20));
        lblNomePaciente.setHorizontalAlignment(SwingConstants.LEFT);
        gbcP.gridx = 0;
        gbcP.gridy = 0;
        pacientePanel.add(lblNomePaciente, gbcP);
        lblTelefone = new JLabel("Telefone:");
        lblTelefone.setFont(labelFont);
        lblTelefone.setPreferredSize(new Dimension(400, 20));
        lblTelefone.setHorizontalAlignment(SwingConstants.LEFT);
        gbcP.gridy = 1;
        pacientePanel.add(lblTelefone, gbcP);
        lblIdade = new JLabel("Idade:");
        lblIdade.setFont(labelFont);
        lblIdade.setPreferredSize(new Dimension(400, 20));
        lblIdade.setHorizontalAlignment(SwingConstants.LEFT);
        gbcP.gridy = 2;
        pacientePanel.add(lblIdade, gbcP);
        lblEmail = new JLabel("Email:");
        lblEmail.setFont(labelFont);
        lblEmail.setPreferredSize(new Dimension(400, 20));
        lblEmail.setHorizontalAlignment(SwingConstants.LEFT);
        gbcP.gridy = 3;
        pacientePanel.add(lblEmail, gbcP);
        lblEndereco = new JLabel("Endereço:");
        lblEndereco.setFont(labelFont);
        lblEndereco.setPreferredSize(new Dimension(400, 20));
        lblEndereco.setHorizontalAlignment(SwingConstants.LEFT);
        gbcP.gridy = 4;
        pacientePanel.add(lblEndereco, gbcP);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weighty = 0.3;
        mainGrid.add(pacientePanel, gbc);
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
        cbProfissional.setPreferredSize(new Dimension(250, 30));
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
        cbTipo.setPreferredSize(new Dimension(250, 30));
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
        cbHorario.setPreferredSize(new Dimension(250, 30));
        cbHorario.setEnabled(false);
        cbHorario.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbcSel.gridx = 5;
        gbcSel.weightx = 1.0;
        selecaoPanel.add(cbHorario, gbcSel);
        JLabel lblEmpresa = new JLabel("Parceria:");
        lblEmpresa.setFont(labelFont);
        gbcSel.gridx = 6;
        gbcSel.weightx = 0.0;
        selecaoPanel.add(lblEmpresa, gbcSel);
        cbEmpresaParceira = new JComboBox<>();
        cbEmpresaParceira.setPreferredSize(new Dimension(250, 30));
        cbEmpresaParceira.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cbEmpresaParceira.addItem(null);
        gbcSel.gridx = 7;
        gbcSel.weightx = 1.0;
        selecaoPanel.add(cbEmpresaParceira, gbcSel);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.weighty = 0.0;
        mainGrid.add(selecaoPanel, gbc);
        // Row 4: Calendário e Observações
        JPanel panelDataObservacao = new JPanel(new GridBagLayout());
        panelDataObservacao.setBackground(backgroundColor);
        GridBagConstraints gbcDO = new GridBagConstraints();
        gbcDO.insets = new Insets(10, 10, 10, 10);
        gbcDO.fill = GridBagConstraints.BOTH;
        gbcDO.weightx = 0.5;
        gbcDO.weighty = 1.0;
        // Calendário
        JPanel panelDataHora = criarMiniCalendario();
        gbcDO.gridx = 0;
        gbcDO.gridy = 0;
        panelDataObservacao.add(panelDataHora, gbcDO);
        // Observações
        JPanel observacaoPanel = new JPanel(new BorderLayout());
        observacaoPanel.setBackground(backgroundColor);
        JLabel lblObservacoes = new JLabel("Observações:");
        lblObservacoes.setFont(labelFont);
        lblObservacoes.setBorder(new EmptyBorder(0, 0, 5, 0));
        observacaoPanel.add(lblObservacoes, BorderLayout.NORTH);
        txtObservacoes = new JTextArea(6, 20);
        txtObservacoes.setLineWrap(true);
        txtObservacoes.setWrapStyleWord(true);
        JScrollPane scrollObservacoes = new JScrollPane(txtObservacoes);
        scrollObservacoes.setMinimumSize(new Dimension(400, 100));
        observacaoPanel.add(scrollObservacoes, BorderLayout.CENTER);
        gbcDO.gridx = 1;
        gbcDO.gridy = 0;
        panelDataObservacao.add(observacaoPanel, gbcDO);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.weighty = 0.5;
        mainGrid.add(panelDataObservacao, gbc);
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
        // Listeners
        btnSalvar.addActionListener(e -> salvarAtendimento());
        btnLimpar.addActionListener(e -> {
            try {
                limparCampos();
            } catch (SQLException e1) {
                JOptionPane.showMessageDialog(this, "Erro ao limpar campos: " + e1.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        cbProfissional.addActionListener(e -> atualizarHorarios());
        cbTipo.addActionListener(e -> atualizarHorarios());
        txtBuscaPaciente.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                try {
                    atualizarPaciente();
                } catch (SQLException e1) {
                    JOptionPane.showMessageDialog(MarcacaoAtendimentoPanel.this, "Erro ao atualizar paciente: " + e1.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
            public void removeUpdate(DocumentEvent e) {
                try {
                    atualizarPaciente();
                } catch (SQLException e1) {
                    JOptionPane.showMessageDialog(MarcacaoAtendimentoPanel.this, "Erro ao atualizar paciente: " + e1.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
            public void changedUpdate(DocumentEvent e) {
                try {
                    atualizarPaciente();
                } catch (SQLException e1) {
                    JOptionPane.showMessageDialog(MarcacaoAtendimentoPanel.this, "Erro ao atualizar paciente: " + e1.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        panel.add(mainGrid, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Cria o mini calendário para seleção de data.
     * @return JPanel com o calendário.
     */
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

    /**
     * Atualiza o calendário com os dias do mês selecionado.
     */
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
            btn.setPreferredSize(new Dimension(35, 35));
            btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
            btn.setBorder(BorderFactory.createEmptyBorder());
            btn.setFocusPainted(false);
            boolean isDomingo = d.getDayOfWeek().getValue() == 7;
            if (isDomingo) {
                btn.setEnabled(false); // Desativa botões de domingo
                btn.setBackground(Color.LIGHT_GRAY);
                btn.setForeground(Color.BLACK);
            } else {
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
            }
            painelDias.add(btn);
        }
        painelDias.revalidate();
        painelDias.repaint();
    }

    /**
     * Cria o painel da tabela de atendimentos.
     * @return JPanel com a tabela.
     */
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
        String[] colunas = { "Data", "Horário", "Paciente", "Profissional", "Parceria", "Tipo", "Situação", "Pagamento" };
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
                    String dataStr = (String) getValueAt(row, 0);
                    LocalDate dataAtendimento = LocalDate.parse(dataStr, formatoData);
                    LocalTime horaAtendimento = (LocalTime) getValueAt(row, 1);
                    LocalDateTime dtAtendimento = dataAtendimento.atTime(horaAtendimento);
                    LocalDateTime agora = LocalDateTime.now();
                    boolean isPast = dtAtendimento.isBefore(agora);
                    Atendimento.Situacao situacao = (Atendimento.Situacao) getValueAt(row, 6);
                    Atendimento.StatusPagamento statusPagamento = (Atendimento.StatusPagamento) getValueAt(row, 7);
                    if (column == 7) {
                        if (statusPagamento == Atendimento.StatusPagamento.PAGO) {
                            bgColor = new Color(144, 238, 144);
                        } else {
                            if (situacao == Atendimento.Situacao.AGENDADO) {
                                if (isPast) {
                                    bgColor = new Color(220, 220, 220);
                                } else {
                                    bgColor = (row % 2 == 0) ? Color.decode("#AED6F1") : Color.WHITE;
                                }
                            } else {
                                switch (situacao) {
                                    case REALIZADO -> bgColor = new Color(144, 238, 144);
                                    case FALTOU -> bgColor = new Color(255, 255, 153);
                                    case CANCELADO -> bgColor = new Color(255, 182, 193);
                                    default -> bgColor = backgroundColor;
                                }
                            }
                        }
                    } else {
                        if (situacao == Atendimento.Situacao.AGENDADO) {
                            if (isPast) {
                                bgColor = new Color(220, 220, 220);
                            } else {
                                bgColor = (row % 2 == 0) ? Color.decode("#AED6F1") : Color.WHITE;
                            }
                        } else {
                            switch (situacao) {
                                case REALIZADO -> bgColor = new Color(144, 238, 144);
                                case FALTOU -> bgColor = new Color(255, 255, 153);
                                case CANCELADO -> bgColor = new Color(255, 182, 193);
                                default -> bgColor = backgroundColor;
                            }
                        }
                    }
                } catch (Exception e) {
                    bgColor = backgroundColor;
                }
                c.setBackground(bgColor);
                c.setForeground(Color.BLACK);
                if (isRowSelected(row)) {
                    int lastColumn = getColumnCount() - 1;
                    if (column == 0) {
                        ((JComponent) c).setBorder(BorderFactory.createMatteBorder(1, 1, 1, 0, Color.BLACK));
                    } else if (column == lastColumn) {
                        ((JComponent) c).setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, Color.BLACK));
                    } else if (column == 6) {
                        ((JComponent) c).setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, Color.BLACK));
                    } else {
                        ((JComponent) c).setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.BLACK));
                    }
                } else {
                    if (column == 6) {
                        ((JComponent) c).setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.BLACK));
                    } else {
                        ((JComponent) c).setBorder(BorderFactory.createEmptyBorder());
                    }
                }
                return c;
            }
        };
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
        tabelaAtendimentos.getColumnModel().getColumn(0).setPreferredWidth(100);
        tabelaAtendimentos.getColumnModel().getColumn(1).setPreferredWidth(80);
        tabelaAtendimentos.getColumnModel().getColumn(2).setPreferredWidth(200);
        tabelaAtendimentos.getColumnModel().getColumn(3).setPreferredWidth(200);
        tabelaAtendimentos.getColumnModel().getColumn(4).setPreferredWidth(150);
        tabelaAtendimentos.getColumnModel().getColumn(5).setPreferredWidth(100);
        tabelaAtendimentos.getColumnModel().getColumn(6).setPreferredWidth(100);
        tabelaAtendimentos.getColumnModel().getColumn(7).setPreferredWidth(100);
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

    /**
     * Carrega dados iniciais (profissionais e empresas parceiras) nos combos.
     */
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
            cbEmpresaParceira.removeAllItems();
            cbEmpresaParceira.addItem(null);
            List<EmpresaParceira> empresas = empresaParceiraController.listarTodos();
            empresas.stream()
                    .filter(emp -> emp.getId() > 0)
                    .forEach(cbEmpresaParceira::addItem);
            cbEmpresaParceira.setSelectedIndex(0);
            atualizarPaciente();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Atualiza os dados do paciente com base na busca.
     */
    private void atualizarPaciente() throws SQLException {
        String busca = txtBuscaPaciente.getText().toLowerCase();
        Paciente paciente = pacienteSearcher.buscarPacientePorNome(busca);
        if (paciente != null) {
            lblNomePaciente.setText("Nome: " + paciente.getNome());
            lblTelefone.setText("Telefone: " + (paciente.getTelefone() != null ? paciente.getTelefone() : "N/A"));
            long idade = pacienteSearcher.calcularIdade(paciente);
            lblIdade.setText("Idade: " + idade);
            lblEmail.setText("Email: " + (paciente.getEmail() != null ? paciente.getEmail() : "N/A"));
            String endereco = paciente.getEndereco() != null ? paciente.getEnderecoCompleto() : "N/A";
            lblEndereco.setText("Endereço: " + endereco);
        } else {
            lblNomePaciente.setText("Nome:");
            lblTelefone.setText("Telefone:");
            lblIdade.setText("Idade:");
            lblEmail.setText("Email:");
            lblEndereco.setText("Endereço:");
        }
    }

    /**
     * Atualiza os horários disponíveis no combo.
     */
    private void atualizarHorarios() {
        cbHorario.removeAllItems();
        cbHorario.setEnabled(false);
        Profissional prof = (Profissional) cbProfissional.getSelectedItem();
        Atendimento.Tipo tipo = (Atendimento.Tipo) cbTipo.getSelectedItem();
        if (prof == null || dataSelecionada == null || tipo == null) {
            return;
        }
        try {
            List<LocalTime> horarios = horarioValidator.listarHorariosDisponiveis(prof, dataSelecionada, tipo);
            horarios.forEach(cbHorario::addItem);
            cbHorario.setEnabled(!horarios.isEmpty());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar horários: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Carrega os atendimentos na tabela.
     */
    private void carregarAtendimentos() {
        modeloTabela.setRowCount(0);
        try {
            List<Object[]> atendimentos = atendimentoFilter.listarAtendimentosFiltrados(LocalDate.now(), null, null);
            atendimentos.forEach(modeloTabela::addRow);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar atendimentos: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Filtra a tabela com base nos campos de busca.
     * @param paciente Nome do paciente para filtro.
     * @param prof Nome do profissional para filtro.
     */
    private void filtrar(String paciente, String prof) {
        modeloTabela.setRowCount(0);
        try {
            List<Object[]> atendimentos = atendimentoFilter.listarAtendimentosFiltrados(LocalDate.now(), paciente, prof);
            atendimentos.forEach(modeloTabela::addRow);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao filtrar atendimentos: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Salva um novo atendimento.
     */
    private void salvarAtendimento() {
        try {
            String nomePaciente = lblNomePaciente.getText().replace("Nome: ", "").trim();
            Paciente paciente = pacienteSearcher.buscarPacientePorNome(nomePaciente);
            if (paciente == null) {
                throw new Exception("Paciente não encontrado!");
            }
            Profissional prof = (Profissional) cbProfissional.getSelectedItem();
            LocalTime hora = (LocalTime) cbHorario.getSelectedItem();
            Atendimento.Tipo tipo = (Atendimento.Tipo) cbTipo.getSelectedItem();
            EmpresaParceira empresa = (EmpresaParceira) cbEmpresaParceira.getSelectedItem();
            LocalDateTime dataHora = dataSelecionada.atTime(hora);
            AgendamentoRequest request = new AgendamentoRequest(
                    paciente,
                    prof,
                    empresa,
                    dataHora,
                    tipo,
                    txtObservacoes.getText(),
                    Sessao.getUsuarioLogado().getLogin()
            );
            if (agendamentoService.criarAgendamento(request)) {
                JOptionPane.showMessageDialog(this, "Atendimento salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
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

    /**
     * Limpa os campos do formulário.
     */
    private void limparCampos() throws SQLException {
        txtBuscaPaciente.setText("");
        cbTipo.setSelectedIndex(0);
        cbHorario.removeAllItems();
        txtObservacoes.setText("");
        cbEmpresaParceira.setSelectedIndex(0);
        atualizarPaciente();
        if (cbProfissional.getItemCount() > 0) {
            cbProfissional.setSelectedIndex(0);
        }
    }
}