package view.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.awt.Desktop;

import controller.AtendimentoController;
import controller.DocumentoAtendimentoController;
import controller.PacienteController;
import model.Atendimento;
import model.DocumentoAtendimento;
import model.Paciente;
import util.Sessao;
import view.AgendaPanel;
import view.dialogs.HistoricoPacienteDialog;

// Classe principal para a janela de diálogo de detalhes do atendimento
public class PacienteAtendimentoDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final Atendimento atendimento;
    private final AgendaPanel agendaPanel;
    private final AtendimentoController atendimentoController = new AtendimentoController();
    private final DocumentoAtendimentoController documentoController = new DocumentoAtendimentoController();
    private final PacienteController pacienteController = new PacienteController();

    private JEditorPane txtObservacoesAtendimento;
    private JTable tabelaHistorico;
    private DefaultTableModel modeloHistorico;
    private JComboBox<Atendimento.Situacao> cbSituacao;
    private JPanel panelDocumentos;
    private List<DocumentoComponent> listaDocumentos;
    private final DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final Color primaryColor = new Color(30, 144, 255);
    private final Color backgroundColor = new Color(245, 245, 245);
    private final Color textAreaBackground = Color.WHITE;
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 14);
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 18);
    private final Font buttonFont = new Font("SansSerif", Font.PLAIN, 12);

    // Componente para exibir documentos anexados
    private static class DocumentoComponent {
        DocumentoAtendimento doc;
        JPanel panel;
        JLabel lblArquivo;

        DocumentoComponent(DocumentoAtendimento doc) {
            this.doc = doc;
            this.panel = new JPanel(new BorderLayout(5, 5));
            this.panel.setBackground(new Color(245, 245, 245));
            this.lblArquivo = new JLabel();
            this.lblArquivo.setFont(new Font("SansSerif", Font.PLAIN, 14));
            this.lblArquivo.setForeground(new Color(30, 144, 255));
            this.lblArquivo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            ImageIcon pdfIcon = new ImageIcon("src/main/resources/images/pdf.png");
            Image scaledImage = pdfIcon.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
            this.lblArquivo.setIcon(new ImageIcon(scaledImage));
            this.lblArquivo.setText(doc.getNomeArquivo());
            this.lblArquivo.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        File file = new File(doc.getCaminhoArquivo());
                        if (file.exists()) {
                            Desktop.getDesktop().open(file);
                        } else {
                            JOptionPane.showMessageDialog(null, "Arquivo não encontrado: " + doc.getCaminhoArquivo(), 
                                    "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Erro ao abrir arquivo: " + ex.getMessage(), 
                                "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
        }
    }

    // Renderizador para o combobox de cores
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

    // Item de cor para o combobox
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

    // Ícone de cor para o combobox
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

    // Construtor da janela de diálogo
    public PacienteAtendimentoDialog(Frame parent, Atendimento atendimento, AgendaPanel agendaPanel) {
        super(parent, "Detalhes do Atendimento", true);
        this.atendimento = atendimento;
        this.agendaPanel = agendaPanel;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);
        setSize(650, 700);
        setMinimumSize(new Dimension(500, 500));
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(backgroundColor);

        listaDocumentos = new ArrayList<>();
        initComponents();
        carregarDados();
    }

    // Inicializa os componentes da interface
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(backgroundColor);

        JLabel lblTitulo = new JLabel("Detalhes do Atendimento", SwingConstants.CENTER);
        lblTitulo.setFont(titleFont);
        lblTitulo.setForeground(primaryColor);
        lblTitulo.setBorder(new EmptyBorder(0, 0, 5, 0));
        add(lblTitulo, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(labelFont);
        tabbedPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JScrollPane atendimentoScrollPane = new JScrollPane(criarPainelAtendimentoAtual(), 
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        atendimentoScrollPane.getVerticalScrollBar().setUnitIncrement(32);
        tabbedPane.addTab("Atendimento Atual", atendimentoScrollPane);
        tabbedPane.addTab("Histórico do Paciente", criarPainelHistorico());

        tabbedPane.setBackgroundAt(0, new Color(200, 220, 255));
        tabbedPane.setBackgroundAt(1, backgroundColor);
        tabbedPane.addChangeListener(e -> {
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                tabbedPane.setBackgroundAt(i, i == tabbedPane.getSelectedIndex() ? new Color(200, 220, 255) : backgroundColor);
            }
        });
        tabbedPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                tabbedPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                tabbedPane.setCursor(Cursor.getDefaultCursor());
            }
        });
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

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

        cbSituacao = new JComboBox<>(Atendimento.Situacao.values());
        cbSituacao.setFont(labelFont);
        cbSituacao.setBackground(textAreaBackground);
        cbSituacao.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        cbSituacao.setPreferredSize(new Dimension(150, 30));
        cbSituacao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (atendimento.getSituacao() != null) {
            cbSituacao.setSelectedItem(atendimento.getSituacao());
        }

        buttonPanel.add(cbSituacao);
        buttonPanel.add(btnCancelar);
        buttonPanel.add(btnSalvar);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);

        btnSalvar.addActionListener(e -> salvar());
        btnCancelar.addActionListener(e -> dispose());
    }

    // Cria o painel da aba "Atendimento Atual"
    private JPanel criarPainelAtendimentoAtual() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(backgroundColor);

        // Seção de informações do paciente: card com bordas suaves
        JPanel pacientePanel = new JPanel(new GridBagLayout());
        pacientePanel.setBackground(textAreaBackground);
        pacientePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        pacientePanel.setPreferredSize(new Dimension(0, 120));
        GridBagConstraints gbcP = new GridBagConstraints();
        gbcP.insets = new Insets(2, 0, 2, 0);
        gbcP.anchor = GridBagConstraints.WEST;

        JLabel lblNomePaciente = new JLabel();
        lblNomePaciente.setFont(new Font("SansSerif", Font.BOLD, 16));
        gbcP.gridx = 0;
        gbcP.gridy = 0;
        gbcP.weightx = 1;
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

        // Painel principal de formulário
        JPanel formPanel = new JPanel(new BorderLayout(0, 15));
        formPanel.setBackground(backgroundColor);

        // Seção de observações
        JPanel obsPanel = new JPanel(new BorderLayout(10, 5));
        obsPanel.setBackground(backgroundColor);
        JLabel lblObservacoes = new JLabel("Observações");
        lblObservacoes.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblObservacoes.setForeground(primaryColor);
        lblObservacoes.setBorder(new EmptyBorder(0, 0, 8, 0));
        obsPanel.add(lblObservacoes, BorderLayout.NORTH);

        // Toolbar compacta e fixa
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(textAreaBackground);
        toolBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        toolBar.setPreferredSize(new Dimension(0, 40));
        toolBar.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));

        JButton btnBold = new JButton("N");
        btnBold.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnBold.setToolTipText("Negrito");
        btnBold.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBold.setBackground(backgroundColor);
        btnBold.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        btnBold.setMargin(new Insets(0, 0, 0, 0));
        btnBold.addActionListener(new StyledEditorKit.BoldAction());

        JButton btnItalic = new JButton("I");
        btnItalic.setFont(new Font("SansSerif", Font.ITALIC, 14));
        btnItalic.setToolTipText("Itálico");
        btnItalic.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnItalic.setBackground(backgroundColor);
        btnItalic.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        btnItalic.setMargin(new Insets(0, 0, 0, 0));
        btnItalic.addActionListener(new StyledEditorKit.ItalicAction());

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
        colorCombo.setPreferredSize(new Dimension(100, 30));
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
        toolBar.add(Box.createHorizontalStrut(5));
        toolBar.add(btnItalic);
        toolBar.add(Box.createHorizontalStrut(10));
        toolBar.add(colorCombo);
        obsPanel.add(toolBar, BorderLayout.NORTH);

        txtObservacoesAtendimento = new JEditorPane();
        txtObservacoesAtendimento.setContentType("text/html");
        HTMLEditorKit editorKit = new HTMLEditorKit();
        txtObservacoesAtendimento.setEditorKit(editorKit);
        HTMLDocument doc = new HTMLDocument();
        txtObservacoesAtendimento.setDocument(doc);
        txtObservacoesAtendimento.setBackground(textAreaBackground);
        txtObservacoesAtendimento.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        txtObservacoesAtendimento.setPreferredSize(new Dimension(0, 140));
        txtObservacoesAtendimento.setText("<html><body style='font-family: SansSerif; margin: 3; padding: 3; line-height: 1.0;'></body></html>");

        txtObservacoesAtendimento.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        editorKit.insertHTML(doc, txtObservacoesAtendimento.getCaretPosition(), "<br>", 0, 0, null);
                        e.consume();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        JScrollPane scrollObs = new JScrollPane(txtObservacoesAtendimento);
        scrollObs.setBackground(backgroundColor);
        scrollObs.setBorder(BorderFactory.createEmptyBorder());
        scrollObs.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollObs.getVerticalScrollBar().setUnitIncrement(32);
        obsPanel.add(scrollObs, BorderLayout.CENTER);

        formPanel.add(obsPanel, BorderLayout.CENTER);

        // Seção de documentos
        JPanel documentosPanel = new JPanel(new BorderLayout(10, 10));
        documentosPanel.setBackground(backgroundColor);
        documentosPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel headerDocumentosPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        headerDocumentosPanel.setBackground(backgroundColor);
        headerDocumentosPanel.setBorder(new EmptyBorder(0, 10, 10, 10));

        JButton btnAnexar = new JButton();
        ImageIcon anexarIcon = new ImageIcon("src/main/resources/images/anexar.png");
        Image scaledImage = anexarIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
        btnAnexar.setIcon(new ImageIcon(scaledImage));
        btnAnexar.setBackground(Color.LIGHT_GRAY);
        btnAnexar.setForeground(Color.BLACK);
        btnAnexar.setFont(buttonFont);
        btnAnexar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAnexar.setPreferredSize(new Dimension(40, 40));
        btnAnexar.setBorder(BorderFactory.createEmptyBorder());
        btnAnexar.setToolTipText("Anexar Arquivo");
        btnAnexar.addActionListener(e -> adicionarDocumento());
        headerDocumentosPanel.add(btnAnexar);

        JLabel lblAnexarDocumento = new JLabel("Anexar Documento");
        lblAnexarDocumento.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblAnexarDocumento.setForeground(primaryColor);
        headerDocumentosPanel.add(lblAnexarDocumento);

        documentosPanel.add(headerDocumentosPanel, BorderLayout.NORTH);

        panelDocumentos = new JPanel();
        panelDocumentos.setLayout(new BoxLayout(panelDocumentos, BoxLayout.Y_AXIS));
        panelDocumentos.setBackground(backgroundColor);
        JScrollPane scrollDocumentos = new JScrollPane(panelDocumentos);
        scrollDocumentos.setBackground(backgroundColor);
        scrollDocumentos.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        scrollDocumentos.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollDocumentos.getVerticalScrollBar().setUnitIncrement(32);
        documentosPanel.add(scrollDocumentos, BorderLayout.CENTER);

        formPanel.add(documentosPanel, BorderLayout.SOUTH);

        panel.add(formPanel, BorderLayout.CENTER);

        // Carrega dados do paciente
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

    // Cria o painel da aba "Histórico do Paciente" com layout refatorado
    private JPanel criarPainelHistorico() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(backgroundColor);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Modelo da tabela
        String[] colunas = {"Data/Hora", "Profissional", "Tipo", "Situação"};
        modeloHistorico = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        // Tabela com estilo profissional
        tabelaHistorico = new JTable(modeloHistorico);
        tabelaHistorico.setFont(labelFont);
        tabelaHistorico.setRowHeight(28);
        tabelaHistorico.setShowGrid(true);
        tabelaHistorico.setGridColor(new Color(220, 220, 220));
        tabelaHistorico.setBackground(Color.WHITE);
        tabelaHistorico.setForeground(new Color(50, 50, 50));
        tabelaHistorico.setSelectionBackground(Color.WHITE);
        tabelaHistorico.setSelectionForeground(new Color(50, 50, 50));
        tabelaHistorico.setBorder(new LineBorder(Color.BLACK));
        tabelaHistorico.setCursor(Cursor.getDefaultCursor());

        // Renderizador centralizado com linhas alternadas
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (isSelected) {
                    c.setBackground(table.getBackground());
                    ((JComponent) c).setBorder(new LineBorder(Color.BLACK, 1));
                } else {
                    ((JComponent) c).setBorder(null);
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 250, 250));
                }
                return c;
            }
        };
        for (int i = 0; i < tabelaHistorico.getColumnCount(); i++) {
            tabelaHistorico.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Header da tabela elegante
        JTableHeader header = tabelaHistorico.getTableHeader();
        header.setBackground(primaryColor);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)));
        header.setReorderingAllowed(false);

        // Listener para duplo clique
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
                                tabelaHistorico.clearSelection();
                                new HistoricoPacienteDialog((Frame) SwingUtilities.getWindowAncestor(PacienteAtendimentoDialog.this), selectedAtendimento).setVisible(true);
                            } else {
                                JOptionPane.showMessageDialog(PacienteAtendimentoDialog.this, "Atendimento não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(PacienteAtendimentoDialog.this, "Erro ao abrir histórico: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });

        // Scroll pane com scroll vertical/horizontal se necessário
        JScrollPane scrollTabela = new JScrollPane(tabelaHistorico);
        scrollTabela.setBackground(backgroundColor);
        scrollTabela.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        scrollTabela.getVerticalScrollBar().setUnitIncrement(32);
        scrollTabela.setViewportBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        panel.add(scrollTabela, BorderLayout.CENTER);

        // Listener para redimensionamento de colunas
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

        // Botão com ícone "historico_completo.png" e texto
        JButton btnHistoricoCompleto = new JButton("Histórico Completo");
        ImageIcon historicoIcon = new ImageIcon("src/main/resources/images/historico_completo.png");
        Image scaledImage = historicoIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
        btnHistoricoCompleto.setIcon(new ImageIcon(scaledImage));
        btnHistoricoCompleto.setBackground(new Color(230, 230, 230)); // Cinza mais claro
        btnHistoricoCompleto.setForeground(Color.BLACK);
        btnHistoricoCompleto.setFont(buttonFont);
        btnHistoricoCompleto.setPreferredSize(new Dimension(180, 40)); // Tamanho ajustado para texto
        btnHistoricoCompleto.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnHistoricoCompleto.setBorder(BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED)); // Efeito 3D
        btnHistoricoCompleto.setToolTipText("Histórico Completo do Paciente");
        btnHistoricoCompleto.addActionListener(e -> {
            // TODO: Implementar funcionalidade se necessário
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(backgroundColor);
        buttonPanel.add(btnHistoricoCompleto);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    // Adiciona um documento à interface
    private void adicionarDocumento() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Imagens e PDFs (*.jpg, *.jpeg, *.png, *.gif, *.pdf)", "jpg", "jpeg", "png", "gif", "pdf"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String fileName = file.getName().toLowerCase();
            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || 
                fileName.endsWith(".png") || fileName.endsWith(".gif") || fileName.endsWith(".pdf")) {
                DocumentoAtendimento doc = new DocumentoAtendimento();
                doc.setAtendimentoId(atendimento.getId());
                doc.setNomeArquivo(file.getName());
                doc.setCaminhoArquivo(file.getAbsolutePath());
                doc.setTipoArquivo(fileName.substring(fileName.lastIndexOf(".") + 1).toUpperCase());
                adicionarDocumento(doc);
            } else {
                JOptionPane.showMessageDialog(this, "Por favor, selecione uma imagem ou PDF.", "Formato Inválido", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    // Adiciona um documento à lista e à interface
    private void adicionarDocumento(DocumentoAtendimento doc) {
        DocumentoComponent comp = new DocumentoComponent(doc);
        comp.panel.add(comp.lblArquivo, BorderLayout.CENTER);

        JButton btnRemover = new JButton("Remover");
        btnRemover.setBackground(new Color(255, 99, 71));
        btnRemover.setForeground(Color.WHITE);
        btnRemover.setFont(buttonFont);
        btnRemover.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRemover.addActionListener(e -> removerDocumento(comp));
        comp.panel.add(btnRemover, BorderLayout.EAST);

        listaDocumentos.add(comp);
        panelDocumentos.add(comp.panel);
        panelDocumentos.revalidate();
        panelDocumentos.repaint();
    }

    // Remove um documento da interface e do banco
    private void removerDocumento(DocumentoComponent comp) {
        if (comp.doc.getId() > 0) {
            try {
                documentoController.deletar(comp.doc.getId());
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao remover documento: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
        panelDocumentos.remove(comp.panel);
        listaDocumentos.remove(comp);
        panelDocumentos.revalidate();
        panelDocumentos.repaint();
    }

    // Carrega os dados do atendimento e paciente
    private void carregarDados() {
        txtObservacoesAtendimento.setText(atendimento.getNotas() != null ? 
                atendimento.getNotas() : "<html><body style='font-family: SansSerif; font-size: 16px; margin: 0; padding: 0; line-height: 1.0;'></body></html>");
        try {
            List<DocumentoAtendimento> documentos = documentoController.listarPorAtendimentoId(atendimento.getId());
            for (DocumentoAtendimento doc : documentos) {
                adicionarDocumento(doc);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar documentos: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }

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

    // Salva as alterações no atendimento
    private void salvar() {
        try {
            if (cbSituacao != null) {
                atendimento.setSituacao((Atendimento.Situacao) cbSituacao.getSelectedItem());
            }
            atendimento.setNotas(txtObservacoesAtendimento.getText());
            atendimentoController.atualizarAtendimento(atendimento, Sessao.getUsuarioLogado().getLogin());

            for (DocumentoComponent comp : listaDocumentos) {
                if (comp.doc.getId() == 0) {
                    comp.doc.setAtendimentoId(atendimento.getId());
                    documentoController.criar(comp.doc, Sessao.getUsuarioLogado().getLogin());
                }
            }

            if (agendaPanel != null) {
                agendaPanel.atualizarTabela();
            }

            JOptionPane.showMessageDialog(this, "Dados salvos com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}