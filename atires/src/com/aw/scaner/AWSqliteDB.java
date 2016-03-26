package com.aw.scaner;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class AWSqliteDB extends SQLiteOpenHelper {
	private final static String VIN_DB_PATH = "";
	private final static String VIN_DB_NAME = "vin.sqlite3";
	private final static int VIN_DB_VERSION = 1;
	
	private final static String VIN_DATA_TABLE = "t_vin_data";
	private final static String[] VIN_DATA_COLUMNS = new String[] { "id", "vin", "remark" };
	
	private final static String WMI_TABLE = "t_wmi";
	private final static String[] WMI_COLUMNS = new String[] { "id", "code", "name", "remark" };
	private final static String WMI1_TABLE = "t_wmi1";
	private final static String[] WMI1_COLUMNS = new String[]{ "id", "code", "name", "remark" };
	private final static String WMI2_TABLE = "t_wmi";
	private final static String[] WMI2_COLUMNS = new String[]{ "id", "code", "name_en", "name_cn", "remark" };
	
	private final static String VDS_TABLE = "t_vds";
	private final static String[] VDS_COLUMNS = new String[] { "id", "belong_wmi", "code", "name", "remark" };
	
	private final static String VIS_TABLE = "t_vis";
	private final static String[] VIS_COLUMNS = new String[] { "id", "belong_wmi", "code", "name", "remark" };
	
	public class ClsVINInfo{
		int id = 0;
		String vin = "";
		int belong_wmi = 0;
		String code = "";
		String name_en = "";
		String name_cn = "";
		String remark = "";
	}

 	public AWSqliteDB(Context context) {
		super(context, VIN_DB_NAME, null, VIN_DB_VERSION);
		// TODO Auto-generated constructor stub
	}
	public AWSqliteDB(Context context, String name, CursorFactory factory,
			int version) {
		super(context, VIN_DB_NAME, factory, VIN_DB_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	public ClsVINInfo[] queryVINData(String strVIN){
		ClsVINInfo[] vinInfo = null;
		SQLiteDatabase db = null;
		try{
			String fullPathDBName = VIN_DB_PATH + VIN_DB_NAME;
			db = SQLiteDatabase.openDatabase(fullPathDBName, null, SQLiteDatabase.OPEN_READONLY );
			Cursor cur = db.query(VIN_DATA_TABLE,  VIN_DATA_COLUMNS, "code=?", new String[] { strVIN }, null, null, null);
			if ( cur.getCount() > 0){
				vinInfo = new ClsVINInfo[cur.getCount()];
				int idx = 0;
				while (cur.moveToNext()){
					vinInfo[idx].id 	= cur.getInt(0);
					vinInfo[idx].vin	= cur.getString(1);
					vinInfo[idx].remark = cur.getString(2);
				}
			}
		}
		catch (Exception e)
		{
			
		}
		finally{
			if ( db != null){
				db.close();
				db = null;
			}
		}
		return vinInfo;
	}
	
	public ClsVINInfo[] queryWMI(String strWMI){
		ClsVINInfo[] vinInfo = null;
		SQLiteDatabase db = null;
		try{
			String fullPathDBName = VIN_DB_PATH + VIN_DB_NAME;
			db = SQLiteDatabase.openDatabase(fullPathDBName, null, SQLiteDatabase.OPEN_READONLY );
			Cursor cur = db.query(WMI_TABLE,  WMI_COLUMNS, "code=?", new String[] {strWMI}, null, null, null);
			if ( cur.getCount() > 0){
				vinInfo = new ClsVINInfo[cur.getCount()];
				int idx = 0;
				while (cur.moveToNext()){
					vinInfo[idx].id 	= cur.getInt(0);
					vinInfo[idx].code 	= cur.getString(1);
					vinInfo[idx].name_en= cur.getString(2);
					vinInfo[idx].name_cn= cur.getString(3);
					vinInfo[idx].remark = cur.getString(4);
				}
			}
			else{
				cur = db.query(WMI1_TABLE,  WMI1_COLUMNS, "code=?", new String[] { strWMI.substring(0, 0) }, null, null, null);
				if ( cur.getCount() > 0){
					vinInfo = new ClsVINInfo[cur.getCount()];
					int idx = 0;
					while (cur.moveToNext()){
						vinInfo[idx].id 	= cur.getInt(0);
						vinInfo[idx].code 	= cur.getString(1);
						vinInfo[idx].name_cn= cur.getString(2);
						vinInfo[idx].remark = cur.getString(3);
					}
				}
				else{
					cur = db.query(WMI2_TABLE,  WMI2_COLUMNS, "code=?", new String[] { strWMI.substring(0, 0) }, null, null, null);
					if ( cur.getCount() > 0){
						vinInfo = new ClsVINInfo[cur.getCount()];
						int idx = 0;
						while (cur.moveToNext()){
							vinInfo[idx].id 	= cur.getInt(0);
							vinInfo[idx].code 	= cur.getString(1);
							vinInfo[idx].name_cn= cur.getString(2);
							vinInfo[idx].remark = cur.getString(3);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			
		}
		finally{
			if ( db != null){
				db.close();
				db = null;
			}
		}
		return vinInfo;
	}
	
	public ClsVINInfo[] queryWMI1(String strWMI1){
		ClsVINInfo[] vinInfo = null;
		SQLiteDatabase db = null;
		try{
			String fullPathDBName = VIN_DB_PATH + VIN_DB_NAME;
			db = SQLiteDatabase.openDatabase(fullPathDBName, null, SQLiteDatabase.OPEN_READONLY );
			Cursor cur = db.query(WMI1_TABLE,  WMI1_COLUMNS, "code=?", new String[] { strWMI1 }, null, null, null);
			if ( cur.getCount() > 0){
				vinInfo = new ClsVINInfo[cur.getCount()];
				int idx = 0;
				while (cur.moveToNext()){
					vinInfo[idx].id 	= cur.getInt(0);
					vinInfo[idx].code 	= cur.getString(1);
					vinInfo[idx].name_cn= cur.getString(2);
					vinInfo[idx].remark = cur.getString(3);
				}
			}
		}
		catch (Exception e)
		{
			
		}
		finally{
			if ( db != null){
				db.close();
				db = null;
			}
		}
		return vinInfo;
	}
	
	public ClsVINInfo[] queryWMI2(String strWMI2){
		ClsVINInfo[] vinInfo = null;
		SQLiteDatabase db = null;
		try{
			String fullPathDBName = VIN_DB_PATH + VIN_DB_NAME;
			db = SQLiteDatabase.openDatabase(fullPathDBName, null, SQLiteDatabase.OPEN_READONLY );
			Cursor cur = db.query(WMI2_TABLE,  WMI2_COLUMNS, "code=?", new String[] { strWMI2 }, null, null, null);
			if ( cur.getCount() > 0){
				vinInfo = new ClsVINInfo[cur.getCount()];
				int idx = 0;
				while (cur.moveToNext()){
					vinInfo[idx].id 	= cur.getInt(0);
					vinInfo[idx].code 	= cur.getString(1);
					vinInfo[idx].name_en= cur.getString(2);
					vinInfo[idx].name_cn= cur.getString(3);
					vinInfo[idx].remark = cur.getString(4);
				}
			}
		}
		catch (Exception e)
		{
			
		}
		finally{
			if ( db != null){
				db.close();
				db = null;
			}
		}
		return vinInfo;
	}
	
	public ClsVINInfo[] queryVDS(String strVDS){
		ClsVINInfo[] vinInfo = null;
		SQLiteDatabase db = null;
		try{
			String fullPathDBName = VIN_DB_PATH + VIN_DB_NAME;
			db = SQLiteDatabase.openDatabase(fullPathDBName, null, SQLiteDatabase.OPEN_READONLY );
			Cursor cur = db.query(VDS_TABLE,  VDS_COLUMNS, "code=?", new String[] { strVDS }, null, null, null);
			if ( cur.getCount() > 0){
				vinInfo = new ClsVINInfo[cur.getCount()];
				int idx = 0;
				while (cur.moveToNext()){
					vinInfo[idx].id 	= cur.getInt(0);
					vinInfo[idx].belong_wmi= cur.getInt(1);
					vinInfo[idx].code 	= cur.getString(2);
					vinInfo[idx].name_cn= cur.getString(3);
					vinInfo[idx].remark = cur.getString(4);
				}
			}
		}
		catch (Exception e)
		{
			
		}
		finally{
			if ( db != null){
				db.close();
				db = null;
			}
		}
		return vinInfo;
	}
	
	public ClsVINInfo[] queryVIS(String strVIS){
		ClsVINInfo[] vinInfo = null;
		SQLiteDatabase db = null;
		try{
			String fullPathDBName = VIN_DB_PATH + VIN_DB_NAME;
			db = SQLiteDatabase.openDatabase(fullPathDBName, null, SQLiteDatabase.OPEN_READONLY );
			Cursor cur = db.query(VIS_TABLE,  VIS_COLUMNS, "code=?", new String[] { strVIS }, null, null, null);
			if ( cur.getCount() > 0){
				vinInfo = new ClsVINInfo[cur.getCount()];
				int idx = 0;
				while (cur.moveToNext()){
					vinInfo[idx].id 	= cur.getInt(0);
					vinInfo[idx].belong_wmi= cur.getInt(1);
					vinInfo[idx].code 	= cur.getString(2);
					vinInfo[idx].name_cn= cur.getString(3);
					vinInfo[idx].remark = cur.getString(4);
				}
			}
		}
		catch (Exception e)
		{
			
		}
		finally{
			if ( db != null){
				db.close();
				db = null;
			}
		}
		return vinInfo;
	}
		
}
