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
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
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

public class MarcacaoAtendimentoPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private JTextField txtBuscaPaciente;
    private JLabel lblNomePaciente;
    private JLabel lblTelefone;
    private JLabel lblIdade;
    private JLabel lblEmail;
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
    private JLabel lblMesAno;

    private JTextField txtBuscaProfissional;

    private final String[] meses = {"Janeiro","Fevereiro","Março","Abril","Maio","Junho",
                                    "Julho","Agosto","Setembro","Outubro","Novembro","Dezembro"};

    private final Color primaryColor = new Color(30, 144, 255); // Azul profissional
    private final Color backgroundColor = new Color(245, 245, 245); // Fundo claro
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 18);
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 14);

    private final AtendimentoController atendimentoController = new AtendimentoController();
    private final PacienteController pacienteController = new PacienteController();
    private final ProfissionalController profissionalController = new ProfissionalController();
    private final EscalaProfissionalController escalaController = new EscalaProfissionalController();

    public MarcacaoAtendimentoPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(backgroundColor);

        JLabel lblTitulo = new JLabel("Marcação de Atendimento", SwingConstants.CENTER);
        lblTitulo.setFont(titleFont);
        lblTitulo.setForeground(primaryColor);
        lblTitulo.setBorder(new EmptyBorder(10, 0, 20, 0));
        add(lblTitulo, BorderLayout.NORTH);

        JPanel painelFormulario = criarPainelFormulario();
        JPanel painelTabela = criarPainelTabela();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, painelFormulario, painelTabela);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerSize(8);
        splitPane.setBackground(backgroundColor);

        add(splitPane, BorderLayout.CENTER);

        dataSelecionada = LocalDate.now();
        atualizarCalendario();
        carregarAtendimentos();
        carregarDadosIniciais();
    }

    private JPanel criarPainelFormulario() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(primaryColor, 1, true), "Agendar Consulta", TitledBorder.LEFT, TitledBorder.TOP, labelFont, primaryColor),
                new EmptyBorder(10, 10, 10, 10)));
        panel.setBackground(backgroundColor);

        // Parte 1: Busca e Dados do Paciente
        JPanel panelPaciente = new JPanel(new BorderLayout(5, 5));
        panelPaciente.setBorder(new EmptyBorder(0, 0, 10, 0));
        panelPaciente.setBackground(backgroundColor);
        JLabel lblBuscaPaciente = new JLabel("Buscar Paciente:");
        lblBuscaPaciente.setFont(labelFont);
        txtBuscaPaciente = new JTextField(20);
        txtBuscaPaciente.setPreferredSize(new Dimension(300, 30));
        JPanel buscaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buscaPanel.add(lblBuscaPaciente);
        buscaPanel.add(txtBuscaPaciente);
        buscaPanel.setBackground(backgroundColor);
        panelPaciente.add(buscaPanel, BorderLayout.NORTH);

        JPanel panelDados = new JPanel(new GridLayout(4, 1, 0, 5));
        panelDados.setBackground(backgroundColor);
        lblNomePaciente = new JLabel("Nome:");
        lblNomePaciente.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTelefone = new JLabel("Telefone:");
        lblIdade = new JLabel("Idade:");
        lblEmail = new JLabel("Email:");
        lblTelefone.setFont(labelFont);
        lblIdade.setFont(labelFont);
        lblEmail.setFont(labelFont);
        panelDados.add(lblNomePaciente);
        panelDados.add(lblTelefone);
        panelDados.add(lblIdade);
        panelDados.add(lblEmail);
        panelPaciente.add(panelDados, BorderLayout.CENTER);

        // Parte 2: Profissional e Tipo
        JPanel panelSelecao = new JPanel(new GridBagLayout());
        panelSelecao.setBackground(backgroundColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblProf = new JLabel("Profissional:");
        lblProf.setFont(labelFont);
        gbc.gridx = 0; gbc.gridy = 0;
        panelSelecao.add(lblProf, gbc);
        cbProfissional = new JComboBox<>();
        cbProfissional.setPreferredSize(new Dimension(300, 30));
        cbProfissional.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridx = 1;
        panelSelecao.add(cbProfissional, gbc);

        JLabel lblTipo = new JLabel("Tipo:");
        lblTipo.setFont(labelFont);
        gbc.gridx = 0; gbc.gridy = 1;
        panelSelecao.add(lblTipo, gbc);
        cbTipo = new JComboBox<>(Atendimento.Tipo.values());
        cbTipo.setPreferredSize(new Dimension(300, 30));
        cbTipo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridx = 1;
        panelSelecao.add(cbTipo, gbc);

        // Parte 3: Calendário e Horário
        JPanel panelDataHora = new JPanel(new BorderLayout(10, 10));
        panelDataHora.setBackground(backgroundColor);
        panelDataHora.add(criarMiniCalendario(), BorderLayout.CENTER);

        JPanel horarioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        horarioPanel.setBackground(backgroundColor);
        JLabel lblHorario = new JLabel("Horário:");
        lblHorario.setFont(labelFont);
        cbHorario = new JComboBox<>();
        cbHorario.setPreferredSize(new Dimension(150, 30));
        cbHorario.setEnabled(false);
        cbHorario.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        horarioPanel.add(lblHorario);
        horarioPanel.add(cbHorario);
        panelDataHora.add(horarioPanel, BorderLayout.SOUTH);

        // Montagem vertical
        JPanel panelVertical = new JPanel();
        panelVertical.setLayout(new BoxLayout(panelVertical, BoxLayout.Y_AXIS));
        panelVertical.setBackground(backgroundColor);
        panelVertical.add(panelPaciente);
        panelVertical.add(Box.createVerticalStrut(20));
        panelVertical.add(panelSelecao);
        panelVertical.add(Box.createVerticalStrut(20));
        panelVertical.add(panelDataHora);

        // Botões
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
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
        panelBotoes.add(btnSalvar);
        panelBotoes.add(btnLimpar);

        btnSalvar.addActionListener(e -> salvarAtendimento());
        btnLimpar.addActionListener(e -> {
            try {
                limparCampos();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        });

        panel.add(panelVertical, BorderLayout.CENTER);
        panel.add(panelBotoes, BorderLayout.SOUTH);

        cbProfissional.addActionListener(e -> atualizarHorarios());
        txtBuscaPaciente.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { try {
                atualizarPaciente();
            } catch (SQLException e1) {
                e1.printStackTrace();
            } }
            public void removeUpdate(DocumentEvent e) { try {
                atualizarPaciente();
            } catch (SQLException e1) {
                e1.printStackTrace();
            } }
            public void changedUpdate(DocumentEvent e) { try {
                atualizarPaciente();
            } catch (SQLException e1) {
                e1.printStackTrace();
            } }
        });

        return panel;
    }

    private void atualizarPaciente() throws SQLException {
        String busca = txtBuscaPaciente.getText().toLowerCase();
        Paciente p = pacienteController.listarTodos().stream()
                .filter(pa -> pa.getNome().toLowerCase().contains(busca))
                .findFirst().orElse(null);
        if (p != null) {
            lblNomePaciente.setText("Nome: " + p.getNome());
            lblTelefone.setText("Telefone: " + (p.getTelefone() != null ? p.getTelefone() : "N/A"));
            long idade = p.getDataNascimento() != null ? ChronoUnit.YEARS.between(p.getDataNascimento(), LocalDate.now()) : 0;
            lblIdade.setText("Idade: " + idade);
            lblEmail.setText("Email: " + (p.getEmail() != null ? p.getEmail() : "N/A"));
        } else {
            lblNomePaciente.setText("Nome:");
            lblTelefone.setText("Telefone:");
            lblIdade.setText("Idade:");
            lblEmail.setText("Email:");
        }
    }

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
        for (int i = anoAtual - 5; i <= anoAtual + 5; i++) cbAno.addItem(i);
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
        lblMesAno = new JLabel("", SwingConstants.CENTER);
        lblMesAno.setFont(labelFont);

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
        topo.add(lblMesAno);
        panel.add(topo, BorderLayout.NORTH);
        panel.add(painelDias, BorderLayout.CENTER);

        return panel;
    }

    private void atualizarCalendario() {
        painelDias.removeAll();
        int mes = cbMes.getSelectedIndex() + 1;
        int ano = (Integer) cbAno.getSelectedItem();
        YearMonth ym = YearMonth.of(ano, mes);

        lblMesAno.setText(meses[mes - 1] + " " + ano);

        String[] diasSemana = {"Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"};
        for (String d : diasSemana) {
            JLabel lbl = new JLabel(d, SwingConstants.CENTER);
            lbl.setFont(labelFont);
            lbl.setForeground(Color.GRAY);
            painelDias.add(lbl);
        }

        LocalDate primeira = ym.atDay(1);
        int diaSemanaInicio = primeira.getDayOfWeek().getValue() % 7;
        for (int i = 0; i < diaSemanaInicio; i++) painelDias.add(new JLabel(""));

        int diasNoMes = ym.lengthOfMonth();
        LocalDate hoje = LocalDate.now();
        for (int dia = 1; dia <= diasNoMes; dia++) {
            LocalDate d = ym.atDay(dia);
            JButton btn = new JButton(String.valueOf(dia));
            btn.setPreferredSize(new Dimension(40, 40));
            btn.setFont(labelFont);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setBorder(BorderFactory.createEmptyBorder());
            btn.setFocusPainted(false);

            // Definir cores com base na data
            if (d.equals(dataSelecionada)) {
                btn.setBackground(new Color(255, 204, 153)); // Laranja suave para dia selecionado
                btn.setForeground(Color.BLACK);
                btn.setBorder(BorderFactory.createLineBorder(primaryColor, 2)); // Borda para efeito pressionado
            } else if (d.isBefore(hoje)) {
                btn.setBackground(new Color(220, 220, 220)); // Cinza claro para dias passados
                btn.setForeground(Color.BLACK);
            } else if (d.equals(hoje)) {
                btn.setBackground(new Color(144, 238, 144)); // Verde suave para o dia atual
                btn.setForeground(Color.BLACK);
            } else {
                btn.setBackground(new Color(173, 216, 230)); // Azul suave para dias futuros
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
                        btn.setBackground(new Color(255, 204, 153)); // Laranja suave
                        btn.setForeground(Color.BLACK);
                        btn.setBorder(BorderFactory.createLineBorder(primaryColor, 2));
                    } else if (d.isBefore(hoje)) {
                        btn.setBackground(new Color(220, 220, 220)); // Cinza claro
                        btn.setForeground(Color.BLACK);
                    } else if (d.equals(hoje)) {
                        btn.setBackground(new Color(144, 238, 144)); // Verde suave
                        btn.setForeground(Color.BLACK);
                    } else {
                        btn.setBackground(new Color(173, 216, 230)); // Azul suave
                        btn.setForeground(Color.BLACK);
                    }
                }
            });

            btn.addActionListener(e -> {
                dataSelecionada = d;
                atualizarHorarios();
                atualizarCalendario(); // Atualiza para destacar o novo dia selecionado
            });
            painelDias.add(btn);
        }
        painelDias.revalidate();
        painelDias.repaint();
    }

    private JPanel criarPainelTabela() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(primaryColor, 1, true), "Atendimentos", TitledBorder.LEFT, TitledBorder.TOP, labelFont, primaryColor),
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

        String[] colunas = {"Data", "Horário", "Paciente", "Profissional", "Tipo", "Situação"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tabelaAtendimentos = new JTable(modeloTabela) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                Atendimento.Situacao situacao = (Atendimento.Situacao) getValueAt(row, 5);
                switch (situacao) {
                    case AGENDADO -> c.setBackground(new Color(173, 216, 230));
                    case REALIZADO -> c.setBackground(new Color(144, 238, 144));
                    case FALTOU -> c.setBackground(new Color(255, 255, 153));
                    case CANCELADO -> c.setBackground(new Color(255, 182, 193));
                    default -> c.setBackground(Color.WHITE);
                }
                c.setForeground(Color.BLACK);
                return c;
            }
        };
        tabelaAtendimentos.setFillsViewportHeight(true);
        tabelaAtendimentos.setRowHeight(25);
        tabelaAtendimentos.setFont(labelFont);
        tabelaAtendimentos.setBackground(Color.WHITE);

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
            public void insertUpdate(DocumentEvent e) { filtrar(txtBuscaPacienteTabela.getText(), txtBuscaProfissional.getText()); }
            public void removeUpdate(DocumentEvent e) { filtrar(txtBuscaPacienteTabela.getText(), txtBuscaProfissional.getText()); }
            public void changedUpdate(DocumentEvent e) { filtrar(txtBuscaPacienteTabela.getText(), txtBuscaProfissional.getText()); }
        });

        txtBuscaProfissional.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtrar(txtBuscaPacienteTabela.getText(), txtBuscaProfissional.getText()); }
            public void removeUpdate(DocumentEvent e) { filtrar(txtBuscaPacienteTabela.getText(), txtBuscaProfissional.getText()); }
            public void changedUpdate(DocumentEvent e) { filtrar(txtBuscaPacienteTabela.getText(), txtBuscaProfissional.getText()); }
        });

        return panel;
    }

    private void filtrar(String paciente, String prof) {
        RowFilter<DefaultTableModel, Object> filterPaciente = RowFilter.regexFilter("(?i)" + paciente, 2);
        RowFilter<DefaultTableModel, Object> filterProf = RowFilter.regexFilter("(?i)" + prof, 3);
        sorter.setRowFilter(RowFilter.andFilter(List.of(filterPaciente, filterProf)));
    }

    private void carregarDadosIniciais() {
        try {
            cbProfissional.removeAllItems();
            profissionalController.listarTodos().stream().filter(Profissional::isAtivo).forEach(cbProfissional::addItem);
            atualizarPaciente();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void atualizarHorarios() {
        cbHorario.removeAllItems();
        cbHorario.setEnabled(false);

        Profissional prof = (Profissional) cbProfissional.getSelectedItem();
        if (prof == null || dataSelecionada == null) return;

        try {
            int diaSemana = dataSelecionada.getDayOfWeek().getValue();

            List<EscalaProfissional> escalas = escalaController.listarTodas().stream()
                    .filter(e -> e.getProfissionalId() == prof.getId() && e.getDiaSemana() == diaSemana && e.isDisponivel())
                    .collect(Collectors.toList());

            List<LocalTime> ocupados = atendimentoController.listarTodos().stream()
                    .filter(a -> a.getProfissional().getId() == prof.getId() &&
                            a.getDataHora().toLocalDateTime().toLocalDate().equals(dataSelecionada))
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
            cbHorario.setEnabled(cbHorario.getItemCount() > 0);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar horários: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarAtendimentos() {
        modeloTabela.setRowCount(0);
        try {
            List<Atendimento> atendimentos = atendimentoController.listarTodos();
            for (Atendimento a : atendimentos) {
                modeloTabela.addRow(new Object[]{
                        a.getDataHora().toLocalDateTime().toLocalDate(),
                        a.getDataHora().toLocalDateTime().toLocalTime(),
                        a.getPacienteNome(),
                        a.getProfissional().getNome(),
                        a.getTipo(),
                        a.getSituacao()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar atendimentos: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void salvarAtendimento() {
        try {
            String nomePaciente = lblNomePaciente.getText().replace("Nome: ", "").trim();
            if (nomePaciente.isEmpty()) throw new CampoObrigatorioException("Selecione um paciente!");

            Paciente p = pacienteController.listarTodos().stream()
                    .filter(pa -> pa.getNome().equals(nomePaciente))
                    .findFirst().orElseThrow(() -> new CampoObrigatorioException("Paciente não encontrado!"));

            Profissional prof = (Profissional) cbProfissional.getSelectedItem();
            LocalTime hora = (LocalTime) cbHorario.getSelectedItem();
            Atendimento.Tipo tipo = (Atendimento.Tipo) cbTipo.getSelectedItem();

            if (prof == null || hora == null || dataSelecionada == null || tipo == null)
                throw new CampoObrigatorioException("Preencha todos os campos!");

            Atendimento at = new Atendimento();
            at.setPaciente(p);
            at.setProfissional(prof);
            at.setDataHora(java.sql.Timestamp.valueOf(dataSelecionada.atTime(hora)));
            at.setDuracaoMin(30);
            at.setTipo(tipo);
            at.setSituacao(Atendimento.Situacao.AGENDADO);
            at.setUsuario(Sessao.getUsuarioLogado().getLogin());

            if (atendimentoController.criarAtendimento(at, Sessao.getUsuarioLogado().getLogin())) {
                JOptionPane.showMessageDialog(this, "Atendimento salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                carregarAtendimentos();
                limparCampos();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limparCampos() throws SQLException {
        txtBuscaPaciente.setText("");
        cbProfissional.setSelectedIndex(-1);
        cbTipo.setSelectedIndex(0);
        cbHorario.removeAllItems();
        atualizarPaciente();
    }
}