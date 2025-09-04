package exception;

public class CampoObrigatorioException extends Exception {

    private static final long serialVersionUID = 1L;

    // Construtor que recebe a mensagem de erro
    public CampoObrigatorioException(String mensagem) {
        super(mensagem);
    }

    // Construtor padrão (opcional)
    public CampoObrigatorioException() {
        super("Campo obrigatório não preenchido!");
    }
}
