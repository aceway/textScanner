package com.aw.scaner;

import android.util.DisplayMetrics;
import android.util.Log;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.PorterDuffXfermode; 
import android.graphics.PorterDuff; 
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.MotionEvent;

public class MuskSurfaceDraw extends SurfaceView implements SurfaceHolder.Callback{
    protected SurfaceHolder sfHolder = null;
    protected Rect rectScan= null;
    protected Rect topRect = null;
    protected Rect btmRect = null;
    protected Rect lftRect = null;
    protected Rect RgtRect = null;    
    protected Paint paintRect = null;
    protected Paint paintMask = null; 
    protected Paint paintEraser=null;  
    private float lineWidth = (float)3.0;
    private int canvasWidth = 0;
    private int canvasHight = 0;
    private int areaType = 0;
    private final int splitCntW = 32;
    private final int splitCntH = 32;
    private String screenOrientation = "portrait";
    //private final boolean bPortrait = false;
    
    @SuppressLint("ClickableViewAccessibility") @Override  
    public boolean onTouchEvent(MotionEvent event) {  
        Log.w("MuskSurfaceDraw", "Touching............");
        areaType += 1;
        areaType = areaType % 8;

        Canvas cvs = sfHolder.lockCanvas();
        if(cvs != null){
        	myDraw(cvs);
            sfHolder.unlockCanvasAndPost(cvs);	
        }
        return super.onTouchEvent(event);  
    }
   
    public MuskSurfaceDraw(Context context, AttributeSet attrs) {
        super(context, attrs);
        sfHolder = getHolder();
        if (sfHolder != null){
            sfHolder.addCallback(this);
            sfHolder.setFormat(PixelFormat.TRANSPARENT);
            setZOrderOnTop(true);	
        }
    }
   
    public void surfaceCreated(SurfaceHolder sfhd) {
    	Log.i("MuskSurfaceDraw", "MuskSurfaceDraw::surfaceCreated");
    	paintRect = new Paint();
    	paintRect.setAntiAlias(true);
    	paintRect.setColor(Color.GREEN);
    	paintRect.setStyle(Style.STROKE);
    	paintRect.setStrokeWidth(lineWidth);
    	
    	paintMask = new Paint();
    	paintMask.setAlpha(150);
    	
    	paintEraser = new Paint();
    	paintEraser.setAlpha(0);
    	paintEraser.setColor(Color.TRANSPARENT);
    	paintEraser.setMaskFilter(null);
    	paintEraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    	
    	if(sfhd != null){
    		Canvas cvs = sfhd.lockCanvas();
    		if (cvs != null){
    			myDraw(cvs);
            	sfhd.unlockCanvasAndPost(cvs);
    		}
    	}
    }
    
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int w, int h) {
    	if(arg0 != null){
    		Log.i("MuskSurfaceDraw", "surfaceChanged");
    		if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {    
                Log.i("surfaceChanged", "landscape");
                screenOrientation = "landscape";
            } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {    
                Log.i("MuskSurfaceDraw", "portrait");   
                screenOrientation = "portrait";
            }
            else{
            	Log.i("MuskSurfaceDraw", "Default set portrait");   
                screenOrientation = "portrait";
            }
    		Canvas cvs = arg0.lockCanvas();
    		if (cvs != null){
    			myDraw(cvs);
    	    	arg0.unlockCanvasAndPost(cvs);	
    		}
    	}
    }
    
    public void surfaceDestroyed(SurfaceHolder arg0) {
    }
 
    @SuppressLint("DefaultLocale") private Rect caclRect(int w, int h, String orientation){
    	if(orientation.toLowerCase().equals("portrait") && w > h){ //swap width and height;
    		int tmp = w;
    		w = h; h = tmp;
    	}
    	else if(orientation.toLowerCase().equals("landscape") && w < h){ //swap width and height;
    		int tmp = w;
    		w = h; h = tmp;
    	}
    	Rect rtTmp = new Rect(0,0,w,h);
    	float unitW = w / splitCntW;
    	float unitH = h / splitCntH;
    	switch(areaType){
    	case 0:
    		rtTmp.left = (int)unitW; 	rtTmp.right = (int)(unitW * (splitCntW-1));
        	rtTmp.top= (int)(5*unitH);	rtTmp.bottom = (int)(7*unitH);
    		break;
    	case 1:
    		rtTmp.left = (int)unitW; 	rtTmp.right = (int)(unitW * (splitCntW-1));
        	rtTmp.top= (int)(5*unitH);	rtTmp.bottom = (int)(8*unitH);
    		break;
    	case 2:
    		rtTmp.left = (int)unitW; 	rtTmp.right = (int)(unitW * (splitCntW-1));
        	rtTmp.top= (int)(3*unitH);	rtTmp.bottom = (int)(9*unitH);
    		break;
    	case 3:
    		rtTmp.left = (int)unitW; 	rtTmp.right = (int)(unitW * (splitCntW-1));
        	rtTmp.top= (int)(3*unitH);	rtTmp.bottom = (int)(15*unitH);
    		break;
    	case 4:
    		rtTmp.left = (int)unitW; 	rtTmp.right = (int)(unitW * (splitCntW-1));
        	rtTmp.top= (int)(2*unitH);	rtTmp.bottom = (int)(18*unitH);
    		break;
    	case 5:
    		rtTmp.left = (int)unitW; 	rtTmp.right = (int)(unitW * (splitCntW-1));
        	rtTmp.top= (int)(2*unitH);	rtTmp.bottom = (int)(20*unitH);
    		break;
    	case 6:
    		rtTmp.left = (int)unitW; 	rtTmp.right = (int)(unitW * (splitCntW-1));
        	rtTmp.top= (int)unitH;	rtTmp.bottom = (int)(23*unitH);
    		break;
    	case 7:
    		rtTmp.left = (int)unitW; 	rtTmp.right = (int)(unitW * (splitCntW-1));
        	rtTmp.top= (int)unitH;	rtTmp.bottom = (int)(25*unitH);
    		break;
    	default:
    		rtTmp.left = (int)unitW; 	rtTmp.right = (int)(unitW * (splitCntW-1));
        	rtTmp.top= (int)(5*unitH);	rtTmp.bottom = (int)(7*unitH);
    		break;
    	}
    	Log.i("caclRect", "Rect :" + rtTmp.width() + "W * " + rtTmp.height() + "H");
    	return rtTmp;
    }
    
    @SuppressLint("DefaultLocale") private void recaclMastRect(Canvas cvs){
    	canvasWidth = cvs.getHeight();
		canvasHight = cvs.getWidth();
		Log.i("MuskSurfaceDraw", "recaclMastRect: " + canvasWidth + " * " + canvasHight);
    	
    	DisplayMetrics dm = getResources().getDisplayMetrics();
    	if(canvasWidth == 0){
    		canvasWidth = dm.widthPixels;
    	}
    	if(canvasHight == 0)
    	{
    		canvasHight = dm.heightPixels;
    	}
    	
    	if(screenOrientation.toLowerCase().equals("portrait") && canvasWidth > canvasHight){ //swap width and height;
    		int tmp = canvasWidth;
    		canvasWidth = canvasHight; canvasHight = tmp;
    	}
    	else if(screenOrientation.toLowerCase().equals("landscape") && canvasWidth < canvasHight){ //swap width and height;
    		int tmp = canvasWidth;
    		canvasWidth = canvasHight; canvasHight = tmp;
    	}
    	rectScan = caclRect(canvasWidth, canvasHight, screenOrientation);
		topRect = new Rect(0, 				0, 					canvasWidth, 	rectScan.top);
		btmRect = new Rect(0, 				rectScan.bottom, 	canvasWidth, 	canvasHight);
		lftRect = new Rect(0, 				rectScan.top,		rectScan.left,	rectScan.bottom);
		RgtRect = new Rect(rectScan.right,	rectScan.top,		canvasWidth,	rectScan.bottom);
    }
    
    void clearDraw(Canvas cvs)
    {
    	Log.w("MuskSurfaceDraw", "clearDraw.......");
        if (cvs != null){
        	Rect rt = new Rect(0, 0, canvasWidth, canvasHight);
        	cvs.drawColor(Color.TRANSPARENT);
        	cvs.drawRect(rt, paintEraser);	
        	Log.i("MuskSurfaceDraw", "Clear area:" + rt.left + ", " + rt.top + ", " + rt.right + ", " + rt.bottom);
        }
    }
    
    private void myDraw(Canvas canvas){
    	Log.w("MuskSurfaceDraw", "Drawing.......");
    	if (canvas != null){
        	recaclMastRect(canvas);
        	clearDraw(canvas);
        	//drawScanRect(canvas);
        	drawMask(canvas);	
    	}
    }
    
    public void drawScanRect(Canvas cvs)
    {
    	cvs.drawColor(Color.TRANSPARENT);        
    	cvs.drawRect(rectScan, paintRect);
    	Log.i("MuskSurfaceDraw", "DRAW Rect postion:" + rectScan.left + ", " + rectScan.top + ", " + rectScan.right + ", " + rectScan.bottom);
    }
    
    public void drawMask(Canvas cvs){
    	cvs.drawColor(Color.TRANSPARENT);        
    	cvs.drawRect(topRect, paintMask);
    	cvs.drawRect(btmRect, paintMask);
    	cvs.drawRect(lftRect, paintMask);
    	cvs.drawRect(RgtRect, paintMask);
    }
    
    public Rect getScanRect(){ return rectScan; }    
}