package realtalk.util;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * ChatManager is a helper class that allows the Android side of RealTalk to cleanly
 * communicate with the server while keeping it abstracted.
 * 
 * @author Taylor Williams
 *
 */
public final class ChatManager {
	
	//HUNGARIAN TAGS:
	//	rrs		RequestResultSet
	//	pmrs	PullMessageResultSet
	//	crrs	ChatRoomResultSet
	
	public static final String URL_QUALIFIER = "http://realtalkserverbeta.herokuapp.com/";
	
	//User servlets
    public static final String URL_ADD_USER = URL_QUALIFIER+"register";
    public static final String URL_REMOVE_USER = URL_QUALIFIER+"unregister";
    public static final String URL_AUTHENTICATE = URL_QUALIFIER+"authenticate";
    public static final String URL_CHANGE_PASSWORD = URL_QUALIFIER+"changePwd";
    public static final String URL_CHANGE_ID = URL_QUALIFIER+"changeRegId";
    //Chat room servlets
    public static final String URL_ADD_ROOM = URL_QUALIFIER+"addRoom";
    public static final String URL_JOIN_ROOM = URL_QUALIFIER+"joinRoom";
    public static final String URL_LEAVE_ROOM = URL_QUALIFIER+"leaveRoom";
    public static final String URL_POST_MESSAGE = URL_QUALIFIER+"post";
    public static final String URL_GET_RECENT_MESSAGES = URL_QUALIFIER+"pullRecentChat";
    public static final String URL_GET_ALL_MESSAGES = URL_QUALIFIER + "pullChat";
    public static final String URL_GET_NEARBY_CHATROOMS = URL_QUALIFIER + "nearbyRooms";
    public static final String URL_GET_USERS_ROOMS = URL_QUALIFIER + "userRooms";
    
	/**
	 * Private contructor prevents this class from being instantiated.
	 */
    private ChatManager() {
    	throw new UnsupportedOperationException("ChatManager is a utility class and should not be instantiated.");
    }
    
    /**
     * @param messageinfo         Message info object
     * @return the list of parameters as basic name value pairs
     */
    private static List<NameValuePair> rgparamsMessageInfo(MessageInfo messageinfo) {
        List<NameValuePair> rgparams = new ArrayList<NameValuePair>();
        rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_MESSAGE_TIMESTAMP, Long.valueOf(messageinfo.timestampGet().getTime()).toString()));
        rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_MESSAGE_BODY, messageinfo.stBody()));
        rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_MESSAGE_SENDER, messageinfo.stSender()));
        return rgparams;
    }
    
    /**
     * @param userinfo         User info object
     * @return the list of parameters as basic name value pairs
     */
    private static List<NameValuePair> rgparamsUserBasicInfo(UserInfo userinfo) {
        List<NameValuePair> rgparams = new ArrayList<NameValuePair>();
        rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_REG_ID, userinfo.stRegistrationId()));
        rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_USER, userinfo.stUserName()));
        rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_PWORD, userinfo.stPassword()));
        return rgparams;
    }
    
    /**
     * @param chatroominfo         Chat room info object
     * @return the list of parameters as basic name value pairs
     */
    private static List<NameValuePair> rgparamsChatRoomBasicInfo(ChatRoomInfo chatroominfo) {
        List<NameValuePair> rgparams = new ArrayList<NameValuePair>();
        rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_ROOM_NAME, chatroominfo.stName()));
        rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_ROOM_ID, chatroominfo.stId()));
        rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_ROOM_DESCRIPTION, chatroominfo.stDescription()));
        rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_USER_LATITUDE, Double.valueOf(chatroominfo.getLatitude()).toString()));
        rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_USER_LONGITUDE, Double.valueOf(chatroominfo.getLongitude()).toString()));
        return rgparams;
    }
    
    /**
     * @param latitude
     * @param longitude
     * @param radiusMeters
     * @return the list of parameters as basic name value pairs
     */
    private static List<NameValuePair> rgparamsLocationInfo(double latitude, double longitude, double radiusMeters) {
        List<NameValuePair> rgparams = new ArrayList<NameValuePair>();
        rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_USER_LATITUDE, Double.valueOf(latitude).toString()));
        rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_USER_LONGITUDE, Double.valueOf(longitude).toString()));
        rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_USER_RADIUS, Double.valueOf(radiusMeters).toString()));
        return rgparams;
    }
    
    /**
     * @param rgparam         List of parameters to embed in the request
     * @param stUrl			The url to send the request to
     * @return A RequestResultSet containing the result of the request
     */
    private static RequestResultSet rrsPostRequest(List<NameValuePair> rgparam, String stUrl) {
    	JSONObject json = null;
    	JSONParser jsonParser = new JSONParser();
		json = jsonParser.makeHttpRequest(stUrl, "POST", rgparam);
        try {
        	boolean fSucceeded = json.getString(RequestParameters.PARAMETER_SUCCESS).equals("true");
        	String stErrorCode = fSucceeded ? "NO ERROR MESSAGE" : json.getString(ResponseParameters.PARAMETER_ERROR_CODE);
        	String stErrorMessage = fSucceeded ? "NO ERROR MESSAGE" : json.getString(ResponseParameters.PARAMETER_ERROR_MSG);
            return new RequestResultSet(fSucceeded, stErrorCode, stErrorMessage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //if all else fails, return generic error code and message
    	return new RequestResultSet(false, "REQUEST FAILED", 
    			"REQUEST FAILED");
    }
    
    /**
     * @param rgparam         List of parameters to embed in the request
     * @param stUrl			The url to send the request to
     * @return A RequestResultSet containing the result of the request
     */
    private static ChatRoomResultSet crrsPostRequest(List<NameValuePair> rgparam, String stUrl) {
    	JSONObject json = null;
    	JSONParser jsonParser = new JSONParser();
		json = jsonParser.makeHttpRequest(stUrl, "POST", rgparam);
        try {
        	boolean fSucceeded = json.getString(RequestParameters.PARAMETER_SUCCESS).equals("true");
        	if (fSucceeded) {
        		List<ChatRoomInfo> rgchatroominfo = new ArrayList<ChatRoomInfo>();
        		//get list of rooms from response
        		JSONArray rgroom = json.getJSONArray(RequestParameters.PARAMETER_ROOM_ROOMS);
        		for (int i = 0; i < rgroom.length(); i++) {
        			JSONObject jsonobject = rgroom.getJSONObject(i);
        			String stName = jsonobject.getString(RequestParameters.PARAMETER_ROOM_NAME);
        			String stId = jsonobject.getString(RequestParameters.PARAMETER_ROOM_ID);
        			String stDescription = jsonobject.getString(RequestParameters.PARAMETER_ROOM_DESCRIPTION);
        			double latitude = jsonobject.getDouble(RequestParameters.PARAMETER_ROOM_LATITUDE);
        			double longitude = jsonobject.getDouble(RequestParameters.PARAMETER_ROOM_LONGITUDE);
        			String stCreator = jsonobject.getString(RequestParameters.PARAMETER_ROOM_CREATOR);
        			int numUsers = jsonobject.getInt(RequestParameters.PARAMETER_ROOM_NUM_USERS);
        			long ticks = jsonobject.getLong(RequestParameters.PARAMETER_TIMESTAMP);
        			rgchatroominfo.add(new ChatRoomInfo(stName, stId, stDescription, latitude, longitude, stCreator, numUsers, new Timestamp(ticks)));
        		}
        		return new ChatRoomResultSet(true, rgchatroominfo, "NO ERROR CODE", "NO ERROR MESSAGE");
        	}
        	return new ChatRoomResultSet(false, ResponseParameters.RESPONSE_ERROR_CODE_ROOM, 
        			ResponseParameters.RESPONSE_MESSAGE_ERROR);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //if all else fails, return generic error code and message
    	return new ChatRoomResultSet(false, "REQUEST FAILED", 
    			"REQUEST FAILED");
    }
    
    /** Sends a message/chatroom specific request.
     * @param rgparam         List of parameters to embed in the request
     * @param stUrl			The url to send the request to
     * @return A PullMessageResultSet containing the result of the request
     */
    private static PullMessageResultSet pmrsPostRequest(List<NameValuePair> rgparam, String stUrl) {
    	JSONObject json = null;
    	JSONParser jsonParser = new JSONParser();
		json = jsonParser.makeHttpRequest(stUrl, "POST", rgparam);
        try {
        	boolean fSucceeded = json.getString(RequestParameters.PARAMETER_SUCCESS).equals("true");
        	if (fSucceeded) {
        		List<MessageInfo> rgmessageinfo = new ArrayList<MessageInfo>();
        		JSONArray rgmessage = json.getJSONArray(RequestParameters.PARAMETER_MESSAGE_MESSAGES);
        		for (int i = 0; i < rgmessage.length(); i++) {
        			JSONObject jsonobject = rgmessage.getJSONObject(i);
        			String stBody = jsonobject.getString(RequestParameters.PARAMETER_MESSAGE_BODY);
        			long ticks = jsonobject.getLong(RequestParameters.PARAMETER_MESSAGE_TIMESTAMP);
        			String stSender = jsonobject.getString(RequestParameters.PARAMETER_MESSAGE_SENDER);
        			rgmessageinfo.add(new MessageInfo(stBody, stSender, ticks));
        		}
        		return new PullMessageResultSet(true, rgmessageinfo, "NO ERROR CODE", "NO ERROR MESSAGE");
        	}
            return new PullMessageResultSet(false, new ArrayList<MessageInfo>(), 
            		json.getString(ResponseParameters.PARAMETER_ERROR_CODE), 
            		json.getString(ResponseParameters.PARAMETER_ERROR_MSG));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //if all else fails, return generic error code and message
    	return new PullMessageResultSet(false, "REQUEST FAILED", 
    			"REQUEST FAILED");
    }
	
    /** Authenticates a user
     * @param userinfo		The user to authenticate
     * @return A resultset containing the result of the authentication
     */
	public static RequestResultSet rrsAuthenticateUser(UserInfo userinfo) {
        List<NameValuePair> rgparams = rgparamsUserBasicInfo(userinfo);
        return rrsPostRequest(rgparams, URL_AUTHENTICATE);
	}
	
    /** Adds a user
     * @param userinfo		The user to add
     * @return A resultset containing the result of the addition
     */
	public static RequestResultSet rrsAddUser(UserInfo userinfo) {
        List<NameValuePair> rgparams = rgparamsUserBasicInfo(userinfo);
        return rrsPostRequest(rgparams, URL_ADD_USER);
	}
	
    /** Remove a user
     * @param userinfo		The user to remove
     * @return A resultset containing the result of the removal
     */
	public static RequestResultSet rrsRemoveUser(UserInfo userinfo) {
        List<NameValuePair> rgparams = rgparamsUserBasicInfo(userinfo);
        return rrsPostRequest(rgparams, URL_REMOVE_USER);
	}
	
    /** Changes a user's password
     * @param userinfo		The user to change
     * @param stPasswordNew		The new password
     * @return A resultset containing the result of the change
     */
	public static RequestResultSet rrsChangePassword(UserInfo userinfo, String stPasswordNew) {
        List<NameValuePair> rgparams = rgparamsUserBasicInfo(userinfo);
        rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_NEW_PWORD, stPasswordNew));
        return rrsPostRequest(rgparams, URL_CHANGE_PASSWORD);
	}
	
    /** Changes a user's ID
     * @param userinfo		The user to change
     * @param stIdNew		The new ID
     * @return A resultset containing the result of the change
     */
	public static RequestResultSet rrsChangeID(UserInfo userinfo, String stIdNew) {
        List<NameValuePair> rgparams = rgparamsUserBasicInfo(userinfo);
        rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_NEW_REG_ID, stIdNew));
        return rrsPostRequest(rgparams, URL_CHANGE_ID);
	}
	
    /** Adds a new chatroom
     * @param chatroominfo		The chatroom to add
     * @param userinfo		The user to associate with the new room
     * @return A resultset containing the result of the addition
     */
	public static RequestResultSet rrsAddRoom(ChatRoomInfo chatroominfo, UserInfo userinfo) {
        List<NameValuePair> rgparams = rgparamsChatRoomBasicInfo(chatroominfo);
        rgparams.addAll(rgparamsUserBasicInfo(userinfo));
		return rrsPostRequest(rgparams, URL_ADD_ROOM);
	}
	
    /** Joins a user to a chatroom
     * @param chatroominfo		The chatroom to join
     * @param userinfo		The user to join into the room
     * @return A resultset containing the result of the join
     */
	public static RequestResultSet rrsJoinRoom(UserInfo userinfo, ChatRoomInfo chatroominfo) {
        List<NameValuePair> rgparams = rgparamsUserBasicInfo(userinfo);
        rgparams.addAll(rgparamsChatRoomBasicInfo(chatroominfo));
		return rrsPostRequest(rgparams, URL_JOIN_ROOM);
	}
	
    /** Leaves a chatroom
     * @param chatroominfo		The chatroom to leave
     * @param userinfo		The user leaving the room
     * @return A resultset containing the result of the leave
     */
	public static RequestResultSet rrsLeaveRoom(UserInfo userinfo, ChatRoomInfo chatroominfo) {
        List<NameValuePair> rgparams = rgparamsUserBasicInfo(userinfo);
        rgparams.addAll(rgparamsChatRoomBasicInfo(chatroominfo));
		return rrsPostRequest(rgparams, URL_LEAVE_ROOM);
	}
	
    /** Posts a message to a chatroom
     * @param chatroominfo		The chatroom to post a message to
     * @param userinfo		The user posting the message
     * @return A resultset containing the result of the post
     */
	public static RequestResultSet rrsPostMessage(UserInfo userinfo, ChatRoomInfo chatroominfo, MessageInfo message) {
        List<NameValuePair> rgparams = rgparamsUserBasicInfo(userinfo);
        rgparams.addAll(rgparamsChatRoomBasicInfo(chatroominfo));
        rgparams.addAll(rgparamsMessageInfo(message));
		return rrsPostRequest(rgparams, URL_POST_MESSAGE);
	}
	
    /** Returns the chatlog for a certain chatroom
     * @param chatroominfo		The chatroom to pull the log from
     * @return A resultset containing the result of the pull
     */
	public static PullMessageResultSet pmrsChatLogGet(ChatRoomInfo chatroominfo) {
        List<NameValuePair> rgparams = rgparamsChatRoomBasicInfo(chatroominfo);
		return pmrsPostRequest(rgparams, URL_GET_ALL_MESSAGES);
	}
	
	/**
	 * This method pulls all recent messages to a specific given chatroom after a given time as indicated
	 * in timestamp
	 * 
	 * @param chatroominfo Information about chatroom to pull messages from.
	 * @param timestamp    Time 
	 * @return             Result set that contains a boolean that indicates success or failure and 
	 *                     returns an error code and message if failure was occurred. If success,
	 *                     it returns a list of MessageInfo that have a timestamp later than the given
	 *                     timestamp
	 */
	public static PullMessageResultSet pmrsChatRecentChat(ChatRoomInfo chatroominfo, Timestamp timestamp) {
		long rawtimestamp = timestamp.getTime();
		String stTimestamp = "";
		try {
			stTimestamp = String.valueOf(rawtimestamp);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return new PullMessageResultSet(false, "ERROR_INVALID_TIMESTAMP", "ERROR_MESSAGE_PARSING_ERROR");
		}
		List<NameValuePair> rgparams = rgparamsChatRoomBasicInfo(chatroominfo);
		rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_TIMESTAMP, stTimestamp));
		return pmrsPostRequest(rgparams, URL_GET_RECENT_MESSAGES);
	}
	
	/**
	 * This method pulls all nearby chatrooms, given a latitude, longitude, and a radius.
	 * 
	 * @param latitude	users latitude
	 * @param longitude	users longitude
	 * @param radiusMeters radius from the user in which to find chatrooms
	 * @return             Result set that contains a boolean that indicates success or failure and 
	 *                     returns an error code and message if failure was occurred. If success,
	 *                     it holds a list of ChatRoomInfo objects describing the nearby rooms.
	 */
	public static ChatRoomResultSet crrsNearbyChatrooms(double latitude, double longitude, double radiusMeters) {
		List<NameValuePair> rgparams = rgparamsLocationInfo(latitude, longitude, radiusMeters);
		return crrsPostRequest(rgparams, URL_GET_NEARBY_CHATROOMS);
	}
	
	/**
	 * This method pulls all chatrooms that the given user has joined from the server
	 * 
	 * @param userinfo     Information about the user
     * @return             Result set that contains a boolean that indicates success or failure and 
     *                     returns an error code and message if failure was occurred. If success,
     *                     it holds a list of ChatRoomInfo objects describing the user's rooms.
	 */
	public static ChatRoomResultSet crrsUsersChatrooms(UserInfo userinfo) {
	    List<NameValuePair> rgparams = rgparamsUserBasicInfo(userinfo);
	    return crrsPostRequest(rgparams, URL_GET_USERS_ROOMS);
	}
}
