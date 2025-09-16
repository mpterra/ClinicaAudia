package view.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.text.StyledEditorKit;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import controller.AtendimentoController;
import controller.EvolucaoAtendimentoController;
import controller.PacienteController;
import model.Atendimento;
import model.EvolucaoAtendimento;
import model.Paciente;
import util.Sessao;

// Diálogo para exibir detalhes do atendimento e histórico do paciente
public class PacienteAtendimentoDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final Atendimento atendimento;
    private final AtendimentoController atendimentoController = new AtendimentoController();
    private final EvolucaoAtendimentoController evolucaoController = new EvolucaoAtendimentoController();
    private final PacienteController pacienteController = new PacienteController();

    private JTextArea txtObservacoesAtendimento;
    private JEditorPane txtEvolucaoNotas; // Editor para notas da evolução com formatação
    private List<EvolucaoComponent> listaEvolucoesArquivos; // Apenas para arquivos
    private JPanel panelEvolucoesArquivos;
    private JTable tabelaHistorico;
    private DefaultTableModel modeloHistorico;
    private EvolucaoAtendimento evolucaoTextoExistente; // Para gerenciar a evolução de texto existente
    private JComboBox<Atendimento.Situacao> cbSituacao; // ComboBox para status do atendimento
    private final DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final Color primaryColor = new Color(30, 144, 255);
    private final Color backgroundColor = new Color(245, 245, 245);
    private final Color textAreaBackground = Color.WHITE; // Fundo branco padrão para campos editáveis
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 14);
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 18);
    private final Font buttonFont = new Font("SansSerif", Font.PLAIN, 12); // Fonte menor para botões

    // Classe interna para componentes de evolução (apenas arquivos)
    private static class EvolucaoComponent {
        EvolucaoAtendimento evo;
        JPanel panel;
        JLabel lblArquivo; // Para tipo arquivo
        String tipo; // "arquivo"

        EvolucaoComponent(EvolucaoAtendimento evo, String tipo) {
            this.evo = evo;
            this.tipo = tipo;
            this.panel = new JPanel(new BorderLayout(5, 5));
            this.panel.setBackground(new Color(245, 245, 245));
        }
    }

    // Classe para renderizar cores no JComboBox
    private static class ColorComboBoxRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof ColorItem) {
                ColorItem colorItem = (ColorItem) value;
                label.setText(colorItem.name);
                label.setIcon(new ColorIcon(colorItem.color));
                label.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
                label.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            }
            return label;
        }
    }

    // Classe para representar itens de cor no JComboBox
    private static class ColorItem {
        String name;
        Color color;

        ColorItem(String name, Color color) {
            this.name = name;
            this.color = color;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    // Classe para criar ícone de amostra de cor
    private static class ColorIcon implements Icon {
        private final Color color;
        private final int width = 16;
        private final int height = 16;

        ColorIcon(Color color) {
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(color);
            g2d.fillRect(x, y, width, height);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(x, y, width - 1, height - 1);
            g2d.dispose();
        }

        @Override
        public int getIconWidth() {
            return width;
        }

        @Override
        public int getIconHeight() {
            return height;
        }
    }

    public PacienteAtendimentoDialog(Frame parent, Atendimento atendimento) {
        super(parent, "Detalhes do Atendimento e Paciente", true);
        this.atendimento = atendimento;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true); // Permite redimensionamento
        setSize(650, 700); // Tamanho padrão 650x700
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(backgroundColor);

        listaEvolucoesArquivos = new ArrayList<>();
        initComponents();
        carregarDados();
    }

    // Inicializa os componentes da interface
    private void initComponents() {
        // Painel principal
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(backgroundColor);

        // Título
        JLabel lblTitulo = new JLabel("Detalhes do Atendimento e Paciente", SwingConstants.CENTER);
        lblTitulo.setFont(titleFont);
        lblTitulo.setForeground(primaryColor);
        lblTitulo.setBorder(new EmptyBorder(0, 0, 20, 0));
        add(lblTitulo, BorderLayout.NORTH);

        // Abas com hand cursor
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(labelFont);
        tabbedPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        tabbedPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                tabbedPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (!tabbedPane.isCursorSet()) {
                    tabbedPane.setCursor(Cursor.getDefaultCursor());
                }
            }
        });
        JScrollPane atendimentoScrollPane = new JScrollPane(criarPainelAtendimentoAtual(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        atendimentoScrollPane.getVerticalScrollBar().setUnitIncrement(32); // Scroll mais rápido
        tabbedPane.addTab("Atendimento Atual", atendimentoScrollPane);
        tabbedPane.addTab("Histórico do Paciente", criarPainelHistorico());
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(backgroundColor);

        JButton btnSalvar = new JButton("Salvar");
        btnSalvar.setBackground(primaryColor);
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setFont(buttonFont);
        btnSalvar.setPreferredSize(new Dimension(100, 35));
        btnSalvar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setBackground(Color.LIGHT_GRAY);
        btnCancelar.setForeground(Color.BLACK);
        btnCancelar.setFont(buttonFont);
        btnCancelar.setPreferredSize(new Dimension(100, 35));
        btnCancelar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        buttonPanel.add(btnCancelar);
        buttonPanel.add(btnSalvar);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER); // Removido scroll extra para simplificar

        btnSalvar.addActionListener(e -> salvar());
        btnCancelar.addActionListener(e -> dispose());
    }

    // Cria o painel da aba "Atendimento Atual"
    private JPanel criarPainelAtendimentoAtual() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(backgroundColor);

        // Dados do paciente
        JPanel pacientePanel = new JPanel(new GridBagLayout());
        pacientePanel.setBackground(backgroundColor);
        pacientePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(primaryColor), "Dados do Paciente",
                        TitledBorder.CENTER, TitledBorder.TOP, labelFont, primaryColor),
                new EmptyBorder(15, 15, 15, 15)));
        GridBagConstraints gbcP = new GridBagConstraints();
        gbcP.insets = new Insets(5, 0, 5, 0);
        gbcP.anchor = GridBagConstraints.CENTER;

        JLabel lblNomePaciente = new JLabel();
        lblNomePaciente.setFont(new Font("SansSerif", Font.BOLD, 16));
        gbcP.gridx = 0;
        gbcP.gridy = 0;
        pacientePanel.add(lblNomePaciente, gbcP);

        JLabel lblTelefone = new JLabel();
        lblTelefone.setFont(labelFont);
        gbcP.gridy = 1;
        pacientePanel.add(lblTelefone, gbcP);

        JLabel lblIdade = new JLabel();
        lblIdade.setFont(labelFont);
        gbcP.gridy = 2;
        pacientePanel.add(lblIdade, gbcP);

        JLabel lblEmail = new JLabel();
        lblEmail.setFont(labelFont);
        gbcP.gridy = 3;
        pacientePanel.add(lblEmail, gbcP);

        panel.add(pacientePanel, BorderLayout.NORTH);

        // Seção principal de formulário com espaçamento limpo
        JPanel formPanel = new JPanel(new BorderLayout(15, 15));
        formPanel.setBackground(backgroundColor);

        // Status do atendimento
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(backgroundColor);
        JLabel lblStatus = new JLabel("Status do Atendimento: ");
        lblStatus.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblStatus.setForeground(primaryColor);
        cbSituacao = new JComboBox<>(Atendimento.Situacao.values());
        cbSituacao.setFont(labelFont);
        cbSituacao.setBackground(textAreaBackground);
        cbSituacao.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        cbSituacao.setPreferredSize(new Dimension(150, 30));
        cbSituacao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Define o status atual do atendimento, se disponível
        if (atendimento.getSituacao() != null) {
            cbSituacao.setSelectedItem(atendimento.getSituacao());
        }
        statusPanel.add(lblStatus);
        statusPanel.add(cbSituacao);
        formPanel.add(statusPanel, BorderLayout.NORTH);

        // Observações do atendimento
        JPanel obsPanel = new JPanel(new BorderLayout(10, 10));
        obsPanel.setBackground(backgroundColor);
        JLabel lblObservacoes = new JLabel("Observações do Atendimento");
        lblObservacoes.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblObservacoes.setForeground(primaryColor);
        lblObservacoes.setBorder(new EmptyBorder(0, 0, 10, 0)); // Espaçamento abaixo
        obsPanel.add(lblObservacoes, BorderLayout.NORTH);
        txtObservacoesAtendimento = new JTextArea(5, 30);
        txtObservacoesAtendimento.setLineWrap(true);
        txtObservacoesAtendimento.setWrapStyleWord(true);
        txtObservacoesAtendimento.setBackground(textAreaBackground);
        txtObservacoesAtendimento.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1)); // Borda sutil
        JScrollPane scrollObs = new JScrollPane(txtObservacoesAtendimento);
        scrollObs.setBackground(backgroundColor);
        scrollObs.setBorder(BorderFactory.createEmptyBorder());
        scrollObs.getVerticalScrollBar().setUnitIncrement(32); // Scroll mais rápido
        obsPanel.add(scrollObs, BorderLayout.CENTER);
        formPanel.add(obsPanel, BorderLayout.CENTER);

        // Evoluções
        JPanel evolucoesPanel = new JPanel(new BorderLayout(15, 15));
        evolucoesPanel.setBackground(backgroundColor);

        // Notas da evolução com barra de ferramentas
        JPanel notasPanel = new JPanel(new BorderLayout(10, 10));
        notasPanel.setBackground(backgroundColor);
        JLabel lblNotasEvolucao = new JLabel("Notas da Evolução");
        lblNotasEvolucao.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblNotasEvolucao.setForeground(primaryColor);
        lblNotasEvolucao.setBorder(new EmptyBorder(0, 0, 10, 0)); // Espaçamento abaixo
        notasPanel.add(lblNotasEvolucao, BorderLayout.NORTH);

        // Barra de ferramentas para formatação
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(backgroundColor);
        toolBar.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1)); // Borda sutil

        // Botão Negrito
        JButton btnBold = new JButton("N");
        btnBold.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnBold.setToolTipText("Negrito");
        btnBold.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBold.setBackground(backgroundColor);
        btnBold.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btnBold.addActionListener(new StyledEditorKit.BoldAction());

        // Botão Itálico
        JButton btnItalic = new JButton("I");
        btnItalic.setFont(new Font("SansSerif", Font.ITALIC, 14));
        btnItalic.setToolTipText("Itálico");
        btnItalic.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnItalic.setBackground(backgroundColor);
        btnItalic.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btnItalic.addActionListener(new StyledEditorKit.ItalicAction());

        // Label e ComboBox para tamanho da fonte
        JLabel lblFontSize = new JLabel("Tamanho da Fonte:");
        lblFontSize.setFont(labelFont);
        lblFontSize.setForeground(Color.BLACK);
        JComboBox<String> fontSizeCombo = new JComboBox<>(new String[]{"12", "14", "16", "18", "20"});
        fontSizeCombo.setMaximumSize(new Dimension(60, 30));
        fontSizeCombo.setToolTipText("Tamanho da Fonte");
        fontSizeCombo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        fontSizeCombo.setBackground(textAreaBackground);
        fontSizeCombo.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        fontSizeCombo.setSelectedItem("14"); // Tamanho padrão
        fontSizeCombo.addActionListener(e -> {
            String size = (String) fontSizeCombo.getSelectedItem();
            String currentText = txtEvolucaoNotas.getText();
            String newText = "<html><body style='font-family: SansSerif; font-size: " + size + "px;'>" + 
                             stripBodyContent(currentText) + "</body></html>";
            txtEvolucaoNotas.setText(newText);
        });

        // ComboBox para cores
        ColorItem[] colors = {
            new ColorItem("Preto", Color.BLACK),
            new ColorItem("Azul", Color.BLUE),
            new ColorItem("Vermelho", Color.RED),
            new ColorItem("Verde", new Color(0, 128, 0)),
            new ColorItem("Cinza", Color.GRAY),
            new ColorItem("Laranja", new Color(255, 140, 0)),
            new ColorItem("Roxo", new Color(128, 0, 128)),
            new ColorItem("Marrom", new Color(139, 69, 19))
        };
        JComboBox<ColorItem> colorCombo = new JComboBox<>(colors);
        colorCombo.setMaximumSize(new Dimension(100, 30));
        colorCombo.setToolTipText("Cor do Texto");
        colorCombo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        colorCombo.setBackground(textAreaBackground);
        colorCombo.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        colorCombo.setRenderer(new ColorComboBoxRenderer());
        colorCombo.addActionListener(e -> {
            ColorItem selectedColor = (ColorItem) colorCombo.getSelectedItem();
            if (selectedColor != null) {
                new StyledEditorKit.ForegroundAction("Color", selectedColor.color).actionPerformed(null);
            }
        });

        toolBar.add(btnBold);
        toolBar.add(Box.createHorizontalStrut(5)); // Espaçamento entre botões
        toolBar.add(btnItalic);
        toolBar.add(Box.createHorizontalStrut(10));
        toolBar.add(lblFontSize);
        toolBar.add(Box.createHorizontalStrut(5));
        toolBar.add(fontSizeCombo);
        toolBar.add(Box.createHorizontalStrut(10));
        toolBar.add(colorCombo);
        notasPanel.add(toolBar, BorderLayout.CENTER);

        txtEvolucaoNotas = new JEditorPane();
        txtEvolucaoNotas.setContentType("text/html");
        HTMLEditorKit editorKit = new HTMLEditorKit();
        txtEvolucaoNotas.setEditorKit(editorKit);
        txtEvolucaoNotas.setBackground(textAreaBackground);
        txtEvolucaoNotas.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1)); // Borda sutil
        txtEvolucaoNotas.setPreferredSize(new Dimension(0, 120)); // Tamanho padrão (8 linhas)
        // Define tamanho padrão da fonte como 14px
        txtEvolucaoNotas.setText("<html><body style='font-family: SansSerif; font-size: 14px;'></body></html>");
        JScrollPane scrollNotas = new JScrollPane(txtEvolucaoNotas);
        scrollNotas.setBackground(backgroundColor);
        scrollNotas.setBorder(BorderFactory.createEmptyBorder());
        scrollNotas.getVerticalScrollBar().setUnitIncrement(32); // Scroll mais rápido
        notasPanel.add(scrollNotas, BorderLayout.SOUTH);
        evolucoesPanel.add(notasPanel, BorderLayout.NORTH);

        // Arquivos de evolução
        JPanel arquivosPanel = new JPanel(new BorderLayout(10, 10));
        arquivosPanel.setBackground(backgroundColor);

        // Painel para o rótulo e botão de anexar
        JPanel headerArquivosPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        headerArquivosPanel.setBackground(backgroundColor);
        JLabel lblEvolucoes = new JLabel("Arquivos Anexados");
        lblEvolucoes.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblEvolucoes.setForeground(primaryColor);
        headerArquivosPanel.add(lblEvolucoes);

        JButton btnAnexar = new JButton("Anexar Arquivo");
        btnAnexar.setBackground(Color.LIGHT_GRAY); // Cor cinza padrão do sistema
        btnAnexar.setForeground(Color.BLACK);
        btnAnexar.setFont(buttonFont); // Fonte menor
        btnAnexar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAnexar.setPreferredSize(new Dimension(110, 28)); // Tamanho ajustado
        btnAnexar.addActionListener(e -> adicionarEvolucaoArquivo());
        headerArquivosPanel.add(btnAnexar);

        arquivosPanel.add(headerArquivosPanel, BorderLayout.NORTH);

        // Painel para lista de arquivos
        panelEvolucoesArquivos = new JPanel();
        panelEvolucoesArquivos.setLayout(new BoxLayout(panelEvolucoesArquivos, BoxLayout.Y_AXIS));
        panelEvolucoesArquivos.setBackground(backgroundColor);
        JScrollPane scrollEvolucoes = new JScrollPane(panelEvolucoesArquivos);
        scrollEvolucoes.setBackground(backgroundColor);
        scrollEvolucoes.setBorder(BorderFactory.createEmptyBorder());
        scrollEvolucoes.getVerticalScrollBar().setUnitIncrement(32); // Scroll mais rápido
        arquivosPanel.add(scrollEvolucoes, BorderLayout.CENTER);

        evolucoesPanel.add(arquivosPanel, BorderLayout.SOUTH);

        formPanel.add(evolucoesPanel, BorderLayout.SOUTH);

        panel.add(formPanel, BorderLayout.CENTER);

        // Preenche os campos do paciente
        try {
            Paciente paciente = atendimento.getPaciente();
            if (paciente != null && paciente.getId() > 0) {
                paciente = pacienteController.buscarPorId(paciente.getId());
                lblNomePaciente.setText("Nome: " + (paciente.getNome() != null ? paciente.getNome() : "Não informado"));
                lblTelefone.setText("Telefone: " + (paciente.getTelefone() != null ? paciente.getTelefone() : "Não informado"));
                long idade = paciente.getDataNascimento() != null
                        ? java.time.temporal.ChronoUnit.YEARS.between(paciente.getDataNascimento(), java.time.LocalDate.now())
                        : 0;
                lblIdade.setText("Idade: " + (idade > 0 ? idade : "Não informada"));
                lblEmail.setText("Email: " + (paciente.getEmail() != null ? paciente.getEmail() : "Não informado"));
            } else {
                lblNomePaciente.setText("Nome: Não informado");
                lblTelefone.setText("Telefone: Não informado");
                lblIdade.setText("Idade: Não informada");
                lblEmail.setText("Email: Não informado");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados do paciente: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }

        return panel;
    }

    // Método auxiliar para extrair conteúdo do corpo do HTML
    private String stripBodyContent(String htmlText) {
        String content = htmlText;
        if (content.startsWith("<html><body")) {
            content = content.replaceFirst("<html><body[^>]*>", "");
            content = content.replaceFirst("</body></html>", "");
        }
        return content;
    }

    // Cria o painel da aba "Histórico do Paciente"
    private JPanel criarPainelHistorico() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(backgroundColor);

        // Tabela de histórico
        String[] colunas = {"Data/Hora", "Profissional", "Tipo", "Situação"};
        modeloHistorico = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tabelaHistorico = new JTable(modeloHistorico);
        tabelaHistorico.setFont(labelFont);
        tabelaHistorico.setRowHeight(25);
        tabelaHistorico.setShowGrid(false);
        tabelaHistorico.setBackground(backgroundColor);
        tabelaHistorico.getTableHeader().setBackground(primaryColor);
        tabelaHistorico.getTableHeader().setForeground(Color.WHITE);
        tabelaHistorico.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        tabelaHistorico.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1)); // Borda sutil na tabela

        // Duplo clique para abrir EvolucaoDialog
        tabelaHistorico.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tabelaHistorico.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        String dataHoraStr = (String) tabelaHistorico.getValueAt(row, 0);
                        try {
                            Atendimento selectedAtendimento = atendimentoController.listarTodos().stream()
                                    .filter(a -> a.getDataHora().toLocalDateTime().format(formatoData).equals(dataHoraStr))
                                    .findFirst()
                                    .orElse(null);
                            if (selectedAtendimento != null) {
                                new HistoricoEvolucaoDialog((Frame) SwingUtilities.getWindowAncestor(PacienteAtendimentoDialog.this), selectedAtendimento, pacienteController.buscarPorId(selectedAtendimento.getPacienteId())).setVisible(true);
                            }
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(PacienteAtendimentoDialog.this, "Erro ao abrir evolução: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });
        tabelaHistorico.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tabelaHistorico.getColumnCount(); i++) {
            tabelaHistorico.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollTabela = new JScrollPane(tabelaHistorico);
        scrollTabela.setBackground(backgroundColor);
        scrollTabela.setBorder(BorderFactory.createEmptyBorder());
        scrollTabela.getVerticalScrollBar().setUnitIncrement(32); // Scroll mais rápido
        panel.add(scrollTabela, BorderLayout.CENTER);

        // Ajustar largura das colunas proporcionalmente
        tabelaHistorico.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int larguraTotal = tabelaHistorico.getWidth();
                if (larguraTotal > 0) {
                    tabelaHistorico.getColumnModel().getColumn(0).setPreferredWidth((int)(larguraTotal * 0.25));
                    tabelaHistorico.getColumnModel().getColumn(1).setPreferredWidth((int)(larguraTotal * 0.30));
                    tabelaHistorico.getColumnModel().getColumn(2).setPreferredWidth((int)(larguraTotal * 0.20));
                    tabelaHistorico.getColumnModel().getColumn(3).setPreferredWidth((int)(larguraTotal * 0.25));
                }
            }
        });

        return panel;
    }

    // Carrega os dados iniciais do atendimento e histórico
    private void carregarDados() {
        // Carrega observações do atendimento
        txtObservacoesAtendimento.setText(atendimento.getNotas() != null ? atendimento.getNotas() : "");

        // Carrega evoluções existentes
        try {
            List<EvolucaoAtendimento> evolucoes = evolucaoController.listarPorAtendimento(atendimento.getId());
            for (EvolucaoAtendimento evo : evolucoes) {
                if (evo.getNotas() != null && !evo.getNotas().isEmpty()) {
                    evolucaoTextoExistente = evo;
                    txtEvolucaoNotas.setText(evo.getNotas());
                } else if (evo.getArquivo() != null && !evo.getArquivo().isEmpty()) {
                    adicionarEvolucaoArquivo(evo);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar evoluções: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }

        // Carrega histórico do paciente
        try {
            List<Atendimento> atendimentos = atendimentoController.listarTodos().stream()
                    .filter(a -> a.getPacienteId() == atendimento.getPacienteId())
                    .collect(Collectors.toList());
            for (Atendimento at : atendimentos) {
                modeloHistorico.addRow(new Object[]{
                        at.getDataHora().toLocalDateTime().format(formatoData),
                        at.getProfissionalNome(),
                        at.getTipo(),
                        at.getSituacao()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar histórico: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Adiciona uma evolução com arquivo
    private void adicionarEvolucaoArquivo() {
        JFileChooser fileChooser = new JFileChooser();
        // Configura filtro para imagens e PDFs
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Imagens e PDFs (*.jpg, *.jpeg, *.png, *.gif, *.pdf)", "jpg", "jpeg", "png", "gif", "pdf"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            // Verifica se o arquivo é válido
            String fileName = file.getName().toLowerCase();
            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || 
                fileName.endsWith(".png") || fileName.endsWith(".gif") || fileName.endsWith(".pdf")) {
                EvolucaoAtendimento evo = new EvolucaoAtendimento();
                evo.setArquivo(file.getAbsolutePath());
                adicionarEvolucaoArquivo(evo);
            } else {
                JOptionPane.showMessageDialog(this, "Por favor, selecione uma imagem ou PDF.", "Formato Inválido", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void adicionarEvolucaoArquivo(EvolucaoAtendimento evo) {
        String fileName = new File(evo.getArquivo()).getName();
        EvolucaoComponent comp = new EvolucaoComponent(evo, "arquivo");
        comp.lblArquivo = new JLabel("Arquivo: " + fileName);
        comp.lblArquivo.setFont(labelFont);
        comp.panel.add(comp.lblArquivo, BorderLayout.CENTER);

        JButton btnRemover = new JButton("Remover");
        btnRemover.setBackground(new Color(255, 99, 71));
        btnRemover.setForeground(Color.WHITE);
        btnRemover.setFont(buttonFont);
        btnRemover.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRemover.addActionListener(e -> removerEvolucaoArquivo(comp));
        comp.panel.add(btnRemover, BorderLayout.EAST);

        listaEvolucoesArquivos.add(comp);
        panelEvolucoesArquivos.add(comp.panel);
        panelEvolucoesArquivos.revalidate();
        panelEvolucoesArquivos.repaint();
    }

    // Remove uma evolução de arquivo
    private void removerEvolucaoArquivo(EvolucaoComponent comp) {
        if (comp.evo.getId() > 0) {
            try {
                evolucaoController.removerEvolucao(comp.evo.getId());
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao remover evolução: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
        panelEvolucoesArquivos.remove(comp.panel);
        listaEvolucoesArquivos.remove(comp);
        panelEvolucoesArquivos.revalidate();
        panelEvolucoesArquivos.repaint();
    }

    // Salva as alterações
    private void salvar() {
        try {
            // Atualiza status do atendimento
            if (cbSituacao != null) {
                atendimento.setSituacao((Atendimento.Situacao) cbSituacao.getSelectedItem());
            }

            // Atualiza observações do atendimento
            atendimento.setNotas(txtObservacoesAtendimento.getText());
            atendimentoController.atualizarAtendimento(atendimento, Sessao.getUsuarioLogado().getLogin());

            // Salva ou atualiza a evolução de texto
            String notas = txtEvolucaoNotas.getText();
            if (!notas.trim().isEmpty()) {
                if (evolucaoTextoExistente == null) {
                    evolucaoTextoExistente = new EvolucaoAtendimento();
                    evolucaoTextoExistente.setAtendimentoId(atendimento.getId());
                    evolucaoTextoExistente.setUsuario(Sessao.getUsuarioLogado().getLogin());
                }
                evolucaoTextoExistente.setNotas(notas);
                evolucaoController.criarEvolucao(evolucaoTextoExistente, Sessao.getUsuarioLogado().getLogin());
            } else if (evolucaoTextoExistente != null && evolucaoTextoExistente.getId() > 0) {
                // Remove a evolução de texto se o campo estiver vazio
                evolucaoController.removerEvolucao(evolucaoTextoExistente.getId());
                evolucaoTextoExistente = null;
            }

            // Salva novas evoluções de arquivo
            for (EvolucaoComponent comp : listaEvolucoesArquivos) {
                if (comp.evo.getId() == 0) { // Nova evolução
                    comp.evo.setAtendimentoId(atendimento.getId());
                    comp.evo.setUsuario(Sessao.getUsuarioLogado().getLogin());
                    evolucaoController.criarEvolucao(comp.evo, Sessao.getUsuarioLogado().getLogin());
                }
            }

            JOptionPane.showMessageDialog(this, "Dados salvos com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}