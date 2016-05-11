/*
* Class that shows a messageDialog window
* 
*  WARNING!!! it's not desirable to invoke DialogFragment.show(...) from  OnCloseListener's methods
*             because Z-order of these DialogFragments could be violated when the phone is rotated.
*/
package com.kivsw.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.kivsw.dialoglib.R;


public class MessageDialog extends BaseDialog implements OnClickListener
{
	/*public interface OnOkListener
	{
		void onClickOk(MessageDialog msg);
	};	
	public interface OnCloseListener
	{
		void onClickOk(MessageDialog msg);
		void onClickCancel(MessageDialog msg);
	};*/
	public interface OnCloseListener  
	{
		void onClickOk(MessageDialog msg);
		void onClickCancel(MessageDialog msg);
		void onClickExtra(MessageDialog msg);
	};


	private boolean doDismiss; // allow to dismiss dialog after any button was pressed 
	Button okBtn= null, cancelBtn= null, extraBtn= null;
	TextView tv;
	CheckBox checkBoxDontShowAgain=null;
	//---------------------------------------------------------------------
	// creates a message dialog, that holds only "ok" button
	public static MessageDialog newInstance(String title, String msg)
	{
		//android.R.string.error_message_title;
		return  newInstance(-1,title, msg, false, null, "", null, null);
	}

	//---------------------------------------------------------------------
	// creates a message dialog instance, that holds "ok" and "cancel" buttons
	public static MessageDialog newInstance(int dlgId,String title, String msg, final OnCloseListener listener)
	{
		return newInstance(dlgId,title, msg, false, listener, "", "", null);
	}
	// creates a message dialog instance, that holds "ok" and "cancel" buttons
	public static MessageDialog newInstance(int dlgId,String title, String msg, boolean askDontShowAgain, final OnCloseListener listener)
	{
		return newInstance(dlgId,title, msg, askDontShowAgain, listener, "", "", null);
	}
	//---------------------------------------------------------------------
	// creates a message dialog instance, that may hold any 3 buttons
	// okTitle,cancelTitle,exTitle parameters are button's title:
	//     null value means that a button is invisible
	//     "" value means that button is visible and has its default title.
	//     another values entitle appropriate button
	public static MessageDialog newInstance(int dlgId,String title, String msg, boolean askDontShowAgain, OnCloseListener listener, String okTitle, String cancelTitle, String exTitle)
	{
		MessageDialog Instance=new MessageDialog();
		
        Bundle args = new Bundle();
        args.putString("message",msg);
        args.putString("title",title);
        args.putInt("dlgId", dlgId);
		args.putBoolean("askDontShowAgain",askDontShowAgain);
        
        args.putString("okbtn",okTitle);
        args.putString("cancelbtn",cancelTitle);
        args.putString("extrabtn",exTitle);
        
       
        Instance.setListener(listener);
        Instance.setArguments(args);

        return Instance;

	}
	//-------------------------------------------------------------------
	// prevent dismissing this dialog after any button was pressed
	// it should be invoked inside of OnCloseListener
	public void dontDismiss() 
	{doDismiss=false;}
	//-------------------------------------------------------------------
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
    	LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	View v=inflater.inflate(R.layout.messagedialog, null);
    	
    	okBtn= (Button)v.findViewById(R.id.dlButtonOk);
        cancelBtn=(Button)v.findViewById(R.id.dlButtonCancel);
        extraBtn=(Button)v.findViewById(R.id.dlButtonExtra);
        okBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        extraBtn.setOnClickListener(this);
        
        tv = (TextView)v.findViewById(R.id.dlMessageTextView);
        tv.setText(Html.fromHtml(getArguments().getString("message")));
    	
    	String okTitle=getArguments().getString("okbtn");
    	String cancelTitle=getArguments().getString("cancelbtn");
    	String extraTitle=getArguments().getString("extrabtn");
    	
    	if(okTitle!=null)
    	{
    		if(okTitle.length()==0) okTitle=getText(android.R.string.ok).toString();
    		okBtn.setText(okTitle);
    		okBtn.setVisibility(View.VISIBLE);
    	}
    	else okBtn.setVisibility(View.GONE);

    	if(cancelTitle!=null)
    	{
    		if(cancelTitle.length()==0) cancelTitle=getText(android.R.string.cancel).toString();
    		cancelBtn.setText(cancelTitle);
    		cancelBtn.setVisibility(View.VISIBLE);
    	}
    	else cancelBtn.setVisibility(View.GONE);
    	
    	if(extraTitle!=null)
    	{
    		if(extraTitle.length()==0) extraTitle=getText(android.R.string.unknownName).toString();
    		extraBtn.setText(extraTitle);
    		extraBtn.setVisibility(View.VISIBLE);
    	}
    	else extraBtn.setVisibility(View.GONE);

		checkBoxDontShowAgain = (CheckBox)v.findViewById(R.id.checkBoxDontShowAgain);
		if(!getArguments().getBoolean("askDontShowAgain"))
		  checkBoxDontShowAgain.setVisibility(View.GONE);
		else checkBoxDontShowAgain.setVisibility(View.VISIBLE);
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    	
    	//builder.setIcon(R.drawable.ic_launcher);
    	builder.setTitle(getArguments().getString("title"));
    	builder.setView(v);
    	
    	setDlgId(getArguments().getInt("dlgId"));
    	
    	return builder.create();
	}
    //-------------------------------------------------------------------
	public boolean getDontShowAgain()
	{
		return checkBoxDontShowAgain.isChecked();
	}

	//-------------------------------------------------------------------
	void setListener(OnCloseListener listener)
	{
		super.setOnCloseLister((Object)listener);
	};
	//-------------------------------------------------------------------
	OnCloseListener getListener()
	{
		return (OnCloseListener)super.getOnCloseLister();
	};
	//-------------------------------------------------------------------
	public void onCancel (DialogInterface dialog)
	{
		super.onCancel(dialog);
		if(getListener()!=null) getListener().onClickCancel(this);
	}
	//-------------------------------------------------------------------
	/*public void onClick(DialogInterface dialog, int id) {
		
		if(listener!=null)
		{
		  if(id==DialogInterface.BUTTON_POSITIVE) // positive button
			  listener.onClickOk(this);
		  else 
		  if(id==DialogInterface.BUTTON_NEGATIVE) // positive button
			  listener.onClickCancel(this);
		}

    }*/
	//-------------------------------------------------------------------
	@Override
	public void onClick(View v) 
	{
        doDismiss=true;
		if(getListener()!=null) 
		{
			int id = v.getId();
			if (id == R.id.dlButtonOk) {
				getListener().onClickOk(this);
			} else if (id == R.id.dlButtonCancel) {
				getListener().onClickCancel(this);
			} else if (id == R.id.dlButtonExtra) {
				getListener().onClickExtra(this);
			}
			
		}
		if(doDismiss)
		{
		  setListener(null); // set to null in order to prevent to invoke onClickCancel in onCancel method
		  dismiss();
		}

	}

}
