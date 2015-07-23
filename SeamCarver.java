import java.awt.Color;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class SeamCarver {
	
	private Picture img;
	
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
   public int width() {
	// width of current picture
	   return img.width();
   }
   public int height() {
	// height of current picture   
	   return img.height();
   }
   public static double energy(int x, int y, Picture pic){
	// energy of pixel at column x and row y
	   int col = x;
	   int row = y;
	
	   return deltaX(row,col,pic) + deltaY(row,col,pic);
   }
   public Integer[] findHorizontalSeam(){
	// sequence of indices for horizontal seam
	   
	   //transpose current img  => height() and width() reference class scoped variable "img" so 
	   	//these will change as well
	   Picture oldImg = copyPic(this.img);
	   this.img = transpose(this.img);

	   //A Vertical seam in the transposed image is a Horizontal Seam in the original image
	   Integer[] horizSeamT = findVerticalSeam();
	   
	   //map returned indices from transposed image to non-transposed image
	   Integer[] horizSeam = tran2origIndex(oldImg.width(), oldImg.height(), horizSeamT);
	   
	   //transpose img back  ==> garbage collection should take care of memory from transpose image
	   img = oldImg;
	   
	   return horizSeam;
   }
   public Integer[] findVerticalSeam(){
	// sequence of indices for vertical seam
	   
	   //distTo is a 2D matrix of cumulative distances to reach that node
	   //edgeTo is a 2D matrix of linear indices (using sub2ind and ind2sub to map between 2D and 1D)
	   		//each entry in edgeTo[row][col] is the (linear) index of the last node in the path to reach it
	   		//following (chasing) the parent pointers gives the lowest cost path to reach each node from top
	   double[][] distTo = new double[this.height()][this.width()];
	   int[][]    edgeTo = new int[this.height()][this.width()];
	   double[][] energyMatrix = fillEnergyMatrix(this.img);
	   
	   //initialize all distances to INFINITY, edge trace to -1	
	   for(int col = 0; col < this.width(); col++){
		   for(int row = 0; row < this.height(); row++){
			distTo[row][col] = Double.POSITIVE_INFINITY;
			edgeTo[row][col] = -1;
		   }
	   }
	   //initialize first row of distTo to their energy
	   for(int col = 0; col < this.width(); col++){ distTo[0][col] = energyMatrix[0][col]; }
	   
	   //loop through in topological order
	   		//the directed edges only go down, thus looping from top to bottom guarantees a topological ordering
	   		//start at top: left  --> right, increment row, stop BEFORE last row (this.height()-1)
	   for(int row = 0; row < (this.height()-1); row++){
		   for(int col = 0; col < (this.width()); col++){
			 relax(row,col,distTo,edgeTo,energyMatrix);
			 //printMatrix(distTo);
			 //System.out.println();
		   }
	   }
	   
	   //find min distTo in last row
	   int rowIndex = (this.height()-1);
	   int colIndex = minIndex(distTo[this.height()-1]);

	   return verticalSeam(rowIndex,colIndex, edgeTo);	   
   }
   public void removeHorizontalSeam(Integer[] seam){
	// remove horizontal seam from current picture
	  //leverage vertical's remove seam method by transposing
	   this.img = transpose(this.img);
	   this.img = removeVerticalSeam(seam,this.img);
	   
	   //transpose back to have the original image minus a horizontal seam
	   this.img = transpose(this.img);
   }
   public void removeVerticalSeam(Integer [] seam){
	  // remove vertical seam from current picture
	   	this.img = removeVerticalSeam(seam,this.img);
   }
   
   private Picture removeVerticalSeam(Integer[] seam, Picture old){
	   if (seam.length != img.height()) throw new IllegalArgumentException("seam of pixels does not match the height of this image");
	   //create new picture with one column fewer
	   Picture newPic = new Picture((old.width()-1),old.height());
	   //holder for the 2D index
	   int[] index = new int[2];
	   int columnOfPixel2remove;
	   for(int row = 0; row < newPic.height(); row++){
		   //find which column (pixel) in this row should be removed => store in columnOfPixel2remove
		   index = ind2sub(old.height(),old.width(),seam[row]);
		   columnOfPixel2remove = index[1];
		   int oldCol = 0;
		   //populate new smaller picture with same pixels as old picture, only skipping over pixels to be removed (seam)
		   for(int col = 0; col < newPic.width(); col++){
			   //check if this is the column that we will not include => if it is, then increment the index for the oldPicture an
			   	//extra time thus skipping it before a "set" operation can be called
			   if(columnOfPixel2remove == col) oldCol++;
			   newPic.set(col, row, old.get(oldCol, row));
			   oldCol++;
		   }
	   }
	   return newPic;
   }
   private Integer[] verticalSeam(int bottomRow, int bottomCol, int[][] edgeTo){
	   //returns an array of linear indices from top --> bottom to be removed

	   LinkedList<Integer> seam = new LinkedList<Integer>();
	   //chase parent pointer starting from bottom: List will end up with top at beggining bottom at end
	   int node = sub2ind(bottomRow,bottomCol,this.width());
	   
	   while(node>=0){
		   //add found node to list
		   seam.add(node);
		   //convert linear index to index for 2D array edgeTo
		   int[] indices = ind2sub(height(),width(),node);
		   //index edgeTo with found indeces for next node in chain
		   node = edgeTo[indices[0]][indices[1]];
	   }
	   
	   //need to return an array: copy Integers in list into array
	   Integer[] seamArr = new Integer[seam.size()];
	   int iter = seam.size()-1;
	   for(Integer i: seam){ seamArr[iter] = i; iter--; }
	   
	   return seamArr;
	   
   }  
  
   private void relax(int row, int col, double[][] distTo, int[][] edgeTo, double[][] energyMatrix){
		 //this function "relaxes" a vertex: it checks whether it can offer a shorter distance to any node it can reach
		   //by adding its energy to the energy of its connected node and checking whether it is better than the best the
		   //connected node has seen thus far (its current value).  If it is, set current value to this new distance.
		   
		   //2 types of nodes this function will handle, either left of right
		   	//Guaranteed to not reach bottom row - thus will always have at least
		   	//node directly below to check and either one to bottom left/right or both
		    //ex) [ 5 6 7 8;
		    //      8 9 _ _;
		   	//      _ _ _ _;
		  
		   int currentIndex = sub2ind(row,col,this.width());
		   
		   //will always check node below
		   double bottom = distTo[row][col] + energyMatrix[row+1][col];   
		   if (bottom<distTo[row+1][col]) {
			   distTo[row+1][col] = bottom; 
			   edgeTo[row+1][col] = currentIndex;
		   }
		   
		   if(col==0){
			   //left side - only check bottom right, not below left 
			   // ex) if we are inspecting (0,0) -> 5 above, we have no bottom left node to check
			   double bottomRight = distTo[row][col] + energyMatrix[row+1][col+1];
			   if (bottomRight<distTo[row+1][col+1]) {
				   distTo[row+1][col+1] = bottomRight;
				   edgeTo[row+1][col+1] = currentIndex;
			   }
			   return;
		   }
		   else if(col==(this.width()-1)){
			   //right side - only check bottom left, not below right 
			   // ex) if we are inspecting (0,0) -> 8 above, we have no bottom right node to check
			   double bottomLeft = distTo[row][col] + energyMatrix[row+1][col-1];
			   if (bottomLeft<distTo[row+1][col-1]) {
				   distTo[row+1][col-1] = bottomLeft;
				   edgeTo[row+1][col-1] = currentIndex;
			   }
			   return;
		   }
		   else{
			   double bottomRight = distTo[row][col] + energyMatrix[row+1][col+1];
			   double bottomLeft = distTo[row][col] + energyMatrix[row+1][col-1];
			   if (bottomRight<distTo[row+1][col+1]) {
				   distTo[row+1][col+1] = bottomRight;
				   edgeTo[row+1][col+1] = currentIndex;
			   }
			   if (bottomLeft<distTo[row+1][col-1]) {
				   distTo[row+1][col-1] = bottomLeft;
				   edgeTo[row+1][col-1] = currentIndex;
			   }
			   return;
		   }
		   
	   }
   private static Picture transpose(Picture old){
	   Picture temp = new Picture(old.height(),old.width());
	   
	   for(int i = 0; i < old.height(); i++){
		   for(int j = 0; j < old.width(); j++){
			   temp.set(i, j, old.get(j, i));
		   }
	   }
	   return temp;
   }
   private static double[][] transpose(double [][] m){
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
//			   System.out.println("Linear Index transposed image: " + linIndexT[i]);
//			   System.out.println("Subscript in transposed image: " + Arrays.deepToString(int2Int(subInTransp)));
//			   System.out.println("Subscript in original image: " + Arrays.deepToString(int2Int(subInOrig)));
//			   System.out.println("Linear Index in original image: " + newLinearIndeces);
		   }
		   return int2Int(origInd);
	   }
   
   private static double deltaY(int row, int col, Picture pic){
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
   private static double deltaX(int row, int col, Picture pic){
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
   private static double[][] fillEnergyMatrix(Picture pic){
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
   private static Picture copyPic(Picture a){
	   Picture img = new Picture(a.width(), a.height());
	   //make defensive copy
	   for(int i = 0; i<a.width(); i++){
		   for(int j = 0; j<a.height(); j++){
			   img.set(i, j, a.get(i, j));
		   }
	   }
	   return img;
   }
   private static int minIndex(double[] a){
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
   public static void main(String args[]){

	   String[] filename = {"3x4.png"};
	   SeamCarver a = new SeamCarver(new Picture(filename[0]));
	   //System.out.println("3x4.png: width (3) = " + a.width() + ", hieght (4) = " + a.height() + "\n");
	   
	   String[] filename1 = {"6x5.png"};
	   SeamCarver f = new SeamCarver(new Picture(filename1[0]));
	   //System.out.println("6x5.png: width (6) = " + f.width() + ", hieght (5) = " + f.height());
	   //PrintEnergy.main(filename1);
	   
	   
	   //Relax
	   //System.out.println("test Relax");
	   double inf = Double.POSITIVE_INFINITY;
	   int[][] sampleImg = { {1,2,3}, {2,2,2},{1,2,2}};
	   double[][] sampleEnergyMatrix = { {8,8,3}, {0,0,5}, {3,3,8} };
	   double[][] sampledistTo = {{8,8,3}, {inf, inf, inf}, {inf, inf, inf}};
	   int[][] sampleedgeTo = {{-1,-1,-1}, {0, 0, 0}, {0, 0, 0}};
	   //relax(0,0,sampledistTo, sampleedgeTo, sampleEnergyMatrix);

	 //test findVerticalSeam
	 		System.out.println("    Test findVerticalSeam 6x5");
	 		PrintSeams.main(new String[] {"6x5.png"});
	 		Integer[] s = f.findVerticalSeam();
	 		Integer[] correctSeam = {sub2ind(0,3,6), sub2ind(1,4,6), sub2ind(2,3,6), sub2ind(3,2,6), sub2ind(4,2,6)};
	 		System.out.println(Arrays.deepEquals(s, correctSeam));

	 		System.out.println("    Test findVerticalSeam 3x4");
	 		PrintSeams.main(new String[] {"3x4.png"});
	 		Integer[] s1 = a.findVerticalSeam();
	 		Integer[] correctSea1 = {sub2ind(3,0,3), sub2ind(2,0,3), sub2ind(1,0,3), sub2ind(0,0,3)};
	 		Integer[] correctSeam1 = {sub2ind(0,0,3), sub2ind(1,0,3), sub2ind(2,0,3), sub2ind(3,0,3)};
	 		System.out.println(Arrays.deepEquals(s1, correctSeam1)); 	
	 
	//test removeVerticalSeam
	 		System.out.println("    Test removeVerticalSeam 6x5");
	 		f.removeVerticalSeam(s);
	 		
	 		
		

	 //test sub2ind
	 	   System.out.println("    Test sub2ind  : for a Height x Width: 3x3 matrix");
	 	   int width = 3;
	 	   System.out.println(0 == sub2ind(0,0,width));
	 	   System.out.println(5 == sub2ind(1,2,width));
	 	   System.out.println(8 == sub2ind(2,2,width));
	 	   System.out.println(0 == sub2ind(0,0,width));
	 	   System.out.println(5 == sub2ind(1,2,width));
	 	   System.out.println("    Test ind2sub: for a Height x Width: 3x4 matrix");
	 	   int height = 3;
	 	   width = 4;
	 	   int[] j = new int[2];
	 	   j[0] = 0; j[1] = 0;
	 	   int[] b = ind2sub(height,width,0);
	 	   System.out.println((j[0] == b[0]) && (j[1] == b[1]));
	 	   
	 	   j[0] = 1; j[1] = 1;
	 	   b = ind2sub(height,width,5);
	 	   System.out.println((j[0] == b[0]) && (j[1] == b[1]));
	 	   
	 	   j[0] = 2; j[1] = 0;
	 	   b = ind2sub(height,width,8);
	 	   System.out.println((j[0] == b[0]) && (j[1] == b[1]));
	 	   
	 	   //minIndex
	 	   System.out.println("    Test minIndex");
	 	   double[] m = {0.5, 0.65, 1.5, 0.03};
	 	   System.out.println(3 == minIndex(m));
	 	   m[0] = 0.0;
	 	   
	 	   
	 	   System.out.println(0 == minIndex(m));
	   
	 	  System.out.println("    Test deltaX/Y on 3x4.png");
	// test deltaX deltaY	   
	 		   	//top left
	 		// deltaX
	 		   	System.out.println("DX Top Left, Should be 10404: " + (10404 == deltaX(0,0,a.picture())));
	 		// deltaY
	 		   	System.out.println("DY Top Left, Should be 10404: " + (10404 == deltaY(0,0,a.picture())));
	 		// Energy
	 		   	System.out.println("EN Top Left, Should be 10404: " + (20808 == energy(0,0,a.picture()) ));
	 		   	//top right
	 		   	System.out.println("DX Top Right, Should be 10404: " + (10404 == deltaX(0,(a.width()-1),a.picture()) ) );
	 		   	System.out.println("DY Top Right, Should be 10404: " + (10404 == deltaY(0,(a.width()-1),a.picture()) ) );
	 		   	System.out.println("EN Top Right, Should be 20808: " + (20808 == energy((a.width()-1),0,a.picture()) ) );
	 		   	//bottom left
	 		   	System.out.println("DX Bottom Left, Should be 10404: " + (10404 == deltaX(a.height()-1,0,a.picture())));
	 		   	System.out.println("DY Bottom Left, Should be 10404: " + (10404 == deltaY(a.height()-1,0,a.picture())));
	 		   	System.out.println("EN Bottom Left, Should be 20808: " + (20808 == energy(0,a.height()-1,a.picture())));
	 		   	//bottom right
	 		   	System.out.println("DX Bottom Right, Should be 10404: " + (10404 == deltaX(a.height()-1,a.width()-1,a.picture())));
	 		   	System.out.println("DY Bottom Right, Should be 10404: " + (10816 == deltaY(a.height()-1,a.width()-1,a.picture())));
	 		   	System.out.println("EN Bottom Right, Should be 21220: " + (21220 == energy(a.width()-1,a.height()-1,a.picture())));
	 		   	//middle	
	 		   	System.out.println("DX Middle, Should be 10404: " + (41616 == deltaX(1,1,a.picture())));
	 		   	System.out.println("DY Middle, Should be 10404: " + (10609 == deltaY(1,1,a.picture())));
	 		   	System.out.println("EN Middle, Should be 52225: " + (52225 == energy(1,1,a.picture())));	 
	 		   	
	 // Test TransIndex2RegIndex
	 	System.out.println("    Test TransIndex2RegIndex: 3x3 matrix");	 
	 	
	 	Integer[] regIndices   = { sub2ind(0,0,3), sub2ind(0,1,3), sub2ind(0,2,3), sub2ind(0,3,3) };
	 	Integer[] transIndices = { sub2ind(0,0,3), sub2ind(1,0,3), sub2ind(2,0,3), sub2ind(3,0,3) };
	 	System.out.println("regIndices:" + Arrays.toString(regIndices));
	 	System.out.println("transIndices:" + Arrays.toString(transIndices));
	 	Integer[] fromMethod = tran2origIndex(3,3,regIndices);
	 	System.out.println("transIndices from method:" + Arrays.toString(fromMethod));
	 	System.out.println(Arrays.deepEquals(tran2origIndex(3,3,transIndices), transIndices) );

	 // Test Transpose
	 	Picture p = new Picture(2,2);
	 	Color[][] c = new Color[2][2];
	 	Color col = Color.GRAY;
	 	for(int i = 0; i<2; i++){
	 		for(int h1 = 0; h1<2;h1++){
	 			if(i == 0) col = Color.RED;
	 			if(i == 1) col = Color.WHITE;
	 			if(i == 2) col = Color.BLUE;
	 			p.set(h1, i, col);
	 			c[i][h1] = col;
	 		}
	  	}
	 	System.out.println(Arrays.deepToString(c));
	 	Color[][] cT = new Color[2][2];
	 	Picture pT = transpose(p);
	 	for(int i = 0; i<2; i++){
	 		for(int h1 = 0; h1<2; h1++){
	 			cT[i][h1] = pT.get(h1, i);
	 		}
	  	}
	 	
	 	System.out.println(Arrays.deepToString(cT));
	
	 	
   }
   
}