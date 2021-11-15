
package org.scijava.persist;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public interface IClassRuntimeAdapter<B, T extends B> extends
	IObjectScijavaAdapter, JsonSerializer<T>, JsonDeserializer<T>
{

	Class<? extends B> getBaseClass();

	Class<? extends T> getRunTimeClass();

	default boolean useCustomAdapter() {
		return false;
	}

	@Override
	default T deserialize(JsonElement json, Type typeOfT,
		JsonDeserializationContext context) throws JsonParseException
	{
		throw new JsonParseException("Default deserializer for class " +
			getBaseClass() + " (" + getRunTimeClass() +
			") should not be used, return false in method useCustomAdapter instead");
	}

	@Override
	default JsonElement serialize(T src, Type typeOfSrc,
		JsonSerializationContext context)
	{
		throw new JsonIOException("Default serializer for class " + getBaseClass() +
			" (" + getRunTimeClass() +
			") should not be used, should not be used, return false in method useCustomAdapter instead");
	}
}
