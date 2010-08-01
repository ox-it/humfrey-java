package uk.ac.ox.oucs.humfrey;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import uk.ac.ox.oucs.humfrey.serializers.AbstractSerializer;

public class FormatPreferences {
	String defaultNotProvided, defaultNotFound;
	Set<String> available;
	
	public static FormatPreferences noFormats = new FormatPreferences(null, null);
	
	public FormatPreferences(String defaultNotProvided, String defaultNotFound, Map<String,AbstractSerializer> serializers) {
		this(defaultNotProvided, defaultNotFound, serializers.keySet());
	}
	
	public FormatPreferences(String defaultNotProvided, String defaultNotFound, String... available) {
		this.defaultNotProvided = defaultNotProvided;
		this.defaultNotFound = defaultNotFound;
		this.available = new HashSet<String>();
		for (String format : available)
			this.available.add(format);
	}
	
	public FormatPreferences(String defaultNotProvided, String defaultNotFound, Set<String> available) {
		this.defaultNotProvided = defaultNotProvided;
		this.defaultNotFound = defaultNotFound;
		this.available = available;
		

	}
	
	public String getDefaultNotProvided() {
		return defaultNotProvided;
	}
	public String getDefaultNotFound() {
		return defaultNotFound;
	}
	
	public boolean formatAvailable(String format) {
		return available.contains(format);
	}
	
	public Set<String> all() {
		return available;
	}
	
	public Set<String> union(FormatPreferences other) {
		Set<String> all = new HashSet<String>();
		all.addAll(available);
		all.addAll(other.available);
		return all;
	}
}
