package se.kth.ag2411.mapalgebra;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.File;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashSet;

public class Layer {
    // Attributes
    public String name; // name of this layer
    public int nRows; // number of rows
    public int nCols; // number of columns
    public double[] origin = new double[2]; // x,y-coordinates of lower-left corner
    public double resolution; // cell size
    public double[][] values; // data.
    public double nullValue; // value designated as "No data"

    public Layer(String layerName, String fileName) {

        this.name = layerName;
        String col;
        String rows;
        String llx;
        String lly;
        String cellSize;
        String noDataVal;

        try {
            FileReader fr = new FileReader(fileName);
            BufferedReader bf = new BufferedReader(fr);

            // the row containing the number of the columns
            col = bf.readLine();
            String col_trimmed = col.substring(5).trim(); //fixing the string
            int nCol = Integer.parseInt(col_trimmed); // converting to integer

            // the row containing the number of the row
            rows = bf.readLine();
            String rows_trimmed = rows.substring(5).trim();
            int nRow = Integer.parseInt(rows_trimmed);

            //x coordinate of lower left corner
            llx = bf.readLine();
            String llx_trimmed = llx.substring(10).trim();
            double originX = Double.parseDouble(llx_trimmed);

            //y coordinate of lower left corner
            lly = bf.readLine();
            String lly_trimmed = lly.substring(10).trim();
            double originY = Double.parseDouble(lly_trimmed);

            //resolution
            cellSize = bf.readLine();
            String cellSize_trimmed = cellSize.substring(10).trim();
            double resolution = Double.parseDouble(cellSize_trimmed);

            //noData Values
            noDataVal = bf.readLine(); //
            String noData_trimmed = noDataVal.substring(12).trim();
            double noDataValue = Double.parseDouble(noData_trimmed);

            //Values
            double values[][] = new double[nRow][nCol];

            for (int i = 0; i < nRow; i++) {
                String text = bf.readLine(); // next line is read
                String[] rowArray = text.split(" ");

                for (int j = 0; j < nCol; j++) {
                    values[i][j] = Double.parseDouble(rowArray[j]);
                }
            }

            fr.close();

            //// assigning values
            this.nCols = nCol;
            this.nRows = nRow;
            this.origin[0] = originX;
            this.origin[1] = originY;
            this.resolution = resolution;
            this.nullValue = noDataValue;
            this.values = values;
        } catch (Exception e) {
        }
    }

    // construct a new layer by assigning a value to each of its attributes
    public Layer(String outLayerName, int nRows, int nCols, double[] origin,
                 double resolution, double nullValue) {
        this.name = outLayerName;
        this.nRows = nRows;
        this.nCols = nCols;
        this.origin = origin;
        this.resolution = resolution;
        double[][] values = new double[nRows][nCols];
        this.values = values;
        this.nullValue = nullValue;
    }

    public int getnCols() {
        return nCols;
    }

    public int getnRows() {
        return nRows;
    }

    public void print() {
        System.out.println("ncols " + nCols);
        System.out.println("nrows " + nRows);
        System.out.println("xllcorner " + origin[0]);
        System.out.println("yllcorner " + origin[1]);
        System.out.println("cellsize " + resolution);
        System.out.println("NODATA_value " + nullValue);
        for (int i = 0; i < nRows; i++) {
            for (int j = 0; j < nCols; j++) {
                System.out.print(values[i][j] + " ");
            }
            System.out.println();
        }
    }

    // Save
    public void save(String outputFileName) {
        // save this layer as an ASCII file that can be imported to ArcGIS

        try {
            File file = new File(outputFileName);
            // This object represents ASCII data (to be) stored in the file
            FileWriter fw = new FileWriter(file);

            // Write to the file
            fw.write("ncols     " + nCols + "\n"); // nr of cols
            fw.write("nrows     " + nRows + "\n"); // nr of rows
            fw.write("xllcorner  " + origin[0] + "\n"); //x coordinate
            fw.write("yllcorner   " + origin[0] + "\n"); //y coordinate
            fw.write("cellsize    " + resolution + "\n");
            fw.write("NODATA_value " + nullValue + "\n");
            for (int i = 0; i < nRows; i++) { //Values
                for (int j = 0; j < nCols; j++) {
                    fw.write(values[i][j] + " ");
                }
                fw.write("\n");
            }
            fw.close();
        } catch (Exception e) {
        }
    }

    // This method creates a raster of the layer
    public BufferedImage toImage() {
        BufferedImage image = new BufferedImage(nCols, nRows, BufferedImage.TYPE_INT_RGB);
        WritableRaster raster = image.getRaster();

        //normalizing the color scheme
        double min = getMin();
        double max = getMax();

        //iterating through the grid to assign colors
        for (int i = 0; i < nRows; i++) {
            for (int j = 0; j < nCols; j++) {
                int[] color = new int[3];
                //Normalize so contrast is maximized.
                double normalizedColor;
                if (max - min == 0) { // if all values are the same (cannot divide by zero)
                    normalizedColor = 0;
                } else {
                    normalizedColor = (max - values[i][j]) * 255 / (max - min);
                }

                color[0] = (int) normalizedColor; // Red
                color[1] = (int) normalizedColor; // Green
                color[2] = (int) normalizedColor; // Blue
                raster.setPixel(j, i, color);
            }
        }
        return image;
    }

    // This method creates a raster with certain values highlighted
    public BufferedImage toImage(double[] highlightValues) {
        BufferedImage image = new BufferedImage(nCols, nRows, BufferedImage.TYPE_INT_RGB); //create buffered image
        WritableRaster raster = image.getRaster(); //create raster

        int[][] highlightColor = new int[highlightValues.length][3]; //this array contains RGB values for each value that should be highlighted

        // adding RGB palette for each value that should be highlighted
        for (int k = 0; k < highlightValues.length; k++) {
            highlightColor[k][0] = (int) Math.round(Math.random() * 255); // Red
            highlightColor[k][1] = (int) Math.round(Math.random() * 255);// Green
            highlightColor[k][2] = (int) Math.round(Math.random() * 255); // Blue
        }

        //iterating through the grid to assign colors
        for (int i = 0; i < nRows; i++) {
            for (int j = 0; j < nCols; j++) {
                for (int k = 0; k < highlightValues.length; k++) {
                    if (values[i][j] == highlightValues[k]) {
                        raster.setPixel(j, i, highlightColor[k]); //set color
                        break;
                    }
                }
            }
        }
        return image;
    }

    private double getMax() {
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < nRows; i++) {
            for (int j = 0; j < nCols; j++) {
                if (values[i][j] > max) {
                    max = values[i][j];
                }
            }
        }
        return max;
    }

    private double getMin() {
        double min = Double.POSITIVE_INFINITY;
        for (int i = 0; i < nRows; i++) {
            for (int j = 0; j < nCols; j++) {
                if (values[i][j] < min) {
                    min = values[i][j];
                }
            }
        }
        return min;
    }

    public Layer localSum(Layer inLayer, String outLayerName) {
        Layer outLayer = new Layer(outLayerName, nRows, nCols, origin,
                resolution, nullValue);
        for (int i = 0; i < (nRows); i++) {
            for (int j = 0; j < (nCols); j++) {
                outLayer.values[i][j] = values[i][j] + inLayer.values[i][j]; //summarizing the two layers.
            }
        }
        return outLayer;
    }

    public Layer focalVariety(int r, boolean IsSquare, String outLayerName) {

        Layer outLayer = new Layer(outLayerName, nRows, nCols, origin,
                resolution, nullValue);
        for (int i = 0; i < (nRows); i++) {
            for (int j = 0; j < (nCols); j++) {

                ArrayList<Index> list = this.getNeighborhood(i, j, r, IsSquare); //get neighborhood
                HashSet<Double> hashSet = new HashSet<Double>(); //New Hashset
                int counter = 0; //restore counter to zero for each cell.
                //for (int k = 0; k < list.size(); k++) {
                for (Index index: list) {
                    //Index index = list.get(k);
                    int row = index.getRow();
                    int col = index.getCol();

                    if (!hashSet.contains(values[row][col])) {
                        hashSet.add(values[row][col]);
                        counter += 1;
                    }
                }
                outLayer.values[i][j] = counter;
            }
        }
        return outLayer;
    }

    //Returns the minimum value of each zone.
    public Layer zonalMinimum(Layer zoneLayer, String outLayerName) {
        HashMap<Double, Double> hashMap = new HashMap<Double, Double>(); //Key: zone value, Value: minimum
        Layer outLayer = new Layer(outLayerName, nRows, nCols, origin,
                resolution, nullValue);

        // finding minimum values
        for (int i = 0; i < (nRows); i++) {
            for (int j = 0; j < (nCols); j++) {
                if (!hashMap.containsKey(zoneLayer.values[i][j]) || this.values[i][j] < hashMap.get(zoneLayer.values[i][j])) {//if it does not exist or is the lowest value so far
                    hashMap.put(zoneLayer.values[i][j], this.values[i][j]);  //put in hashmap
                }
            }
        }
        // setting all cells to their respective zones minimum values.
        for (int i = 0; i < (nRows); i++) {
            for (int j = 0; j < (nCols); j++) {
                outLayer.values[i][j] = hashMap.get(zoneLayer.values[i][j]);
            }
        }
        return outLayer;
    }

    //returns a list of indexes in the neighborhood.
    public ArrayList getNeighborhood(int row, int col, int r, boolean isSquare) {
        ArrayList<Index> l = new ArrayList<Index>(); // A list with Indexes.

        //deriving the neighborhood range.
        int rowStart = row - r / 2;
        int rowEnd = row + r / 2;
        int colStart = col - r / 2;
        int colEnd = col + r / 2;

        if (isSquare) {
            //checking if neighbors exist and add them to list
            for (int i = rowStart; i <= rowEnd; i++) { //go through rows
                if (i >= 0 && i < this.nRows) { //avoid invalid indices
                    for (int j = colStart; j <= colEnd; j++) { //go through cols
                        if (j >= 0 && j < this.nCols) { //avoid invalid indices
                            Index neighbor = new Index(i, j); //create instance
                            l.add(neighbor); //add to list
                        }
                    }
                }
            }
        } else { //Neighborhood is a circle
            //checking if neighbors exist and add them to list
            double radius = r / 2 * resolution; //radius
            for (int i = rowStart; i <= rowEnd; i++) { //go through rows
                if (i >= 0 && i < this.nRows) { //avoid invalid indices

                    for (int j = colStart; j <= colEnd; j++) { //go through cols
                        //check if it is inside circle
                        boolean inCircle = false;
                        int rowDist = row - i; //horizontal distance from circle
                        int colDist = col - j; //vertical distance from circle
                        double distance = Math.sqrt(rowDist * resolution * rowDist * resolution + colDist * resolution * colDist * resolution); //pythagoras. maybe fix

                        if (distance <= radius) {
                            inCircle = true;
                        }

                        if (j >= 0 && j < this.nCols && inCircle) { //avoid invalid indices
                            Index neighbor = new Index(i, j); //if valid, create instance
                            l.add(neighbor); //add to list
                        }
                    }
                }
            }
        }
        return l; //return the list
    }
}
