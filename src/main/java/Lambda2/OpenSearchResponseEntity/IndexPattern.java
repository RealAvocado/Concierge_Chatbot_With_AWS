package Lambda2.OpenSearchResponseEntity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "_index",
        "_id",
        "_score",
        "_source"
})

public class IndexPattern {

    @JsonProperty("_index")
    private String index;
    @JsonProperty("_id")
    private String id;
    @JsonProperty("_score")
    private double score;
    @JsonProperty("_source")
    private Source source;

    /**
     * No args constructor for use in serialization
     *
     */
    public IndexPattern() {
    }

    /**
     *
     * @param score
     * @param index
     * @param id
     * @param source
     */
    public IndexPattern(String index, String id, double score, Source source) {
        super();
        this.index = index;
        this.id = id;
        this.score = score;
        this.source = source;
    }

    @JsonProperty("_index")
    public String getIndex() {
        return index;
    }

    @JsonProperty("_index")
    public void setIndex(String index) {
        this.index = index;
    }

    @JsonProperty("_id")
    public String getId() {
        return id;
    }

    @JsonProperty("_id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("_score")
    public double getScore() {
        return score;
    }

    @JsonProperty("_score")
    public void setScore(double score) {
        this.score = score;
    }

    @JsonProperty("_source")
    public Source getSource() {
        return source;
    }

    @JsonProperty("_source")
    public void setSource(Source source) {
        this.source = source;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(IndexPattern.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("index");
        sb.append('=');
        sb.append(((this.index == null)?"<null>":this.index));
        sb.append(',');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null)?"<null>":this.id));
        sb.append(',');
        sb.append("score");
        sb.append('=');
        sb.append(this.score);
        sb.append(',');
        sb.append("source");
        sb.append('=');
        sb.append(((this.source == null)?"<null>":this.source));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

}
