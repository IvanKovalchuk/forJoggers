package com.kivsw.dialog;
/**
 * This class is a dialog (DialogFragment) that is meant to input a value
 * this class is not reusable, so you should create a new instance when you need to show this dialog
 */
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.kivsw.dialoglib.R;

public class InputDialog extends BaseDialog 
implements OnClickListener
{
	
	public interface OnCloseListener  // interface allows to get result
	{
		void onClickOk(InputDialog dlg, String newValue, String initialValue);
		void onClickCancel(InputDialog dlg);
	};
	
	
	Button okBtn,cancelBtn;
	TextView textView;
	EditText editText;

    public static InputDialog newInstance(int dlgId, String title, String msg, String value, int inputType, final OnCloseListener listener)
	{
        InputDialog instance=new InputDialog();
        initNewInstance(instance,  dlgId,  title,  msg,  value,  inputType, listener);
        return instance;

	}
	//-------------------------------------------------------------
	protected static void initNewInstance(InputDialog instance, int dlgId, String title, String msg, String value, int inputType, final OnCloseListener listener)
	{
		instance.setOnCloseListener(listener);
		
        Bundle args = new Bundle();
        args.putString("title",title);
        args.putString("msg",msg);
        args.putString("value",value);
        args.putInt("inputType", inputType);
        args.putInt("dlgId",dlgId);
        instance.setArguments(args);


	}
	//-------------------------------------------------------------
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
    	LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	View v=inflater.inflate(R.layout.inputdialog, null);
    	
    	okBtn= (Button)v.findViewById(R.id.dlButtonOk);
		okBtn.setOnClickListener(this);
		okBtn.setText(android.R.string.ok);
        cancelBtn=(Button)v.findViewById(R.id.dlButtonCancel);
		cancelBtn.setOnClickListener(this);
		cancelBtn.setText(android.R.string.cancel);

        editText=(EditText)v.findViewById(R.id.dlEditValue);
        
        editText.setInputType(this.getArguments().getInt("inputType"));
        editText.setText(this.getArguments().getString("value"));
		editText.requestFocus();
		editText.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
		editText.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
        //getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

		textView=(TextView)v.findViewById(R.id.dlInputValTextView);
		textView.setText(this.getArguments().getString("msg"));

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

    	builder.setTitle(getArguments().getString("title"));
    	builder.setView(v);
    	
    	setDlgId(getArguments().getInt("dlgId"));

        return builder.create();
	}
	//-------------------------------------------------------------------

	public void onCancel (DialogInterface dialog)
	{
		super.onCancel(dialog);
		if(getOnCloseListener()!=null) getOnCloseListener().onClickCancel(this);
	}
	//-------------------------------------------------------------------
	protected void setOnCloseListener(OnCloseListener listener)
	{
		super.setOnCloseLister((Object) listener);
	};
	//-------------------------------------------------------------------
	OnCloseListener getOnCloseListener()
	{
		return (OnCloseListener)super.getOnCloseLister();
	};
	//---------------------------------------------------------------
	@Override
	public void onClick(View v) {
		int id=v.getId();
		if(id==R.id.dlButtonOk)
		{
            doOk();
		}
		else if(id==R.id.dlButtonCancel)
		{
            doCancel();
		}
		setOnCloseListener( null);// set to null in order to prevent to invoke onClickCancel in onCancel method
		dismiss();
	}

    /**
     * user presses Ok button
     */
    protected void doOk()
    {
        if(getOnCloseListener()!=null)
            getOnCloseListener().onClickOk(this,getValue(),getArguments().getString("value"));
    }
    /**
     * user presses Cancel button
     */
    protected void doCancel()
    {
        if(getOnCloseListener()!=null)
            getOnCloseListener().onClickCancel(this);
    }
    public String getValue()
    {
        return editText.getText().toString();
    }
}
