package Lambda1;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.LexEvent;
import com.amazonaws.services.lexruntime.AmazonLexRuntimeClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

public class RecommendationHandler implements RequestHandler<LexEvent, Object> {
    LambdaLogger logger;
    @Override
    public Object handleRequest(LexEvent lexEvent, Context context) {
        logger = context.getLogger();
        logger.log("Received event: " + lexEvent);
        String intentName = lexEvent.getCurrentIntent().getName();

        if ("DiningSuggestionsIntent".equals(intentName)) {
            return getRestaurants(lexEvent);
        } else {
            throw new UnsupportedOperationException("Intent with name " + intentName + " not supported");
        }
    }

    private Map<String, Object> getRestaurants(LexEvent lexEvent) {
        Map<String, Object> response = new HashMap<>();
        String source = lexEvent.getInvocationSource();
        Map<String, String> slotDict = new HashMap<>();
        String location = "";
        String cuisine = "";
        String diningTime = "";
        String numberOfPeople = "";
        String email = "";

        if ("DialogCodeHook".equals(source)) {
            Map<String, String> slots = lexEvent.getCurrentIntent().getSlots();

            location = slots.get("Location");
            cuisine = slots.get("Cuisine");
            diningTime = slots.get("Dining_time");
            numberOfPeople = slots.get("Number_of_people");
            email = slots.get("Email");
            logger.log("Slots: Location=" + location + ", Cuisine=" + cuisine + ", Dining_time=" + diningTime + ", Number_of_people=" + numberOfPeople + ", Email=" + email);

            slotDict.put("Location", location);
            slotDict.put("Cuisine", cuisine);
            slotDict.put("Dining_time", diningTime);
            slotDict.put("Number_of_people", numberOfPeople);
            slotDict.put("Email", email);

            Map<String, Object> validationResult = validateParameters(location, cuisine, diningTime, numberOfPeople, email);
            if (!(boolean) validationResult.get("isValid")) {
                slots.put((String) validationResult.get("violatedSlot"), null);
                return elicitSlot(lexEvent.getSessionAttributes(), lexEvent.getCurrentIntent().getName(), slots,
                        (String) validationResult.get("violatedSlot"), (String) validationResult.get("message"));
            }
        }

        // Push to SQS
        String queueUrl = "https://sqs.us-east-2.amazonaws.com/508829990173/userRequest";
        AmazonSQS SQS = AmazonSQSClientBuilder.standard().build();
        SQSClient sqs = new SQSClient(SQS,queueUrl);
        String jsonMessage = "{\"cuisine\": \"" + cuisine + "\", \"email\": \"" + email + "\"}";

        sqs.sendMessage(jsonMessage);

        final String emailValue = email;
        final String numberOfPeopleValue = numberOfPeople;
        final String diningTimeValue = diningTime;
        final String locationValue = location;
        response.put("dialogAction", new HashMap<String, Object>() {{
            put("fulfillmentState", "Fulfilled");
            put("type", "Close");
            put("message", new HashMap<String, Object>() {{
                put("contentType", "PlainText");
                put("content", "We have received your request. You will receive recommendations to your email " +
                        emailValue + ". Have a great day with your group of " + numberOfPeopleValue + " to dine at " + diningTimeValue +
                        " in the city of " + locationValue + "!");
            }});
        }});
        return response;
    }

    private Map<String, Object> elicitSlot(Map<String, String> sessionAttributes, String intentName, Map<String, String> slots, String slotToElicit, String message) {
        Map<String, Object> dialogAction = new HashMap<>();
        dialogAction.put("type", "ElicitSlot");
        dialogAction.put("intentName", intentName);
        dialogAction.put("slots", slots);
        dialogAction.put("slotToElicit", slotToElicit);
        dialogAction.put("message", new HashMap<String, Object>() {{
            put("contentType", "PlainText");
            put("content", message);
        }});

        Map<String, Object> response = new HashMap<>();
        response.put("sessionAttributes", sessionAttributes);
        response.put("dialogAction", dialogAction);
        return response;
    }

    private Map<String, Object> validateParameters(String location, String cuisine, String diningTime, String numberOfPeople, String email) {
        List<String> locationTypes = List.of("manhattan", "new york", "ny", "nyc", "new york city");
        Map<String, Object> validationResult = new HashMap<>();
        if (location == null || location.isEmpty()) {
            validationResult.put("isValid", false);
            validationResult.put("violatedSlot", "Location");
            validationResult.put("message", "What city are you looking to dine in?");
            return validationResult;
        }

        if (!locationTypes.contains(location.toLowerCase())) {
            validationResult.put("isValid", false);
            validationResult.put("violatedSlot", "Location");
            validationResult.put("message", "We do not have any restaurant serving there, please enter a nearby location");
            return validationResult;
        }

        List<String> cuisineTypes = List.of("afghan", "african", "senegalese", "south african", "american (new)", "american (traditional)", "andalusian", "arabic", "arab pizza", "argentine", "armenian", "asian fusion", "asturian", "australian", "austrian", "baguettes", "bangladeshi", "barbeque", "bbq", "basque", "bavarian", "beer garden", "beer hall", "beisl", "belgian", "flemish", "bistros", "black sea", "brasseries", "brazilian", "brazilian empanadas", "central brazilian", "northeastern brazilian", "northern brazilian", "rodizios", "breakfast & brunch", "pancakes", "british", "buffets", "bulgarian", "burgers", "burmese", "cafes", "themed cafes", "cafeteria", "cajun/creole", "cambodian", "canadian (new)", "canteen", "caribbean", "dominican", "haitian", "puerto rican", "trinidadian", "catalan", "cheesesteaks", "chicken shop", "chicken wings", "chilean", "chinese", "cantonese", "congee", "dim sum", "fuzhou", "hainan", "hakka", "henghwa", "hokkien", "hunan", "pekinese", "shanghainese", "szechuan", "teochew", "comfort food", "corsican", "creperies", "cuban", "curry sausage", "cypriot", "czech", "czech/slovakian", "danish", "delis", "diners", "dinner theater", "dumplings", "eastern european", "eritrean", "ethiopian", "fast food", "hot dogs", "filipino", "fischbroetchen", "fish & chips", "flatbread", "fondue", "food court", "food stands", "freiduria", "french", "alsatian", "auvergnat", "berrichon", "bourguignon", "mauritius", "nicoise", "provencal", "reunion", "french southwest", "galician", "game meat", "gastropubs", "georgian", "german", "baden", "eastern german", "hessian", "northern german", "palatine", "rhinelandian", "swabian", "giblets", "gluten-free", "greek", "grilled cheese", "grocery", "gyros", "halal", "hawaiian", "heuriger", "himalayan/nepalese", "honduran", "hong kong style cafe", "hot pot", "hungarian", "iberian", "indian", "indonesian", "international", "irish", "island pub", "italian", "calabrian", "sardinian", "sicilian", "tuscan", "japanese", "kaiseki", "karaoke", "kebab", "korean", "kosher", "kurdish", "laos", "laotian", "latin american", "colombian", "salvadoran", "venezuelan", "live/raw food", "malaysian", "mediterranean", "mexican", "tacos", "middle eastern", "egyptian", "lebanese", "modern australian", "modern european", "mongolian", "moroccan", "new zealand", "nicaraguan", "noodles", "norwegian", "open sandwiches", "oriental", "pakistani", "parent cafes", "eltern cafes", "parma", "persian/iranian", "peruvian", "pita", "pizza", "polish", "pop-up restaurants", "portuguese", "pueblan", "puerto rican", "pulp fiction", "rice", "romanian", "rotisserie chicken", "rumanian", "russian", "salad", "sandwiches", "scandinavian", "scottish", "seafood", "serbo croatian", "signature cuisine", "singaporean", "slovakian", "soul food", "soup", "southern", "spanish", "arroceria / paella", "sri lankan", "steakhouses", "steak", "sushi bars", "swabian", "swedish", "swiss food", "syrian", "tabernas", "taiwanese", "tapas bars", "tapas/small plates", "tavola calda", "thai", "theater cafes", "turkish", "ukrainian", "uzbek", "vegan", "vegetarian", "venison", "vietnamese", "wok", "wurstelstand");

        if (cuisine == null || cuisine.isEmpty()) {
            validationResult.put("isValid", false);
            validationResult.put("violatedSlot", "Cuisine");
            validationResult.put("message", "What cuisine would you like to try?");
            return validationResult;
        }

        cuisine = cuisine.toLowerCase();

        boolean isMatchingCuisine = false;
        for (String c : cuisineTypes) {
            if (c.toLowerCase().contains(cuisine)) {
                isMatchingCuisine = true;
                cuisine = c;
                break;
            }
        }

        if (!isMatchingCuisine) {
            validationResult.put("isValid", false);
            validationResult.put("violatedSlot", "Cuisine");
            validationResult.put("message", "We do not have any restaurant that serves " + cuisine +
                    ". Would you like to enter a different cuisine?");
            return validationResult;
        }

        if (diningTime == null || diningTime.isEmpty()) {
            validationResult.put("isValid", false);
            validationResult.put("violatedSlot", "Dining_time");
            validationResult.put("message", "Which day do you plan on going?");
            return validationResult;
        }

        if (numberOfPeople == null || numberOfPeople.isEmpty()) {
            validationResult.put("isValid", false);
            validationResult.put("violatedSlot", "Number_of_people");
            validationResult.put("message", "How many people are in your party?");
            return validationResult;
        } else {
            int numPeople = Integer.parseInt(numberOfPeople);
            if (numPeople > 20 || numPeople < 0) {
                validationResult.put("isValid", false);
                validationResult.put("violatedSlot", "Number_of_people");
                validationResult.put("message", "We only support groups smaller than 20 people. Please enter a valid number of people.");
                return validationResult;
            }
        }

        if (email == null || email.isEmpty()) {
            validationResult.put("isValid", false);
            validationResult.put("violatedSlot", "Email");
            validationResult.put("message", "Please provide me with your email so I can send you my findings.");
            return validationResult;
        }

        validationResult.put("isValid", true);
        return validationResult;
    }
}
