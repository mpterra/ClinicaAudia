package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import controller.UsuarioController;
import controller.ProfissionalController;
import exception.LoginDuplicadoException;
import model.Usuario;
import model.Profissional;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Painel para cadastro e listagem de usuários
public class CadastroUsuarioPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    // Componentes de entrada
    private JTextField tfLogin, tfPesquisar;
    private JPasswordField pfSenha;
    private JComboBox<String> cbTipo;
    private JButton btnSalvar, btnLimpar;
    private JTable tabelaUsuarios;
    private DefaultTableModel modeloTabela;
    private TableRowSorter<DefaultTableModel> sorter;
    private JRadioButton rbSim, rbNao;
    private JComboBox<String> cbProfissionais;
    private List<Profissional> listaProfissionais = new ArrayList<>();
    private JLabel lblSenhaInfo;

    // Estilo visual
    private final Color primaryColor = new Color(138, 43, 226); // Roxo
    private final Color backgroundColor = new Color(245, 245, 245); // Fundo geral
    private final Color rowColorLightLilac = new Color(230, 230, 250); // Lilás claro para linhas pares
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 18); // Título principal
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 14); // Labels, TitledBorder, tabela

    // Construtor
    public CadastroUsuarioPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(5, 15, 15, 15));
        setBackground(backgroundColor);

        // Título do painel
        JLabel lblTitulo = new JLabel("Cadastro de Usuário", SwingConstants.CENTER);
        lblTitulo.setFont(titleFont);
        lblTitulo.setForeground(primaryColor);
        lblTitulo.setBorder(new EmptyBorder(10, 0, 10, 0));
        add(lblTitulo, BorderLayout.NORTH);

        // Painéis de cadastro e tabela
        JPanel panelCadastro = criarPainelCadastro();
        JPanel panelTabela = criarTabelaUsuariosComPesquisa();

        // SplitPane para dividir cadastro e tabela
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelCadastro, panelTabela);
        splitPane.setResizeWeight(0.60); // Proporção 60-40
        splitPane.setDividerSize(7);
        splitPane.setContinuousLayout(true); // Transição suave ao redimensionar
        splitPane.setBackground(backgroundColor);

        add(splitPane, BorderLayout.CENTER);

        // Garantir proporção 60-40 do JSplitPane
        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.60));
        revalidate();
        repaint();

        // Listeners dos botões
        btnLimpar.addActionListener(e -> limparCampos());
        btnSalvar.addActionListener(e -> {
            try {
                salvarUsuario();
            } catch (SQLException e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erro ao salvar usuário: " + e1.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            } catch (LoginDuplicadoException e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(this, "Usuário já cadastrado. Utilize outro login", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        carregarUsuarios();
        carregarProfissionais();
    }

    // Cria o painel de cadastro
    private JPanel criarPainelCadastro() {
        JPanel panelCadastroWrapper = new JPanel(new BorderLayout());
        panelCadastroWrapper.setBackground(backgroundColor);
        panelCadastroWrapper.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel panelCadastro = new JPanel(new GridBagLayout());
        panelCadastro.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(primaryColor, 1, true),
                        "Cadastrar Novo Usuário",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        labelFont,
                        primaryColor),
                new EmptyBorder(10, 10, 10, 10)));
        panelCadastro.setBackground(backgroundColor);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Subtítulo
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        JLabel lblSubtitulo = new JLabel("Preencha os dados do usuário");
        lblSubtitulo.setFont(labelFont);
        lblSubtitulo.setForeground(Color.DARK_GRAY);
        panelCadastro.add(lblSubtitulo, gbc);
        gbc.gridwidth = 1;

        // Login
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel lblLogin = new JLabel("Login:");
        lblLogin.setFont(labelFont);
        panelCadastro.add(lblLogin, gbc);

        tfLogin = new JTextField();
        tfLogin.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCadastro.add(tfLogin, gbc);

        // Label informativo ao lado do login
        JLabel lblLoginInfo = new JLabel("Use somente letras minúsculas e/ou números");
        lblLoginInfo.setFont(labelFont);
        lblLoginInfo.setForeground(Color.GRAY);
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        panelCadastro.add(lblLoginInfo, gbc);

        // Senha
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lblSenha = new JLabel("Senha:");
        lblSenha.setFont(labelFont);
        panelCadastro.add(lblSenha, gbc);

        pfSenha = new JPasswordField();
        pfSenha.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCadastro.add(pfSenha, gbc);

        // Label de feedback da senha
        lblSenhaInfo = new JLabel("<html>✖ 6 caracteres &nbsp;&nbsp;✖ 1 número &nbsp;&nbsp;✖ 1 símbolo</html>");
        lblSenhaInfo.setFont(labelFont);
        lblSenhaInfo.setForeground(Color.RED);
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        panelCadastro.add(lblSenhaInfo, gbc);

        // Listener dinâmico para validação da senha
        pfSenha.getDocument().addDocumentListener(new DocumentListener() {
            private void verificarSenha() {
                String senha = new String(pfSenha.getPassword());
                boolean temNumero = senha.matches(".*[0-9].*");
                boolean temSimbolo = senha.matches(".*[^a-zA-Z0-9].*");
                boolean temTamanho = senha.length() >= 6;

                String texto = "<html>";
                texto += temTamanho ? "✔ " : "✖ ";
                texto += "6 caracteres &nbsp;&nbsp;";
                texto += temNumero ? "✔ " : "✖ ";
                texto += "1 número &nbsp;&nbsp;";
                texto += temSimbolo ? "✔ " : "✖ ";
                texto += "1 símbolo</html>";

                lblSenhaInfo.setText(texto);
                lblSenhaInfo.setForeground((temTamanho && temNumero && temSimbolo) ? new Color(0, 128, 0) : Color.RED);
            }

            public void insertUpdate(DocumentEvent e) { verificarSenha(); }
            public void removeUpdate(DocumentEvent e) { verificarSenha(); }
            public void changedUpdate(DocumentEvent e) { verificarSenha(); }
        });

        // Tipo
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lblTipo = new JLabel("Tipo:");
        lblTipo.setFont(labelFont);
        panelCadastro.add(lblTipo, gbc);

        cbTipo = new JComboBox<>(new String[]{"ADMIN", "FONOAUDIOLOGO", "SECRETARIA", "FINANCEIRO"});
        cbTipo.setPreferredSize(new Dimension(200, 30));
        cbTipo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCadastro.add(cbTipo, gbc);

        // Label informativo ao lado do tipo
        JLabel lblTipoInfo = new JLabel("Escolha o tipo de login");
        lblTipoInfo.setFont(labelFont);
        lblTipoInfo.setForeground(Color.GRAY);
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        panelCadastro.add(lblTipoInfo, gbc);

        // Vincular a profissional
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lblVincular = new JLabel("Vincular a Profissional?");
        lblVincular.setFont(labelFont);
        panelCadastro.add(lblVincular, gbc);

        rbSim = new JRadioButton("Sim");
        rbNao = new JRadioButton("Não", true);
        rbSim.setFont(labelFont);
        rbNao.setFont(labelFont);
        rbSim.setBackground(backgroundColor);
        rbNao.setBackground(backgroundColor);
        rbSim.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        rbNao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbSim);
        bg.add(rbNao);
        JPanel panelRadios = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panelRadios.setBackground(backgroundColor);
        panelRadios.add(rbSim);
        panelRadios.add(rbNao);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCadastro.add(panelRadios, gbc);
        gbc.gridwidth = 1;

        // ComboBox de Profissionais
        cbProfissionais = new JComboBox<>();
        cbProfissionais.setPreferredSize(new Dimension(200, 30));
        cbProfissionais.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cbProfissionais.setVisible(false);
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCadastro.add(cbProfissionais, gbc);
        gbc.gridwidth = 1;

        rbSim.addActionListener(e -> cbProfissionais.setVisible(true));
        rbNao.addActionListener(e -> cbProfissionais.setVisible(false));

        // Botões
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panelBotoes.setBackground(backgroundColor);
        btnSalvar = new JButton("Salvar");
        btnSalvar.setBackground(primaryColor);
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setPreferredSize(new Dimension(100, 35));
        btnSalvar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLimpar = new JButton("Limpar");
        btnLimpar.setBackground(Color.LIGHT_GRAY);
        btnLimpar.setForeground(Color.BLACK);
        btnLimpar.setPreferredSize(new Dimension(100, 35));
        btnLimpar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panelBotoes.add(btnLimpar);
        panelBotoes.add(btnSalvar);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCadastro.add(panelBotoes, gbc);

        panelCadastroWrapper.add(panelCadastro, BorderLayout.NORTH);
        return panelCadastroWrapper;
    }

    // Cria o painel da tabela com pesquisa
    private JPanel criarTabelaUsuariosComPesquisa() {
        JPanel panelTabelaWrapper = new JPanel(new BorderLayout(10, 10));
        panelTabelaWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(primaryColor, 1, true),
                        "Usuários Cadastrados",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        labelFont,
                        primaryColor),
                new EmptyBorder(10, 10, 10, 10)));
        panelTabelaWrapper.setBackground(backgroundColor);

        // Pesquisa
        JPanel panelPesquisa = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelPesquisa.setBackground(backgroundColor);
        JLabel lblPesquisar = new JLabel("Pesquisar por Login:");
        lblPesquisar.setFont(labelFont);
        panelPesquisa.add(lblPesquisar);
        tfPesquisar = new JTextField();
        tfPesquisar.setPreferredSize(new Dimension(200, 30));
        panelPesquisa.add(tfPesquisar);
        tfPesquisar.getDocument().addDocumentListener(new DocumentListener() {
            private void filtrar() {
                String texto = tfPesquisar.getText().trim();
                sorter.setRowFilter(texto.isEmpty() ? null : RowFilter.regexFilter("(?i)" + texto, 0));
            }
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            public void changedUpdate(DocumentEvent e) { filtrar(); }
        });

        // Tabela
        String[] colunas = {"Login", "Tipo", "Status"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaUsuarios = new JTable(modeloTabela);
        tabelaUsuarios.setRowHeight(25);
        tabelaUsuarios.setShowGrid(false);
        tabelaUsuarios.setIntercellSpacing(new Dimension(0, 0));
        tabelaUsuarios.setFont(labelFont);
        tabelaUsuarios.setFillsViewportHeight(true);

        // Renderizador para alternar cores das linhas
        DefaultTableCellRenderer rowRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? rowColorLightLilac : Color.WHITE);
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        };
        for (int i = 0; i < tabelaUsuarios.getColumnCount(); i++) {
            tabelaUsuarios.getColumnModel().getColumn(i).setCellRenderer(rowRenderer);
        }

        JTableHeader header = tabelaUsuarios.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.setBackground(primaryColor);
        header.setForeground(Color.WHITE);

        sorter = new TableRowSorter<>(modeloTabela);
        tabelaUsuarios.setRowSorter(sorter);

        JScrollPane scrollTabela = new JScrollPane(tabelaUsuarios);
        scrollTabela.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        panelTabelaWrapper.add(panelPesquisa, BorderLayout.NORTH);
        panelTabelaWrapper.add(scrollTabela, BorderLayout.CENTER);

        return panelTabelaWrapper;
    }

    // Limpa os campos do formulário
    private void limparCampos() {
        tfLogin.setText("");
        pfSenha.setText("");
        cbTipo.setSelectedIndex(0);
        rbNao.setSelected(true);
        cbProfissionais.setVisible(false);
        lblSenhaInfo.setText("<html>✖ 6 caracteres &nbsp;&nbsp;✖ 1 número &nbsp;&nbsp;✖ 1 símbolo</html>");
        lblSenhaInfo.setForeground(Color.RED);
    }

    // Salva o usuário no banco
    private void salvarUsuario() throws SQLException, LoginDuplicadoException {
        String login = tfLogin.getText().trim();
        String senha = new String(pfSenha.getPassword());
        String tipo = (String) cbTipo.getSelectedItem();

        boolean temNumero = senha.matches(".*[0-9].*");
        boolean temSimbolo = senha.matches(".*[^a-zA-Z0-9].*");
        boolean temTamanho = senha.length() >= 6;

        if (!temNumero || !temSimbolo || !temTamanho) {
            JOptionPane.showMessageDialog(this, "Senha inválida! Use pelo menos 6 caracteres, 1 número e 1 símbolo.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (login.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Usuario user = new Usuario();
        user.setLogin(login);
        user.setSenha(senha);
        user.setTipo(tipo);

        if (rbSim.isSelected() && cbProfissionais.getSelectedIndex() != -1) {
            int profId = listaProfissionais.get(cbProfissionais.getSelectedIndex()).getId();
            user.setProfissionalId(profId);
        }

        UsuarioController uc = new UsuarioController();
        if (uc.salvar(user)) {
            JOptionPane.showMessageDialog(this, "Usuário salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            limparCampos();
            carregarUsuarios();
        }
    }

    // Carrega os usuários na tabela
    private void carregarUsuarios() {
        try {
            UsuarioController uc = new UsuarioController();
            List<Usuario> usuarios = uc.listarTodos();

            modeloTabela.setRowCount(0);

            for (Usuario u : usuarios) {
                modeloTabela.addRow(new Object[]{
                        u.getLogin(),
                        u.getTipo(),
                        u.isAtivo() ? "Ativo" : "Inativo"
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao carregar usuários: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Carrega os profissionais no JComboBox
    private void carregarProfissionais() {
        try {
            ProfissionalController pc = new ProfissionalController();
            listaProfissionais = pc.buscarPorAtivo(true);
            cbProfissionais.removeAllItems();
            for (Profissional p : listaProfissionais) {
                cbProfissionais.addItem(p.getNome());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao carregar profissionais: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}