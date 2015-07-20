import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

public class SeamCarver {
	
	Picture img;
	Double[][] energyMatrix;
	
	public SeamCarver(Picture picture){
	   // create a seam carver object based on the given picture
	   Picture imgTemp = new Picture(picture);
	   
	   //make defensive copy
	   Picture img = copyPic(imgTemp);
	   this.img = img;
	   imgTemp = null;
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
   private double deltaY1(int col, int row1, int row2){
	   Color a = img.get(col, row1);
	   Color b = img.get(col, row2);
	   double deltaR = Math.pow((a.getRed()   - b.getRed()),  2);
	   double deltaG = Math.pow((a.getGreen() - b.getGreen()),2);
	   double deltaB = Math.pow((a.getBlue()  - b.getBlue()), 2);
	   
	   return deltaR + deltaG + deltaB;   
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
   
   public Set<gridLocate> locate(int x, int y){
	   int col = x;
	   int row = y;
	   
	   Set<gridLocate> where = new HashSet<gridLocate>();
	   
	   //check left wall
	   if(col == 0){
		   where.add(gridLocate.left);
		   if(row == 0){
			   //top left
			   where.add(gridLocate.top);
			   System.out.println("Top Left");
			   return where;
		   }
		   else if(row == (img.height()-1)){
			   //bottom left
			   where.add(gridLocate.bottom);
			   System.out.println("Bottom Left");
			   return where;
		   }
		   else{
			   //left wall
			   System.out.println("Left Wall");
			   return where;
		   }
	   }
		   
		 //check right wall
		if(col == (img.width()-1)){
			 where.add(gridLocate.right);
			if(row == 0){
   	        //top right
		    where.add(gridLocate.top);
			System.out.println("Top Right");
			return where;
			}
			else if(row == (img.height()-1)){
		    //bottom right
			where.add(gridLocate.bottom);
		    System.out.println("Bottom Right");
		    return where;
			}
			else{
		     //right wall
		    System.out.println("Right Wall");
		    return where;
			} 
		}
			   
		//check top wall
	    if(row == 0){
	    	//top middle
	    	where.add(gridLocate.top);
	    	System.out.println("Top Middle");
	    	return where;
	    }
	    
	    //check bottom wall
	    if(row == (img.height()-1)){
	    	//bottom middle
	    	where.add(gridLocate.bottom);
	    	System.out.println("Bottom Middle");
	    	return where;
	    }
	    
	    else{
	    	//in middle
	    	System.out.println("Middle");
	    	return where;
	    }
	    

	   
	   
   }
   
   private enum gridLocate{
	   top, bottom, left, right;
   }
   private void fillEnergyMatrix(){
	   //energyMatrix: [ row 0;  <-- row is length Width-1
	   //  				 row 1;
	   //				 row Height-1;
	   energyMatrix = new Double[img.height()][img.width()];
	   
	   for(int row = 0; row<img.height(); row++){
		   for (int col = 0; col<img.width(); col++){
			   //energyMatrix[row][col] = this.energy(col,row);
		   }
	   }
	   
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
	   //PrintEnergy.main(filename);
	   
	   // [  (255,101,51) , (255,101,153) , (255,101,255);
	   //     ...
	   //     ...						  , (255,255,255)];
	   
	   // deltaX
	   	//top left
	   System.out.print("Top Left, Should be 10404: " (10404 == a.deltaX(0, col1, col2)) );
	   	//top right
	   	//bottom left
	   	//bottom right
	   	//middle
	   	// deltaY
	   int row = 0;
	   int col = 0;
	   System.out.print("[" + row + ", "+ col + "]: Should be: Top Left =?  " );a.energy(row, col);
	   
	   
   }
}