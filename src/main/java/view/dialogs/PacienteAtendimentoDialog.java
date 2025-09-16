package view.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
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
    private JTextArea txtEvolucaoNotas; // Único campo para notas da evolução
    private List<EvolucaoComponent> listaEvolucoesArquivos; // Apenas para arquivos
    private JPanel panelEvolucoesArquivos;
    private JTable tabelaHistorico;
    private DefaultTableModel modeloHistorico;
    private EvolucaoAtendimento evolucaoTextoExistente; // Para gerenciar a evolução de texto existente
    private final DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final Color primaryColor = new Color(30, 144, 255);
    private final Color backgroundColor = new Color(245, 245, 245);
    private final Color textAreaBackground = Color.WHITE; // Fundo branco padrão para campos editáveis
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 14);
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 18);

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

    public PacienteAtendimentoDialog(Frame parent, Atendimento atendimento) {
        super(parent, "Detalhes do Atendimento e Paciente", true);
        this.atendimento = atendimento;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Ajuste de tamanho proporcional à tela
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize((int) (screenSize.width * 0.7), (int) (screenSize.height * 0.7));
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(backgroundColor);

        listaEvolucoesArquivos = new ArrayList<>();
        initComponents();
        carregarDados();
    }

    // Inicializa os componentes da interface
    private void initComponents() {
        // Painel principal com scroll se necessário
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
        tabbedPane.addTab("Atendimento Atual", new JScrollPane(criarPainelAtendimentoAtual(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        tabbedPane.addTab("Histórico do Paciente", criarPainelHistorico());
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(backgroundColor);

        JButton btnSalvar = new JButton("Salvar");
        btnSalvar.setBackground(primaryColor);
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setPreferredSize(new Dimension(100, 35));

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setBackground(Color.LIGHT_GRAY);
        btnCancelar.setForeground(Color.BLACK);
        btnCancelar.setPreferredSize(new Dimension(100, 35));

        buttonPanel.add(btnCancelar);
        buttonPanel.add(btnSalvar);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(new JScrollPane(mainPanel), BorderLayout.CENTER); // Scroll no mainPanel se necessário

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

        // Observações e evoluções
        JPanel formPanel = new JPanel(new BorderLayout(10, 10));
        formPanel.setBackground(backgroundColor);

        // Observações do atendimento
        JPanel obsPanel = new JPanel(new BorderLayout(5, 5));
        obsPanel.setBackground(backgroundColor);
        JLabel lblObservacoes = new JLabel("Observações do Atendimento");
        lblObservacoes.setFont(labelFont);
        lblObservacoes.setForeground(primaryColor);
        obsPanel.add(lblObservacoes, BorderLayout.NORTH);
        obsPanel.add(new JSeparator(), BorderLayout.CENTER);
        txtObservacoesAtendimento = new JTextArea(5, 30);
        txtObservacoesAtendimento.setLineWrap(true);
        txtObservacoesAtendimento.setWrapStyleWord(true);
        txtObservacoesAtendimento.setBackground(textAreaBackground);
        JScrollPane scrollObs = new JScrollPane(txtObservacoesAtendimento);
        scrollObs.setBackground(backgroundColor);
        obsPanel.add(scrollObs, BorderLayout.SOUTH);
        formPanel.add(obsPanel, BorderLayout.NORTH);

        // Evoluções
        JPanel evolucoesPanel = new JPanel(new BorderLayout(10, 10));
        evolucoesPanel.setBackground(backgroundColor);

        // Notas da evolução
        JPanel notasPanel = new JPanel(new BorderLayout(5, 5));
        notasPanel.setBackground(backgroundColor);
        JLabel lblNotasEvolucao = new JLabel("Notas da Evolução");
        lblNotasEvolucao.setFont(labelFont);
        lblNotasEvolucao.setForeground(primaryColor);
        notasPanel.add(lblNotasEvolucao, BorderLayout.NORTH);
        notasPanel.add(new JSeparator(), BorderLayout.CENTER);
        txtEvolucaoNotas = new JTextArea(5, 30);
        txtEvolucaoNotas.setLineWrap(true);
        txtEvolucaoNotas.setWrapStyleWord(true);
        txtEvolucaoNotas.setBackground(textAreaBackground);
        JScrollPane scrollNotas = new JScrollPane(txtEvolucaoNotas);
        scrollNotas.setBackground(backgroundColor);
        notasPanel.add(scrollNotas, BorderLayout.SOUTH);
        evolucoesPanel.add(notasPanel, BorderLayout.NORTH);

        // Arquivos de evolução
        panelEvolucoesArquivos = new JPanel();
        panelEvolucoesArquivos.setLayout(new BoxLayout(panelEvolucoesArquivos, BoxLayout.Y_AXIS));
        panelEvolucoesArquivos.setBackground(backgroundColor);
        JScrollPane scrollEvolucoes = new JScrollPane(panelEvolucoesArquivos);
        scrollEvolucoes.setBackground(backgroundColor);
        JPanel evolucoesHeader = new JPanel(new BorderLayout(5, 5));
        evolucoesHeader.setBackground(backgroundColor);
        JLabel lblEvolucoes = new JLabel("Arquivos Anexados");
        lblEvolucoes.setFont(labelFont);
        lblEvolucoes.setForeground(primaryColor);
        evolucoesHeader.add(lblEvolucoes, BorderLayout.NORTH);
        evolucoesHeader.add(new JSeparator(), BorderLayout.CENTER);
        panelEvolucoesArquivos.add(evolucoesHeader);

        // Botões para adicionar evoluções
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(backgroundColor);
        JButton btnAnexarDocumento = new JButton("Anexar Documento");
        btnAnexarDocumento.setBackground(primaryColor);
        btnAnexarDocumento.setForeground(Color.WHITE);
        btnAnexarDocumento.addActionListener(e -> adicionarEvolucaoArquivo());
        btnPanel.add(btnAnexarDocumento);

        evolucoesPanel.add(scrollEvolucoes, BorderLayout.CENTER);
        formPanel.add(evolucoesPanel, BorderLayout.CENTER);
        formPanel.add(btnPanel, BorderLayout.SOUTH);

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
        String[] colunas = {"Data/Hora", "Profissional", "Tipo", "Situação", "Observações", "Evoluções"};
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

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tabelaHistorico.getColumnCount(); i++) {
            tabelaHistorico.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollTabela = new JScrollPane(tabelaHistorico);
        scrollTabela.setBackground(backgroundColor);
        panel.add(scrollTabela, BorderLayout.CENTER);

        // Ajustar largura das colunas proporcionalmente
        tabelaHistorico.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int larguraTotal = tabelaHistorico.getWidth();
                if (larguraTotal > 0) {
                    tabelaHistorico.getColumnModel().getColumn(0).setPreferredWidth((int)(larguraTotal * 0.15));
                    tabelaHistorico.getColumnModel().getColumn(1).setPreferredWidth((int)(larguraTotal * 0.20));
                    tabelaHistorico.getColumnModel().getColumn(2).setPreferredWidth((int)(larguraTotal * 0.15));
                    tabelaHistorico.getColumnModel().getColumn(3).setPreferredWidth((int)(larguraTotal * 0.15));
                    tabelaHistorico.getColumnModel().getColumn(4).setPreferredWidth((int)(larguraTotal * 0.20));
                    tabelaHistorico.getColumnModel().getColumn(5).setPreferredWidth((int)(larguraTotal * 0.15));
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
                List<EvolucaoAtendimento> evolucoes = evolucaoController.listarPorAtendimento(at.getId());
                String evolucoesStr = evolucoes.stream()
                        .map(e -> {
                            String str = "";
                            if (e.getNotas() != null && !e.getNotas().isEmpty()) str += e.getNotas();
                            if (e.getArquivo() != null && !e.getArquivo().isEmpty()) str += (str.isEmpty() ? "" : "; ") + "Arquivo: " + e.getArquivo();
                            return str;
                        })
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.joining("; "));
                modeloHistorico.addRow(new Object[]{
                        at.getDataHora().toLocalDateTime().format(formatoData),
                        at.getProfissionalNome(),
                        at.getTipo(),
                        at.getSituacao(),
                        at.getNotas(),
                        evolucoesStr
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar histórico: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Adiciona uma evolução com arquivo
    private void adicionarEvolucaoArquivo() {
        adicionarEvolucaoArquivo(new EvolucaoAtendimento());
    }

    private void adicionarEvolucaoArquivo(EvolucaoAtendimento evo) {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            evo.setArquivo(file.getAbsolutePath());

            EvolucaoComponent comp = new EvolucaoComponent(evo, "arquivo");
            comp.lblArquivo = new JLabel("Arquivo: " + file.getAbsolutePath());
            comp.lblArquivo.setFont(labelFont);
            comp.panel.add(comp.lblArquivo, BorderLayout.CENTER);

            JButton btnRemover = new JButton("Remover");
            btnRemover.setBackground(new Color(255, 99, 71));
            btnRemover.setForeground(Color.WHITE);
            btnRemover.addActionListener(e -> removerEvolucaoArquivo(comp));
            comp.panel.add(btnRemover, BorderLayout.EAST);

            listaEvolucoesArquivos.add(comp);
            panelEvolucoesArquivos.add(comp.panel);
            panelEvolucoesArquivos.revalidate();
            panelEvolucoesArquivos.repaint();
        }
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