package Lambda1;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.google.gson.Gson;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class RestaurantRecommendationHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        Logger logger = Logger.getLogger(RestaurantRecommendationHandler.class.getName());

        String httpMethod = input.getHttpMethod();
        if (httpMethod.equals("POST")) {
            String requestBody = input.getBody();
            Map<String, Object> request = new Gson().fromJson(requestBody, Map.class);

            // Extract intent name and handle it
            String intentName = (String) ((Map<String, Object>) request.get("currentIntent")).get("name");

            if ("DiningSuggestionsIntent".equals(intentName)) {
                return getRestaurants(request);
            } else {
                throw new RuntimeException("Intent with name " + intentName + " not supported");
            }
        } else {
            throw new RuntimeException("Unsupported HTTP method: " + httpMethod);
        }
    }

    private APIGatewayProxyResponseEvent getRestaurants(Map<String, Object> request) {
        Map<String, Object> currentIntent = (Map<String, Object>) request.get("currentIntent");
        Map<String, Object> sessionAttributes = (Map<String, Object>) request.get("sessionAttributes");

        // Extract slot values
        Map<String, Object> slots = (Map<String, Object>) currentIntent.get("slots");
        String time = (String) slots.get("Dining_time");
        String cuisine = (String) slots.get("Cuisine");
        String location = (String) slots.get("Location");
        String numPeople = (String) slots.get("Number_of_people");
        String email = (String) slots.get("Email");

        // Implement the logic to validate and process the parameters
        Map<String, Object> validationResults = validateParameters(time, cuisine, location, numPeople, email);

        if (!(boolean) validationResults.get("isValid")) {
            String violatedSlot = (String) validationResults.get("violatedSlot");
            String messageContent = (String) ((Map<String, Object>) validationResults.get("message")).get("content");

            // Build a response for elicitSlot to re-prompt for the violated slot
            return elicitSlot(sessionAttributes, currentIntent.get("name").toString(), slots, violatedSlot, messageContent);
        } else {
            // Push the validated parameters to an SQS queue, *fix the queue url
            boolean pushedToQueue = pushToSqs("https://sqs.us-east-2.amazonaws.com/508829990173/userRequest", slots);

            if (pushedToQueue) {
                // Build a response for a successful request
                String responseMessage = String.format("We have received your request for %s cuisine. " +
                                "You will receive recommendations at %s. " +
                                "Have a great day with your group of %s to dine at %s in the city of %s!",
                        cuisine, email, numPeople, time, location);

                return close(sessionAttributes, responseMessage);
            } else {
                // Build a response for an error
                return close(sessionAttributes, "Sorry, come back after some time!");
            }
        }
    }

    private Map<String, Object> validateParameters(String time, String cuisine, String location, String numPeople, String email) {
        Map<String, Object> validationResult = new HashMap<>();
        boolean isValid = true;
        String violatedSlot = null;
        String messageContent = null;

        // Validate location
        List<String> validLocations = Arrays.asList("New York","New York City", "new york", "Manhattan", "manhattan","nyc", "NYC", "new york city");
        if (!validLocations.contains(location.toLowerCase())) {
            isValid = false;
            violatedSlot = "location";
            messageContent = "We do not have any restaurants serving in this location. Please enter a valid location.";
        }

        // Validate numPeople as an integer less than 20
        try {
            int numPeopleInt = Integer.parseInt(numPeople);
            if (numPeopleInt <= 0 || numPeopleInt >= 20) {
                isValid = false;
                violatedSlot = "num_people";
                messageContent = "Please enter a valid number of people (1-20).";
            }
        } catch (NumberFormatException e) {
            isValid = false;
            violatedSlot = "num_people";
            messageContent = "Please enter a valid number of people (1-20).";
        }

        // Validate cuisine
        List<String> validCuisines = Arrays.asList("Afghan", "afghani", "African", "african", "Senegalese", "senegalese", "South African", "southafrican", "American (New)", "newamerican", "American (Traditional)", "tradamerican", "Andalusian", "andalusian", "Arabic", "arabian", "Arab Pizza", "arabpizza", "Argentine", "argentine", "Armenian", "armenian", "Asian Fusion", "asianfusion", "Asturian", "asturian", "Australian", "australian", "Austrian", "austrian", "Baguettes", "baguettes", "Bangladeshi", "bangladeshi", "Barbeque", "bbq", "Basque", "basque", "Bavarian", "bavarian", "Beer Garden", "beergarden", "Beer Hall", "beerhall", "Beisl", "beisl", "Belgian", "belgian", "Flemish", "flemish", "Bistros", "bistros", "Black Sea", "blacksea", "Brasseries", "brasseries", "Brazilian", "brazilian", "Brazilian Empanadas", "brazilianempanadas", "Central Brazilian", "centralbrazilian", "Northeastern Brazilian", "northeasternbrazilian", "Northern Brazilian", "northernbrazilian", "Rodizios", "rodizios", "Breakfast & Brunch", "breakfast_brunch", "Pancakes", "pancakes", "British", "british", "Buffets", "buffets", "Bulgarian", "bulgarian", "Burgers", "burgers", "Burmese", "burmese", "Cafes", "cafes", "Themed Cafes", "themedcafes", "Cafeteria", "cafeteria", "Cajun/Creole", "cajun", "Cambodian", "cambodian", "Canadian (New)", "newcanadian", "Canteen", "canteen", "Caribbean", "caribbean", "Dominican", "dominican", "Haitian", "haitian", "Puerto Rican", "puertorican", "Trinidadian", "trinidadian", "Catalan", "catalan", "Cheesesteaks", "cheesesteaks", "Chicken Shop", "chickenshop", "Chicken Wings", "chicken_wings", "Chilean", "chilean", "Chinese", "chinese", "Cantonese", "cantonese", "Congee", "congee", "Dim Sum", "dimsum", "Fuzhou", "fuzhou", "Hainan", "hainan", "Hakka", "hakka", "Henghwa", "henghwa", "Hokkien", "hokkien", "Hunan", "hunan", "Pekinese", "pekinese", "Shanghainese", "shanghainese", "Szechuan", "szechuan", "Teochew", "teochew", "Comfort Food", "comfortfood", "Corsican", "corsican", "Creperies", "creperies", "Cuban", "cuban", "Curry Sausage", "currysausage", "Cypriot", "cypriot", "Czech", "czech", "Czech/Slovakian", "czechslovakian", "Danish", "danish", "Delis", "delis", "Diners", "diners", "Dinner Theater", "dinnertheater", "Dumplings", "dumplings", "Eastern European", "eastern_european", "Eritrean", "eritrean", "Ethiopian", "ethiopian", "Fast Food", "hotdogs", "Filipino", "filipino", "Fischbroetchen", "fischbroetchen", "Fish & Chips", "fishnchips", "Flatbread", "flatbread", "Fondue", "fondue", "Food Court", "food_court", "Food Stands", "foodstands", "Freiduria", "freiduria", "French", "french", "Alsatian", "alsatian", "Auvergnat", "auvergnat", "Berrichon", "berrichon", "Bourguignon", "bourguignon", "Mauritius", "mauritius", "Nicoise", "nicois", "Provencal", "provencal", "Reunion", "reunion", "French Southwest", "sud_ouest", "Galician", "galician", "Game Meat", "gamemeat", "Gastropubs", "gastropubs", "Georgian", "georgian", "German", "german", "Baden", "baden", "Eastern German", "easterngerman", "Hessian", "hessian", "Northern German", "northerngerman", "Palatine", "palatine", "Rhinelandian", "rhinelandian", "Swabian", "swabian", "Giblets", "giblets", "Gluten-Free", "gluten_free", "Greek", "greek", "Grilled Cheese", "grilledcheese", "Grocery", "grocery", "Gyros", "gyros", "Halal", "halal", "Hawaiian", "hawaiian", "Heuriger", "heuriger", "Himalayan/Nepalese", "himalayan", "Honduran", "honduran", "Hong Kong Style Cafe", "hkcafe", "Hot Dogs", "hotdog", "Hot Pot", "hotpot", "Hungarian", "hungarian", "Iberian", "iberian", "Indian", "indpak", "Indonesian", "indonesian", "International", "international", "Irish", "irish", "Island Pub", "island_pub", "Italian", "italian", "Calabrian", "calabrian", "Sardinian", "sardinian", "Sicilian", "sicilian", "Tuscan", "tuscan", "Japanese", "japanese", "Kaiseki", "kaiseki", "Karaoke", "karaoke", "Kebab", "kebab", "Korean", "korean", "Kosher", "kosher", "Kurdish", "kurdish", "Laos", "laos", "Laotian", "laotian", "Latin American", "latin", "Colombian", "colombian", "Salvadoran", "salvadoran", "Venezuelan", "venezuelan", "Live/Raw Food", "raw_food", "Malaysian", "malaysian", "Mediterranean", "mediterranean", "Eastern European", "eastern_european", "Mexican", "mexican", "Tacos", "tacos", "Middle Eastern", "mideastern", "Egyptian", "egyptian", "Lebanese", "lebanese", "Modern Australian", "modern_australian", "Modern European", "modern_european", "Mongolian", "mongolian", "Moroccan", "moroccan", "New Zealand", "newzealand", "Nicaraguan", "nicaraguan", "Noodles", "noodles", "Norwegian", "norwegian", "Open Sandwiches", "opensandwiches", "Oriental", "oriental", "Pakistani", "pakistani", "Parent Cafes", "eltern_cafes", "Parma", "parma", "Persian/Iranian", "persian", "Peruvian", "peruvian", "Pita", "pita", "Pizza", "pizza", "Polish", "polish", "Pop-Up Restaurants", "popuprestaurants", "Portuguese", "portuguese", "Pueblan", "pueblan", "Puerto Rican", "puertorican", "Pulp Fiction", "pulpfiction", "Rice", "rice", "Romanian", "romanian", "Rotisserie Chicken", "rotisserie_chicken", "Rumanian", "rumanian", "Russian", "russian", "Salad", "salad", "Sandwiches", "sandwiches", "Scandinavian", "scandinavian", "Scottish", "scottish", "Seafood", "seafood", "Serbo Croatian", "serbocroatian", "Signature Cuisine", "signature_cuisine", "Singaporean", "singaporean", "Slovakian", "slovakian", "Soul Food", "soulfood", "Soup", "soup", "Southern", "southern", "Spanish", "spanish", "Arroceria / Paella", "arroceria_paella", "Sri Lankan", "srilankan", "Steakhouses", "steak", "Sushi Bars", "sushi", "Swabian", "swabian", "Swedish", "swedish", "Swiss Food", "swissfood", "Syrian", "syrian", "Tabernas", "tabernas", "Taiwanese", "taiwanese", "Tapas Bars", "tapas", "Tapas/Small Plates", "tapasmallplates", "Tavola Calda", "tavolacalda", "Thai", "thai", "Theater Cafes", "theatercafes", "Turkish", "turkish", "Ukrainian", "ukrainian", "Uzbek", "uzbek", "Vegan", "vegan", "Vegetarian", "vegetarian", "Venison", "venison", "Vietnamese", "vietnamese", "Wok", "wok", "Wurstelstand", "wurstelstand");
        if (!validCuisines.contains(cuisine.toLowerCase())) {
            isValid = false;
            violatedSlot = "cuisine";
            messageContent = "We do not have any restaurants serving that cuisine. Please choose a valid cuisine.";
        }

        // If a parameter is invalid, set isValid to false and provide the violated slot and message
        if (!isValid) {
            validationResult.put("isValid", false);
            validationResult.put("violatedSlot", violatedSlot);
            String finalMessageContent = messageContent;
            validationResult.put("message", new HashMap<String, Object>() {{
                put("contentType", "PlainText");
                put("content", finalMessageContent);
            }});
        } else {
            validationResult.put("isValid", true);
        }

        return validationResult;
    }

    private APIGatewayProxyResponseEvent elicitSlot(Map<String, Object> sessionAttributes, String intentName, Map<String, Object> slots, String slotToElicit, String message) {
        // Implement elicitSlot response
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> dialogAction = new HashMap<>();
        Map<String, Object> messageContent = new HashMap<>();

        dialogAction.put("type", "ElicitSlot");
        dialogAction.put("intentName", intentName);
        dialogAction.put("slots", slots);
        dialogAction.put("slotToElicit", slotToElicit);

        messageContent.put("contentType", "PlainText");
        messageContent.put("content", message);

        dialogAction.put("message", messageContent);

        response.put("sessionAttributes", sessionAttributes);
        response.put("dialogAction", dialogAction);

        APIGatewayProxyResponseEvent apiResponse = new APIGatewayProxyResponseEvent();
        // Set response body, headers, etc.
        apiResponse.setStatusCode(200);
        apiResponse.setBody(new Gson().toJson(response));

        return apiResponse;
    }

    private boolean pushToSqs(String queueUrl, Map<String, Object> msgBody) {
        try {
            AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
            String msgBodyString = new Gson().toJson(msgBody);

            SendMessageRequest sendMsgRequest = new SendMessageRequest()
                    .withQueueUrl(queueUrl)
                    .withMessageBody(msgBodyString);

            // Optionally, you can set message attributes if needed
            // sendMsgRequest.withMessageAttributes(messageAttributes);

            sqs.sendMessage(sendMsgRequest);
            return true; // Message successfully sent to SQS
        } catch (Exception e) {
            // Handle any exceptions, e.g., logging, error reporting, etc.
            e.printStackTrace();
            return false; // Message failed to send to SQS
        }
    }

    private APIGatewayProxyResponseEvent close(Map<String, Object> sessionAttributes, String message) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> dialogAction = new HashMap<>();
        Map<String, Object> messageContent = new HashMap<>();

        dialogAction.put("type", "Close");
        dialogAction.put("fulfillmentState", "Fulfilled");

        messageContent.put("contentType", "PlainText");
        messageContent.put("content", message);

        dialogAction.put("message", messageContent);

        response.put("sessionAttributes", sessionAttributes);
        response.put("dialogAction", dialogAction);

        APIGatewayProxyResponseEvent apiResponse = new APIGatewayProxyResponseEvent();
        apiResponse.setStatusCode(200);
        apiResponse.setBody(new Gson().toJson(response));

        return apiResponse;
    }
}
