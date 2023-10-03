package DataScraping.RestaurantEntity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "latitude",
        "longitude"
})

public class Coordinates {

    @JsonProperty("latitude")
    private double latitude;
    @JsonProperty("longitude")
    private double longitude;

    /**
     * No args constructor for use in serialization
     *
     */
    public Coordinates() {
    }

    /**
     *
     * @param latitude
     * @param longitude
     */
    public Coordinates(double latitude, double longitude) {
        super();
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @JsonProperty("latitude")
    public double getLatitude() {
        return latitude;
    }

    @JsonProperty("latitude")
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @JsonProperty("longitude")
    public double getLongitude() {
        return longitude;
    }

    @JsonProperty("longitude")
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "Coordinates{" + "latitude=" + latitude + ", longitude=" + longitude + '}';
    }
}