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

    private final DateTimeFormatter formatterData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter formatterHora = DateTimeFormatter.ofPattern("HH:mm");
    private final String[] meses = {"Janeiro","Fevereiro","Março","Abril","Maio","Junho",
                                    "Julho","Agosto","Setembro","Outubro","Novembro","Dezembro"};
    // Definindo a fonte para a tabela, igual ao MarcacaoAtendimentoPanel
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 14);

    public AgendaPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblTitulo = new JLabel("Agenda de Atendimentos", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(30, 30, 60));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        add(lblTitulo, BorderLayout.NORTH);

        JPanel panelEsquerdo = criarPainelCalendario();
        JPanel panelDireito = criarTabelaAtendimentos();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelEsquerdo, panelDireito);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(5);
        splitPane.setDividerLocation(0.45); // 45% calendário, 55% tabela

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                splitPane.setDividerLocation(0.45);
            }
        });

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

        JButton btnPrevMes = new JButton("<");
        JButton btnNextMes = new JButton(">");
        btnPrevMes.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnNextMes.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        cbMes = new JComboBox<>(meses);
        cbAno = new JComboBox<>();
        int anoAtual = LocalDate.now().getYear();
        for(int i = anoAtual - 5; i <= 2030; i++) cbAno.addItem(i);

        cbMes.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        cbAno.setSelectedItem(anoAtual);

        lblMesAno = new JLabel("", SwingConstants.CENTER);
        lblMesAno.setFont(new Font("SansSerif", Font.BOLD, 16));

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

        // Colunas: Dia, Hora, Paciente, Status, Observação
        String[] colunas = {"Dia", "Hora", "Paciente", "Status", "Observação"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tabelaAtendimentos = new JTable(modeloTabela) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                Color bgColor;

                try {
                    // Converte a String de volta para LocalDate para comparar
                    String dataStr = (String) getValueAt(row, 0);
                    LocalDate dataLinha = LocalDate.parse(dataStr, formatterData);
                    LocalDate hoje = LocalDate.now();

                    if (dataLinha.isBefore(hoje)) {
                        bgColor = new Color(220, 220, 220); // Passado
                    } else {
                        Object statusObj = getValueAt(row, 3);
                        String status = statusObj != null ? statusObj.toString().toUpperCase() : "";
                        switch (status) {
                            case "AGENDADO" -> bgColor = new Color(173, 216, 230);
                            case "REALIZADO" -> bgColor = new Color(144, 238, 144);
                            case "FALTOU" -> bgColor = new Color(255, 255, 153);
                            case "CANCELADO" -> bgColor = new Color(255, 182, 193);
                            default -> bgColor = dataLinha.equals(hoje) ? new Color(0, 200, 0) : new Color(173, 216, 230);
                        }
                    }
                } catch (Exception e) {
                    bgColor = Color.WHITE; // Fallback
                }

                c.setBackground(bgColor);
                c.setForeground(Color.BLACK);

                // Aplica borda preta apenas na linha selecionada, contornando a linha inteira
                if (isRowSelected(row)) {
                    int lastColumn = getColumnCount() - 1;
                    if (column == 0) {
                        // Primeira coluna: borda esquerda e bordas horizontais
                        ((JComponent) c).setBorder(BorderFactory.createMatteBorder(1, 1, 1, 0, Color.BLACK));
                    } else if (column == lastColumn) {
                        // Última coluna: borda direita e bordas horizontais
                        ((JComponent) c).setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, Color.BLACK));
                    } else {
                        // Colunas intermediárias: apenas bordas horizontais
                        ((JComponent) c).setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.BLACK));
                    }
                } else {
                    ((JComponent) c).setBorder(BorderFactory.createEmptyBorder());
                }

                return c;
            }
        };

        // Desativa a grade da tabela para remover divisórias entre células
        tabelaAtendimentos.setShowGrid(false);
        tabelaAtendimentos.setIntercellSpacing(new Dimension(0, 0)); // Remove espaçamento entre células

        // Aplicando a mesma formatação de fonte e altura de linha do MarcacaoAtendimentoPanel
        tabelaAtendimentos.setFont(labelFont); // Fonte SansSerif, Plain, tamanho 14
        tabelaAtendimentos.setRowHeight(25); // Altura da linha 25 pixels

        tabelaAtendimentos.setFillsViewportHeight(true);
        tabelaAtendimentos.getTableHeader().setBackground(new Color(30, 144, 255)); // Azul escuro
        tabelaAtendimentos.getTableHeader().setForeground(Color.WHITE);
        tabelaAtendimentos.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));

        DefaultTableCellRenderer centralizado = new DefaultTableCellRenderer();
        centralizado.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tabelaAtendimentos.getColumnCount(); i++) {
            tabelaAtendimentos.getColumnModel().getColumn(i).setCellRenderer(centralizado);
        }

        JScrollPane scrollTabela = new JScrollPane(tabelaAtendimentos);
        panelTabela.add(scrollTabela, BorderLayout.CENTER);

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

        tabelaAtendimentos.getColumnModel().getColumn(0).setPreferredWidth((int)(larguraTotal * 0.15)); // Dia
        tabelaAtendimentos.getColumnModel().getColumn(1).setPreferredWidth((int)(larguraTotal * 0.10)); // Hora
        tabelaAtendimentos.getColumnModel().getColumn(2).setPreferredWidth((int)(larguraTotal * 0.30)); // Paciente
        tabelaAtendimentos.getColumnModel().getColumn(3).setPreferredWidth((int)(larguraTotal * 0.15)); // Status
        tabelaAtendimentos.getColumnModel().getColumn(4).setPreferredWidth((int)(larguraTotal * 0.30)); // Observação
    }

    private void atualizarCalendario() {
        painelDias.removeAll();

        int mes = cbMes.getSelectedIndex() + 1;
        int ano = (Integer) cbAno.getSelectedItem();
        YearMonth anoMes = YearMonth.of(ano, mes);

        lblMesAno.setText(meses[mes - 1] + " " + ano);

        String[] diasSemana = {"Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"};
        for (String d : diasSemana) {
            JLabel lblDia = new JLabel(d, SwingConstants.CENTER);
            lblDia.setFont(new Font("SansSerif", Font.BOLD, 12));
            painelDias.add(lblDia);
        }

        LocalDate primeiraData = anoMes.atDay(1);
        int diaSemanaInicio = primeiraData.getDayOfWeek().getValue() % 7;

        for (int i = 0; i < diaSemanaInicio; i++) painelDias.add(new JLabel(""));

        int diasNoMes = anoMes.lengthOfMonth();
        for (int dia = 1; dia <= diasNoMes; dia++) {
            LocalDate dataAtual = anoMes.atDay(dia);
            JButton btnDia = new JButton(String.valueOf(dia));
            btnDia.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            if (dataAtual.getDayOfWeek().getValue() == 7) {
                btnDia.setEnabled(false);
                btnDia.setBackground(Color.decode("#E0E0E0"));
                btnDia.setForeground(Color.decode("#666666"));
            } else if (dataAtual.equals(dataSelecionada)) {
                btnDia.setBackground(Color.decode("#FAD7A0"));
                btnDia.setForeground(Color.BLACK);
                btnDia.getModel().setPressed(true);
                btnDia.getModel().setArmed(true);
            } else if (dataAtual.equals(LocalDate.now())) {
                btnDia.setBackground(Color.decode("#32CD32")); // Verde vibrante hoje
                btnDia.setForeground(Color.BLACK);
            } else if (dataAtual.isBefore(LocalDate.now())) {
                btnDia.setBackground(Color.WHITE);
                btnDia.setForeground(Color.BLACK);
            } else {
                btnDia.setBackground(Color.decode("#AED6F1"));
                btnDia.setForeground(Color.BLACK);
            }

            btnDia.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

            btnDia.addActionListener(e -> {
                dataSelecionada = dataAtual;
                atualizarCalendario();
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
                        a.getDataHora().toLocalDateTime().format(formatterData),
                        a.getDataHora().toLocalDateTime().format(formatterHora),
                        a.getPacienteNome(),
                        a.getSituacao(),   // Status
                        a.getNotas()      // Observação
                });
            }

            ajustarLarguraColunas();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao carregar atendimentos: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}