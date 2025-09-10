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
import java.sql.SQLException;
import java.sql.Time;
import java.util.List;

public class CadastroEscalaProfissionalPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private JComboBox<Profissional> cbProfissional;
    private JComboBox<String> cbDiaSemana;
    private JSpinner spHoraInicio, spHoraFim;
    private JCheckBox chkDisponivel;
    private JButton btnSalvar, btnLimpar, btnExcluir;
    private JTable tabelaEscalas;
    private DefaultTableModel modeloTabela;
    private TableRowSorter<DefaultTableModel> sorter;

    private EscalaProfissionalController escalaController = new EscalaProfissionalController();

    public CadastroEscalaProfissionalPanel() {
        setLayout(new BorderLayout(10, 20));

        // TÍTULO
        JLabel lblTitulo = new JLabel("Cadastro de Agenda de Profissionais", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(30, 30, 60));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        add(lblTitulo, BorderLayout.NORTH);

        // Criar painéis
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
            try { salvarEscala(); } 
            catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erro ao salvar escala: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnExcluir.addActionListener(e -> excluirEscalaSelecionada());

        carregarProfissionais();
        carregarEscalas();
    }

    private JPanel criarPainelCadastro() {
        JPanel panelWrapper = new JPanel(new BorderLayout());
        JPanel panelCadastro = new JPanel(new GridBagLayout());

        panelCadastro.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                "Cadastrar/Editar Escala",
                TitledBorder.LEADING,
                TitledBorder.TOP));
        panelWrapper.add(panelCadastro, BorderLayout.NORTH);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.anchor = GridBagConstraints.WEST;

        // Subtítulo
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel lblSubtitulo = new JLabel("Preencha os dados da agenda");
        lblSubtitulo.setFont(new Font("SansSerif", Font.ITALIC, 13));
        lblSubtitulo.setForeground(Color.DARK_GRAY);
        panelCadastro.add(lblSubtitulo, gbc);
        gbc.gridwidth = 1;

        // PROFISSIONAL
        gbc.gridx = 0; gbc.gridy = 1;
        panelCadastro.add(new JLabel("Profissional:"), gbc);
        cbProfissional = new JComboBox<>();
        cbProfissional.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Profissional) setText(((Profissional) value).getNome());
                else setText("");
                return this;
            }
        });
        gbc.gridx = 1; gbc.weightx = 0.5; gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCadastro.add(cbProfissional, gbc);

        // DIA DA SEMANA
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        panelCadastro.add(new JLabel("Dia da Semana:"), gbc);
        cbDiaSemana = new JComboBox<>(new String[]{"Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado", "Domingo"});
        gbc.gridx = 1; gbc.weightx = 0.5; gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCadastro.add(cbDiaSemana, gbc);

        // HORA INÍCIO
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        panelCadastro.add(new JLabel("Hora Início:"), gbc);
        spHoraInicio = new JSpinner(new SpinnerDateModel());
        spHoraInicio.setEditor(new JSpinner.DateEditor(spHoraInicio, "HH:mm"));
        gbc.gridx = 1; gbc.weightx = 0.5; gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCadastro.add(spHoraInicio, gbc);

        // HORA FIM
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        panelCadastro.add(new JLabel("Hora Fim:"), gbc);
        spHoraFim = new JSpinner(new SpinnerDateModel());
        spHoraFim.setEditor(new JSpinner.DateEditor(spHoraFim, "HH:mm"));
        gbc.gridx = 1; gbc.weightx = 0.5; gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCadastro.add(spHoraFim, gbc);

        // DISPONÍVEL
        gbc.gridx = 0; gbc.gridy = 5;
        panelCadastro.add(new JLabel("Disponível:"), gbc);
        chkDisponivel = new JCheckBox();
        chkDisponivel.setSelected(true);
        gbc.gridx = 1; gbc.weightx = 0.5; gbc.fill = GridBagConstraints.NONE;
        panelCadastro.add(chkDisponivel, gbc);

        // BOTÕES
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSalvar = new JButton("Salvar");
        btnLimpar = new JButton("Limpar");
        btnExcluir = new JButton("Excluir");
        panelBotoes.add(btnSalvar);
        panelBotoes.add(btnLimpar);
        panelBotoes.add(btnExcluir);

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.EAST;
        panelCadastro.add(panelBotoes, gbc);

        Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
        btnSalvar.setCursor(handCursor);
        btnLimpar.setCursor(handCursor);
        btnExcluir.setCursor(handCursor);

        return panelWrapper;
    }

    private JPanel criarTabelaComPesquisa() {
        JPanel panelWrapper = new JPanel(new BorderLayout());

        String[] colunas = {"ID", "Profissional", "Dia", "Hora Início", "Hora Fim", "Disponível"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        tabelaEscalas = new JTable(modeloTabela);
        tabelaEscalas.setFillsViewportHeight(true);

        DefaultTableCellRenderer centralizado = new DefaultTableCellRenderer();
        centralizado.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tabelaEscalas.getColumnCount(); i++)
            tabelaEscalas.getColumnModel().getColumn(i).setCellRenderer(centralizado);

        sorter = new TableRowSorter<>(modeloTabela);
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
                if (texto.isEmpty()) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto, 1));
            }
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            public void changedUpdate(DocumentEvent e) { filtrar(); }
        });

        panelWrapper.add(panelPesquisa, BorderLayout.NORTH);
        panelWrapper.add(scrollTabela, BorderLayout.CENTER);

        panelWrapper.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                int totalWidth = panelWrapper.getWidth();
                tabelaEscalas.getColumnModel().getColumn(0).setPreferredWidth((int)(totalWidth*0.05));
                tabelaEscalas.getColumnModel().getColumn(1).setPreferredWidth((int)(totalWidth*0.25));
                tabelaEscalas.getColumnModel().getColumn(2).setPreferredWidth((int)(totalWidth*0.15));
                tabelaEscalas.getColumnModel().getColumn(3).setPreferredWidth((int)(totalWidth*0.15));
                tabelaEscalas.getColumnModel().getColumn(4).setPreferredWidth((int)(totalWidth*0.15));
                tabelaEscalas.getColumnModel().getColumn(5).setPreferredWidth((int)(totalWidth*0.10));
            }
        });

        return panelWrapper;
    }

    private void limparCampos() {
        cbProfissional.setSelectedIndex(-1);
        cbDiaSemana.setSelectedIndex(0);
        spHoraInicio.setValue(new java.util.Date());
        spHoraFim.setValue(new java.util.Date());
        chkDisponivel.setSelected(true);
    }

    private void salvarEscala() throws SQLException {
        Profissional prof = (Profissional) cbProfissional.getSelectedItem();
        if (prof == null) {
            JOptionPane.showMessageDialog(this, "Selecione um profissional!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int diaSemana = cbDiaSemana.getSelectedIndex();
        Time horaInicio = new Time(((java.util.Date) spHoraInicio.getValue()).getTime());
        Time horaFim = new Time(((java.util.Date) spHoraFim.getValue()).getTime());

        if (horaFim.before(horaInicio)) {
            JOptionPane.showMessageDialog(this, "Hora de término deve ser depois da hora de início.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        EscalaProfissional escala = new EscalaProfissional();
        escala.setProfissionalId(prof.getId());
        escala.setDiaSemana(diaSemana);
        escala.setHoraInicio(horaInicio);
        escala.setHoraFim(horaFim);
        escala.setDisponivel(chkDisponivel.isSelected());

        boolean sucesso = escalaController.criarEscala(escala, Sessao.getUsuarioLogado().getLogin());
        if (sucesso) {
            JOptionPane.showMessageDialog(this, "Escala salva com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            limparCampos();
            carregarEscalas();
        }
    }

    private void excluirEscalaSelecionada() {
        int linha = tabelaEscalas.getSelectedRow();
        if (linha == -1) return;
        int idEscala = (int) tabelaEscalas.getValueAt(tabelaEscalas.convertRowIndexToModel(linha), 0);

        try {
            escalaController.removerEscala(idEscala);
            JOptionPane.showMessageDialog(this, "Escala excluída com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            carregarEscalas();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao excluir escala: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarProfissionais() {
        try {
            ProfissionalController controller = new ProfissionalController();
            List<Profissional> profs = controller.listarTodos();
            cbProfissional.removeAllItems();
            for (Profissional p : profs) cbProfissional.addItem(p);
            cbProfissional.setSelectedIndex(-1);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao carregar profissionais: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarEscalas() {
        try {
            List<EscalaProfissional> escalas = escalaController.listarTodas();
            modeloTabela.setRowCount(0);
            for (EscalaProfissional e : escalas) {
                Profissional p = new ProfissionalController().buscarPorId(e.getProfissionalId());
                modeloTabela.addRow(new Object[]{
                        e.getId(),
                        p != null ? p.getNome() : "?",
                        cbDiaSemana.getItemAt(e.getDiaSemana()),
                        e.getHoraInicio().toString(),
                        e.getHoraFim().toString(),
                        e.isDisponivel() ? "Sim" : "Não"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao carregar escalas: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
