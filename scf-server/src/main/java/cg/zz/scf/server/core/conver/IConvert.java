package cg.zz.scf.server.core.conver;

/**
 * 所有的数据包序列化解析器均需实现此接口
 * @author chengang
 *
 */
public interface IConvert {
	
	/**
	 * 解析Object为String类型
	 * @param obj - Object
	 * @return String
	 */
	public abstract String convertToString(Object obj);

	/**
	 * 解析Object为int类型
	 * @param obj - Object
	 * @return int
	 */
	public abstract int convertToint(Object obj);

	/**
	 * 解析Object为Integer类型
	 * @param obj - Object
	 * @return Integer
	 */
	public abstract Integer convertToInteger(Object obj);

	/**
	 * 解析Object为long类型
	 * @param obj - Object
	 * @return long
	 */
	public abstract long convertTolong(Object obj);

	/**
	 * 解析Object为Long类型
	 * @param obj - Object
	 * @return Long
	 */
	public abstract Long convertToLong(Object obj);

	/**
	 * 解析Object为short类型
	 * @param obj - Object
	 * @return short
	 */
	public abstract short convertToshort(Object obj);

	/**
	 * 解析Object为Short类型
	 * @param obj - Object
	 * @return Short
	 */
	public abstract Short convertToShort(Object obj);

	/**
	 * 解析Object为float类型
	 * @param obj - Object
	 * @return float
	 */
	public abstract float convertTofloat(Object obj);

	/**
	 * 解析Object为Float类型
	 * @param obj - Object
	 * @return Float
	 */
	public abstract Float convertToFloat(Object obj);

	/**
	 * 解析Object为boolean类型
	 * @param obj - Object
	 * @return boolean
	 */
	public abstract boolean convertToboolean(Object obj);

	/**
	 * 解析Object为Boolean类型
	 * @param obj - Object
	 * @return Boolean
	 */
	public abstract Boolean convertToBoolean(Object obj);

	/**
	 * 解析Object为double类型
	 * @param obj - Object
	 * @return double
	 */
	public abstract double convertTodouble(Object obj);

	/**
	 * 解析Object为Double类型
	 * @param obj - Object
	 * @return Double
	 */
	public abstract Double convertToDouble(Object obj);

	/**
	 * 解析Object为byte类型
	 * @param obj - Object
	 * @return byte
	 */
	public abstract byte convertTobyte(Object obj);

	/**
	 * 解析Object为Byte类型
	 * @param obj - Object
	 * @return Byte
	 */
	public abstract Byte convertToByte(Object obj);

	/**
	 * 解析Object为char类型
	 * @param obj - Object
	 * @return char
	 */
	public abstract char convertTochar(Object obj);

	/**
	 * 解析Object为Character类型
	 * @param obj - Object
	 * @return Character
	 */
	public abstract Character convertToCharacter(Object obj);

	/**
	 * 解析Object类型
	 * @param obj - Object
	 * @param clazz - Clazz
	 * @return Object
	 * @throws Exception
	 */
	public abstract Object convertToT(Object obj, Class<?> clazz) throws Exception;

	/**
	 * 解析Object类型
	 * @param obj - Object
	 * @param containClass
	 * @param itemClass
	 * @return Object
	 * @throws Exception
	 */
	public abstract Object convertToT(Object obj, Class<?> containClass, Class<?> itemClass) throws Exception;

}
