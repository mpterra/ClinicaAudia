package service;

import controller.ValorAtendimentoController;
import controller.ValorAtendimentoEmpresaController;
import model.EmpresaParceira;
import model.Profissional;
import model.ValorAtendimento;
import model.ValorAtendimentoEmpresa;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Serviço para calcular o valor de atendimentos com base em profissional, tipo e empresa.
 */
public class ValorAtendimentoCalculator {
    private final ValorAtendimentoController valorAtendimentoController;
    private final ValorAtendimentoEmpresaController valorAtendimentoEmpresaController;

    // Construtor
    public ValorAtendimentoCalculator(ValorAtendimentoController valorAtendimentoController,
                                      ValorAtendimentoEmpresaController valorAtendimentoEmpresaController) {
        this.valorAtendimentoController = valorAtendimentoController;
        this.valorAtendimentoEmpresaController = valorAtendimentoEmpresaController;
    }

    /**
     * Calcula o valor do atendimento, priorizando valor por empresa se aplicável.
     * @param prof Profissional (não pode ser nulo).
     * @param tipo Tipo de atendimento (não pode ser nulo).
     * @param empresa Empresa parceira (pode ser null).
     * @return Valor calculado do atendimento.
     * @throws IllegalArgumentException se profissional ou tipo forem nulos.
     * @throws SQLException se houver erro ao acessar o banco de dados.
     * @throws Exception se não encontrar valor cadastrado.
     */
    public BigDecimal calcularValor(Profissional prof, ValorAtendimento.Tipo tipo, EmpresaParceira empresa) throws SQLException, Exception {
        // Valida parâmetros obrigatórios
        if (prof == null) {
            throw new IllegalArgumentException("Profissional não pode ser nulo.");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo de atendimento não pode ser nulo.");
        }

        BigDecimal valor = null;
        // Verifica se há empresa parceira e busca valor específico
        if (empresa != null && empresa.getId() > 0) {
            List<ValorAtendimentoEmpresa> valoresEmpresa = valorAtendimentoEmpresaController.buscarPorProfissionalEEmpresa(prof.getId(), empresa.getId());
            for (ValorAtendimentoEmpresa valorEmpresa : valoresEmpresa) {
                if (valorEmpresa.getTipo().name().equals(tipo.name()) && valorEmpresa.getValor() != null) {
                    valor = valorEmpresa.getValor();
                    break;
                }
            }
        }

        // Se não encontrou valor para empresa ou empresa é nula, busca valor padrão
        if (valor == null) {
            ValorAtendimento valorAtendimento = valorAtendimentoController.buscarPorProfissionalETipo(prof.getId(), tipo);
            if (valorAtendimento == null || valorAtendimento.getValor() == null) {
                throw new Exception("Nenhum valor cadastrado para o profissional e tipo de atendimento selecionados!");
            }
            valor = valorAtendimento.getValor();
        }

        return valor;
    }
}