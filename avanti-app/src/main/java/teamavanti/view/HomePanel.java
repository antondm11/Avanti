package teamavanti.view;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//Panel principal de la aplicación, que aparece al iniciarla
public class HomePanel extends JPanel implements ActionListener {
    private JButton btnSignUp;
    private JButton btnSignIn;
    private JButton btnQuit;

    // Referencia al marco principal
    private MainFrame mainFrame;

    public HomePanel(MainFrame frame) {
        this.mainFrame = frame;
        setPreferredSize(new Dimension(1000, 600));

        // Faltaría ver si ponemos fondo o alguna imagen, para insertar Paint Component

        // Rectángulo exterior con GridBagLayout
        setLayout(new GridBagLayout());
        setBackground(new Color(45, 45, 45));

        // Rectángulo interior con BoxLayout
        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
        innerPanel.setBackground(new Color(60, 60, 60));
        // Agrandar innerPanel incrementando el padding interior
        innerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100), 2),
                BorderFactory.createEmptyBorder(100, 150, 100, 150)));

        // Crear los botones
        btnSignUp = new JButton("Registrarse");
        btnSignUp.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSignUp.addActionListener(this);

        btnSignIn = new JButton("Iniciar Sesión");
        btnSignIn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSignIn.addActionListener(this);

        btnQuit = new JButton("Salir");
        btnQuit.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnQuit.addActionListener(this);

        // Añadir los botones al rectángulo interior con espacios entre ellos
        innerPanel.add(btnSignUp);
        innerPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        innerPanel.add(btnSignIn);
        innerPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        innerPanel.add(btnQuit);

        // Rectángulo intermedio que contiene al rectángulo interior (la pantalla TV)
        JPanel outerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                // Opción de antialiasing activado para suavizar *Opcional*
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Color para los adornos
                g2d.setColor(new Color(120, 120, 120));

                // Situar e implementar los círculos
                int circleSize = 30;
                int leftX = 40;
                g2d.fillOval(leftX, h / 3 - circleSize / 2, circleSize, circleSize);
                g2d.fillOval(leftX, 2 * h / 3 - circleSize / 2, circleSize, circleSize);

                // Barra vertical a la derecha
                int rightX = w - 50;
                g2d.setStroke(new BasicStroke(4));

                // Dibujar barra vertical (que comprende del 25% al 75% de la altura)
                g2d.drawLine(rightX, h / 4, rightX, 3 * h / 4);

                // Barra horizontal para simular el dial a la derecha
                int lineCrossWidth = 15;
                g2d.drawLine(rightX - lineCrossWidth, h / 3, rightX + lineCrossWidth, h / 3);
            }
        };

        // Aumentar el tamaño del outerPanel hasta casi el HomePanel (800x600)
        outerPanel.setPreferredSize(new Dimension(760, 560));
        outerPanel.setLayout(new GridBagLayout());
        outerPanel.setBackground(new Color(55, 55, 55));
        outerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80), 3),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        // Añadir el rectángulo interior al panel intermedio
        outerPanel.add(innerPanel);

        // Añadir el panel intermedio al panel principal (HomePanel)
        add(outerPanel);
    }

    // Eventos según el botón pulsado (cambiar al panel correspondiente)
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnSignUp) {

            mainFrame.showPanel("signUp");
        } else if (e.getSource() == btnSignIn) {
            mainFrame.showPanel("signIn");
        } else if (e.getSource() == btnQuit) {

            // Cerrar la conexión a la BD cuando se vaya a salir de la App
            // Descomentar este if cuando esté la bd y se compruebe que funciona la conexion
            // if (DatabaseManager.getInstance().getConnection() != null) {
            // DatabaseManager.getInstance().closeConnection();
            System.exit(0);
            // }
        }
    }
}