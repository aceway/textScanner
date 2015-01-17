package com.aw.scaner;

import android.annotation.SuppressLint;
import java.util.Map;
import java.util.HashMap;  
import java.util.Calendar;

public class VINManager {
	private String myStrVIN = null;
	private String myErrorDesc = "no error.";
	private String myStrCarInfo = "";
	
	@SuppressLint("UseSparseArrays") private Map<Integer, Integer> myMapWeight = new HashMap<Integer, Integer>();
	private Map<Character, Integer> myMapValue  = new HashMap<Character, Integer>();
	private String myStrYearMap  = "ABCDEFGHJKLMNPRSTVWXY123456789";
	
	public VINManager() {initVINCodeMapInfo();}
	
	public VINManager(String strVIN) {
		myStrVIN = strVIN.toUpperCase();
		initVINCodeMapInfo();
	}
	
	private void initVINCodeMapInfo(){		
		//No 9
		myMapWeight.clear(); 
		myMapWeight.put(1, 8);myMapWeight.put(2, 7);myMapWeight.put(3, 6);myMapWeight.put(4, 5);myMapWeight.put(5, 4);myMapWeight.put(6, 3);myMapWeight.put(7, 2);myMapWeight.put(8, 10);		
		myMapWeight.put(10, 9);myMapWeight.put(11, 8);myMapWeight.put(12, 7);myMapWeight.put(13, 6);myMapWeight.put(14, 5);myMapWeight.put(15, 4);myMapWeight.put(16, 3);myMapWeight.put(17, 2);
		
		//No I, O, Q
		myMapValue.clear();
		myMapValue.put('A', 1);	myMapValue.put('B', 2);	myMapValue.put('C', 3); myMapValue.put('D', 4);myMapValue.put('E', 5);myMapValue.put('F', 6);myMapValue.put('G', 7);myMapValue.put('H', 8);	myMapValue.put('J', 1);	myMapValue.put('K', 2);
		myMapValue.put('L', 3);	myMapValue.put('M', 4);	myMapValue.put('N', 5);	myMapValue.put('P', 7);myMapValue.put('R', 9);myMapValue.put('S', 2);myMapValue.put('T', 3);myMapValue.put('U', 4);	myMapValue.put('V', 5);	myMapValue.put('W', 6);	
		myMapValue.put('X', 7);	myMapValue.put('Y', 8);	myMapValue.put('Z', 9);
		myMapValue.put('0', 0); myMapValue.put('1', 1); myMapValue.put('2', 2); myMapValue.put('3', 3);myMapValue.put('4', 4);myMapValue.put('5', 5);myMapValue.put('6', 6);myMapValue.put('7', 7); myMapValue.put('8', 8);	myMapValue.put('9', 9);
	}
	
	public void SetStrVIN(String strVIN){ myStrVIN = strVIN.toUpperCase(); }
	
	public String GetErrorDesc(){ return myErrorDesc; }
	
	public String GetCardInfo(){
		myStrCarInfo = this.GetGeneratedYear() + "年生产的汽车。";
		return myStrCarInfo;
	}
	
	public boolean CheckVIN(){
		 if ( myStrVIN == null) return false;
		 if ( myStrVIN.length() != 17 ){
			 myErrorDesc = "VIN length must be 17.";
			 return false;
		 }
		 Character checkCode =  myStrVIN.charAt(8) ; 
		 if ( checkCode != (Character)'X' && !Character.isDigit(checkCode) ){
			 myErrorDesc = "VIN check code error, it is at postion 9, must be 0-9 or X.";
		 }
		 Integer caclCode = 0;
		 for(int idx = 0; idx < myStrVIN.length(); idx +=1){
			 if(idx == 8) continue;
			 Character c =  myStrVIN.charAt(idx);				 
			 if( c == (Character)'I' || c == (Character)'O' || c == (Character)'Q' ){
				 myErrorDesc = "Character I, O, Q should not be in VIN.";
				 return false;
			 }
			 caclCode += myMapWeight.get(idx+1) * myMapValue.get(c);
		 }
		 caclCode %= 11;
		 if ( checkCode == (Character)'X' && caclCode == (Integer)10 ){
			 myErrorDesc = "no error. check VIN format OK.";
			 return true;
		 }
		 if ( Integer.parseInt(checkCode.toString()) ==  caclCode ) {
			 myErrorDesc = "no error. check VIN format OK.";
			 return true;
		 }
		 myErrorDesc = "VIN check code error. " + checkCode + " != " + caclCode;
		 return false;
	 }

	public void ParseVINInfo(){
		pasreWMIInfo();
		parseVDSInfo();
		pasreVISInfo();
	}
	
	private void pasreWMIInfo(){

	}
	
	private void parseVDSInfo(){

	}
	
	private void pasreVISInfo(){

	}
	
	public int GetGeneratedYear(){
		char yChar = myStrVIN.charAt(9);
		int carYear = myStrYearMap.indexOf(yChar) + 1980;
		if (carYear < 1980){
			myErrorDesc = "Year code error at index 10. ";
			return 0;
		}
		int thisYear = Calendar.getInstance().get(Calendar.YEAR); 
		while ( (thisYear - carYear) > 30) carYear += 30;		
		return carYear;
	}
}
