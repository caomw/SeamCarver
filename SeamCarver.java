import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

public class SeamCarver {
	
	final private Picture img;
	private final Double[][] energyMatrix;
	
	public SeamCarver(Picture picture){
	   // create a seam carver object based on the given picture
	   Picture imgTemp = new Picture(picture);
	   
	   //make defensive copy
	   Picture img = copyPic(imgTemp);
	   this.img = img;
	   imgTemp = null;
	   
	   //compute energies
	   this.energyMatrix = fillEnergyMatrix();

   }
   public Picture picture(){
	   // current picture
	   // make defensive copy and return 
	   return copyPic(img);
   }
   // width of current picture
   public int width() {
	   return img.width();
   }
   // height of current picture   
   public int height() {
	   return img.height();
   }
   // energy of pixel at column x and row y
   public double energy(int x, int y){
	   int col = x;
	   int row = y;
	
	   return deltaX(row,col) + deltaY(row,col);
   }
   //squared difference between two pixels in Y direction
   private double deltaY(int row, int col){
	   //outputs the squared difference between two pixels
	   Color a;
	   Color b;
	   
	   //bottom wall
	   if(row == (img.height()-1))  { a = img.get(col, row-1); b = img.get(col, 0);} 
	   //top wall
	   else if(row == 0)  			{ a = img.get(col, img.height()-1); b = img.get(col, row+1); }
	   //between top/bottom
	   else 						{ a = img.get(col, row-1); b = img.get(col, row+1); }
	   
	   double deltaR = Math.pow((a.getRed()   - b.getRed()),  2);
	   double deltaG = Math.pow((a.getGreen() - b.getGreen()),2);
	   double deltaB = Math.pow((a.getBlue()  - b.getBlue()), 2);
	   
	   return deltaR + deltaG + deltaB;   
   }
   //squared difference between two pixels in X direction
   private double deltaX(int row, int col){
	   //outputs the squared difference between two pixels
	   Color a;
	   Color b;
	   
	   //right wall
	   if(col == (img.width()-1))  { a = img.get(col-1, row); b = img.get(0, row);} 
	   //left wall
	   else if(col == 0)  			{ a = img.get(img.width()-1, row); b = img.get(col+1, row); }
	   //between left/right
	   else 						{ a = img.get(col-1, row); b = img.get(col+1, row); }
	   
	   double deltaR = Math.pow((a.getRed()   - b.getRed()),  2);
	   double deltaG = Math.pow((a.getGreen() - b.getGreen()),2);
	   double deltaB = Math.pow((a.getBlue()  - b.getBlue()), 2);
	   
	   return deltaR + deltaG + deltaB;   
   }
   
   private Double[][] fillEnergyMatrix(){
	   //energyMatrix: [ row 0;  <-- row is length Width-1
	   //  				 row 1;
	   //				 row Height-1;
	   
	   Double[][] matrix = new Double[img.height()][img.width()];
	   for(int row = 0; row<img.height(); row++){
		   for (int col = 0; col<img.width(); col++){
			   matrix[row][col] = energy(col,row);
		   }
	   }
	   return matrix;
	   
   }
//   public   int[] findHorizontalSeam()               // sequence of indices for horizontal seam
//   public   int[] findVerticalSeam()                 // sequence of indices for vertical seam
//   public    void removeHorizontalSeam(int[] seam)   // remove horizontal seam from current picture
//   public    void removeVerticalSeam(int[] seam)     // remove vertical seam from current picture
   private Picture copyPic(Picture a){
	   Picture img = new Picture(a.width(), a.height());
	   //make defensive copy
	   for(int i = 0; i<a.width(); i++){
		   for(int j = 0; j<a.height(); j++){
			   img.set(i, j, a.get(i, j));
		   }
	   }
	   return img;
   }
   
   public static void main(String args[]){
	   String[] filename = {"3x4.png"};
	   SeamCarver a = new SeamCarver(new Picture(filename[0]));
	   System.out.println("3x4.png: width (3) = " + a.width() + "hieght (4) = " + a.height());
	   PrintEnergy.main(filename);
	   
	   // [  (255,101,51) , (255,101,153) , (255,101,255);
	   //     ...
	   //     ...						  , (255,255,255)];
	   
// test deltaX deltaY	   
	   	//top left
	// deltaX
	   	System.out.println("DX Top Left, Should be 10404: " + (10404 == a.deltaX(0,0)));
	// deltaY
	   	System.out.println("DY Top Left, Should be 10404: " + (10404 == a.deltaY(0,0)));
	// Energy
	   	System.out.println("EN Top Left, Should be 10404: " + (20808 == a.energy(0,0)));
	   	//top right
	   	System.out.println("DX Top Right, Should be 10404: " + (10404 == a.deltaX(0,a.width()-1)));
	   	System.out.println("DY Top Right, Should be 10404: " + (10404 == a.deltaY(0,a.width()-1)));
	   	System.out.println("EN Top Right, Should be 20808: " + (20808 == a.energy(a.width()-1,0)));
	   	//bottom left
	   	System.out.println("DX Bottom Left, Should be 10404: " + (10404 == a.deltaX(a.height()-1,0)));
	   	System.out.println("DY Bottom Left, Should be 10404: " + (10404 == a.deltaY(a.height()-1,0)));
	   	System.out.println("EN Bottom Left, Should be 20808: " + (20808 == a.energy(0,a.height()-1)));
	   	//bottom right
	   	System.out.println("DX Bottom Right, Should be 10404: " + (10404 == a.deltaX(a.height()-1,a.width()-1)));
	   	System.out.println("DY Bottom Right, Should be 10404: " + (10816 == a.deltaY(a.height()-1,a.width()-1)));
	   	System.out.println("EN Bottom Right, Should be 21220: " + (21220 == a.energy(a.width()-1,a.height()-1)));
	   	//middle	
	   	System.out.println("DX Middle, Should be 10404: " + (41616 == a.deltaX(1,1)));
	   	System.out.println("DY Middle, Should be 10404: " + (10609 == a.deltaY(1,1)));
	   	System.out.println("EN Middle, Should be 52225: " + (52225 == a.energy(1,1)));

	   
	   
   }
}