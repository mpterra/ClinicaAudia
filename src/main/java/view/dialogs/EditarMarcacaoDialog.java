package view.dialogs;

import controller.AtendimentoController;
import controller.EmpresaParceiraController;
import controller.EscalaProfissionalController;
import controller.ProfissionalController;
import controller.PacienteController;
import controller.ValorAtendimentoController;
import exception.CampoObrigatorioException;
import model.Atendimento;
import model.EmpresaParceira;
import model.EscalaProfissional;
import model.Paciente;
import model.Profissional;
import model.ValorAtendimento;
import service.ValorAtendimentoCalculator;
import util.Sessao;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Classe auxiliar para intervalos de tempo
class Intervalo {
    LocalTime inicio;
    LocalTime fim;
    Intervalo(LocalTime inicio, LocalTime fim) {
        this.inicio = inicio;
        this.fim = fim;
    }
}

public class EditarMarcacaoDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private Atendimento atendimento;
    private final AtendimentoController atendimentoController = new AtendimentoController();
    private final ProfissionalController profissionalController = new ProfissionalController();
    private final EscalaProfissionalController escalaController = new EscalaProfissionalController();
    private final PacienteController pacienteController = new PacienteController();
    private final ValorAtendimentoController valorAtendimentoController = new ValorAtendimentoController();
    private final EmpresaParceiraController empresaParceiraController = new EmpresaParceiraController();
    private final ValorAtendimentoCalculator valorCalculator;
    private JLabel lblNomePaciente;
    private JLabel lblTelefone;
    private JLabel lblIdade;
    private JLabel lblEmail;
    private JComboBox<Profissional> cbProfissional;
    private JComboBox<Atendimento.Tipo> cbTipo;
    private JComboBox<Atendimento.Situacao> cbSituacao;
    private JComboBox<LocalTime> cbHorario;
    private JTextPane txtObservacoes;
    private JComboBox<String> cbData;
    private JComboBox<EmpresaParceira> cbEmpresaParceira;
    private JLabel lblValor;
    private JLabel lblStatusPagamento;
    private JButton btnReceberPagamento;
    private final DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final Color primaryColor = new Color(30, 144, 255);
    private final Color backgroundColor = new Color(245, 245, 245);
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 14);
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 18);
    private final Font boldFont = new Font("SansSerif", Font.BOLD, 14);

    // Construtor do diálogo
    public EditarMarcacaoDialog(Frame parent, Atendimento atendimento) {
        super(parent, "Editar Atendimento", true);
        this.atendimento = atendimento;
        this.valorCalculator = new ValorAtendimentoCalculator(valorAtendimentoController, new controller.ValorAtendimentoEmpresaController());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(700, 650);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        setBackground(backgroundColor);
        initComponents();
        try {
            carregarDadosIniciais();
            try {
                Atendimento full = atendimentoController.buscarPorId(this.atendimento.getId());
                if (full != null) this.atendimento = full;
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Aviso: falha ao recarregar atendimento: " + ex.getMessage(),
                        "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados iniciais: " + e.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
        preencherCampos();
    }

    // Inicializa os componentes da interface
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(backgroundColor);
        // Título
        JLabel lblTitulo = new JLabel("Editar Atendimento", SwingConstants.CENTER);
        lblTitulo.setFont(titleFont);
        lblTitulo.setForeground(primaryColor);
        lblTitulo.setBorder(new EmptyBorder(0, 0, 20, 0));
        add(lblTitulo, BorderLayout.NORTH);
        // Painel principal do formulário
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(backgroundColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        // Painel dados do paciente
        JPanel pacientePanel = new JPanel(new GridBagLayout());
        pacientePanel.setBackground(backgroundColor);
        pacientePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(primaryColor), "Dados do Paciente",
                        TitledBorder.CENTER, TitledBorder.TOP, labelFont, primaryColor),
                new EmptyBorder(15, 15, 15, 15)));
        GridBagConstraints gbcP = new GridBagConstraints();
        gbcP.insets = new Insets(5, 0, 5, 0);
        gbcP.anchor = GridBagConstraints.CENTER;
        lblNomePaciente = new JLabel();
        lblNomePaciente.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblNomePaciente.setHorizontalAlignment(SwingConstants.CENTER);
        gbcP.gridx = 0;
        gbcP.gridy = 0;
        pacientePanel.add(lblNomePaciente, gbcP);
        lblTelefone = new JLabel();
        lblTelefone.setFont(labelFont);
        lblTelefone.setHorizontalAlignment(SwingConstants.CENTER);
        gbcP.gridy = 1;
        pacientePanel.add(lblTelefone, gbcP);
        lblIdade = new JLabel();
        lblIdade.setFont(labelFont);
        lblIdade.setHorizontalAlignment(SwingConstants.CENTER);
        gbcP.gridy = 2;
        pacientePanel.add(lblIdade, gbcP);
        lblEmail = new JLabel();
        lblEmail.setFont(labelFont);
        lblEmail.setHorizontalAlignment(SwingConstants.CENTER);
        gbcP.gridy = 3;
        pacientePanel.add(lblEmail, gbcP);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        formPanel.add(pacientePanel, gbc);
        // Profissional
        JLabel lblProfissional = new JLabel("Profissional:");
        lblProfissional.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 15, 5, 5);
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(lblProfissional, gbc);
        cbProfissional = new JComboBox<>();
        cbProfissional.setPreferredSize(new Dimension(200, 25));
        cbProfissional.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 15);
        formPanel.add(cbProfissional, gbc);
        // Empresa Parceira
        JLabel lblEmpresaParceira = new JLabel("Empresa Parceira:");
        lblEmpresaParceira.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 15, 5, 5);
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(lblEmpresaParceira, gbc);
        cbEmpresaParceira = new JComboBox<>();
        cbEmpresaParceira.setPreferredSize(new Dimension(200, 25));
        cbEmpresaParceira.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cbEmpresaParceira.addItem(null);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 15);
        formPanel.add(cbEmpresaParceira, gbc);
        // Linha dupla: Tipo | Situação
        JLabel lblTipo = new JLabel("Tipo:");
        lblTipo.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 15, 5, 5);
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(lblTipo, gbc);
        cbTipo = new JComboBox<>(Atendimento.Tipo.values());
        cbTipo.setPreferredSize(new Dimension(150, 25));
        cbTipo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridx = 1;
        gbc.insets = new Insets(5, 5, 5, 15);
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(cbTipo, gbc);
        JLabel lblSituacao = new JLabel("Situação:");
        lblSituacao.setFont(labelFont);
        gbc.gridx = 2;
        gbc.insets = new Insets(5, 15, 5, 5);
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(lblSituacao, gbc);
        cbSituacao = new JComboBox<>(Atendimento.Situacao.values());
        cbSituacao.setPreferredSize(new Dimension(150, 25));
        cbSituacao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridx = 3;
        gbc.insets = new Insets(5, 5, 5, 15);
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(cbSituacao, gbc);
        // Linha dupla: Data | Horário
        JLabel lblData = new JLabel("Data:");
        lblData.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.insets = new Insets(5, 15, 5, 5);
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(lblData, gbc);
        cbData = new JComboBox<>();
        cbData.setPreferredSize(new Dimension(150, 25));
        cbData.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridx = 1;
        gbc.insets = new Insets(5, 5, 5, 15);
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(cbData, gbc);
        JLabel lblHorario = new JLabel("Horário:");
        lblHorario.setFont(labelFont);
        gbc.gridx = 2;
        gbc.insets = new Insets(5, 15, 5, 5);
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(lblHorario, gbc);
        cbHorario = new JComboBox<>();
        cbHorario.setPreferredSize(new Dimension(150, 25));
        cbHorario.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridx = 3;
        gbc.insets = new Insets(5, 5, 5, 15);
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(cbHorario, gbc);
        // Linha dupla: Valor | Status Pagamento
        JLabel lblValorTitle = new JLabel("Valor:");
        lblValorTitle.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.insets = new Insets(5, 15, 5, 5);
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(lblValorTitle, gbc);
        lblValor = new JLabel();
        lblValor.setFont(labelFont);
        gbc.gridx = 1;
        gbc.insets = new Insets(5, 5, 5, 15);
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(lblValor, gbc);
        JLabel lblStatusPagamentoTitle = new JLabel("Status Pagamento:");
        lblStatusPagamentoTitle.setFont(labelFont);
        gbc.gridx = 2;
        gbc.insets = new Insets(5, 15, 5, 5);
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(lblStatusPagamentoTitle, gbc);
        lblStatusPagamento = new JLabel();
        lblStatusPagamento.setFont(boldFont);
        gbc.gridx = 3;
        gbc.insets = new Insets(5, 5, 5, 15);
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(lblStatusPagamento, gbc);
        // Observações
        JLabel lblObservacoes = new JLabel("Observações:");
        lblObservacoes.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 1.0;
        formPanel.add(lblObservacoes, gbc);
        txtObservacoes = new JTextPane();
        txtObservacoes.setContentType("text/html");
        txtObservacoes.setEditorKit(new HTMLEditorKit());
        txtObservacoes.setText("<html></html>");
        JScrollPane scrollObservacoes = new JScrollPane(txtObservacoes);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(scrollObservacoes, gbc);
        // Painel de botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(backgroundColor);
        btnReceberPagamento = new JButton("Receber Pagamento");
        btnReceberPagamento.setBackground(new Color(34, 139, 34));
        btnReceberPagamento.setForeground(Color.WHITE);
        btnReceberPagamento.setPreferredSize(new Dimension(150, 35));
        btnReceberPagamento.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnReceberPagamento.setEnabled(false);
        JButton btnSalvar = new JButton("Salvar");
        btnSalvar.setBackground(primaryColor);
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setPreferredSize(new Dimension(100, 35));
        btnSalvar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JButton btnExcluir = new JButton("Excluir");
        btnExcluir.setBackground(new Color(255, 99, 71));
        btnExcluir.setForeground(Color.WHITE);
        btnExcluir.setPreferredSize(new Dimension(100, 35));
        btnExcluir.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setBackground(Color.LIGHT_GRAY);
        btnCancelar.setForeground(Color.BLACK);
        btnCancelar.setPreferredSize(new Dimension(100, 35));
        btnCancelar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        buttonPanel.add(btnCancelar);
        buttonPanel.add(btnReceberPagamento);
        buttonPanel.add(btnExcluir);
        buttonPanel.add(btnSalvar);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);
        // Ações dos botões
        btnSalvar.addActionListener(e -> salvar());
        btnExcluir.addActionListener(e -> excluir());
        btnCancelar.addActionListener(e -> dispose());
        btnReceberPagamento.addActionListener(e -> receberPagamento());
        cbProfissional.addActionListener(e -> {
            atualizarHorarios();
            atualizarValor();
        });
        cbData.addActionListener(e -> atualizarHorarios());
        cbTipo.addActionListener(e -> {
            atualizarHorarios();
            atualizarValor();
        });
        cbEmpresaParceira.addActionListener(e -> atualizarValor());
    }

    // Preenche os campos com os dados do atendimento
    private void preencherCampos() {
        try {
            Paciente paciente = atendimento.getPaciente();
            if (paciente != null && paciente.getId() > 0) {
                try {
                    paciente = pacienteController.buscarPorId(paciente.getId());
                } catch (SQLException ignored) {}
            }
            lblNomePaciente.setText("Nome: " + (paciente != null && paciente.getNome() != null ? paciente.getNome() : "Não informado"));
            lblTelefone.setText("Telefone: " + (paciente != null && paciente.getTelefone() != null ? paciente.getTelefone() : "Não informado"));
            long idade = paciente != null && paciente.getDataNascimento() != null
                    ? java.time.temporal.ChronoUnit.YEARS.between(paciente.getDataNascimento(), LocalDate.now())
                    : 0;
            lblIdade.setText("Idade: " + (idade > 0 ? idade : "Não informada"));
            lblEmail.setText("Email: " + (paciente != null && paciente.getEmail() != null ? paciente.getEmail() : "Não informado"));
        } catch (Exception e) {
            lblNomePaciente.setText("Nome: Não informado");
            lblTelefone.setText("Telefone: Não informado");
            lblIdade.setText("Idade: Não informada");
            lblEmail.setText("Email: Não informado");
        }
        if (atendimento.getProfissional() != null) {
            selectProfissionalById(atendimento.getProfissional().getId());
        } else {
            cbProfissional.setSelectedIndex(-1);
        }
        if (atendimento.getEmpresaParceira() != null) {
            selectEmpresaParceiraById(atendimento.getEmpresaParceira().getId());
        } else {
            cbEmpresaParceira.setSelectedItem(null);
        }
        cbTipo.setSelectedItem(atendimento.getTipo());
        cbSituacao.setSelectedItem(atendimento.getSituacao());
        String dataStr = atendimento.getDataHora().toLocalDateTime().toLocalDate().format(formatoData);
        boolean found = false;
        for (int i = 0; i < cbData.getItemCount(); i++) {
            if (cbData.getItemAt(i).equals(dataStr)) {
                found = true;
                break;
            }
        }
        if (!found) cbData.insertItemAt(dataStr, 0);
        cbData.setSelectedItem(dataStr);
        txtObservacoes.setText(atendimento.getNotas() != null ? "<html>" + atendimento.getNotas() + "</html>" : "<html></html>");
        atualizarValor();
        atualizarHorarios();
    }

    // Carrega dados iniciais (profissionais, empresas e datas)
    private void carregarDadosIniciais() throws SQLException {
        // Carrega profissionais (apenas FONOAUDIOLOGA e ativos)
        cbProfissional.removeAllItems();
        profissionalController.buscarPorTipo("FONOAUDIOLOGA").stream()
                .filter(Profissional::isAtivo)
                .forEach(cbProfissional::addItem);
        // Carrega empresas parceiras
        cbEmpresaParceira.removeAllItems();
        cbEmpresaParceira.addItem(null);
        empresaParceiraController.listarTodos().forEach(cbEmpresaParceira::addItem);
        // Carrega datas
        cbData.removeAllItems();
        LocalDate hoje = LocalDate.now();
        for (int i = 0; i < 30; i++) {
            LocalDate data = hoje.plusDays(i);
            cbData.addItem(data.format(formatoData));
        }
        if (atendimento != null && atendimento.getDataHora() != null) {
            String dataAt = atendimento.getDataHora().toLocalDateTime().toLocalDate().format(formatoData);
            boolean achou = false;
            for (int i = 0; i < cbData.getItemCount(); i++) {
                if (cbData.getItemAt(i).equals(dataAt)) {
                    achou = true;
                    break;
                }
            }
            if (!achou) cbData.insertItemAt(dataAt, 0);
            cbData.setSelectedItem(dataAt);
        }
    }

    // Atualiza os horários disponíveis, considerando a duração e excluindo cancelados
    private void atualizarHorarios() {
        cbHorario.removeAllItems();
        cbHorario.setEnabled(false);
        Profissional prof = (Profissional) cbProfissional.getSelectedItem();
        String dataStr = (String) cbData.getSelectedItem();
        Atendimento.Tipo tipo = (Atendimento.Tipo) cbTipo.getSelectedItem();
        if (prof == null || dataStr == null || tipo == null) return;
        try {
            LocalDate data = LocalDate.parse(dataStr, formatoData);
            int diaSemana = data.getDayOfWeek().getValue() - 1;
            int duracaoMin = (tipo == Atendimento.Tipo.AVALIACAO) ? 90 : 60;
            // Carrega escalas do profissional para o dia da semana
            List<EscalaProfissional> escalas = escalaController.listarTodas().stream()
                    .filter(e -> e.getProfissionalId() == prof.getId() && e.getDiaSemana() == diaSemana && e.isDisponivel())
                    .collect(Collectors.toList());
            // Carrega intervalos ocupados (excluindo CANCELADO e o próprio atendimento)
            List<Atendimento> atendimentos = atendimentoController.listarTodos().stream()
                    .filter(a -> a.getProfissional().getId() == prof.getId()
                            && a.getDataHora().toLocalDateTime().toLocalDate().equals(data)
                            && a.getSituacao() != Atendimento.Situacao.CANCELADO
                            && a.getId() != atendimento.getId())
                    .collect(Collectors.toList());
            List<Intervalo> ocupados = new ArrayList<>();
            for (Atendimento a : atendimentos) {
                LocalTime inicio = a.getDataHora().toLocalDateTime().toLocalTime();
                LocalTime fim = inicio.plusMinutes(a.getDuracaoMin());
                ocupados.add(new Intervalo(inicio, fim));
            }
            // Gera horários disponíveis
            LocalTime horarioOriginal = atendimento.getDataHora().toLocalDateTime().toLocalTime();
            boolean horarioOriginalAdicionado = false;
            for (EscalaProfissional e : escalas) {
                LocalTime hora = e.getHoraInicio().toLocalTime();
                LocalTime fimEscala = e.getHoraFim().toLocalTime();
                while (!hora.isAfter(fimEscala.minusMinutes(duracaoMin))) {
                    LocalTime fimProposto = hora.plusMinutes(duracaoMin);
                    boolean sobreposto = false;
                    for (Intervalo occ : ocupados) {
                        if (!(fimProposto.compareTo(occ.inicio) <= 0 || hora.compareTo(occ.fim) >= 0)) {
                            sobreposto = true;
                            break;
                        }
                    }
                    if (hora.equals(horarioOriginal) && !sobreposto) {
                        cbHorario.addItem(horarioOriginal);
                        horarioOriginalAdicionado = true;
                    } else if (!sobreposto && !hora.equals(horarioOriginal)) {
                        cbHorario.addItem(hora);
                    }
                    hora = hora.plusMinutes(30);
                }
            }
            if (!horarioOriginalAdicionado) {
                cbHorario.addItem(horarioOriginal);
            }
            cbHorario.setSelectedItem(horarioOriginal);
            cbHorario.setEnabled(cbHorario.getItemCount() > 0);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar horários: " + ex.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Atualiza o valor do atendimento com base no profissional, tipo e empresa
    private void atualizarValor() {
        Color pago = new Color(0, 150, 10);
        Color pendente = new Color(255, 0, 0);
        Color isento = Color.BLACK;
        Profissional prof = (Profissional) cbProfissional.getSelectedItem();
        Atendimento.Tipo tipo = (Atendimento.Tipo) cbTipo.getSelectedItem();
        EmpresaParceira empresa = (EmpresaParceira) cbEmpresaParceira.getSelectedItem();
        if (prof != null && tipo != null) {
            try {
                if (tipo == Atendimento.Tipo.REUNIAO || tipo == Atendimento.Tipo.PESSOAL) {
                    atendimento.setValor(java.math.BigDecimal.ZERO);
                    atendimento.setStatusPagamento(Atendimento.StatusPagamento.ISENTO);
                    lblValor.setText("R$ 0,00");
                    lblStatusPagamento.setText(Atendimento.StatusPagamento.ISENTO.name());
                    lblStatusPagamento.setFont(boldFont);
                    lblStatusPagamento.setForeground(isento);
                    btnReceberPagamento.setEnabled(false);
                    return;
                }
                BigDecimal valor = valorCalculator.calcularValor(prof, tipo, empresa);
                atendimento.setValor(valor);
                lblValor.setText("R$ " + String.format("%.2f", valor).replace(".", ","));
                if (valor.compareTo(BigDecimal.ZERO) == 0) {
                    atendimento.setStatusPagamento(Atendimento.StatusPagamento.ISENTO);
                    lblStatusPagamento.setForeground(isento);
                } else {
                    if (atendimento.getStatusPagamento() != Atendimento.StatusPagamento.ISENTO) {
                        lblStatusPagamento.setForeground(
                                atendimento.getStatusPagamento() == Atendimento.StatusPagamento.PAGO ? pago : pendente);
                    } else {
                        atendimento.setStatusPagamento(Atendimento.StatusPagamento.PENDENTE);
                        lblStatusPagamento.setForeground(pendente);
                    }
                }
            } catch (Exception ex) {
                atendimento.setValor(BigDecimal.ZERO);
                atendimento.setStatusPagamento(Atendimento.StatusPagamento.ISENTO);
                lblValor.setText("R$ 0,00");
                lblStatusPagamento.setForeground(isento);
            }
        } else {
            atendimento.setValor(BigDecimal.ZERO);
            atendimento.setStatusPagamento(Atendimento.StatusPagamento.ISENTO);
            lblValor.setText("R$ 0,00");
            lblStatusPagamento.setForeground(isento);
        }
        lblStatusPagamento.setText(atendimento.getStatusPagamento().name());
        lblStatusPagamento.setFont(boldFont);
        boolean podeCobrar = atendimento.getValor().compareTo(BigDecimal.ZERO) > 0 &&
                atendimento.getStatusPagamento() != Atendimento.StatusPagamento.PAGO;
        btnReceberPagamento.setEnabled(podeCobrar);
    }

    // Abre a dialog para receber pagamento
    private void receberPagamento() {
        try {
            ReceberPagamentoAtendimentoDialog pagamentoDialog = new ReceberPagamentoAtendimentoDialog(this, atendimento);
            pagamentoDialog.setVisible(true);
            Atendimento updatedAtendimento = atendimentoController.buscarPorId(atendimento.getId());
            if (updatedAtendimento != null) {
                this.atendimento = updatedAtendimento;
                atualizarValor();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao abrir diálogo de pagamento: " + ex.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Salva as alterações no atendimento
    private void salvar() {
        try {
            Profissional prof = (Profissional) cbProfissional.getSelectedItem();
            Atendimento.Tipo tipo = (Atendimento.Tipo) cbTipo.getSelectedItem();
            Atendimento.Situacao situacao = (Atendimento.Situacao) cbSituacao.getSelectedItem();
            LocalTime hora = (LocalTime) cbHorario.getSelectedItem();
            String dataStr = (String) cbData.getSelectedItem();
            EmpresaParceira empresa = (EmpresaParceira) cbEmpresaParceira.getSelectedItem();
            if (prof == null || tipo == null || situacao == null || hora == null || dataStr == null) {
                throw new CampoObrigatorioException("Preencha todos os campos!");
            }
            LocalDate data = LocalDate.parse(dataStr, formatoData);
            LocalDateTime originalDataHora = atendimento.getDataHora().toLocalDateTime();
            LocalDate originalData = originalDataHora.toLocalDate();
            LocalTime originalHora = originalDataHora.toLocalTime();
            boolean dataHoraMudou = !data.equals(originalData) || !hora.equals(originalHora);
            if (dataHoraMudou) {
                LocalDate hoje = LocalDate.now();
                LocalTime agora = LocalTime.now();
                if (data.isBefore(hoje) || (data.equals(hoje) && hora.isBefore(agora))) {
                    throw new CampoObrigatorioException("Não é possível agendar consultas em datas ou horários passados!");
                }
            }
            atendimento.setProfissional(prof);
            atendimento.setEmpresaParceira(empresa);
            atendimento.setDataHora(Timestamp.valueOf(data.atTime(hora)));
            atendimento.setDuracaoMin(tipo == Atendimento.Tipo.AVALIACAO ? 90 : 60);
            atendimento.setTipo(tipo);
            atendimento.setSituacao(situacao);
            atendimento.setNotas(txtObservacoes.getText().replaceAll("<html>|</html>", ""));
            atualizarValor();
            if (atendimentoController.atualizarAtendimento(atendimento, Sessao.getUsuarioLogado().getLogin())) {
                JOptionPane.showMessageDialog(this, "Atendimento atualizado com sucesso!", "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Exclui o atendimento
    private void excluir() {
        int confirm = JOptionPane.showConfirmDialog(this, "Deseja realmente excluir este atendimento?", "Confirmação",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (atendimentoController.removerAtendimento(atendimento.getId())) {
                    JOptionPane.showMessageDialog(this, "Atendimento excluído com sucesso!", "Sucesso",
                            JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir atendimento: " + e.getMessage(), "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Seleciona profissional pelo ID
    private void selectProfissionalById(int profId) {
        for (int i = 0; i < cbProfissional.getItemCount(); i++) {
            Profissional p = cbProfissional.getItemAt(i);
            if (p != null && p.getId() == profId) {
                cbProfissional.setSelectedIndex(i);
                return;
            }
        }
        cbProfissional.setSelectedIndex(-1);
    }

    // Seleciona empresa parceira pelo ID
    private void selectEmpresaParceiraById(int empresaId) {
        for (int i = 0; i < cbEmpresaParceira.getItemCount(); i++) {
            EmpresaParceira e = cbEmpresaParceira.getItemAt(i);
            if (e != null && e.getId() == empresaId) {
                cbEmpresaParceira.setSelectedIndex(i);
                return;
            }
        }
        cbEmpresaParceira.setSelectedItem(null);
    }
}