package util;

public class CPFUtils {

    /**
     * Verifica se um CPF é válido.
     * O CPF deve conter apenas números (11 dígitos).
     *
     * @param cpf String com 11 dígitos do CPF
     * @return true se válido, false caso contrário
     */
    public static boolean isCPFValido(String cpf) {
        if (cpf == null) return false;

        // Remove qualquer caracter que não seja número
        cpf = cpf.replaceAll("\\D", "");

        // Verifica tamanho
        if (cpf.length() != 11) return false;

        // CPFs com todos os dígitos iguais são inválidos
        if (cpf.matches("(\\d)\\1{10}")) return false;

        try {
            int soma1 = 0;
            for (int i = 0; i < 9; i++) {
                soma1 += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
            }
            int digito1 = 11 - (soma1 % 11);
            if (digito1 >= 10) digito1 = 0;

            int soma2 = 0;
            for (int i = 0; i < 10; i++) {
                soma2 += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
            }
            int digito2 = 11 - (soma2 % 11);
            if (digito2 >= 10) digito2 = 0;

            return digito1 == Character.getNumericValue(cpf.charAt(9)) &&
                   digito2 == Character.getNumericValue(cpf.charAt(10));

        } catch (NumberFormatException e) {
            return false;
        }
    }

}
