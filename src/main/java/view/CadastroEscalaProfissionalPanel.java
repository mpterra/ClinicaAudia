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
import controller.EscalaProfissionalController;
import controller.ProfissionalController;
import model.EscalaProfissional;
import model.Profissional;
import util.Sessao;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Painel para cadastro e listagem de escalas de profissionais
public class CadastroEscalaProfissionalPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    // Componentes de entrada
    private JComboBox<Profissional> cbProfissional;
    private JButton btnSalvar, btnLimpar, btnExcluir, btnExcluirTodas;
    private JTable tabelaEscalas;
    private DefaultTableModel modeloTabelaEscalas;
    private TableRowSorter<DefaultTableModel> sorter;
    private EscalaProfissionalController escalaController = new EscalaProfissionalController();
    private static final String[] DIAS_SEMANA = { "Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado" };
    private JComboBox<String>[] cbHoraInicio = new JComboBox[DIAS_SEMANA.length];
    private JComboBox<String>[] cbHoraFim = new JComboBox[DIAS_SEMANA.length];
    private JRadioButton[][] rbDisponivel = new JRadioButton[DIAS_SEMANA.length][2];
    private ButtonGroup[] bgDisponivel = new ButtonGroup[DIAS_SEMANA.length];

    // Estilo visual
    private final Color primaryColor = new Color(138, 43, 226); // Roxo
    private final Color backgroundColor = new Color(245, 245, 245); // Fundo geral
    private final Color rowColorLightLilac = new Color(230, 230, 250); // Lilás claro para linhas pares
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 18); // Título principal
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 14); // Labels, TitledBorder, tabela

    // Construtor
    public CadastroEscalaProfissionalPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(5, 15, 15, 15));
        setBackground(backgroundColor);

        // Título do painel
        JLabel lblTitulo = new JLabel("Cadastro de Agenda de Profissionais", SwingConstants.CENTER);
        lblTitulo.setFont(titleFont);
        lblTitulo.setForeground(primaryColor);
        lblTitulo.setBorder(new EmptyBorder(10, 0, 10, 0));
        add(lblTitulo, BorderLayout.NORTH);

        // Painéis de cadastro e tabela
        JPanel panelCadastro = criarPainelCadastro();
        JPanel panelTabela = criarTabelaComPesquisa();

        // SplitPane para dividir cadastro e tabela
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelCadastro, panelTabela);
        splitPane.setResizeWeight(0.60); // Ajustado para 60-40
        splitPane.setDividerSize(7);
        splitPane.setBackground(backgroundColor);

        add(splitPane, BorderLayout.CENTER);

        // Garantir proporção 60-40 do JSplitPane
        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.60));
        revalidate();
        repaint();

        // Ações dos botões
        btnLimpar.addActionListener(e -> limparCampos());
        btnSalvar.addActionListener(e -> {
            try {
                salvarEscalas();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erro ao salvar escalas: " + ex.getMessage(), "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        btnExcluir.addActionListener(e -> excluirEscalaSelecionada());
        btnExcluirTodas.addActionListener(e -> excluirTodasEscalasDoProfissional());

        // Ação do combo de profissional
        cbProfissional.addActionListener(e -> carregarEscalasDoProfissionalSelecionado());

        // Carregar dados iniciais
        carregarProfissionais();
        carregarEscalas();
    }

    // Cria o painel de cadastro
    private JPanel criarPainelCadastro() {
        JPanel panelWrapper = new JPanel(new BorderLayout());
        panelWrapper.setBackground(backgroundColor);
        panelWrapper.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel panelCadastro = new JPanel(new GridBagLayout());
        panelCadastro.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(primaryColor, 1, true),
                        "Escala do Profissional",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        labelFont,
                        primaryColor),
                new EmptyBorder(10, 10, 10, 10)));
        panelCadastro.setBackground(backgroundColor);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Subtítulo
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel lblSubtitulo = new JLabel("Preencha a agenda semanal do profissional");
        lblSubtitulo.setFont(labelFont);
        lblSubtitulo.setForeground(Color.DARK_GRAY);
        panelCadastro.add(lblSubtitulo, gbc);
        gbc.gridwidth = 1;

        // Profissional
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel lblProfissional = new JLabel("Profissional:");
        lblProfissional.setFont(labelFont);
        panelCadastro.add(lblProfissional, gbc);
        cbProfissional = new JComboBox<>();
        cbProfissional.setPreferredSize(new Dimension(200, 30));
        cbProfissional.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cbProfissional.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value instanceof Profissional ? ((Profissional) value).getNome() : "");
                return this;
            }
        });
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCadastro.add(cbProfissional, gbc);
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;

        // Painel de dias
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        JScrollPane scrollDias = criarPainelDiasSemana();
        panelCadastro.add(scrollDias, gbc);
        gbc.weighty = 0.0;

        // Botões
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panelBotoes.setBackground(backgroundColor);
        btnSalvar = new JButton("Salvar");
        btnSalvar.setBackground(primaryColor);
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setPreferredSize(new Dimension(100, 35));
        btnSalvar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLimpar = new JButton("Limpar");
        btnLimpar.setBackground(Color.LIGHT_GRAY);
        btnLimpar.setForeground(Color.BLACK);
        btnLimpar.setPreferredSize(new Dimension(100, 35));
        btnLimpar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnExcluir = new JButton("Excluir");
        btnExcluir.setBackground(Color.RED);
        btnExcluir.setForeground(Color.WHITE);
        btnExcluir.setPreferredSize(new Dimension(100, 35));
        btnExcluir.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnExcluirTodas = new JButton("Excluir Todas");
        btnExcluirTodas.setBackground(Color.RED);
        btnExcluirTodas.setForeground(Color.WHITE);
        btnExcluirTodas.setPreferredSize(new Dimension(120, 35));
        btnExcluirTodas.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panelBotoes.add(btnLimpar);
        panelBotoes.add(btnSalvar);
        panelBotoes.add(btnExcluir);
        panelBotoes.add(btnExcluirTodas);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCadastro.add(panelBotoes, gbc);

        panelWrapper.add(panelCadastro, BorderLayout.NORTH);
        return panelWrapper;
    }

    // Cria o painel de dias da semana
    @SuppressWarnings("unchecked")
    private JScrollPane criarPainelDiasSemana() {
        JPanel panelDias = new JPanel(new GridBagLayout());
        panelDias.setBackground(backgroundColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Colunas
        String[] colunasDia = { "Dia", "Hora Início", "Hora Fim", "Disponível" };
        for (int i = 0; i < colunasDia.length; i++) {
            gbc.gridx = i;
            gbc.gridy = 0;
            JLabel lblColuna = new JLabel(colunasDia[i]);
            lblColuna.setFont(labelFont);
            panelDias.add(lblColuna, gbc);
        }

        String[] horarios = gerarHorariosLimitados();

        for (int i = 0; i < DIAS_SEMANA.length; i++) {
            gbc.gridy = i + 1;

            // Dia
            gbc.gridx = 0;
            gbc.weightx = 0.0;
            JLabel lblDia = new JLabel(DIAS_SEMANA[i]);
            lblDia.setFont(labelFont);
            panelDias.add(lblDia, gbc);

            // Hora Início
            gbc.gridx = 1;
            gbc.weightx = 1.0;
            cbHoraInicio[i] = new JComboBox<>(horarios);
            cbHoraInicio[i].setSelectedItem("09:00");
            cbHoraInicio[i].setPreferredSize(new Dimension(200, 30));
            cbHoraInicio[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            panelDias.add(cbHoraInicio[i], gbc);

            // Hora Fim
            gbc.gridx = 2;
            cbHoraFim[i] = new JComboBox<>(horarios);
            cbHoraFim[i].setSelectedItem("17:30");
            cbHoraFim[i].setPreferredSize(new Dimension(200, 30));
            cbHoraFim[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            panelDias.add(cbHoraFim[i], gbc);

            // Disponível
            gbc.gridx = 3;
            gbc.weightx = 0.0;
            rbDisponivel[i][0] = new JRadioButton("Sim", true);
            rbDisponivel[i][1] = new JRadioButton("Não");
            rbDisponivel[i][0].setFont(labelFont);
            rbDisponivel[i][1].setFont(labelFont);
            rbDisponivel[i][0].setBackground(backgroundColor);
            rbDisponivel[i][1].setBackground(backgroundColor);
            rbDisponivel[i][0].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            rbDisponivel[i][1].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            bgDisponivel[i] = new ButtonGroup();
            bgDisponivel[i].add(rbDisponivel[i][0]);
            bgDisponivel[i].add(rbDisponivel[i][1]);
            JPanel pDisponivel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            pDisponivel.setBackground(backgroundColor);
            pDisponivel.add(rbDisponivel[i][0]);
            pDisponivel.add(rbDisponivel[i][1]);
            panelDias.add(pDisponivel, gbc);

            final int index = i;
            ActionListener toggleDisponivel = e -> {
                boolean ativo = rbDisponivel[index][0].isSelected();
                cbHoraInicio[index].setEnabled(ativo);
                cbHoraFim[index].setEnabled(ativo);
            };
            rbDisponivel[i][0].addActionListener(toggleDisponivel);
            rbDisponivel[i][1].addActionListener(toggleDisponivel);
        }

        JScrollPane scroll = new JScrollPane(panelDias);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scroll.setPreferredSize(new Dimension(600, 300));
        return scroll;
    }

    // Gera horários limitados (08:00 a 18:00, intervalos de 30 minutos)
    private String[] gerarHorariosLimitados() {
        List<String> horarios = new ArrayList<>();
        for (int h = 8; h <= 18; h++) {
            for (int m = 0; m < 60; m += 30) {
                if (h == 18 && m > 0)
                    break;
                horarios.add(String.format("%02d:%02d", h, m));
            }
        }
        return horarios.toArray(new String[0]);
    }

    // Cria o painel da tabela com pesquisa
    private JPanel criarTabelaComPesquisa() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(primaryColor, 1, true),
                        "Escalas Cadastradas",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        labelFont,
                        primaryColor),
                new EmptyBorder(10, 10, 10, 10)));
        panel.setBackground(backgroundColor);

        // Pesquisa
        JPanel panelPesquisa = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelPesquisa.setBackground(backgroundColor);
        JLabel lblPesquisar = new JLabel("Pesquisar por Profissional:");
        lblPesquisar.setFont(labelFont);
        panelPesquisa.add(lblPesquisar);
        JTextField tfPesquisar = new JTextField(20);
        tfPesquisar.setPreferredSize(new Dimension(200, 30));
        panelPesquisa.add(tfPesquisar);
        tfPesquisar.getDocument().addDocumentListener(new DocumentListener() {
            private void filtrar() {
                String texto = tfPesquisar.getText().trim();
                sorter.setRowFilter(texto.isEmpty() ? null : RowFilter.regexFilter("(?i)" + texto, 0));
            }
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            public void changedUpdate(DocumentEvent e) { filtrar(); }
        });

        // Tabela
        String[] colunas = { "Profissional", "Dia", "Hora Início", "Hora Fim", "Disponível" };
        modeloTabelaEscalas = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelaEscalas = new JTable(modeloTabelaEscalas);
        tabelaEscalas.setRowHeight(25);
        tabelaEscalas.setShowGrid(false);
        tabelaEscalas.setIntercellSpacing(new Dimension(0, 0));
        tabelaEscalas.setFont(labelFont);
        tabelaEscalas.setFillsViewportHeight(true);

        // Renderizador para alternar cores das linhas
        DefaultTableCellRenderer rowRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? rowColorLightLilac : Color.WHITE);
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        };
        for (int i = 0; i < tabelaEscalas.getColumnCount(); i++) {
            tabelaEscalas.getColumnModel().getColumn(i).setCellRenderer(rowRenderer);
        }

        JTableHeader header = tabelaEscalas.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.setBackground(primaryColor);
        header.setForeground(Color.WHITE);

        sorter = new TableRowSorter<>(modeloTabelaEscalas);
        tabelaEscalas.setRowSorter(sorter);

        JScrollPane scrollTabela = new JScrollPane(tabelaEscalas);
        scrollTabela.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        panel.add(panelPesquisa, BorderLayout.NORTH);
        panel.add(scrollTabela, BorderLayout.CENTER);

        return panel;
    }

    // Limpa os campos do formulário
    private void limparCampos() {
        for (int i = 0; i < DIAS_SEMANA.length; i++) {
            cbHoraInicio[i].setSelectedItem("09:00");
            cbHoraFim[i].setSelectedItem("17:30");
            rbDisponivel[i][0].setSelected(true);
            cbHoraInicio[i].setEnabled(true);
            cbHoraFim[i].setEnabled(true);
        }
    }

    // Salva as escalas no banco
    private void salvarEscalas() throws SQLException {
        Profissional prof = (Profissional) cbProfissional.getSelectedItem();
        if (prof == null) {
            JOptionPane.showMessageDialog(this, "Selecione um profissional!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        escalaController.removerTodasEscalasDoProfissional(prof.getId());

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        List<EscalaProfissional> escalas = new ArrayList<>();

        for (int i = 0; i < DIAS_SEMANA.length; i++) {
            boolean disponivel = rbDisponivel[i][0].isSelected();
            if (!disponivel)
                continue;

            String horaInicioStr = (String) cbHoraInicio[i].getSelectedItem();
            String horaFimStr = (String) cbHoraFim[i].getSelectedItem();

            try {
                Date hi = sdf.parse(horaInicioStr);
                Date hf = sdf.parse(horaFimStr);
                Time horaInicio = new Time(hi.getTime());
                Time horaFim = new Time(hf.getTime());

                if (horaFim.before(horaInicio)) {
                    JOptionPane.showMessageDialog(this,
                            "Hora de término deve ser depois da hora de início para " + DIAS_SEMANA[i] + ".", "Erro",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                EscalaProfissional escala = new EscalaProfissional();
                escala.setProfissionalId(prof.getId());
                escala.setDiaSemana(i);
                escala.setHoraInicio(horaInicio);
                escala.setHoraFim(horaFim);
                escala.setDisponivel(disponivel);
                escalas.add(escala);

            } catch (ParseException e) {
                JOptionPane.showMessageDialog(this, "Formato de hora inválido para " + DIAS_SEMANA[i] + ". Use HH:mm.",
                        "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        boolean sucesso = true;
        for (EscalaProfissional e : escalas)
            sucesso &= escalaController.criarEscala(e, Sessao.getUsuarioLogado().getLogin());

        if (sucesso) {
            JOptionPane.showMessageDialog(this, "Escalas atualizadas com sucesso!", "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);
            carregarEscalas();
        } else {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar algumas escalas.", "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Exclui a escala selecionada
    private void excluirEscalaSelecionada() {
        int linha = tabelaEscalas.getSelectedRow();
        if (linha == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma escala para excluir!", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            List<EscalaProfissional> escalas = escalaController.listarTodas();
            int modelRow = tabelaEscalas.convertRowIndexToModel(linha);
            int idEscala = escalas.get(modelRow).getId();
            escalaController.removerEscala(idEscala);
            JOptionPane.showMessageDialog(this, "Escala excluída com sucesso!", "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);
            carregarEscalas();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao excluir escala: " + e.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Exclui todas as escalas do profissional selecionado
    private void excluirTodasEscalasDoProfissional() {
        Profissional prof = (Profissional) cbProfissional.getSelectedItem();
        if (prof == null) {
            JOptionPane.showMessageDialog(this, "Selecione um profissional!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Deseja excluir todas as escalas do profissional " + prof.getNome() + "?", "Confirmação",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION)
            return;

        escalaController.removerTodasEscalasDoProfissional(prof.getId());
        JOptionPane.showMessageDialog(this, "Todas as escalas do profissional foram excluídas!", "Sucesso",
                JOptionPane.INFORMATION_MESSAGE);
        limparCampos();
        carregarEscalas();
    }

    // Carrega os profissionais no JComboBox
    private void carregarProfissionais() {
        try {
            List<Profissional> profs = new ProfissionalController().listarTodos();
            profs.sort((p1, p2) -> Integer.compare(p1.getId(), p2.getId()));
            cbProfissional.removeAllItems();
            for (Profissional p : profs)
                cbProfissional.addItem(p);
            if (!profs.isEmpty())
                cbProfissional.setSelectedIndex(0);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao carregar profissionais: " + e.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Carrega as escalas na tabela
    private void carregarEscalas() {
        try {
            List<EscalaProfissional> escalas = escalaController.listarTodas();
            modeloTabelaEscalas.setRowCount(0);
            for (EscalaProfissional e : escalas) {
                if (e.getDiaSemana() < 0 || e.getDiaSemana() >= DIAS_SEMANA.length)
                    continue;
                Profissional p = new ProfissionalController().buscarPorId(e.getProfissionalId());
                modeloTabelaEscalas.addRow(new Object[] { p != null ? p.getNome() : "?", DIAS_SEMANA[e.getDiaSemana()],
                        e.getHoraInicio().toString(), e.getHoraFim().toString(),
                        e.isDisponivel() ? "Sim" : "Não" });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Carrega as escalas do profissional selecionado
    private void carregarEscalasDoProfissionalSelecionado() {
        Profissional prof = (Profissional) cbProfissional.getSelectedItem();
        if (prof == null)
            return;
        List<EscalaProfissional> escalas = escalaController.listarPorProfissional(prof.getId());
        for (int i = 0; i < DIAS_SEMANA.length; i++) {
            cbHoraInicio[i].setSelectedItem("09:00");
            cbHoraFim[i].setSelectedItem("17:30");
            rbDisponivel[i][0].setSelected(true);
            cbHoraInicio[i].setEnabled(true);
            cbHoraFim[i].setEnabled(true);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        for (EscalaProfissional e : escalas) {
            int dia = e.getDiaSemana();
            cbHoraInicio[dia].setSelectedItem(sdf.format(e.getHoraInicio()));
            cbHoraFim[dia].setSelectedItem(sdf.format(e.getHoraFim()));
            rbDisponivel[dia][0].setSelected(e.isDisponivel());
            rbDisponivel[dia][1].setSelected(!e.isDisponivel());
            cbHoraInicio[dia].setEnabled(e.isDisponivel());
            cbHoraFim[dia].setEnabled(e.isDisponivel());
        }
    }
}