package service;

import controller.ValorAtendimentoController;
import controller.ValorAtendimentoEmpresaController;
import model.Atendimento;
import model.EmpresaParceira;
import model.Profissional;
import model.ValorAtendimento;
import model.ValorAtendimentoEmpresa;

import java.math.BigDecimal;
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
     * @param prof Profissional.
     * @param tipo Tipo de atendimento.
     * @param empresa Empresa parceira (pode ser null).
     * @return Valor calculado.
     * @throws Exception se não encontrar valor cadastrado.
     */
    public BigDecimal calcularValor(Profissional prof, Atendimento.Tipo tipo, EmpresaParceira empresa) throws Exception {
        // Valida parâmetros obrigatórios
        if (prof == null || tipo == null) {
            throw new IllegalArgumentException("Profissional e tipo de atendimento são obrigatórios.");
        }

        BigDecimal valor = null;
        // Verifica se há empresa parceira e busca valor específico
        if (empresa != null && empresa.getId() > 0) {
            List<ValorAtendimentoEmpresa> valoresEmpresa = valorAtendimentoEmpresaController.buscarPorProfissionalEEmpresa(prof.getId(), empresa.getId());
            ValorAtendimentoEmpresa valorEmpresa = valoresEmpresa.stream()
                    .filter(v -> v.getTipo().name().equals(tipo.name())) // Comparação por nome do enum
                    .findFirst()
                    .orElse(null);
            if (valorEmpresa != null && valorEmpresa.getValor() != null) {
                valor = valorEmpresa.getValor();
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