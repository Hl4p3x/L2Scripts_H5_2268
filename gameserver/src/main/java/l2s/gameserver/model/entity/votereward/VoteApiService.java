package l2s.gameserver.model.entity.votereward;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * reworked by Bonux
 */
public class VoteApiService {
	private static final Logger LOGGER = LoggerFactory.getLogger(VoteApiService.class);

	public static String getApiResponse(String endpoint) {
		HttpURLConnection connection = null;
		try {
			StringBuilder stringBuilder = new StringBuilder();
			URL url = new URL(endpoint);
			connection = (HttpURLConnection) url.openConnection();
			connection.addRequestProperty("User-Agent", "Mozilla/5.0");
			connection.setRequestMethod("GET");
			connection.setReadTimeout(5000);
			connection.connect();

			int responseCode = connection.getResponseCode();
			if(responseCode != 200) {
				LOGGER.warn("VoteApiService::getApiResponse returned error CODE[" + responseCode + "] LINK[" + endpoint + "]");
				return null;
			}

			try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream())))
			{
				String line;
				while((line = reader.readLine()) != null) {
					stringBuilder.append(line).append("\n");
				}
			}
			return stringBuilder.toString();
		}
		catch (Exception e) {
			LOGGER.warn("Something went wrong in VoteApiService::getApiResponse LINK[" + endpoint + "]", e);
		}
		finally {
			if(connection != null)
				connection.disconnect();
		}
		return null;
	}
}