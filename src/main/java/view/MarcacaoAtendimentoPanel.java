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
 * Painel para marcação de atendimentos, com formulário e tabela.
 */
public class MarcacaoAtendimentoPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private JTextField txtBuscaPaciente, txtBuscaProfissional;
    private JLabel lblNomePaciente, lblTelefone, lblIdade, lblEmail;
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
    private final DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final String[] meses = { "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto",
            "Setembro", "Outubro", "Novembro", "Dezembro" };
    // Estilo visual
    private final Color primaryColor = new Color(30, 144, 255); // Azul
    private final Color backgroundColor = new Color(245, 245, 245); // Fundo geral
    private final Color rowColorLightBlue = new Color(230, 240, 255); // Azul claro para linhas pares
    private final Color buttonColor = new Color(161, 207, 239); // Azul para botões
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 17);
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 13);
    private final Font tableFont = new Font("SansSerif", Font.PLAIN, 13);
    // Injeção de dependências
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
     * Construtor do painel, inicializa a interface gráfica.
     */
    public MarcacaoAtendimentoPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(5, 10, 10, 10));
        setBackground(backgroundColor);

        // Título do painel
        JLabel lblTitulo = new JLabel("Marcação de Atendimento", SwingConstants.CENTER);
        lblTitulo.setFont(titleFont);
        lblTitulo.setForeground(primaryColor);
        lblTitulo.setBorder(new EmptyBorder(5, 0, 10, 0));
        add(lblTitulo, BorderLayout.NORTH);

        // Painéis de formulário e tabela
        JPanel panelFormulario = criarPainelFormulario();
        JPanel panelTabela = criarPainelTabela();

        // SplitPane para dividir formulário e tabela
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelFormulario, panelTabela);
        splitPane.setResizeWeight(0.50);
        splitPane.setDividerSize(7);
        splitPane.setBackground(backgroundColor);
        add(splitPane, BorderLayout.CENTER);

        // Inicializa data selecionada e carrega dados
        dataSelecionada = LocalDate.now();
        atualizarCalendario();
        carregarAtendimentos();
        carregarDadosIniciais();

        // Garantir que o JSplitPane inicie com proporção correta
        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.42));
        revalidate();
        repaint();
    }

    /**
     * Cria o painel de formulário para agendamento.
     * @return JPanel com os componentes do formulário.
     */
    private JPanel criarPainelFormulario() {
        JPanel panelWrapper = new JPanel();
        panelWrapper.setLayout(new BoxLayout(panelWrapper, BoxLayout.Y_AXIS));
        panelWrapper.setBackground(backgroundColor);
        panelWrapper.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Painel de formulário
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(primaryColor, 1, true),
                        "Marcar Novo Atendimento",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        labelFont,
                        primaryColor),
                new EmptyBorder(5, 5, 5, 5)));
        panelFormulario.setBackground(backgroundColor);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;

        // Busca Paciente
        JLabel lblBuscaPaciente = new JLabel("Buscar Paciente:");
        lblBuscaPaciente.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        panelFormulario.add(lblBuscaPaciente, gbc);

        txtBuscaPaciente = new JTextField(20);
        txtBuscaPaciente.setPreferredSize(new Dimension(250, 25));
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panelFormulario.add(txtBuscaPaciente, gbc);
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;

        // Painel para dados do paciente e observações (lado a lado)
        JPanel dadosObservacoesPanel = new JPanel(new GridBagLayout());
        dadosObservacoesPanel.setBackground(backgroundColor);
        GridBagConstraints gbcDadosObs = new GridBagConstraints();
        gbcDadosObs.insets = new Insets(5, 10, 5, 10);
        gbcDadosObs.anchor = GridBagConstraints.WEST;
        gbcDadosObs.weightx = 0.0;
        gbcDadosObs.weighty = 0.0;

        // Dados do Paciente (esquerda)
        JPanel dadosPacientePanel = new JPanel(new GridBagLayout());
        dadosPacientePanel.setBackground(backgroundColor);
        GridBagConstraints gbcPaciente = new GridBagConstraints();
        gbcPaciente.insets = new Insets(3, 0, 3, 0);
        gbcPaciente.anchor = GridBagConstraints.WEST;

        lblNomePaciente = new JLabel("Nome:");
        lblNomePaciente.setFont(new Font("SansSerif", Font.BOLD, 15));
        gbcPaciente.gridx = 0;
        gbcPaciente.gridy = 0;
        gbcPaciente.gridwidth = 2;
        dadosPacientePanel.add(lblNomePaciente, gbcPaciente);

        lblTelefone = new JLabel("Telefone:");
        lblTelefone.setFont(labelFont);
        gbcPaciente.gridy = 1;
        dadosPacientePanel.add(lblTelefone, gbcPaciente);

        lblIdade = new JLabel("Idade:");
        lblIdade.setFont(labelFont);
        gbcPaciente.gridy = 2;
        dadosPacientePanel.add(lblIdade, gbcPaciente);

        lblEmail = new JLabel("Email:");
        lblEmail.setFont(labelFont);
        gbcPaciente.gridy = 3;
        dadosPacientePanel.add(lblEmail, gbcPaciente);

        gbcDadosObs.gridx = 0;
        gbcDadosObs.gridy = 0;
        gbcDadosObs.fill = GridBagConstraints.BOTH;
        gbcDadosObs.weightx = 0.4;
        gbcDadosObs.weighty = 1.0;
        dadosObservacoesPanel.add(dadosPacientePanel, gbcDadosObs);

        // Observações (direita, altura reduzida)
        JPanel observacoesPanel = new JPanel(new BorderLayout());
        observacoesPanel.setBackground(backgroundColor);
        JLabel lblObservacoes = new JLabel("Observações:");
        lblObservacoes.setFont(labelFont);
        lblObservacoes.setBorder(new EmptyBorder(0, 0, 5, 0));
        observacoesPanel.add(lblObservacoes, BorderLayout.NORTH);
        txtObservacoes = new JTextArea(4, 20);
        txtObservacoes.setLineWrap(true);
        txtObservacoes.setWrapStyleWord(true);
        JScrollPane scrollObservacoes = new JScrollPane(txtObservacoes);
        scrollObservacoes.setPreferredSize(new Dimension(250, 75));
        scrollObservacoes.setMinimumSize(new Dimension(250, 60));
        observacoesPanel.add(scrollObservacoes, BorderLayout.CENTER);

        gbcDadosObs.gridx = 1;
        gbcDadosObs.gridy = 0;
        gbcDadosObs.fill = GridBagConstraints.BOTH;
        gbcDadosObs.weightx = 0.6;
        gbcDadosObs.weighty = 1.0;
        dadosObservacoesPanel.add(observacoesPanel, gbcDadosObs);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.3;
        panelFormulario.add(dadosObservacoesPanel, gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;

        // Seleções (Profissional e Tipo)
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        JLabel lblProf = new JLabel("Profissional:");
        lblProf.setFont(labelFont);
        panelFormulario.add(lblProf, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;
        cbProfissional = new JComboBox<>();
        cbProfissional.setPreferredSize(new Dimension(200, 25));
        cbProfissional.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panelFormulario.add(cbProfissional, gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;

        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        JLabel lblTipo = new JLabel("Tipo:");
        lblTipo.setFont(labelFont);
        panelFormulario.add(lblTipo, gbc);

        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;
        cbTipo = new JComboBox<>(Atendimento.Tipo.values());
        cbTipo.setPreferredSize(new Dimension(150, 25));
        cbTipo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panelFormulario.add(cbTipo, gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;

        // Seleções (Horário e Parceria)
        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel lblHorario = new JLabel("Horário:");
        lblHorario.setFont(labelFont);
        panelFormulario.add(lblHorario, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;
        cbHorario = new JComboBox<>();
        cbHorario.setPreferredSize(new Dimension(150, 25));
        cbHorario.setEnabled(false);
        cbHorario.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panelFormulario.add(cbHorario, gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;

        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        JLabel lblEmpresa = new JLabel("Parceria:");
        lblEmpresa.setFont(labelFont);
        panelFormulario.add(lblEmpresa, gbc);

        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;
        cbEmpresaParceira = new JComboBox<>();
        cbEmpresaParceira.setPreferredSize(new Dimension(150, 25));
        cbEmpresaParceira.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cbEmpresaParceira.addItem(null);
        panelFormulario.add(cbEmpresaParceira, gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;

        // Mini Calendário
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.4;
        JPanel panelDataHora = criarMiniCalendario();
        panelFormulario.add(panelDataHora, gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        gbc.weighty = 0.0;

        // Botões
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        panelBotoes.setBackground(backgroundColor);
        JButton btnLimpar = new JButton("Limpar");
        btnLimpar.setBackground(Color.LIGHT_GRAY);
        btnLimpar.setForeground(Color.BLACK);
        btnLimpar.setPreferredSize(new Dimension(100, 35));
        btnLimpar.setFont(labelFont);
        btnLimpar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JButton btnSalvar = new JButton("Salvar");
        btnSalvar.setBackground(primaryColor);
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setPreferredSize(new Dimension(100, 35));
        btnSalvar.setFont(labelFont);
        btnSalvar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panelBotoes.add(btnLimpar);
        panelBotoes.add(btnSalvar);
        panelFormulario.add(panelBotoes, gbc);

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

        panelWrapper.add(panelFormulario);
        panelWrapper.add(Box.createVerticalStrut(5));
        return panelWrapper;
    }

    /**
     * Cria o mini calendário para seleção de data.
     * @return JPanel com o calendário.
     */
    private JPanel criarMiniCalendario() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(primaryColor, 1, true),
                        "Selecionar Data",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        labelFont,
                        primaryColor),
                new EmptyBorder(5, 5, 5, 5)));
        panel.setBackground(backgroundColor);

        // Painel topo com FlowLayout (como na AgendaPanel)
        JPanel panelTopo = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panelTopo.setBackground(backgroundColor);
        JButton btnPrev = new JButton("<");
        btnPrev.setPreferredSize(new Dimension(40, 25));
        btnPrev.setBackground(Color.WHITE);
        btnPrev.setFont(labelFont);
        btnPrev.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JButton btnNext = new JButton(">");
        btnNext.setPreferredSize(new Dimension(40, 25));
        btnNext.setBackground(Color.WHITE);
        btnNext.setFont(labelFont);
        btnNext.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cbMes = new JComboBox<>(meses);
        cbMes.setPreferredSize(new Dimension(120, 25));
        cbMes.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cbAno = new JComboBox<>();
        cbAno.setPreferredSize(new Dimension(80, 25));
        cbAno.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        int anoAtual = LocalDate.now().getYear();
        for (int i = anoAtual - 5; i <= anoAtual + 5; i++)
            cbAno.addItem(i);
        cbMes.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        cbAno.setSelectedItem(anoAtual);

        panelTopo.add(btnPrev);
        panelTopo.add(cbMes);
        panelTopo.add(cbAno);
        panelTopo.add(btnNext);

        // Painel de dias
        painelDias = new JPanel(new GridLayout(0, 7, 5, 5));
        painelDias.setBackground(backgroundColor);
        painelDias.setPreferredSize(new Dimension(300, 200));
        painelDias.setMinimumSize(new Dimension(300, 200));

        panel.add(panelTopo, BorderLayout.NORTH);
        panel.add(painelDias, BorderLayout.CENTER);

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

        return panel;
    }

    /**
     * Atualiza o calendário com os dias do mês selecionado.
     */
    private void atualizarCalendario() {
        painelDias.removeAll();
        int mes = cbMes.getSelectedIndex() + 1;
        int ano = (Integer) cbAno.getSelectedItem();
        YearMonth anoMes = YearMonth.of(ano, mes);

        // Adiciona dias da semana
        String[] diasSemana = { "Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb" };
        for (String d : diasSemana) {
            JLabel lblDia = new JLabel(d, SwingConstants.CENTER);
            lblDia.setFont(new Font("SansSerif", Font.BOLD, 12));
            lblDia.setForeground(Color.GRAY);
            painelDias.add(lblDia);
        }

        // Calcula espaços iniciais e dias do mês
        LocalDate primeiraData = anoMes.atDay(1);
        int diaSemanaInicio = primeiraData.getDayOfWeek().getValue() % 7; // 0 = domingo
        for (int i = 0; i < diaSemanaInicio; i++) {
            painelDias.add(new JLabel(""));
        }

        // Adiciona os dias do mês
        int diasNoMes = anoMes.lengthOfMonth();
        for (int dia = 1; dia <= diasNoMes; dia++) {
            LocalDate dataAtual = anoMes.atDay(dia);
            JButton btnDia = new JButton(String.valueOf(dia));
            btnDia.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnDia.setPreferredSize(new Dimension(35, 30));
            btnDia.setFont(new Font("SansSerif", Font.PLAIN, 12));
            btnDia.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            btnDia.setFocusPainted(false);

            // Desabilita domingos (primeira coluna)
            if (dataAtual.getDayOfWeek().getValue() == 7) {
                btnDia.setEnabled(false);
                btnDia.setBackground(Color.decode("#E0E0E0"));
                btnDia.setForeground(Color.decode("#666666"));
            } else if (dataAtual.equals(dataSelecionada)) {
                btnDia.setBackground(new Color(255, 204, 153)); // #FAD7A0 adaptado
                btnDia.setForeground(Color.BLACK);
                btnDia.getModel().setPressed(true);
                btnDia.getModel().setArmed(true);
            } else if (dataAtual.equals(LocalDate.now())) {
                btnDia.setBackground(new Color(144, 238, 144)); // #32CD32 adaptado
                btnDia.setForeground(Color.BLACK);
            } else if (dataAtual.isBefore(LocalDate.now())) {
                btnDia.setBackground(Color.WHITE);
                btnDia.setForeground(Color.BLACK);
            } else {
                btnDia.setBackground(buttonColor); // #AED6F1 adaptado
                btnDia.setForeground(Color.BLACK);
            }

            btnDia.addActionListener(e -> {
                dataSelecionada = dataAtual;
                atualizarHorarios();
                atualizarCalendario();
            });

            painelDias.add(btnDia);
        }

        painelDias.revalidate();
        painelDias.repaint();
    }

    /**
     * Cria o painel da tabela de atendimentos.
     * @return JPanel com a tabela.
     */
    private JPanel criarPainelTabela() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(primaryColor, 1, true),
                        "Atendimentos Agendados",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        labelFont,
                        primaryColor),
                new EmptyBorder(5, 5, 5, 5)));
        panel.setBackground(backgroundColor);

        // Pesquisa
        JPanel panelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panelBusca.setBackground(backgroundColor);
        JLabel lblBuscaPacienteTabela = new JLabel("Pesquisar Paciente:");
        lblBuscaPacienteTabela.setFont(labelFont);
        JTextField txtBuscaPacienteTabela = new JTextField(15);
        txtBuscaPacienteTabela.setPreferredSize(new Dimension(150, 25));
        JLabel lblBuscaProf = new JLabel("Pesquisar Profissional:");
        lblBuscaProf.setFont(labelFont);
        txtBuscaProfissional = new JTextField(15);
        txtBuscaProfissional.setPreferredSize(new Dimension(150, 25));
        panelBusca.add(lblBuscaPacienteTabela);
        panelBusca.add(txtBuscaPacienteTabela);
        panelBusca.add(lblBuscaProf);
        panelBusca.add(txtBuscaProfissional);
        panel.add(panelBusca, BorderLayout.NORTH);

        // Tabela
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
                                bgColor = isPast ? new Color(220, 220, 220) : (row % 2 == 0 ? rowColorLightBlue : Color.WHITE);
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
                            bgColor = isPast ? new Color(220, 220, 220) : (row % 2 == 0 ? rowColorLightBlue : Color.WHITE);
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
        tabelaAtendimentos.setRowHeight(20);
        tabelaAtendimentos.setShowGrid(false);
        tabelaAtendimentos.setIntercellSpacing(new Dimension(0, 0));
        tabelaAtendimentos.setFont(tableFont);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tabelaAtendimentos.getColumnCount(); i++) {
            tabelaAtendimentos.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        tabelaAtendimentos.getColumnModel().getColumn(0).setPreferredWidth(100);
        tabelaAtendimentos.getColumnModel().getColumn(1).setPreferredWidth(80);
        tabelaAtendimentos.getColumnModel().getColumn(2).setPreferredWidth(200);
        tabelaAtendimentos.getColumnModel().getColumn(3).setPreferredWidth(120);
        tabelaAtendimentos.getColumnModel().getColumn(4).setPreferredWidth(95);
        tabelaAtendimentos.getColumnModel().getColumn(5).setPreferredWidth(100);
        tabelaAtendimentos.getColumnModel().getColumn(6).setPreferredWidth(100);
        tabelaAtendimentos.getColumnModel().getColumn(7).setPreferredWidth(100);
        JTableHeader header = tabelaAtendimentos.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(primaryColor);
        header.setForeground(Color.WHITE);
        sorter = new TableRowSorter<>(modeloTabela);
        tabelaAtendimentos.setRowSorter(sorter);
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
        JScrollPane scrollTabela = new JScrollPane(tabelaAtendimentos);
        scrollTabela.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(scrollTabela, BorderLayout.CENTER);

        // Listeners de pesquisa
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
        } else {
            lblNomePaciente.setText("Nome:");
            lblTelefone.setText("Telefone:");
            lblIdade.setText("Idade:");
            lblEmail.setText("Email:");
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
            if (prof == null) {
                throw new Exception("Selecione um profissional!");
            }
            LocalTime hora = (LocalTime) cbHorario.getSelectedItem();
            if (hora == null) {
                throw new Exception("Selecione um horário!");
            }
            Atendimento.Tipo tipo = (Atendimento.Tipo) cbTipo.getSelectedItem();
            if (tipo == null) {
                throw new Exception("Selecione o tipo de atendimento!");
            }
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