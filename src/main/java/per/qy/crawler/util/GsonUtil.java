package per.qy.crawler.util;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Gson处理json工具类
 */
public class GsonUtil {

	private static final Gson GSON = new Gson();
	private static final JsonParser JSONPARSER = new JsonParser();

	public static Gson getInstance() {
		return GSON;
	}

	public static String toJson(Object obj) {
		return GSON.toJson(obj);
	}

	public static <T> T fromJson(String json, Class<T> classOfT) {
		return GSON.fromJson(json, classOfT);
	}

	public static <T> T fromJson(String json, Type typeOfT) {
		return GSON.fromJson(json, typeOfT);
	}

	public static JsonElement parse(String jsonStr) {
		return JSONPARSER.parse(jsonStr);
	}
}
