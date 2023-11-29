import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Random;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;

public class Layer {
    public String name;
    private int nRows;
    private int nCols;
    private double[] origin = new double[2];
    private double[] values;
    private double resolution;
    private static double nullValue;

    public Layer(String layerName, String fileName) {
        try {
            File rFile = new File(fileName);

            FileReader fReader = new FileReader(rFile);

            // This object represents lines of Strings created from the stream of characters.
            BufferedReader bReader = new BufferedReader(fReader);

            name = layerName;

            // Read lines
            String[] text = bReader.readLine().split(" ");
            nCols = Integer.parseInt(text[text.length-1]);

            text = bReader.readLine().split(" ");
            nRows = Integer.parseInt(text[text.length-1]);

            text = bReader.readLine().split(" ");
            origin[0] = Double.parseDouble(text[text.length-1]);

            text = bReader.readLine().split(" ");
            origin[1] = Double.parseDouble(text[text.length-1]);

            text = bReader.readLine().split(" ");
            resolution = Double.parseDouble(text[text.length-1]);

            text = bReader.readLine().split(" ");
            nullValue = Double.parseDouble(text[text.length-1]);

            values = new double[nRows * nCols];
            int count = 0;
            String textValue = bReader.readLine();
            while (textValue != null) {
                String[] stringValue = textValue.split(" ");
                for (String s : stringValue) {
                    values[count] = Double.parseDouble(s);
                    count++;
                }
                textValue = bReader.readLine();
            }
            fReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Create an empty layer
    public Layer (String layerName, int rows, int cols, double[] oxy, double res, double nullv) {
        name = layerName;
        nRows = rows;
        nCols = cols;
        origin = oxy;
        resolution = res;
        nullValue = nullv;
        values = new double[nRows*nCols];
    }

    public void print() {
        System.out.println("ncols " + nCols);
        System.out.println("nrows " + nRows);
        System.out.println("xllcorner " + origin[0]);
        System.out.println("yllcorner " + origin[1]);
        System.out.println("cellsize " + resolution);
        System.out.println("NODATA_VALUE " + nullValue);

        for (int i = 0; i < values.length; i++) {
            System.out.print(values[i] + " ");
            if ((i+1) % nCols == 0){
                System.out.print("\n");
            }
        }
    }

    public void save(String outputFileName) {
        try {
            File file = new File(outputFileName);
            FileWriter fWriter = new FileWriter(file);
            fWriter.write("ncols " + nCols + "\n");
            fWriter.write("nrows " + nRows + "\n");
            fWriter.write("xllcorner " + origin[0] + "\n");
            fWriter.write("yllcorner " + origin[1] + "\n");
            fWriter.write("cellsize " + resolution + "\n");
            fWriter.write("NODATA_VALUE " + nullValue + "\n");

            for (int i = 0; i < values.length; i++){
                fWriter.write(values[i] + " ");
                if ((i+1) % nCols == 0){
                    fWriter.write("\n");
                }
            }
            fWriter.close();
            System.out.println("File created.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BufferedImage toImage() {
        // create a BufferedImage of the layer in grayscale
        // This object represents a 24-bit RBG image
        BufferedImage image = new BufferedImage(nRows, nCols, BufferedImage.TYPE_INT_RGB);

        // The above image is empty. To color the image, you first need to get access to
        // its raster, which is represented by the following object.
        WritableRaster raster = image.getRaster();

        int[] color = {128, 128, 128};
        for (int i = 0; i < nRows; i++){
            for (int j = 0; j < nCols; j++){
                if(getMax() == getMin()){
                    raster.setPixel(j, i, color);
                }
                else{
                    color[0] = (int)(255 - (values[i*nCols+j] - getMin()) / (getMax() - getMin()) * 255);
                    color[1] = color[0];
                    color[2] = color[0];
                    raster.setPixel(j, i, color);
                }
            }
        }
        return image;
    }

    public BufferedImage toImage(double[] highlight) {
        // visualize a BufferedImage of the layer in color
        BufferedImage image = new BufferedImage(nRows, nCols, BufferedImage.TYPE_INT_RGB);
        WritableRaster raster = image.getRaster();

        // Generate a random color for each value
        Random random = new Random();
        int[][] hColors = new int[highlight.length][3];
        int count = 0;
        for (double value: highlight){
            hColors[count][0] = random.nextInt(256);
            hColors[count][1] = random.nextInt(256);
            hColors[count][2] = random.nextInt(256);
            count++;
        }

        // Assign colors to values
        // Color for pixels not in highlight
        int[] otherColor = {128, 128, 128};

        // Colors for pixels in highlight
        int[] colors = new int[3];

        int index = -1;
        for (int i = 0; i < nRows; i++){
            for (int j = 0; j < nCols; j++){
                index = findIndex(highlight, values[i*nCols+j]);
                if (index != -1){
                    colors[0] = hColors[index][0];
                    colors[1] = hColors[index][1];
                    colors[2] = hColors[index][2];
                    raster.setPixel(j, i, colors);
                }
                else{
                    colors[0] = otherColor[0];
                    colors[1] = otherColor[1];
                    colors[2] = otherColor[2];
                    raster.setPixel(j, i, colors);
                }
            }
        }
        return image;
    }

    public Layer localSum(Layer inLayer, String outLayerName) {
        Layer outLayer = new Layer(outLayerName, nRows, nCols, origin, resolution, nullValue);

        for (int i = 0; i < (nRows*nCols); i++){
            if (outLayer.values[i] == nullValue){
                continue;
            }
            else{
                outLayer.values[i] = values[i] + inLayer.values[i];
            }
        }
        return outLayer;
    }

    public Layer focalVariety(int radius, boolean isSquare, String outLayerName) {
        Layer outLayer = new Layer(outLayerName, nRows, nCols, origin, resolution, nullValue);
        outLayer.values = new double[nRows*nCols];

        // Iterate every cell
        for (int i = 0; i < nRows; i++){
            for (int j = 0; j < nCols; j++){
                // Get neighborhood indices
                int[] neighborIndices = getNeighborhood(i*nCols+j, radius, isSquare);

                // Get neighborhood values
                double[] neighborValues = new double[neighborIndices.length];
                int count = 0;
                for (int id : neighborIndices){
                    neighborValues[count] = this.values[id];
                    count++;
                }
                // Focal variety value
                outLayer.values[i*nCols+j] = countUniqueValues(neighborValues);
            }
        }
        return outLayer;
    }

    public Layer zonalMinimum(Layer inLayer, String outLayerName) {
        Layer outLayer = new Layer(outLayerName, nRows, nCols, origin, resolution, nullValue);
        outLayer.values = new double[nRows*nCols];

        // Hashmap to store zone and minimum value
        HashMap<Double, Double> zone_min = new HashMap<>();
        for (int i = 0; i < nRows; i++){
            for (int j = 0; j < nCols; j++){
                double zone = inLayer.values[i*nCols+j];
                if(!zone_min.containsKey(zone)){
                    zone_min.put(zone, this.values[i*nCols+j]);
                }
                else{
                    if (zone_min.get(zone) > this.values[i*nCols+j]){
                        zone_min.put(zone, this.values[i*nCols+j]);
                    }
                    else{
                        continue;
                    }
                }
            }
        }

        // Get output values from map
        for (int i = 0; i < nRows; i++){
            for (int j = 0; j < nCols; j++){
                outLayer.values[i*nCols+j] = zone_min.get(inLayer.values[i*nCols+j]);
            }
        }

        return outLayer;
    }

    // Return indices of cells in the neighborhood
    private int[] getNeighborhood(int index, int radius, boolean isSquare) {
        ArrayList<Integer> list = new ArrayList<Integer>();

        int neighbor;

        int row = index / nCols; // the row number of cell i
        int col = index % nCols; // the column number of cell i

        // Get bounds of neighborhood
        int up = Math.max(row - radius, 0);
        int bottom = Math.min(row + radius, nRows-1);
        int left = Math.max(col - radius, 0);
        int right = Math.min(col + radius, nCols-1);

        // Square
        if(isSquare){
            // Get neighbor indices
            for (int r = up; r <= bottom; r++){
                for (int c = left; c <= right; c++){
                    neighbor = r * nCols + c; // converting back to the index in 1D array
                    list.add(neighbor);
                }
            }
        }
        // Circle
        else{
            // Get neighbor indices
            for (int r = up; r <= bottom; r++){
                for (int c = left; c <= right; c++){
                    if (getDistance(row, col, r, c) <= radius){
                        neighbor = r * nCols + c; // converting back to the index in 1D array
                        list.add(neighbor);
                    }
                }
            }
        }

        // Convert list to array
        int[] neighborArray= new int[list.size()];
        int counter = 0;
        for (Integer intObj: list) {
            neighborArray[counter] = intObj;
            counter++;
        }
        return neighborArray;
    }

    private double getMax() {
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < nRows; i++) {
            for (int j = 0; j < nCols; j++) {
                if (values[i*nCols+j] > max) {
                    max = values[i*nCols+j];
                }
            }
        }
        return max;
    }

    private double getMin() {
        double min = Double.POSITIVE_INFINITY;
        for (int i = 0; i < nRows; i++) {
            for (int j = 0; j < nCols; j++) {
                if (values[i*nCols+j] < min) {
                    min = values[i*nCols+j];
                }
            }
        }
        return min;
    }

    private static int findIndex(double[] array, double targetValue) {
        for (int i = 0; i < array.length; i++){
            if (array[i] == targetValue) {
                return i; // Return the index if the value is found
            }
        }
        return -1; // Return -1 if the value is not found in the array
    }

    private static int countUniqueValues(double[] array) {
        // Use a Set to store unique values
        Set<Double> uniqueSet = new HashSet<>();

        // Iterate through the array and add each value to the Set
        for (double value : array){
            // Exclude no data value
            if (value != nullValue)
                uniqueSet.add(value);
        }

        // The size of the Set represents the number of unique values
        return uniqueSet.size();
    }

    private static double getDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x1-x2, 2) + Math.pow(y1-y2, 2));
    }
}