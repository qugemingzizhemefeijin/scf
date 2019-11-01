package cg.zz.scf.client.proxy.builder;

import java.lang.reflect.Type;

import cg.zz.scf.server.contract.entity.Out;

/**
 * 远程代理方法的参数描述对象
 * @author chengang
 *
 */
public class Parameter {
	
	/**
	 * 参数的Class类型
	 */
	private Class<?> clazz;
	
	/**
	 * 参数带泛型标识的类型
	 */
	private Type type;
	
	/**
	 * 参数的值
	 */
	private Object value;
	
	/**
	 * 参数是输入参数还是输出参数
	 */
	private ParaType paraType;
	
	/**
	 * 是否是泛型的参数
	 */
	private boolean isGeneric;
	
	/**
	 * 参数类型的字符描述，如List<String>或者String
	 */
	private String simpleName;
	
	/**
	 * 如果isGeneric=true，则此值代表的是外层的容器类型，否则为空
	 */
	private Class<?> containerClass;
	
	/**
	 * 如果isGeneric=false，则此值是clazz字段，否则为空
	 */
	private Class<?> itemClass;
	
	/**
	 * 此数组长度为2<br/>
	 * 如果isGeneric=true，则[0]=泛型1类型Class,[1]=泛型2类型Class，最多支持2个泛型，超过2个则全部为null；<br/>
	 * isGeneric=false，则[0]=外层类型Class,[1]=null
	 */
	private Class<?>[] itemClass_;
	

	/**
	 * 构造Parameter对象
	 * @param value - 参数值
	 * @param clazz - 参数的Class
	 * @param type - 参数带泛型的Class
	 * @throws ClassNotFoundException
	 */
	public Parameter(Object value, Class<?> clazz, Type type) throws ClassNotFoundException {
		this(value , clazz , type , ParaType.In);
	}

	/**
	 * 构造Parameter对象
	 * @param value - 参数值
	 * @param clazz - 参数的Class
	 * @param type - 参数带泛型的Class
	 * @param ptype - 参数类型枚举
	 * @throws ClassNotFoundException
	 */
	public Parameter(Object value, Class<?> clazz, Type type, ParaType ptype) throws ClassNotFoundException {
		setClazz(clazz);
		setType(type);
		setValue(value);
		setParaType(ptype);
		init(value, clazz, type);
	}

	/**
	 * 初始化参数的信息
	 * @param value - 参数值
	 * @param clazz - 参数的Class
	 * @param type - 参数带泛型的Class
	 * @throws ClassNotFoundException
	 */
	private void init(Object value, Class<?> clazz, Type type) throws ClassNotFoundException {
		Class<?>[] itemClassO_ = new Class[2];
		//判断参数是否是带泛型标识
		if (!clazz.equals(type) && !clazz.getCanonicalName().equalsIgnoreCase(type.toString())) {
			//解析出里面的泛型
			String itemName = type.toString().replaceAll(clazz.getCanonicalName(), "").replaceAll("\\<", "").replaceAll("\\>", "");

			//这里最多只支持两个泛型
			if (itemName.indexOf(",") == -1) {
				Class<?> itemCls = Class.forName(itemName);
				itemClassO_[0] = itemCls;
			} else {
				String[] itemArray = itemName.split(",");
				if (itemArray != null && itemArray.length == 2) {
					itemClassO_[0] = Class.forName(itemArray[0].replaceFirst(" ", ""));
					itemClassO_[1] = Class.forName(itemArray[1].replaceFirst(" ", ""));
				}

			}

			//泛型Class数组
			setItemClass_(itemClassO_);
			//泛型容器的Class
			setContainerClass(clazz);
			//设置为支持泛型的参数
			setIsGeneric(true);

			String sn = "";
			if (value instanceof Out) {
				sn = itemName.substring(itemName.lastIndexOf(".") + 1);
			} else {
				//下面代码拼接方法的描述信息
				//如下：clazz = java.util.List , item = java.util.Date,java.lang.String
				//拼接后：List<Date,String>
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
			//设置为不支持泛型的参数
			setIsGeneric(false);

			//泛型Class数组直接为参数的Class
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
