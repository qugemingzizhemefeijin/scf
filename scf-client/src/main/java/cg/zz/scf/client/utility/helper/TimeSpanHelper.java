package cg.zz.scf.client.utility.helper;

public class TimeSpanHelper {
	
	/**
	 * 将时:分:秒格式的时间转换为毫秒数
	 * @param timeSpan - String
	 * @return int
	 */
	public static int getIntFromTimeSpan(String timeSpan) {
		int returnint = 0;
		String[] times = timeSpan.split(":");
		if (times.length == 3) {
			returnint += Integer.parseInt(times[0]) * 60 * 60 * 1000;
			returnint += Integer.parseInt(times[1]) * 60 * 1000;
			returnint += Integer.parseInt(times[2]) * 1000;
		}
		return returnint;
	}

	/**
	 * 将毫秒数转换为时:分:秒格式的时间
	 * @param timeSpan - int
	 * @return String
	 * @throws Exception
	 */
	public static String getTimeSpanFromInt(int timeSpan) throws Exception {
		throw new Exception("NotImplementedException");
	}
	
	/**
	 * 00(秒) 00:00(分:秒)
	 * 00:00:00(时:分:秒)
	 * 00:00:00:00(时:分:秒:毫秒)
	 * @param timeSpan - 将以上格式的时间转换为毫秒数
	 * @return int
	 */
	public static int getIntFromTimeMsSpan(String timeSpan) {
		int returnint = 0;
		String[] times = timeSpan.split(":");

		switch (times.length) {
		case 1:
			returnint += Integer.parseInt(times[0]) * 1000;
			break;
		case 2:
			returnint += Integer.parseInt(times[0]) * 60 * 1000;
			returnint += Integer.parseInt(times[1]) * 1000;
			break;
		case 3:
			returnint += Integer.parseInt(times[0]) * 60 * 60 * 1000;
			returnint += Integer.parseInt(times[1]) * 60 * 1000;
			returnint += Integer.parseInt(times[2]) * 1000;
			break;
		case 4:
			returnint += Integer.parseInt(times[0]) * 60 * 60 * 1000;
			returnint += Integer.parseInt(times[1]) * 60 * 1000;
			returnint += Integer.parseInt(times[2]) * 1000;
			returnint += Integer.parseInt(times[3]);
			break;
		}

		return returnint;
	}

}
