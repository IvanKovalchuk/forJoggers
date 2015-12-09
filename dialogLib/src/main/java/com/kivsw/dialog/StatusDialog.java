/*
* Class that shows a message without any button.
* if informs the user that an operation is executing.
*/
package com.kivsw.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kivsw.dialoglib.R;

public class StatusDialog extends DialogFragment {
	
	TextView text=null, exText=null;
	//-----------------------------------------------------
	public static StatusDialog getInstance(String msg)
	{
		StatusDialog dlg = new StatusDialog();
		Bundle args = new Bundle();
        args.putString("message", msg);
        dlg.setArguments(args);
        dlg.setCancelable(false);
       // dlg.setStyle (STYLE_NO_TITLE, 0);
        return dlg;
	};
	//-----------------------------------------------------
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	 {
	        super.onCreateView(inflater, container, savedInstanceState);
	        

	        View v = inflater.inflate(R.layout.statusdialog, container, false);
	        text = (TextView)v.findViewById(R.id.dlStatusDialogText);
	        exText = (TextView)v.findViewById(R.id.dlStatusDialogExText);
	        getDialog().setTitle(R.string.Wait);
	        /*text.setMinWidth (getActivity().getWindow().getDecorView().getWidth()/2);
	        text.setMinHeight(getActivity().getWindow().getDecorView().getHeight()/2);*/
	        
	        if(savedInstanceState==null)
	        {
	        	savedInstanceState = getArguments();
	        };
	        text.setText(savedInstanceState.getString("message"));
	        //new ViewGroup.LayoutParams (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	        
	        return v;
	 };
	//-----------------------------------------------------
	@Override
	public void onSaveInstanceState(Bundle outState) {
	        super.onSaveInstanceState(outState);
	        
	        outState.putString("message", text.getText().toString());
	 }
	
	//-----------------------------------------------------
	public void setExText(String s)
	{
		if(s!=null && s.length()>0)
		{
		  exText.setText(s);
		  exText.setVisibility(View.VISIBLE);
		}
		else
		{
		  exText.setText("");
		  exText.setVisibility(View.GONE);
		}
			
	};
}
