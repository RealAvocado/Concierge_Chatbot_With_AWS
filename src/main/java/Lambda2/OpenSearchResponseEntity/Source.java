package Lambda2.OpenSearchResponseEntity;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "ID",
        "Cuisine"
})

public class Source {

    @JsonProperty("ID")
    private String id;
    @JsonProperty("Cuisine")
    private List<String> cuisine;

    /**
     * No args constructor for use in serialization
     *
     */
    public Source() {
    }

    /**
     *
     * @param cuisine
     * @param id
     */
    public Source(String id, List<String> cuisine) {
        super();
        this.id = id;
        this.cuisine = cuisine;
    }

    @JsonProperty("ID")
    public String getId() {
        return id;
    }

    @JsonProperty("ID")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("Cuisine")
    public List<String> getCuisine() {
        return cuisine;
    }

    @JsonProperty("Cuisine")
    public void setCuisine(List<String> cuisine) {
        this.cuisine = cuisine;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Source.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null)?"<null>":this.id));
        sb.append(',');
        sb.append("cuisine");
        sb.append('=');
        sb.append(((this.cuisine == null)?"<null>":this.cuisine));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

}