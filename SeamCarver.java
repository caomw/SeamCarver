import java.util.Arrays;
import java.util.LinkedList;

public class SeamCarver {
	
	private Picture img;
	
	public SeamCarver(Picture picture){
	   // create a seam carver object based on the given picture
	   Picture imgTemp = new Picture(picture);
	   
	   //make defensive copy
	   Picture img = SChelper.copyPic(imgTemp);
	   this.img = img;
	   imgTemp = null;

   }
   public Picture picture(){
	   // current picture
	   // make defensive copy and return 
	   return SChelper.copyPic(img);
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
	
	   return SChelper.deltaX(row,col,pic) + SChelper.deltaY(row,col,pic);
   }
   public Integer[] findHorizontalSeam(){
	// sequence of indices for horizontal seam
	   
	   //transpose current img  => height() and width() reference class scoped variable "img" so 
	   	//these will change as well
	   Picture oldImg = SChelper.copyPic(this.img);
	   this.img = SChelper.transpose(this.img);

	   //A Vertical seam in the transposed image is a Horizontal Seam in the original image
	   Integer[] horizSeamT = findVerticalSeam();
	   
	   //map returned indices from transposed image to non-transposed image
	   Integer[] horizSeam = SChelper.tran2origIndex(oldImg.width(), oldImg.height(), horizSeamT);
	   
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
	   double[][] energyMatrix = SChelper.fillEnergyMatrix(this.img);
	   
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
	   int colIndex = SChelper.minIndex(distTo[this.height()-1]);

	   return verticalSeam(rowIndex,colIndex, edgeTo);	   
   }
   public void removeHorizontalSeam(Integer[] seam){
	// remove horizontal seam from current picture
	  //leverage vertical's remove seam method by transposing
	   this.img = SChelper.transpose(this.img);
	   this.img = removeVerticalSeam(seam,this.img);
	   
	   //transpose back to have the original image minus a horizontal seam
	   this.img = SChelper.transpose(this.img);
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
		   index = SChelper.ind2sub(old.height(),old.width(),seam[row]);
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
	   int node = SChelper.sub2ind(bottomRow,bottomCol,this.width());
	   
	   while(node>=0){
		   //add found node to list
		   seam.add(node);
		   //convert linear index to index for 2D array edgeTo
		   int[] indices = SChelper.ind2sub(height(),width(),node);
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
		  
		   int currentIndex = SChelper.sub2ind(row,col,this.width());
		   
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

   public static void main(String args[]){
	   String[] filename = {"3x4.png"};
	   SeamCarver a = new SeamCarver(new Picture(filename[0]));
	   
	   String[] filename1 = {"6x5.png"};
	   SeamCarver f = new SeamCarver(new Picture(filename1[0]));
	   //PrintEnergy.main(filename1);

	 //test findVerticalSeam
	 		System.out.println("Test findVerticalSeam 6x5");
	 		//PrintSeams.main(new String[] {"6x5.png"});
	 		Integer[] v = f.findVerticalSeam();
	 		Integer[] h = f.findHorizontalSeam();
	 		Integer[] vSeam = {SChelper.sub2ind(0,3,6), SChelper.sub2ind(1,4,6), SChelper.sub2ind(2,3,6), SChelper.sub2ind(3,2,6), SChelper.sub2ind(4,2,6)};
	 		Integer[] hSeam = {SChelper.sub2ind(2,0,6), SChelper.sub2ind(2,1,6), SChelper.sub2ind(1,2,6), SChelper.sub2ind(2,3,6), SChelper.sub2ind(1,4,6), SChelper.sub2ind(2,5,6)};
	 		System.out.println("    Correct Vertical Seam on 6x5:    " + Arrays.deepEquals(v, vSeam));
	 		System.out.println("    Correct Horizontal Seam on 6x5:  " + Arrays.deepEquals(h, hSeam));

	 		System.out.println("\nTest findVerticalSeam 3x4");
	 		//PrintSeams.main(new String[] {"3x4.png"});
	 		Integer[] v1 = a.findVerticalSeam();
	 		Integer[] h1 = a.findHorizontalSeam();
	 		Integer[] vSeam1 = {SChelper.sub2ind(0,0,3), SChelper.sub2ind(1,0,3), SChelper.sub2ind(2,0,3), SChelper.sub2ind(3,0,3)};
	 		Integer[] hSeam1 = {SChelper.sub2ind(0,0,3), SChelper.sub2ind(0,1,3), SChelper.sub2ind(0,2,3)};
	 		System.out.println("    Correct Vertical Seam on 3x4:    " + Arrays.deepEquals(v1, vSeam1)); 	
	 		System.out.println("    Correct Horizont Seam on 3x4:    " + Arrays.deepEquals(h1, hSeam1));
	 
	//test removeVerticalSeam
	 		System.out.println("\nTest removeVerticalSeam 6x5");
	 		int widthBeforeRemove = f.width();
	 		f.removeVerticalSeam(v);
	 		int widthAfterRemove = f.width();
	 		System.out.println("    Width decr after vertical remove: " + (widthAfterRemove == (widthBeforeRemove-1)) );
	 		
	 		
		

	 //test sub2ind
	 	   System.out.println("\nTest sub2ind  : for a Height x Width: 3x3 matrix");
	 	   int width = 3;
	 	   System.out.println(0 == SChelper.sub2ind(0,0,width));
	 	   System.out.println(5 == SChelper.sub2ind(1,2,width));
	 	   System.out.println(8 == SChelper.sub2ind(2,2,width));
	 	   System.out.println(0 == SChelper.sub2ind(0,0,width));
	 	   System.out.println(5 == SChelper.sub2ind(1,2,width));
	 	   System.out.println("\nTest ind2sub: for a Height x Width: 3x4 matrix");
	 	   int height = 3;
	 	   width = 4;
	 	   int[] j = new int[2];
	 	   j[0] = 0; j[1] = 0;
	 	   int[] b = SChelper.ind2sub(height,width,0);
	 	   System.out.println((j[0] == b[0]) && (j[1] == b[1]));
	 	   
	 	   j[0] = 1; j[1] = 1;
	 	   b = SChelper.ind2sub(height,width,5);
	 	   System.out.println((j[0] == b[0]) && (j[1] == b[1]));
	 	   
	 	   j[0] = 2; j[1] = 0;
	 	   b = SChelper.ind2sub(height,width,8);
	 	   System.out.println((j[0] == b[0]) && (j[1] == b[1]));
	 	   
	 	   //minIndex
	 	   System.out.println("\nTest minIndex");
	 	   double[] m = {0.5, 0.65, 1.5, 0.03};
	 	   System.out.println(3 == SChelper.minIndex(m));
	 	   m[0] = 0.0;
	 	   
	 	   
	 	   System.out.println(0 == SChelper.minIndex(m));
	   
	 	  System.out.println("\nTest deltaX/Y on 3x4.png");
	// test deltaX deltaY	   
	 		   	//top left
	 		// deltaX
	 		   	System.out.println("DX Top Left, Should be 10404:     " + (10404 == SChelper.deltaX(0,0,a.picture())));
	 		// deltaY
	 		   	System.out.println("DY Top Left, Should be 10404:     " + (10404 == SChelper.deltaY(0,0,a.picture())));
	 		// Energy
	 		   	System.out.println("EN Top Left, Should be 10404:     " + (20808 == energy(0,0,a.picture()) ));
	 		   	//top right
	 		   	System.out.println("DX Top Right, Should be 10404:    " + (10404 == SChelper.deltaX(0,(a.width()-1),a.picture()) ) );
	 		   	System.out.println("DY Top Right, Should be 10404:    " + (10404 == SChelper.deltaY(0,(a.width()-1),a.picture()) ) );
	 		   	System.out.println("EN Top Right, Should be 20808:    " + (20808 == SChelper.energy((a.width()-1),0,a.picture()) ) );
	 		   	//bottom left
	 		   	System.out.println("DX Bottom Left, Should be 10404:  " + (10404 == SChelper.deltaX(a.height()-1,0,a.picture())));
	 		   	System.out.println("DY Bottom Left, Should be 10404:  " + (10404 == SChelper.deltaY(a.height()-1,0,a.picture())));
	 		   	System.out.println("EN Bottom Left, Should be 20808:  " + (20808 == energy(0,a.height()-1,a.picture())));
	 		   	//bottom right
	 		   	System.out.println("DX Bottom Right, Should be 10404: " + (10404 == SChelper.deltaX(a.height()-1,a.width()-1,a.picture())));
	 		   	System.out.println("DY Bottom Right, Should be 10404: " + (10816 == SChelper.deltaY(a.height()-1,a.width()-1,a.picture())));
	 		   	System.out.println("EN Bottom Right, Should be 21220: " + (21220 == energy(a.width()-1,a.height()-1,a.picture())));
	 		   	//middle	
	 		   	System.out.println("DX Middle, Should be 10404:       " + (41616 == SChelper.deltaX(1,1,a.picture())));
	 		   	System.out.println("DY Middle, Should be 10404:       " + (10609 == SChelper.deltaY(1,1,a.picture())));
	 		   	System.out.println("EN Middle, Should be 52225:       " + (52225 == energy(1,1,a.picture())));	 
	 		   	
	 // Test tran2origIndex
	 	System.out.println("\nTest tran2origIndex: 3x3 matrix");	 
	 	
	 	Integer[] regIndices   = { SChelper.sub2ind(0,0,3), SChelper.sub2ind(0,1,3), SChelper.sub2ind(0,2,3) };
	 	Integer[] transIndices = { SChelper.sub2ind(0,0,3), SChelper.sub2ind(1,0,3), SChelper.sub2ind(2,0,3) };
	 	System.out.println("    regIndices:" + Arrays.toString(regIndices));
	 	System.out.println("    transIndices:" + Arrays.toString(transIndices));
	 	Integer[] fromMethod = SChelper.tran2origIndex(3,3,regIndices);
	 	System.out.println("    transIndices from method:" + Arrays.toString(fromMethod));
	 	System.out.println("    transpose Indeces mapping corectly to regular indeces: " + Arrays.deepEquals(SChelper.tran2origIndex(3,3,regIndices), transIndices) );
   
   }
}