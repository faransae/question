package com.sangsang.beyondtportal;


public class PaintItem {
	 
    private String[] mData;
    
    public PaintItem(String version, String number){
        
        mData = new String[2];
        mData[0] = version;
        mData[1] = number;
    }
    
    public String[] getData(){
        return mData;
    }
    
    public String getData(int index){
        return mData[index];
    }
    
    public void setData(String[] data){
        mData = data;
    }
    
    
}
