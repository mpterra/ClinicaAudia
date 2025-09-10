package view;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import controller.AtendimentoController;
import controller.PacienteController;
import controller.ProfissionalController;
import controller.EscalaProfissionalController;
import exception.CampoObrigatorioException;
import model.Atendimento;
import model.Paciente;
import model.Profissional;
import model.EscalaProfissional;
import util.Sessao;

import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class MarcacaoAtendimentoPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private JComboBox<Paciente> cbPaciente;
    private JComboBox<Profissional> cbProfissional;
    private JComboBox<LocalDate> cbData;
    private JComboBox<LocalTime> cbHorario;
    private JButton btnSalvar, btnLimpar;

    private JTable tabelaAtendimentos;
    private DefaultTableModel modeloTabela;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField tfPesquisar;

    private final DateTimeFormatter formatterData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter formatterHora = DateTimeFormatter.ofPattern("HH:mm");

    public MarcacaoAtendimentoPanel() {
        setLayout(new BorderLayout(10, 20));

        JLabel lblTitulo = new JLabel("Marcação de Atendimento", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(30, 30, 60));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        add(lblTitulo, BorderLayout.NORTH);

        JPanel panelFormulario = criarPainelFormulario();
        JPanel panelTabela = criarTabelaAtendimentosComPesquisa();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelFormulario, panelTabela);
        splitPane.setResizeWeight(0.5);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(5);

        panelFormulario.setPreferredSize(new Dimension(500, 600));
        panelTabela.setPreferredSize(new Dimension(500, 600));

        add(splitPane, BorderLayout.CENTER);

        btnLimpar.addActionListener(e -> limparCampos());
        btnSalvar.addActionListener(e -> {
            try {
                salvarAtendimento();
            } catch (CampoObrigatorioException | SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        carregarPacientes();
        carregarProfissionais();
        carregarDatas();
        carregarAtendimentos();
    }

    private JPanel criarPainelFormulario() {
        JPanel panelWrapper = new JPanel();
        panelWrapper.setLayout(new BoxLayout(panelWrapper, BoxLayout.Y_AXIS));
        panelWrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                "Marcar Atendimento",
                TitledBorder.LEADING,
                TitledBorder.TOP
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);

        // Paciente
        gbc.gridx = 0; gbc.gridy = 0;
        panelForm.add(new JLabel("Paciente:"), gbc);
        cbPaciente = new JComboBox<>();
        cbPaciente.setCursor(handCursor);
        gbc.gridx = 1;
        panelForm.add(cbPaciente, gbc);

        // Profissional
        gbc.gridx = 0; gbc.gridy = 1;
        panelForm.add(new JLabel("Profissional:"), gbc);
        cbProfissional = new JComboBox<>();
        cbProfissional.setCursor(handCursor);
        gbc.gridx = 1;
        panelForm.add(cbProfissional, gbc);

        // Data
        gbc.gridx = 0; gbc.gridy = 2;
        panelForm.add(new JLabel("Data:"), gbc);
        cbData = new JComboBox<>();
        cbData.setCursor(handCursor);
        gbc.gridx = 1;
        panelForm.add(cbData, gbc);

        // Horário
        gbc.gridx = 0; gbc.gridy = 3;
        panelForm.add(new JLabel("Horário:"), gbc);
        cbHorario = new JComboBox<>();
        cbHorario.setCursor(handCursor);
        gbc.gridx = 1;
        panelForm.add(cbHorario, gbc);

        // Atualiza horários sempre que Profissional ou Data mudar
        cbProfissional.addActionListener(e -> atualizarHorariosDisponiveis());
        cbData.addActionListener(e -> atualizarHorariosDisponiveis());

        // Botões
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSalvar = new JButton("Salvar");
        btnLimpar = new JButton("Limpar");
        panelBotoes.add(btnSalvar);
        panelBotoes.add(btnLimpar);

        panelWrapper.add(panelForm);
        panelWrapper.add(Box.createVerticalStrut(10));
        panelWrapper.add(panelBotoes);

        btnSalvar.setCursor(handCursor);
        btnLimpar.setCursor(handCursor);

        return panelWrapper;
    }

    private JPanel criarTabelaAtendimentosComPesquisa() {
        JPanel panelTabelaWrapper = new JPanel(new BorderLayout());

        String[] colunas = {"Paciente", "Profissional", "Data", "Horário", "Situação"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tabelaAtendimentos = new JTable(modeloTabela);
        tabelaAtendimentos.setFillsViewportHeight(true);

        DefaultTableCellRenderer centralizado = new DefaultTableCellRenderer();
        centralizado.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tabelaAtendimentos.getColumnCount(); i++) {
            tabelaAtendimentos.getColumnModel().getColumn(i).setCellRenderer(centralizado);
        }

        sorter = new TableRowSorter<>(modeloTabela);
        tabelaAtendimentos.setRowSorter(sorter);

        JScrollPane scrollTabela = new JScrollPane(tabelaAtendimentos);

        // Painel de pesquisa
        JPanel panelPesquisa = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblPesquisar = new JLabel("Pesquisar atendimento:");
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

        JPanel panelTabelaComMargem = new JPanel(new BorderLayout());
        panelTabelaComMargem.setBorder(BorderFactory.createEmptyBorder(0,0,15,15));
        panelTabelaComMargem.add(scrollTabela, BorderLayout.CENTER);

        panelTabelaWrapper.add(panelTabelaComMargem, BorderLayout.CENTER);

        return panelTabelaWrapper;
    }

    private void carregarPacientes() {
        try {
            PacienteController pc = new PacienteController();
            List<Paciente> pacientes = pc.listarTodos();
            cbPaciente.removeAllItems();
            for (Paciente p : pacientes) {
                cbPaciente.addItem(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void carregarProfissionais() {
        try {
            ProfissionalController profController = new ProfissionalController();
            List<Profissional> profissionais = profController.listarTodos();
            cbProfissional.removeAllItems();
            for (Profissional p : profissionais) {
                cbProfissional.addItem(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void carregarDatas() {
        cbData.removeAllItems();
        LocalDate hoje = LocalDate.now();
        for (int i = 0; i <= 30; i++) { // próximos 30 dias
            cbData.addItem(hoje.plusDays(i));
        }
    }

    private void atualizarHorariosDisponiveis() {
        cbHorario.removeAllItems();
        Profissional prof = (Profissional) cbProfissional.getSelectedItem();
        LocalDate data = (LocalDate) cbData.getSelectedItem();
        if (prof == null || data == null) return;

        try {
            EscalaProfissionalController escalaController = new EscalaProfissionalController();
            AtendimentoController atendimentoController = new AtendimentoController();

            int diaSemana = data.getDayOfWeek().getValue(); // 1=Segunda ... 7=Domingo
            List<EscalaProfissional> escalas = escalaController.listarDisponiveis(prof.getId(), diaSemana);

            List<LocalTime> horariosOcupados = atendimentoController.listarTodos().stream()
                    .filter(a -> a.getProfissional().getId() == prof.getId() && a.getData().equals(data))
                    .map(Atendimento::getHora)
                    .collect(Collectors.toList());

            for (EscalaProfissional e : escalas) {
                LocalTime hora = e.getHoraInicio();
                while (!hora.isAfter(e.getHoraFim().minusMinutes(30))) {
                    if (!horariosOcupados.contains(hora)) {
                        cbHorario.addItem(hora);
                    }
                    hora = hora.plusMinutes(30);
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void limparCampos() {
        cbPaciente.setSelectedIndex(0);
        cbProfissional.setSelectedIndex(0);
        cbData.setSelectedIndex(0);
        cbHorario.removeAllItems();
    }

    private void salvarAtendimento() throws CampoObrigatorioException, SQLException {
        Paciente paciente = (Paciente) cbPaciente.getSelectedItem();
        Profissional profissional = (Profissional) cbProfissional.getSelectedItem();
        LocalDate data = (LocalDate) cbData.getSelectedItem();
        LocalTime hora = (LocalTime) cbHorario.getSelectedItem();

        if (paciente == null || profissional == null || data == null || hora == null) {
            throw new CampoObrigatorioException("Preencha todos os campos obrigatórios!");
        }

        AtendimentoController ac = new AtendimentoController();

        // valida sobreposição
        boolean existe = ac.listarTodos().stream()
                .anyMatch(a -> a.getProfissional().getId() == profissional.getId()
                        && a.getData().equals(data)
                        && a.getHora().equals(hora));

        if (existe) {
            throw new CampoObrigatorioException("Horário já ocupado!");
        }

        Atendimento atendimento = new Atendimento();
        atendimento.setPaciente(paciente);
        atendimento.setProfissional(profissional);
        atendimento.setData(data);
        atendimento.setHora(hora);
        atendimento.setUsuario(Sessao.getUsuarioLogado().getLogin());

        if (ac.salvar(atendimento)) {
            JOptionPane.showMessageDialog(this, "Atendimento salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            limparCampos();
            atualizarHorariosDisponiveis();
            carregarAtendimentos();
        }
    }

    private void carregarAtendimentos() {
        try {
            AtendimentoController ac = new AtendimentoController();
            List<Atendimento> atendimentos = ac.listarTodos();

            modeloTabela.setRowCount(0);
            for (Atendimento a : atendimentos) {
                modeloTabela.addRow(new Object[]{
                        a.getPaciente().getNome(),
                        a.getProfissional().getNome(),
                        a.getData().format(formatterData),
                        a.getHora().format(formatterHora),
                        a.getSituacao()
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao carregar atendimentos: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
