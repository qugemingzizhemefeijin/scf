package cg.zz.scf.serializer.serializer;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

import cg.zz.scf.serializer.component.SCFInStream;
import cg.zz.scf.serializer.component.SCFOutStream;
import cg.zz.scf.serializer.component.exception.OutOfRangeException;
import cg.zz.scf.serializer.component.helper.ByteHelper;

/**
 * 时间序列化
 * @author chengang
 *
 */
public class DateTimeSerializer extends SerializerBase {
	
	//东八区起始时间是8小时
	private long TimeZone = 8 * 60 * 60 * 1000L;

	@Override
	public void WriteObject(Object obj, SCFOutStream outStream) throws Exception {
		byte[] buffer = ConvertToBinary((Date) obj);
		outStream.write(buffer);
	}

	@Override
	public Object ReadObject(SCFInStream inStream, Class<?> defType) throws Exception {
		byte[] buffer = new byte[8];
		inStream.SafeRead(buffer);
		Date date = GetDateTime(buffer);
		if (defType == Timestamp.class)
			return new Timestamp(date.getTime());
		if (defType == java.sql.Date.class)
			return new java.sql.Date(date.getTime());
		if (defType == Time.class) {
			return new Time(date.getTime());
		}
		return date;
	}
	
	/**
	 * 将Date类型转换成byte[]数组
	 * @param dt - Date
	 * @return byte[]
	 */
	private byte[] ConvertToBinary(Date dt) {
		Date dt2 = new Date();
		dt2.setTime(0L);
		long rel = dt.getTime() - dt2.getTime();
		return ByteHelper.GetBytesFromInt64(rel + this.TimeZone);
	}

	/**
	 * 将byte[]数组转换成Date类型
	 * @param buffer - byte[]
	 * @return Date
	 * @throws OutOfRangeException
	 */
	private Date GetDateTime(byte[] buffer) throws OutOfRangeException {
		long rel = ByteHelper.ToInt64(buffer);
		Date dt = new Date(rel - this.TimeZone);
		return dt;
	}

}
