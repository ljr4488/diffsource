package com.scm.diff;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

public class diff {

	public static void main(String[] args) throws Exception {
		if(args.length < 1){
			System.out.println("Must Argument ISID..");
			System.exit(1);
			
		}
		
		int ISID = Integer.valueOf(args[0]);
	
		ArrayList<HashMap<String,String>> scmList = new ArrayList<HashMap<String,String>>();
		ArrayList<HashMap<String,String>> srmsList = new ArrayList<HashMap<String,String>>();
		

		
		String srmsQuery = " SELECT E.SFILENAME, E.SFILEPATH, "+
										"CASE WHEN INSTR(MAX(E.SREVISION),'.') = 0 THEN TO_CHAR(MAX(TO_NUMBER(E.SREVISION))) "+
										"ELSE '1.'||MAX(TO_NUMBER(SUBSTR(E.SREVISION,3,5))) "+
										"END AS SREVISION, F.ISID FROM PROADMIN.SYSDC01 A, "+
										"PROADMIN.SYSDB01 B, PROADMIN.SYSDA02 C, PROADMIN.SYSDB03 D, "+
										"PROADMIN.SYSDC02 E, TNZZI_REQ_MTR_LIST F "+
										"WHERE     A.NREQUESTID = B.NREQUESTID "+
										"AND B.NMODULEID  = C.NMODULEID AND A.NGROUPID   = D.NGROUPID "+
										"AND A.NSTAGEID   = D.NSTAGEID "+
										"AND A.NDEPLOYID  = E.NDEPLOYID "+
										"AND A.NCSRID     = F.REQ_NO "+
										"AND F.ISID = '" + ISID + 
										"' GROUP BY  E.SFILENAME, E.SFILEPATH,F.ISID ORDER BY  E.SFILENAME ";
		
		String scmQuery = 	"select DISTINCT udt.FILENAME, udt.DESTDIR, MAX(udt.EXTVERINDEX)  "+
							"from updatetd u left outer join updatedtd udt on u.UPTDID = udt.UPTDID "+
							"where u.UPTYPE in (0,7,8) and u.ISID = "+ ISID + 
							" and FILENAME is not null "+
							"group by udt.FILENAME, udt.DESTDIR ";

		
		String driver="oracle.jdbc.driver.OracleDriver";			
		Class.forName(driver);

		
		Connection con = DriverManager.getConnection(metaCon, metauser,metapw);
		con.setAutoCommit(true);
		
		
		Statement stmt = con.createStatement();
		ResultSet rs = null;
		rs = stmt.executeQuery(scmQuery);
		while(rs.next()){
			HashMap<String,String> tmp = new HashMap<String,String>();
			String tmpName = rs.getString("FILENAME");
			if(tmpName.indexOf(".class") >= 0)
				continue;
			if(tmpName.toLowerCase().indexOf(".java") >= 0)
				tmpName = tmpName.substring(0,tmpName.lastIndexOf("."));
			tmp.put("DESTDIR", rs.getString("DESTDIR"));
			tmp.put("FILENAME",tmpName);
			tmp.put("VERSION",rs.getString("EXTVERINDEX"));
			scmList.add(tmp);
		}
		
		rs.close();
		stmt.close();
		con.close();

		
		con = DriverManager.getConnection(srmsCon,srmsuser,srmspw);
		stmt = con.createStatement();
		rs = stmt.executeQuery(srmsQuery);
		while(rs.next()){
			HashMap<String,String> tmp = new HashMap<String,String>();
			String tmpName = rs.getString("SFILENAME");
			if(tmpName.toLowerCase().indexOf(".java") >= 0)
				tmpName = tmpName.substring(0,tmpName.lastIndexOf("."));

			tmp.put("DESTDIR", rs.getString("SFILEPATH"));
			tmp.put("FILENAME",tmpName);
			tmp.put("VERSION",rs.getString("SREVISION"));
			srmsList.add(tmp);
		}
		
		rs.close();
		stmt.close();
		con.close();

		if(scmList.size() == 0){
			System.out.println("metaplus scm deploy file count Zero!! Error");
			System.exit(1);
		}
		
		if(srmsList.size() == 0){
			System.out.println("proworks deploy file count Zero!! Error");
			System.exit(1);
		}
		
		if(scmList.size() != srmsList.size()){
			System.out.println("proworsk, metaplus deploy count not matched..!! Error..");
			System.exit(1);
		}
		boolean isNotMatch = false;
		for(int i=0;i<srmsList.size();i++){
			String filename = srmsList.get(i).get("FILENAME");
			String ver = srmsList.get(i).get("VERSION");
			isNotMatch = true;
			for(int j=0;j<scmList.size();j++){
				String ofilename = scmList.get(j).get("FILENAME");
				String over = scmList.get(j).get("VERSION");
				if(filename.equals(ofilename)){
					if(ver.equals(over)){
						isNotMatch = false;
						break;
					}
				}
			}
			if(isNotMatch){
				System.out.println("proworks file - " + filename + " is not matched!!");
				System.exit(1);
			}
		}
		
	
		System.out.println("Matched Files.. OK!!");
		
	}

}


/*
 * PROWORKS HH 33l SELECT A.NDEPLOYID --A.NREQUESTID. --A. SDEPLOYDATE, A. SDEPLOYTIME. HH SUBSTR(A. SDEPLOYDATE,0.4) II' Il SUBSTR (A.SDEPLOYDATE.5.2) II'- SUBSTR(A.SDEPLOYDATE. 7.2) II 'I SUBSTR (A.SDEPLOYTIME,0.2) II I SUBSTR (A.SDEPLOY TIME,3,2) A.N GROUP ID, --D.SGROUPNAME A. NSTAGEID --D.SSTAGENAME. A.NCSRID A. SDESCRIPTION B. SUSERID --B. NMODULEID --C.SMODULENAME --C.SMODULETITLE. --A.SDEPLOYRESULTOUT --A.SDEPLOYRESULTERR. E.SFILENAME, E.SFILEPATH CASE WHEN INSTR (MAX(E.SREVISION) 0 THEN TO CHAR(MAX(TO NUME ELSE 1. MAX TO NUMBER (SUBSTR(E.SREVISION,3,5))) END AS SREVISION NVL(F. CNT,0) AS CNT FROM PROADMIN.SYSDCO1 A. PROADMIN.SYSDB01 B, PROADMIN.SYSDA02 C, PROADMIN SYSDB03 D, PROADMIN.SYSDCO2 E. (SELECT SFILENAME ,COUNT AS CNT FROM (SELECT DISTINCT E.SFILENAME,F. REQ NO FROM PROADMIN. SYSDC01 A, PROADMIN SYSnco? E
 */
