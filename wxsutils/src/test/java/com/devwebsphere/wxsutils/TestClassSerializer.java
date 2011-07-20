package com.devwebsphere.wxsutils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.devwebsphere.wxsutils.utils.ClassSerializer;

public class TestClassSerializer {
	@Test
	public void testSerializer() throws IOException {
		Map<String, Object> test = new HashMap<String, Object>();
		test.put("0", new Byte((byte) 0));
		test.put("1", new Integer(0));
		test.put("2", new Long(0));
		test.put("3", "String");
		test.put("4", new Float(1.2));
		test.put("5", new Double(2.3));
		test.put("6", new Timestamp(System.currentTimeMillis()));
		test.put("7", new Date(System.currentTimeMillis()));
		test.put("8", new ArrayList());
		test.put("9", new HashMap());
		test.put("10", new byte[2]);

		ClassSerializer serializer = new ClassSerializer();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream dos = new ObjectOutputStream(bos);
		serializer.writeObject(dos, test);
		dos.close();
		bos.close();
		byte[] rawBytes = bos.toByteArray();
		ByteArrayInputStream bis = new ByteArrayInputStream(rawBytes);
		ObjectInputStream ois = new ObjectInputStream(bis);
		Object o = serializer.readObject(ois);
		Map<String, Object> copy = (Map<String, Object>) o;
		for (int i = 0; i < test.size(); ++i) {
			String key = Integer.toString(i);
			Object v = test.get(key);
			Object v2 = copy.get(key);
			if (v instanceof byte[]) {
				Assert.assertTrue(Arrays.equals((byte[]) v, (byte[]) v2));
			} else
				Assert.assertEquals(v, v2);
		}
	}

	@Test
	public void testNulls() throws IOException {
		ClassSerializer serializer = new ClassSerializer();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream dos = new ObjectOutputStream(bos);
		serializer.writeList(dos, null);
		serializer.writeMap(dos, null);
		serializer.writeSet(dos, null);
		dos.close();
		byte[] rawBytes = bos.toByteArray();
		ByteArrayInputStream bis = new ByteArrayInputStream(rawBytes);
		ObjectInputStream ois = new ObjectInputStream(bis);
		Object o = serializer.readList(ois);
		Assert.assertEquals(null, o);
		o = serializer.readMap(ois);
		Assert.assertEquals(null, o);
		o = serializer.readSet(ois);
		Assert.assertEquals(null, o);
	}

}
