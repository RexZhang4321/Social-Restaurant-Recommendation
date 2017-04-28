package IR.Project.bean;

import java.util.ArrayList;
import java.util.List;

public class Business {
	String business_id;
	String  name;
	String  neighborhood;
	String  address;
	String  city;
	String  state;
	String  postal_code;
	double latitude;
	double longitude;
	double stars;
	int review_count;
	int is_open;
	List<String> categories;
	List<String> hours;
	String type;
	
	public Business(){
		categories = new ArrayList<String>();
	}
	
	//attributes :[ Alcohol: none , Ambience: {'romantic': False, 'intimate': False, 'classy': False, 'hipster': False, 'divey': False, 'touristy': False, 'trendy': False, 'upscale': False, 'casual': True} , BusinessAcceptsCreditCards: True , BusinessParking: {'garage': False, 'street': False, 'validated': False, 'lot': True, 'valet': False} , Caters: False , GoodForKids: True , GoodForMeal: {'dessert': False, 'latenight': False, 'lunch': True, 'dinner': False, 'breakfast': False, 'brunch': False} , HasTV: True , NoiseLevel: average , OutdoorSeating: False , RestaurantsAttire: casual , RestaurantsDelivery: False , RestaurantsGoodForGroups: True , RestaurantsPriceRange2: 1 , RestaurantsReservations: False , RestaurantsTableService: True , RestaurantsTakeOut: True , WheelchairAccessible: True , WiFi: no ]
	public String getBusiness_id() {
		return business_id;
	}
	public void setBusiness_id(String business_id) {
		this.business_id = business_id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getNeighborhood() {
		return neighborhood;
	}
	public void setNeighborhood(String neighborhood) {
		this.neighborhood = neighborhood;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getPostal_code() {
		return postal_code;
	}
	public void setPostal_code(String postal_code) {
		this.postal_code = postal_code;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public double getStars() {
		return stars;
	}
	public void setStars(double stars) {
		this.stars = stars;
	}
	public int getReview_count() {
		return review_count;
	}
	public void setReview_count(int review_count) {
		this.review_count = review_count;
	}
	public int getIs_open() {
		return is_open;
	}
	public void setIs_open(int is_open) {
		this.is_open = is_open;
	}
	public List<String> getCategories() {
		return categories;
	}
	public void setCategories(List<String> categories) {
		this.categories = categories;
	}
	public List<String> getHours() {
		return hours;
	}
	public void setHours(List<String> hours) {
		this.hours = hours;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	
}
