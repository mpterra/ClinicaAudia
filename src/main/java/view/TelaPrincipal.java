package view;

import java.awt.*;
import java.net.URL;
import javax.swing.*;

public class TelaPrincipal extends JFrame {

    private JPanel painelCentral;

    public TelaPrincipal() {
        setTitle("Clínica Áudia - Sistema de Gestão");

        // ====== Ícone do JFrame ======
        try {
            URL iconURL = getClass().getClassLoader().getResource("images/icon.png");
            if (iconURL != null) {
                Image icon = new ImageIcon(iconURL).getImage();
                setIconImage(icon);
            } else {
                System.out.println("Ícone não encontrado! Verifique src/main/resources/images/icon.png");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // =============================

        // Tamanho padrão
        setSize(980, 680);
        setMinimumSize(new Dimension(700, 480));

        // Centraliza na tela
        setLocationRelativeTo(null);

        // Abre maximizado por padrão
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Painel central
        painelCentral = new JPanel(new BorderLayout());
        add(painelCentral, BorderLayout.CENTER);

        setJMenuBar(createMenuBar());
        
        //abrir a tela que quero de inicio
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // MENU ATENDIMENTO
        JMenu menuAtendimento = createMenu("Atendimento");
        menuAtendimento.add(createMenuItem("Agenda Profissional", e -> {}));
        menuAtendimento.add(createMenuItem("Marcar Atendimento", e -> {}));
        menuAtendimento.add(createMenuItem("Evolução do Paciente", e -> {}));

        // MENU FINANCEIRO
        JMenu menuFinanceiro = createMenu("Financeiro");
        menuFinanceiro.add(createMenuItem("Pagamentos Atendimentos", e -> {}));
        menuFinanceiro.add(createMenuItem("Vendas", e -> {}));
        menuFinanceiro.add(createMenuItem("Pagamentos Vendas", e -> {}));
        menuFinanceiro.add(createMenuItem("Caixa", e -> {}));

        // MENU ESTOQUE
        JMenu menuEstoque = createMenu("Estoque");
        menuEstoque.add(createMenuItem("Movimento de Estoque", e -> {}));
        menuEstoque.add(createMenuItem("Consultar Estoque", e -> {}));

        // MENU RELATÓRIOS
        JMenu menuRelatorios = createMenu("Relatórios");
        menuRelatorios.add(createMenuItem("Pacientes", e -> {}));
        menuRelatorios.add(createMenuItem("Atendimentos", e -> {}));
        menuRelatorios.add(createMenuItem("Vendas", e -> {}));
        menuRelatorios.add(createMenuItem("Caixa", e -> {}));

        // MENU CADASTRO
        JMenu menuCadastro = createMenu("Cadastro");
        menuCadastro.add(createMenuItem("Pacientes", e -> {}));
        menuCadastro.addSeparator();
        menuCadastro.add(createMenuItem("Tipos de Produto", e -> {}));
        menuCadastro.add(createMenuItem("Produtos", e -> {}));
        menuCadastro.addSeparator();
        menuCadastro.add(createMenuItem("Profissionais", e -> {}));
        menuCadastro.add(createMenuItem("Usuários", e -> abrirCadastroUsuario()));

        menuBar.add(menuAtendimento);
        menuBar.add(menuFinanceiro);
        menuBar.add(menuEstoque);
        menuBar.add(menuRelatorios);
        menuBar.add(menuCadastro);

        return menuBar;
    }

    private JMenu createMenu(String text) {
        JMenu menu = new JMenu(text);
        menu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return menu;
    }

    private JMenuItem createMenuItem(String text, java.awt.event.ActionListener action) {
        JMenuItem item = new JMenuItem(text);
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        item.addActionListener(action);
        return item;
    }

    // ==========================
    // MÉTODOS PARA ABRIR PAINÉIS
    // ==========================
    
    private void abrirCadastroUsuario() {
        painelCentral.removeAll();
        painelCentral.add(new CadastroUsuarioPanel(), BorderLayout.CENTER);
        painelCentral.revalidate();
        painelCentral.repaint();
    }
}
