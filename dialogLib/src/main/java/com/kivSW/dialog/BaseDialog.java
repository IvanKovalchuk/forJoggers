/*
 *  Base class of all the dialog in this library.
 *   It holds, saves and restore button's listener
 *   this work properly only when the listener is FragmentActivity or Fragment
 */
 
package com.kivSW.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

public class BaseDialog extends DialogFragment {
	enum TypeListener {SimpleListener, ActivityListener, FragmentListener };
	private Object listener=null;
	TypeListener typeListener=null;
	int dlgId=0;
	//------------------------------------
	/** @return current listener
	 */
	public Object getOnCloseLister()
	{
		return listener;
	};
	//------------------------------------
	/**
	 *  Sets up a new button's listener.
	 * @param listener is desirable to be FragmentActivity or Fragment which holds this dialog
	 */
	public void setOnCloseLister(Object listener)
	{
		if(listener instanceof Fragment)
	    {
	     	this.listener = listener;
	     	this.setTargetFragment((Fragment)listener, getId());
	     	typeListener = TypeListener.FragmentListener;

	    }
	    else if(listener instanceof FragmentActivity)
	    {
        	this.listener = listener;
        	this.setTargetFragment(null, 0);
        	typeListener = TypeListener.ActivityListener;
        }      
        else
        {  // this listener can't be saved further
        	this.listener = listener;
        	typeListener = TypeListener.SimpleListener;
        }
	}
	//-------------------------------------------------------------------
	/**
	 * Sets the identifier of this dialog 
	 * @param id 
	 */
	public void setDlgId(int id)
	{
		this.dlgId =id;  
	}
	//-------------------------------------------------------------------
	/**
	 *  returns the identifier of this dialog 
	 * @return the identifier
	 */
	public int getDlgId()
	{
		return dlgId;
	}
	//-------------------------------------------------------------------
	/** restore listener in case it's FragmentActivity or Fragment
	 * 
	 */
	@Override
	public void onCreate (Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if(savedInstanceState!=null)
		{
			dlgId=savedInstanceState.getInt("dlgId",dlgId);

			int i=savedInstanceState.getInt("typeListener",TypeListener.SimpleListener.ordinal());
			typeListener = TypeListener.values()[i];
			switch(typeListener)
			{
			  case  ActivityListener:
				  listener = getActivity();
				  break;
			  case  FragmentListener:
				  listener = getTargetFragment();
				  break;
			  default:
				  listener = null;
			}
		}
	}
	//-------------------------------------------------------------------
	/** save listener
	 * 
	 */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(typeListener!=null)
            outState.putInt("typeListener", typeListener.ordinal());
        outState.putInt("dlgId",dlgId);
    }


}
