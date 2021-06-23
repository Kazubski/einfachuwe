package de.calitobundo.twitch.desktop.config;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public final class Configuration {

	//https://twitchapps.com/tokengen/
	// OAuth Token for IRC USer (You can get yours here: https://twitchapps.com/tmi/)
	//channel:read:subscriptions user:read:blocked_users user:edit:follows user:edit:broadcast
	//chat:edit chat:read whispers:read whispers:edit channel:moderate moderation:read channel:manage:broadcast

	public static final Map<String, String> map = new HashMap<>();
	public static final String IDENTITY_PROVIDER = "IDENTITY_PROVIDER";
	public static final String O_AUTH = "O_AUTH";
	public static final String CLIENT_ID = "CLIENT_ID";
	public static final String CLIENT_SECRET = "CLIENT_SECRET";
	public static final String CHANNEL_NAME = "CHANNEL_NAME";

	static {
		try {
			InputStream dd = Configuration.class.getClassLoader().getResourceAsStream("twitch.txt");
			Scanner scanner = new Scanner(dd);
			while (scanner.hasNextLine()) {
				String[] pair = scanner.nextLine().split("=");
				map.put(pair[0], pair[1]);
			}
			scanner.close();
		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	public static String getValue(String name){
		return map.get(name);
	}


}
