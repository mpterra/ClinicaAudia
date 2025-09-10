package view;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import controller.EscalaProfissionalController;
import controller.ProfissionalController;
import model.EscalaProfissional;
import model.Profissional;
import util.Sessao;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CadastroEscalaProfissionalPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private JComboBox<Profissional> cbProfissional;
    private JTable tabelaDiasSemana;
    private DefaultTableModel modeloTabelaDias;
    private JButton btnSalvar, btnLimpar, btnExcluir, btnExcluirTodas;
    private JTable tabelaEscalas;
    private DefaultTableModel modeloTabelaEscalas;
    private TableRowSorter<DefaultTableModel> sorter;

    private EscalaProfissionalController escalaController = new EscalaProfissionalController();
    private static final String[] DIAS_SEMANA = { "Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado" };

    public CadastroEscalaProfissionalPanel() {
        setLayout(new BorderLayout(10, 10));

        // Título
        JLabel lblTitulo = new JLabel("Cadastro de Agenda de Profissionais", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(30, 30, 60));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(lblTitulo, BorderLayout.NORTH);

        // Painéis
        JPanel panelCadastro = criarPainelCadastro();
        JPanel panelTabela = criarTabelaComPesquisa();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelCadastro, panelTabela);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(5);
        splitPane.setDividerLocation(0.4);
        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.4));

        JPanel panelWrapper = new JPanel(new BorderLayout());
        panelWrapper.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        panelWrapper.add(splitPane, BorderLayout.CENTER);
        add(panelWrapper, BorderLayout.CENTER);

        // Listeners
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

        cbProfissional.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                carregarEscalasDoProfissionalSelecionado();
            }
        });

        carregarProfissionais();
        carregarEscalas();
    }

    private JPanel criarPainelCadastro() {
        JPanel panelWrapper = new JPanel(new BorderLayout());
        JPanel panelCadastro = new JPanel(new GridBagLayout());

        panelCadastro.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK),
                "Cadastrar/Editar Escala Semanal", TitledBorder.LEADING, TitledBorder.TOP));
        panelWrapper.add(panelCadastro, BorderLayout.NORTH);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Subtítulo
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel lblSubtitulo = new JLabel("Preencha a agenda semanal do profissional");
        lblSubtitulo.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lblSubtitulo.setForeground(Color.DARK_GRAY);
        panelCadastro.add(lblSubtitulo, gbc);
        gbc.gridwidth = 1;

        // Profissional
        gbc.gridx = 0;
        gbc.gridy = 1;
        panelCadastro.add(new JLabel("Profissional:"), gbc);
        cbProfissional = new JComboBox<>();
        cbProfissional.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Profissional)
                    setText(((Profissional) value).getNome());
                else
                    setText("");
                return this;
            }
        });
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCadastro.add(cbProfissional, gbc);

        // Tabela de Dias da Semana
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        JScrollPane scrollTabelaDias = criarTabelaDiasSemana();
        scrollTabelaDias.setPreferredSize(new Dimension(0, 150));
        panelCadastro.add(scrollTabelaDias, gbc);

        // Botões
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSalvar = new JButton("Salvar");
        btnSalvar.setToolTipText("Salvar escalas da semana");
        btnLimpar = new JButton("Limpar");
        btnLimpar.setToolTipText("Limpar formulário");
        btnExcluir = new JButton("Excluir");
        btnExcluir.setToolTipText("Excluir escala selecionada");
        btnExcluirTodas = new JButton("Excluir Todas");
        btnExcluirTodas.setToolTipText("Excluir todas as escalas do profissional selecionado");
        btnExcluirTodas.setPreferredSize(new Dimension(120, 25));
        btnExcluirTodas.setBackground(Color.RED);
        btnExcluirTodas.setForeground(Color.WHITE);

        panelBotoes.add(btnSalvar);
        panelBotoes.add(btnLimpar);
        panelBotoes.add(btnExcluir);
        panelBotoes.add(btnExcluirTodas);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.EAST;
        panelCadastro.add(panelBotoes, gbc);

        Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
        btnSalvar.setCursor(handCursor);
        btnLimpar.setCursor(handCursor);
        btnExcluir.setCursor(handCursor);
        btnExcluirTodas.setCursor(handCursor);

        // Ajuste dinâmico ao redimensionar
        panelWrapper.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                int height = panelWrapper.getHeight();
                int tableHeight = Math.max(100, (int) (height * 0.4));
                scrollTabelaDias.setPreferredSize(new Dimension(0, tableHeight));
                panelWrapper.revalidate();
                panelWrapper.repaint();
            }
        });

        return panelWrapper;
    }

    private JScrollPane criarTabelaDiasSemana() {
        String[] colunas = { "Dia", "Cadastrar Dia", "Hora Início", "Hora Fim", "Disponível" };
        modeloTabelaDias = new DefaultTableModel(colunas, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 1 || columnIndex == 4 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0;
            }
        };

        for (String dia : DIAS_SEMANA) {
            modeloTabelaDias.addRow(new Object[] { dia, true, "08:00", "17:00", true });
        }

        tabelaDiasSemana = new JTable(modeloTabelaDias);
        tabelaDiasSemana.setFillsViewportHeight(true);
        tabelaDiasSemana.setRowHeight(20);
        tabelaDiasSemana.setCursor(new Cursor(Cursor.HAND_CURSOR));

        tabelaDiasSemana.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setText((Boolean) value ? "Sim" : "Não");
                setHorizontalAlignment(SwingConstants.CENTER);
                return this;
            }
        });

        tabelaDiasSemana.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setText((Boolean) value ? "Sim" : "Não");
                setHorizontalAlignment(SwingConstants.CENTER);
                return this;
            }
        });

        tabelaDiasSemana.getColumnModel().getColumn(0).setPreferredWidth(100);
        tabelaDiasSemana.getColumnModel().getColumn(1).setPreferredWidth(80);
        tabelaDiasSemana.getColumnModel().getColumn(2).setPreferredWidth(80);
        tabelaDiasSemana.getColumnModel().getColumn(3).setPreferredWidth(80);
        tabelaDiasSemana.getColumnModel().getColumn(4).setPreferredWidth(80);

        DefaultTableCellRenderer centralizado = new DefaultTableCellRenderer();
        centralizado.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tabelaDiasSemana.getColumnCount(); i++) {
            if (i != 1 && i != 4) {
                tabelaDiasSemana.getColumnModel().getColumn(i).setCellRenderer(centralizado);
            }
        }

        return new JScrollPane(tabelaDiasSemana);
    }

    private JPanel criarTabelaComPesquisa() {
        JPanel panelWrapper = new JPanel(new BorderLayout());

        String[] colunas = { "Profissional", "Dia", "Hora Início", "Hora Fim", "Disponível" };
        modeloTabelaEscalas = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaEscalas = new JTable(modeloTabelaEscalas);
        tabelaEscalas.setFillsViewportHeight(true);

        DefaultTableCellRenderer centralizado = new DefaultTableCellRenderer();
        centralizado.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tabelaEscalas.getColumnCount(); i++) {
            tabelaEscalas.getColumnModel().getColumn(i).setCellRenderer(centralizado);
        }

        sorter = new TableRowSorter<>(modeloTabelaEscalas);
        tabelaEscalas.setRowSorter(sorter);

        JScrollPane scrollTabela = new JScrollPane(tabelaEscalas);

        JPanel panelPesquisa = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblPesquisar = new JLabel("Pesquisar por Profissional:");
        lblPesquisar.setFont(new Font("SansSerif", Font.ITALIC, 14));
        lblPesquisar.setForeground(Color.DARK_GRAY);
        panelPesquisa.add(lblPesquisar);

        JTextField tfPesquisar = new JTextField(20);
        panelPesquisa.add(tfPesquisar);
        tfPesquisar.getDocument().addDocumentListener(new DocumentListener() {
            private void filtrar() {
                String texto = tfPesquisar.getText().trim();
                if (texto.isEmpty())
                    sorter.setRowFilter(null);
                else
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto, 0));
            }

            public void insertUpdate(DocumentEvent e) {
                filtrar();
            }

            public void removeUpdate(DocumentEvent e) {
                filtrar();
            }

            public void changedUpdate(DocumentEvent e) {
                filtrar();
            }
        });

        panelWrapper.add(panelPesquisa, BorderLayout.NORTH);
        panelWrapper.add(scrollTabela, BorderLayout.CENTER);

        panelWrapper.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                int totalWidth = panelWrapper.getWidth();
                tabelaEscalas.getColumnModel().getColumn(0).setPreferredWidth((int) (totalWidth * 0.30));
                tabelaEscalas.getColumnModel().getColumn(1).setPreferredWidth((int) (totalWidth * 0.20));
                tabelaEscalas.getColumnModel().getColumn(2).setPreferredWidth((int) (totalWidth * 0.20));
                tabelaEscalas.getColumnModel().getColumn(3).setPreferredWidth((int) (totalWidth * 0.20));
                tabelaEscalas.getColumnModel().getColumn(4).setPreferredWidth((int) (totalWidth * 0.10));
            }
        });

        return panelWrapper;
    }

    private void limparCampos() {
        cbProfissional.setSelectedIndex(-1);
        for (int i = 0; i < modeloTabelaDias.getRowCount(); i++) {
            modeloTabelaDias.setValueAt(true, i, 1);
            modeloTabelaDias.setValueAt("08:00", i, 2);
            modeloTabelaDias.setValueAt("17:00", i, 3);
            modeloTabelaDias.setValueAt(true, i, 4);
        }
    }

    private void salvarEscalas() throws SQLException {
        Profissional prof = (Profissional) cbProfissional.getSelectedItem();
        if (prof == null) {
            JOptionPane.showMessageDialog(this, "Selecione um profissional!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        escalaController.removerTodasEscalasDoProfissional(prof.getId());

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        List<EscalaProfissional> escalas = new ArrayList<>();
        boolean peloMenosUmAtivo = false;

        for (int i = 0; i < modeloTabelaDias.getRowCount(); i++) {
            boolean ativo = (boolean) modeloTabelaDias.getValueAt(i, 1);
            if (!ativo)
                continue;
            peloMenosUmAtivo = true;

            String horaInicioStr = (String) modeloTabelaDias.getValueAt(i, 2);
            String horaFimStr = (String) modeloTabelaDias.getValueAt(i, 3);
            boolean disponivel = (boolean) modeloTabelaDias.getValueAt(i, 4);

            try {
                Date horaInicioDate = sdf.parse(horaInicioStr);
                Date horaFimDate = sdf.parse(horaFimStr);
                Time horaInicio = new Time(horaInicioDate.getTime());
                Time horaFim = new Time(horaFimDate.getTime());

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

        if (!peloMenosUmAtivo) {
            JOptionPane.showMessageDialog(this, "Selecione pelo menos um dia para cadastrar!", "Erro",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean sucesso = true;
        for (EscalaProfissional escala : escalas) {
            sucesso &= escalaController.criarEscala(escala, Sessao.getUsuarioLogado().getLogin());
        }

        if (sucesso) {
            JOptionPane.showMessageDialog(this, "Escalas atualizadas com sucesso!", "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);
            limparCampos();
            carregarEscalas();
        } else {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar algumas escalas.", "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

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
            carregarEscalasDoProfissionalSelecionado();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao excluir escala: " + e.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirTodasEscalasDoProfissional() {
        Profissional prof = (Profissional) cbProfissional.getSelectedItem();
        if (prof == null) {
            JOptionPane.showMessageDialog(this, "Selecione um profissional para excluir todas as escalas!", "Erro",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja excluir todas as escalas do profissional " + prof.getNome() + "?",
                "Confirmação", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION)
            return;

        escalaController.removerTodasEscalasDoProfissional(prof.getId());
        JOptionPane.showMessageDialog(this, "Todas as escalas do profissional foram excluídas com sucesso!", "Sucesso",
                JOptionPane.INFORMATION_MESSAGE);
        limparCampos();
        carregarEscalas();
    }

    private void carregarProfissionais() {
        try {
            ProfissionalController controller = new ProfissionalController();
            List<Profissional> profs = controller.listarTodos();
            cbProfissional.removeAllItems();
            for (Profissional p : profs)
                cbProfissional.addItem(p);
            cbProfissional.setSelectedIndex(-1);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao carregar profissionais: " + e.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarEscalas() {
        try {
            List<EscalaProfissional> escalas = escalaController.listarTodas();
            modeloTabelaEscalas.setRowCount(0);
            for (EscalaProfissional e : escalas) {
                if (e.getDiaSemana() < 0 || e.getDiaSemana() >= DIAS_SEMANA.length) {
                    continue;
                }
                Profissional p = new ProfissionalController().buscarPorId(e.getProfissionalId());
                modeloTabelaEscalas.addRow(new Object[] { p != null ? p.getNome() : "?", DIAS_SEMANA[e.getDiaSemana()],
                        e.getHoraInicio().toString(), e.getHoraFim().toString(), e.isDisponivel() ? "Sim" : "Não" });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao carregar escalas: " + e.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarEscalasDoProfissionalSelecionado() {
        Profissional prof = (Profissional) cbProfissional.getSelectedItem();
        if (prof == null) {
            limparCampos();
            return;
        }

        List<EscalaProfissional> escalas = escalaController.listarPorProfissional(prof.getId());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        for (int i = 0; i < modeloTabelaDias.getRowCount(); i++) {
            modeloTabelaDias.setValueAt(true, i, 1);
            modeloTabelaDias.setValueAt("08:00", i, 2);
            modeloTabelaDias.setValueAt("17:00", i, 3);
            modeloTabelaDias.setValueAt(true, i, 4);
        }

        for (EscalaProfissional e : escalas) {
            int dia = e.getDiaSemana();
            if (dia >= 0 && dia < DIAS_SEMANA.length) {
                modeloTabelaDias.setValueAt(true, dia, 1);
                modeloTabelaDias.setValueAt(sdf.format(e.getHoraInicio()), dia, 2);
                modeloTabelaDias.setValueAt(sdf.format(e.getHoraFim()), dia, 3);
                modeloTabelaDias.setValueAt(e.isDisponivel(), dia, 4);
            }
        }
    }
}