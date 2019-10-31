package cg.zz.scf.server.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import cg.zz.scf.protocol.sdp.ExceptionProtocol;
import cg.zz.scf.protocol.utility.ProtocolConst;

/**
 * 异常辅助类
 * @author chengang
 *
 */
public class ExceptionHelper {
	
	/**
	 * 创建错误的协议信息
	 * @param sfe - ServiceFrameException
	 * @return ExceptionProtocol
	 */
	public static ExceptionProtocol createError(ServiceFrameException sfe) {
		ExceptionProtocol error = new ExceptionProtocol();
		if (sfe != null) {
			if (sfe.getState() == null) {
				sfe.setState(ErrorState.OtherException);
			}
			error.setErrorCode(sfe.getState().getStateNum());
			
			StringBuilder sbError = new StringBuilder();
			sbError.append("error num:");
			sbError.append(System.nanoTime());
			sbError.append("--state:");
			sbError.append(sfe.getState().toString());
			
			sbError.append("--fromIP:");
			if(sfe.getFromIP()!= null) {
				sbError.append(sfe.getFromIP());
			}
			sbError.append("--toIP:");
			if(sfe.getToIP()!= null) {
				sbError.append(sfe.getToIP());
			}
			
			sbError.append("--Message:");

			if (sfe.getMessage() != null) {
				sbError.append(sfe.getMessage());
			}

			sbError.append(getStackTrace(sfe));

			error.setErrorMsg(sbError.toString());
			error.setFromIP(sfe.getFromIP());
			error.setToIP(sfe.getToIP());
		}
		
		return error;
	}
	
	/**
	 * 创建错误协议信息
	 * @param state - 错误状态枚举
	 * @param fromIP - 来源ID
	 * @param toIP - 目的ID
	 * @return ExceptionProtocol
	 */
	public static ExceptionProtocol createError(ErrorState state, String fromIP, String toIP) {
		ExceptionProtocol error = new ExceptionProtocol();
		if (state == null) {
			state = ErrorState.OtherException;
		}
		error.setErrorCode(state.getStateNum());
		error.setErrorMsg("error num:" + System.nanoTime() + "--state:" + state.toString());
		error.setFromIP(fromIP);
		error.setToIP(toIP);
		return error;
	}
	
	/**
	 * 创建错误协议信息
	 * @param state - 错误枚举
	 * @param fromIP - 来源IP
	 * @param toIP - 目的IP
	 * @param e - Exception
	 * @return ExceptionProtocol
	 */
	public static ExceptionProtocol createError(ErrorState state, String fromIP, String toIP, Exception e) {
		ExceptionProtocol error = new ExceptionProtocol();
		if (state == null) {
			state = ErrorState.OtherException;
		}
		error.setErrorCode(state.getStateNum());

		StringBuilder sbError = new StringBuilder();
		sbError.append("error num:" + System.nanoTime());
		sbError.append("--state:");
		sbError.append(state.toString());
		if (e != null) {
			sbError.append("--Message:");

			if (e.getMessage() != null) {
				sbError.append(e.getMessage());
			}

			StackTraceElement[] trace = e.getStackTrace();
			if (trace != null) {
				for (int i = 0; i < trace.length; i++) {
					sbError.append(trace[i].toString());
					sbError.append("---");
				}
			}
		}
		error.setErrorMsg(sbError.toString());

		error.setFromIP(fromIP);
		error.setToIP(toIP);
		return error;
	}
	
	/**
	 * 创建错误协议信息
	 * @param e - Exception
	 * @return ExceptionProtocol
	 */
	public static ExceptionProtocol createError(Throwable e) {
		ExceptionProtocol error = new ExceptionProtocol();
		error.setErrorCode(ErrorState.OtherException.getStateNum());

		StringBuilder sbError = new StringBuilder();
		sbError.append("error num:" + System.nanoTime());
		sbError.append("--state:");
		sbError.append(ErrorState.OtherException.toString());
		if (e != null) {
			sbError.append("--Message:");

			if (e.getMessage() != null) {
				sbError.append(e.getMessage());
			}

			StackTraceElement[] trace = e.getStackTrace();
			if (trace != null) {
				for (int i = 0; i < trace.length; i++) {
					sbError.append(trace[i].toString());
					sbError.append("---");
				}
			}
		}
		error.setErrorMsg(sbError.toString());
		error.setFromIP("");
		error.setToIP("");
		return error;
	}
	
	/**
	 * 创建错误协议内容
	 * @return byte[]
	 */
	public static byte[] createErrorProtocol() {
		byte[] pByte = new byte[ProtocolConst.P_START_TAG.length + ProtocolConst.P_END_TAG.length + 1];
		System.arraycopy(ProtocolConst.P_START_TAG, 0, pByte, 0, ProtocolConst.P_START_TAG.length);
		pByte[ProtocolConst.P_START_TAG.length] = 0;
		System.arraycopy(ProtocolConst.P_END_TAG, 0, pByte, ProtocolConst.P_END_TAG.length + 1, ProtocolConst.P_END_TAG.length);
		return pByte;
	}
	
	/**
	 * 获得错误堆栈信息
	 * @param e - Throwable
	 * @return String
	 */
	public static String getStackTrace(Throwable e) {
		String stackTrace = "";
		Writer writer = null;
		PrintWriter printWriter = null;
		try {
			writer = new StringWriter();
			printWriter = new PrintWriter(writer);
			e.printStackTrace(printWriter);
			stackTrace = writer.toString();
		} catch(Exception ex) {
			ex.printStackTrace();
		} finally {
			if(printWriter != null) try {printWriter.close();} catch(Exception ex) {ex.printStackTrace();}
			if(writer != null) try {writer.close();} catch(Exception ex) {ex.printStackTrace();}
		}
		
		return stackTrace;
	}

}
