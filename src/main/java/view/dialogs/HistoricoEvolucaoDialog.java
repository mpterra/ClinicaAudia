package view.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

import controller.EvolucaoAtendimentoController;
import model.Atendimento;
import model.EvolucaoAtendimento;
import model.Paciente;

// Diálogo para exibir evoluções de um atendimento específico
public class HistoricoEvolucaoDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final Atendimento atendimento;
    private final Paciente paciente;
    private final EvolucaoAtendimentoController evolucaoController = new EvolucaoAtendimentoController();

    private final Color primaryColor = new Color(30, 144, 255);
    private final Color backgroundColor = new Color(245, 245, 245);
    private final Color textAreaBackground = Color.WHITE; // Fundo branco padrão para campos editáveis
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 14);
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 18);

    private JPanel evolucoesPanel; // Painel para evoluções

    public HistoricoEvolucaoDialog(Frame parent, Atendimento atendimento, Paciente paciente) {
        super(parent, "Evoluções do Atendimento", true);
        this.atendimento = atendimento;
        this.paciente = paciente;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);
        setSize(500, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(backgroundColor);

        initComponents();
        carregarEvolucoes();
    }

    // Inicializa os componentes da interface
    private void initComponents() {
        // Título
        JLabel lblTitulo = new JLabel("Evoluções do Atendimento", SwingConstants.CENTER);
        lblTitulo.setFont(titleFont);
        lblTitulo.setForeground(primaryColor);
        lblTitulo.setBorder(new EmptyBorder(0, 0, 20, 0));
        add(lblTitulo, BorderLayout.NORTH);

        // Dados do paciente
        JPanel pacientePanel = new JPanel(new GridBagLayout());
        pacientePanel.setBackground(backgroundColor);
        pacientePanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbcP = new GridBagConstraints();
        gbcP.insets = new Insets(5, 0, 5, 0);
        gbcP.anchor = GridBagConstraints.CENTER;

        JLabel lblNomePaciente = new JLabel("Nome: " + (paciente.getNome() != null ? paciente.getNome() : "Não informado"));
        lblNomePaciente.setFont(new Font("SansSerif", Font.BOLD, 16));
        gbcP.gridx = 0;
        gbcP.gridy = 0;
        pacientePanel.add(lblNomePaciente, gbcP);

        JLabel lblTelefone = new JLabel("Telefone: " + (paciente.getTelefone() != null ? paciente.getTelefone() : "Não informado"));
        lblTelefone.setFont(labelFont);
        gbcP.gridy = 1;
        pacientePanel.add(lblTelefone, gbcP);

        long idade = paciente.getDataNascimento() != null
                ? java.time.temporal.ChronoUnit.YEARS.between(paciente.getDataNascimento(), java.time.LocalDate.now())
                : 0;
        JLabel lblIdade = new JLabel("Idade: " + (idade > 0 ? idade : "Não informada"));
        lblIdade.setFont(labelFont);
        gbcP.gridy = 2;
        pacientePanel.add(lblIdade, gbcP);

        JLabel lblEmail = new JLabel("Email: " + (paciente.getEmail() != null ? paciente.getEmail() : "Não informado"));
        lblEmail.setFont(labelFont);
        gbcP.gridy = 3;
        pacientePanel.add(lblEmail, gbcP);

        add(pacientePanel, BorderLayout.NORTH);

        // Painel de evoluções
        evolucoesPanel = new JPanel(new BorderLayout(10, 10));
        evolucoesPanel.setBackground(backgroundColor);
        JScrollPane scrollEvolucoes = new JScrollPane(evolucoesPanel);
        scrollEvolucoes.getVerticalScrollBar().setUnitIncrement(32); // Scroll mais rápido
        add(scrollEvolucoes, BorderLayout.CENTER);

        // Botão fechar
        JButton btnFechar = new JButton("Fechar");
        btnFechar.setBackground(Color.LIGHT_GRAY);
        btnFechar.setForeground(Color.BLACK);
        btnFechar.setPreferredSize(new Dimension(100, 35));
        btnFechar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnFechar.addActionListener(e -> dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(backgroundColor);
        buttonPanel.add(btnFechar);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // Carrega as evoluções do atendimento
    private void carregarEvolucoes() {
        evolucoesPanel.removeAll();

        try {
            List<EvolucaoAtendimento> evolucoes = evolucaoController.listarPorAtendimento(atendimento.getId());
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setBackground(backgroundColor);

            for (EvolucaoAtendimento evo : evolucoes) {
                if (evo.getNotas() != null && !evo.getNotas().isEmpty()) {
                    JTextArea txtNotas = new JTextArea(evo.getNotas());
                    txtNotas.setEditable(false);
                    txtNotas.setLineWrap(true);
                    txtNotas.setWrapStyleWord(true);
                    txtNotas.setBackground(textAreaBackground);
                    txtNotas.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
                    contentPanel.add(new JLabel("Notas:"));
                    contentPanel.add(txtNotas);
                } else if (evo.getArquivo() != null && !evo.getArquivo().isEmpty()) {
                    JLabel lblArquivo = new JLabel("Arquivo: " + evo.getArquivo());
                    lblArquivo.setFont(labelFont);
                    contentPanel.add(lblArquivo);
                }
                contentPanel.add(Box.createVerticalStrut(10)); // Espaçamento entre evoluções
            }

            evolucoesPanel.add(contentPanel, BorderLayout.NORTH);
            evolucoesPanel.revalidate();
            evolucoesPanel.repaint();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar evoluções: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}