package ourneighborschild;

public class GoogleGeocode 
{
	private Result[] results;
	@SuppressWarnings("unused")
	private String status;
	
	//getter
	String getGeocode() { return results[0].geometry().getLocation(); }
	String getFormattedAddress() { return results[0].formatted_address(); }

	private class Result
	{
		@SuppressWarnings("unused")
		private Address_Component[] address_components;
		private String formatted_address;
		private Geometry geometry;
		@SuppressWarnings("unused")
		private String place_id;
		@SuppressWarnings("unused")
		private String[] types;
		
		//getters
		String formatted_address() { return formatted_address; }
		Geometry geometry() { return geometry; }
	}
	
	private class Address_Component
	{
		@SuppressWarnings("unused")
		private String long_name;
		@SuppressWarnings("unused")
		private String short_name;
		@SuppressWarnings("unused")
		private String[] types;
	}
	
	
	private class Geometry
	{
		private Location location;
		@SuppressWarnings("unused")
		private String location_type;
		@SuppressWarnings("unused")
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
		@SuppressWarnings("unused")
		private Location northeast;
		@SuppressWarnings("unused")
		private Location southwest;
	}
}
