package cg.zz.scf.server.core.conver;

/**
 * java序列化解析器
 * @author chengang
 *
 */
public class JavaConvert implements IConvert {

	@Override
	public String convertToString(Object obj) {
		if(obj == null) {
			return "";
		}
		return obj.toString();
	}
	
	@Override
	public int convertToint(Object obj) {
		return new Integer(obj.toString()).intValue();
	}
	
	@Override
	public Integer convertToInteger(Object obj) {
		return new Integer(obj.toString());
	}
	
	@Override
	public long convertTolong(Object obj) {
		return new Long(obj.toString()).longValue();
	}
	
	@Override
	public Long convertToLong(Object obj) {
		return new Long(obj.toString());
	}

	@Override
	public short convertToshort(Object obj) {
		return new Short(obj.toString()).shortValue();
	}
	
	@Override
	public Short convertToShort(Object obj) {
		return new Short(obj.toString());
	}

	@Override
	public float convertTofloat(Object obj) {
		return new Float(obj.toString()).floatValue();
	}
	
	@Override
	public Float convertToFloat(Object obj) {
		return new Float(obj.toString());
	}

	@Override
	public boolean convertToboolean(Object obj) {
		return new Boolean(obj.toString()).booleanValue();
	}
	
	@Override
	public Boolean convertToBoolean(Object obj) {
		return new Boolean(obj.toString());
	}
	
	@Override
	public double convertTodouble(Object obj) {
		return new Double(obj.toString()).doubleValue();
	}
	
	@Override
	public Double convertToDouble(Object obj) {
		return new Double(obj.toString());
	}
	
	@Override
	public byte convertTobyte(Object obj) {
		return new Byte(obj.toString()).byteValue();
	}
	
	@Override
	public Byte convertToByte(Object obj) {
		return new Byte(obj.toString());
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
	public Object convertToT(Object obj, Class<?> clazz) throws Exception {
		return obj;
	}

	@Override
	public Object convertToT(Object obj, Class<?> containClass, Class<?> itemClass) throws Exception {
		return obj;
	}

}
