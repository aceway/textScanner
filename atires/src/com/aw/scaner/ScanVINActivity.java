package com.aw.scaner;

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;

import android.util.Log;
import android.content.Context;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.AsyncTask;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.view.View;
import android.view.SurfaceView;  
import android.view.SurfaceHolder;
import android.graphics.Rect;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;   
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Color;
import android.widget.Toast;
import android.widget.Button;
import android.hardware.Camera;

import android.content.ContentResolver;
import android.provider.MediaStore;

import com.googlecode.tesseract.android.*;

public class ScanVINActivity extends Activity implements SurfaceHolder.Callback, Camera.ShutterCallback, Camera.PictureCallback{	
	private Camera 		myCam = null;
	private SurfaceView mySView = null;
	private MuskSurfaceDraw  	mySVDraw = null;
	private String 		myOcrText = null;
	private final String TESSBASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "AWScaner" + File.separator;
	private String DEFAULT_LANGUAGE = "eng";
	private String ENG_DATA = "eng.traineddata";
	private String CHI_DATA = "chi_sim.traineddata";
	private String OSD_DATA = "osd.traineddata";
	private Camera.AutoFocusCallback myAutoFocusCallback;	
	private Button btnFocus = null;
	private Button btnDigit = null;
	private Button btnChiSim = null;
	private Button btnEn = null;
	private Button btnVIN = null;
	private boolean bCamInited = false;
	private boolean scanDigitOnly = false;
	private boolean scanVINOnly   = false;
	private boolean saveSDCard = false;
	
	private ProgressDialog pd;
	
	void initOcrDatabase(){
		File dir = new File(TESSBASE_PATH);
		if (!dir.exists()) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			dir.mkdir();
		}
		else if (!dir.exists()) {
			dir = new File(TESSBASE_PATH + "tessdata/");
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			dir.mkdir();
		}
		File f1 = new File(TESSBASE_PATH + "tessdata/" + ENG_DATA);
		File f2 = new File(TESSBASE_PATH + "tessdata/" + OSD_DATA);
		File f3 = new File(TESSBASE_PATH + "tessdata/" + CHI_DATA);
		if (!f1.exists() || !f2.exists() || !f3.exists()) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			StartFrameTask startFrameTask = new StartFrameTask();
	        startFrameTask.cntxt = this;
	        startFrameTask.savePath = TESSBASE_PATH + "tessdata/";
	        startFrameTask.assetsNames.clear();
	        startFrameTask.assetsNames.add(ENG_DATA);
	        startFrameTask.assetsNames.add(OSD_DATA);
	        startFrameTask.assetsNames.add(CHI_DATA);
	        startFrameTask.execute();
        }        
	}
	
	@Override
	public void  onCreate(Bundle savedInstanceState){
		Log.i("ScanVINActivity", "ScanVINActivity::onCreate");
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_camera);
		mySView = (SurfaceView)findViewById(R.id.preview_cam);
		mySView.setZOrderOnTop(false);
		mySView.getHolder().addCallback(this);
		mySView.getHolder().setFormat(PixelFormat.TRANSPARENT);
		mySView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        	
		mySVDraw = (com.aw.scaner.MuskSurfaceDraw)findViewById(R.id.mDraw);						
		mySVDraw.setVisibility(View.VISIBLE);
		
		btnFocus = (Button)findViewById(R.id.btn_scan_focus);
		btnDigit= (Button)findViewById(R.id.btn_scan_digit);
		btnChiSim = (Button)findViewById(R.id.btn_scan_chi_sim);
		btnEn = (Button)findViewById(R.id.btn_scan_en);
		btnVIN = (Button)findViewById(R.id.btn_scan_vin);
		
		initOcrDatabase();
	}
	
	@Override
	public void  onPause(){
		super.onPause();
		if( myCam != null){
			myCam.stopPreview();
		}		
	}
	
	@Override
	public void  onDestroy(){
		super.onDestroy();
		if(myCam != null){
			myCam.release();
			myCam = null;
			bCamInited = false;
		}		
	}
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH) @SuppressLint("NewApi") 
	public void onBtnScanFocus(View v){
		if(myCam != null && bCamInited){			
			//btnFocus.setEnabled(true));
			btnDigit.setEnabled(false);
			btnChiSim.setEnabled(false);
			btnEn.setEnabled(false);
			btnVIN.setEnabled(false);
			
			Camera.Parameters params = myCam.getParameters();
			int nFA = params.getMaxNumFocusAreas();
			int nMA = params.getMaxNumMeteringAreas();
			Rect rtSurf = new Rect(0, 0, mySView.getWidth(), mySView.getHeight());
			Rect rtScan = mySVDraw.getScanRect();
			if(rtScan == null){
				rtScan = new Rect(rtSurf.centerX()-20, rtSurf.centerY()-20, rtSurf.centerX()+20, rtSurf.centerY()+20);
			}
			Rect rtTo = new Rect(-1000, -1000, 1000, 1000);
			Rect rtFoucus = TransCoordinates(rtSurf, rtTo, rtScan);
			Log.e("CAM", "Focus area:" + rtFoucus.left + ", " + rtFoucus.top + ", " + rtFoucus.right + ", " + rtFoucus.bottom);
			if (nFA > 0){
				Log.w("CAM", "setFocusAreas");
			    List<Camera.Area> foucusAreas = new ArrayList<Camera.Area>();  
			    foucusAreas.add(new Camera.Area(rtFoucus, 1000));
			    params.setFocusAreas(foucusAreas);
			}
			if (nMA > 0){
				Log.w("CAM", "setMeteringAreas");
				List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>(); 
			    meteringAreas.add(new Camera.Area(rtFoucus, 1000));
			    params.setMeteringAreas(meteringAreas);
			}
			myCam.setParameters(params);
			myCam.cancelAutoFocus();
			myCam.autoFocus(myAutoFocusCallback);
		}
	}
	
	public void onBtnScanDigit(View v){
		DEFAULT_LANGUAGE = "eng";
		if(myCam != null && bCamInited){
			scanDigitOnly = true;
			scanVINOnly = false;
			
			btnFocus.setEnabled(false);
			btnDigit.setEnabled(false);
			btnChiSim.setEnabled(false);
			btnEn.setEnabled(false);
			btnVIN.setEnabled(false);
			
			myCam.takePicture(this, null, null, this);
		}		
	}

	public void onBtnScanChiSim(View v){
		DEFAULT_LANGUAGE = "chi_sim";
		if(myCam != null && bCamInited){
			scanDigitOnly = false;
			scanVINOnly = false;
			
			btnFocus.setEnabled(false);
			btnDigit.setEnabled(false);
			btnChiSim.setEnabled(false);
			btnEn.setEnabled(false);
			btnVIN.setEnabled(false);
			
			myCam.takePicture(this, null, null, this);
		}		
	}
	
	public void onBtnScanEn(View v){
		DEFAULT_LANGUAGE = "eng";
		if(myCam != null && bCamInited){
			scanDigitOnly = false;
			scanVINOnly = false;
			
			btnFocus.setEnabled(false);
			btnDigit.setEnabled(false);
			btnChiSim.setEnabled(false);
			btnEn.setEnabled(false);
			btnVIN.setEnabled(false);
			
			myCam.takePicture(this, null, null, this);
		}		
	}

	public void onBtnScanVIN(View v){
		DEFAULT_LANGUAGE = "eng";
		if(myCam != null && bCamInited){
			scanDigitOnly = false;
			scanVINOnly = true;
			
			btnFocus.setEnabled(false);
			btnDigit.setEnabled(false);
			btnChiSim.setEnabled(false);
			btnEn.setEnabled(false);
			btnVIN.setEnabled(false);
			
			myCam.takePicture(this, null, null, this);
		}		
	}
	
	@Override
	public void onShutter(){
		Toast.makeText(this, R.string.cam_shutter_tips , Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onPictureTaken(byte[] data, Camera cam){
		try{
			if("chi_sim" == DEFAULT_LANGUAGE ){
				ocrCameraAreaData(data);
				if (myOcrText.length() > 0){
					if(myCam != null){
						myCam.stopPreview();
	        			myCam.release();
	        			myCam = null;
	        			bCamInited = false;
	        		}
	    			Intent tnt = new Intent(ScanVINActivity.this, ResultTextActivity.class);
	    			tnt.putExtra("scan_vin_code", myOcrText);
	    			ScanVINActivity.this.startActivity(tnt);
				}
				else{
					Toast.makeText(this, R.string.ocr_failed , Toast.LENGTH_SHORT).show();
				}
			}
			else if ("eng" == DEFAULT_LANGUAGE){
				ocrCameraAreaData(data);
				if (myOcrText.length() > 0){
	        		if(myCam != null){
	        			myCam.stopPreview();
	        			myCam.release();
	        			myCam = null;
	        			bCamInited = false;
	        		}	
	        		
	    			Intent tnt = new Intent(ScanVINActivity.this, ResultTextActivity.class);
	    			tnt.putExtra("scan_vin_code", myOcrText);
	    			ScanVINActivity.this.startActivity(tnt);
				}
				else{
					Toast.makeText(this, R.string.ocr_failed , Toast.LENGTH_SHORT).show();
				}
			}
			if (myCam != null) myCam.startPreview();
		}
		catch(Exception e){
			e.printStackTrace();
		}
        btnFocus.setEnabled(true);
        btnDigit.setEnabled(true);
		btnChiSim.setEnabled(true);
		btnEn.setEnabled(true);
		btnVIN.setEnabled(true);
	} 

    public String OcrFiltered(String strOCR){
    	String strFiltered = "";
    	for(int idx = 0; idx < strOCR.length(); idx++){ 
    		char ch = strOCR.charAt(idx);
    		if (Character.isLetterOrDigit(ch) || ch == ' ' || ch == ',' || ch == '.' || ch == '?' || ch == '!'){
    			strFiltered += ch;
    		}
    		else{
    			strFiltered  += ' ';
    		}
    	}
    	return strFiltered;
    }
    
	public Bitmap convertRGB2GreyImg(Bitmap img){
	     int width = img.getWidth();
	     int height= img.getHeight();
	     int []pixels = new int[width * height];
	     img.getPixels(pixels, 0, width, 0, 0, width, height);
	     int alpha = 0xFF << 24; 
	     for(int i = 0; i < height; i++) {
	    	 for(int j = 0; j < width; j++) {
	    		 int grey 	= pixels[width * i + j];
	    		 int red 	= ((grey  & 0x00FF0000 ) >> 16);
	    		 int green 	= ((grey & 0x0000FF00) >> 8);
	    		 int blue 	= (grey & 0x000000FF);
	    		 grey = (int)((float) red * 0.3 + (float)green * 0.59 + (float)blue * 0.11);
	    		 grey = alpha | (grey << 16) | (grey << 8) | grey;
	    		 pixels[width * i + j] = grey;
	    	 }
	     }
	     Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
	     result.setPixels(pixels, 0, width, 0, 0, width, height);
	     return result;
	}
	
	private void ocrCameraAreaData(byte[] data){
		Log.i("CAM", "ocrCameraAreaData...");
		Bitmap mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
		if(mBitmap == null){
			Log.e("Bitmap", "BitmapFactory.decodeByteArray(data, 0, data.length);" );
		}
		Matrix matrix = new Matrix();
		Bitmap rotaBitmap = mBitmap;
		if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {    
			//matrix.postRotate((float)90.0);
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {   
        	matrix.postRotate((float)90.0);
        	rotaBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, false);
    		if(rotaBitmap == null){
    			Log.e("Bitmap", "createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, false);" );
    		}
        }
        else{
            matrix.postRotate((float)90.0);
            rotaBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, false);
    		if(rotaBitmap == null){
    			Log.e("Bitmap", "createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, false);" );
    		}
        }
		
		Bitmap scaledBitmap = Bitmap.createScaledBitmap(rotaBitmap, mySView.getWidth(), mySView.getHeight(), true);
		if(scaledBitmap == null){
			Log.e("Bitmap", "Bitmap.createScaledBitmap(rotaBitmap, mySView.getWidth(), mySView.getHeight(), true);" );
		}
		Rect rt = mySVDraw.getScanRect();
		Bitmap rectBitmap = Bitmap.createBitmap(scaledBitmap, rt.left, rt.top, rt.width(), rt.height());
		if(scaledBitmap == null){
			Log.e("Bitmap", "Bitmap.createBitmap(scaledBitmap, rt.left, rt.top, rt.width(), rt.height());" );
		}
		Bitmap binBitmap = convertRGB2GreyImg(rectBitmap);
		Bitmap greyBitmap = binarizationBitmap(binBitmap);
		
		Log.i("PICTURE", "OriginBMPSize:" + mBitmap.getWidth() + "W *" + mBitmap.getHeight() + "H");
		Log.i("PICTURE", "SurfSize:" + mySView.getWidth() + "W *" + mySView.getHeight() + "H" );
		Log.i("PICTURE", "RotaBMPSize:" + rotaBitmap.getWidth() + "W *" + rotaBitmap.getHeight() + "H");
		Log.i("PICTURE", "ScaledBMPSize:" + scaledBitmap.getWidth() + "W *" + scaledBitmap.getHeight() + "H");
		Log.i("PICTURE", "mySVDrawPSize:" + rt.width() + "W *" + rt.height() + "H");
		Log.i("PICTURE", "RectBMPSize:" + rectBitmap.getWidth() + "W *" + rectBitmap.getHeight() + "H");
		if(null == greyBitmap)
		{ 
			Log.e("Bitmap", "createBitmap(sizeBitmap, rt.left, rt.top, rt.width(), rt.height());" );
		}
		else{
			ContentResolver resolver = getContentResolver();    
			MediaStore.Images.Media.insertImage(resolver, greyBitmap, "ocr", "ocr image.");
		}
		String fileName = "scan_vin.jpg";
        try {
        	
        	File fullPathFile = null;
        	FileOutputStream outStream = null;
        	if(saveSDCard){
        		fullPathFile = new File(TESSBASE_PATH, fileName);
        		outStream = new FileOutputStream(fullPathFile);     //SSD CARD file opt.
        		Log.i("SAVE", "saveJpeg: " + fullPathFile);
        	}
        	else{
        		outStream = this.openFileOutput(fileName, Activity.MODE_PRIVATE); // internal memory file opt.        		
        	}            
            BufferedOutputStream bufferStream = new BufferedOutputStream(outStream);  
            greyBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bufferStream);  
            bufferStream.flush();  
            bufferStream.close();
            outStream.close();

            FileInputStream inputStream = null;
            if( saveSDCard ){
            	inputStream = new FileInputStream(fullPathFile);	//SSD CARD file opt.	
            }
            else{
            	inputStream = this.openFileInput(fileName);		// internal memory file opt.
            }			
			BitmapFactory.Options options=new BitmapFactory.Options();
			options.inSampleSize=2;
			Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
			inputStream.close();
		    
		    if(saveSDCard){
		    	//fullPathFile.delete();  //SSD CARD file opt.
		    }
		    else{
		    	//this.deleteFile( fileName ); // internal memory file opt.
		    	
		    }
		    if(bitmap == null){
		    	Log.e("Bitmap", "BitmapFactory.decodeStream(instream, null, options);" );
		    }
		    Toast.makeText(this, R.string.ocr_vin_tips, Toast.LENGTH_SHORT).show();
			TessBaseAPI baseApi=new TessBaseAPI();
			Log.w("TESSBASE_PATH", TESSBASE_PATH);
			if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
				Log.v("SD Card", "SD state is :" + Environment.getExternalStorageState());
			}
			baseApi.init(TESSBASE_PATH, DEFAULT_LANGUAGE);
			if(scanDigitOnly){
				baseApi.setVariable("tessedit_char_whitelist", "0123456789-,.");
			}
			else if(scanVINOnly){
				baseApi.setVariable("tessedit_char_whitelist", "0123456789ABCDEFGHJKLMNPRSTUVWXYZabcdefghjklmnprstuvwxyz");
			}
			
			baseApi.setImage(bitmap);
			String ocrStr = baseApi.getUTF8Text();
			myOcrText = OcrFiltered(ocrStr);
			Log.i("OCR", ocrStr);
			Log.e("OCR", myOcrText);
			Toast.makeText(this,  R.string.ocr_finish_tips, Toast.LENGTH_SHORT).show();
			baseApi.clear();
			baseApi.end();
        } catch (Exception e) {    
            Log.e("OCR", "EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");  
            e.printStackTrace();  
        }	
	}
	
	public Rect getScreenRect(){
		Rect rt = new Rect(0, 0, 0, 0);
		int screenW = getWindowManager().getDefaultDisplay().getWidth();
		int screenH = getWindowManager().getDefaultDisplay().getHeight();
		rt.left = 0; rt.right = screenW; rt.top = 0; rt.bottom = screenH;
		return rt;
	}
	
	private boolean checkInSize(List<Camera.Size> sizeList, Rect sizeChecked){
		int w = sizeChecked.width(), h = sizeChecked.height();
		for(Camera.Size sz:sizeList){  
			if(sz.width == w && sz.height == h) return true;  
        }
		return false;
	}
	
	private Rect findOneGoodRect(List<Camera.Size> sizeListPreview, List<Camera.Size> sizeListPicture){
		Rect rt = new Rect(0, 0, 0, 0);
		for(Camera.Size szPrev:sizeListPreview){  
			for(Camera.Size szPict:sizeListPicture){  
				if(szPrev.width == szPict.width && szPrev.height == szPict.height ){
					rt.right = szPrev.width; rt.bottom = szPrev.height;
					return rt;
				}
	        }
        }
		return rt;
	}
	
	@SuppressLint("NewApi") 
	private void initCamera(Camera myCam0){
		Camera.Parameters params = myCam0.getParameters();
		//params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
		
		int maxZoom = params.getMaxZoom();
		params.setZoom(1);
		Log.i("CAM", "initCamera: MaxZoom = " + maxZoom); 
		Log.w("CAM", "initCamera: Zoom=1"); 
		
		List<Camera.Size> sizesSupportedPrev = params.getSupportedPreviewSizes();			
		for(Camera.Size sz:sizesSupportedPrev){  
			Log.i("CAM", "initCamera:  width = "+sz.width+", height = "+sz.height);  
        }
		List<Camera.Size> sizesSupportedPict = params.getSupportedPictureSizes(); 
		for(Camera.Size sz:sizesSupportedPict){    
			Log.i("CAM", "initCamera: width = "+sz.width+", height = "+sz.height);  
        }
		Rect screenRT = getScreenRect();
		if (checkInSize(sizesSupportedPrev, screenRT) && checkInSize(sizesSupportedPict, screenRT)){				
			params.setPreviewSize(screenRT.width(), screenRT.height());
			params.setPictureSize(screenRT.width(), screenRT.height());
			
		}
		else{
			Rect rt = findOneGoodRect(sizesSupportedPrev, sizesSupportedPict);
			if (rt.width() > 0 && rt.height() > 0){					
				params.setPreviewSize(rt.width(), rt.height());
				params.setPictureSize(rt.width(), rt.height());
			}
			else{
				Camera.Size sz = sizesSupportedPrev.get(0);
				params.setPreviewSize(sz.width, sz.height);
				sz = sizesSupportedPict.get(0);
				params.setPictureSize(sz.width, sz.height);
			}
		}
		Log.w("CAM", "initCamera: width = " + params.getPreviewSize().width +", height = " + params.getPreviewSize().height);
		Log.w("CAM", "initCamera: width = " + params.getPictureSize().width +", height = " + params.getPictureSize().height);
		
		List<String> focusSupported = params.getSupportedFocusModes();
		for(String fcs:focusSupported){   
			Log.i("CAM", "initCamera: " + fcs);  
        }
		if(focusSupported.contains("macro")){
        	params.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
        	Log.w("CAM", "initCamera: macro");
        }/*else if(focusSupported.contains("continuous-picture")){
        	params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        	Log.w("CAM", "initCamera: continuous-picture");
        }*/else if(focusSupported.contains("continuous-video")){  
        	params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO );
        	Log.w("CAM", "initCamera: continuous-video");
        }else if(focusSupported.contains("auto")){
        	params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        	Log.w("CAM", "initCamera: auto");
        }
        else if(focusSupported.contains("infinity")){
        	params.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
        	Log.w("CAM", "initCamera: infinity");
        }
		int nFA = params.getMaxNumFocusAreas();
		int nMA = params.getMaxNumMeteringAreas();
		Log.w("CAM", "getMaxNumFocusAreas:" + nFA);
		Log.w("CAM", "getMaxNumMeteringAreas:" + nMA);
		Rect rtSurf = new Rect(0, 0, mySView.getWidth(), mySView.getHeight());
		Rect rtScan = mySVDraw.getScanRect();
		if(rtScan == null){
			rtScan = new Rect(rtSurf.centerX()-20, rtSurf.centerY()-20, rtSurf.centerX()+20, rtSurf.centerY()+20);
		}
		Rect rtTo = new Rect(-1000, -1000, 1000, 1000);
		Rect rtFoucus = TransCoordinates(rtSurf, rtTo, rtScan);		
		if (nFA > 0){
			Log.i("CAM", "setFocusAreas");
		    List<Camera.Area> foucusAreas = new ArrayList<Camera.Area>();  
		    foucusAreas.add(new Camera.Area(rtFoucus, 1000));
		    params.setFocusAreas(foucusAreas);
		}
		if (nMA > 0){
			Log.i("CAM", "setMeteringAreas");
			List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>(); 
		    meteringAreas.add(new Camera.Area(rtFoucus, 1000));
		    params.setMeteringAreas(meteringAreas);
		}
		
		try {
			myCam0.setParameters(params);
			if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {    
				//myCam0.setDisplayOrientation(90);
            } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {   
                myCam0.setDisplayOrientation(90);
            }
            else{
                myCam0.setDisplayOrientation(90);
            }
			//myCam0.startPreview(); 
			bCamInited = true;
        } catch (Exception e) {
        	Log.e("CAM", "EEEEEEEEEEEEEEEE");
            e.printStackTrace();  
        }
	}
	
	private Rect TransCoordinates(Rect rectFrom, Rect rectTo, Rect rectTransed){
		Log.i("TransCoordinates", "From area:" + rectFrom.left + ", " + rectFrom.top + ", " + rectFrom.right + ", " + rectFrom.bottom);
		Log.i("TransCoordinates", "To area:" + rectTo.left + ", " + rectTo.top + ", " + rectTo.right + ", " + rectTo.bottom);
		Log.i("TransCoordinates", "Transed area:" + rectTransed.left + ", " + rectTransed.top + ", " + rectTransed.right + ", " + rectTransed.bottom);
		Rect rt = new Rect(0, 0, 0, 0);
		
		float rW = (float)rectTo.width() / (float)rectFrom.width();
		float rH = (float)rectTo.height() / (float)rectFrom.height();
		
		float offsetW = (float)rectTransed.left * rW;
		float offsetH = (float)rectTransed.top * rH;
		
		float newW = (float)rectTransed.width() * rW;
		float newH = (float)rectTransed.height()* rH;
		
		rt.left = rectTo.left + (int)offsetW;
		rt.top  = rectTo.top + (int)offsetH;
		rt.right = rt.left + (int)newW;
		rt.bottom= rt.top + (int)newH;
		Log.i("CAM", "Result area:" + rt.left + ", " + rt.top + ", " + rt.right + ", " + rt.bottom);
		return rt;
	}
	
	@Override
	public void  surfaceCreated(SurfaceHolder holder){
		Log.i("ScanVINActivity", "ScanVINActivity::surfaceCreated");
	}
	
	@Override
	public void  surfaceChanged(SurfaceHolder holder, int format, int width, int height){
		Log.i("ScanVINActivity", "ScanVINActivity::surfaceCreated");
		try{
			if(myCam == null){
				myAutoFocusCallback = new Camera.AutoFocusCallback() {  
		            public void onAutoFocus(boolean success, Camera camera) {
		                if(success){
		                	camera.setOneShotPreviewCallback(null);  
		                    Toast.makeText(ScanVINActivity.this, "Focus OK!", Toast.LENGTH_SHORT).show();

		        			//btnFocus.setEnabled(true));
		                    btnDigit.setEnabled(true);
		                    btnChiSim.setEnabled(true);
		                    btnEn.setEnabled(true);
		                    btnVIN.setEnabled(true);
		                    Log.w("CAM", "Auto focus ok.");
		                }
		                else{
		                	Log.w("CAM", "camera auto focusing...");
		                	camera.cancelAutoFocus();
		                	camera.autoFocus(myAutoFocusCallback);
		                }
		            }  
		        };
		        myCam = Camera.open();
				initCamera(myCam);
				myCam.startPreview();
		        myCam.setPreviewDisplay(mySView.getHolder());
		        myCam.autoFocus(myAutoFocusCallback);
			} 
		}
		catch(Exception e){
			Log.e("Surface", "EEEEEEEEEEEEEEEEEE");
			e.printStackTrace();
		}
	}

	public Bitmap binarizationBitmap(Bitmap img) {
		int width = img.getWidth();
		int height = img.getHeight();
		int area = width * height;
		int gray[][] = new int[width][height];
		int average = 0;
		int graysum = 0;
		int graymean = 0;
		int grayfrontmean = 0;
		int graybackmean = 0;
		int pixelGray;
		int front = 0;
		int back = 0;
		int[] pix = new int[width * height];
		img.getPixels(pix, 0, width, 0, 0, width, height);
		for (int i = 1; i < width; i++) { 
			for (int j = 1; j < height; j++) {
				int x = j * width + i;
				int r = (pix[x] >> 16) & 0xff;
				int g = (pix[x] >> 8) & 0xff;
				int b = pix[x] & 0xff;
				pixelGray = (int) (0.3 * r + 0.59 * g + 0.11 * b);
				gray[i][j] = (pixelGray << 16) + (pixelGray << 8) + (pixelGray);
				graysum += pixelGray;
			}
		}
		graymean = (int) (graysum / area);
		average = graymean;
		Log.i("binarization","Average:"+average);
		for (int i = 0; i < width; i++) 
		{
			for (int j = 0; j < height; j++) {
				if (((gray[i][j]) & (0x0000ff)) < graymean) {
					graybackmean += ((gray[i][j]) & (0x0000ff));
					back++;
				} else {
					grayfrontmean += ((gray[i][j]) & (0x0000ff));
					front++;
				}
			}
		}
		int frontvalue = (int) (grayfrontmean / front);
		int backvalue = (int) (graybackmean / back);
		float G[] = new float[frontvalue - backvalue + 1];
		int s = 0;
		Log.i("binarization","Front:"+front+"**Frontvalue:"+frontvalue+"**Backvalue:"+backvalue);
		for (int i1 = backvalue; i1 < frontvalue + 1; i1++)
		{
			back = 0;
			front = 0;
			grayfrontmean = 0;
			graybackmean = 0;
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					if (((gray[i][j]) & (0x0000ff)) < (i1 + 1)) {
						graybackmean += ((gray[i][j]) & (0x0000ff));
						back++;
					} else {
						grayfrontmean += ((gray[i][j]) & (0x0000ff));
						front++;
					}
				}
			}
			grayfrontmean = (int) (grayfrontmean / front);
			graybackmean = (int) (graybackmean / back);
			G[s] = (((float) back / area) * (graybackmean - average)
					* (graybackmean - average) + ((float) front / area)
					* (grayfrontmean - average) * (grayfrontmean - average));
			s++;
		}
		float max = G[0];
		int index = 0;
		for (int i = 1; i < frontvalue - backvalue + 1; i++) {
			if (max < G[i]) {
				max = G[i];
				index = i;
			}
		}

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int in = j * width + i;
				if (((gray[i][j]) & (0x0000ff)) < (index + backvalue)) {
					pix[in] = Color.rgb(0, 0, 0);
				} else {
					pix[in] = Color.rgb(255, 255, 255);
				}
			}
		}
		
		Bitmap temp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		temp.setPixels(pix, 0, width, 0, 0, width, height);
		return temp;
    }

	@Override
	public void  surfaceDestroyed(SurfaceHolder holder){}	

	private void startProgressDialog() {
        if (pd == null) {
            pd = new ProgressDialog(this);
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.setMessage("loading ocr database...");
            pd.setCancelable(false);
        }
        pd.show();
    }
 
    private void stopProgressDialog() {
        if (pd != null) {
            pd.dismiss();
            pd = null;
        }
    }
    
    public class StartFrameTask extends AsyncTask<Integer, String, Integer> {
    	public Context cntxt  = null;
    	public String savePath= null;
    	public List<String> assetsNames =  new ArrayList<String>();
    	
    	public void copyFromAssets() {
    		if (cntxt != null && savePath != null && assetsNames  != null){
        		File dir = new File(savePath);
        		if (!dir.exists()) dir.mkdir();
        		List<String> copiedFiles = null;
        		int availableTotal = 0;
    			try{
           			copiedFiles = new ArrayList<String>();
        			for (String aName : assetsNames){
        				File f = new File(savePath + aName);
        				if ( !f.exists() ) {
        					InputStream is = cntxt.getResources().getAssets().open(aName);
        					availableTotal += is.available();
        					is.close();
        					copiedFiles.add(aName);
        				}
        			}
    			}
    			catch(Exception e){
    				Log.e("copyFromAssets", "calculate copy size exception.");
    				e.printStackTrace();
    				availableTotal = 0;
    				copiedFiles.clear();
        		}
        		try {
        			if (availableTotal > 0 && copiedFiles.size() > 0 ){
        				pd.setMax( availableTotal );
        				Log.w("copyFromAssets", "availableTotal:" + availableTotal);
        				int process = 0;
        				for (String aName : copiedFiles){
            				File f = new File(savePath + aName);
            				if ( !f.exists() ) {
            					InputStream is = cntxt.getResources().getAssets().open(aName);
            					FileOutputStream fos = new FileOutputStream(savePath + aName);
                				byte[] buffer = new byte[8192];   // 8K
                				int count = 0;
                				while ((count = is.read(buffer)) > 0) {
                					fos.write(buffer, 0, count);
                					process += count;
                					pd.setProgress(process);
                				}
                				fos.close();
                				is.close();
            				}
            			}
        			}
        			else{
        				Log.e("copyFromAssets", "availableTotal:" + availableTotal);
        				Log.e("copyFromAssets", "copiedFiles.size():" + copiedFiles.size());
        			}
        		} catch (Exception e) {
        			Log.e("copyFromAssets", "copy data exception.");
        			e.printStackTrace();
        		}	
    		}
    	}

        public StartFrameTask() { }
 
        @Override
        protected void onCancelled() {
            stopProgressDialog();
            super.onCancelled();
        }
 
        @Override
        protected Integer doInBackground(Integer... params) {
	        try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        	copyFromAssets();
            return null;
        }
 
        @Override
        protected void onPreExecute() {
            startProgressDialog();
        }
 
        @Override
        protected void onPostExecute(Integer result) {
            stopProgressDialog();
        }
 
    }
}
