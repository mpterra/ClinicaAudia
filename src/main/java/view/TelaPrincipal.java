package view;

import java.awt.*;
import java.net.URL;
import javax.swing.*;

import util.MenuUtils;
import util.Sessao;

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

		// abrir a tela que quero de inicio
		abrirAgenda();
	}

	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		
		//MENU AGENDA
		menuBar.add(MenuUtils.createClickableMenu("Agenda", () -> abrirAgenda()));
		
		// MENU ATENDIMENTO
		JMenu menuAtendimento = createMenu("Atendimento");
		menuAtendimento.add(createMenuItem("Marcar Atendimento", e -> {		}));
		menuAtendimento.add(createMenuItem("Vender Produto", e -> {		}));
		menuAtendimento.addSeparator();
		menuAtendimento.add(createMenuItem("Relatório", e -> {		}));

		// MENU PACIENTES
		JMenu menuPacientes = createMenu("Pacientes");
		menuPacientes.add(createMenuItem("Novo Paciente", e -> {try {
			abrirCadastroPaciente();
		} catch (Exception e1) {
			e1.printStackTrace();
		}}));
		menuPacientes.addSeparator();
		menuPacientes.add(createMenuItem("Prontuários", e -> {		}));
		
		//MENU ORÇAMENTOS
		JMenu menuOrcamentos = createMenu("Orçamentos");
		menuOrcamentos.add(createMenuItem("Novo Orçamento", e -> {		}));
		menuOrcamentos.addSeparator();
		menuOrcamentos.add(createMenuItem("Consultar Orçamentos", e -> {		}));


		// MENU FINANCEIRO
		JMenu menuFinanceiro = createMenu("Financeiro");
		menuFinanceiro.add(createMenuItem("Caixa", e -> {		}));
		menuFinanceiro.addSeparator();
		menuFinanceiro.add(createMenuItem("Lançar Despesa", e -> {		}));
		menuFinanceiro.addSeparator();
		menuFinanceiro.add(createMenuItem("Contas a Pagar", e -> {		}));
		menuFinanceiro.add(createMenuItem("Contas a Receber", e -> {		}));
		menuFinanceiro.addSeparator();
		menuFinanceiro.add(createMenuItem("Relatórios", e -> {		}));

		// MENU ESTOQUE
		JMenu menuEstoque = createMenu("Estoque");
		menuEstoque.add(createMenuItem("Conferir Estoque", e -> {		}));
		menuEstoque.add(createMenuItem("Comprar Itens", e -> {		}));
		menuEstoque.addSeparator();
		menuEstoque.add(createMenuItem("Baixa de Estoque", e -> {		}));
		menuEstoque.addSeparator();
		menuEstoque.add(createMenuItem("Relatórios", e -> {		}));

		// MENU CADASTRO
		JMenu menuCadastro = createMenu("Cadastro");
		menuCadastro.add(createMenuItem("Tipos de Produto", e -> {		}));
		menuCadastro.add(createMenuItem("Produtos", e -> {		}));
		menuCadastro.addSeparator();
		menuCadastro.add(createMenuItem("Profissionais", e -> {abrirCadastroProfissional();}));
		menuCadastro.add(createMenuItem("Usuários", e -> abrirCadastroUsuario()));
		
		//MONTAGEM DO RESTANTE DO MENU
		
		menuBar.add(menuAtendimento);
		menuBar.add(menuPacientes);
		menuBar.add(menuOrcamentos);
		menuBar.add(menuFinanceiro);
		menuBar.add(menuEstoque);
		menuBar.add(menuCadastro);
		
		
		
		// Espaço flexível para empurrar o label do usuário para a direita
		menuBar.add(Box.createHorizontalGlue());

		// label do usuário logado
		JLabel lblUsuarioLogado = new JLabel("Usuário: " + Sessao.getUsuarioLogado().getLogin());
		lblUsuarioLogado.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
		menuBar.add(lblUsuarioLogado);

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
	
	private void abrirCadastroPaciente() throws Exception {
		painelCentral.removeAll();
		painelCentral.add(new CadastroPacientePanel(), BorderLayout.CENTER);
		painelCentral.revalidate();
		painelCentral.repaint();
	}
	
	private void abrirAgenda() {
		painelCentral.removeAll();
		painelCentral.add(new AgendaPanel(), BorderLayout.CENTER);
		painelCentral.revalidate();
		painelCentral.repaint();
	}
	
	private void abrirCadastroProfissional() {
		painelCentral.removeAll();
		painelCentral.add(new CadastroProfissionalPanel(), BorderLayout.CENTER);
		painelCentral.revalidate();
		painelCentral.repaint();
	}
}
