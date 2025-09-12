package view;

import javax.swing.*;
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

    public CadastroEscalaProfissionalPanel() {
        setLayout(new BorderLayout(10, 10));

        JLabel lblTitulo = new JLabel("Cadastro de Agenda de Profissionais", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(30, 30, 60));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(lblTitulo, BorderLayout.NORTH);

        JPanel panelCadastro = criarPainelCadastro();
        JPanel panelTabela = criarTabelaComPesquisa();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelCadastro, panelTabela);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(5);
        splitPane.setDividerLocation(0.5);
        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.5));

        JPanel panelWrapper = new JPanel(new BorderLayout());
        panelWrapper.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        panelWrapper.add(splitPane, BorderLayout.CENTER);
        add(panelWrapper, BorderLayout.CENTER);

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

        cbProfissional.addActionListener(e -> carregarEscalasDoProfissionalSelecionado());

        carregarProfissionais();
        carregarEscalas();
    }

    private JPanel criarPainelCadastro() {
        JPanel panelWrapper = new JPanel(new BorderLayout());
        JPanel panelCadastro = new JPanel(new GridBagLayout());

        // TitledBorder preto
        panelCadastro.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK),
                "Escala do Profissional"));

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
                setText(value instanceof Profissional ? ((Profissional) value).getNome() : "");
                return this;
            }
        });
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCadastro.add(cbProfissional, gbc);

        // Painel de dias
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JScrollPane scrollDias = criarPainelDiasSemana();
        panelCadastro.add(scrollDias, gbc);

        // Botões
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSalvar = new JButton("Salvar");
        btnLimpar = new JButton("Limpar");
        btnExcluir = new JButton("Excluir");
        btnExcluirTodas = new JButton("Excluir Todas");
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
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCadastro.add(panelBotoes, gbc);

        // Cursor de mão para todos
        Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);

        // Botões
        btnSalvar.setCursor(handCursor);
        btnLimpar.setCursor(handCursor);
        btnExcluir.setCursor(handCursor);
        btnExcluirTodas.setCursor(handCursor);

        // JComboBoxes
        cbProfissional.setCursor(handCursor);

        // JRadioButtons e JComboBoxes dias
        for (int i = 0; i < DIAS_SEMANA.length; i++) {
            if (cbHoraInicio[i] != null) cbHoraInicio[i].setCursor(handCursor);
            if (cbHoraFim[i] != null) cbHoraFim[i].setCursor(handCursor);
            if (rbDisponivel[i][0] != null) rbDisponivel[i][0].setCursor(handCursor);
            if (rbDisponivel[i][1] != null) rbDisponivel[i][1].setCursor(handCursor);
        }

        return panelWrapper;
    }

    @SuppressWarnings("unchecked")
    private JScrollPane criarPainelDiasSemana() {
        JPanel panelDias = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.anchor = GridBagConstraints.CENTER;

        // Colunas
        String[] colunasDia = { "Dia", "Hora Início", "Hora Fim", "Disponível" };
        for (int i = 0; i < colunasDia.length; i++) {
            gbc.gridx = i;
            gbc.gridy = 0;
            panelDias.add(new JLabel(colunasDia[i]), gbc);
        }

        String[] horarios = gerarHorariosLimitados();

        for (int i = 0; i < DIAS_SEMANA.length; i++) {
            gbc.gridy = i + 1;

            // Dia
            gbc.gridx = 0;
            panelDias.add(new JLabel(DIAS_SEMANA[i]), gbc);

            // Hora Início
            gbc.gridx = 1;
            cbHoraInicio[i] = new JComboBox<>(horarios);
            cbHoraInicio[i].setSelectedItem("09:00");
            cbHoraInicio[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
            panelDias.add(cbHoraInicio[i], gbc);

            // Hora Fim
            gbc.gridx = 2;
            cbHoraFim[i] = new JComboBox<>(horarios);
            cbHoraFim[i].setSelectedItem("17:30");
            cbHoraFim[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
            panelDias.add(cbHoraFim[i], gbc);

            // Disponível
            gbc.gridx = 3;
            rbDisponivel[i][0] = new JRadioButton("Sim", true);
            rbDisponivel[i][1] = new JRadioButton("Não");
            rbDisponivel[i][0].setCursor(new Cursor(Cursor.HAND_CURSOR));
            rbDisponivel[i][1].setCursor(new Cursor(Cursor.HAND_CURSOR));
            bgDisponivel[i] = new ButtonGroup();
            bgDisponivel[i].add(rbDisponivel[i][0]);
            bgDisponivel[i].add(rbDisponivel[i][1]);
            JPanel pDisponivel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
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
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(panelDias.getPreferredSize());
        return scroll;
    }

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
        for (int i = 0; i < tabelaEscalas.getColumnCount(); i++)
            tabelaEscalas.getColumnModel().getColumn(i).setCellRenderer(centralizado);

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
                sorter.setRowFilter(texto.isEmpty() ? null : RowFilter.regexFilter("(?i)" + texto, 0));
            }

            public void insertUpdate(DocumentEvent e) { filtrar(); }
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            public void changedUpdate(DocumentEvent e) { filtrar(); }
        });

        panelWrapper.add(panelPesquisa, BorderLayout.NORTH);
        panelWrapper.add(scrollTabela, BorderLayout.CENTER);

        return panelWrapper;
    }

    private void limparCampos() {
        for (int i = 0; i < DIAS_SEMANA.length; i++) {
            cbHoraInicio[i].setSelectedItem("09:00");
            cbHoraFim[i].setSelectedItem("17:30");
            rbDisponivel[i][0].setSelected(true);
            cbHoraInicio[i].setEnabled(true);
            cbHoraFim[i].setEnabled(true);
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
