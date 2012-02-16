package com.devwebsphere.wxsutils.filter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.InputSource;

import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.io.XsDataInputStream;
import com.ibm.websphere.objectgrid.io.XsDataOutputStream;
import com.ibm.websphere.objectgrid.plugins.io.DataSerializer;
import com.ibm.websphere.objectgrid.plugins.io.KeySerializerPlugin;
import com.ibm.websphere.objectgrid.plugins.io.MapSerializerPlugin;
import com.ibm.websphere.objectgrid.plugins.io.ValueSerializerPlugin;
import com.ibm.websphere.objectgrid.plugins.io.datadescriptor.Association;
import com.ibm.websphere.objectgrid.plugins.io.datadescriptor.Attribute;
import com.ibm.websphere.objectgrid.plugins.io.datadescriptor.AttributeType;
import com.ibm.websphere.objectgrid.plugins.io.datadescriptor.DataDescriptorFactory;
import com.ibm.websphere.objectgrid.plugins.io.datadescriptor.MapDataDescriptor;
import com.ibm.websphere.objectgrid.plugins.io.datadescriptor.ValueDataDescriptor;
import com.ibm.websphere.objectgrid.plugins.io.dataobject.DataObjectContext;

public class PersonMapSerializer implements MapSerializerPlugin {

	static class PersonMapDataDescriptor implements MapDataDescriptor {

		@Override
		public void initialize() {
		}

		@Override
		public boolean isInitialized() {
			return true;
		}

		@Override
		public void destroy() {
		}

		@Override
		public boolean isDestroyed() {
			return false;
		}

		@Override
		public String getAddressableKeyName() {
			return MapDataDescriptor.DEFAULT_ADDRESSABLEKEYNAME;
		}

		@Override
		public Map<String, Association> getAssociations() {
			return Collections.emptyMap();
		}

		@Override
		public void setAddressableKeyName(String arg0) {
		}

		@Override
		public void setAssociations(Map<String, Association> arg0) {
		}
	}

	static class PersonValueSerializer implements ValueSerializerPlugin {
		BackingMap bmap;
		ValueDataDescriptor vdd;

		public PersonValueSerializer() {
			DataDescriptorFactory ddf = DataDescriptorFactory.instance();
			HashMap<String, Attribute> attrs = new HashMap<String, Attribute>(3);
			attrs.put("firstName", ddf.createAttribute("firstName", AttributeType.STRING, null, false));
			attrs.put("lastName", ddf.createAttribute("lastName", AttributeType.STRING, null, false));
			attrs.put("age", ddf.createAttribute("age", AttributeType.DOUBLE, null, false));
			vdd = ddf.createValueDataDescriptor(attrs, "/");
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
			Map<String, Attribute> attrs = getValueDataDescriptor().getAttributes();
			Attribute[] ctxs = new Attribute[arg0.length];
			for (int i = 0; i < arg0.length; ++i) {
				ctxs[i] = attrs.get(arg0[i]);
			}
			return ctxs;
		}

		@Override
		public Object[] inflateDataObjectAttributes(DataObjectContext arg0, XsDataInputStream arg1, Object arg2) throws IOException {
			Attribute[] attrs = (Attribute[]) arg2;
			Object[] vals = new Object[attrs.length];

			InputSource is = new InputSource(new ByteArrayInputStream(arg1.toByteArray()));

			for (int i = 0; i < attrs.length; ++i) {
				Attribute attr = attrs[i];
				QName resultType = XPathConstants.STRING;
				if (attr.getAttributeType() == AttributeType.DOUBLE) {
					resultType = XPathConstants.NUMBER;
				}
				Object o = null;
				try {
					o = XPathFactory.newInstance().newXPath().evaluate("/person/" + attr.getAttributeName() + "/text()", is, resultType);
				} catch (XPathExpressionException e) {
					e.printStackTrace();
				}
				if (o == null) {
					o = DataSerializer.SpecialValue.NOT_FOUND;
				}
				vals[i] = o;
			}

			return vals;
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

		@Override
		public ValueDataDescriptor getValueDataDescriptor() {
			return vdd;
		}
	}

	BackingMap bmap;

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
		return bmap != null;
	}

	@Override
	public void destroy() {
		bmap = null;
	}

	@Override
	public boolean isDestroyed() {
		return bmap == null;
	}

	@Override
	public KeySerializerPlugin getKeySerializerPlugin() {
		return null;
	}

	@Override
	public MapDataDescriptor getMapDataDescriptor() {
		return new PersonMapDataDescriptor();
	}

	@Override
	public ValueSerializerPlugin getValueSerializerPlugin() {
		return new PersonValueSerializer();
	}

}
