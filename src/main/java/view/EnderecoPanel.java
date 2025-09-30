package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.MaskFormatter;
import model.Endereco;
import util.ViaCepService;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

// Painel reutilizável para cadastro de endereço
public class EnderecoPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(EnderecoPanel.class.getName());

    // Componentes de entrada
    private JTextField tfRua, tfNumero, tfComplemento, tfBairro, tfCidade;
    private JFormattedTextField tfCep;
    private JComboBox<String> cbEstado;

    // Estilo visual
    private Color primaryColor;
    private final Color backgroundColor = new Color(245, 245, 245); // Fundo geral
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 12); // Labels e TitledBorder

    // Construtor
    public EnderecoPanel(Color primaryColor) {
    	this.primaryColor = primaryColor;
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(primaryColor, 1, true),
                        "Endereço",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        labelFont,
                        primaryColor),
                new EmptyBorder(10, 10, 10, 10)));
        setBackground(backgroundColor);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // CEP
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel lblCep = new JLabel("CEP:");
        lblCep.setFont(labelFont);
        add(lblCep, gbc);
        try {
            MaskFormatter cepMask = new MaskFormatter("#####-###");
            cepMask.setPlaceholderCharacter('_');
            tfCep = new JFormattedTextField(cepMask);
            tfCep.setPreferredSize(new Dimension(100, 25));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao criar máscara de CEP: " + e.getMessage(), e);
        }
        gbc.gridx = 1;
        add(tfCep, gbc);

        // Buscar endereço via CEP
        tfCep.getDocument().addDocumentListener(new DocumentListener() {
            private void buscarEndereco() {
                String cep = tfCep.getText().replaceAll("\\D", "");
                if (cep.length() != 8) return;
                new SwingWorker<Endereco, Void>() {
                    @Override
                    protected Endereco doInBackground() throws Exception {
                        return ViaCepService.buscarEndereco(cep);
                    }
                    @Override
                    protected void done() {
                        try {
                            Endereco endereco = get();
                            if (endereco != null) {
                                tfRua.setText(endereco.getRua());
                                tfBairro.setText(endereco.getBairro());
                                tfCidade.setText(endereco.getCidade());
                                cbEstado.setSelectedItem(endereco.getEstado());
                            } else {
                                tfRua.setText("");
                                tfBairro.setText("");
                                tfCidade.setText("");
                                cbEstado.setSelectedIndex(0);
                            }
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, "Erro ao buscar endereço via CEP: " + ex.getMessage(), ex);
                        }
                    }
                }.execute();
            }
            public void insertUpdate(DocumentEvent e) { buscarEndereco(); }
            public void removeUpdate(DocumentEvent e) { buscarEndereco(); }
            public void changedUpdate(DocumentEvent e) { buscarEndereco(); }
        });

        // Rua
        gbc.gridx = 2;
        gbc.gridy = 0;
        JLabel lblRua = new JLabel("Rua:");
        lblRua.setFont(labelFont);
        add(lblRua, gbc);
        tfRua = new JTextField(20);
        tfRua.setPreferredSize(new Dimension(120, 25));
        gbc.gridx = 3;
        add(tfRua, gbc);

        // Número
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel lblNumero = new JLabel("Número:");
        lblNumero.setFont(labelFont);
        add(lblNumero, gbc);
        tfNumero = new JTextField(8);
        tfNumero.setPreferredSize(new Dimension(100, 25));
        gbc.gridx = 1;
        add(tfNumero, gbc);

        // Complemento
        gbc.gridx = 2;
        gbc.gridy = 1;
        JLabel lblComplemento = new JLabel("Complemento:");
        lblComplemento.setFont(labelFont);
        add(lblComplemento, gbc);
        tfComplemento = new JTextField(15);
        tfComplemento.setPreferredSize(new Dimension(120, 25));
        gbc.gridx = 3;
        add(tfComplemento, gbc);

        // Bairro
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel lblBairro = new JLabel("Bairro:");
        lblBairro.setFont(labelFont);
        add(lblBairro, gbc);
        tfBairro = new JTextField(15);
        tfBairro.setPreferredSize(new Dimension(120, 25));
        gbc.gridx = 1;
        add(tfBairro, gbc);

        // Cidade
        gbc.gridx = 2;
        gbc.gridy = 2;
        JLabel lblCidade = new JLabel("Cidade:");
        lblCidade.setFont(labelFont);
        add(lblCidade, gbc);
        tfCidade = new JTextField(15);
        tfCidade.setPreferredSize(new Dimension(120, 25));
        gbc.gridx = 3;
        add(tfCidade, gbc);

        // Estado
        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel lblEstado = new JLabel("Estado:");
        lblEstado.setFont(labelFont);
        add(lblEstado, gbc);
        String[] estados = {"AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG",
                "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO"};
        cbEstado = new JComboBox<>(estados);
        cbEstado.setPreferredSize(new Dimension(100, 30));
        cbEstado.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridx = 1;
        add(cbEstado, gbc);
    }

    // Retorna um objeto Endereco preenchido com os dados do painel
    public Endereco getEndereco() {
        Endereco endereco = new Endereco();
        endereco.setCep(tfCep.getText().trim());
        endereco.setRua(tfRua.getText().trim());
        endereco.setNumero(tfNumero.getText().trim());
        endereco.setComplemento(tfComplemento.getText().trim());
        endereco.setBairro(tfBairro.getText().trim());
        endereco.setCidade(tfCidade.getText().trim());
        endereco.setEstado((String) cbEstado.getSelectedItem());
        return endereco;
    }

    // Limpa os campos do painel
    public void limparCampos() {
        tfCep.setText("");
        tfRua.setText("");
        tfNumero.setText("");
        tfComplemento.setText("");
        tfBairro.setText("");
        tfCidade.setText("");
        cbEstado.setSelectedIndex(0);
    }
}