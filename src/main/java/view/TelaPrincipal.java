package view;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.net.URL;
import javax.swing.*;

public class TelaPrincipal extends JFrame {

    private JDesktopPane desktop;

    public TelaPrincipal() {
        setTitle("Clínica Áudia - Sistema de Gestão");

        // ====== Ícone do JFrame ======
        try {
            // Busca o ícone no classpath (src/main/resources/images/)
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
        // =================================

        // Tamanho padrão
        setSize(980, 680);
        setMinimumSize(new Dimension(700, 480));

        // Centraliza na tela
        setLocationRelativeTo(null);

        // Abre maximizado por padrão
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        desktop = new JDesktopPane();
        add(desktop);

        setJMenuBar(createMenuBar());
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // MENU ATENDIMENTO
        JMenu menuAtendimento = createMenu("Atendimento");
        menuAtendimento.add(createMenuItem("Agenda Profissional"));
        menuAtendimento.add(createMenuItem("Marcar Atendimento"));
        menuAtendimento.add(createMenuItem("Evolução do Paciente"));

        // MENU FINANCEIRO
        JMenu menuFinanceiro = createMenu("Financeiro");
        menuFinanceiro.add(createMenuItem("Pagamentos Atendimentos"));
        menuFinanceiro.add(createMenuItem("Vendas"));
        menuFinanceiro.add(createMenuItem("Pagamentos Vendas"));
        menuFinanceiro.add(createMenuItem("Caixa"));

        // MENU ESTOQUE
        JMenu menuEstoque = createMenu("Estoque");
        menuEstoque.add(createMenuItem("Movimento de Estoque"));
        menuEstoque.add(createMenuItem("Consultar Estoque"));

        // MENU RELATÓRIOS
        JMenu menuRelatorios = createMenu("Relatórios");
        menuRelatorios.add(createMenuItem("Pacientes"));
        menuRelatorios.add(createMenuItem("Atendimentos"));
        menuRelatorios.add(createMenuItem("Vendas"));
        menuRelatorios.add(createMenuItem("Caixa"));

        // MENU CADASTRO
        JMenu menuCadastro = createMenu("Cadastro");
        menuCadastro.add(createMenuItem("Pacientes"));
        menuCadastro.addSeparator();
        menuCadastro.add(createMenuItem("Tipos de Produto"));
        menuCadastro.add(createMenuItem("Produtos"));
        menuCadastro.addSeparator();
        menuCadastro.add(createMenuItem("Profissionais"));
        menuCadastro.add(createMenuItem("Usuários"));

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

    private JMenuItem createMenuItem(String text) {
        JMenuItem item = new JMenuItem(text);
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return item;
    }

    public void openInternalFrame(final JInternalFrame frame) {
        desktop.add(frame);
        frame.setVisible(true);

        Runnable centralizar = () -> {
            Dimension desktopSize = desktop.getSize();
            Dimension frameSize = frame.getSize();
            frame.setLocation((desktopSize.width - frameSize.width) / 2, (desktopSize.height - frameSize.height) / 2);
        };

        centralizar.run();

        desktop.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                centralizar.run();
            }
        });
    }
}
