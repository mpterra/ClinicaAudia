package view;

import java.awt.*;
import java.net.URL;
import java.sql.SQLException;

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

		// MENU AGENDA
		menuBar.add(MenuUtils.createClickableMenu("Agenda", () -> abrirAgenda()));

		// MENU ATENDIMENTO
		JMenu menuAtendimento = createMenu("Atendimento");
		menuAtendimento.add(createMenuItem("Marcar Atendimento", e -> {
			abrirMarcacaoAtendimentoPanel();
		}));
		menuAtendimento.addSeparator();
		menuAtendimento.add(createMenuItem("Vender Produto", e -> {
			abrirVendaProdutoPanel();
		}));
		menuAtendimento.add(createMenuItem("Hisórico de Vendas", e -> {
			abrirHistoricoVendasPanel();
		}));
		menuAtendimento.addSeparator();
		menuAtendimento.add(createMenuItem("Emprestar Aparelho", e -> { 
			abrirEmprestimoProdutoPanel();
		}));
		menuAtendimento.addSeparator();
		menuAtendimento.add(createMenuItem("Relatório", e -> {
		}));

		// MENU PACIENTES
		JMenu menuPacientes = createMenu("Pacientes");
		menuPacientes.add(createMenuItem("Novo Paciente", e -> {
			try {
				abrirCadastroPaciente();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}));
		menuPacientes.addSeparator();
		menuPacientes.add(createMenuItem("Prontuários", e -> {
		}));

		// MENU ORÇAMENTOS
		JMenu menuOrcamentos = createMenu("Orçamentos");
		menuOrcamentos.add(createMenuItem("Novo Orçamento", e -> {
		}));
		menuOrcamentos.addSeparator();
		menuOrcamentos.add(createMenuItem("Consultar Orçamentos", e -> {
		}));

		// MENU FINANCEIRO
		JMenu menuFinanceiro = createMenu("Financeiro");
		menuFinanceiro.add(createMenuItem("Caixa", e -> {
			abrirCaixaPanel();
		}));
		menuFinanceiro.addSeparator();
		menuFinanceiro.add(createMenuItem("Despesas", e -> {
			abrirLancamentoDespesaPanel();
		}));
		menuFinanceiro.addSeparator();
		menuFinanceiro.add(createMenuItem("Compras a Pagar", e -> {
			abrirPagamentoCompraPanel();
		}));
		menuFinanceiro.add(createMenuItem("Vendas a Receber", e -> {
			abrirPagamentoVendaPanel();
		}));
		menuFinanceiro.addSeparator();
		menuFinanceiro.add(createMenuItem("Relatórios", e -> {
		}));

		// MENU ESTOQUE
		JMenu menuEstoque = createMenu("Estoque");
		menuEstoque.add(createMenuItem("Conferir Estoque", e -> {
			abrirEstoquePanel();
		}));
		menuEstoque.add(createMenuItem("Ajustar Estoque", e -> {
		}));
		menuEstoque.addSeparator();
		menuEstoque.add(createMenuItem("Comprar Itens", e -> {
			abrirCompraProdutoPanel();
		}));		
		menuEstoque.addSeparator();
		menuEstoque.add(createMenuItem("Relatórios", e -> {
		}));

		// MENU CADASTRO
		JMenu menuCadastro = createMenu("Configurações");
		menuCadastro.add(createMenuItem("Tipos de Produto", e -> {
			abrirCadastroTipoProduto();
		}));
		menuCadastro.add(createMenuItem("Produtos", e -> {
			abrirCadastroProduto();
		}));
		menuCadastro.add(createMenuItem("Fornecedores", e -> {
			abrirCadastroFornecedorPanel();
		}));
		menuCadastro.addSeparator();
		menuCadastro.add(createMenuItem("Profissionais", e -> {
			try {
				abrirCadastroProfissional();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}));
		menuCadastro.add(createMenuItem("Agenda por Profissional", e -> {
			abrirCadastroEscalaProfissional();
		}));
		menuCadastro.add(createMenuItem("Valores de Consultas", e -> { 
			abrirCadastroValorAtendimentoPanel();
		}));
		menuCadastro.addSeparator();
		menuCadastro.add(createMenuItem("Empresas Parceiras", e -> { 
			abrirCadastroEmpresaParceiraPanel();
		}));
		menuCadastro.add(createMenuItem("Valores por Empresa", e -> { 
			abrirCadastroValorAtendimentoEmpresaPanel();
		}));
		menuCadastro.addSeparator();
		menuCadastro.add(createMenuItem("Usuários", e -> abrirCadastroUsuario()));

		// MONTAGEM DO RESTANTE DO MENU

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

	private void abrirCadastroProfissional() throws SQLException {
		painelCentral.removeAll();
		painelCentral.add(new CadastroProfissionalPanel(), BorderLayout.CENTER);
		painelCentral.revalidate();
		painelCentral.repaint();
	}

	private void abrirCadastroTipoProduto() {
		painelCentral.removeAll();
		painelCentral.add(new CadastroTipoProdutoPanel(), BorderLayout.CENTER);
		painelCentral.revalidate();
		painelCentral.repaint();
	}

	private void abrirCadastroProduto() {
		painelCentral.removeAll();
		painelCentral.add(new CadastroProdutoPanel(), BorderLayout.CENTER);
		painelCentral.revalidate();
		painelCentral.repaint();
	}

	private void abrirCadastroEscalaProfissional() {
		painelCentral.removeAll();
		painelCentral.add(new CadastroEscalaProfissionalPanel(), BorderLayout.CENTER);
		painelCentral.revalidate();
		painelCentral.repaint();
	}

	private void abrirMarcacaoAtendimentoPanel() {
		painelCentral.removeAll();
		painelCentral.add(new MarcacaoAtendimentoPanel(), BorderLayout.CENTER);
		painelCentral.revalidate();
		painelCentral.repaint();
	}

	private void abrirVendaProdutoPanel() {
		painelCentral.removeAll();
		painelCentral.add(new VendaProdutoPanel(), BorderLayout.CENTER);
		painelCentral.revalidate();
		painelCentral.repaint();
	}

	private void abrirCaixaPanel() {
		painelCentral.removeAll();
		painelCentral.add(new CaixaPanel(), BorderLayout.CENTER);
		painelCentral.revalidate();
		painelCentral.repaint();
	}
	
	private void abrirCadastroValorAtendimentoPanel() {
		painelCentral.removeAll();
		painelCentral.add(new CadastroValorAtendimentoPanel(), BorderLayout.CENTER);
		painelCentral.revalidate();
		painelCentral.repaint();
	}
	
	private void abrirCadastroEmpresaParceiraPanel() {
		painelCentral.removeAll();
		painelCentral.add(new CadastroEmpresaParceiraPanel(), BorderLayout.CENTER);
		painelCentral.revalidate();
		painelCentral.repaint();
	}
	
	private void abrirCadastroValorAtendimentoEmpresaPanel() {
		painelCentral.removeAll();
		painelCentral.add(new CadastroValorAtendimentoEmpresaPanel(), BorderLayout.CENTER);
		painelCentral.revalidate();
		painelCentral.repaint();
	}
	
	private void abrirCompraProdutoPanel() {
		painelCentral.removeAll();
		painelCentral.add(new CompraProdutoPanel(), BorderLayout.CENTER);
		painelCentral.revalidate();
		painelCentral.repaint();
	}
	private void abrirCadastroFornecedorPanel() {
		painelCentral.removeAll();
		painelCentral.add(new CadastroFornecedorPanel(), BorderLayout.CENTER);
		painelCentral.revalidate();
		painelCentral.repaint();
	}
	
	private void abrirEstoquePanel() {
		painelCentral.removeAll();
		painelCentral.add(new EstoquePanel(), BorderLayout.CENTER);
		painelCentral.revalidate();
		painelCentral.repaint();
	}
	
	private void abrirLancamentoDespesaPanel() {
		painelCentral.removeAll();
		painelCentral.add(new LancamentoDespesaPanel(), BorderLayout.CENTER);
		painelCentral.revalidate();
		painelCentral.repaint();
	}
	
	private void abrirEmprestimoProdutoPanel() {
		painelCentral.removeAll();
		painelCentral.add(new EmprestimoProdutoPanel(), BorderLayout.CENTER);
		painelCentral.revalidate();
		painelCentral.repaint();
	}
	
	private void abrirPagamentoCompraPanel() {
		painelCentral.removeAll();
		painelCentral.add(new PagamentoCompraPanel(), BorderLayout.CENTER);
		painelCentral.revalidate();
		painelCentral.repaint();
	}
	
	private void abrirPagamentoVendaPanel() {
		painelCentral.removeAll();
		painelCentral.add(new PagamentoVendaPanel(), BorderLayout.CENTER);
		painelCentral.revalidate();
		painelCentral.repaint();
	}
	
	private void abrirHistoricoVendasPanel() {
		painelCentral.removeAll();
		painelCentral.add(new HistoricoVendasPanel(), BorderLayout.CENTER);
		painelCentral.revalidate();
		painelCentral.repaint();
	}
}
