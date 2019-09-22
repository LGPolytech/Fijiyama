package com.vitimage;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.io.OpenDialog;
import ij.io.SaveDialog;
import ij.plugin.Duplicator;
import ij.plugin.frame.RoiManager;
import math3d.Point3d;

public interface VitiDialogs {
	public final static int NO_AUTO_CHOICE=-10;
	public final static int TOOL_NOTHING=-1;
	public final static int TOOL_1_TRANSFORM_3D=0;
	public final static int TOOL_2_MRI_WATER_TRACKER=1;
	public final static int TOOL_3_MULTIMODAL_ASSISTANT=2;
	public final static int TR3D_0_MANUAL_TR=0;
	public final static int TR3D_1_AUTO_TR=1;
	public final static int TR3D_2_MAT_TR=2;
	public final static int TR3D_3_MANALIGN_TR=3;
	public final static int TR3D_4_AUTOALIGN_TR=4;
	public final static int TR3D_5_MANCAP_TR=5;
	public final static int TR3D_6_AUTOCAP_TR=6;
	public final static int MRI_0_EXPLORER=0;
	public final static int MRI_1_T1_CALCULATION=1;
	public final static int MRI_2_T2_CALCULATION=2;	
	public final static boolean SUPERVISED=false;
	public final static boolean AUTOMATIC=true;
	public final static String OS_SEPARATOR=System.getProperties().getProperty("file.separator");

	
	public static void notYet(String strGuess) {
		IJ.showMessage("Not yet implemented : "+strGuess+"\nOur team constantly struggle to provide you new features, accordingly to you needs.\n"+
						"Please consider sending a feature request to : romainfernandez06@gmail.com");
	}
	
	/** 
	 * UI interfaces for the tools
	 * */
	public static ImagePlus[] chooseTwoImagesUI(String strGuess,String strImg1, String strImg2) {
			ImagePlus[]imgRet=new ImagePlus[2];
			String open="* Je vais choisir une image dans l'explorateur de fichiers *";
			int index1,index2;
			int[] wList = WindowManager.getIDList();
			String[] titles=(wList==null) ? new String[1] : new String[wList.length+1] ;
			titles[0]=open;
			if (wList!=null) {
		        for (int i=0; i<wList.length; i++) {
		        	ImagePlus imp = WindowManager.getImage(wList[i]);
		            titles[i+1] = imp!=null?imp.getTitle():"";
		        }
	        }
	        GenericDialog gd= new GenericDialog(strGuess);
	        gd.addChoice(strImg1, titles,open);
	        gd.addChoice(strImg2, titles,open);
			gd.showDialog();
	        if (gd.wasCanceled()) return null;
	       	index1 = gd.getNextChoiceIndex()-1;
	       	index2 = gd.getNextChoiceIndex()-1;

	       	if(index1 < 0) {
	       		OpenDialog od1=new OpenDialog("Select "+strImg1);
	       		imgRet[0]=IJ.openImage(od1.getPath());
	       	}
	       	else imgRet[0]=WindowManager.getImage(wList[index1]);
	      
	       	if(index2 < 0) {
	       		OpenDialog od2=new OpenDialog("Select "+strImg2);
	       		imgRet[1]=IJ.openImage(od2.getPath());
	       	}
	       	else imgRet[1]=WindowManager.getImage(wList[index2]);
	   	    
        	return imgRet;
	}
		
	public static double[] chooseVoxSizeUI(ImagePlus img,String strGuess,boolean autonomyLevel) {
		 double[]tabRet=new double[] {img.getCalibration().pixelWidth,img.getCalibration().pixelHeight,img.getCalibration().pixelDepth};
		 if(autonomyLevel==AUTOMATIC)return tabRet;
		 else {
				GenericDialog gd = new GenericDialog(strGuess);
		        gd.addNumericField("Vx suggestion :",tabRet[0], 5);
		        gd.addNumericField("Vy suggestion :",tabRet[1], 5);
		        gd.addNumericField("Vz suggestion :",tabRet[2], 5);
		        gd.showDialog();
		        if (gd.wasCanceled()) {System.out.println("Warning : vox sizes set by default 1.0 1.0 1.0"); return new double[] {1.0,1.0,1.0};}
		 
		        tabRet[0] = gd.getNextNumber();
		        tabRet[1] = gd.getNextNumber();
		        tabRet[2] = gd.getNextNumber();
			 return tabRet;
		 }
	}

	
	public static double getDoubleUI(String strGuess,String parameter,double value) {
		double ret=0;
		GenericDialog gd = new GenericDialog(strGuess);
		gd.addNumericField(parameter+" :",value,1);
        gd.showDialog();
        if (gd.wasCanceled()) {return value;}
 
	        ret = gd.getNextNumber();
		 return ret;
	 }
	
	
	public static boolean getYesNoUI(String strGuess) {
        GenericDialog gd=new GenericDialog(strGuess);
        gd.addMessage(strGuess);
        gd.enableYesNoCancel("Yes", "No");
        gd.showDialog();
    	return (gd.wasOKed());
	}

	public static int[] chooseSizeUI(ImagePlus img,String strGuess,boolean autonomyLevel) {
		 int[]tabRet=new int[] {img.getWidth(),img.getHeight(),img.getStack().getSize()};
		 if(autonomyLevel==AUTOMATIC)return tabRet;
		 else {
				GenericDialog gd = new GenericDialog(strGuess);
		        gd.addNumericField("Dim_X suggestion :",tabRet[0],1);
		        gd.addNumericField("Dim_Y suggestion :",tabRet[1],1);
		        gd.addNumericField("Dim_Z suggestion :",tabRet[2],1);
		        gd.showDialog();
		        if (gd.wasCanceled()) {System.out.println("Warning : Dims set by default 100.0 100.0 100.0"); return new int[] {100,100,100};}
		 
		        tabRet[0] = (int) gd.getNextNumber();
		        tabRet[1] = (int) gd.getNextNumber();
		        tabRet[2] = (int) gd.getNextNumber();
			 return tabRet;
		 }
	}

	public static String chooseDirectoryUI(String strGuess){
		OpenDialog od=new OpenDialog(strGuess);
		return(od.getDirectory());
	 }

	
	public static ItkTransform chooseTransformsUI(String strGuess,boolean autonomyLevel){
		ItkTransform globalTransform = null;
		int iTr=-1;
		boolean oneAgain=true;
		GenericDialog gd2;
		do {
			iTr++;
			OpenDialog od=new OpenDialog("Select_transformation_#"+(iTr)+"");
			if(iTr==0)globalTransform=ItkTransform.readTransformFromFile(od.getPath());
			else globalTransform.addTransform(ItkTransform.readTransformFromFile(od.getPath()));
			gd2 = new GenericDialog("Encore une transformation ?");
	        gd2.addMessage("One again ?");
	        gd2.enableYesNoCancel("Yes", "No");
	        gd2.showDialog();
	    	oneAgain=gd2.wasOKed();
		} while(oneAgain);
		iTr++;
		return (globalTransform);
	 }
		

	public static ItkTransform chooseOneTransformsUI(String strGuess,String path,boolean autonomyLevel){
		if(autonomyLevel==AUTOMATIC) {
			return ItkTransform.readTransformFromFile(path);
		}
		ItkTransform globalTransform = null;
		OpenDialog od=new OpenDialog(strGuess);
		return(ItkTransform.readTransformFromFile(od.getPath()));
	 }
		


	
	public static void saveDenseFieldTransformUI(ItkTransform tr,String strGuess,boolean autonomyLevel,String path,String title,ImagePlus imgRef){
		if(autonomyLevel==AUTOMATIC) {
			String pathSave=path+""+title;
			tr.writeAsDenseField(pathSave,imgRef);
		}
		else {
			SaveDialog sd=new SaveDialog(strGuess,title,".transform.tif");
			if(sd.getDirectory()==null ||  sd.getFileName()==null)return;
			String pathSave=sd.getDirectory()+""+sd.getFileName();
			tr.writeAsDenseField(pathSave,imgRef);
		}
	}

	
	
	public static void saveMatrixTransformUI(ItkTransform tr,String strGuess,boolean autonomyLevel,String path,String title){
		if(autonomyLevel==AUTOMATIC) {
			String pathSave=path+""+title;
			tr.writeMatrixTransformToFile(pathSave);
		}
		else {
			SaveDialog sd=new SaveDialog(strGuess,title,".txt");
			if(sd.getDirectory()==null ||  sd.getFileName()==null)return;
			String pathSave=sd.getDirectory()+""+sd.getFileName();
			tr.writeMatrixTransformToFile(pathSave);
		}
	}

	public static void saveImageUI(ImagePlus img,String strGuess,boolean autonomyLevel,String path,String title) {
		if(autonomyLevel==AUTOMATIC) {
			String pathSave=path+title;
			IJ.saveAsTiff(img,pathSave);
		}
		else {
			SaveDialog sd=new SaveDialog(strGuess,title,".tif");
			if(sd.getDirectory()==null ||  sd.getFileName()==null)return;
			String pathSave=sd.getDirectory()+""+sd.getFileName();
			IJ.saveAsTiff(img,pathSave);
		}
	
	}
	

	public static double[][] waitForReferencePointsUI(int nbWantedPoints,ImagePlus img,boolean realCoordinates){
		double[][]tabRet=new double[nbWantedPoints][3];
		RoiManager rm=RoiManager.getRoiManager();
		ImagePlus temp=img.duplicate();
		temp.show();
		rm.reset();
		IJ.setTool("point");
		boolean finished =false;
		do {
			try {
				java.util.concurrent.TimeUnit.MILLISECONDS.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(rm.getCount()==nbWantedPoints && getYesNoUI("Confirm points ?"))finished=true;
			System.out.println("Waiting "+nbWantedPoints+". Current number="+rm.getCount());
		}while (!finished);	
		for(int indP=0;indP<nbWantedPoints;indP++){
			tabRet[indP][0]=rm.getRoi(indP).getXBase();
			tabRet[indP][1]=rm.getRoi(indP).getYBase();
			tabRet[indP][2]=rm.getRoi(indP).getZPosition();
			if(realCoordinates) {
				tabRet[indP][0]=(tabRet[indP][0]+0.5)*(img.getCalibration().pixelWidth);
				tabRet[indP][1]=(tabRet[indP][1]+0.5)*(img.getCalibration().pixelHeight);
				tabRet[indP][2]=(tabRet[indP][2]+0.5)*(img.getCalibration().pixelDepth);
			}	
			System.out.println("Point retenu numéro "+indP+" : {"+tabRet[indP][0]+","+tabRet[indP][1]+","+tabRet[indP][2]+"}");
		}
		temp.close();
		return tabRet;
	}


	public static Point3d[] waitForPointsUIUntilClickOnSlice1(ImagePlus rx,ImagePlus mri,ImagePlus img2,boolean realCoordinates){
		ImagePlus img=new Duplicator().run(img2);
		img.setTitle("waitForPointsUI");
		img.show();
		rx.show();
		mri.show();
		VitimageUtils.adjustImageCalibration(img, img2);
		double[][]tabRet=new double[50000][3];
		RoiManager rm=RoiManager.getRoiManager();
		rm.reset();
		IJ.setTool("point");
		boolean finished =false;
		//getYesNoUI("Identification of the four corners \nof the inoculation point with ROI points\nAre you ready  ?");
		do {
			try {
				java.util.concurrent.TimeUnit.MILLISECONDS.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(rm.getCount()>0 && rm.getRoi(rm.getCount()-1).getZPosition()==1)finished=true;
			System.out.println("Waiting. Number of points : "+rm.getCount());
		}while (!finished);	
		for(int indP=0;indP<rm.getCount()-1;indP++){
			tabRet[indP][0]=rm.getRoi(indP).getXBase();
			tabRet[indP][1]=rm.getRoi(indP).getYBase();
			tabRet[indP][2]=rm.getRoi(indP).getZPosition();
			if(realCoordinates) {
				tabRet[indP][0]=(tabRet[indP][0])*(img.getCalibration().pixelWidth);
				tabRet[indP][1]=(tabRet[indP][1])*(img.getCalibration().pixelHeight);
				tabRet[indP][2]=(tabRet[indP][2])*(img.getCalibration().pixelDepth);
			}	
			System.out.println("Point retenu numéro "+indP+" : {"+tabRet[indP][0]+","+tabRet[indP][1]+","+tabRet[indP][2]+"}");
		}
		Point3d []ptRet=new Point3d[rm.getCount()-1];
		for(int i=0;i<rm.getCount()-1;i++)ptRet[i]=new Point3d(tabRet[i][0],tabRet[i][1],tabRet[i][2]);
		img.hide();
		rx.hide();
		mri.hide();
		return ptRet;
	}
	
	
	public static double[][] waitForPointsUI(int nbWantedPoints,ImagePlus img,boolean realCoordinates){
		double[][]tabRet=new double[nbWantedPoints][3];
		RoiManager rm=RoiManager.getRoiManager();
		rm.reset();
		IJ.setTool("point");
		boolean finished =false;
		//getYesNoUI("Identification of the four corners \nof the inoculation point with ROI points\nAre you ready  ?");
		do {
			try {
				java.util.concurrent.TimeUnit.MILLISECONDS.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(rm.getCount()==nbWantedPoints)finished=true;
			System.out.println("Waiting "+nbWantedPoints+". Current number="+rm.getCount());
		}while (!finished);	
		for(int indP=0;indP<nbWantedPoints;indP++){
			tabRet[indP][0]=rm.getRoi(indP).getXBase();
			tabRet[indP][1]=rm.getRoi(indP).getYBase();
			tabRet[indP][2]=rm.getRoi(indP).getZPosition();
			if(realCoordinates) {
				tabRet[indP][0]=(tabRet[indP][0]+0.5)*(img.getCalibration().pixelWidth);
				tabRet[indP][1]=(tabRet[indP][1]+0.5)*(img.getCalibration().pixelHeight);
				tabRet[indP][2]=(tabRet[indP][2]+0.5)*(img.getCalibration().pixelDepth);
			}	
			System.out.println("Point retenu numéro "+indP+" : {"+tabRet[indP][0]+","+tabRet[indP][1]+","+tabRet[indP][2]+"}");
		}
		return tabRet;
	}
	
	public static Point3d[][] registrationPointsUI(int nbWantedPointsPerImage,ImagePlus imgRef,ImagePlus imgMov,boolean realCoordinates){
		ImagePlus imgRefBis=imgRef.duplicate();
		ImagePlus imgMovBis=imgMov.duplicate();
		imgRefBis.getProcessor().resetMinAndMax();
		imgMovBis.getProcessor().resetMinAndMax();
		imgRefBis.show();
		imgMovBis.show();
		imgMovBis.setTitle("Moving image");
		imgRefBis.setTitle("Reference image");
		RoiManager rm=RoiManager.getRoiManager();
		rm.reset();
		IJ.setTool("point");
		IJ.showMessage("Examine images and click on "+(nbWantedPointsPerImage*2)+" points to compute the correspondances,\n on both reference image and moving image (see image titles)"+
		"For each selected point, use the Roi Manager to save it, with \"add to manager\" option.\n Please follow the following order : "+
		"\n   Point 1 : item A on reference image\n    Point 2 : item A on moving image\n    Point 3 : item B on reference image\n Point 4 : item B on moving image \n...");
		boolean finished =false;
		do {
			try {
				java.util.concurrent.TimeUnit.MILLISECONDS.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(rm.getCount()>=nbWantedPointsPerImage*2 && getYesNoUI("Confirm points ?"))finished=true;
			System.out.println("Waiting "+(nbWantedPointsPerImage*2)+". Current number="+rm.getCount());
		}while (!finished);	
		int nCouples=Math.max(nbWantedPointsPerImage, rm.getCount()/2);
		Point3d []pRef=new Point3d[nCouples];
		Point3d []pMov=new Point3d[nCouples];
		for(int indP=0;indP<nCouples;indP++){
			pRef[indP]=new Point3d(rm.getRoi(indP*2 ).getXBase() , rm.getRoi(indP * 2).getYBase() ,  rm.getRoi(indP * 2).getZPosition());
			pMov[indP]=new Point3d(rm.getRoi(indP*2 +1 ).getXBase() , rm.getRoi(indP * 2 +1 ).getYBase() ,  rm.getRoi(indP * 2 +1 ).getZPosition());
			if(realCoordinates) {
				pRef[indP]=TransformUtils.convertPointToRealSpace(pRef[indP],imgRef);
				pMov[indP]=TransformUtils.convertPointToRealSpace(pMov[indP],imgRef);				
			}	
			//System.out.println("Point retenu numéro "+indP+" : {"+tabRet[indP][0]+","+tabRet[indP][1]+","+tabRet[indP][2]+"}");
		}
		imgRefBis.close();
		imgMovBis.close();
		return new Point3d[][] {pRef,pMov};
	}
	
	public static Point3d inspectInoculationPoint(ImagePlus img,Point3d suggestedInocPoint) {
		double ray=1;//en mm
		ImagePlus imgInspect=new Duplicator().run(img);
		Point3d pointCoordImage=TransformUtils.convertPointToImageSpace(suggestedInocPoint,imgInspect);
		imgInspect.getProcessor().resetMinAndMax();
		IJ.run(imgInspect,"8-bit","");
		ImagePlus imgPoint=ij.gui.NewImage.createImage("point",img.getWidth(),img.getHeight(),img.getStackSize(),8,ij.gui.NewImage.FILL_BLACK);
		VitimageUtils.adjustImageCalibration(imgPoint,imgInspect);
		imgPoint=VitimageUtils.drawCircleInImage(imgPoint,ray,(int)Math.round(pointCoordImage.x),(int)Math.round(pointCoordImage.y),(int)Math.round(pointCoordImage.z));
		ImagePlus comp=VitimageUtils.compositeOf(imgInspect, imgPoint);
		VitimageUtils.imageChecking(comp, (int)Math.round(pointCoordImage.z)-comp.getStackSize()/10,
										  (int)Math.round(pointCoordImage.z)+comp.getStackSize()/10, 
									  	  3, "Suggested inoculation point", 5,false);
		comp.show();
		comp.setSlice((int)Math.round(pointCoordImage.z)+1);
		int secondsLast=15;
		RoiManager rm=RoiManager.getRoiManager();
		rm.reset();
		IJ.setTool("point");
		while(secondsLast-- > 0) {
			comp.setTitle("Change inoc point if necessary. "+secondsLast+" s left...");
			VitimageUtils.waitFor(1000);
		}
		if(rm.getCount()==0) {
			rm.close();
			comp.changes=false;
			comp.close();
			imgInspect.changes=false;
			imgInspect.close();		
			return suggestedInocPoint;
		}
		
		pointCoordImage=new Point3d(rm.getRoi(0 ).getXBase() , rm.getRoi(0).getYBase() ,  rm.getRoi(0).getZPosition());
		pointCoordImage=TransformUtils.convertPointToRealSpace(pointCoordImage,imgInspect);
		rm.close();
		comp.changes=false;
		comp.close();
		imgInspect.changes=false;
		imgInspect.close();
		return pointCoordImage;		
	}
	
	
	
	public static double[][] inspectAxis(ImagePlus img ,double[] vectZ,Point3d ptOrigine,int delayForReacting){
		double ray=1;//en mm
		ImagePlus imgInspect=new Duplicator().run(img);
		Point3d pointCoordImage=TransformUtils.convertPointToImageSpace(ptOrigine,imgInspect);
		imgInspect.getProcessor().resetMinAndMax();
		IJ.run(imgInspect,"8-bit","");
		ImagePlus imgPoint=ij.gui.NewImage.createImage("point",img.getWidth(),img.getHeight(),img.getStackSize(),8,ij.gui.NewImage.FILL_BLACK);
		VitimageUtils.adjustImageCalibration(imgPoint,imgInspect);
		imgPoint=VitimageUtils.drawThickLineInImage(imgPoint,ray,(int)Math.round(pointCoordImage.x),(int)Math.round(pointCoordImage.y),(int)Math.round(pointCoordImage.z),vectZ);
		ImagePlus comp=VitimageUtils.compositeOf(imgInspect, imgPoint);
		VitimageUtils.imageCheckingFast(comp,  "Suggested axis");
		comp.show();
		comp.setSlice((int)Math.round(pointCoordImage.z)+1);
		int secondsLast=delayForReacting;
		RoiManager rm=RoiManager.getRoiManager();
		rm.reset();
		IJ.setTool("point");
		while(secondsLast-- > 0) {
			comp.setTitle("Change axis if necessary. Z-up then Z-down. "+secondsLast+" s left...");
			VitimageUtils.waitFor(1000);
		}
		if(rm.getCount()<2) {
			rm.close();
			comp.changes=false;
			comp.close();
			imgInspect.changes=false;
			imgInspect.close();		
			return new double[][]{vectZ,new double[] {0,0,0},new double[] {0,0,0}};
		}
		
		Point3d pointCoordImageUp=new Point3d(rm.getRoi(0 ).getXBase() , rm.getRoi(0).getYBase() ,  rm.getRoi(0).getZPosition());
		pointCoordImageUp=TransformUtils.convertPointToRealSpace(pointCoordImageUp,imgInspect);
		Point3d pointCoordImageDown=new Point3d(rm.getRoi(1 ).getXBase() , rm.getRoi(1).getYBase() ,  rm.getRoi(1).getZPosition());
		pointCoordImageDown=TransformUtils.convertPointToRealSpace(pointCoordImageDown,imgInspect);
		rm.close();
		comp.changes=false;
		comp.close();
		imgInspect.changes=false;
		imgInspect.close();
		System.out.println("On passe par ici");
		return new double[][]{TransformUtils.normalize(new double[] { pointCoordImageUp.x - pointCoordImageDown.x , 
																pointCoordImageUp.y - pointCoordImageDown.y , 
																pointCoordImageUp.z - pointCoordImageDown.z } ), 
													new double[] { pointCoordImageUp.x , pointCoordImageUp.y , pointCoordImageUp.z } ,
													new double[] { pointCoordImageDown.x , pointCoordImageDown.y , pointCoordImageDown.z } };
		}

		
		
		
}
	
	
	
	
	
	
	
	