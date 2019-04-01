/*
 *  FileAdapter class is intended to supply FileListFragment with 
 *  a file list. Adapter gets the file list of a directory
 */
package com.kivsw.dialog;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.kivsw.dialoglib.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import java.util.Locale;

//---------------------------------------------------------
//---------------------------------------------------------
//---------------------------------------------------------
//
class FileAdapter extends Object implements ListAdapter
{
	//-------------------------------------------------------------------------------
	/**Class contains file information
	 * 
	 * @author ivan
	 *
	 */
	class FileInfo
	{
		String name;
		boolean isDir=false,isFile=false,isHidden=false;
		long size=0, Modified=0;
		
		protected  FileInfo()
		{
			
		}
		// class gets and holds file information
		public FileInfo(String path, String FN)
		{
			File f = new File(path, FN);
			isDir = f.isDirectory();
			isFile = f.isFile();
			isHidden = f.isHidden();
			name = FN;
			if(isFile)  size = f.length();
			else		size = 0;
			Modified = f.lastModified();
		}
	};
	/** creates FileInfor of ".."-directory
	 *  
	 * @return
	 */
	protected FileInfo createColon()
	{
		FileInfo fi=new FileInfo();
		fi.name = "..";
		fi.isDir=true;
		return fi;
	}
	//-------------------------------------------------------------------------------		
	private ArrayList<DataSetObserver> dataSetObservers;
	private String path;

	private boolean isAllowedDir=true, isAllowedFile=true, isAllowedHidden=true; // file type filter
	private ArrayList<Pattern> filters;  // file name filters
	private String usedWildCard="";



	private ArrayList<FileInfo> fileList=null; // null value means that it's necessary to rebuild fileList
	private Context context;
	//----------------------------------------
	/** Creates an entitle of FileAdapter
	 * @param cnt
	 */
	public FileAdapter(Context cnt)
	{
		context = cnt;
		filters = new ArrayList<Pattern>();
		dataSetObservers = new ArrayList<DataSetObserver>();
		setPath("/");
	}
	//----------------------------------------
	/** returns position number of the Dir in fileList
	 * @param Dir 
	 * @return position number of the Dir in fileList
	 */
	public int getDirPosition(String Dir) {
		
		int i;
		if(fileList==null) buildFileList();
		for(i=0;i<fileList.size();i++)
		{
			if(0==fileList.get(i).name.compareTo(Dir))
				return i;
		}
		return -1;
	
	}
	//----------------------------------------
	/**
	 * @return File list of the curent directory
	 */
	public ArrayList<FileInfo>  getFileList()
	{
		if(fileList==null) buildFileList();
		return fileList;
	};
	//----------------------------------------
	/**
	 * @return current path
	 */
	public String getPath()
	{
		return path;
	}
	//----------------------------------------
	/** this method gets the file list of the path
	 * 
	 */
	protected void buildFileList()
	{
		//Mtimer mtimer = new Mtimer();
		
		if(fileList==null)
			fileList = new ArrayList<FileInfo>();
		
		File f = new File(path);
		String[] items =	f.list(); // gets list of file

		fileList.clear();
		if(path!="/") fileList.add(createColon()); // add ".."-directory
		if(items!=null)
		{
			for(int i=0;i<items.length;i++)
			{
				FileInfo fi=new FileInfo(path,items[i]);
				if(checkFilter(fi))	
					fileList.add(fi);
			}
		}
		else
		{ 
			/*MessageDialog msg=MessageDialog.getInstance(context.getText(R.string.Warning).toString(), 
					                  context.getText(R.string.Access_is_impossible ).toString(), null);
			FragmentActivity fa= ((FragmentActivity)context);
			FragmentManager fm=fa.getSupportFragmentManager();
			msg.show(((FragmentActivity)context).getSupportFragmentManager(), "");*/
			
			Toast t=Toast.makeText(context, R.string.Access_is_impossible , Toast.LENGTH_LONG);
			t.setGravity(Gravity.CENTER, 0, 0);
			t.show();
		}
			
		Collections.sort(fileList, new Comparator<FileInfo>()
		{
			@Override
			public int compare(FileInfo lhs, FileInfo rhs) {

				int r=0;
				if(lhs.isDir==rhs.isDir)
				    r= lhs.name.compareToIgnoreCase(rhs.name);
				else
				{
					if(lhs.isDir) r=-1;
					else r=1;
				}
				return r;
			}
		});
		
	}
	//----------------------------------------
	/** this method forces to rebuild the file list
	 * 
	 */
	public void updateFileList()
	{
		if(fileList!=null)	fileList.clear();
		fileList=null;
		
		for(int i=0;i<dataSetObservers.size();i++)
		{
			DataSetObserver o=dataSetObservers.get(i);
			if(o!=null)	o.onChanged();
		};
	}
	//----------------------------------------
	/** this method changes the current path
	 * 
	 * @param newPath - a new path
	 * @return true if the current path has been changed
	 */
	public boolean setPath(String newPath)
	{
		if(newPath.isEmpty())
			newPath="/";
		
		File f = new File(newPath);
		if(!f.isDirectory())
			return false;

		path = newPath;
		if( path.charAt(path.length()-1) != '/')
			path+='/';
		
		updateFileList();

		return true;
	}

	//----------------------------------------
	/** this method sets the filter
	 * 
	 * @param isAllowedDir boolean value that allow to show directories
	 * @param isAllowedFile boolean value that allow to show files
	 * @param isAllowedHidden boolean value that allow to show hidden files and directories
	 */
	void setFilter(boolean isAllowedDir, boolean isAllowedFile, boolean isAllowedHidden)
	{
		this.isAllowedDir=isAllowedDir;
		this.isAllowedFile=isAllowedFile;
		this.isAllowedHidden=isAllowedHidden;
		
		updateFileList(); // rebuild the file list
	}
	//----------------------------------------
	/** This method sets the file name filter.
	 *  wildCard can hold a number of filters separated by ';'. null or "" value cancels any filter
	 * @param wildCard
	 * @return
	 */
	boolean setFilter(String wildCard)
	{
		boolean r=true;
		filters.clear();
		usedWildCard="";
		
		// translate wildCard into the corresponded Regular Expression
		
		try{
			if(wildCard!=null && !wildCard.isEmpty() && !wildCard.equals("*"))
			{
				int e,b;
				b=e=wildCard.length();
				while(e>0)
				{
				   b=wildCard.lastIndexOf (';', b-1);
				   if(b>0 && wildCard.charAt(b-1)=='\\') // ommit "\\;" sequence
					   continue;
				   String str=wildCard.substring(b+1, e);
				   if(str.length()>0) // ommit empty strings
				   {
					   str="^"+Pattern.quote(str)+"$"; // screen possible special symbols
					   str=str.replace("\\;", ";");    
		               str=str.replace("*", "\\E.*\\Q"); // convert masks into the appropriate regular expressions
		               str=str.replace("?", "\\E.{1}\\Q");
			           filters.add(Pattern.compile(str));
				   }
				   e=b;
				}
				
	        	usedWildCard = wildCard;
	  		};

		}
		catch(Exception e)
		{
			filters.clear();
			r=false;
		};
  		
		updateFileList(); // rebuild the file list
		return r;
	}
	//----------------------------------------
	/** this method returns the file filters
	 *  
	 * @return
	 */
	String getFilter()
	{
		return usedWildCard;
	}
	//----------------------------------------
	/** function checks whether file 'fi' should be shown
	 *  
	 * @param fi
	 * @return true if the filter allows this file fi
	 */
	boolean checkFilter(FileInfo fi)
	{
		boolean r=true;
		
		// check up file type
		if(fi.isDir)    r = r && isAllowedDir;
		if(!fi.isDir) 	r = r && isAllowedFile;
		if(fi.isHidden) r = r && isAllowedHidden;
		
		// check up file mask, if it's necessary
		if(r && (!filters.isEmpty()) && (!fi.isDir))
		{
			boolean rr=false;
			for(int i=filters.size()-1; !rr && i>=0;  i--)
			{
				Matcher matcher = filters.get(i).matcher(fi.name);
		        rr|=matcher.find();
			}
			r=rr;
		}

		return r;
		
	}
	//----------------------------------------
	/**  return amount of the files in fileList
	 *  @return 
	 */
	public int getCount() {
		
		if(fileList==null) buildFileList();
		return fileList.size();
	}

	//----------------------------------------
	public Object getItem(int position) {
		
		if((position<0)||(position>=getCount()))
		   return null;

		if(fileList==null) buildFileList();
		return fileList.get(position);
	
	}

	//----------------------------------------
	public long getItemId(int position) {
		
		return position;
	}

	//----------------------------------------
	public int getItemViewType(int position) {
		
    return IGNORE_ITEM_VIEW_TYPE;
	}

	//----------------------------------------
	/** return and create (if necessary) the view of a file
	 *   
	 */
	public View getView(int position, View convertView, ViewGroup parent) {

		String s;

		if(convertView==null)
		{
			convertView = View.inflate(context, R.layout.file_item, null);
		};

		TextView fileName, fileInfo;
		ImageView image;
		fileName = (TextView) convertView.findViewById(R.id.textViewFileName);
    	fileInfo = (TextView) convertView.findViewById(R.id.textViewFileInfo);
		image = (ImageView) convertView.findViewById(R.id.imageView);

		if(fileList==null) buildFileList();
		
		if((position>=0)&&(position<getCount()))
		{
			FileInfo fi=fileList.get(position);
	       	
	       	// choose file/dir picture
	       	if(fi.isDir)
	       	{
	       		if((position==0) && 0==fi.name.compareTo("..")) image.setImageResource(R.drawable.icodirup);
	       		else image.setImageResource(R.drawable.icodir);
	       	}
	       	else
	       	if(fi.isFile)  image.setImageResource(R.drawable.icofile);
	       	else image.setImageResource(R.drawable.icospecial);
	       	
			// file name
			fileName.setText(fi.name);
	       	
	       	if(!fi.isDir)
	       	{
	         	// the file size
		       	if(fi.size<1024) 
		       		s=(String.format("%4db ",fi.size));
		       	else if(fi.size<1024*1024) 
		       		s=(String.format("%4dk ",fi.size/1024));
		       	else if(fi.size<1024*1024*1024) 
		       		s=(String.format("%4dM ",fi.size/(1024*1024))); 
		       	else if(fi.size<1024*1024*1024*1024) 
		       		s=(String.format("%4dG ",fi.size/(1024*1024*1024))); 
		       	else
		       		s=(String.format("%4dT ",fi.size/(1024*1024*1024*1024))); 

				fileInfo.setText(s);
	       	}
	       	else
	       	if(!fi.isDir || !fi.name.equals(".."))
	       	{
		       	// date and time when the file was modified 
		       	s = String.format("%tF\n%tT", fi.Modified, fi.Modified);
		       	//s = s+String.format("\n%tT\n%tF", fi.Modified, fi.Modified);
				fileInfo.setText(s);


	       	}
			else
				fileInfo.setText("");
		}
		
		return convertView;
	}

	//----------------------------------------
	public int getViewTypeCount() {
		
		return 1;
	}

	//----------------------------------------
	public boolean hasStableIds() {
		
		return true;
	}

	//----------------------------------------
	public boolean isEmpty() {
		
		return getCount()==0;
	}

	//----------------------------------------
	public void registerDataSetObserver(DataSetObserver observer) 
	{
		int i= dataSetObservers.indexOf(observer);
		if(i<0)
		{
		    dataSetObservers.add(observer);
		    observer.onChanged();
		}
		
	}

	//----------------------------------------
	public void unregisterDataSetObserver(DataSetObserver observer) {
		
		dataSetObservers.remove(observer);
	}

	//----------------------------------------
	public boolean areAllItemsEnabled() {

		return true;
	}

	//----------------------------------------
	public boolean isEnabled(int position) {

		return true;
	}
	
}
//-------------------------------------------------------------------------------
//-------------------------------------------------------------------------------

