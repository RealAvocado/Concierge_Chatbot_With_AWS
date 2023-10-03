package DataScraping.RestaurantEntity;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "address1",
        "address2",
        "address3",
        "city",
        "zip_code",
        "country",
        "state",
        "display_address"
})

public class Location {

    @JsonProperty("address1")
    private String address1;
    @JsonProperty("address2")
    private Object address2;
    @JsonProperty("address3")
    private String address3;
    @JsonProperty("city")
    private String city;
    @JsonProperty("zip_code")
    private String zipCode;
    @JsonProperty("country")
    private String country;
    @JsonProperty("state")
    private String state;
    @JsonProperty("display_address")
    private List<String> displayAddress;

    /**
     * No args constructor for use in serialization
     *
     */
    public Location() {
    }

    /**
     *
     * @param zipCode
     * @param country
     * @param address3
     * @param address2
     * @param city
     * @param address1
     * @param displayAddress
     * @param state
     */
    public Location(String address1, Object address2, String address3, String city, String zipCode, String country, String state, List<String> displayAddress) {
        super();
        this.address1 = address1;
        this.address2 = address2;
        this.address3 = address3;
        this.city = city;
        this.zipCode = zipCode;
        this.country = country;
        this.state = state;
        this.displayAddress = displayAddress;
    }

    @JsonProperty("address1")
    public String getAddress1() {
        return address1;
    }

    @JsonProperty("address1")
    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    @JsonProperty("address2")
    public Object getAddress2() {
        return address2;
    }

    @JsonProperty("address2")
    public void setAddress2(Object address2) {
        this.address2 = address2;
    }

    @JsonProperty("address3")
    public String getAddress3() {
        return address3;
    }

    @JsonProperty("address3")
    public void setAddress3(String address3) {
        this.address3 = address3;
    }

    @JsonProperty("city")
    public String getCity() {
        return city;
    }

    @JsonProperty("city")
    public void setCity(String city) {
        this.city = city;
    }

    @JsonProperty("zip_code")
    public String getZipCode() {
        return zipCode;
    }

    @JsonProperty("zip_code")
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    @JsonProperty("country")
    public String getCountry() {
        return country;
    }

    @JsonProperty("country")
    public void setCountry(String country) {
        this.country = country;
    }

    @JsonProperty("state")
    public String getState() {
        return state;
    }

    @JsonProperty("state")
    public void setState(String state) {
        this.state = state;
    }

    @JsonProperty("display_address")
    public List<String> getDisplayAddress() {
        return displayAddress;
    }

    @JsonProperty("display_address")
    public void setDisplayAddress(List<String> displayAddress) {
        this.displayAddress = displayAddress;
    }

    @Override
    public String toString() {
        return "Location{" + "address1='" + address1 + '\'' + ", address2=" + address2 + ", address3='" + address3 + '\'' + ", city='" + city + '\'' + ", zipCode='" + zipCode + '\'' + ", country='" + country + '\'' + ", state='" + state + '\'' + ", displayAddress=" + displayAddress + '}';
    }
}