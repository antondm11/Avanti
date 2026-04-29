package teamavanti.view;

import javax.swing.*;

import java.awt.CardLayout;

public class MainFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel contenedor;

    // Paneles que contendrá el MainFrame
    private HomePanel homePanel;
    private SignUpPanel signUpPanel;
    private SignInPanel signInPanel;
    private UserPanel userPanel;
    // Aquí faltaría importar los paneles que quedan

    public MainFrame() {
        cardLayout = new CardLayout();
        contenedor = new JPanel(cardLayout);
        setTitle("Avanti-App");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        /*
         * REVISAR LO DE ESTE COMENTARIO PARA AJUSTAR LA RESOLUCIÓN DEL FRAME
         * 
         * layeredPane = new JLayeredPane();
         * layeredPane.setPreferredSize(new Dimension(1000, 600));
         * 
         * contenedor.setBounds(0, 0, 1000, 600);
         * layeredPane.add(contenedor, JLayeredPane.DEFAULT_LAYER);
         * 
         */

        // Instanciar los paneles
        homePanel = new HomePanel(this);
        signUpPanel = new SignUpPanel();
        signInPanel = new SignInPanel();
        userPanel = new UserPanel();
        // moviePanel = new MoviePanel();

        // Agregar los paneles al contenedor
        contenedor.add(homePanel, "home");
        // AÑADIR LOS PANELES COMENTADOS CUANDO ESTÉN LISTOS
        // contenedor.add(signUpPanel, "signUp");
        // contenedor.add(signInPanel, "signIn");
        // contenedor.add(userPanel, "user");
        // contenedor.add(moviePanel, "movie");
        add(contenedor);
        pack();
        setLocationRelativeTo(null);
    }

    // Método para mostrar un panel
    public void showPanel(String panel) {
        cardLayout.show(contenedor, panel);
    }

}
