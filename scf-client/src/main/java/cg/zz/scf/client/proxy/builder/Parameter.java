package cg.zz.scf.client.proxy.builder;

import java.lang.reflect.Type;

import cg.zz.scf.server.contract.entity.Out;

public class Parameter {
	
	private Class<?> clazz;
	private Type type;
	private Object value;
	private ParaType paraType;
	private boolean isGeneric;
	private String simpleName;
	private Class<?> containerClass;
	private Class<?> itemClass;
	private Class<?>[] itemClass_;
	
	public Parameter(Object value, Class<?> clazz, Type type) throws ClassNotFoundException {
		setValue(value);
		setClazz(clazz);
		setType(type);
		setParaType(ParaType.In);
		init(value, clazz, type);
	}

	public Parameter(Object value, Class<?> clazz, Type type, ParaType ptype) throws ClassNotFoundException {
		setClazz(clazz);
		setType(type);
		setValue(value);
		setParaType(ptype);
		init(value, clazz, type);
	}

	private void init(Object value, Class<?> clazz, Type type) throws ClassNotFoundException {
		Class<?>[] itemClassO_ = new Class[2];
		if ((!clazz.equals(type)) && (!clazz.getCanonicalName().equalsIgnoreCase(type.toString()))) {
			String itemName = type.toString().replaceAll(clazz.getCanonicalName(), "").replaceAll("\\<", "").replaceAll("\\>", "");

			if (itemName.indexOf(",") == -1) {
				Class<?> itemCls = Class.forName(itemName);
				itemClassO_[0] = itemCls;
			} else {
				String[] itemArray = itemName.split(",");
				if ((itemArray != null) && (itemArray.length == 2)) {
					itemClassO_[0] = Class.forName(itemArray[0].replaceFirst(" ", ""));
					itemClassO_[1] = Class.forName(itemArray[1].replaceFirst(" ", ""));
				}

			}

			setItemClass_(itemClassO_);
			setContainerClass(clazz);
			setIsGeneric(true);

			String sn = "";
			if ((value instanceof Out)) {
				sn = itemName.substring(itemName.lastIndexOf(".") + 1);
			} else {
				sn = clazz.getCanonicalName();
				sn = sn.substring(sn.lastIndexOf(".") + 1);

				if (itemName.indexOf(",") == -1) {
					itemName = itemName.substring(itemName.lastIndexOf(".") + 1);
					sn = sn + "<" + itemName + ">";
				} else {
					String[] genericItem = type.toString().replaceAll(clazz.getCanonicalName(), "").replaceAll("\\<", "").replaceAll("\\>", "").split(",");
					sn = sn + "<" + genericItem[0].substring(genericItem[0].lastIndexOf(".") + 1) + "," + genericItem[1].substring(genericItem[1].lastIndexOf(".") + 1) + ">";
				}
			}
			setSimpleName(sn);
		} else {
			setIsGeneric(false);

			itemClassO_[0] = clazz;
			setItemClass_(itemClassO_);
			setItemClass(clazz);
			setSimpleName(clazz.getSimpleName());
		}
	}

	public Class<?> getClazz() {
		return this.clazz;
	}

	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}

	public ParaType getParaType() {
		return this.paraType;
	}

	public void setParaType(ParaType paraType) {
		this.paraType = paraType;
	}

	public Type getType() {
		return this.type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Object getValue() {
		return this.value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Class<?> getContainerClass() {
		return this.containerClass;
	}

	public void setContainerClass(Class<?> containerClass) {
		this.containerClass = containerClass;
	}

	public Class<?> getItemClass() {
		return this.itemClass;
	}

	public void setItemClass(Class<?> itemClass) {
		this.itemClass = itemClass;
	}

	public String getSimpleName() {
		return this.simpleName;
	}

	public void setSimpleName(String simpleName) {
		this.simpleName = simpleName;
	}

	public boolean isIsGeneric() {
		return this.isGeneric;
	}

	public void setIsGeneric(boolean isGeneric) {
		this.isGeneric = isGeneric;
	}

	public Class<?>[] getItemClass_() {
		return this.itemClass_;
	}

	public void setItemClass_(Class<?>[] itemClass_) {
		this.itemClass_ = itemClass_;
	}

}
