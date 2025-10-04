package util;

import java.util.Random;

public class CpfGenerator {
    private static final Random rnd = new Random();

    public static void main(String[] args) {
        // Gera e imprime 2 CPFs válidos para uso em ambiente de teste
        for (int i = 0; i < 2; i++) {
            String cpf = gerarCpf();
            System.out.println(cpf);
        }
    }

    // Gera CPF no formato XXX.XXX.XXX-YY
    public static String gerarCpf() {
        int[] n = new int[11]; // 0..8 = base, 9 = dig1, 10 = dig2

        // Gera 9 dígitos iniciais (pode ajustar para evitar sequências triviais)
        for (int i = 0; i < 9; i++) {
            n[i] = rnd.nextInt(10);
        }

        // Calcula primeiro dígito verificador
        int soma = 0;
        int peso = 10;
        for (int i = 0; i < 9; i++) {
            soma += n[i] * peso;
            peso--;
        }
        int resto = soma % 11;
        n[9] = (resto < 2) ? 0 : 11 - resto;

        // Calcula segundo dígito verificador
        soma = 0;
        peso = 11;
        for (int i = 0; i < 10; i++) {
            soma += n[i] * peso;
            peso--;
        }
        resto = soma % 11;
        n[10] = (resto < 2) ? 0 : 11 - resto;

        // Formata como XXX.XXX.XXX-YY
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 11; i++) {
            sb.append(n[i]);
            if (i == 2 || i == 5) sb.append('.');
            if (i == 8) sb.append('-');
        }
        return sb.toString();
    }
}
