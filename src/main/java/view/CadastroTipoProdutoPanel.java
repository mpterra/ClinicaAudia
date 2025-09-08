package view;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import controller.TipoProdutoController;
import model.TipoProduto;
import util.Sessao;

import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CadastroTipoProdutoPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private JTextField tfNome, tfPesquisar;
    private JTextArea taDescricao;
    private JButton btnSalvar, btnLimpar;

    private JTable tabelaTipos;
    private DefaultTableModel modeloTabela;
    private TableRowSorter<DefaultTableModel> sorter;

    public CadastroTipoProdutoPanel() {
        setLayout(new BorderLayout(10, 20));

        // TÍTULO elegante no topo
        JLabel lblTitulo = new JLabel("Cadastro de Tipo de Produto", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(30, 30, 60));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        add(lblTitulo, BorderLayout.NORTH);

        // Criar os painéis
        JPanel panelCadastro = criarPainelCadastro();
        JPanel panelTabela = criarTabelaComPesquisa();

        // Divisor horizontal: 50% cadastro, 50% tabela
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelCadastro, panelTabela);
        splitPane.setResizeWeight(0.5);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(5);

        // Wrapper para espaçamento
        JPanel panelWrapper = new JPanel(new BorderLayout());
        panelWrapper.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        panelWrapper.add(splitPane, BorderLayout.CENTER);

        add(panelWrapper, BorderLayout.CENTER);

        // Listeners dos botões
        btnLimpar.addActionListener(e -> limparCampos());
        btnSalvar.addActionListener(e -> {
            try {
                salvarTipoProduto();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erro ao salvar tipo de produto: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        carregarTipos();
    }

    private JPanel criarPainelCadastro() {
        JPanel panelCadastroWrapper = new JPanel(new BorderLayout());
        JPanel panelCadastro = new JPanel(new GridBagLayout());

        panelCadastro.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                "Cadastrar novo tipo de produto",
                TitledBorder.LEADING,
                TitledBorder.TOP));

        panelCadastroWrapper.add(panelCadastro, BorderLayout.NORTH);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // SUBTÍTULO elegante dentro do painel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel lblSubtitulo = new JLabel("Preencha os dados do tipo de produto");
        lblSubtitulo.setFont(new Font("SansSerif", Font.ITALIC, 13));
        lblSubtitulo.setForeground(Color.DARK_GRAY);
        panelCadastro.add(lblSubtitulo, gbc);
        gbc.gridwidth = 1;

        // NOME
        gbc.gridx = 0;
        gbc.gridy = 1;
        panelCadastro.add(new JLabel("Nome:"), gbc);

        tfNome = new JTextField();
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCadastro.add(tfNome, gbc);

        // DESCRIÇÃO
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panelCadastro.add(new JLabel("Descrição:"), gbc);

        taDescricao = new JTextArea(4, 20);
        taDescricao.setLineWrap(true);
        taDescricao.setWrapStyleWord(true);
        JScrollPane scrollDescricao = new JScrollPane(taDescricao);
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCadastro.add(scrollDescricao, gbc);

        // BOTOES
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSalvar = new JButton("Salvar");
        btnLimpar = new JButton("Limpar");
        panelBotoes.add(btnSalvar);
        panelBotoes.add(btnLimpar);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.EAST;
        panelCadastro.add(panelBotoes, gbc);

        // Cursor de mãozinha
        Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
        btnSalvar.setCursor(handCursor);
        btnLimpar.setCursor(handCursor);

        return panelCadastroWrapper;
    }

    private JPanel criarTabelaComPesquisa() {
        JPanel panelTabelaWrapper = new JPanel(new BorderLayout());

        String[] colunas = {"Nome", "Descrição", "Criado", "Atualizado", "Usuário"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaTipos = new JTable(modeloTabela);
        tabelaTipos.setFillsViewportHeight(true);

        DefaultTableCellRenderer centralizado = new DefaultTableCellRenderer();
        centralizado.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tabelaTipos.getColumnCount(); i++) {
            tabelaTipos.getColumnModel().getColumn(i).setCellRenderer(centralizado);
        }

        sorter = new TableRowSorter<>(modeloTabela);
        tabelaTipos.setRowSorter(sorter);

        JScrollPane scrollTabela = new JScrollPane(tabelaTipos);

        JPanel panelPesquisa = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblPesquisar = new JLabel("Pesquisar tipo:");
        lblPesquisar.setFont(new Font("SansSerif", Font.ITALIC, 14));
        lblPesquisar.setForeground(Color.DARK_GRAY);
        panelPesquisa.add(lblPesquisar);

        tfPesquisar = new JTextField(20);
        panelPesquisa.add(tfPesquisar);
        tfPesquisar.getDocument().addDocumentListener(new DocumentListener() {
            private void filtrar() {
                String texto = tfPesquisar.getText().trim();
                if (texto.isEmpty()) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto, 0));
            }
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            public void changedUpdate(DocumentEvent e) { filtrar(); }
        });

        panelTabelaWrapper.add(panelPesquisa, BorderLayout.NORTH);
        panelTabelaWrapper.add(scrollTabela, BorderLayout.CENTER);

        panelTabelaWrapper.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                int totalWidth = panelTabelaWrapper.getWidth();
                tabelaTipos.getColumnModel().getColumn(0).setPreferredWidth((int)(totalWidth * 0.2));
                tabelaTipos.getColumnModel().getColumn(1).setPreferredWidth((int)(totalWidth * 0.5));
                tabelaTipos.getColumnModel().getColumn(2).setPreferredWidth((int)(totalWidth * 0.15));
                tabelaTipos.getColumnModel().getColumn(3).setPreferredWidth((int)(totalWidth * 0.15));
                tabelaTipos.getColumnModel().getColumn(4).setPreferredWidth((int)(totalWidth * 0.15));
            }
        });

        return panelTabelaWrapper;
    }

    private void limparCampos() {
        tfNome.setText("");
        taDescricao.setText("");
    }

    private void salvarTipoProduto() throws SQLException {
        String nome = tfNome.getText().trim();
        String descricao = taDescricao.getText().trim();

        if (nome.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha o nome do tipo de produto!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        TipoProduto tipo = new TipoProduto();
        tipo.setNome(nome);
        tipo.setDescricao(descricao);
        tipo.setUsuario(Sessao.getUsuarioLogado().getLogin()); // Exemplo de usuário logado

        TipoProdutoController controller = new TipoProdutoController();
        TipoProduto salvo = controller.salvar(tipo);

        if (salvo != null) {
            JOptionPane.showMessageDialog(this, "Tipo de produto salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            limparCampos();
            carregarTipos();
        }
    }

    private void carregarTipos() {
        try {
            TipoProdutoController controller = new TipoProdutoController();
            List<TipoProduto> tipos = controller.listarTodos();

            modeloTabela.setRowCount(0);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (TipoProduto t : tipos) {
                String criado = t.getCriadoEm() != null ? t.getCriadoEm().format(formatter) : "";
                String atualizado = t.getAtualizadoEm() != null ? t.getAtualizadoEm().format(formatter) : "";

                modeloTabela.addRow(new Object[]{
                        t.getNome(),
                        t.getDescricao(),
                        criado,
                        atualizado,
                        t.getUsuario() != null ? t.getUsuario() : "?"
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao carregar tipos: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
