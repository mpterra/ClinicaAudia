package util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MenuUtils {

    /**
     * Cria um JMenu clicável diretamente, sem JMenuItem, com ação passada.
     * @param titulo Título do menu
     * @param acao Runnable a executar ao clicar
     * @return JMenu pronto
     */
    public static JMenu createClickableMenu(String titulo, Runnable acao) {
        JMenu menu = new JMenu(titulo);
        menu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        menu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                acao.run();
            }
        });
        return menu;
    }
}
