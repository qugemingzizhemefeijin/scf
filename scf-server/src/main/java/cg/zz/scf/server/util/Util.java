package cg.zz.scf.server.util;

import java.util.ArrayList;
import java.util.List;

public final class Util {
	
	/**
	 * 得到一个简单的参数名称
	 * 如 java.util.Map<String,String> 转换为 Map<String,String>
	 * @param paraName
	 * @return String
	 */
	public static String getSimpleParaName(String paraName) {
		paraName = paraName.replaceAll(" ", "");
		if(paraName.indexOf(".") > 0) {
			String[] pnAry = paraName.split("");
			List<String> originalityList = new ArrayList<String>();
			List<String> replaceList = new ArrayList<String>();
			
			String tempP = "";
			for(int i=0; i<pnAry.length; i++) {
				if(pnAry[i].equalsIgnoreCase("<")) {
					originalityList.add(tempP);
					replaceList.add(tempP.substring(tempP.lastIndexOf(".") + 1));
					tempP = "";
				} else if(pnAry[i].equalsIgnoreCase(">")) {
					originalityList.add(tempP);
					replaceList.add(tempP.substring(tempP.lastIndexOf(".") + 1));
					tempP = "";
				} else if(pnAry[i].equalsIgnoreCase(",")){
					originalityList.add(tempP);
					replaceList.add(tempP.substring(tempP.lastIndexOf(".") + 1));
					tempP = "";
				} else if(i == pnAry.length - 1){
					originalityList.add(tempP);
					replaceList.add(tempP.substring(tempP.lastIndexOf(".") + 1));
					tempP = "";
				} else {
					if(!pnAry[i].equalsIgnoreCase("[") && !pnAry[i].equalsIgnoreCase("]")) {
						tempP += pnAry[i];
					}
				}
			}
			
			for(int i=0; i<replaceList.size(); i++) {
				paraName = paraName.replaceAll(originalityList.get(i), replaceList.get(i));
			}
			return paraName;
		} else {
			return paraName;
		}
	}

}
