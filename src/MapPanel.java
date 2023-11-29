import java.awt.image.*;
import java.awt.Graphics;
import javax.swing.*;

public class MapPanel extends JPanel { // MapPanel is a subclass of JPanel and thus
    // inherits all its attributes and methods.
// ATTRIBUTES
    public BufferedImage image;
    public int scale;
    // CONSTRUCTORS
    public MapPanel(BufferedImage image, int scale) {
        super(); // first, instantiate a MapPanel in the same way JPanel does.
        this.image = image; // then, initialize additional attributes
        this.scale = scale;
    }

    // All the other methods of JPanel (which are automatically inherited), plus:
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g); // first, do what JPanel would normally do. Then do:
        g.drawImage(image, 0, 0, image.getWidth()*scale, image.getHeight()*scale, this);
    }
}