/*
 *  this class is a dialog that allows to choose file or directory
 *  
 */
package com.kivsw.dialog;
//-----------------------------------------------------------------

import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.kivsw.dialoglib.R;

import java.io.File;

//-----------------------------------------------------------------
public class FileDialog extends BaseDialog
						implements  FileListView.OnFileClick, 
									MessageDialog.OnCloseListener ,
									InputDialog.OnCloseListener,
									View.OnClickListener,
									OnCreateContextMenuListener,
									MenuItem.OnMenuItemClickListener, PopupMenu.OnMenuItemClickListener,
									OnKeyListener
									
{
	
	public enum TypeDialog {OPEN, SAVE, SELDIR}; // Action type of FileDialog

	public interface OnCloseListener  // interface allows to get result
	{
		void onClickOk(FileDialog dlg, String fileName);
		void onClickCancel(FileDialog dlg);
	};
	//-----------------------------------------------------------------
	TypeDialog td=null;
	String InitialDir=null, Mask=null;
	//OnCloseListener onCloseListener=null;
	Button buttonOk, buttonCancel;
	EditText editFile;
	TextView editPath,labelFile;

	LinearLayout fileNameLayout; 
	
	ListView fileList;
	FileListView fileListView;
    String resultName;
	
	//-----------------------------------------------------------------
    /**
     * Creates new instance of FileDialog
     * @param dlgId identifier of dialog
     * @param td dialog type
     * @param InitialDir current directory
     * @param Mask wildcard mask
     * @param onCloseListener listener of events
     * @return new instance of FileDialog
     */

	 public static FileDialog newInstance(int dlgId,TypeDialog td, String InitialDir, String Mask, OnCloseListener onCloseListener)
	 {
		 FileDialog dlg= new FileDialog();
		 if(td==null) td=TypeDialog.OPEN;
		 dlg.td=td;
		 dlg.InitialDir=InitialDir;
		 dlg.Mask=Mask;
		 dlg.setOnCloseListener(onCloseListener);
		 dlg.setDlgId(dlgId);
		 return dlg;
	 }

	//-----------------------------------------------------------------
	 /**
	  * 
	  */
	    @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) 
	    {
	    	super.onCreateView(inflater, container, savedInstanceState);
	    	// setStyle(STYLE_NO_FRAME, android.R.style.Theme_Holo_Light);
	    	//if(container==null) container=new ViewGroup();
	        View v = inflater.inflate(R.layout.filedialog, container, false);
	        
	        buttonOk = (Button)v.findViewById(R.id.dlFilebuttonOk);
			buttonCancel = (Button)v.findViewById(R.id.dlFilebuttonCancel);
			editPath = (TextView)v.findViewById(R.id.dlFileEditPath);
			labelFile = (TextView)v.findViewById(R.id.dlTextView2);
			editFile = (EditText)v.findViewById(R.id.dlFileEditFileName);
			//fileNameLayout = (ExLinearLayout)v.findViewById(R.id.fileNameLayout);
			fileNameLayout = (LinearLayout)v;
			
			fileListView = (FileListView)v.findViewById(R.id.FileList);
			
			buttonOk.setOnClickListener(this);
			buttonCancel.setOnClickListener(this);
			fileListView.setOnCreateContextMenuListener(this);
			
			fileListView.setOnFileClick(this);
			//fileListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

	        
			if(savedInstanceState!=null)
			{
				fileListView.setFilter(savedInstanceState.getString("mask"));
				fileListView.setPath(savedInstanceState.getString("path"));
				savedInstanceState.getInt("td",0);
				td=TypeDialog.values()[savedInstanceState.getInt("td",0)];
				
				resultName = savedInstanceState.getString("ResultName");
				
			}
			else 
			{
				if(Mask!=null && Mask.length()!=0) fileListView.setFilter(Mask);
				if(InitialDir==null || InitialDir.length()==0) InitialDir="/";
				fileListView.setPath(InitialDir);
				if(td==null) td=TypeDialog.OPEN;
			};
			
			
			switch(td)
			{
				case OPEN:
					getDialog().setTitle(getActivity().getText(R.string.open_file));
					break;
				case SAVE:
					getDialog().setTitle(getActivity().getText(R.string.save_file));
		    		break;
				case SELDIR:
					getDialog().setTitle(getActivity().getText(R.string.choose_directory));
					editFile.setVisibility( View.GONE);
					labelFile.setVisibility( View.GONE);
	                break;
			}
			
			//setHasOptionsMenu (true);
			 
	        return v;
	    }

	    //-------------------------------------------------------- 
	    public void onActivityCreated(Bundle savedInstanceState)
	    {   
	    	// fills full screen with this dialog
	        super.onActivityCreated(savedInstanceState);
	        Window window = getDialog().getWindow();
	        window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            getDialog().setOnKeyListener(this);
	    }
	  //-------------------------------------------------------------------
		void setOnCloseListener(OnCloseListener listener)
		{
			super.setOnCloseLister((Object)listener);
		};
		//-------------------------------------------------------------------
		OnCloseListener getOnCloseListener()
		{
			return (OnCloseListener)super.getOnCloseLister();
		};
	  //------------------------------------------------------------------
	    @Override
	    public void onResume()
	    {
	    	super.onResume();
	    	fileListView.update();
	    }
	  //------------------------------------------------------------------
		@Override
		public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
			if(event.getAction()==KeyEvent.ACTION_UP)
				switch(keyCode)
				{
				case KeyEvent.KEYCODE_MENU:
					PopupMenu menu=new PopupMenu(getActivity(),editFile);
					menu.inflate(R.menu.filelistmenu);
					menu.setOnMenuItemClickListener(this);
					menu.show();
					break;
				}
			return false;
		}
	  //------------------------------------------------------------------
	  @Override
      public void onCreateContextMenu(ContextMenu menu, View v,
	                                ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
	    /*menu.setHeaderTitle ("qqqq");
	    menu.setHeaderIcon(R.drawable.iconewfile);*/
	    MenuInflater inflater = getActivity().getMenuInflater();
	    inflater.inflate(R.menu.filelistcontextmenu, menu);
	    
 
	    for (int i = 0, n = menu.size(); i < n; i++)
	        menu.getItem(i).setOnMenuItemClickListener(this);
	    
	    
	  }
	  //------------------------------------------------------------------
	  @Override
	  public boolean onMenuItemClick(MenuItem item) 
	  {
		  int id=item.getItemId();
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        //if(info.position==-3) id=item.getItemId();

        if(id==R.id.itemcreatedirectory) // creates a new file
    	{
    		createDir();
    		return true;
    	}
    	else if(id==R.id.itemcreatefile) // creates a new file
    	{
    		createFile();
    		return true;
    	}
    	else if(id==R.id.itemdelete && info!=null) // deletes file or directory
    	{
    		deleteFile(fileListView.getFileList().get(info.position).name);
    		return true;
    	}
    	else if(id==R.id.itemrename && info!=null) // rename file or directory
    	{
    		renameFile(fileListView.getFileList().get(info.position).name);
    		return true;
    	}        
    	return false;
	  }

	  //--------------------------------------------------------
	    @Override
		public void onSaveInstanceState (Bundle outState)
		{
	    	super.onSaveInstanceState (outState);
			outState.putString("path", fileListView.getPath());
			outState.putString("mask", fileListView.getFilter());
			outState.putInt("td", td.ordinal());
			outState.putString("ResultName", resultName); 
		};
		//--------------------------------------------------------
		/** Handle when user press 'ok' or 'cancel' button
		 * 
		 */
		@Override
		public void onClick(View v) {
			
			resultName = null;
			if(v==buttonOk)
			{
				// check up whether entered name is a pattern
				if(isMask(editFile.getText().toString()))
				{
					if(fileListView.setFilter(editFile.getText().toString()))
					{
					  //onDirChange(fileListView,fileListView.getPath());
					  //editFile.getText().clear();
					  return;
					}
				}
					
				if(td==TypeDialog.SAVE) // for saving
				{
					if(editFile.getText().length()==0)
					{
						return;
					}
					resultName = fileListView.getPath()+editFile.getText().toString();
					
					File file = new File(resultName);
					if(file.exists()) // if the chosen file is exist
					{ // to demand user to confirm rewriting the file
						MessageDialog dlg=MessageDialog.newInstance(R.string.file_exists,getText(R.string.Confirmation).toString(),getText(R.string.file_exists).toString(),this);
						dlg.show(getActivity().getSupportFragmentManager(),"");
						return;
					}
				}
				else
				if(td==TypeDialog.OPEN) // for opening
				{
					if(editFile.getText().length()==0)
					{
						return;
					};
					resultName = fileListView.getPath()+editFile.getText().toString();
					File file = new File(resultName);
					if(!file.exists())
					{
						MessageDialog dlg=MessageDialog.newInstance(getText(R.string.Error).toString(),getText(R.string.file_doesnt_exist).toString());
						dlg.show(getActivity().getSupportFragmentManager(),"");
						return;
					}
				}
		    	else
				if(td==TypeDialog.SELDIR) // for select a directory
				{
					resultName = fileListView.getPath(); 
				};
				
				if(resultName!=null)
				{
					getOnCloseListener().onClickOk(this, resultName);
					dismiss();
				}
				
			}
			else
			{
				getOnCloseListener().onClickCancel(this);
				dismiss();
			}

		}
		//--------------------------------------------------------------------------
		// MessageDialog.OnCloseListener
		public void onClickOk(MessageDialog msg)
		{
			int dlgId=msg.getDlgId();

			if(dlgId==R.string.file_exists) // when the user confirmed re-writing the chosen file
			{
			   if(resultName==null) return;
			   if(getOnCloseListener()!=null)
			      getOnCloseListener().onClickOk(this, resultName);
			   dismiss();
			}
			else if(dlgId==R.string.doYouWantToDeleteFile)
				doDeleteFile(resultName);   // delete ResultName file
			
		};
		public void onClickCancel(MessageDialog msg){};
		public void onClickExtra(MessageDialog msg){};
		//--------------------------------------------------------------------------
		// InputDialog.OnCloseListener
			@Override
			public void onClickOk(InputDialog dlg,	String value, String initialValue)
			{
				int dlgId=dlg.getDlgId();
				if(dlgId==R.string.enter_dir_name) // creates a new directory
					doCreateDir(value);
				else if(dlgId==R.string.enter_file_name) // creates a new file
				{
					if(initialValue==null)
					    doCreateFile(value);
					else
						doRenameFile(initialValue, value);
				}


				
			}

			@Override
			public void onClickCancel(InputDialog dlg) {}

		//----------------------------------------------------------
		// Click on a file
		public void onFileClick(FileListView flf, FileAdapter.FileInfo fi)
		{
			editFile.setText(fi.name);
		}; 
		//----------------------------------------------------------
		// must return true to allow to change the directory.
		public boolean beforeDirChanged(FileListView flf, String newPath)
		{
			return true;
		};
		//----------------------------------------------------------
		public void onDirChanged(FileListView flf, String newPath)
		{
			//editPath.setText(newPath+fileListFragment.getFilter());
			editPath.setText(flf.getPath()+flf.getFilter());
	   	    editFile.setText("");
		}; 
		//----------------------------------------------------------
		public void onFileListChanged(FileListView flf)
		{
			
		}; 
		//----------------------------------------------------------
		// check up whether str is a mask
		boolean isMask(String str)
		{
	    	// str is a pattern in case it consists '*' or '?' symbols without the slash before
			int index;
			boolean r=false;
			String symbols="*?";
			for(int i=0;   !r && i<symbols.length();   i++)
			{
				index = str.lastIndexOf(symbols.charAt(i));
				if(index>=0)
				{
					if(index > 0)
					      r = str.charAt(index-1)!='\\';
		            else
						  r=true;
				}
			}
			
			return r;
		}
		//----------------------------------------------------------
		// creates a new dir
    	void createDir()
    	{
    		InputDialog dlg=
    		   InputDialog.newInstance(R.string.enter_dir_name,
    				                   getText(R.string.create_dir).toString(), getText(R.string.enter_dir_name).toString(),
    				                   null, android.text.InputType.TYPE_CLASS_TEXT, this   );
    		dlg.show(getActivity().getSupportFragmentManager(), "createdir");
    	}
        void doCreateDir(String value)
        {
        	String name=fileListView.getPath()+value;
			File f=new File(name);
			if(f.mkdir())
				fileListView.setPath(name); //update();
			else
			{
				MessageDialog.newInstance(getText(R.string.Error).toString(), getText(R.string.cant_create_dir).toString())
				  .show(getFragmentManager(),"");
			}
        }
    	//----------------------------------------------------------
    	// creates a new file
    	void createFile()
    	{
		   InputDialog.newInstance(R.string.enter_file_name,getText(R.string.create_file).toString(), getText(R.string.enter_file_name).toString(),
				                   null, android.text.InputType.TYPE_CLASS_TEXT, this)
		   .show(getActivity().getSupportFragmentManager(), "createdir");
    	};
    	void doCreateFile(String value)
    	{
			File f=new File(fileListView.getPath()+value);
			String errMsg="";
			boolean r=false;
			try{
				r=f.createNewFile();
			}catch(Exception e){
				errMsg = e.toString();
			};
			
			if(r)
				fileListView.update();
			else
			{
				MessageDialog d = 
				    MessageDialog.newInstance(getText(R.string.Error).toString(), getText(R.string.cant_create_file).toString()+errMsg);
				d.setTargetFragment(FileDialog.this, 112);
				d.show(getFragmentManager(),"");
			}	
    	}
    	//----------------------------------------------------------
    	// delete file or dir
    	void deleteFile(final String filename)
    	{
    		
    		resultName = filename;
    		String ask = String.format(getText(R.string.doYouWantToDeleteFile).toString(), filename);
    		
    		MessageDialog.newInstance(R.string.doYouWantToDeleteFile,getText(R.string.Confirmation).toString(), ask,this)
    		.show(getFragmentManager(),"");
    		
    	};
        void doDeleteFile(String filename)
        {
        	File f=new File(fileListView.getPath(), filename);
			if(f.delete())
				fileListView.update();
			else
			{
				MessageDialog.newInstance(getText(R.string.Error).toString(), getText(R.string.cant_delete_file).toString())
				  .show(getFragmentManager(),"");
			}
        }
    	//----------------------------------------------------------
    	// rename file or dir
    	void renameFile(final String filename)
    	{
    		InputDialog.newInstance(R.string.enter_file_name,getText(R.string.rename).toString(), getText(R.string.enter_file_name).toString(),
	                   filename, android.text.InputType.TYPE_CLASS_TEXT, this)
			.show(getActivity().getSupportFragmentManager(), "createdir");
    	};
    	//----------------------------------------------------------
		void doRenameFile(String fileName, String newFileName)
		{
			File newf=new File(fileListView.getPath()+newFileName);
			File f=new File(fileListView.getPath()+fileName);

			if(f.renameTo(newf))
				fileListView.update();
			else
			{
				MessageDialog.newInstance(getText(R.string.Error).toString(), getText(R.string.cant_rename_file).toString())
				  .show(getFragmentManager(),"");
			}
		}
			
		
    	
}
