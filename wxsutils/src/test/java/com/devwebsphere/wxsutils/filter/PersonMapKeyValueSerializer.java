package com.devwebsphere.wxsutils.filter;

import java.io.IOException;
import java.util.HashMap;

import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.io.XsDataInputStream;
import com.ibm.websphere.objectgrid.io.XsDataOutputStream;
import com.ibm.websphere.objectgrid.plugins.io.KeySerializerPlugin;
import com.ibm.websphere.objectgrid.plugins.io.datadescriptor.Attribute;
import com.ibm.websphere.objectgrid.plugins.io.datadescriptor.AttributeType;
import com.ibm.websphere.objectgrid.plugins.io.datadescriptor.DataDescriptorFactory;
import com.ibm.websphere.objectgrid.plugins.io.datadescriptor.KeyDataDescriptor;
import com.ibm.websphere.objectgrid.plugins.io.dataobject.DataObjectContext;
import com.ibm.ws.xs.jdk5.java.util.Arrays;

public class PersonMapKeyValueSerializer extends PersonMapValueSerializer {

	static class PersonKeySerializer implements KeySerializerPlugin {
		BackingMap bmap;
		KeyDataDescriptor kdd;

		public PersonKeySerializer() {
			DataDescriptorFactory ddf = DataDescriptorFactory.instance();
			HashMap<String, Attribute> attrs = new HashMap<String, Attribute>(3);
			attrs.put("firstName", ddf.createAttribute("firstName", AttributeType.STRING, null, false));
			attrs.put("lastName", ddf.createAttribute("lastName", AttributeType.STRING, null, false));
			kdd = ddf.createKeyDataDescriptor(attrs, "/");
		}

		@Override
		public KeyDataDescriptor getKeyDataDescriptor() {
			return kdd;
		}

		@Override
		public Object inflateDataObject(DataObjectContext arg0, XsDataInputStream arg1) throws IOException {
			return arg1.toByteArray();
		}

		@Override
		public void serializeDataObject(DataObjectContext arg0, Object arg1, XsDataOutputStream arg2) throws IOException {
			arg2.write((byte[]) arg1);
		}

		@Override
		public Object getAttributeContexts(String... arg0) {
			return PersonMapKeyValueSerializer.getAttributeContexts(getKeyDataDescriptor(), arg0);
		}

		@Override
		public Object[] inflateDataObjectAttributes(DataObjectContext arg0, XsDataInputStream arg1, Object arg2) throws IOException {
			Attribute[] attrs = (Attribute[]) arg2;
			return inflateDataObjectAttributes(arg0, arg1, attrs);
		}

		@Override
		public boolean equals(DataObjectContext arg0, XsDataInputStream arg1, XsDataInputStream arg2) throws IOException {
			return Arrays.equals(arg1.toByteArray(), arg2.toByteArray());
		}

		@Override
		public boolean hasBinaryEquality() {
			return true;
		}

		@Override
		public int hashCode(DataObjectContext arg0, XsDataInputStream arg1) throws IOException {
			return Arrays.hashCode(arg1.toByteArray());
		}

		@Override
		public BackingMap getBackingMap() {
			return bmap;
		}

		@Override
		public void setBackingMap(BackingMap arg0) {
			bmap = arg0;
		}

		@Override
		public void initialize() {
		}

		@Override
		public boolean isInitialized() {
			return true;
		}

		@Override
		public void destroy() {
			bmap = null;
		}

		@Override
		public boolean isDestroyed() {
			return bmap == null;
		}

	}

	@Override
	public KeySerializerPlugin getKeySerializerPlugin() {
		return new PersonKeySerializer();
	}
}
