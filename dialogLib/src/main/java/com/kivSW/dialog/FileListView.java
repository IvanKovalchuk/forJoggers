/*
* This Fragment is intended to show the file list of a directory
*/

package com.kivSW.dialog;

//import android.app.Activity;
//import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.ListFragment;
import java.util.ArrayList;

import com.kivSW.dialog.FileAdapter.FileInfo;

import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.content.Context;
//import android.database.DataSetObserver;

public class FileListView extends ListView 
                              implements AdapterView.OnItemClickListener            
{

	// interface for notification of directory change and a clicked file 
    interface OnFileClick
    {
    	void onFileClick(FileListView flf, FileAdapter.FileInfo fi); 
    	boolean beforeDirChanged(FileListView flf,String newPath); // must return true to allow to change the directory.
    	void onDirChanged(FileListView flf,String newPath); 
    	void onFileListChanged(FileListView flf); 
    };
    
    private FileAdapter fileAdapter; // 
    private OnFileClick  onFileClick=null;
    
	//-------------------------------------------------------
	public FileListView(Context context) {
		super(context);
		init();
	}
	public  FileListView(Context context, AttributeSet attrs)
	{
		super(context,attrs);
		init();
	}
	public  FileListView(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context,attrs, defStyleAttr);
		init();	
	}
	private void init()
	{
		fileAdapter = new FileAdapter(getContext());
		setAdapter(fileAdapter);

		setItemsCanFocus(true);
		setOnItemClickListener(this);	
	}
	//--------------------------------------------------------
	// Changes the current directory
	public boolean setPath(String NewPath)
	{
		if((onFileClick==null) || onFileClick.beforeDirChanged(this,NewPath))
		{
		  if(doSetPath(NewPath))
		  {
		    if((onFileClick!=null)) onFileClick.onDirChanged(this,fileAdapter.getPath());
		    return true;
		  }
		}
		return false;
	};
	protected boolean doSetPath(String Path)
	{
		return fileAdapter.setPath(Path);
	};
	//----------------------------------------
	// undates its file list
	public void update()
	{
		fileAdapter.updateFileList();
		if((onFileClick!=null)) onFileClick.onFileListChanged(this);
	}
	//--------------------------------------------------------
	public ArrayList<FileInfo>  getFileList()
	{
		return fileAdapter.getFileList();
	}
	//--------------------------------------------------------
	// returns the current directory
	String getPath()
	{
		return fileAdapter.getPath();
	};
	//--------------------------------------------------------
	// this method adjusts the file name filter
	boolean setFilter(String filters)
	{
		boolean r= fileAdapter.setFilter(filters);
		if((onFileClick!=null)) onFileClick.onFileListChanged(this);
		return r;
	};
	//--------------------------------------------------------
	// this method returns the actual file filter 
	String getFilter()
	{
		return fileAdapter.getFilter();
	};
	//--------------------------------------------------------
	void setOnFileClick(OnFileClick ocf)
	{
		onFileClick = ocf;
	};
	//--------------------------------------------------------
	// implements AdapterView.OnItemClickListener
	// Processes a click on a file or directory
	 public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	 {
		 FileAdapter.FileInfo fi=(FileAdapter.FileInfo)fileAdapter.getItem(position);
		 
	     if(fi.isDir)
	     {
	    	 String NewPath="", SelDir=null;
	    	 if(0==fi.name.compareTo(".."))  // go to the upper directory
	    	 {
	    		 String Path = getPath();
	    		 int i=0;
	    		 i=Path.lastIndexOf("/",Path.length()-2);
	    		 if(i>=0)
	    			    NewPath = Path.substring(0,i);

	    		 SelDir = Path.substring(i+1, Path.length()-1);
	    	 }
	    	 else
	    		 NewPath=getPath()+fi.name; // go to the chosen directory
	    	 
	    	 if(setPath(NewPath))
	    	 {
	    		 if(SelDir!=null && SelDir.length()>0)
		    	 {
		    		 int p= fileAdapter.getDirPosition(SelDir);
		    		 if(p>=0) 
		    			 setSelection(p);
		    	 }
	    		 else
	    			 setSelection(0);
	    	 }
	    	 /*if((onFileClick==null) || onFileClick.beforeDirChange(this,NewPath))
	    	 {
		    	 doSetPath(NewPath);
		    	 if((onFileClick!=null)) onFileClick.onDirChange(this,fileAdapter.getPath());
		    	 
		    	 if(SelDir!=null && SelDir.length()>0)
		    	 {
		    		 int p= fileAdapter.getDirPosition(SelDir);
		    		 if(p>=0) 
		    			 setSelection(p);
		    	 };
	    	 }*/
	     }
	     else
	     {
	    	 if(onFileClick!=null) onFileClick.onFileClick(this,fi);
	     }
		 
	 }
	 //----------------------------------------------------------------------------------

}
