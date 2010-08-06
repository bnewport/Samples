package com.devwebsphere.wxs.fs;


public class ThreadLocalInputStreamState extends ThreadLocal<GridInputStreamState>{

	@Override
	protected GridInputStreamState initialValue() {
		GridInputStreamState d = new GridInputStreamState();
		return d;
	}
}
