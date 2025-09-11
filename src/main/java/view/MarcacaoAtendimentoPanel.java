package view;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import com.toedter.calendar.JCalendar;
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

public class MarcacaoAtendimentoPanel extends JPanel {

    private JComboBox<Paciente> cbPaciente;
    private JComboBox<Profissional> cbProfissional;
    private JCalendar calendar;
    private JComboBox<LocalTime> cbHorario;
    private JButton btnSalvar, btnLimpar;
    private JTable tabelaAtendimentos;
    private DefaultTableModel modeloTabela;
    private JTextField tfPesquisar;

    private final PacienteController pacienteController = new PacienteController();
    private final ProfissionalController profissionalController = new ProfissionalController();
    private final EscalaProfissionalController escalaController = new EscalaProfissionalController();
    private final AtendimentoController atendimentoController = new AtendimentoController();

    private final DateTimeFormatter formatterData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter formatterHora = DateTimeFormatter.ofPattern("HH:mm");

    public MarcacaoAtendimentoPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 245, 245));

        // Título
        JLabel titulo = new JLabel("Marcação de Atendimento", JLabel.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titulo.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        add(titulo, BorderLayout.NORTH);

        // Painéis
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerSize(5);
        add(splitPane, BorderLayout.CENTER);

        splitPane.setLeftComponent(criarPainelFormulario());
        splitPane.setRightComponent(criarPainelTabela());

        carregarDadosIniciais();
    }

    private JPanel criarPainelFormulario() {
        JPanel panelWrapper = new JPanel();
        panelWrapper.setLayout(new BoxLayout(panelWrapper, BoxLayout.Y_AXIS));
        panelWrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // apenas padding

        JPanel panelFormulario = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);

        // Paciente
        gbc.gridx = 0;
        gbc.gridy = 0;
        panelFormulario.add(new JLabel("Paciente:"), gbc);
        cbPaciente = new JComboBox<>();
        cbPaciente.setPreferredSize(new Dimension(300, 30));
        gbc.gridx = 1;
        panelFormulario.add(cbPaciente, gbc);

        // Profissional
        gbc.gridx = 0;
        gbc.gridy = 1;
        panelFormulario.add(new JLabel("Profissional:"), gbc);
        cbProfissional = new JComboBox<>();
        cbProfissional.setPreferredSize(new Dimension(300, 30));
        gbc.gridx = 1;
        panelFormulario.add(cbProfissional, gbc);

        // Data
        gbc.gridx = 0;
        gbc.gridy = 2;
        panelFormulario.add(new JLabel("Data:"), gbc);
        calendar = new JCalendar();
        calendar.setPreferredSize(new Dimension(320, 150));
        gbc.gridx = 1;
        panelFormulario.add(calendar, gbc);

        // Horário
        gbc.gridx = 0;
        gbc.gridy = 3;
        panelFormulario.add(new JLabel("Horário:"), gbc);
        cbHorario = new JComboBox<>();
        cbHorario.setPreferredSize(new Dimension(300, 30));
        cbHorario.setEnabled(false);
        gbc.gridx = 1;
        panelFormulario.add(cbHorario, gbc);

        // Botões
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSalvar = new JButton("Salvar");
        btnLimpar = new JButton("Limpar");
        panelBotoes.add(btnSalvar);
        panelBotoes.add(btnLimpar);

        panelWrapper.add(panelFormulario);
        panelWrapper.add(Box.createVerticalStrut(15));
        panelWrapper.add(panelBotoes);

        // Listeners
        calendar.addPropertyChangeListener("calendar", evt -> atualizarHorarios());
        cbProfissional.addActionListener(e -> atualizarHorarios());
        btnSalvar.addActionListener(e -> salvarAtendimento());
        btnLimpar.addActionListener(e -> limparCampos());

        return panelWrapper;
    }

    private JPanel criarPainelTabela() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 15));

        JPanel innerPanel = new JPanel(new BorderLayout(5, 5));

        modeloTabela = new DefaultTableModel(new String[]{"Paciente", "Profissional", "Data", "Horário", "Situação"}, 0);
        tabelaAtendimentos = new JTable(modeloTabela);
        tabelaAtendimentos.getColumnModel().getColumn(4).setCellRenderer(new SituacaoRenderer());

        tfPesquisar = new JTextField();
        tfPesquisar.setPreferredSize(new Dimension(250, 25));

        JPanel painelPesquisa = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        painelPesquisa.add(new JLabel("Pesquisar: "));
        painelPesquisa.add(tfPesquisar);

        innerPanel.add(painelPesquisa, BorderLayout.NORTH);
        innerPanel.add(new JScrollPane(tabelaAtendimentos), BorderLayout.CENTER);

        tfPesquisar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            private void filtrar() {
                String text = tfPesquisar.getText();
                TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modeloTabela);
                tabelaAtendimentos.setRowSorter(sorter);
                if (text.isEmpty()) sorter.setRowFilter(null);
                else sorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + text));
            }
        });

        panel.add(innerPanel, BorderLayout.CENTER);
        return panel;
    }

    private void carregarDadosIniciais() {
        try {
            cbPaciente.removeAllItems();
            pacienteController.listarTodos().forEach(cbPaciente::addItem);

            cbProfissional.removeAllItems();
            profissionalController.listarTodos().stream().filter(Profissional::isAtivo).forEach(cbProfissional::addItem);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados: " + e.getMessage());
        }
        carregarAtendimentos();
    }

    private void atualizarHorarios() {
        cbHorario.removeAllItems();
        cbHorario.setEnabled(false);

        Profissional prof = (Profissional) cbProfissional.getSelectedItem();
        if (prof == null) return;

        Calendar cal = calendar.getCalendar();
        if (cal == null) return;

        LocalDate data = cal.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // Ajuste: Java DayOfWeek -> 1=segunda ... 7=domingo
        int diaSemana = data.getDayOfWeek().getValue(); 

        try {
            // 1) filtrar escalas do profissional e dia correto
            List<EscalaProfissional> escalas = escalaController.listarTodas().stream()
                    .filter(e -> e.getProfissionalId() == prof.getId()
                            && e.getDiaSemana() == diaSemana
                            && e.isDisponivel())
                    .collect(Collectors.toList());

            // 2) pegar horários já ocupados
            List<LocalTime> ocupados = atendimentoController.listarTodos().stream()
                    .filter(a -> a.getProfissional().getId() == prof.getId()
                            && a.getDataHora().toLocalDateTime().toLocalDate().isEqual(data))
                    .map(a -> a.getDataHora().toLocalDateTime().toLocalTime())
                    .collect(Collectors.toList());

            // 3) popular combobox de horários
            for (EscalaProfissional e : escalas) {
                LocalTime hora = e.getHoraInicio().toLocalTime();
                LocalTime fim = e.getHoraFim().toLocalTime();
                while (!hora.isAfter(fim.minusMinutes(30))) {
                    if (!ocupados.contains(hora)) {
                        cbHorario.addItem(hora);
                    }
                    hora = hora.plusMinutes(30);
                }
            }

            cbHorario.setEnabled(cbHorario.getItemCount() > 0);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar horários: " + ex.getMessage());
        }
    }




    private void limparCampos() {
        cbPaciente.setSelectedIndex(-1);
        cbProfissional.setSelectedIndex(-1);
        calendar.setCalendar(null);
        cbHorario.removeAllItems();
        tfPesquisar.setText("");
    }

    private void salvarAtendimento() {
        try {
            Paciente paciente = (Paciente) cbPaciente.getSelectedItem();
            Profissional profissional = (Profissional) cbProfissional.getSelectedItem();
            Calendar cal = calendar.getCalendar();
            if (cal == null || paciente == null || profissional == null || cbHorario.getSelectedItem() == null) {
                throw new CampoObrigatorioException("Preencha todos os campos!");
            }
            LocalDate data = cal.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalTime hora = (LocalTime) cbHorario.getSelectedItem();

            Atendimento at = new Atendimento();
            at.setPaciente(paciente);
            at.setProfissional(profissional);
            at.setDataHora(Timestamp.valueOf(data.atTime(hora)));
            at.setDuracaoMin(30);
            at.setTipo(Atendimento.Tipo.AVALIACAO);
            at.setSituacao(Atendimento.Situacao.AGENDADO);
            at.setUsuario(Sessao.getUsuarioLogado().getLogin());

            if (atendimentoController.criarAtendimento(at, Sessao.getUsuarioLogado().getLogin())) {
                JOptionPane.showMessageDialog(this, "Atendimento salvo!");
                limparCampos();
                carregarAtendimentos();
            }
        } catch (CampoObrigatorioException | SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarAtendimentos() {
        try {
            modeloTabela.setRowCount(0);
            atendimentoController.listarTodos().forEach(a ->
                    modeloTabela.addRow(new Object[]{
                            a.getPacienteNome(),
                            a.getProfissionalNome(),
                            a.getData().format(formatterData),
                            a.getHora().format(formatterHora),
                            a.getSituacao()
                    })
            );
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar atendimentos: " + e.getMessage());
        }
    }

    private static class SituacaoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof Atendimento.Situacao) {
                Atendimento.Situacao sit = (Atendimento.Situacao) value;
                switch (sit) {
                    case AGENDADO -> setBackground(new Color(144, 238, 144));
                    case REALIZADO -> setBackground(new Color(135, 206, 250));
                    case FALTOU -> setBackground(new Color(255, 165, 0));
                    case CANCELADO -> setBackground(new Color(255, 99, 71));
                }
                setForeground(Color.BLACK);
            }
            return this;
        }
    }
}
