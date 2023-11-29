// note: This is copy pasted Ex02, to have something to work with
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
////////////////////////////////////////////////////////////////////////////////
// THE INPUT ARGUMENTS: name, path, size, highlighted value SEPARATED BY COMMA
////////////////////////////////////////////////////////////////////////////////
public class Ex02 {
    public static void main(String[] args) {
        if (args.length == 4) {

            //Create instance of class Layer
            Layer layer = new Layer(args[0], args[1]);

            //create frames
            JFrame appFrame1 = new JFrame();
            JFrame appFrame2 = new JFrame();

            //create image 1
            BufferedImage image = layer.toImage();
            MapPanel myMapPanel = new MapPanel(image, Integer.parseInt(args[2]));

            // reading the highlighted values. SHOULD BE SEPARATED BY COMMA
            String[] highlightString = args[3].split(",");
            double[] highlightValues = new double[highlightString.length];
            for (int i = 0; i < highlightString.length; i++) {
                highlightValues[i] = Double.parseDouble(highlightString[i]);
            }

            int scale = Integer.parseInt(args[2]);

            // create image 2
            BufferedImage image2 = layer.toImage(highlightValues);
            MapPanel myMapPanel2 = new MapPanel(image2, scale);

            //add to frame
            appFrame1.add(myMapPanel);
            appFrame2.add(myMapPanel2);

            // set frame size
            Dimension dimension = new Dimension(scale * layer.getnCols(),scale * layer.getnRows());
            appFrame1.setSize(dimension);
            appFrame2.setSize(dimension);

            // make appFrame visible
            appFrame1.setVisible(true);
            appFrame2.setVisible(true);
        } else {
            System.out.println("Too many or few arguments......");
        }
    }
}