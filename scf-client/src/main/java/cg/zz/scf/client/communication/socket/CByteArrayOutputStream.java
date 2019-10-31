package cg.zz.scf.client.communication.socket;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class CByteArrayOutputStream extends ByteArrayOutputStream {
	
	public byte[] toByteArray(int index, int len) {
		return Arrays.copyOfRange(this.buf, index, Math.min(index + len, size()));
	}

}
