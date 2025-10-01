// Pacote: src/main/java/service/ValorAtendimentoCalculator.java
package service;

import controller.ValorAtendimentoController;
import controller.ValorAtendimentoEmpresaController;
import model.Atendimento;
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
     * @param prof Profissional.
     * @param tipo Tipo de atendimento.
     * @param empresa Empresa parceira (pode ser null).
     * @return Valor calculado.
     * @throws Exception se não encontrar valor cadastrado.
     */
    public BigDecimal calcularValor(Profissional prof, Atendimento.Tipo tipo, EmpresaParceira empresa) throws Exception {
        BigDecimal valor = null;
        if (empresa != null) {
            List<ValorAtendimentoEmpresa> valoresEmpresa = valorAtendimentoEmpresaController.buscarPorProfissionalEEmpresa(prof.getId(), empresa.getId());
            ValorAtendimentoEmpresa valorEmpresa = valoresEmpresa.stream()
                    .filter(v -> v.getTipo().name().equals(tipo.name()))
                    .findFirst()
                    .orElse(null);
            if (valorEmpresa != null) {
                valor = valorEmpresa.getValor();
            }
        }
        if (valor == null) {
            ValorAtendimento valorAtendimento = valorAtendimentoController.buscarPorProfissionalETipo(prof.getId(), tipo);
            if (valorAtendimento == null) {
                throw new Exception("Nenhum valor cadastrado para o profissional e tipo de atendimento selecionados!");
            }
            valor = valorAtendimento.getValor();
        }
        return valor;
    }
}