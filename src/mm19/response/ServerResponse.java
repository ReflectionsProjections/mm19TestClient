package mm19.response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerResponse {
	
	public int responseCode;
	public String[] error;
	public String token;
	
	public ServerResponse(JSONObject obj) throws ServerResponseException{
		try {
			if(!obj.has("responseCode")) {
				throw new ServerResponseException("Server does not have response code.");
			}
			if(!obj.has("token")) {
				throw new ServerResponseException("Server does not have a token");
			}
			
			responseCode = obj.getInt("responseCode");
			token = obj.getString("token");
			
			if(obj.has("error")) {
				JSONArray err = obj.getJSONArray("error");
				error = new String[err.length()];
				
				for(int i = 0; i < err.length(); i++) {
					error[i] = err.getString(i);
				}
			}
			else {
				error = null;
			}
			
			
		}
		catch(JSONException e) {
			e.printStackTrace();
		}
		
		
	}
}
