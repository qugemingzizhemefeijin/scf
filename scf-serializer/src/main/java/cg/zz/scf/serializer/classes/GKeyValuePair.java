package cg.zz.scf.serializer.classes;

public class GKeyValuePair<TKey, TValue> {
	
	private TKey _key;
	private TValue _value;

	public GKeyValuePair(TKey key, TValue value) {
		this._key = key;
		this._value = value;
	}

	public TKey getKey() {
		return this._key;
	}

	public void setKey(TKey key) {
		this._key = key;
	}

	public TValue getValue() {
		return this._value;
	}

	public void setValue(TValue value) {
		this._value = value;
	}

}
