package view;

import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Image;
import java.net.URL;
import java.sql.SQLException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.ImageIcon;

import com.formdev.flatlaf.FlatLightLaf;

import controller.UsuarioController;

public class TelaLogin {

    private JFrame frame;
    private JTextField textLogin;
    private JPasswordField passwordField;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        EventQueue.invokeLater(() -> {
            TelaLogin window = new TelaLogin();
            window.frame.setVisible(true);
        });
    }

    public TelaLogin() {
        initialize();
    }

    private void initialize() {
        // Tamanho do frame ajustado
        int frameWidth = 400;
        int frameHeight = 520;

        frame = new JFrame();
        frame.setResizable(false);
        frame.setTitle("Sistema de Gestão de Clínicas");
        frame.setBounds(100, 100, frameWidth, frameHeight);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setLayout(null);

        // Painel com imagem ajustada para caber proporcionalmente
        JPanel panel = new JPanel() {
            private static final long serialVersionUID = 1L;
			private Image imagem;

            {
                URL url = getClass().getResource("/images/logo.png");
                if (url != null) {
                    imagem = new ImageIcon(url).getImage();
                } else {
                    System.out.println("Imagem não encontrada!");
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (imagem != null) {
                    int panelWidth = getWidth();
                    int panelHeight = getHeight();

                    int imgWidth = imagem.getWidth(this);
                    int imgHeight = imagem.getHeight(this);

                    // escala proporcional para caber dentro do painel
                    double scale = Math.min((double) panelWidth / imgWidth, (double) panelHeight / imgHeight);

                    int width = (int) (imgWidth * scale);
                    int height = (int) (imgHeight * scale);

                    int x = (panelWidth - width) / 2;
                    int y = (panelHeight - height) / 2;

                    g.drawImage(imagem, x, y, width, height, this);
                }
            }
        };
        panel.setBounds(52, 31, 290, 128);
        frame.getContentPane().add(panel);

        // Componentes de login
        JLabel lblLogin = new JLabel("Login");
        lblLogin.setHorizontalAlignment(SwingConstants.LEFT);
        lblLogin.setFont(new Font("Tahoma", Font.PLAIN, 13));
        lblLogin.setBounds(173, 193, 34, 21);
        frame.getContentPane().add(lblLogin);

        JLabel lblSenha = new JLabel("Senha");
        lblSenha.setHorizontalAlignment(SwingConstants.LEFT);
        lblSenha.setFont(new Font("Tahoma", Font.PLAIN, 13));
        lblSenha.setBounds(173, 255, 44, 21);
        frame.getContentPane().add(lblSenha);

        textLogin = new JTextField();
        textLogin.setBounds(100, 216, 191, 20);
        frame.getContentPane().add(textLogin);
        textLogin.setColumns(10);

        passwordField = new JPasswordField();
        passwordField.setBounds(100, 277, 191, 20);
        frame.getContentPane().add(passwordField);

        JLabel lblUsuarioIncorreto = new JLabel("Usuário ou senha incorretos");
        lblUsuarioIncorreto.setHorizontalAlignment(SwingConstants.CENTER);
        lblUsuarioIncorreto.setFont(new Font("Tahoma", Font.PLAIN, 14));
        lblUsuarioIncorreto.setForeground(Color.RED);
        lblUsuarioIncorreto.setBounds(100, 384, 191, 14);
        frame.getContentPane().add(lblUsuarioIncorreto);
        lblUsuarioIncorreto.setVisible(false);

        JButton btnEntrar = new JButton("Entrar");
        btnEntrar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEntrar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String login = textLogin.getText();
                String senha = new String(passwordField.getPassword());
                boolean sucesso = false;
                
                UsuarioController uc = new UsuarioController();
                try {
					sucesso = uc.login(login, senha);
					if (sucesso) {
	                    frame.dispose();
	                    new TelaPrincipal().setVisible(true);
	                } else {
	                    lblUsuarioIncorreto.setVisible(true);
	                }
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
                
            }
        });

        btnEntrar.setBounds(149, 329, 89, 23);
        frame.getContentPane().add(btnEntrar);
    }
}
