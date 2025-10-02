package view;

import dao.CaixaDAO;
import dao.CaixaMovimentoDAO;
import dao.DespesaDAO;
import model.Caixa;
import model.CaixaMovimento;
import model.Despesa;
import util.Database;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Exemplo de referência de layout: assumindo um painel com formulário no topo e tabela abaixo, similar a painéis de CRUD em Swing.
// Usei GridBagLayout para o formulário e JScrollPane para a tabela.

public class LancamentoDespesaPanel extends JPanel {

    // Componentes do formulário
    private JTextField txtId;
    private JTextField txtDescricao;
    private JComboBox<Despesa.Categoria> cmbCategoria;
    private JTextField txtValor;
    private JComboBox<Despesa.FormaPagamento> cmbFormaPagamento;
    private JTextField txtDataVencimento; // Formato dd/MM/yyyy
    private JCheckBox chkPago;
    private JTextField txtDataPagamento; // Habilitado se chkPago marcado
    private JButton btnNovo;
    private JButton btnSalvar;
    private JButton btnEditar;
    private JButton btnDeletar;
    private JButton btnPagar; // Para pagar despesa pendente selecionada

    // Tabela
    private JTable tabelaDespesas;
    private DespesaTableModel tableModel;
    private List<Despesa> listaDespesas;

    // DAOs
    private DespesaDAO despesaDAO;
    private CaixaDAO caixaDAO;
    private CaixaMovimentoDAO movimentoDAO;

    // Usuário logado (exemplo, deve vir de contexto)
    private String usuarioLogado = "admin"; // Substitua pelo usuário real

    // Formato de data
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    public LancamentoDespesaPanel() {
        setLayout(new BorderLayout());
        despesaDAO = new DespesaDAO();
        caixaDAO = new CaixaDAO();
        movimentoDAO = new CaixaMovimentoDAO();

        // Inicializar lista
        listaDespesas = new ArrayList<>();
        tableModel = new DespesaTableModel(listaDespesas);

        // Painel do formulário (topo)
        JPanel painelFormulario = criarPainelFormulario();
        add(painelFormulario, BorderLayout.NORTH);

        // Tabela (centro)
        tabelaDespesas = new JTable(tableModel);
        tabelaDespesas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(tabelaDespesas);
        add(scrollPane, BorderLayout.CENTER);

        // Carregar dados iniciais
        carregarDespesasFiltradas();

        // Listeners
        chkPago.addActionListener(e -> txtDataPagamento.setEnabled(chkPago.isSelected()));

        tabelaDespesas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                preencherFormularioComSelecao();
            }
        });
    }

    private JPanel criarPainelFormulario() {
        JPanel painel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Linha 1: ID (somente leitura)
        gbc.gridx = 0;
        gbc.gridy = 0;
        painel.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1;
        txtId = new JTextField(5);
        txtId.setEditable(false);
        painel.add(txtId, gbc);

        // Descrição
        gbc.gridx = 2;
        painel.add(new JLabel("Descrição:"), gbc);
        gbc.gridx = 3;
        txtDescricao = new JTextField(20);
        painel.add(txtDescricao, gbc);

        // Categoria
        gbc.gridx = 0;
        gbc.gridy = 1;
        painel.add(new JLabel("Categoria:"), gbc);
        gbc.gridx = 1;
        cmbCategoria = new JComboBox<>(Despesa.Categoria.values());
        painel.add(cmbCategoria, gbc);

        // Valor
        gbc.gridx = 2;
        painel.add(new JLabel("Valor:"), gbc);
        gbc.gridx = 3;
        txtValor = new JTextField(10);
        painel.add(txtValor, gbc);

        // Forma Pagamento
        gbc.gridx = 0;
        gbc.gridy = 2;
        painel.add(new JLabel("Forma Pagamento:"), gbc);
        gbc.gridx = 1;
        cmbFormaPagamento = new JComboBox<>(Despesa.FormaPagamento.values());
        painel.add(cmbFormaPagamento, gbc);

        // Data Vencimento
        gbc.gridx = 2;
        painel.add(new JLabel("Data Vencimento (dd/MM/yyyy):"), gbc);
        gbc.gridx = 3;
        txtDataVencimento = new JTextField(10);
        painel.add(txtDataVencimento, gbc);

        // Pago?
        gbc.gridx = 0;
        gbc.gridy = 3;
        chkPago = new JCheckBox("Pago?");
        painel.add(chkPago, gbc);

        // Data Pagamento
        gbc.gridx = 1;
        painel.add(new JLabel("Data Pagamento (dd/MM/yyyy):"), gbc);
        gbc.gridx = 2;
        txtDataPagamento = new JTextField(10);
        txtDataPagamento.setEnabled(false);
        painel.add(txtDataPagamento, gbc);

        // Botões
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel painelBotoes = new JPanel(new FlowLayout());
        btnNovo = new JButton("Novo");
        btnSalvar = new JButton("Salvar");
        btnEditar = new JButton("Editar");
        btnDeletar = new JButton("Deletar");
        btnPagar = new JButton("Pagar Selecionada");
        painelBotoes.add(btnNovo);
        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnEditar);
        painelBotoes.add(btnDeletar);
        painelBotoes.add(btnPagar);
        painel.add(painelBotoes, gbc);

        // Ações dos botões
        btnNovo.addActionListener(e -> limparFormulario());
        btnSalvar.addActionListener(e -> salvarOuAtualizar(false));
        btnEditar.addActionListener(e -> salvarOuAtualizar(true));
        btnDeletar.addActionListener(e -> deletar());
        btnPagar.addActionListener(e -> pagarDespesaSelecionada());

        return painel;
    }

    private void limparFormulario() {
        txtId.setText("");
        txtDescricao.setText("");
        cmbCategoria.setSelectedIndex(0);
        txtValor.setText("");
        cmbFormaPagamento.setSelectedIndex(0);
        txtDataVencimento.setText("");
        chkPago.setSelected(false);
        txtDataPagamento.setText("");
        txtDataPagamento.setEnabled(false);
    }

    private void preencherFormularioComSelecao() {
        int row = tabelaDespesas.getSelectedRow();
        if (row >= 0) {
            Despesa d = listaDespesas.get(row);
            txtId.setText(String.valueOf(d.getId()));
            txtDescricao.setText(d.getDescricao());
            cmbCategoria.setSelectedItem(d.getCategoria());
            txtValor.setText(d.getValor().toString());
            cmbFormaPagamento.setSelectedItem(d.getFormaPagamento());
            txtDataVencimento.setText(sdf.format(Date.from(d.getDataVencimento().atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant())));
            chkPago.setSelected(d.getStatus() == Despesa.Status.PAGO);
            if (d.getDataPagamento() != null) {
                txtDataPagamento.setText(sdf.format(Date.from(d.getDataPagamento().atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant())));
            } else {
                txtDataPagamento.setText("");
            }
            txtDataPagamento.setEnabled(chkPago.isSelected());
        }
    }

    private void salvarOuAtualizar(boolean isEditar) {
        try {
            Despesa d;
            if (isEditar && !txtId.getText().isEmpty()) {
                d = despesaDAO.buscarPorId(Integer.parseInt(txtId.getText()));
                if (d == null) {
                    JOptionPane.showMessageDialog(this, "Despesa não encontrada.");
                    return;
                }
            } else {
                d = new Despesa();
            }

            d.setDescricao(txtDescricao.getText());
            d.setCategoria((Despesa.Categoria) cmbCategoria.getSelectedItem());
            d.setValor(new BigDecimal(txtValor.getText().replace(",", ".")));
            d.setFormaPagamento((Despesa.FormaPagamento) cmbFormaPagamento.getSelectedItem());
            d.setDataVencimento(parseData(txtDataVencimento.getText()).toLocalDate());

            if (chkPago.isSelected()) {
                d.setDataPagamento(parseData(txtDataPagamento.getText()).toLocalDate());
                d.setStatus(Despesa.Status.PAGO);
                // Registrar movimento no caixa se for novo pagamento ou atualização para pago
                if (!isEditar || d.getStatus() != Despesa.Status.PAGO) {
                    registrarMovimentoCaixa(d);
                }
            } else {
                d.setDataPagamento(null);
                d.setStatus(Despesa.Status.PENDENTE);
            }

            if (isEditar) {
                despesaDAO.atualizar(d, usuarioLogado);
            } else {
                despesaDAO.salvar(d, usuarioLogado);
            }

            JOptionPane.showMessageDialog(this, "Despesa salva com sucesso!");
            carregarDespesasFiltradas();
            limparFormulario();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar: " + ex.getMessage());
        }
    }

    private void deletar() {
        int row = tabelaDespesas.getSelectedRow();
        if (row >= 0) {
            Despesa d = listaDespesas.get(row);
            if (JOptionPane.showConfirmDialog(this, "Confirmar exclusão?") == JOptionPane.YES_OPTION) {
                try {
                    despesaDAO.deletar(d.getId());
                    carregarDespesasFiltradas();
                    limparFormulario();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Erro ao deletar: " + ex.getMessage());
                }
            }
        }
    }

    private void pagarDespesaSelecionada() {
        int row = tabelaDespesas.getSelectedRow();
        if (row >= 0) {
            Despesa d = listaDespesas.get(row);
            if (d.getStatus() == Despesa.Status.PAGO) {
                JOptionPane.showMessageDialog(this, "Despesa já paga.");
                return;
            }

            // Diálogo simples para data pagamento (pode melhorar com date chooser)
            String dataStr = JOptionPane.showInputDialog(this, "Informe a data de pagamento (dd/MM/yyyy):");
            if (dataStr != null && !dataStr.isEmpty()) {
                try {
                    LocalDate dataPagamento = parseData(dataStr).toLocalDate();
                    d.setDataPagamento(dataPagamento);
                    d.setStatus(Despesa.Status.PAGO);
                    despesaDAO.atualizar(d, usuarioLogado);
                    registrarMovimentoCaixa(d);
                    JOptionPane.showMessageDialog(this, "Despesa paga com sucesso!");
                    carregarDespesasFiltradas();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erro ao pagar: " + ex.getMessage());
                }
            }
        }
    }

    private void registrarMovimentoCaixa(Despesa d) throws SQLException {
        Caixa caixaAberto = caixaDAO.buscarCaixaAberto();
        if (caixaAberto == null) {
            throw new SQLException("Não há caixa aberto para registrar o movimento.");
        }

        CaixaMovimento movimento = new CaixaMovimento();
        movimento.setCaixa(caixaAberto);
        movimento.setTipo(CaixaMovimento.TipoMovimento.SAIDA);
        movimento.setOrigem(CaixaMovimento.OrigemMovimento.DESPESA);
        movimento.setFormaPagamento(converterFormaPagamento(d.getFormaPagamento()));
        movimento.setValor(d.getValor());
        movimento.setDescricao("Pagamento de despesa ID " + d.getId() + ": " + d.getDescricao());
        movimento.setDataHora(LocalDateTime.now());
        movimento.setUsuario(usuarioLogado);

        movimentoDAO.inserir(movimento);
    }

    // Converter enum Despesa.FormaPagamento para CaixaMovimento.FormaPagamento
    // Nota: Enums são parecidos, mas mapeie se necessário (aqui assumindo compatíveis, exceto TRANSFERENCIA e BOLETO que podem não ter equivalente direto)
    private CaixaMovimento.FormaPagamento converterFormaPagamento(Despesa.FormaPagamento forma) {
        switch (forma) {
            case DINHEIRO: return CaixaMovimento.FormaPagamento.DINHEIRO;
            case DEBITO: return CaixaMovimento.FormaPagamento.DEBITO;
            case CREDITO: return CaixaMovimento.FormaPagamento.CREDITO;
            case PIX: return CaixaMovimento.FormaPagamento.PIX;
            case TRANSFERENCIA:
            case BOLETO:
                // Assumindo OUTRO ou mapeie para BOLETO se adicionar no enum
                return CaixaMovimento.FormaPagamento.BOLETO; // Como tem BOLETO no enum de CaixaMovimento
            default: return CaixaMovimento.FormaPagamento.OUTRO; // Se adicionar
        }
    }

    private void carregarDespesasFiltradas() {
        try {
            List<Despesa> todas = despesaDAO.listarTodos();
            listaDespesas.clear();
            LocalDateTime agora = LocalDateTime.now();
            for (Despesa d : todas) {
                if (d.getStatus() == Despesa.Status.PENDENTE ||
                    (d.getStatus() == Despesa.Status.PAGO && d.getDataHora() != null &&
                     ChronoUnit.HOURS.between(d.getDataHora(), agora) <= 48)) {
                    listaDespesas.add(d);
                }
            }
            tableModel.fireTableDataChanged();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar despesas: " + ex.getMessage());
        }
    }

    private Date parseData(String dataStr) throws ParseException {
        return sdf.parse(dataStr);
    }

    // TableModel custom
    private class DespesaTableModel extends AbstractTableModel {
        private List<Despesa> data;
        private String[] colunas = {"ID", "Descrição", "Categoria", "Valor", "Forma Pag.", "Data Venc.", "Data Pag.", "Status"};

        public DespesaTableModel(List<Despesa> data) {
            this.data = data;
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return colunas.length;
        }

        @Override
        public String getColumnName(int column) {
            return colunas[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Despesa d = data.get(rowIndex);
            switch (columnIndex) {
                case 0: return d.getId();
                case 1: return d.getDescricao();
                case 2: return d.getCategoria();
                case 3: return new DecimalFormat("#,##0.00").format(d.getValor());
                case 4: return d.getFormaPagamento();
                case 5: return d.getDataVencimento();
                case 6: return d.getDataPagamento();
                case 7: return d.getStatus();
                default: return null;
            }
        }
    }
}