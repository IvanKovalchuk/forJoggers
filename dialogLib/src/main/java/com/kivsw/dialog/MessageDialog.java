/**
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

	public interface OnCloseListener  
	{
		void onClickOk(MessageDialog msg);
		void onClickCancel(MessageDialog msg);
		void onClickExtra(MessageDialog msg);
	};


	private boolean doDismiss; // allow to dismiss dialog after any button was pressed
	private Button okBtn= null, cancelBtn= null, extraBtn= null;
	private TextView tv;
	private CheckBox checkBoxDontShowAgain=null;
	//---------------------------------------------------------------------

	/** creates a message dialog, that holds only "ok" button
	 *
	 * @param title
	 * @param msg
     * @return
     */
	public static MessageDialog newInstance(String title, String msg)
	{
		//android.R.string.error_message_title;
		return  newInstance(-1,title, msg, false, null, "", null, null);
	}

	//---------------------------------------------------------------------

	/** creates a message dialog instance, that holds "ok" and "cancel" buttons
	 *
	 * @param dlgId
	 * @param title
	 * @param msg
	 * @param listener
     * @return
     */
	public static MessageDialog newInstance(int dlgId,String title, String msg, final OnCloseListener listener)
	{
		return newInstance(dlgId,title, msg, false, listener, "", "", null);
	}

	/** creates a message dialog instance, that holds "ok" , "cancel" buttons
	 *   and may have "don't show again" checkbox
	 *
	 * @param dlgId
	 * @param title
	 * @param msg
	 * @param askDontShowAgain set it true if you need "don't show again" checkbox
	 * @param listener
     * @return
     */
	public static MessageDialog newInstance(int dlgId,String title, String msg, boolean askDontShowAgain, final OnCloseListener listener)
	{
		return newInstance(dlgId,title, msg, askDontShowAgain, listener, "", "", null);
	}
	//---------------------------------------------------------------------
	private final static String MESSAGE_PARAM="MESSAGE_PARAM",
	                            TITLE_PARAM="TITLE_PARAM",
	                            DIALOG_ID_PARAM="DIALOG_ID_PARAM",
	                            DONT_SHOW_AGAIN="DONT_SHOW_AGAIN",
	                            OK_TITLE_PARAM="OK_TITLE_PARAM",
	                            CANCEL_BTN_PARAM="CANCEL_BTN_PARAM",
	                            EXTRA_BTN_PARAM="EXTRA_BTN_PARAM";
	/** creates a message dialog instance, that may hold all 3 buttons.
	 *  @param  okTitle,
	 *  @param  cancelTitle,
	 *  @param exTitle These parameters are button's title:
	 *      null value means that a button is invisible
	 *      "" value means that button is visible and has its default title.
	 *      another values entitle appropriate button
	 *
	 *  @param askDontShowAgain enables "Don't show again" checkBox
	 */
	public static MessageDialog newInstance(int dlgId,String title, String msg, boolean askDontShowAgain, OnCloseListener listener, String okTitle, String cancelTitle, String exTitle)
	{
		MessageDialog Instance=new MessageDialog();
		
        Bundle args = new Bundle();
        args.putString(MESSAGE_PARAM,msg);
        args.putString(TITLE_PARAM,title);
        args.putInt(DIALOG_ID_PARAM, dlgId);
		args.putBoolean(DONT_SHOW_AGAIN,askDontShowAgain);
        
        args.putString(OK_TITLE_PARAM,okTitle);
        args.putString(CANCEL_BTN_PARAM,cancelTitle);
        args.putString(EXTRA_BTN_PARAM,exTitle);
        
       
        Instance.setListener(listener);
        Instance.setArguments(args);

        return Instance;

	}
	//-------------------------------------------------------------------
	/** prevent dismissing this dialog after any button was pressed
	 * it should be invoked inside of OnCloseListener
	 */

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
        tv.setText(Html.fromHtml(getArguments().getString(MESSAGE_PARAM)));
    	
    	String okTitle=getArguments().getString(OK_TITLE_PARAM);
    	String cancelTitle=getArguments().getString(CANCEL_BTN_PARAM);
    	String extraTitle=getArguments().getString(EXTRA_BTN_PARAM);
    	
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
		if(!getArguments().getBoolean(DONT_SHOW_AGAIN))
		  checkBoxDontShowAgain.setVisibility(View.GONE);
		else checkBoxDontShowAgain.setVisibility(View.VISIBLE);
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    	
    	builder.setTitle(getArguments().getString(TITLE_PARAM));
    	builder.setView(v);
    	
    	setDlgId(getArguments().getInt(DIALOG_ID_PARAM));
    	
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
