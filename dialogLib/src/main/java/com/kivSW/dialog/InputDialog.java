package com.kivSW.dialog;

import com.kivSW.dialoglib.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View.OnClickListener;

public class InputDialog extends BaseDialog 
implements OnClickListener
{
	
	public interface OnCloseListener  // interface allows to get result
	{
		void onClickOk(InputDialog dlg, String value, String initialValue);
		void onClickCancel(InputDialog dlg);
	};
	
	
	Button okBtn,cancelBtn;
	TextView textView;
	EditText editText;
	
	//-------------------------------------------------------------
	public static InputDialog newInstance(int dlgId, String title, String msg, String value, int inputType, final OnCloseListener listener)
	{
		InputDialog Instance=new InputDialog();
		Instance.setOnCloseListener(listener);
		
        Bundle args = new Bundle();
        args.putString("title",title);
        args.putString("msg",msg);
        args.putString("value",value);
        args.putInt("inputType", inputType);
        args.putInt("dlgId",dlgId);
        Instance.setArguments(args);

        return Instance;
	}
	//-------------------------------------------------------------
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
    	LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	View v=inflater.inflate(R.layout.inputdialog, null);
    	
    	okBtn= (Button)v.findViewById(R.id.dlButtonOk);
        cancelBtn=(Button)v.findViewById(R.id.dlButtonCancel);
        textView=(TextView)v.findViewById(R.id.dlInputValTextView);
        editText=(EditText)v.findViewById(R.id.dlEditValue);
        
        okBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        
        editText.setInputType(this.getArguments().getInt("inputType"));
        editText.setText(this.getArguments().getString("value"));
        textView.setText(this.getArguments().getString("msg"));
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
	void setOnCloseListener(OnCloseListener listener)
	{
		super.setOnCloseLister((Object)listener);
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
			if(getOnCloseListener()!=null)
				getOnCloseListener().onClickOk(this,editText.getText().toString(),getArguments().getString("value"));
		}
		else if(id==R.id.dlButtonCancel)
		{
			if(getOnCloseListener()!=null) getOnCloseListener().onClickCancel(this);
		}
		setOnCloseListener( null);// set to null in order to prevent to invoke onClickCancel in onCancel method
		dismiss();
		
	}
}
