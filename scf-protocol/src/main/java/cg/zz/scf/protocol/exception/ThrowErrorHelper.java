package cg.zz.scf.protocol.exception;

public class ThrowErrorHelper {
	
	public static Exception throwServiceError(int errorcode, String exception) {
		switch (errorcode) {
		case ReturnType.DB:
			return new DBException(exception);
		case ReturnType.NET:
			return new NetException(exception);
		case ReturnType.TIME_OUT:
			return new TimeoutException(exception);
		case ReturnType.PROTOCOL:
			return new ProtocolException(exception);
		case ReturnType.JSON_EXCEPTION:
			return new JSONException(exception);
		case ReturnType.PARA_EXCEPTION:
			return new ParaException(exception);
		case ReturnType.NOT_FOUND_METHOD_EXCEPTION:
			return new NotFoundMethodException(exception);
		case ReturnType.NOT_FOUND_SERVICE_EXCEPTION:
			return new NotFoundServiceException(exception);
		case ReturnType.JSON_SERIALIZE_EXCEPTION:
			return new JSONSerializeException(exception);
		case ReturnType.SERVICE_EXCEPTION:
			return new ServiceException(exception);
		case ReturnType.DATA_OVER_FLOW_EXCEPTION:
			return new DataOverFlowException(exception);
		case ReturnType.OTHER_EXCEPTION:
			return new OtherException(exception);
		}
		return (RemoteException) new Exception("返回状态不可识别!");
	}

}
