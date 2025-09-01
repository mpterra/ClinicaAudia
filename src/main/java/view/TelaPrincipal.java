
package view;

import java.awt.Cursor;
import java.awt.Dimension;
import javax.swing.*;

import com.formdev.flatlaf.FlatLightLaf;

public class TelaPrincipal extends JFrame {

    private JDesktopPane desktop;

    public TelaPrincipal() {
        setTitle("Clínica Áudia - Sistema de Gestão");

        // Tamanho padrão (quando não maximizado)
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

        // ====== MENU ATENDIMENTO ======
        JMenu menuAtendimento = createMenu("Atendimento");
        JMenuItem miAgenda = createMenuItem("Agenda Profissional");
        JMenuItem miAtendimentos = createMenuItem("Marcar Atendimento");
        JMenuItem miEvolucao = createMenuItem("Evolução do Paciente");
        menuAtendimento.add(miAgenda);
        menuAtendimento.add(miAtendimentos);
        menuAtendimento.add(miEvolucao);

        // ====== MENU FINANCEIRO ======
        JMenu menuFinanceiro = createMenu("Financeiro");
        JMenuItem miPagAtendimento = createMenuItem("Pagamentos Atendimentos");
        JMenuItem miVendas = createMenuItem("Vendas");
        JMenuItem miPagVendas = createMenuItem("Pagamentos Vendas");
        JMenuItem miCaixa = createMenuItem("Caixa");
        menuFinanceiro.add(miPagAtendimento);
        menuFinanceiro.add(miVendas);
        menuFinanceiro.add(miPagVendas);
        menuFinanceiro.add(miCaixa);

        // ====== MENU ESTOQUE ======
        JMenu menuEstoque = createMenu("Estoque");
        JMenuItem miMovimento = createMenuItem("Movimento de Estoque");
        JMenuItem miConsulta = createMenuItem("Consultar Estoque");
        menuEstoque.add(miMovimento);
        menuEstoque.add(miConsulta);

        // ====== MENU RELATÓRIOS ======
        JMenu menuRelatorios = createMenu("Relatórios");
        JMenuItem miRelPacientes = createMenuItem("Pacientes");
        JMenuItem miRelAtendimentos = createMenuItem("Atendimentos");
        JMenuItem miRelVendas = createMenuItem("Vendas");
        JMenuItem miRelCaixa = createMenuItem("Caixa");
        menuRelatorios.add(miRelPacientes);
        menuRelatorios.add(miRelAtendimentos);
        menuRelatorios.add(miRelVendas);
        menuRelatorios.add(miRelCaixa);
        
        // ====== MENU CADASTRO ======
        JMenu menuCadastro = createMenu("Cadastro");
        JMenuItem miUsuarios = createMenuItem("Usuários");
        JMenuItem miPacientes = createMenuItem("Pacientes");
        JMenuItem miProfissionais = createMenuItem("Profissionais");
        JMenuItem miProdutos = createMenuItem("Produtos");
        JMenuItem miTipoProduto = createMenuItem("Tipos de Produto");

        menuCadastro.add(miPacientes);
        menuCadastro.addSeparator();
        menuCadastro.add(miTipoProduto);
        menuCadastro.add(miProdutos);
        menuCadastro.addSeparator();
        menuCadastro.add(miProfissionais);
        menuCadastro.add(miUsuarios);

        // Adiciona menus na barra
        menuBar.add(menuAtendimento);
        menuBar.add(menuFinanceiro);
        menuBar.add(menuEstoque);
        menuBar.add(menuRelatorios);
        menuBar.add(menuCadastro);

        return menuBar;
    }

    // ====== PADRÃO: MENU COM CURSOR DE MÃO ======
    private JMenu createMenu(String text) {
        JMenu menu = new JMenu(text);
        menu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return menu;
    }

    // ====== PADRÃO: MENU ITEM COM CURSOR DE MÃO ======
    private JMenuItem createMenuItem(String text) {
        JMenuItem item = new JMenuItem(text);
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return item;
    }

    // Método para abrir qualquer InternalFrame centralizado
    public void openInternalFrame(JInternalFrame frame) {
        desktop.add(frame);
        frame.setVisible(true);

        // Centralizar
        Dimension desktopSize = desktop.getSize();
        Dimension jInternalFrameSize = frame.getSize();
        frame.setLocation(
            (desktopSize.width - jInternalFrameSize.width) / 2,
            (desktopSize.height - jInternalFrameSize.height) / 2
        );
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }

        // sempre dentro do EDT
        javax.swing.SwingUtilities.invokeLater(() -> {
            new TelaPrincipal().setVisible(true);
        });
    }
}
