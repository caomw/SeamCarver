/*************************************************************************
 *  Compilation:  javac PrintSeams.java
 *  Execution:    java PrintSeams input.png
 *  Dependencies: SeamCarver226.java Picture.java StdOut.java
 *
 *  Read image from file specified as command-line argument. Print square
 *  of energies of pixels, a vertical seam, and a horizontal seam.
 *
 *  % java PrintSeams 6x5.png
 *  6x5.png (6-by-5 image)
 *  
 *  The table gives the dual-gradient energies of each pixel.
 *  The asterisks denote a minimum energy vertical or horizontal seam.
 *  
 *  Vertical seam: { 3 4 3 2 2 }
 *   240.18   225.59   302.27   159.43*  181.81   192.99  
 *   124.18   237.35   151.02   234.09   107.89*  159.67  
 *   111.10   138.69   228.10   133.07*  211.51   143.75  
 *   130.67   153.88   174.01*  284.01   194.50   213.53  
 *   179.82   175.49    70.06*  270.80   201.53   191.20  
 *  Total energy = 644.467988
 *  
 *  
 *  Horizontal seam: { 2 2 1 2 1 2 }
 *   240.18   225.59   302.27   159.43   181.81   192.99  
 *   124.18   237.35   151.02*  234.09   107.89*  159.67  
 *   111.10*  138.69*  228.10   133.07*  211.51   143.75* 
 *   130.67   153.88   174.01   284.01   194.50   213.53  
 *   179.82   175.49    70.06   270.80   201.53   191.20  
 *  Total energy = 785.531820
 *
 *************************************************************************/

public class PrintSeams {
    private final static boolean HORIZONTAL   = true;
    private final static boolean VERTICAL     = false;

    private static void printSeam(SeamCarver carver, Integer[] verticalSeam, boolean direction) {
        double totalSeamEnergy = 0.0;

        for (int row = 0; row < carver.height(); row++) {
            for (int col = 0; col < carver.width(); col++) {
                double energy = SeamCarver.energy(col, row,carver.picture());
                String marker = " ";
                if ((direction == HORIZONTAL && row == verticalSeam[col])
                 || (direction == VERTICAL   && col == verticalSeam[row])) {
                    marker = "*";
                    totalSeamEnergy += energy;
                }
                StdOut.printf("%7.2f%s ", energy, marker);
            }
            StdOut.println();
        }                
        // StdOut.println();
        StdOut.printf("Total energy = %f\n", totalSeamEnergy);
        StdOut.println();
        StdOut.println();
    }

    public static void main(String[] args) {
         Picture picture = new Picture(args[0]);
        //Picture picture = new Picture("6x5.png");
        StdOut.printf("%s (%d-by-%d image)\n", picture, picture.width(), picture.height());
        StdOut.println();
        StdOut.println("The table gives the dual-gradient energies of each pixel.");
        StdOut.println("The asterisks denote a minimum energy vertical or horizontal seam.");
        StdOut.println();

        SeamCarver carver = new SeamCarver(picture);
        
        StdOut.printf("Vertical seam: { ");
        Integer[] verticalSeam = carver.findVerticalSeam();
        int[] j = {-1,-1};
        for (int x : verticalSeam){
        	j = SeamCarver.ind2sub(carver.height(),carver.width(),x);
            StdOut.print("(" + j[0] + ", " + j[1] +")" + " ");
        }
        StdOut.println("}");
        printSeam(carver, verticalSeam, VERTICAL);

        StdOut.printf("Horizontal seam: { ");
        Integer[] horizontalSeam = carver.findHorizontalSeam();  //somethings wrong here!!
        for (int y : horizontalSeam){
        	j = SeamCarver.ind2sub(5,6,y);
            StdOut.print("(" + j[0] + ", " + j[1] +")" + " ");
        }
        StdOut.println("}");
        printSeam(carver, horizontalSeam, HORIZONTAL);

    }

}
