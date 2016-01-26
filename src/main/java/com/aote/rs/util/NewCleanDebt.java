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
 * ��Ƿ������    Ҧ����
 * ��˼·�����������
 * 1. 1.19-1.31 ��һ���ǳ���ֱ�Ӳ�����Ƿ���������������Ƿ�ѣ������û������˻���
 *    �ڶ������շѣ�����շ�û�м�¼���������
 *    			        ����У��ڳ����в��ң��������Ѽ�¼��û�ж�Ӧ��Ƿ�Ѽ�¼������1.19֮ǰ��
 *    								         ���û��Ƿ�Ѽ�¼���������
 *    									�����Ƿ�Ѽ�¼����Ƿ�Ѽ�¼����������Ƿ���ң�����һ������Ƿ�Ѳ�Ϊ0������һ�������ٴβ�����Ƿ��������
 * 2. 1.19-1.31 ��һ�����շѣ��ڳ����в��ң��������Ѽ�¼��û�ж�Ӧ��Ƿ�Ѽ�¼������1.19֮ǰ��
 *    								         ���û��Ƿ�Ѽ�¼�����ߴ����շ�ֱ�Ӳ�����Ƿ��
 *    									�����Ƿ�Ѽ�¼����Ƿ�Ѽ�¼����������Ƿ��������
 *    			�ڶ����ǳ������û�г����������
 *    						   ����г�������Դ������������Ƿ��������
 */

/**
 * ����ṹ��
 * 
 * if����û��
 * {
 * 		if�շ�û��
 * 		{
 * 			continue��
 * 		}
 * 		else
 * 		{
 * 			�ڳ����в��ң��������Ѽ�¼��û�ж�Ӧ��Ƿ�Ѽ�¼������1.19֮ǰ��
 *    				         ���û��Ƿ�Ѽ�¼�����ݴ����շ�ֱ�Ӳ�����Ƿ��
 *    				         �����Ƿ�Ѽ�¼����Ƿ�Ѽ�¼����������Ƿ��������
 * 		}
 * }
 * else
 * {
 * 		if�շ�û��
 * 		{
 * 			ֱ�Ӳ�����Ƿ���������������Ƿ�ѣ������û������˻���
 * 		}
 * 		else
 * 		{
 * 			if������ǰ
 * 			{
 * 				1.19-1.31 ��һ���ǳ���ֱ�Ӳ�����Ƿ���������������Ƿ�ѣ������û������˻���
 * 				�ڳ����в��ң��������Ѽ�¼��û�ж�Ӧ��Ƿ�Ѽ�¼������1.19֮ǰ��
 *    				         ���û��Ƿ�Ѽ�¼���������
 *    				         �����Ƿ�Ѽ�¼����Ƿ�Ѽ�¼����������Ƿ���ң�����һ������Ƿ�Ѳ�Ϊ0������һ�������ٴβ�����Ƿ��������
 * 			}
 * 			else
 * 			{
 * 				�ڳ����в��ң��������Ѽ�¼��û�ж�Ӧ��Ƿ�Ѽ�¼������1.19֮ǰ��
 *    				         ���û��Ƿ�Ѽ�¼�����ݴ����շ�ֱ�Ӳ�����Ƿ��
 *    				         �����Ƿ�Ѽ�¼����Ƿ�Ѽ�¼����������Ƿ��������
 *    			�Եڶ������������Ƿ��������
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
			
			//��ѯһ���û�����
			String UserFilesSql = "from t_userfiles where id = ";
			List<Object> UserFilesList = new ArrayList<Object>();
			Map<String, Object> UserFilesMap = new HashMap<String, Object>();
			//��ѯһ�������¼
			String HandPlanOneSql = "from t_handplan where f_state = '�ѳ���' and f_inputdate >= '2015-01-19' and f_inputdate < '2015-02-01' and (oughtamount <> 0 or oughtfee <> 0) and f_userid = ";
			List<Object> HandPlanList = new ArrayList<Object>();
			Map<String, Object> HandPlanOneMap = new HashMap<String, Object>();
			//��ѯһ�����Ѽ�¼
			String SellingGasSql = "from t_sellinggas where f_deliverydate >= '2015-01-19' and f_deliverydate < '2015-02-01' and f_payfeevalid = '��Ч' and f_payfeetype = '�����շ�' and f_userid = ";
			List<Object> SellingGasList = new ArrayList<Object>();
			Map<String, Object> SellingGasOneMap = new HashMap<String, Object>();
			//���ҽ��Ѽ�¼��Ӧ��Ƿ�Ѽ�¼
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
			//����
			double StairOnePrice = 0;
			//�û������˻������
			double AccountZHYE = 0;
			//���������Ƿ�ѽ��
			double DebtMoney = 0;
			double DebtYE = 0;
			//�û�����id��Χ
			for(int i = 853671; i <= 909806; i++)
			{
				//��ѯ����
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
				//������ǻ�����continue.
				if(!GasMeterStyle.equals("����"))
				{
					continue;
				}
				AccountZHYEStr = UserFilesMap.get("f_accountzhye") == null ? "0" : UserFilesMap.get("f_accountzhye").toString();
				//��������˻����Ϊ0����continue.
				if(AccountZHYEStr.equals("0"))
				{
					continue;
				}
				AccountZHYE = Double.parseDouble(AccountZHYEStr);
				UserID = UserFilesMap.get("f_userid") == null ? "" : UserFilesMap.get("f_userid").toString();
				StairOnePriceStr = UserFilesMap.get("f_stair1price") == null ? "" : UserFilesMap.get("f_stair1price").toString();
				StairOnePrice = Double.parseDouble(StairOnePriceStr);
				
				//��ѯ����
				if(HandPlanList.size() != 0)
					HandPlanList.clear();
				HandPlanList = this.hibernateTemplate.find(HandPlanOneSql + "'" + UserID + "'");
				
				//��ѯ����
		        if(SellingGasList.size() != 0)
		        	SellingGasList.clear();
		        SellingGasList = this.hibernateTemplate.find(SellingGasSql + "'" + UserID + "'");
		        
		        //û����
		        if(HandPlanList.size() == 0)
		        {
		        	//û����
		        	if(SellingGasList.size() == 0)
		        	{
		        		continue;
		        	}
		        	//�н���
		        	else
		        	{
		        		if(SellingGasOneMap.size() != 0)
		        			SellingGasOneMap.clear();
		        		SellingGasOneMap = (Map<String, Object>) SellingGasList.get(0);
		        		if(DebtList.size() != 0)
		        			DebtList.clear();
		        		DebtList = this.hibernateTemplate.find(DebtSql + (SellingGasOneMap.get("id") == null ? "''" : "'" + SellingGasOneMap.get("id").toString() + "'"));
		        		//û��1.19֮ǰ�ĳ����¼�����ݴ����շ�ֱ�Ӳ�����Ƿ��
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
									Double.toString(DebtYE), "���ѿ۳�", NetWork, Operator,
									SellGasDate, SellGasTime, "0", SellIDStr)) {
		        				continue;
		        			}
		        			else
		        			{
		        				return "not ok, because of inserting failed!!! error code is 1. The id of t_userfiles is : " + i;
		        			}
		        		}
		        		//��1.19֮ǰ�ĳ����¼��������Ƿ
		        		for(int j = 0; j < DebtList.size(); j++)
		        		{
		        			if(DebtMapList.size() != 0)
		        				DebtMapList.clear();
		        			DebtMapList.add((Map<String, Object>) DebtList.get(j));
		        		}
		        		//�Խ��Ѷ�Ӧ�ĳ����¼������������
		        		Collections.sort(DebtMapList, new MapComparator());
		        		//��ʼ����������Ƿ��¼
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
									Double.toString(DebtYE), "����۳�", 
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
		        //�г���
		        else
		        {
		        	//û����
		        	if(SellingGasList.size() == 0)
		        	{
		        		if(HandPlanOneMap.size() != 0)
		        			HandPlanOneMap.clear();
		        		HandPlanOneMap = (Map<String, Object>) HandPlanList.get(0);
		        		DebtMoneyStr = HandPlanOneMap.get("f_debtmoney") == null ? "0" : HandPlanOneMap.get("f_debtmoney").toString();
		        		DebtMoney = Double.parseDouble(DebtMoneyStr);
		        		//ֱ�Ӳ�����Ƿ���������������Ƿ�ѣ������û������˻���
						if (!CreatDebt(UserID, DebtMoneyStr,
								AccountZHYE > DebtMoney ? Double.toString(DebtMoney) : Double.toString(AccountZHYE),
								AccountZHYE > DebtMoney ? "0" : Double.toString(DebtMoney - AccountZHYE),
								HandPlanOneMap.get("f_inputdate") == null ? "" : HandPlanOneMap.get("f_inputdate").toString(),
								StairOnePriceStr, AccountZHYEStr,
								AccountZHYE < DebtMoney ? "0" : Double.toString(AccountZHYE - DebtMoney),
								"����۳�", 
								HandPlanOneMap.get("f_network") == null ? "" : HandPlanOneMap.get("f_network").toString(), 
								HandPlanOneMap.get("f_operator") == null ? "" : HandPlanOneMap.get("f_operator").toString(),
								HandPlanOneMap.get("f_inputdate") == null ? "" : HandPlanOneMap.get("f_inputdate").toString(),
								HandPlanOneMap.get("f_inputdate") == null ? "" : HandPlanOneMap.get("f_inputdate").toString(),
								HandPlanOneMap.get("id") == null ? "0" : HandPlanOneMap.get("id").toString(), "0"))
	        			{
	        				return "not ok, because of inserting failed!!! error code is 3. The id of t_userfiles is : " + i;
	        			}
		        	}
		        	//�н���
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
		        		
		        		//������ǰ
		        		if(HandPlanOneDate.compareTo(SellGasDate) < 0)
		        		{
		        			DebtMoneyStr = HandPlanOneMap.get("f_debtmoney") == null ? "0" : HandPlanOneMap.get("f_debtmoney").toString();
			        		DebtMoney = Double.parseDouble(DebtMoneyStr);
			        		//ֱ�Ӳ�����Ƿ���������������Ƿ�ѣ������û������˻���
							if (!CreatDebt(UserID, DebtMoneyStr,
									AccountZHYE > DebtMoney ? Double.toString(DebtMoney) : Double.toString(AccountZHYE),
									AccountZHYE > DebtMoney ? "0" : Double.toString(DebtMoney - AccountZHYE),
									HandPlanOneMap.get("f_inputdate") == null ? "" : HandPlanOneMap.get("f_inputdate").toString(),
									StairOnePriceStr, AccountZHYEStr,
									AccountZHYE < DebtMoney ? "0" : Double.toString(AccountZHYE - DebtMoney),
									"����۳�", 
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
			        		//�����µ��û��������
			        		if(UserFilesList.size() != 0)
								UserFilesList.clear();
							UserFilesList = this.hibernateTemplate.find(UserFilesSql + i);
							if(UserFilesMap.size() != 0)
								UserFilesMap.clear();
							UserFilesMap = (Map<String, Object>) UserFilesList.get(0);
							AccountZHYEStr = UserFilesMap.get("f_accountzhye") == null ? "0" : UserFilesMap.get("f_accountzhye").toString();
							AccountZHYE = Double.parseDouble(AccountZHYEStr);
			        		//���ݵڶ����շѲ�����Ƿ��
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
										Double.toString(DebtYE), "���ѿ۳�", NetWork, Operator,
										SellGasDate, SellGasTime, "0", SellIDStr)) {
			        				continue;
			        			}
			        			else
			        			{
			        				return "not ok, because of inserting failed!!! error code is 5. The id of t_userfiles is : " + i;
			        			}
			        		}
			        		//��1.19֮ǰ�ĳ����¼��������Ƿ
			        		for(int j = 0; j < DebtList.size(); j++)
			        		{
			        			if(DebtMapList.size() != 0)
			        				DebtMapList.clear();
			        			DebtMapList.add((Map<String, Object>) DebtList.get(j));
			        		}
			        		//�Խ��Ѷ�Ӧ�ĳ����¼������������
			        		Collections.sort(DebtMapList, new MapComparator());
			        		//��ʼ����������Ƿ��¼
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
									    Double.toString(DebtYE) , "����۳�", 
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
			        		
			        		//��ѯ����
							if(HandPlanList.size() != 0)
								HandPlanList.clear();
							HandPlanList = this.hibernateTemplate.find(HandPlanOneSql + "'" + UserID + "'");
							if(HandPlanOneMap.size() != 0)
			        			HandPlanOneMap.clear();
			        		HandPlanOneMap = (Map<String, Object>) HandPlanList.get(0);
			        		DebtMoneyStr = HandPlanOneMap.get("f_debtmoney") == null ? "0" : HandPlanOneMap.get("f_debtmoney").toString();
			        		DebtMoney = Double.parseDouble(DebtMoneyStr);
			        		//���������Ƿ�����µĳ���Ƿ��Ϊ0����continue.
							if(DebtMoney == 0)
							{
								continue;
							}
							//���������Ƿ�����µĳ���Ƿ�Ѳ�Ϊ0
							//��ѯ����
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
			        		//ֱ�Ӳ�����Ƿ���������������Ƿ�ѣ������û������˻���
							if (!CreatDebt(UserID, DebtMoneyStr,
									DebtMoneyStr,
									"0",
									HandPlanOneMap.get("f_inputdate") == null ? "" : HandPlanOneMap.get("f_inputdate").toString(),
									StairOnePriceStr, AccountZHYEStr,
									Double.toString(AccountZHYE - DebtMoney),
									"����۳�", 
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
		        		//������ǰ
		        		else
		        		{
			        		SellingGasOneMap = (Map<String, Object>) SellingGasList.get(0);
			        		if(DebtList.size() != 0)
			        			DebtList.clear();
			        		DebtList = this.hibernateTemplate.find(DebtSql + (SellingGasOneMap.get("id") == null ? "''" : "'" + SellingGasOneMap.get("id").toString() + "'"));
			        		//û��1.19֮ǰ�ĳ����¼�����ݴ����շ�ֱ�Ӳ�����Ƿ��
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
										Double.toString(DebtYE), "���ѿ۳�", NetWork, Operator,
										SellGasDate, SellGasTime, "0", SellIDStr)) {
			        				continue;
			        			}
			        			else
			        			{
			        				return "not ok, because of inserting failed!!! error code is 8. The id of t_userfiles is : " + i;
			        			}
			        		}
			        		//��1.19֮ǰ�ĳ����¼��������Ƿ
			        		for(int j = 0; j < DebtList.size(); j++)
			        		{
			        			if(DebtMapList.size() != 0)
			        				DebtMapList.clear();
			        			DebtMapList.add((Map<String, Object>) DebtList.get(j));
			        		}
			        		//�Խ��Ѷ�Ӧ�ĳ����¼������������
			        		Collections.sort(DebtMapList, new MapComparator());
			        		//��ʼ����������Ƿ��¼
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
										Double.toString(DebtYE), "����۳�", DebtOneMap.get("f_network") == null ? "" : DebtOneMap.get("f_network").toString(), 
										DebtOneMap.get("f_operator") == null ? "" : DebtOneMap.get("f_operator").toString(),
										DebtOneMap.get("f_inputdate") == null ? "" : DebtOneMap.get("f_inputdate").toString(),
										SellingGasOneMap.get("f_deliverytime") == null ? "" : SellingGasOneMap.get("f_deliverytime").toString(), 
										DebtOneMap.get("id") == null ? "0" : DebtOneMap.get("id").toString(), 
										SellingGasOneMap.get("id") == null ? "0" : SellingGasOneMap.get("id").toString()))
			        			{
			        				return "not ok, because of inserting failed!!! error code is 9. The id of t_userfiles is : " + i;
			        			}
			        		}
			        		
			        		//��ѯ����
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
			        		//ֱ�Ӳ�����Ƿ���������������Ƿ�ѣ������û������˻���
							if (!CreatDebt(UserID, DebtMoneyStr,
									AccountZHYE > DebtMoney ? Double.toString(DebtMoney) : Double.toString(AccountZHYE),
									AccountZHYE > DebtMoney ? "0" : Double.toString(DebtMoney - AccountZHYE),
									HandPlanOneMap.get("f_inputdate") == null ? "" : HandPlanOneMap.get("f_inputdate").toString(),
									StairOnePriceStr, AccountZHYEStr,
									AccountZHYE < DebtMoney ? "0" : Double.toString(AccountZHYE - DebtMoney),
									"����۳�", 
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
	
	//������Ƿ
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
					+ ", '��Ч', '"
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
			// �����û�����
			String UpdateUserFileSql = "update t_userfiles set f_accountzhye = " + AccountZHYE + " where f_userid = '"
					+ UserID + "'";
			execSQL(UpdateUserFileSql);
			// ���³����
			String UpdateHandPlan = "update t_handplan set f_debtmoney = " + DebtMoney + " where id = " + HandID;
			execSQL(UpdateHandPlan);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	//��List<Map<String, Object>>���f_inputdate�ֶν�����������
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
