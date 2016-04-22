package com.kivsw.forjoggers.model;

/**
 * Created by ivan on 4/22/16.
 */
public class DataModel {

    static DataModel dataModel=null;
    static public  DataModel getInstance()
    {
        if(dataModel==null)
            dataModel = new DataModel();
        return dataModel;
    }
}
