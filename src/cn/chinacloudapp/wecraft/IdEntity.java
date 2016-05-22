package cn.chinacloudapp.wecraft;
import java.util.*;
import java.io.*;
import java.net.*;

import org.json.JSONException;


public class IdEntity {
	public class Author {
		private String AuId;
		private String AfId;
		public String getAuId() {
			return AuId;
		}
		public void setAuId(String auId) {
			AuId = auId;
		}
		public String getAfId() {
			return AfId;
		}
		public void setAfId(String afId) {
			AfId = afId;
		}
		public boolean equals(Author a) {
			return (this.AuId == a.getAuId() && this.AfId == a.getAfId());
		}
	}
	
	private String Id;
	private ArrayList<String> RId;
	private ArrayList<Author> AA;
	// AA.AuId, AA.AfId may be NULL
	private String CId;
	private String JId;
	private ArrayList<String> FId;
	
	public String getId() {
		return Id;
	}
	public void setId(String id) {
		Id = id;
	}
	public ArrayList<String> getRId() {
		return RId;
	}
	public void setRId(ArrayList<String> rId) {
		RId = rId;
	}
	public ArrayList<Author> getAA() {
		return AA;
	}
	public String findAfId(String AuId) {
		for(Author au : AA) 
			if(au.getAuId() == AuId)
				return au.getAfId();
		return null;
	}
	public void setAA(ArrayList<String> aa) {
		AA = new ArrayList<Author>();
		for(int i = 0; i < aa.size(); i ++) {
			try {
				Author au = new Author();
				Map<String, String> detail = JsonHelper.toMap(aa.get(i));
				if(detail.containsKey("AuId"))
					au.setAuId(detail.get("AuId"));
				if(detail.containsKey("AfId"))
					au.setAfId(detail.get("AfId"));
				AA.add(au);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public String getCId() {
		return CId;
	}
	public void setCId(Map<String, String> c) {
		if(c.containsKey("CId"))
				CId = c.get("CId");
	}
	public ArrayList<String> getFId() {
		return FId;
	}
	public void setFId(ArrayList<String> fId) {
		FId = new ArrayList<String>();
		for (int i = 0;  i < fId.size(); i ++) {
			try {
				Map<String, String> ff = JsonHelper.toMap(fId.get(i));
				if (ff.containsKey("FId"))
					FId.add(ff.get("FId"));	
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public String getJId() {
		return JId;
	}
	public void setJId(Map<String, String> j) {
		if (j.containsKey("JId"))
			JId = j.get("JId");
	}
	
	
	
}
