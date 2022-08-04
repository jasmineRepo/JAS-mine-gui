package microsim.gui.shell;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The panel used by AboutFrame window.
 */
public class AboutPanel extends JPanel {

    @Serial
    private static final long serialVersionUID = 1L;

    BorderLayout borderLayout1 = new BorderLayout();

    ImageIcon imageJAS = new ImageIcon(
            java.awt.Toolkit.getDefaultToolkit().getImage(getClass().getResource("/microsim/gui/icons/logo_2.png")));

    /**
     * Create a new about panel.
     */
    public AboutPanel() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void jbInit() throws Exception {
        this.setLayout(borderLayout1);
    }

    /**
     * Draw the panel content.
     *
     * @param g The graphic device context.
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.white);
        g.fillRect(0, 0, getWidth(), getHeight());

        int leftCorner = (getWidth() > imageJAS.getIconWidth()) ? (getWidth() - imageJAS.getIconWidth() - 40) / 2 : 0;
        int areaHeight = imageJAS.getIconHeight() + 80;
        int upperCorner = (getHeight() > areaHeight) ? (getHeight() - areaHeight) / 2 : 0;

        g.drawImage(imageJAS.getImage(), leftCorner, upperCorner,
                imageJAS.getIconWidth(), imageJAS.getIconHeight(), this);

        g.setColor(Color.black);
        Font font = new Font("Arial", Font.BOLD, 12);
        Font font2 = new Font("Script", Font.BOLD, 12);
        g.setFont(font);
        int start = 30 + upperCorner;
        leftCorner += 160;
        g.drawString("JAS-mine", leftCorner + 10, start + 10);

        SimpleDateFormat sdf = new SimpleDateFormat("yy");
        g.drawString("Copyright (C) 2014-" + sdf.format(new Date()) + " Ross E. Richardson", leftCorner + 10,
                start + 30);
        g.drawString("& Matteo Richiardi", leftCorner + 135, start + 50);// todo check the copyright
        g.setFont(font2);
        g.drawString("https://github.com/jasmineRepo", leftCorner + 10, start + 70);
        g.setColor(new Color(63, 0xc3, 0xe7));
        g.drawString("https://www.jas-mine.net", leftCorner + 10, start + 90);
        g.setColor(Color.black);
        g.setFont(font);
        g.drawString("Distributed under GNU Lesser General Public License", leftCorner - 40, start + 110);
    }
}
