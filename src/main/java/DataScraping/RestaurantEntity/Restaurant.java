package DataScraping.RestaurantEntity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonIgnoreProperties({"price"})
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "alias",
        "name",
        "image_url",
        "is_closed",
        "url",
        "review_count",
        "categories",
        "rating",
        "coordinates",
        "transactions",
        "location",
        "phone",
        "display_phone",
        "distance"
})

public class Restaurant {

    @JsonProperty("id")
    private String id;
    @JsonProperty("alias")
    private String alias;
    @JsonProperty("name")
    private String name;
    @JsonProperty("image_url")
    private String imageUrl;
    @JsonProperty("is_closed")
    private boolean isClosed;
    @JsonProperty("url")
    private String url;
    @JsonProperty("review_count")
    private int reviewCount;
    @JsonProperty("categories")
    private List<Category> categories;
    @JsonProperty("rating")
    private double rating;
    @JsonProperty("coordinates")
    private Coordinates coordinates;
    @JsonProperty("transactions")
    private List<String> transactions;
    @JsonProperty("location")
    private Location location;
    @JsonProperty("phone")
    private String phone;
    @JsonProperty("display_phone")
    private String displayPhone;
    @JsonProperty("distance")
    private double distance;

    /**
     * No args constructor for use in serialization
     *
     */
    public Restaurant() {
    }

    /**
     *
     * @param displayPhone
     * @param distance
     * @param rating
     * @param coordinates
     * @param transactions
     * @param url
     * @param isClosed
     * @param reviewCount
     * @param phone
     * @param imageUrl
     * @param name
     * @param alias
     * @param location
     * @param id
     * @param categories
     */
    public Restaurant(String id, String alias, String name, String imageUrl, boolean isClosed, String url, int reviewCount, List<Category> categories, double rating, Coordinates coordinates, List<String> transactions, Location location, String phone, String displayPhone, double distance) {
        super();
        this.id = id;
        this.alias = alias;
        this.name = name;
        this.imageUrl = imageUrl;
        this.isClosed = isClosed;
        this.url = url;
        this.reviewCount = reviewCount;
        this.categories = categories;
        this.rating = rating;
        this.coordinates = coordinates;
        this.transactions = transactions;
        this.location = location;
        this.phone = phone;
        this.displayPhone = displayPhone;
        this.distance = distance;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("alias")
    public String getAlias() {
        return alias;
    }

    @JsonProperty("alias")
    public void setAlias(String alias) {
        this.alias = alias;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("image_url")
    public String getImageUrl() {
        return imageUrl;
    }

    @JsonProperty("image_url")
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @JsonProperty("is_closed")
    public boolean isIsClosed() {
        return isClosed;
    }

    @JsonProperty("is_closed")
    public void setIsClosed(boolean isClosed) {
        this.isClosed = isClosed;
    }

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty("review_count")
    public int getReviewCount() {
        return reviewCount;
    }

    @JsonProperty("review_count")
    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    @JsonProperty("categories")
    public List<Category> getCategories() {
        return categories;
    }

    @JsonProperty("categories")
    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    @JsonProperty("rating")
    public double getRating() {
        return rating;
    }

    @JsonProperty("rating")
    public void setRating(double rating) {
        this.rating = rating;
    }

    @JsonProperty("coordinates")
    public Coordinates getCoordinates() {
        return coordinates;
    }

    @JsonProperty("coordinates")
    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    @JsonProperty("transactions")
    public List<String> getTransactions() {
        return transactions;
    }

    @JsonProperty("transactions")
    public void setTransactions(List<String> transactions) {
        this.transactions = transactions;
    }

    @JsonProperty("location")
    public Location getLocation() {
        return location;
    }

    @JsonProperty("location")
    public void setLocation(Location location) {
        this.location = location;
    }

    @JsonProperty("phone")
    public String getPhone() {
        return phone;
    }

    @JsonProperty("phone")
    public void setPhone(String phone) {
        this.phone = phone;
    }

    @JsonProperty("display_phone")
    public String getDisplayPhone() {
        return displayPhone;
    }

    @JsonProperty("display_phone")
    public void setDisplayPhone(String displayPhone) {
        this.displayPhone = displayPhone;
    }

    @JsonProperty("distance")
    public double getDistance() {
        return distance;
    }

    @JsonProperty("distance")
    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "Restaurant{" + "id='" + id + '\'' + ", alias='" + alias + '\'' + ", name='" + name + '\'' + ", imageUrl='" + imageUrl + '\'' + ", isClosed=" + isClosed + ", url='" + url + '\'' + ", reviewCount=" + reviewCount + ", categories=" + categories + ", rating=" + rating + ", coordinates=" + coordinates + ", transactions=" + transactions + ", location=" + location + ", phone='" + phone + '\'' + ", displayPhone='" + displayPhone + '\'' + ", distance=" + distance + '}';
    }
}