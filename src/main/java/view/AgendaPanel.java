package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

import controller.AtendimentoController;
import model.Atendimento;

public class AgendaPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private JTable tabelaAtendimentos;
    private DefaultTableModel modeloTabela;
    private LocalDate dataSelecionada;
    private JLabel lblMesAno;
    private JPanel painelDias;
    private JComboBox<String> cbMes;
    private JComboBox<Integer> cbAno;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final String[] meses = {"Janeiro","Fevereiro","Março","Abril","Maio","Junho",
                                    "Julho","Agosto","Setembro","Outubro","Novembro","Dezembro"};

    public AgendaPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15)); // padding 15%

        JLabel lblTitulo = new JLabel("Agenda de Atendimentos", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(30, 30, 60));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        add(lblTitulo, BorderLayout.NORTH);

        JPanel panelEsquerdo = criarPainelCalendario();
        JPanel panelDireito = criarTabelaAtendimentos();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelEsquerdo, panelDireito);
        splitPane.setResizeWeight(0.67); // 67% calendário / 33% tabela
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(5);

        panelEsquerdo.setPreferredSize(new Dimension(600, 600));
        panelDireito.setPreferredSize(new Dimension(300, 600));

        add(splitPane, BorderLayout.CENTER);

        dataSelecionada = LocalDate.now();
        atualizarCalendario();
        carregarAtendimentosDoDia();
    }

    private JPanel criarPainelCalendario() {
        JPanel panelCalendario = new JPanel(new BorderLayout(5, 5));
        panelCalendario.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                "Calendário",
                TitledBorder.LEADING,
                TitledBorder.TOP
        ));

        JPanel panelTopo = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        // Setas de navegação mês a mês
        JButton btnPrevMes = new JButton("<");
        JButton btnNextMes = new JButton(">");
        btnPrevMes.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnNextMes.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // JComboBox de mês e ano
        cbMes = new JComboBox<>(meses);
        cbAno = new JComboBox<>();
        int anoAtual = LocalDate.now().getYear();
        for(int i = anoAtual - 5; i <= 2030; i++) cbAno.addItem(i); // mantido como estava

        cbMes.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        cbAno.setSelectedItem(anoAtual);

        // lblMesAno deve ser inicializado antes de adicionar ao painel
        lblMesAno = new JLabel("", SwingConstants.CENTER);
        lblMesAno.setFont(new Font("SansSerif", Font.BOLD, 16));

        // Ações das setas
        btnPrevMes.addActionListener(e -> {
            dataSelecionada = dataSelecionada.minusMonths(1);
            cbMes.setSelectedIndex(dataSelecionada.getMonthValue() - 1);
            cbAno.setSelectedItem(dataSelecionada.getYear());
            atualizarCalendario();
        });

        btnNextMes.addActionListener(e -> {
            dataSelecionada = dataSelecionada.plusMonths(1);
            cbMes.setSelectedIndex(dataSelecionada.getMonthValue() - 1);
            cbAno.setSelectedItem(dataSelecionada.getYear());
            atualizarCalendario();
        });

        // Troca via JComboBox
        cbMes.addActionListener(e -> atualizarCalendario());
        cbAno.addActionListener(e -> atualizarCalendario());

        panelTopo.add(btnPrevMes);
        panelTopo.add(cbMes);
        panelTopo.add(cbAno);
        panelTopo.add(btnNextMes);
        panelTopo.add(lblMesAno);

        painelDias = new JPanel(new GridLayout(0, 7, 5, 5));

        panelCalendario.add(panelTopo, BorderLayout.NORTH);
        panelCalendario.add(painelDias, BorderLayout.CENTER);

        return panelCalendario;
    }

    private JPanel criarTabelaAtendimentos() {
        JPanel panelTabela = new JPanel(new BorderLayout());

        String[] colunas = {"Horário", "Paciente", "Descrição"};
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

        JScrollPane scrollTabela = new JScrollPane(tabelaAtendimentos);
        panelTabela.add(scrollTabela, BorderLayout.CENTER);

        // Ajuste relativo das colunas
        tabelaAtendimentos.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                ajustarLarguraColunas();
            }
        });

        return panelTabela;
    }

    private void ajustarLarguraColunas() {
        int larguraTotal = tabelaAtendimentos.getWidth();
        if (larguraTotal == 0) return;

        // Horário 20%, Paciente 40%, Descrição 40%
        tabelaAtendimentos.getColumnModel().getColumn(0).setPreferredWidth((int)(larguraTotal * 0.2));
        tabelaAtendimentos.getColumnModel().getColumn(1).setPreferredWidth((int)(larguraTotal * 0.4));
        tabelaAtendimentos.getColumnModel().getColumn(2).setPreferredWidth((int)(larguraTotal * 0.4));
    }

    private void atualizarCalendario() {
        painelDias.removeAll();

        int mes = cbMes.getSelectedIndex() + 1;
        int ano = (Integer) cbAno.getSelectedItem();
        YearMonth anoMes = YearMonth.of(ano, mes);

        lblMesAno.setText(meses[mes - 1] + " " + ano);

        // Cabeçalho dias da semana
        String[] diasSemana = {"Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"};
        for (String d : diasSemana) {
            JLabel lblDia = new JLabel(d, SwingConstants.CENTER);
            lblDia.setFont(new Font("SansSerif", Font.BOLD, 12));
            painelDias.add(lblDia);
        }

        LocalDate primeiraData = anoMes.atDay(1);
        int diaSemanaInicio = primeiraData.getDayOfWeek().getValue() % 7; // domingo = 0

        for (int i = 0; i < diaSemanaInicio; i++) painelDias.add(new JLabel(""));

        int diasNoMes = anoMes.lengthOfMonth();
        for (int dia = 1; dia <= diasNoMes; dia++) {
            LocalDate dataAtual = anoMes.atDay(dia);
            JButton btnDia = new JButton(String.valueOf(dia));
            btnDia.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            // Domingo sempre desabilitado
            if (dataAtual.getDayOfWeek().getValue() == 7) {
                btnDia.setEnabled(false);
                btnDia.setBackground(Color.LIGHT_GRAY);
            } else if (dataAtual.isBefore(LocalDate.now())) {
                btnDia.setBackground(Color.LIGHT_GRAY);
            } else if (dataAtual.equals(LocalDate.now())) {
                btnDia.setBackground(new Color(173, 216, 230)); // azul
            } else {
                btnDia.setBackground(new Color(144, 238, 144)); // verde
            }

            btnDia.addActionListener(e -> {
                dataSelecionada = dataAtual;
                carregarAtendimentosDoDia();
            });

            painelDias.add(btnDia);
        }

        painelDias.revalidate();
        painelDias.repaint();
    }

    private void carregarAtendimentosDoDia() {
        modeloTabela.setRowCount(0);
        try {
            AtendimentoController ac = new AtendimentoController();

            LocalDateTime inicio = dataSelecionada.atStartOfDay();
            LocalDateTime fim = dataSelecionada.atTime(23, 59, 59);

            List<Atendimento> atendimentos = ac.listarPorPeriodo(inicio, fim);

            for (Atendimento a : atendimentos) {
                modeloTabela.addRow(new Object[]{
                        a.getDataHora().toLocalTime().toString(),
                        a.getPaciente().getNome(),
                        a.getNotas()
                });
            }

            ajustarLarguraColunas(); // garante que colunas se ajustem ao conteúdo inicial
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao carregar atendimentos: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
