import java.awt.Color;


public class SChelper {
	 public static double energy(int x, int y, Picture pic){
			// energy of pixel at column x and row y
			   int col = x;
			   int row = y;
			
			   return SChelper.deltaX(row,col,pic) + SChelper.deltaY(row,col,pic);
		   }
	   public static Picture transpose(Picture old){
		   Picture temp = new Picture(old.height(),old.width());
		   
		   for(int i = 0; i < old.height(); i++){
			   for(int j = 0; j < old.width(); j++){
				   temp.set(i, j, old.get(j, i));
			   }
		   }
		   return temp;
	   }
	   public static double[][] transpose(double [][] m){
	       double[][] temp = new double[m[0].length][m.length];
	       for (int i = 0; i < m.length; i++)
	           for (int j = 0; j < m[0].length; j++)
	               temp[j][i] = m[i][j];
	       return temp;
	   }
	   public static Integer[] tran2origIndex(int width, int height, Integer[] linIndexT){
			  //Maps linear indices in a transposed image to linear indices in non-transposed ("original") image
			   //takes in WIDTH and HEIGHT of ORIGINAL NON-TRANSPOSED IMAGE and the linear indices in the transposed image
			   
			   int[] origInd = new int[linIndexT.length];
			   int[] subInOrig = new int[2];
			   int[] subInTransp = new int[2];

			   for(int i = 0; i < linIndexT.length; i++){
				   //convert back to 2D subscripts in transposed image
				   subInTransp = ind2sub(width, height, linIndexT[i]);
				   	   
				   //swap for subscripts in original
				   subInOrig[0] = subInTransp[1];
				   subInOrig[1] = subInTransp[0];
				   
				   // go from 2D subscripts in orignal image, to linear indices in original
				   int newLinearIndeces = sub2ind(subInOrig[0],subInOrig[1], width);
				   
				   origInd[i] = newLinearIndeces;
//				   System.out.println("Linear Index transposed image: " + linIndexT[i]);
//				   System.out.println("Subscript in transposed image: " + Arrays.deepToString(int2Int(subInTransp)));
//				   System.out.println("Subscript in original image: " + Arrays.deepToString(int2Int(subInOrig)));
//				   System.out.println("Linear Index in original image: " + newLinearIndeces);
			   }
			   return int2Int(origInd);
		   }
	   public static double deltaY(int row, int col, Picture pic){
		   //outputs the squared difference between the pixel above and below pixel at (row,col)
		   //if a pixel is on top/bottom row, we "wrap around the image" and grab pixel on other side
		   Color a;
		   Color b;
		   
		   //bottom wall
		   if(row == (pic.height()-1))  { a = pic.get(col, row-1); b = pic.get(col, 0);} 
		   //top wall
		   else if(row == 0)  			{ a = pic.get(col, pic.height()-1); b = pic.get(col, row+1); }
		   //between top/bottom
		   else 						{ a = pic.get(col, row-1); b = pic.get(col, row+1); }
		   
		   double deltaR = Math.pow((a.getRed()   - b.getRed()),  2);
		   double deltaG = Math.pow((a.getGreen() - b.getGreen()),2);
		   double deltaB = Math.pow((a.getBlue()  - b.getBlue()), 2);
		   
		   return deltaR + deltaG + deltaB;   
	   }
	   public static double deltaX(int row, int col, Picture pic){
		   //outputs the squared difference between the pixel to the left and right of pixel at (row,col)
		   //if a pixel is on left/right column, we "wrap around the image" and grab pixel on other side
		   Color a;
		   Color b;
		   
		   //right wall
		   if(col == (pic.width()-1))  { a = pic.get(col-1, row); b = pic.get(0, row);} 
		   //left wall
		   else if(col == 0)  			{ a = pic.get(pic.width()-1, row); b = pic.get(col+1, row); }
		   //between left/right
		   else 						{ a = pic.get(col-1, row); b = pic.get(col+1, row); }
		   
		   double deltaR = Math.pow((a.getRed()   - b.getRed()),  2);
		   double deltaG = Math.pow((a.getGreen() - b.getGreen()),2);
		   double deltaB = Math.pow((a.getBlue()  - b.getBlue()), 2);
		   
		   return deltaR + deltaG + deltaB;   
	   }
	   public static double[][] fillEnergyMatrix(Picture pic){
		  //Populate energyMatrix with "dual-gradient energy" function
		   
		   //energyMatrix: [ row 0;  <-- row is length Width-1
		   //  				 row 1;
		   //				 row Height-1;
		   
		   double[][] matrix = new double[pic.height()][pic.width()];
		   for(int row = 0; row<pic.height(); row++){
			   for (int col = 0; col<pic.width(); col++){
				   matrix[row][col] = energy(col,row, pic);
			   }
		   }
		   return matrix; 
	   }   
	   public static Picture copyPic(Picture a){
		   Picture img = new Picture(a.width(), a.height());
		   //make defensive copy
		   for(int i = 0; i<a.width(); i++){
			   for(int j = 0; j<a.height(); j++){
				   img.set(i, j, a.get(i, j));
			   }
		   }
		   return img;
	   }
	   public static int minIndex(double[] a){
		   //copy elements of array into new array
		   //find minimum element while keeping track of index and return index
		   double[] copyArr = new double[a.length];
		   for(int i = 0; i < a.length; i++){ copyArr[i] = a[i];}
		   
		   int idx = -1;
		   double d= Double.POSITIVE_INFINITY;
		    for(int i = 0; i < copyArr.length; i++)
		        if(copyArr[i] < d) { d = copyArr[i];  idx = i; }
		    
		    return idx;
	   }  
	   public static int sub2ind(int row, int column, int width){
			return (width*row + column);	
		}
	   public static int[] ind2sub(int height, int width, int LinearIndex){
		   //maps indeces to 2D subscripts
		   int row = LinearIndex/width;
		   int col = LinearIndex - (width*row);
		   int[] a = {row,col};
		   return a;
	   }
	   public static Integer[] int2Int(int[] a){
		   Integer[] b = new Integer[a.length];
		   
		   for(int i = 0; i < a.length; i++){
			   b[i] =  a[i];
		   }
		   return b;
	   }
	   public static void printMatrix(int[][] m){
		   for(int i = 0; i<m.length;i++){
			   System.out.println();
			   for(int j = 0; j<m[i].length; j++){
				   System.out.print(m[i][j] + " ");
			   }
		   }
	   }
	   public static void printMatrix(double[][] m){
		   for(int i = 0; i<m.length;i++){
			   System.out.println();
			   for(int j = 0; j<m[i].length; j++){
				   System.out.print(m[i][j] + " ");
			   }
		   }
	   }
	  

}
