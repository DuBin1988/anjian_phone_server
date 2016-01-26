package com.aote.rs.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

/**
 * 清欠表生成    姚嘉澍
 * 总思路，两种情况：
 * 1. 1.19-1.31 第一条是抄表，直接产生清欠，更新这条抄表的欠费，更新用户档案账户余额。
 *    第二条是收费，如果收费没有记录，则结束。
 *    			        如果有，在抄表中查找，此条交费记录有没有对应的欠费记录，且在1.19之前。
 *    								         如果没有欠费记录，则结束。
 *    									如果有欠费记录，按欠费记录逐条产生清欠。且，若第一条抄表欠费不为0，将第一条抄表再次产生清欠，结束。
 * 2. 1.19-1.31 第一条是收费，在抄表中查找，此条交费记录有没有对应的欠费记录，且在1.19之前。
 *    								         如果没有欠费记录，更具此条收费直接产生清欠。
 *    									如果有欠费记录，按欠费记录逐条产生清欠。结束。
 *    			第二条是抄表，如果没有抄表，则结束。
 *    						   如果有抄表，则针对此条抄表产生清欠。结束。
 */

/**
 * 代码结构：
 * 
 * if抄表没有
 * {
 * 		if收费没有
 * 		{
 * 			continue；
 * 		}
 * 		else
 * 		{
 * 			在抄表中查找，此条交费记录有没有对应的欠费记录，且在1.19之前。
 *    				         如果没有欠费记录，根据此条收费直接产生清欠。
 *    				         如果有欠费记录，按欠费记录逐条产生清欠。结束。
 * 		}
 * }
 * else
 * {
 * 		if收费没有
 * 		{
 * 			直接产生清欠，更新这条抄表的欠费，更新用户档案账户余额。
 * 		}
 * 		else
 * 		{
 * 			if抄表在前
 * 			{
 * 				1.19-1.31 第一条是抄表，直接产生清欠，更新这条抄表的欠费，更新用户档案账户余额。
 * 				在抄表中查找，此条交费记录有没有对应的欠费记录，且在1.19之前。
 *    				         如果没有欠费记录，则结束。
 *    				         如果有欠费记录，按欠费记录逐条产生清欠。且，若第一条抄表欠费不为0，将第一条抄表再次产生清欠，结束。
 * 			}
 * 			else
 * 			{
 * 				在抄表中查找，此条交费记录有没有对应的欠费记录，且在1.19之前。
 *    				         如果没有欠费记录，根据此条收费直接产生清欠。
 *    				         如果有欠费记录，按欠费记录逐条产生清欠。结束。
 *    			对第二条抄表产生清欠。结束。
 * 			}
 * 		}
 * }
 */
@Path("newcreatedebt")
@Component
public class NewCleanDebt {
	static Logger log = Logger.getLogger(NewCleanDebt.class);
	@Autowired
	private HibernateTemplate hibernateTemplate;

	@GET
	@Path("newcreate")
	public String cleanDebt(){
		try {
			
			//查询一户用户档案
			String UserFilesSql = "from t_userfiles where id = ";
			List<Object> UserFilesList = new ArrayList<Object>();
			Map<String, Object> UserFilesMap = new HashMap<String, Object>();
			//查询一户抄表记录
			String HandPlanOneSql = "from t_handplan where f_state = '已抄表' and f_inputdate >= '2015-01-19' and f_inputdate < '2015-02-01' and (oughtamount <> 0 or oughtfee <> 0) and f_userid = ";
			List<Object> HandPlanList = new ArrayList<Object>();
			Map<String, Object> HandPlanOneMap = new HashMap<String, Object>();
			//查询一户交费记录
			String SellingGasSql = "from t_sellinggas where f_deliverydate >= '2015-01-19' and f_deliverydate < '2015-02-01' and f_payfeevalid = '有效' and f_payfeetype = '机表收费' and f_userid = ";
			List<Object> SellingGasList = new ArrayList<Object>();
			Map<String, Object> SellingGasOneMap = new HashMap<String, Object>();
			//查找交费记录对应的欠费记录
			String DebtSql = "from t_handplan where f_inputdate < '2015-01-19' and oughtfee <> 0 and f_sellid = ";
			List<Object> DebtList = new ArrayList<Object>();
			List<Map<String, Object>> DebtMapList = new ArrayList<Map<String, Object>>();
			Map<String, Object> DebtOneMap = new HashMap<String, Object>();
			
			String GasMeterStyle = "";
			String UserID = "";
			String StairOnePriceStr = "";
			String AccountZHYEStr = "";
			String HandPlanOneDate = "";
			String SellGasDate = "";
			String SellGasTime = "";
			String DebtMoneyStr = "";
			String NetWork = "";
			String Operator = "";
			String SellIDStr = "";
			String GrossProceedsStr = "";
			double GrossProceeds = 0;
			//单价
			double StairOnePrice = 0;
			//用户档案账户虚余额
			double AccountZHYE = 0;
			//抄表表中虚欠费金额
			double DebtMoney = 0;
			double DebtYE = 0;
			//用户档案id范围
			for(int i = 853671; i <= 909806; i++)
			{
				//查询档案
				if(UserFilesList.size() != 0)
					UserFilesList.clear();
				UserFilesList = this.hibernateTemplate.find(UserFilesSql + i);
				if(UserFilesList.size() != 1)
				{
					continue;
				}
				if(UserFilesMap.size() != 0)
					UserFilesMap.clear();
				UserFilesMap = (Map<String, Object>) UserFilesList.get(0);
				GasMeterStyle = UserFilesMap.get("f_gasmeterstyle") == null ? "" : UserFilesMap.get("f_gasmeterstyle").toString();
				//如果不是机表，则continue.
				if(!GasMeterStyle.equals("机表"))
				{
					continue;
				}
				AccountZHYEStr = UserFilesMap.get("f_accountzhye") == null ? "0" : UserFilesMap.get("f_accountzhye").toString();
				//如果档案账户余额为0，则continue.
				if(AccountZHYEStr.equals("0"))
				{
					continue;
				}
				AccountZHYE = Double.parseDouble(AccountZHYEStr);
				UserID = UserFilesMap.get("f_userid") == null ? "" : UserFilesMap.get("f_userid").toString();
				StairOnePriceStr = UserFilesMap.get("f_stair1price") == null ? "" : UserFilesMap.get("f_stair1price").toString();
				StairOnePrice = Double.parseDouble(StairOnePriceStr);
				
				//查询抄表
				if(HandPlanList.size() != 0)
					HandPlanList.clear();
				HandPlanList = this.hibernateTemplate.find(HandPlanOneSql + "'" + UserID + "'");
				
				//查询交费
		        if(SellingGasList.size() != 0)
		        	SellingGasList.clear();
		        SellingGasList = this.hibernateTemplate.find(SellingGasSql + "'" + UserID + "'");
		        
		        //没抄表
		        if(HandPlanList.size() == 0)
		        {
		        	//没交费
		        	if(SellingGasList.size() == 0)
		        	{
		        		continue;
		        	}
		        	//有交费
		        	else
		        	{
		        		if(SellingGasOneMap.size() != 0)
		        			SellingGasOneMap.clear();
		        		SellingGasOneMap = (Map<String, Object>) SellingGasList.get(0);
		        		if(DebtList.size() != 0)
		        			DebtList.clear();
		        		DebtList = this.hibernateTemplate.find(DebtSql + (SellingGasOneMap.get("id") == null ? "''" : "'" + SellingGasOneMap.get("id").toString() + "'"));
		        		//没有1.19之前的抄表记录，根据此条收费直接产生清欠。
		        		if(DebtList.size() == 0)
		        		{
		        			SellGasDate = SellingGasOneMap.get("f_deliverydate") == null ? "" : SellingGasOneMap.get("f_deliverydate").toString();
		        			GrossProceedsStr = SellingGasOneMap.get("f_grossproceeds") == null ? "0" : SellingGasOneMap.get("f_grossproceeds").toString();
		        			GrossProceeds = Double.parseDouble(GrossProceedsStr);
		        			DebtYE = GrossProceeds + AccountZHYE;
		        			NetWork = SellingGasOneMap.get("f_sgnetwork") == null ? "" : SellingGasOneMap.get("f_sgnetwork").toString();
		        			Operator = SellingGasOneMap.get("f_sgoperator") == null ? "" : SellingGasOneMap.get("f_sgoperator").toString();
		        			SellGasTime = SellingGasOneMap.get("f_deliverytime") == null ? "" : SellingGasOneMap.get("f_deliverytime").toString();
		        			SellIDStr = SellingGasOneMap.get("id") == null ? "" : SellingGasOneMap.get("id").toString();
							if (CreatDebt(UserID, "0", "0", "0", SellGasDate,
									StairOnePriceStr, AccountZHYEStr,
									Double.toString(DebtYE), "交费扣除", NetWork, Operator,
									SellGasDate, SellGasTime, "0", SellIDStr)) {
		        				continue;
		        			}
		        			else
		        			{
		        				return "not ok, because of inserting failed!!! error code is 1. The id of t_userfiles is : " + i;
		        			}
		        		}
		        		//有1.19之前的抄表记录，逐条清欠
		        		for(int j = 0; j < DebtList.size(); j++)
		        		{
		        			if(DebtMapList.size() != 0)
		        				DebtMapList.clear();
		        			DebtMapList.add((Map<String, Object>) DebtList.get(j));
		        		}
		        		//对交费对应的抄表记录进行升序排序
		        		Collections.sort(DebtMapList, new MapComparator());
		        		//开始逐条产生清欠记录
		        		for(int k = 0; k < DebtMapList.size(); k++)
		        		{
		        			DebtOneMap = DebtMapList.get(k);
		        			DebtMoneyStr = DebtOneMap.get("f_debtmoney") == null ? "0" : DebtOneMap.get("f_debtmoney").toString();
		        			DebtMoney = Double.parseDouble(DebtMoneyStr);
		        			if(UserFilesList.size() != 0)
		    					UserFilesList.clear();
		    				UserFilesList = this.hibernateTemplate.find(UserFilesSql + i);
		    				if(UserFilesMap.size() != 0)
		    					UserFilesMap.clear();
		    				UserFilesMap = (Map<String, Object>) UserFilesList.get(0);
		    				AccountZHYEStr = UserFilesMap.get("f_accountzhye") == null ? "0" : UserFilesMap.get("f_accountzhye").toString();
		    				AccountZHYE = Double.parseDouble(AccountZHYEStr);
		    				GrossProceedsStr = SellingGasOneMap.get("f_grossproceeds") == null ? "0" : SellingGasOneMap.get("f_grossproceeds").toString();
		        			GrossProceeds = Double.parseDouble(GrossProceedsStr);
		    				DebtYE = (GrossProceeds + AccountZHYE - DebtMoney) < 0 ? 0 : (GrossProceeds + AccountZHYE - DebtMoney);
		        			if(!CreatDebt(UserID, DebtMoneyStr, DebtMoneyStr, "0", DebtOneMap.get("f_inputdate") == null ? "" : DebtOneMap.get("f_inputdate").toString(),
									StairOnePriceStr, AccountZHYEStr,
									Double.toString(DebtYE), "抄表扣除", 
									DebtOneMap.get("f_network") == null ? "" : DebtOneMap.get("f_network").toString(), 
									DebtOneMap.get("f_operator") == null ? "" : DebtOneMap.get("f_operator").toString(),
									DebtOneMap.get("f_inputdate") == null ? "" : DebtOneMap.get("f_inputdate").toString(),
									SellingGasOneMap.get("f_deliverytime") == null ? "" : SellingGasOneMap.get("f_deliverytime").toString(), 
									DebtOneMap.get("id") == null ? "0" : DebtOneMap.get("id").toString(), 
									SellingGasOneMap.get("id") == null ? "0" : SellingGasOneMap.get("id").toString()))
		        			{
		        				return "not ok, because of inserting failed!!! error code is 2. The id of t_userfiles is : " + i;
		        			}
		        		}
		        	}
		        }
		        //有抄表
		        else
		        {
		        	//没交费
		        	if(SellingGasList.size() == 0)
		        	{
		        		if(HandPlanOneMap.size() != 0)
		        			HandPlanOneMap.clear();
		        		HandPlanOneMap = (Map<String, Object>) HandPlanList.get(0);
		        		DebtMoneyStr = HandPlanOneMap.get("f_debtmoney") == null ? "0" : HandPlanOneMap.get("f_debtmoney").toString();
		        		DebtMoney = Double.parseDouble(DebtMoneyStr);
		        		//直接产生清欠，更新这条抄表的欠费，更新用户档案账户余额。
						if (!CreatDebt(UserID, DebtMoneyStr,
								AccountZHYE > DebtMoney ? Double.toString(DebtMoney) : Double.toString(AccountZHYE),
								AccountZHYE > DebtMoney ? "0" : Double.toString(DebtMoney - AccountZHYE),
								HandPlanOneMap.get("f_inputdate") == null ? "" : HandPlanOneMap.get("f_inputdate").toString(),
								StairOnePriceStr, AccountZHYEStr,
								AccountZHYE < DebtMoney ? "0" : Double.toString(AccountZHYE - DebtMoney),
								"抄表扣除", 
								HandPlanOneMap.get("f_network") == null ? "" : HandPlanOneMap.get("f_network").toString(), 
								HandPlanOneMap.get("f_operator") == null ? "" : HandPlanOneMap.get("f_operator").toString(),
								HandPlanOneMap.get("f_inputdate") == null ? "" : HandPlanOneMap.get("f_inputdate").toString(),
								HandPlanOneMap.get("f_inputdate") == null ? "" : HandPlanOneMap.get("f_inputdate").toString(),
								HandPlanOneMap.get("id") == null ? "0" : HandPlanOneMap.get("id").toString(), "0"))
	        			{
	        				return "not ok, because of inserting failed!!! error code is 3. The id of t_userfiles is : " + i;
	        			}
		        	}
		        	//有交费
		        	else
		        	{
		        		if(SellingGasOneMap.size() != 0)
		        			SellingGasOneMap.clear();
		        		SellingGasOneMap = (Map<String, Object>) SellingGasList.get(0);
		        		SellGasDate = SellingGasOneMap.get("f_deliverydate") == null ? "" : SellingGasOneMap.get("f_deliverydate").toString();
		        		
		        		if(HandPlanOneMap.size() != 0)
		        			HandPlanOneMap.clear();
		        		HandPlanOneMap = (Map<String, Object>) HandPlanList.get(0);
		        		HandPlanOneDate = HandPlanOneMap.get("f_inputdate") == null ? "" : HandPlanOneMap.get("f_inputdate").toString();
		        		
		        		//抄表在前
		        		if(HandPlanOneDate.compareTo(SellGasDate) < 0)
		        		{
		        			DebtMoneyStr = HandPlanOneMap.get("f_debtmoney") == null ? "0" : HandPlanOneMap.get("f_debtmoney").toString();
			        		DebtMoney = Double.parseDouble(DebtMoneyStr);
			        		//直接产生清欠，更新这条抄表的欠费，更新用户档案账户余额。
							if (!CreatDebt(UserID, DebtMoneyStr,
									AccountZHYE > DebtMoney ? Double.toString(DebtMoney) : Double.toString(AccountZHYE),
									AccountZHYE > DebtMoney ? "0" : Double.toString(DebtMoney - AccountZHYE),
									HandPlanOneMap.get("f_inputdate") == null ? "" : HandPlanOneMap.get("f_inputdate").toString(),
									StairOnePriceStr, AccountZHYEStr,
									AccountZHYE < DebtMoney ? "0" : Double.toString(AccountZHYE - DebtMoney),
									"抄表扣除", 
									HandPlanOneMap.get("f_network") == null ? "" : HandPlanOneMap.get("f_network").toString(), 
									HandPlanOneMap.get("f_operator") == null ? "" : HandPlanOneMap.get("f_operator").toString(),
									HandPlanOneMap.get("f_inputdate") == null ? "" : HandPlanOneMap.get("f_inputdate").toString(),
									HandPlanOneMap.get("f_inputdate") == null ? "" : HandPlanOneMap.get("f_inputdate").toString(),
									HandPlanOneMap.get("id") == null ? "0" : HandPlanOneMap.get("id").toString(), "0"))
		        			{
		        				return "not ok, because of inserting failed!!! error code is 4. The id of t_userfiles is : " + i;
		        			}
			        		if(DebtList.size() != 0)
			        			DebtList.clear();
			        		DebtList = this.hibernateTemplate.find(DebtSql + (SellingGasOneMap.get("id") == null ? "''" : "'" + SellingGasOneMap.get("id").toString() + "'"));
			        		//查最新的用户档案余额
			        		if(UserFilesList.size() != 0)
								UserFilesList.clear();
							UserFilesList = this.hibernateTemplate.find(UserFilesSql + i);
							if(UserFilesMap.size() != 0)
								UserFilesMap.clear();
							UserFilesMap = (Map<String, Object>) UserFilesList.get(0);
							AccountZHYEStr = UserFilesMap.get("f_accountzhye") == null ? "0" : UserFilesMap.get("f_accountzhye").toString();
							AccountZHYE = Double.parseDouble(AccountZHYEStr);
			        		//根据第二条收费产生清欠。
			        		if(DebtList.size() == 0)
			        		{
			        			SellGasDate = SellingGasOneMap.get("f_deliverydate") == null ? "" : SellingGasOneMap.get("f_deliverydate").toString();
			        			GrossProceedsStr = SellingGasOneMap.get("f_grossproceeds") == null ? "0" : SellingGasOneMap.get("f_grossproceeds").toString();
			        			GrossProceeds = Double.parseDouble(GrossProceedsStr);
			        			DebtYE = GrossProceeds + AccountZHYE;
			        			NetWork = SellingGasOneMap.get("f_sgnetwork") == null ? "" : SellingGasOneMap.get("f_sgnetwork").toString();
			        			Operator = SellingGasOneMap.get("f_sgoperator") == null ? "" : SellingGasOneMap.get("f_sgoperator").toString();
			        			SellGasTime = SellingGasOneMap.get("f_deliverytime") == null ? "" : SellingGasOneMap.get("f_deliverytime").toString();
			        			SellIDStr = SellingGasOneMap.get("id") == null ? "0" : SellingGasOneMap.get("id").toString();
								if (CreatDebt(UserID, "0", "0", "0", SellGasDate,
										StairOnePriceStr, AccountZHYEStr,
										Double.toString(DebtYE), "交费扣除", NetWork, Operator,
										SellGasDate, SellGasTime, "0", SellIDStr)) {
			        				continue;
			        			}
			        			else
			        			{
			        				return "not ok, because of inserting failed!!! error code is 5. The id of t_userfiles is : " + i;
			        			}
			        		}
			        		//有1.19之前的抄表记录，逐条清欠
			        		for(int j = 0; j < DebtList.size(); j++)
			        		{
			        			if(DebtMapList.size() != 0)
			        				DebtMapList.clear();
			        			DebtMapList.add((Map<String, Object>) DebtList.get(j));
			        		}
			        		//对交费对应的抄表记录进行升序排序
			        		Collections.sort(DebtMapList, new MapComparator());
			        		//开始逐条产生清欠记录
			        		for(int k = 0; k < DebtMapList.size(); k++)
			        		{
			        			DebtOneMap = DebtMapList.get(k);
			        			DebtMoneyStr = DebtOneMap.get("f_debtmoney") == null ? "0" : DebtOneMap.get("f_debtmoney").toString();
			        			DebtMoney = Double.parseDouble(DebtMoneyStr);
			        			if(UserFilesList.size() != 0)
			    					UserFilesList.clear();
			    				UserFilesList = this.hibernateTemplate.find(UserFilesSql + i);
			    				if(UserFilesMap.size() != 0)
			    					UserFilesMap.clear();
			    				UserFilesMap = (Map<String, Object>) UserFilesList.get(0);
			    				AccountZHYEStr = UserFilesMap.get("f_accountzhye") == null ? "0" : UserFilesMap.get("f_accountzhye").toString();
			    				AccountZHYE = Double.parseDouble(AccountZHYEStr);
			    				GrossProceedsStr = SellingGasOneMap.get("f_grossproceeds") == null ? "0" : SellingGasOneMap.get("f_grossproceeds").toString();
			        			GrossProceeds = Double.parseDouble(GrossProceedsStr);
			    				DebtYE = (GrossProceeds + AccountZHYE - DebtMoney) < 0 ? 0 : (GrossProceeds + AccountZHYE - DebtMoney);
			        			if(!CreatDebt(UserID, DebtMoneyStr, DebtMoneyStr, "0", DebtOneMap.get("f_inputdate") == null ? "" : DebtOneMap.get("f_inputdate").toString(),
										StairOnePriceStr, AccountZHYEStr,
									    Double.toString(DebtYE) , "抄表扣除", 
										DebtOneMap.get("f_network") == null ? "" : DebtOneMap.get("f_network").toString(), 
										DebtOneMap.get("f_operator") == null ? "" : DebtOneMap.get("f_operator").toString(),
										DebtOneMap.get("f_inputdate") == null ? "" : DebtOneMap.get("f_inputdate").toString(),
										SellingGasOneMap.get("f_deliverytime") == null ? "" : SellingGasOneMap.get("f_deliverytime").toString(), 
										DebtOneMap.get("id") == null ? "0" : DebtOneMap.get("id").toString(), 
										SellingGasOneMap.get("id") == null ? "0" : SellingGasOneMap.get("id").toString()))
			        			{
			        				return "not ok, because of inserting failed!!! error code is 6. The id of t_userfiles is : " + i;
			        			}
			        		}
			        		
			        		//查询抄表
							if(HandPlanList.size() != 0)
								HandPlanList.clear();
							HandPlanList = this.hibernateTemplate.find(HandPlanOneSql + "'" + UserID + "'");
							if(HandPlanOneMap.size() != 0)
			        			HandPlanOneMap.clear();
			        		HandPlanOneMap = (Map<String, Object>) HandPlanList.get(0);
			        		DebtMoneyStr = HandPlanOneMap.get("f_debtmoney") == null ? "0" : HandPlanOneMap.get("f_debtmoney").toString();
			        		DebtMoney = Double.parseDouble(DebtMoneyStr);
			        		//如果生成清欠后最新的抄表欠费为0，则continue.
							if(DebtMoney == 0)
							{
								continue;
							}
							//如果生成清欠后最新的抄表欠费不为0
							//查询档案
							if(UserFilesList.size() != 0)
								UserFilesList.clear();
							UserFilesList = this.hibernateTemplate.find(UserFilesSql + i);
							if(UserFilesMap.size() != 0)
								UserFilesMap.clear();
							UserFilesMap = (Map<String, Object>) UserFilesList.get(0);
							AccountZHYEStr = UserFilesMap.get("f_accountzhye") == null ? "0" : UserFilesMap.get("f_accountzhye").toString();
							AccountZHYE = Double.parseDouble(AccountZHYEStr);
							
							if(HandPlanOneMap.size() != 0)
			        			HandPlanOneMap.clear();
			        		HandPlanOneMap = (Map<String, Object>) HandPlanList.get(0);
			        		DebtMoneyStr = HandPlanOneMap.get("f_debtmoney") == null ? "0" : HandPlanOneMap.get("f_debtmoney").toString();
			        		DebtMoney = Double.parseDouble(DebtMoneyStr);
			        		//直接产生清欠，更新这条抄表的欠费，更新用户档案账户余额。
							if (!CreatDebt(UserID, DebtMoneyStr,
									DebtMoneyStr,
									"0",
									HandPlanOneMap.get("f_inputdate") == null ? "" : HandPlanOneMap.get("f_inputdate").toString(),
									StairOnePriceStr, AccountZHYEStr,
									Double.toString(AccountZHYE - DebtMoney),
									"抄表扣除", 
									HandPlanOneMap.get("f_network") == null ? "" : HandPlanOneMap.get("f_network").toString(), 
									HandPlanOneMap.get("f_operator") == null ? "" : HandPlanOneMap.get("f_operator").toString(),
									HandPlanOneMap.get("f_inputdate") == null ? "" : HandPlanOneMap.get("f_inputdate").toString(),
									HandPlanOneMap.get("f_inputdate") == null ? "" : HandPlanOneMap.get("f_inputdate").toString(),
									HandPlanOneMap.get("id") == null ? "0" : HandPlanOneMap.get("id").toString(), 
									SellingGasOneMap.get("id") == null ? "0" : SellingGasOneMap.get("id").toString()))
		        			{
		        				return "not ok, because of inserting failed!!! error code is 7. The id of t_userfiles is : " + i;
		        			}
		        		}
		        		//交费在前
		        		else
		        		{
			        		SellingGasOneMap = (Map<String, Object>) SellingGasList.get(0);
			        		if(DebtList.size() != 0)
			        			DebtList.clear();
			        		DebtList = this.hibernateTemplate.find(DebtSql + (SellingGasOneMap.get("id") == null ? "''" : "'" + SellingGasOneMap.get("id").toString() + "'"));
			        		//没有1.19之前的抄表记录，根据此条收费直接产生清欠。
			        		if(DebtList.size() == 0)
			        		{
			        			SellGasDate = SellingGasOneMap.get("f_deliverydate") == null ? "" : SellingGasOneMap.get("f_deliverydate").toString();
			        			GrossProceedsStr = SellingGasOneMap.get("f_grossproceeds") == null ? "0" : SellingGasOneMap.get("f_grossproceeds").toString();
			        			GrossProceeds = Double.parseDouble(GrossProceedsStr);
			        			DebtYE = GrossProceeds + AccountZHYE;
			        			NetWork = SellingGasOneMap.get("f_sgnetwork") == null ? "" : SellingGasOneMap.get("f_sgnetwork").toString();
			        			Operator = SellingGasOneMap.get("f_sgoperator") == null ? "" : SellingGasOneMap.get("f_sgoperator").toString();
			        			SellGasTime = SellingGasOneMap.get("f_deliverytime") == null ? "" : SellingGasOneMap.get("f_deliverytime").toString();
			        			SellIDStr = SellingGasOneMap.get("id") == null ? "0" : SellingGasOneMap.get("id").toString();
								if (CreatDebt(UserID, "0", "0", "0", SellGasDate,
										StairOnePriceStr, AccountZHYEStr,
										Double.toString(DebtYE), "交费扣除", NetWork, Operator,
										SellGasDate, SellGasTime, "0", SellIDStr)) {
			        				continue;
			        			}
			        			else
			        			{
			        				return "not ok, because of inserting failed!!! error code is 8. The id of t_userfiles is : " + i;
			        			}
			        		}
			        		//有1.19之前的抄表记录，逐条清欠
			        		for(int j = 0; j < DebtList.size(); j++)
			        		{
			        			if(DebtMapList.size() != 0)
			        				DebtMapList.clear();
			        			DebtMapList.add((Map<String, Object>) DebtList.get(j));
			        		}
			        		//对交费对应的抄表记录进行升序排序
			        		Collections.sort(DebtMapList, new MapComparator());
			        		//开始逐条产生清欠记录
			        		for(int k = 0; k < DebtMapList.size(); k++)
			        		{
			        			DebtOneMap = DebtMapList.get(k);
			        			DebtMoneyStr = DebtOneMap.get("f_debtmoney") == null ? "0" : DebtOneMap.get("f_debtmoney").toString();
			        			DebtMoney = Double.parseDouble(DebtMoneyStr);
			        			if(UserFilesList.size() != 0)
			    					UserFilesList.clear();
			    				UserFilesList = this.hibernateTemplate.find(UserFilesSql + i);
			    				if(UserFilesMap.size() != 0)
			    					UserFilesMap.clear();
			    				UserFilesMap = (Map<String, Object>) UserFilesList.get(0);
			    				AccountZHYEStr = UserFilesMap.get("f_accountzhye") == null ? "0" : UserFilesMap.get("f_accountzhye").toString();
			    				AccountZHYE = Double.parseDouble(AccountZHYEStr);
			    				DebtYE = AccountZHYE - DebtMoney;
			        			if(!CreatDebt(UserID, DebtMoneyStr, DebtMoneyStr, "0", DebtOneMap.get("f_inputdate") == null ? "" : DebtOneMap.get("f_inputdate").toString(),
										StairOnePriceStr, AccountZHYEStr,
										Double.toString(DebtYE), "抄表扣除", DebtOneMap.get("f_network") == null ? "" : DebtOneMap.get("f_network").toString(), 
										DebtOneMap.get("f_operator") == null ? "" : DebtOneMap.get("f_operator").toString(),
										DebtOneMap.get("f_inputdate") == null ? "" : DebtOneMap.get("f_inputdate").toString(),
										SellingGasOneMap.get("f_deliverytime") == null ? "" : SellingGasOneMap.get("f_deliverytime").toString(), 
										DebtOneMap.get("id") == null ? "0" : DebtOneMap.get("id").toString(), 
										SellingGasOneMap.get("id") == null ? "0" : SellingGasOneMap.get("id").toString()))
			        			{
			        				return "not ok, because of inserting failed!!! error code is 9. The id of t_userfiles is : " + i;
			        			}
			        		}
			        		
			        		//查询档案
							if(UserFilesList.size() != 0)
								UserFilesList.clear();
							UserFilesList = this.hibernateTemplate.find(UserFilesSql + i);
							if(UserFilesMap.size() != 0)
								UserFilesMap.clear();
							UserFilesMap = (Map<String, Object>) UserFilesList.get(0);
							AccountZHYEStr = UserFilesMap.get("f_accountzhye") == null ? "0" : UserFilesMap.get("f_accountzhye").toString();
							AccountZHYE = Double.parseDouble(AccountZHYEStr);
							
							if(HandPlanOneMap.size() != 0)
			        			HandPlanOneMap.clear();
			        		HandPlanOneMap = (Map<String, Object>) HandPlanList.get(0);
			        		DebtMoneyStr = HandPlanOneMap.get("f_debtmoney") == null ? "0" : HandPlanOneMap.get("f_debtmoney").toString();
			        		DebtMoney = Double.parseDouble(DebtMoneyStr);
			        		//直接产生清欠，更新这条抄表的欠费，更新用户档案账户余额。
							if (!CreatDebt(UserID, DebtMoneyStr,
									AccountZHYE > DebtMoney ? Double.toString(DebtMoney) : Double.toString(AccountZHYE),
									AccountZHYE > DebtMoney ? "0" : Double.toString(DebtMoney - AccountZHYE),
									HandPlanOneMap.get("f_inputdate") == null ? "" : HandPlanOneMap.get("f_inputdate").toString(),
									StairOnePriceStr, AccountZHYEStr,
									AccountZHYE < DebtMoney ? "0" : Double.toString(AccountZHYE - DebtMoney),
									"抄表扣除", 
									HandPlanOneMap.get("f_network") == null ? "" : HandPlanOneMap.get("f_network").toString(), 
									HandPlanOneMap.get("f_operator") == null ? "" : HandPlanOneMap.get("f_operator").toString(),
									HandPlanOneMap.get("f_inputdate") == null ? "" : HandPlanOneMap.get("f_inputdate").toString(),
									HandPlanOneMap.get("f_inputdate") == null ? "" : HandPlanOneMap.get("f_inputdate").toString(),
									HandPlanOneMap.get("id") == null ? "0" : HandPlanOneMap.get("id").toString(), "0"))
		        			{
		        				return "not ok, because of inserting failed!!! error code is 10. The id of t_userfiles is : " + i;
		        			}
		        		}
		        	}
		        }
				
				System.out.println(i);
			}
			return "ok";
		} catch (Exception e) {
			e.printStackTrace();
			return "not ok";
		}
	}
	
	//产生清欠
	private boolean CreatDebt(String UserID, String OughtFee, String RealMoney,
			String DebtMoney, String DebtDate, String GasPrice,
			String PrevAccountZHYE, String AccountZHYE, String PayFeeType,
			String SgNetWork, String Operator, String DeliveryDate,
			String DeliveryTime, String HandID, String SellID)
	{
		try {
			String InsertFinanceDetailSql = "insert into t_financedetail (f_userid, f_oughtfee, f_realmoney, f_debtmoney, f_debtdate, f_gasprice, f_prevaccountzhye, f_accountzhye, f_payfeevalid, f_payfeetype, f_sgnetwork, f_opertor, f_deliverydate, f_deliverytime, f_handid, f_sellid) values ('"
					+ UserID
					+ "', "
					+ OughtFee
					+ ", "
					+ RealMoney
					+ ", "
					+ DebtMoney
					+ ", '"
					+ DebtDate
					+ "', "
					+ GasPrice
					+ ", "
					+ PrevAccountZHYE
					+ ", "
					+ AccountZHYE
					+ ", '有效', '"
					+ PayFeeType
					+ "', '"
					+ SgNetWork
					+ "', '"
					+ Operator
					+ "', '"
					+ DeliveryDate
					+ "', '"
					+ DeliveryTime
					+ "', " 
					+ HandID 
					+ ", " 
					+ SellID 
					+ ")";
			execSQL(InsertFinanceDetailSql);
			// 更新用户档案
			String UpdateUserFileSql = "update t_userfiles set f_accountzhye = " + AccountZHYE + " where f_userid = '"
					+ UserID + "'";
			execSQL(UpdateUserFileSql);
			// 更新抄表表
			String UpdateHandPlan = "update t_handplan set f_debtmoney = " + DebtMoney + " where id = " + HandID;
			execSQL(UpdateHandPlan);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	//对List<Map<String, Object>>里的f_inputdate字段进行升序排序
	class MapComparator implements Comparator<Map<String, Object>> {
        @Override
        public int compare(Map<String, Object> o1, Map<String, Object> o2) {
            String b1 = o1.get("f_inputdate") == null ? "" : o1.get("f_inputdate").toString();
            String b2 = o2.get("f_inputdate") == null ? "" : o2.get("f_inputdate").toString();
            if (b1 != null) {
                return b1.compareTo(b2);
            }
            return 0;
        }
    }
	
	/**
	 * execute sql in hibernate
	 * @param sql
	 */
	private void execSQL(final String sql) {
        hibernateTemplate.execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {
                session.createSQLQuery(sql).executeUpdate();
                return null;
            }
        });		
	}
}
