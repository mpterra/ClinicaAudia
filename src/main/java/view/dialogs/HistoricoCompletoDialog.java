package view.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import controller.AtendimentoController;
import controller.DocumentoAtendimentoController;
import model.Atendimento;
import model.DocumentoAtendimento;
import model.Paciente;

// Classe para exibir o histórico completo do paciente em formato de relatório
public class HistoricoCompletoDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final Paciente paciente;
    private final AtendimentoController atendimentoController = new AtendimentoController();
    private final DocumentoAtendimentoController documentoController = new DocumentoAtendimentoController();
    private final DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final Color backgroundColor = new Color(245, 245, 245);
    private final Color primaryColor = new Color(30, 144, 255);
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 12);
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 18);

    // Componentes principais
    private JEditorPane txtNotas;
    private JTable tabelaDocumentos;

    // Construtor da janela de diálogo
    public HistoricoCompletoDialog(Dialog parent, Paciente paciente) {
        super(parent, paciente.getNome() != null ? paciente.getNome() : "Histórico Completo", true);
        this.paciente = paciente;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);
        setSize(600, 800);
        setMinimumSize(new Dimension(500, 600));
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(backgroundColor);

        initComponents();
        carregarDados();
    }

    // Inicializa os componentes da interface
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(backgroundColor);

        // Título
        JLabel lblTitulo = new JLabel("Histórico Completo do Paciente", SwingConstants.CENTER);
        lblTitulo.setFont(titleFont);
        lblTitulo.setForeground(primaryColor);
        add(lblTitulo, BorderLayout.NORTH);

        // Seção de notas
        JPanel notasPanel = new JPanel(new BorderLayout(5, 5));
        notasPanel.setBackground(backgroundColor);
        JLabel lblNotas = new JLabel("Notas de Atendimentos");
        lblNotas.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblNotas.setForeground(primaryColor);
        notasPanel.add(lblNotas, BorderLayout.NORTH);

        txtNotas = new JEditorPane();
        txtNotas.setContentType("text/html");
        txtNotas.setEditable(false);
        txtNotas.setFont(labelFont);
        txtNotas.setBackground(Color.WHITE);
        txtNotas.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        HTMLEditorKit editorKit = new HTMLEditorKit();
        txtNotas.setEditorKit(editorKit);
        HTMLDocument doc = new HTMLDocument();
        txtNotas.setDocument(doc);
        txtNotas.setText("<html><body style='font-family: SansSerif; font-size: 12px; margin: 5px; line-height: 1.2;'></body></html>");
        JScrollPane scrollNotas = new JScrollPane(txtNotas);
        scrollNotas.getVerticalScrollBar().setUnitIncrement(16);
        notasPanel.add(scrollNotas, BorderLayout.CENTER);

        // Seção de documentos
        JPanel documentosPanel = new JPanel(new BorderLayout(5, 5));
        documentosPanel.setBackground(backgroundColor);
        JLabel lblDocumentos = new JLabel("Documentos Anexados");
        lblDocumentos.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblDocumentos.setForeground(primaryColor);
        documentosPanel.add(lblDocumentos, BorderLayout.NORTH);

        DefaultTableModel modeloDocumentos = new DefaultTableModel(new String[]{"Data Atendimento", "Nome Arquivo"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabelaDocumentos = new JTable(modeloDocumentos);
        tabelaDocumentos.setFont(labelFont);
        tabelaDocumentos.setRowHeight(25);
        tabelaDocumentos.setBackground(Color.WHITE);
        tabelaDocumentos.setGridColor(new Color(220, 220, 220));
        tabelaDocumentos.setShowGrid(true);
        tabelaDocumentos.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Listener para abrir documento ao clicar
        tabelaDocumentos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int row = tabelaDocumentos.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        String dataHoraStr = (String) tabelaDocumentos.getValueAt(row, 0);
                        String nomeArquivo = (String) tabelaDocumentos.getValueAt(row, 1);
                        try {
                            Atendimento atendimento = atendimentoController.listarTodos().stream()
                                    .filter(a -> a.getDataHora().toLocalDateTime().format(formatoData).equals(dataHoraStr))
                                    .findFirst()
                                    .orElse(null);
                            if (atendimento != null) {
                                List<DocumentoAtendimento> docs = documentoController.listarPorAtendimentoId(atendimento.getId());
                                DocumentoAtendimento doc = docs.stream()
                                        .filter(d -> d.getNomeArquivo().equals(nomeArquivo))
                                        .findFirst()
                                        .orElse(null);
                                if (doc != null) {
                                    File file = new File(doc.getCaminhoArquivo());
                                    if (file.exists()) {
                                        Desktop.getDesktop().open(file);
                                    } else {
                                        JOptionPane.showMessageDialog(HistoricoCompletoDialog.this, 
                                                "Arquivo não encontrado: " + doc.getCaminhoArquivo(), 
                                                "Erro", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                            }
                        } catch (SQLException | IOException ex) {
                            JOptionPane.showMessageDialog(HistoricoCompletoDialog.this, 
                                    "Erro ao abrir documento: " + ex.getMessage(), 
                                    "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });
        JScrollPane scrollDocumentos = new JScrollPane(tabelaDocumentos);
        scrollDocumentos.getVerticalScrollBar().setUnitIncrement(16);
        documentosPanel.add(scrollDocumentos, BorderLayout.CENTER);

        // Painel dividido para notas e documentos
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, notasPanel, documentosPanel);
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(null);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // Botão fechar
        JButton btnFechar = new JButton("Fechar");
        btnFechar.setBackground(primaryColor);
        btnFechar.setForeground(Color.WHITE);
        btnFechar.setFont(labelFont);
        btnFechar.setPreferredSize(new Dimension(100, 35));
        btnFechar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnFechar.addActionListener(e -> dispose());
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.setBackground(backgroundColor);
        southPanel.add(btnFechar);
        add(southPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
    }

    // Carrega os dados do histórico completo
    private void carregarDados() {
        try {
            List<Atendimento> atendimentos = atendimentoController.listarTodos().stream()
                    .filter(a -> a.getPacienteId() == paciente.getId())
                    .sorted((a1, a2) -> a1.getDataHora().compareTo(a2.getDataHora()))
                    .collect(Collectors.toList());

            // Construir texto de notas
            StringBuilder notasTexto = new StringBuilder("<html><body style='font-family: SansSerif; font-size: 12px; margin: 5px; line-height: 1.2;'>");
            for (Atendimento at : atendimentos) {
                notasTexto.append("<div style='margin-bottom: 10px;'><h4 style='margin: 0 0 5px 0; color: #").append(String.format("%06X", primaryColor.getRGB() & 0xFFFFFF))
                          .append("; font-size: 12px;'>--- Data: ")
                          .append(at.getDataHora().toLocalDateTime().format(formatoData))
                          .append(" ---</h4>");
                String notaContent = at.getNotas() != null ? extrairConteudoHTML(at.getNotas()) : "<p style='color: gray;'>Sem notas</p>";
                notasTexto.append(notaContent).append("</div>");
            }
            notasTexto.append("</body></html>");
            txtNotas.setText(notasTexto.toString());
            txtNotas.setCaretPosition(0); // Scroll para o topo

            // Carregar documentos
            DefaultTableModel modelo = (DefaultTableModel) tabelaDocumentos.getModel();
            modelo.setRowCount(0); // Limpar tabela antes de adicionar
            for (Atendimento at : atendimentos) {
                List<DocumentoAtendimento> docs = documentoController.listarPorAtendimentoId(at.getId());
                for (DocumentoAtendimento doc : docs) {
                    modelo.addRow(new Object[]{
                            at.getDataHora().toLocalDateTime().format(formatoData),
                            doc.getNomeArquivo()
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar histórico completo: " + e.getMessage(), 
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método auxiliar para extrair o conteúdo HTML sem tags externas desnecessárias
    private String extrairConteudoHTML(String html) {
        if (html == null) return "";
        // Remove tags <html> e <body> externas se existirem, mantendo o conteúdo interno
        Pattern pattern = Pattern.compile("<body[^>]*>(.*?)</body>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        }
        // Se não encontrar, remove apenas <html> externa
        pattern = Pattern.compile("<html[^>]*>(.*?)</html>", Pattern.DOTALL);
        matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return html; // Retorna como está se não puder limpar
    }
}