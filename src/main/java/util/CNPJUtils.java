package util;

public class CNPJUtils {

    /**
     * Verifica se um CNPJ é válido.
     * O CNPJ deve conter apenas números (14 dígitos).
     *
     * @param cnpj String com 14 dígitos do CNPJ
     * @return true se válido, false caso contrário
     */
    public static boolean isCNPJValido(String cnpj) {
        if (cnpj == null) return false;

        // Remove qualquer caractere que não seja número
        cnpj = cnpj.replaceAll("\\D", "");

        // Verifica tamanho
        if (cnpj.length() != 14) return false;

        // CNPJs com todos os dígitos iguais são inválidos
        if (cnpj.matches("(\\d)\\1{13}")) return false;

        try {
            // Pesos para o primeiro dígito verificador
            int[] pesos1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
            int soma1 = 0;
            for (int i = 0; i < 12; i++) {
                soma1 += Character.getNumericValue(cnpj.charAt(i)) * pesos1[i];
            }
            int digito1 = 11 - (soma1 % 11);
            if (digito1 >= 10) digito1 = 0;

            // Pesos para o segundo dígito verificador
            int[] pesos2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
            int soma2 = 0;
            for (int i = 0; i < 13; i++) {
                soma2 += Character.getNumericValue(cnpj.charAt(i)) * pesos2[i];
            }
            int digito2 = 11 - (soma2 % 11);
            if (digito2 >= 10) digito2 = 0;

            return digito1 == Character.getNumericValue(cnpj.charAt(12)) &&
                   digito2 == Character.getNumericValue(cnpj.charAt(13));

        } catch (NumberFormatException e) {
            return false;
        }
    }
}