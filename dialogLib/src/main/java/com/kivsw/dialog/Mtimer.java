package com.kivsw.dialog;

import android.os.SystemClock;

public class Mtimer {

	private long t;
	public Mtimer()
	{
		storageTime();
	}
	public void storageTime()
	{
		 t= SystemClock.elapsedRealtime();
	}
	
	// returns time (milliseconds) elapsed since storageTime()was invoked
	public long currentTime()
	{
		return (SystemClock.elapsedRealtime()-t) ;
	}
}
