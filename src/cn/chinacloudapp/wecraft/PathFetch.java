package cn.chinacloudapp.wecraft;
import java.util.*;
import java.io.*;
import java.net.*;

import org.json.*;

import cn.chinacloudapp.wecraft.IdEntity.Author;


public class PathFetch {
	//	https://oxfordhk.azure-api.net/academic/v1.0/evaluate?expr=
	//	Id=1966517895 || 
	//	如果使用AA.AuId, AA.AfId, F.FId, J.JId, C.Id, RId，需要加上composite(AA.AuId=2096482110) 
	//	&attributes=Id,C.CId,RId,AA.AuId,AA.AfId,F.FId,J.JId
	//	&subscription-key=f7cc29509a8443c5b3a5e56b0e38b5a6
	public static int count = 0;
	public static int api_count = 0;
	private long start = System.currentTimeMillis();
	public static Map<String, String> cache = new HashMap<String, String>();
	public static Map<String, IdEntity> entityCache = new HashMap<String, IdEntity>();
	public static Map<String, ArrayList<IdEntity>> auidInverseCache = new HashMap<String, ArrayList<IdEntity>>();
	public static Map<String, ArrayList<IdEntity>> idInverseCache = new HashMap<String, ArrayList<IdEntity>>();
	private static List<ArrayList<String>> ret = new ArrayList<ArrayList<String>>();
	private String urlHead = "https://oxfordhk.azure-api.net/academic/v1.0/";
	private String urlAction = "evaluate";
	private String urlExpression = "?expr=";
	private String[] urlAttribute = {"&count=100&attributes=Id,C.CId,RId,AA.AuId,AA.AfId,F.FId,J.JId",
									"&count=100&attributes=Id,AA.AuId,AA.AfId",
									"&count=100&attributes=AA.AuId",
									"&count=100&attributes=Id"};
	//private String urlAttribute = "&attributes=Id,C.CId,RId,AA.AuId,AA.AfId,F.FId,J.JId";
	private String urlKey = "&subscription-key=f7cc29509a8443c5b3a5e56b0e38b5a6";
	
	public static <T> ArrayList<T> deepCopy(ArrayList<T> src) {  
		try {
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();  
		    ObjectOutputStream out = new ObjectOutputStream(byteOut);  
		    out.writeObject(src);  
		  
		    ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());  
		    ObjectInputStream in = new ObjectInputStream(byteIn);  
		    @SuppressWarnings("unchecked")  
		    ArrayList<T> dest = (ArrayList<T>) in.readObject();  
		    return dest;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	    
	    return null;  
	}  
	
	public List<ArrayList<String>> doDistribute(String sourceType, String sourceId, String targetType,String targetId) 
	{
		PathFetch.count = 0;
		PathFetch.api_count = 0;
		ret = new ArrayList<ArrayList<String>>();
		//ArrayList<String> list = new ArrayList<String>();
		//list.add(sourceId);
		//doCalculate(list, sourceType, sourceId, targetType, targetId, 0);
		//ret.add(doOneHop(sourceType, sourceId, targetType, targetId));
		ArrayList<String> tmp = doOneHop(sourceType, sourceId, targetType, targetId);
		if(tmp != null && !tmp.isEmpty()) {
			ret.add(tmp); 
			//System.out.println("firstReuqest:" + tmp.toString());
		}
		ArrayList<ArrayList<String>> tm = doTwoHop(sourceType, sourceId, targetType, targetId);
		if(tm != null && !tm.isEmpty()) {
			ret.addAll(tm); 
			//System.out.println("secondRequest:"+ tm.toString());
		}
		//ret.addAll(doTwoHop(sourceType, sourceId, targetType, targetId));
		tm = doThreeHop(sourceType, sourceId, targetType, targetId);
		if(tm != null && !tm.isEmpty()) {
			ret.addAll(tm); 
			//System.out.println("thirdRequest:" + tm.toString());
		}
		//ret.addAll(doThreeHop(sourceType, sourceId, targetType, targetId));
		System.out.println(ret);
		PathFetch.count = ret.size();
		return PathFetch.ret;
	}

	public ArrayList<String> doOneHop(String sourceType, String sourceId, String targetType, String targetId)
	{
		fetchContent(sourceType, sourceId); fetchContent(targetType, targetId);
		//if(sourceType == "Id")
			fillEntity(sourceType, sourceId);
		//if(targetType.equals("Id"))
			fillEntity(targetType, targetId);
		if(sourceType.equals("AA.AuId") && targetType.equals("AA.AuId"))
			return null;
		IdEntity source = entityCache.get(sourceId), target = entityCache.get(targetId);
		ArrayList<String> arr = new ArrayList<String>();
		//System.out.println("OneHop:" + source.getRId().toString());
		if (sourceType.equals("Id") && targetType.equals("Id") && source.getRId() != null && source.getRId().contains(targetId)) {
			arr.add(sourceId); arr.add(targetId);
		} 
		if(sourceType.equals("Id") && targetType.equals("AA.AuId") && source.getAA() != null) {
			for(IdEntity.Author au : source.getAA())
				if(au.getAuId().equals(targetId)) {
					arr.add(sourceId); arr.add(targetId);
					break;
				}
		}

		if(sourceType.equals("AA.AuId") && targetType.equals("Id") && target.getAA() != null) {
			//System.out.println(target.getAA());
			//System.out.println("sourceId"+ sourceId);
			for(IdEntity.Author au : target.getAA()) {
				//System.out.println("AuId:" + au.getAuId());
				//System.out.println("AfId:" + au.getAfId());
				//System.out.println(au.getAuId().equals(sourceId));
				if(au.getAuId().equals(sourceId)) {
					arr.add(sourceId); arr.add(targetId);
					break;
				}
			}
		}
		//if(arr != null && !arr.isEmpty())
			//System.out.println("doOneHop:" + arr.toString());
		return arr;
	}
	
	public ArrayList<String> solveCIdJId(String type, IdEntity source, IdEntity target)
	{
		ArrayList<String> ret = new ArrayList<String>();
		if (type.equals("C.CId") && source.getCId() != null && target.getCId() != null && source.getCId().equals(target.getCId())) {
			ret.add(source.getId()); ret.add(source.getCId()); ret.add(target.getId());
		}
		if (type.equals("J.JId") && source.getJId() != null && target.getJId() != null &&source.getJId().equals(target.getJId())) {
			ret.add(source.getId()); ret.add(source.getJId()); ret.add(target.getId());
		}
		//System.out.println("solveCIdJId:" + ret.toString());
		return ret;
	}
	
	public ArrayList<ArrayList<String>> solveFId(String type, IdEntity source, IdEntity target)

	{
		ArrayList<ArrayList<String>> ret = new ArrayList<ArrayList<String>>();
		if(source.getFId() == null || target.getFId() == null)
			return null;
		for (String s : source.getFId()) {
			if(target.getFId() != null && target.getFId().contains(s)) {
				ArrayList<String> tmp = new ArrayList<String>();
				//tmp.add(source.getId()); 
				tmp.add(s); tmp.add(target.getId());
				ret.add(tmp);
			}
		}
		//System.out.println("solveFId:" + ret.toString());
		return new ArrayList(new HashSet(ret));
	}
	
	public ArrayList<ArrayList<String>> solveAfId(String type,String sourceId, ArrayList<IdEntity> source,
															String targetId, ArrayList<IdEntity> target)
	{
		if(source == null || target == null || source.contains(null) || target.contains(null))
			return null;
		ArrayList<ArrayList<String>> ret = new ArrayList<ArrayList<String>>();
		ArrayList<String> sourceAu = new ArrayList<String>(), 
				targetAu = new ArrayList<String>();
		for(IdEntity e : source) {
			if(e.getAA() == null)
				continue;
			for(IdEntity.Author au : e.getAA())
				if(au.getAuId().equals(sourceId) && au.getAfId() != null && !sourceAu.contains(au.getAfId()))
					sourceAu.add(au.getAfId());
		}
		for(IdEntity e : target) {
			if(e.getAA() == null)
				continue;
			for(IdEntity.Author au : e.getAA())
				if(au.getAuId().equals(targetId) && au.getAfId() != null && !targetAu.contains(au.getAfId()))
					targetAu.add(au.getAfId());
		}
		if(sourceAu == null || targetAu == null)
			return null;
		for(String AfId : sourceAu)
			if(targetAu.contains(AfId)) {
				ArrayList<String> tmp = new ArrayList<String>();
				tmp.add(sourceId); tmp.add(AfId); tmp.add(targetId);
				ret.add(tmp);
			}
		//System.out.println("solveAfId:" + ret.toString());
		return new ArrayList(new HashSet(ret));
	}
	
	public void aheadAll(String sourceId, ArrayList<ArrayList<String>> tail, ArrayList<ArrayList<String>> ret)
	{
		//System.out.println("aheadAll:" + ret.toString());
		if(tail == null)
			return;
		for(ArrayList<String> t : tail) {
			if (t != null && !t.isEmpty()) {
				ArrayList<String> tmp = new ArrayList<String>();
				tmp.add(sourceId); tmp.addAll(t);
				ret.add(tmp);
			}
		}
		//System.out.println("aheadAll:" + ret.toString());
		ret = new ArrayList(new HashSet(ret));
		return;
	}
	
	public void afterAll(ArrayList<ArrayList<String>> head, String tailId, ArrayList<ArrayList<String>> ret)
	{
		//System.out.println("afterAll:" + ret.toString());
		if(head == null)
			return;
		for(ArrayList<String> h : head) {
			if (h != null && !h.isEmpty()) {
				ArrayList<String> tmp = new ArrayList<String>();
				tmp.addAll(h); tmp.add(tailId);
				ret.add(tmp);
			}
		}
		ret = new ArrayList(new HashSet(ret));
		//System.out.println("afterAll:" + ret.toString());
		return;
	}
	
	public ArrayList<IdEntity> getIdInverseEntity(String id)
	{
		if(PathFetch.idInverseCache.containsKey(id))
			return PathFetch.idInverseCache.get(id);
		ArrayList<IdEntity> inver = new ArrayList<IdEntity>();
		Map<String, String> total = JsonHelper.toMap(fetchContent("RId", id));
		ArrayList<String> entities = JsonHelper.toList(total.get("entities"));
		for(String e : entities) {
			if(JsonHelper.toMap(e).get("Id") == null)
				continue;
			String Id = JsonHelper.toMap(e).get("Id");
			//PathFetch.cache.put(Id, e);
			fetchContent("Id", Id);
			IdEntity tmp = fillEntity("Id", Id);
			inver.add(tmp);
			// In function fillEntity(Id), entityCache already stores those missed.
			//PathFetch.entityCache.put(Id, tmp);
		}
		if(inver != null && !inver.isEmpty())
			PathFetch.idInverseCache.put(id, inver);
		return inver;
	}
	
	public ArrayList<IdEntity> getAuIdInverseEntity(String id)
	{
		if(PathFetch.auidInverseCache.containsKey(id))
			return PathFetch.auidInverseCache.get(id);
		ArrayList<IdEntity> inver = new ArrayList<IdEntity>();
		Map<String, String> total = JsonHelper.toMap(fetchContent("AA.AuId", id));
		ArrayList<String> entities = JsonHelper.toList(total.get("entities"));
		for(String e : entities) {
			if(JsonHelper.toMap(e).get("Id") == null)
				continue;
			String Id = JsonHelper.toMap(e).get("Id");
			if(Id != null && !Id.isEmpty() && e != null && !e.isEmpty()) {
				//PathFetch.cache.put(Id, fetch);
				fetchContent("Id", Id);
				//System.out.println(Id);
				IdEntity tmp = fillEntity("Id", Id);
				inver.add(tmp);
			}
			// In function fillEntity(Id), entityCache already stores those missed.
			//PathFetch.entityCache.put(Id, tmp);
		}
		PathFetch.auidInverseCache.put(id, inver);
		return inver;
	}
	
	public ArrayList<ArrayList<String>> doTwoHop(String sourceType, String sourceId, String targetType, String targetId)
	{
		fetchContent(sourceType, sourceId); fetchContent(targetType, targetId);
		//if(sourceType == "Id")
			fillEntity(sourceType, sourceId);
		//if(targetType == "Id")
			fillEntity(sourceType, targetId);
		ArrayList<ArrayList<String>> ret = new ArrayList<ArrayList<String>>();
		ArrayList<String> reg;
		IdEntity source = entityCache.get(sourceId), target = entityCache.get(targetId);
		ArrayList<String> rid;
		if(sourceType.equals("Id")) {
			if(targetType.equals("Id")) {
				if(source.getRId() != null) {
				for(String r : source.getRId()) {
					ArrayList<String> tmp = new ArrayList<String>(), one;
					if ((one = doOneHop("Id", r, "Id", targetId)) != null && !one.isEmpty()) {
						tmp.add(sourceId); tmp.addAll(one); 
						ret.add(tmp);
					}	
				}}
				if((reg = solveCIdJId("C.CId", source, target)) != null && !reg.isEmpty())
					ret.add(reg);
				if((reg = solveCIdJId("J.JId", source, target)) != null && !reg.isEmpty())
					ret.add(reg);
				aheadAll(source.getId(), solveFId("FId", source, target), ret);
				if(source.getAA() != null) {
				for(IdEntity.Author au : source.getAA()) {
					ArrayList<String> tmp = new ArrayList<String>();
					if((reg = doOneHop("AA.AuId", au.getAuId(), "Id", targetId)) != null && !reg.isEmpty()) {
						tmp.add(source.getId()); tmp.addAll(reg);
						ret.add(tmp);
					}
				}
				}
			}
			else {
				if(source.getRId() != null) {
				for(String r : source.getRId()) {
					ArrayList<String> tmp = new ArrayList<String>(), one;
					if ((one = doOneHop("Id", r, "AA.AuId", targetId)) != null && !one.isEmpty()) {
						tmp.add(sourceId); tmp.addAll(one); 
						ret.add(tmp);
					}
				}
				}
			}
		}
		if(sourceType.equals("AA.AuId")) {
			if(targetType.equals("Id")) {
				ArrayList<IdEntity> inver = getIdInverseEntity(targetId);
				if(inver != null) {
				for(IdEntity e : inver) {
					ArrayList<String> tmp = new ArrayList<String>(), one;
					if((one = doOneHop("AA.Auid", sourceId, "Id", e.getId())) != null && !one.isEmpty()) {
						tmp.addAll(one); tmp.add(targetId);
						ret.add(tmp);
					}
				}
				}
			}
			else {
				ArrayList<String> sourceAu = new ArrayList<String>(), 
											targetAu = new ArrayList<String>();
				ArrayList<IdEntity> s = this.getAuIdInverseEntity(sourceId),
									t = this.getAuIdInverseEntity(targetId);
					
				ret.addAll(this.solveAfId("AA.AfId", sourceId, s, targetId, t));
				for(IdEntity e : s)
					if(e != null && t != null && t.contains(e)) {
						ArrayList<String> tmp = new ArrayList<String>();
						tmp.add(sourceId); tmp.add(e.getId()); tmp.add(targetId);
						//System.out.println(tmp.toString());
						ret.add(tmp);
					}
			}
		}
		//System.out.println("doTwoHop:" + ret.toString());
		return new ArrayList(new HashSet(ret));
	}
	
	public ArrayList<ArrayList<String>> doThreeHop(String sourceType, String sourceId, String targetType, String targetId)
	{
		fetchContent(sourceType, sourceId); fetchContent(targetType, targetId);
		//if(sourceType == "Id")
			fillEntity(sourceType, sourceId);
		//if(targetType == "Id")
			fillEntity(targetType, targetId);
		ArrayList<ArrayList<String>> ret = new ArrayList<ArrayList<String>>();
		IdEntity source = PathFetch.entityCache.get(sourceId), target = PathFetch.entityCache.get(targetId);
		if(sourceType.equals("Id")) {
			if(targetType.equals("Id")) {
				if(source.getRId() != null) {
				for(String s : source.getRId())
					this.aheadAll(sourceId, doTwoHop("Id", s, "Id", targetId), ret);
				}
					
				ArrayList<IdEntity> inver = this.getIdInverseEntity(targetId);
				if(inver != null) {
				for(IdEntity e : inver) {
					ArrayList<String> reg;
					if(e != null && (reg = solveCIdJId("C.CId", source, e)) != null && !reg.isEmpty())
						ret.add(reg);
					if(e != null && (reg = solveCIdJId("J.JId", source, e)) != null && !reg.isEmpty())
						ret.add(reg);
					this.aheadAll(sourceId, solveFId("FId", source, e), ret);
				}
				}
				ArrayList<String> auid = new ArrayList<String>();
				if(source.getAA() != null) {
				for(IdEntity.Author au : source.getAA())
					auid.add(au.getAuId());
				for(String s : auid)
					this.aheadAll(sourceId, doTwoHop("AA.AuId", s, "Id", targetId), ret);
				}
			} else {
				ArrayList<IdEntity> t = getAuIdInverseEntity(targetId);
				if(t != null) {
				for(IdEntity e : t) {
					this.afterAll(doTwoHop("Id", sourceId, "Id", e.getId()), targetId, ret);
					for(IdEntity.Author au : e.getAA())
						this.aheadAll(sourceId, 
								solveAfId("AA.AfId", au.getAuId(), getAuIdInverseEntity(au.getAuId()), targetId, t), 
																		ret);
				}
				}
			}
		}
		if(sourceType.equals("AA.AuId")) {
			ArrayList<IdEntity> inver = this.getAuIdInverseEntity(sourceId);
			if(targetType.equals("Id")) {
				if(inver != null && !inver.isEmpty()) {
				for(IdEntity e : inver)
					if(e != null && e.getId() != null)
						this.aheadAll(sourceId, doTwoHop("Id", e.getId(), "Id", targetId), ret);
				if(target.getAA() != null && !inver.isEmpty()) {
				for(IdEntity.Author au : target.getAA())
					this.afterAll(this.solveAfId("AA.AfId", sourceId, inver, 
															au.getAuId(), this.getAuIdInverseEntity(au.getAuId())), 
															targetId, ret);
				}
				}
			} else {
				if(inver != null && !inver.isEmpty()) {
				for(IdEntity e : inver)
					this.aheadAll(sourceId, doTwoHop("Id", e.getId(), "AA.AuId", targetId), ret);
				}
			}
		}
		//System.out.println("doThreeHop:" + ret.toString());
		return new ArrayList(new HashSet(ret));
	}
	
	public IdEntity fillEntity(String type, String id)
	{
		if(entityCache.containsKey(id))
			return entityCache.get(id);

		String json = fetchContent(type, id);
		if(json.contains("statusCode") || json.contains("error"))
			return null;
		
		Map<String, String> total = JsonHelper.toMap(fetchContent(type, id));
		//System.out.println(fetchContent(type, id));
		ArrayList<String> entities = JsonHelper.toList(total.get("entities"));
		for(String e : entities) {
			Map<String, String> en = JsonHelper.toMap(e);
			IdEntity idEntity = new IdEntity();
			if (en.containsKey("Id"))
				idEntity.setId(en.get("Id"));
			
			if (en.containsKey("RId"))
				idEntity.setRId(JsonHelper.toList(en.get("RId")));
				
			if (en.containsKey("AA"))
				idEntity.setAA(JsonHelper.toList(en.get("AA")));
	
			if (en.containsKey("C"))
				idEntity.setCId(JsonHelper.toMap(en.get("C")));

			if (en.containsKey("F"))
				idEntity.setFId(JsonHelper.toList(en.get("F")));
		
			if (en.containsKey("J"))
				idEntity.setJId(JsonHelper.toMap(en.get("J")));
			
			entityCache.put(id, idEntity);

		}

		return entityCache.get(id);
	}
	

	public void doCalculate(ArrayList<String> parent, String sourceType, String sourceId, String targetType, String targetId, int depth)
	{ 
		if (depth >= 3 || System.currentTimeMillis() - this.start > 4 * 60 *1000)
			return;
		
		String source = this.fetchContent(sourceType, sourceId);
		if(source.contains("statusCode") || source.contains("error"))
			return;
		
			Map<String, String> total = JsonHelper.toMap(source);
			
			if (total.containsKey("entities")) {
				ArrayList<String> entity = JsonHelper.toList(total.get("entities"));
				for (String e : entity) {
					Map<String, String> en = JsonHelper.toMap(e);
					String Id = "";
					if (en.containsKey("Id")) {
						 Id = en.get("Id");
						this.justify(parent, "Id", Id, sourceId, targetType, targetId, depth);
					}
					
					if (en.containsKey("RId")) {
						ArrayList<String> rid = JsonHelper.toList(en.get("RId"));
						for(String r : rid) {
							this.justify(parent, "Id", r, sourceId, targetType, targetId, depth);
						}
					}
					if (en.containsKey("AA")) {
						ArrayList<String> aa = JsonHelper.toList(en.get("AA"));
						for(String aaDetail : aa) {
							Map<String, String> detail = JsonHelper.toMap(aaDetail);
							if (detail.containsKey("AuId")) {								
								this.justify(parent, "AA.AuId", detail.get("AuId"), sourceId, targetType, targetId, depth);
							}
							if (detail.containsKey("AfId")) {								
								this.justify(parent, "AA.AfId", detail.get("AfId"), sourceId, targetType, targetId, depth);
							}	
						}
					}
					if (en.containsKey("C")) {
						Map<String, String> c = JsonHelper.toMap(en.get("C"));
						if (c.containsKey("CId")) {
							this.justify(parent, "C.CId", c.get("CId"), sourceId, targetType, targetId, depth);
						}							
					}
					if (en.containsKey("F")) {
						ArrayList<String> f = JsonHelper.toList(en.get("F"));
						for (String d : f) {
							Map<String, String> ff = JsonHelper.toMap(d);
							if (ff.containsKey("FId"))  {
								this.justify(parent, "F.FId", ff.get("FId"), sourceId, targetType, targetId, depth);
							}		
						}
					}
					if (en.containsKey("J")) {
						Map<String, String> j = JsonHelper.toMap(en.get("J"));
						if (j.containsKey("JId")) {
							this.justify(parent, "J.JId", j.get("JId"), sourceId, targetType, targetId, depth);
						}
					}
				}
			}
		return;
	}
	
	
	public void justify(ArrayList<String> param, String cType,String cId,String sourceId, String targetType,String targetId, int depth)
	{		
		if(!cId.equals(sourceId)) {
			ArrayList<String> cur = PathFetch.deepCopy(param);
			cur.add(cId);
			if (cId.equals(targetId)) {
				this.syncRet(cur);
			}
			if (depth < 2)
				this.doCalculate(cur, cType, cId, targetType, targetId, depth + 1);
		}
	}
	
	public void syncRet(ArrayList<String> param)
	{
		ArrayList<String> cur = PathFetch.deepCopy(param);
		PathFetch.ret.add(cur);
		PathFetch.count ++;
		//System.out.println(cur);
		return;
	}
	

	
	public String confirmType(String id) 
	{
		String type = "";
		String json = fetchContent("AA.AuId", id);
		//System.out.println(json);
		//System.out.println(json.contains("AA.AfId"));
		if (json.contains("AfId"))
		// PAY ATTETION HERE, in fetchContent cache already
		// holds the right/wrong json with the specific id,
		// so remember to remove the wrong one
			return "AA.AuId";
		else {
			PathFetch.cache.remove(id);
			//PathFetch.entityCache.remove(id);
			json = fetchContent("Id", id);
			return "Id";
		}
	}
	
	public String fetchContent(String type, String id) 
	{
		if(PathFetch.cache.containsKey(id)) 
			return PathFetch.cache.get(id);
			
		
		StringBuilder json = new StringBuilder();  
	    try {  
	    	String url = this.urlHead + this.urlAction + this.queryParam(type, id);
	    	//System.out.println(url);
	    	PathFetch.api_count ++;
	    	URL urlObject = new URL(url);  
	        URLConnection uc = urlObject.openConnection();  
	        BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));  
	        String inputLine = null;  
	        while ( (inputLine = in.readLine()) != null) {  
	        	json.append(inputLine);  
	        }  
	        in.close();
	        // PAY ATTENTION HERE
	        // TO accelerate the speed, store visited json string
	        //	with a MAPSET.
	        PathFetch.cache.put(id, json.toString());
	    } catch (MalformedURLException e) {  
	    	e.printStackTrace();  
	    } catch (IOException e) {  
	    	e.printStackTrace();  
	    }
		//System.out.println(json.toString());
		return json.toString();
	}
	
	public String queryParam(String type, String id)
	{
		String ret = "";
		if(type.equals("Id"))
			ret = "Id" + "=" + id + this.urlAttribute[0];
		else if(type.equals("RId"))
			ret = "composite(" + type + "=" + id + ")" + this.urlAttribute[0];
		else if(type.equals("AA.AuId"))
			ret = "composite(" + type + "=" + id + ")" + this.urlAttribute[0];
		else if(type.equals("AA.AfId"))
			ret =  "composite(" + type + "=" + id + ")" + this.urlAttribute[2];
		else
			ret = "composite(" + type + "=" + id + ")" + this.urlAttribute[3];
		ret = this.urlExpression + ret + this.urlKey;
		//System.out.println(ret);
		return ret;
	}

}
