package ourneighborschild;

public class GoogleGeocode 
{
	private Result[] results;
	private String status;
	
	//getter
	String getGeocode() { return results[0].geometry().getLocation(); }
	String getFormattedAddress() { return results[0].formatted_address(); }

	private class Result
	{
		private Address_Component[] address_components;
		private String formatted_address;
		private Geometry geometry;
		private String place_id;
		private String[] types;
		
		//getters
		String formatted_address() { return formatted_address; }
		Geometry geometry() { return geometry; }
	}
	
	private class Address_Component
	{
		private String long_name;
		private String short_name;
		private String[] types;
	}
	
	
	private class Geometry
	{
		private Location location;
		private String location_type;
		private Viewport viewport;
		
		String getLocation() { return String.format("%.7f, %.7f", location.getLat(), location.getLng()); }
	}
	
	private class Location
	{
		double lat;
		double lng;
		
		//getters
		double getLat() { return lat; }
		double getLng() { return lng; }
	}
	
	private class Viewport
	{
		private Location northeast;
		private Location southwest;
	}
}
