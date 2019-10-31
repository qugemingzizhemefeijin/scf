package cg.zz.scf.server.core.conver;

import cg.zz.scf.server.util.JsonHelper;

/**
 * json序列化解析器
 * @author chengang
 *
 */
public class JsonConvert implements IConvert {

	@Override
	public Boolean convertToBoolean(Object obj) {
		return new Boolean(obj.toString());
	}

	@Override
	public Byte convertToByte(Object obj) {
		return new Byte(obj.toString());
	}

	@Override
	public Character convertToCharacter(Object obj) {
		String str = obj.toString();
		if(str.length() > 1) {
			str = str.replaceFirst("\"", "");
		}
		if(!str.equals(null) && !str.equals("")){
			return new Character(str.charAt(0));
		}
		return new Character('\0');
	}

	@Override
	public Double convertToDouble(Object obj) {
		return new Double(obj.toString());
	}

	@Override
	public Float convertToFloat(Object obj) {
		return new Float(obj.toString());
	}

	@Override
	public Integer convertToInteger(Object obj) {
		return new Integer(obj.toString());
	}

	@Override
	public Long convertToLong(Object obj) {
		return new Long(obj.toString());
	}

	@Override
	public Short convertToShort(Object obj) {
		return new Short(obj.toString());
	}

	@Override
	public String convertToString(Object obj) {
		if(obj == null) {
			return "";
		}
		return obj.toString();
	}

	@Override
	public boolean convertToboolean(Object obj) {
		return Boolean.parseBoolean(obj.toString());
	}

	@Override
	public byte convertTobyte(Object obj) {
		return Byte.parseByte(obj.toString());
	}

	@Override
	public char convertTochar(Object obj) {
		String str = obj.toString();
		if(str.length() > 1) {
			str = str.replaceFirst("\"", "");
		}
		if(!str.equals(null) && !str.equals("")){
			return str.charAt(0);
		}
		return '\0';
	}

	@Override
	public double convertTodouble(Object obj) {
		return Double.parseDouble(obj.toString());
	}

	@Override
	public float convertTofloat(Object obj) {
		return Float.parseFloat(obj.toString());
	}

	@Override
	public int convertToint(Object obj) {
		return Integer.parseInt(obj.toString());
	}

	@Override
	public long convertTolong(Object obj) {
		return Long.parseLong(obj.toString());
	}

	@Override
	public short convertToshort(Object obj) {
		return Short.parseShort(obj.toString());
	}

	@Override
	public Object convertToT(Object obj, Class<?> clazz) throws Exception {
		if(obj == null || obj.toString().equalsIgnoreCase("")) {
			return null;
		}
		return JsonHelper.toJava(obj.toString(), clazz);
	}
	
	@Override
	public Object convertToT(Object obj, Class<?> containClass, Class<?> itemClass) throws Exception {
		if(obj == null || obj.toString().equalsIgnoreCase("")) {
			return null;
		}
		return JsonHelper.toJava(obj.toString(), containClass, itemClass);
	}

}
