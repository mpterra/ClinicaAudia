package view.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
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

// Importações para controllers, modelos e AgendaPanel
import controller.AtendimentoController;
import controller.DocumentoAtendimentoController;
import controller.PacienteController;
import model.Atendimento;
import model.DocumentoAtendimento;
import model.Paciente;
import util.Sessao;
import view.AgendaPanel;

// Diálogo para exibir detalhes do atendimento e histórico do paciente
public class PacienteAtendimentoDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final Atendimento atendimento;
    private final AgendaPanel agendaPanel; // Referência ao AgendaPanel pai
    private final AtendimentoController atendimentoController = new AtendimentoController();
    private final DocumentoAtendimentoController documentoController = new DocumentoAtendimentoController();
    private final PacienteController pacienteController = new PacienteController();

    private JEditorPane txtObservacoesAtendimento; // Editor para observações com formatação HTML
    private JTable tabelaHistorico;
    private DefaultTableModel modeloHistorico;
    private JComboBox<Atendimento.Situacao> cbSituacao; // ComboBox para status do atendimento
    private JPanel panelDocumentos;
    private List<DocumentoComponent> listaDocumentos; // Lista de documentos anexados
    private final DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final Color primaryColor = new Color(30, 144, 255);
    private final Color backgroundColor = new Color(245, 245, 245);
    private final Color textAreaBackground = Color.WHITE;
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 14);
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 18);
    private final Font buttonFont = new Font("SansSerif", Font.PLAIN, 12);

    // Classe interna para componentes de documentos
    private static class DocumentoComponent {
        DocumentoAtendimento doc;
        JPanel panel;
        JLabel lblArquivo;

        DocumentoComponent(DocumentoAtendimento doc) {
            this.doc = doc;
            this.panel = new JPanel(new BorderLayout(5, 5));
            this.panel.setBackground(new Color(245, 245, 245));
            this.lblArquivo = new JLabel("Arquivo: " + doc.getNomeArquivo());
            this.lblArquivo.setFont(new Font("SansSerif", Font.PLAIN, 14));
            this.lblArquivo.setForeground(new Color(30, 144, 255));
            this.lblArquivo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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

    public PacienteAtendimentoDialog(Frame parent, Atendimento atendimento, AgendaPanel agendaPanel) {
        super(parent, "Detalhes do Atendimento e Paciente", true);
        this.atendimento = atendimento;
        this.agendaPanel = agendaPanel; // Inicializa a referência ao AgendaPanel
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

        // Abas
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
                tabbedPane.setCursor(Cursor.getDefaultCursor());
            }
        });
        JScrollPane atendimentoScrollPane = new JScrollPane(criarPainelAtendimentoAtual(), 
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        atendimentoScrollPane.getVerticalScrollBar().setUnitIncrement(32);
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
        add(mainPanel, BorderLayout.CENTER);

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
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(primaryColor), 
                        "Dados do Paciente", TitledBorder.CENTER, TitledBorder.TOP, labelFont, primaryColor),
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

        // Seção principal de formulário
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
        if (atendimento.getSituacao() != null) {
            cbSituacao.setSelectedItem(atendimento.getSituacao());
        }
        statusPanel.add(lblStatus);
        statusPanel.add(cbSituacao);
        formPanel.add(statusPanel, BorderLayout.NORTH);

        // Observações do atendimento com formatação
        JPanel obsPanel = new JPanel(new BorderLayout(10, 10));
        obsPanel.setBackground(backgroundColor);
        JLabel lblObservacoes = new JLabel("Observações do Atendimento");
        lblObservacoes.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblObservacoes.setForeground(primaryColor);
        lblObservacoes.setBorder(new EmptyBorder(0, 0, 10, 0));
        obsPanel.add(lblObservacoes, BorderLayout.NORTH);

        // Barra de ferramentas para formatação
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(backgroundColor);
        toolBar.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

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
        toolBar.add(Box.createHorizontalStrut(5));
        toolBar.add(btnItalic);
        toolBar.add(Box.createHorizontalStrut(10));
        toolBar.add(colorCombo);
        obsPanel.add(toolBar, BorderLayout.CENTER);

        // Editor de texto com formatação HTML
        txtObservacoesAtendimento = new JEditorPane();
        txtObservacoesAtendimento.setContentType("text/html");
        HTMLEditorKit editorKit = new HTMLEditorKit();
        txtObservacoesAtendimento.setEditorKit(editorKit);
        HTMLDocument doc = new HTMLDocument();
        txtObservacoesAtendimento.setDocument(doc);
        txtObservacoesAtendimento.setBackground(textAreaBackground);
        txtObservacoesAtendimento.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        txtObservacoesAtendimento.setPreferredSize(new Dimension(0, 140)); // Aproximadamente 7 linhas
        txtObservacoesAtendimento.setText("<html><body style='font-family: SansSerif;  margin: 2; padding: 2; line-height: 1.0;'></body></html>");

        // Configurar quebra de linha com Enter
        txtObservacoesAtendimento.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        editorKit.insertHTML(doc, txtObservacoesAtendimento.getCaretPosition(), "<br>", 0, 0, null);
                        e.consume(); // Evita comportamento padrão indesejado
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        JScrollPane scrollObs = new JScrollPane(txtObservacoesAtendimento);
        scrollObs.setBackground(backgroundColor);
        scrollObs.setBorder(BorderFactory.createEmptyBorder());
        scrollObs.getVerticalScrollBar().setUnitIncrement(32);
        obsPanel.add(scrollObs, BorderLayout.SOUTH);

        formPanel.add(obsPanel, BorderLayout.CENTER);

        // Documentos
        JPanel documentosPanel = new JPanel(new BorderLayout(10, 10));
        documentosPanel.setBackground(backgroundColor);

        // Painel para rótulo e botão de anexar
        JPanel headerDocumentosPanel = new JPanel();
        headerDocumentosPanel.setLayout(new BoxLayout(headerDocumentosPanel, BoxLayout.X_AXIS));
        headerDocumentosPanel.setBackground(backgroundColor);
        headerDocumentosPanel.setBorder(new EmptyBorder(0, 10, 5, 10));
        JLabel lblDocumentos = new JLabel("Documentos Anexados");
        lblDocumentos.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblDocumentos.setForeground(primaryColor);
        headerDocumentosPanel.add(lblDocumentos);
        headerDocumentosPanel.add(Box.createHorizontalStrut(10));

        JButton btnAnexar = new JButton("Anexar Arquivo");
        btnAnexar.setBackground(Color.LIGHT_GRAY);
        btnAnexar.setForeground(Color.BLACK);
        btnAnexar.setFont(buttonFont);
        btnAnexar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAnexar.setPreferredSize(new Dimension(100, 26));
        btnAnexar.addActionListener(e -> adicionarDocumento());
        headerDocumentosPanel.add(btnAnexar);

        documentosPanel.add(headerDocumentosPanel, BorderLayout.NORTH);

        // Painel para lista de documentos
        panelDocumentos = new JPanel();
        panelDocumentos.setLayout(new BoxLayout(panelDocumentos, BoxLayout.Y_AXIS));
        panelDocumentos.setBackground(backgroundColor);
        JScrollPane scrollDocumentos = new JScrollPane(panelDocumentos);
        scrollDocumentos.setBackground(backgroundColor);
        scrollDocumentos.setBorder(BorderFactory.createEmptyBorder());
        scrollDocumentos.getVerticalScrollBar().setUnitIncrement(32);
        documentosPanel.add(scrollDocumentos, BorderLayout.CENTER);

        formPanel.add(documentosPanel, BorderLayout.SOUTH);

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
        tabelaHistorico.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

        // Duplo clique para abrir detalhes do atendimento
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
                                new PacienteAtendimentoDialog((Frame) SwingUtilities.getWindowAncestor(PacienteAtendimentoDialog.this), selectedAtendimento, agendaPanel).setVisible(true);
                            }
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(PacienteAtendimentoDialog.this, "Erro ao abrir atendimento: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
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
        scrollTabela.getVerticalScrollBar().setUnitIncrement(32);
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

    // Adiciona um documento
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

    // Remove um documento
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

    // Carrega os dados iniciais do atendimento e histórico
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

    // Salva as alterações
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

            // Atualiza a tabela no AgendaPanel
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