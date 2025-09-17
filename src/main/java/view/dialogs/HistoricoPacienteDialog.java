package view.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.time.format.DateTimeFormatter;
import controller.DocumentoAtendimentoController;
import model.DocumentoAtendimento;
import model.Atendimento;
import java.io.File;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class HistoricoPacienteDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final Atendimento atendimento;
    private final DocumentoAtendimentoController documentoController = new DocumentoAtendimentoController();
    private final DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final Color primaryColor = new Color(30, 144, 255); // ainda usado para os lblArquivo
    private final Color backgroundColor = new Color(245, 245, 245);
    private final Color textAreaBackground = Color.WHITE;
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 18);
    private final Font subtitleFont = new Font("SansSerif", Font.BOLD, 14); // 🔹 agora normal, sem negrito
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 14);

    private JEditorPane txtObservacoes;
    private JPanel panelDocumentos;

    public HistoricoPacienteDialog(Frame parent, Atendimento atendimento) {
        super(parent, "Histórico do Atendimento", true);
        this.atendimento = atendimento;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);
        setSize(500, 600);
        setMinimumSize(new Dimension(400, 400));
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(backgroundColor);

        initComponents();
        carregarDados();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(backgroundColor);

        // Título
        JLabel lblTitulo = new JLabel(
                atendimento.getPaciente().getNome() != null ? atendimento.getPaciente().getNome() : "Não informado",
                SwingConstants.CENTER);
        lblTitulo.setFont(titleFont);
        lblTitulo.setForeground(Color.BLACK);
        lblTitulo.setBorder(new EmptyBorder(0, 0, 2, 0)); // 🔹 espaço mínimo
        add(lblTitulo, BorderLayout.NORTH);

        // Subtítulo
        String subtitulo = String.format("%s - %s",
                atendimento.getDataHora().toLocalDateTime().format(formatoData),
                atendimento.getTipo() != null ? atendimento.getTipo() : "Não informado");
        JLabel lblSubtitulo = new JLabel(subtitulo, SwingConstants.CENTER);
        lblSubtitulo.setFont(subtitleFont);
        lblSubtitulo.setForeground(Color.BLACK);
        // 🔹 removi o EmptyBorder extra
        mainPanel.add(lblSubtitulo, BorderLayout.NORTH);

        // Observações
        JPanel obsPanel = new JPanel(new BorderLayout(10, 10));
        obsPanel.setBackground(backgroundColor);
        JLabel lblObservacoes = new JLabel("Observações");
        lblObservacoes.setFont(subtitleFont);
        lblObservacoes.setForeground(Color.BLACK);
        obsPanel.add(lblObservacoes, BorderLayout.NORTH);

        txtObservacoes = new JEditorPane();
        txtObservacoes.setContentType("text/html");
        txtObservacoes.setEditable(false);
        txtObservacoes.setBackground(textAreaBackground);
        txtObservacoes.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        JScrollPane scrollObs = new JScrollPane(txtObservacoes);
        scrollObs.setBackground(backgroundColor);
        scrollObs.setBorder(BorderFactory.createEmptyBorder());
        scrollObs.getVerticalScrollBar().setUnitIncrement(32);
        // 🔹 define altura menor (40% a menos em relação à altura do dialog)
        int alturaDialog = getPreferredSize().height;
        scrollObs.setPreferredSize(new Dimension(0, (int) (alturaDialog * 0.35)));
        obsPanel.add(scrollObs, BorderLayout.CENTER);
        mainPanel.add(obsPanel, BorderLayout.CENTER);

        // Documentos
        JPanel documentosPanel = new JPanel(new BorderLayout(10, 10));
        documentosPanel.setBackground(backgroundColor);
        JLabel lblDocumentos = new JLabel("Documentos Anexados");
        lblDocumentos.setFont(subtitleFont);
        lblDocumentos.setForeground(Color.BLACK);
        documentosPanel.add(lblDocumentos, BorderLayout.NORTH);

        panelDocumentos = new JPanel();
        panelDocumentos.setLayout(new BoxLayout(panelDocumentos, BoxLayout.Y_AXIS));
        panelDocumentos.setBackground(backgroundColor);
        JScrollPane scrollDocumentos = new JScrollPane(panelDocumentos);
        scrollDocumentos.setBackground(backgroundColor);
        scrollDocumentos.setBorder(BorderFactory.createEmptyBorder());
        scrollDocumentos.getVerticalScrollBar().setUnitIncrement(32);
        documentosPanel.add(scrollDocumentos, BorderLayout.CENTER);
        mainPanel.add(documentosPanel, BorderLayout.SOUTH);

        // Botão Fechar
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(backgroundColor);
        JButton btnFechar = new JButton("Fechar");
        btnFechar.setBackground(primaryColor);
        btnFechar.setForeground(Color.WHITE);
        btnFechar.setFont(labelFont);
        btnFechar.setPreferredSize(new Dimension(100, 35));
        btnFechar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnFechar.addActionListener(e -> dispose());
        buttonPanel.add(btnFechar);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void carregarDados() {
        txtObservacoes.setText(atendimento.getNotas() != null
                ? atendimento.getNotas()
                : "<html><body style='font-family: SansSerif; font-size: 16px; margin: 0; padding: 0; line-height: 1.0;'></body></html>");

        try {
            List<DocumentoAtendimento> documentos = documentoController.listarPorAtendimentoId(atendimento.getId());
            for (DocumentoAtendimento doc : documentos) {
                JPanel docPanel = new JPanel(new BorderLayout(5, 5));
                docPanel.setBackground(backgroundColor);
                JLabel lblArquivo = new JLabel(doc.getNomeArquivo());
                lblArquivo.setFont(labelFont);
                lblArquivo.setForeground(primaryColor); // continua azul
                lblArquivo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                ImageIcon pdfIcon = new ImageIcon("src/main/resources/images/pdf.png");
                Image scaledImage = pdfIcon.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
                lblArquivo.setIcon(new ImageIcon(scaledImage));
                lblArquivo.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        try {
                            File file = new File(doc.getCaminhoArquivo());
                            if (file.exists()) {
                                Desktop.getDesktop().open(file);
                            } else {
                                JOptionPane.showMessageDialog(null,
                                        "Arquivo não encontrado: " + doc.getCaminhoArquivo(),
                                        "Erro", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(null,
                                    "Erro ao abrir arquivo: " + ex.getMessage(),
                                    "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
                docPanel.add(lblArquivo, BorderLayout.CENTER);
                panelDocumentos.add(docPanel);
            }
            panelDocumentos.revalidate();
            panelDocumentos.repaint();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar documentos: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
