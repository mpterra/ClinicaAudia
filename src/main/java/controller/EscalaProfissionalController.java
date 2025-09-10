package controller;

import dao.EscalaProfissionalDAO;
import model.EscalaProfissional;

import java.sql.SQLException;
import java.util.List;

public class EscalaProfissionalController {

    private final EscalaProfissionalDAO dao;

    public EscalaProfissionalController() {
        this.dao = new EscalaProfissionalDAO();
    }

    public boolean criarEscala(EscalaProfissional escala, String usuarioLogado) throws SQLException {
        if (escala.getHoraFim().before(escala.getHoraInicio())) {
            throw new IllegalArgumentException("Hora de término deve ser depois da hora de início.");
        }
        return dao.salvar(escala, usuarioLogado);
    }

    public boolean atualizarEscala(EscalaProfissional escala, String usuarioLogado) throws SQLException {
        if (escala.getHoraFim().before(escala.getHoraInicio())) {
            throw new IllegalArgumentException("Hora de término deve ser depois da hora de início.");
        }
        return dao.atualizar(escala, usuarioLogado);
    }

    public boolean removerEscala(int id) throws SQLException {
        return dao.deletar(id);
    }

    public EscalaProfissional buscarPorId(int id) throws SQLException {
        return dao.buscarPorId(id);
    }

    public List<EscalaProfissional> listarTodas() throws SQLException {
        return dao.listarTodos();
    }

    public List<EscalaProfissional> listarDisponiveis(int profissionalId, int diaSemana) throws SQLException {
        return dao.listarTodos().stream()
                .filter(e -> e.getProfissionalId() == profissionalId && e.getDiaSemana() == diaSemana && e.isDisponivel())
                .toList();
    }

	public List<EscalaProfissional> listarPorProfissional(int id) {
		return dao.listarPorProfissional(id);
	}

	public void removerTodasEscalasDoProfissional(int id) {
		dao.removerTodasEscalasDoProfissional(id);
		
	}
}
