package org.icatproject.core.manager;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import jakarta.json.Json;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParsingException;
import jakarta.json.stream.JsonParser.Event;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.util.EntityUtils;
import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;

public class Rest {

	public static void checkStatus(HttpResponse response, IcatExceptionType et) throws IcatException {
		StatusLine status = response.getStatusLine();
		if (status == null) {
			throw new IcatException(IcatExceptionType.INTERNAL, "Status line in response is empty");
		}
		int rc = status.getStatusCode();
		if (rc / 100 != 2) {
			HttpEntity entity = response.getEntity();
			String error;
			if (entity == null) {
				throw new IcatException(IcatExceptionType.INTERNAL, "No http entity returned in response");
			} else {
				try {
					error = EntityUtils.toString(entity);
				} catch (ParseException | IOException e) {
					throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
				}
			}
			try (JsonParser parser = Json.createParser(new ByteArrayInputStream(error.getBytes()))) {
				String code = null;
				String message = null;
				String key = "";
				while (parser.hasNext()) {
					JsonParser.Event event = parser.next();
					if (event == Event.KEY_NAME) {
						key = parser.getString();
					} else if (event == Event.VALUE_STRING) {
						if (key.equals("code")) {
							code = parser.getString();
						} else if (key.equals("message")) {
							message = parser.getString();
						}
					}
				}

				if (code == null || message == null) {
					throw new IcatException(IcatExceptionType.INTERNAL, error);
				}
				throw new IcatException(et, message);

			} catch (JsonParsingException e) {
				throw new IcatException(IcatExceptionType.INTERNAL, error);
			}
		}
	}
}
