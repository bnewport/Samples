//
//This sample program is provided AS IS and may be used, executed, copied and
//modified without royalty payment by customer (a) for its own instruction and 
//study, (b) in order to develop applications designed to run with an IBM 
//WebSphere product, either for customer's own internal use or for redistribution 
//by customer, as part of such an application, in customer's own products. "
//
//5724-J34 (C) COPYRIGHT International Business Machines Corp. 2005
//All Rights Reserved * Licensed Materials - Property of IBM
//
package com.devwebsphere.wxssearch;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

/**
 * byte[] isn't usable as a key in hash maps as the equals method doesn't work. This wraps one to make it usable as a
 * key.
 * 
 */
@Deprecated
public class ByteArrayKey implements Externalizable, Comparable<ByteArrayKey> {

	public ByteArrayKey() {

	}

	public ByteArrayKey(byte[] b) {
		this.b = b;
	}

	@Override
	public boolean equals(Object obj) {
		ByteArrayKey other = (ByteArrayKey) obj;
		return Arrays.equals(b, other.b);
	}

	byte[] b;

	@Override
	public int hashCode() {
		return Arrays.hashCode(b);
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		int sz = in.read();
		b = new byte[sz];
		in.readFully(b);
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.write(b.length);
		out.write(b);
	}

	public byte[] getBytes() {
		return b;
	}
	
	public int compareTo(ByteArrayKey o) {
		byte[] v1 = b;
		byte[] v2 = o.b;
		int len1 = v1.length;
		int len2 = v2.length;
		int n = Math.min(len1, len2);

		int k = 0;
		int lim = n;
		while (k < lim) {
			byte c1 = v1[k];
			byte c2 = v2[k];
			if (c1 != c2) {
				return c1 - c2;
			}
			k++;
		}
		return len1 - len2;
	}

}
