package com.aw.scaner;

import android.content.Intent;
import android.app.Activity;
//import android.content.Intent;
import android.os.Bundle;
//import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class ResultTextActivity extends Activity {
    private String ocr_result_text = null;
    private TextView infoShow = null;
    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_result_text);
        
        Intent ntnt = getIntent();
        ocr_result_text = ntnt.getStringExtra("scan_vin_code");
        String txt = String.format("\n%s\n", ocr_result_text);        
        infoShow = (TextView)findViewById(R.id.ocr_ok_result);
        infoShow.setText(txt);
    }  

    @Override  
    protected void onPause() {  
        super.onPause();  
    }  

    @Override  
    protected void onResume() {  
        super.onResume();  
    }
    
      
    @Override  
    protected void onDestroy() {  
        
        super.onDestroy();  
    }
    
    public void onBtnReturn(View v){    	
    	//String tips = String.format(getResources().getString(R.string.publish_desc_tips), tire_replacement_shop_count);
    	//Toast.makeText(getApplicationContext(),  tips, Toast.LENGTH_LONG).show();
    	finish();
    }
}
