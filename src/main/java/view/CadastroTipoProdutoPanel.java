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
import controller.TipoProdutoController;
import model.TipoProduto;
import util.Sessao;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

// Painel para cadastro e listagem de tipos de produto
public class CadastroTipoProdutoPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    // Componentes de entrada
    private JTextField tfNome, tfPesquisar;
    private JTextArea taDescricao;
    private JButton btnSalvar, btnLimpar;

    // Componentes da tabela
    private JTable tabelaTipos;
    private DefaultTableModel modeloTabela;
    private TableRowSorter<DefaultTableModel> sorter;

    // Estilo visual
    private final Color primaryColor = new Color(138, 43, 226); // Roxo
    private final Color backgroundColor = new Color(245, 245, 245); // Fundo geral
    private final Color rowColorLightLilac = new Color(230, 230, 250); // Lilás claro para linhas pares
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 18);
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 14);

    // Construtor
    public CadastroTipoProdutoPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(5, 15, 15, 15));
        setBackground(backgroundColor);

        // Título do painel
        JLabel lblTitulo = new JLabel("Cadastro de Tipo de Produto", SwingConstants.CENTER);
        lblTitulo.setFont(titleFont);
        lblTitulo.setForeground(primaryColor);
        lblTitulo.setBorder(new EmptyBorder(5, 0, 10, 0));
        add(lblTitulo, BorderLayout.NORTH);

        // Painéis de cadastro e tabela
        JPanel panelCadastro = criarPainelCadastro();
        JPanel panelTabela = criarPainelTabela();

        // SplitPane para dividir cadastro e tabela
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelCadastro, panelTabela);
        splitPane.setResizeWeight(0.495);
        splitPane.setDividerSize(7);
        splitPane.setBackground(backgroundColor);

        add(splitPane, BorderLayout.CENTER);

        // Ações dos botões
        btnLimpar.addActionListener(e -> limparCampos());
        btnSalvar.addActionListener(e -> salvarTipoProduto());

        // Carregar dados iniciais
        carregarTipos();
    }

    // Cria o painel de cadastro
    private JPanel criarPainelCadastro() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(primaryColor, 1, true),
                        "Novo Tipo de Produto",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        labelFont,
                        primaryColor),
                new EmptyBorder(10, 10, 10, 10)));
        panel.setBackground(backgroundColor);

        JPanel mainGrid = new JPanel(new GridBagLayout());
        mainGrid.setBackground(backgroundColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Nome
        JLabel lblNome = new JLabel("Nome:");
        lblNome.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        mainGrid.add(lblNome, gbc);

        tfNome = new JTextField(20);
        tfNome.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        mainGrid.add(tfNome, gbc);

        // Descrição
        JLabel lblDescricao = new JLabel("Descrição:");
        lblDescricao.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        mainGrid.add(lblDescricao, gbc);

        taDescricao = new JTextArea(4, 20);
        taDescricao.setLineWrap(true);
        taDescricao.setWrapStyleWord(true);
        JScrollPane scrollDescricao = new JScrollPane(taDescricao);
        scrollDescricao.setPreferredSize(new Dimension(200, 100));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        mainGrid.add(scrollDescricao, gbc);

        // Botões (Limpar à esquerda, Salvar à direita)
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panelBotoes.setBackground(backgroundColor);
        btnLimpar = new JButton("Limpar");
        btnLimpar.setBackground(Color.LIGHT_GRAY);
        btnLimpar.setForeground(Color.BLACK);
        btnLimpar.setPreferredSize(new Dimension(100, 35));
        btnLimpar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSalvar = new JButton("Salvar");
        btnSalvar.setBackground(primaryColor);
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setPreferredSize(new Dimension(100, 35));
        btnSalvar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panelBotoes.add(btnLimpar);
        panelBotoes.add(btnSalvar);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        mainGrid.add(panelBotoes, gbc);

        panel.add(mainGrid, BorderLayout.NORTH);
        return panel;
    }

    // Cria o painel da tabela com pesquisa
    private JPanel criarPainelTabela() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(primaryColor, 1, true),
                        "Tipos de Produto Cadastrados",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        labelFont,
                        primaryColor),
                new EmptyBorder(10, 10, 10, 10)));
        panel.setBackground(backgroundColor);

        // Pesquisa
        JPanel panelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelBusca.setBackground(backgroundColor);
        JLabel lblPesquisar = new JLabel("Pesquisar Tipo:");
        lblPesquisar.setFont(labelFont);
        tfPesquisar = new JTextField(15);
        tfPesquisar.setPreferredSize(new Dimension(200, 30));
        panelBusca.add(lblPesquisar);
        panelBusca.add(tfPesquisar);

        tfPesquisar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filtrarTabela(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filtrarTabela(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filtrarTabela(); }

            private void filtrarTabela() {
                String texto = tfPesquisar.getText().trim();
                sorter.setRowFilter(texto.isEmpty() ? null : RowFilter.regexFilter("(?i)" + texto, 0));
            }
        });

        // Tabela
        String[] colunas = {"Nome", "Descrição"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabelaTipos = new JTable(modeloTabela);
        tabelaTipos.setRowHeight(25);
        tabelaTipos.setShowGrid(false);
        tabelaTipos.setIntercellSpacing(new Dimension(0, 0));
        tabelaTipos.setFont(labelFont);

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
        for (int i = 0; i < tabelaTipos.getColumnCount(); i++) {
            tabelaTipos.getColumnModel().getColumn(i).setCellRenderer(rowRenderer);
        }

        JTableHeader header = tabelaTipos.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.setBackground(primaryColor);
        header.setForeground(Color.WHITE);

        sorter = new TableRowSorter<>(modeloTabela);
        tabelaTipos.setRowSorter(sorter);

        JScrollPane scrollTabela = new JScrollPane(tabelaTipos);
        scrollTabela.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        panel.add(panelBusca, BorderLayout.NORTH);
        panel.add(scrollTabela, BorderLayout.CENTER);

        // Ajuste dinâmico das colunas
        panel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                int totalWidth = panel.getWidth();
                tabelaTipos.getColumnModel().getColumn(0).setPreferredWidth((int) (totalWidth * 0.3));
                tabelaTipos.getColumnModel().getColumn(1).setPreferredWidth((int) (totalWidth * 0.7));
            }
        });

        return panel;
    }

    // Limpa os campos do formulário
    private void limparCampos() {
        tfNome.setText("");
        taDescricao.setText("");
    }

    // Salva o tipo de produto no banco
    private void salvarTipoProduto() {
        try {
            String nome = tfNome.getText().trim();
            if (nome.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Preencha o nome do tipo de produto.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            TipoProduto tipo = new TipoProduto();
            tipo.setNome(nome);
            tipo.setDescricao(taDescricao.getText().trim());
            tipo.setUsuario(Sessao.getUsuarioLogado().getLogin());

            TipoProdutoController controller = new TipoProdutoController();
            TipoProduto salvo = controller.salvar(tipo);

            if (salvo != null) {
                JOptionPane.showMessageDialog(this, "Tipo de produto salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                limparCampos();
                carregarTipos();
            } else {
                JOptionPane.showMessageDialog(this, "Falha ao salvar o tipo de produto.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar tipo de produto: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Carrega os tipos de produto na tabela
    private void carregarTipos() {
        try {
            TipoProdutoController controller = new TipoProdutoController();
            List<TipoProduto> tipos = controller.listarTodos();
            modeloTabela.setRowCount(0);

            for (TipoProduto t : tipos) {
                modeloTabela.addRow(new Object[]{t.getNome(), t.getDescricao()});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar tipos: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}