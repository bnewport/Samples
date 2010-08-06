package com.devwebsphere.wxs.fs;

class GridInputStreamState
{
	long currentBucket;
	long currentAbsolutePosition;
	int currentPosition;
	byte[] currentValue;
	
	public GridInputStreamState()
	{
		currentAbsolutePosition = 0;
		currentBucket = 0;
		currentValue = null;
	}
}