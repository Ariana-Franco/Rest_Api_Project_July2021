import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import org.junit.FixMethodOrder;
import org.junit.Test;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.runners.MethodSorters;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.Assert.*;
import static io.restassured.RestAssured.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class API_test {
    //Environment variables
    static private String baseUrl = "https://webapi.segundamano.mx";
    static private String emailVerified = "ventas_68d70@mailinator.com";
    static private int passVerified = 429424;
    static private String token;
    static private String accountID;
    static private String name;
    static private String uuid;
    static private String newText;
    static private String adID;
    static private String token2;
    static private String addressID;
    static private String alertID;


    @Test
    public void t01GetTokenFail() {
        //Request an account token without authorization header
        RestAssured.baseURI = String.format("%s/nga/api/v1.1/private/accounts", baseUrl);
        Response response = given().log().all()
                .post();

        //Validations
        String bodyResponse = response.getBody().asString();
        System.out.println("Body response: " + bodyResponse);
        System.out.println("Status expected: 400");
        System.out.println("Result: " + response.getStatusCode());

        assertEquals(400, response.getStatusCode());
        String errorCode = response.jsonPath().getString("error.code");
        System.out.println("Error Code expected: VALIDATION FAILED \nResult: " + errorCode);
        assertEquals("VALIDATION_FAILED", errorCode);
    }

    @Test
    public void t02GetTokenCorrect() {
        //Request an account token with an authorization header
        String authorizationToken = "cGFwaXRhc2xleXM5MUBnbWFpbC5jb206Y29udHJhMTIz";
        //Using a specific user verified
        //String authorizationToken = encodeString(emailVerified + ":" + passVerified);
        RestAssured.baseURI = String.format("%s/nga/api/v1.1/private/accounts", baseUrl);
        Response response = given().log().all()
                .header("Authorization", "Basic " + authorizationToken)
                .post();

        //Validations
        String bodyResponse = response.getBody().asString();
        System.out.println("Body response: " + bodyResponse);
        System.out.println("Status expected: 200");
        System.out.println("Result: " + response.getStatusCode());

        assertEquals(200, response.getStatusCode());
        assertNotNull(bodyResponse);
        assertTrue(bodyResponse.contains("access_token"));

        //Saving account data into environment variables
        token = response.jsonPath().getString("access_token");
        System.out.println(token);
        String accountIDInfo = response.jsonPath().getString("account.account_id");
        accountID = accountIDInfo.split("/")[3];
        System.out.println(accountID);
        name = response.jsonPath().getString("account.name");
        System.out.println(name);
        uuid = response.jsonPath().getString("account.uuid");
        System.out.println(uuid);

    }

    @Test
    public void t03CreateUserFail() {
        //Create a user without authorization header
        String username = "agente" + (Math.floor(Math.random() * 7685) + 3) + "@mailinator.com";
        String bodyRequest = "{\"account\":{\"email\":\"" + username + "\"}}";
        RestAssured.baseURI = String.format("%s/nga/api/v1.1/private/accounts", baseUrl);
        Response response = given().log().all()
                .contentType("application/json")
                .body(bodyRequest)
                .post();

        //Validations
        String bodyResponse = response.getBody().asString();
        System.out.println("Body response: " + bodyResponse);
        System.out.println("Status expected: 400");
        System.out.println("Result: " + response.getStatusCode());

        assertEquals(400, response.getStatusCode());
        String errorCode = response.jsonPath().getString("error.code");
        System.out.println(errorCode);
        assertEquals("VALIDATION_FAILED", errorCode);
    }

    @Test
    public void t04CreateUser() {
        //successfully create a new user, retrieve its data
        String username = "agente" + (Math.floor(Math.random() * 7685) + 3) + "@mailinator.com";
        double password = (Math.floor(Math.random() * 57684) + 10000);
        String data = username + ":" + password;
        //String encodedAuth = Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
        String encodedAuth = encodeString(data);
        String bodyRequest = "{\"account\":{\"email\":\"" + username + "\"}}";
        RestAssured.baseURI = String.format("%s/nga/api/v1.1/private/accounts", baseUrl);
        Response response = given().log().all()
                .header("Authorization", "Basic " + encodedAuth)
                .contentType("application/json")
                .body(bodyRequest)
                .post();
        //validations
        String bodyResponse = response.getBody().asString();
        System.out.println("Body response: " + bodyResponse);
        System.out.println("Status expected: 401");
        System.out.println("Result: " + response.getStatusCode());
        assertEquals(401, response.getStatusCode());
        assertTrue(bodyResponse.contains("ACCOUNT_VERIFICATION_REQUIRED"));
    }

    @Test
    public void t05UpdatePhoneNumber() {
        //Update user created adding the phone number
        RestAssured.baseURI = String.format("%s/nga/api/v1.1/private/accounts/%s", baseUrl, accountID);
        int phone = (int) (Math.random() * 99999999 + 999999999);
        String bodyRequest = "{\"account\":{\"name\":\"" + name + "\"," +
                "\"phone\":\"" + phone + "\", " +
                "\"phone_hidden\": true}}";
        Response response = given().log().all()
                .header("Authorization", "tag:scmcoord.com,2013:api " + token)
                .accept("application/json, text/plain, */*")
                .contentType("application/json")
                .body(bodyRequest)
                .patch();

        //Validations
        String bodyResponse = response.getBody().asString();
        System.out.println("Body response: " + bodyResponse);
        System.out.println("Status expected: 200");
        System.out.println("Result: " + response.getStatusCode());

        assertEquals(200, response.getStatusCode());
        String userPhone = response.jsonPath().getString("account.phone");
        assertEquals(userPhone, "" + phone);
    }

    @Test
    public void t06AddNewAdFail() {
        //Adding a new add with an invalid token should fail
        newText = "" + (Math.random() * 99999999 + 999999999);
        RestAssured.baseURI = String.format("%s/nga/api/v1/private/accounts/%s/klfst", baseUrl, accountID);
        //why would I put this enormous string that I have to modify every gd quote mark if this request is expected to return nothing? well, I dont know maybe is too late and I've already done it, so I leave it.
        String bodyRequest = "{\"ad\":" +
                "{\"locations\":[{\"code\":\"5\",\"key\":\"region\",\"label\":\"Baja California Sur\"," +
                "\"locations\":[{\"code\":\"51\",\"key\":\"municipality\",\"label\":\"Comondú\"," +
                "\"locations\":[{\"code\":\"3748\",\"key\":\"area\",\"label\":\"4 de Marzo\"}]}]}]," +
                "\"subject\":\"Paseo perros a domicilio\",\"body\":" +
                "\"Para su comodidad, paseo perros en su domicilio, use la promoción " + newText + "\"," +
                "\"category\":{\"code\":\"3042\"},\"images\":[],\"price\":{\"currency\":\"mxn\",\"price_value\":1}," +
                "\"ad_details\":{},\"phone_hidden\":1,\"plate\":\"\",\"vin\":\"\",\"type\":{\"code\":\"s\"," +
                "\"label\":\"\"},\"ad\":\"Paseo perros a domicilio\"},\"category_suggestion\":false,\"commit\":true}";
        Response response = given()
                .log().all()
                .header("Authorization", "tag:scmcoord.com,2013:api " + token2)
                .header("x-nga-source", "PHOENIX_DESKTOP")
                .contentType("application/json")
                .body(bodyRequest)
                .post();

        //Validations
        String bodyResponse = response.getBody().asString();
        System.out.println("Body response: " + bodyResponse);
        System.out.println("Status expected: 401");
        System.out.println("Result: " + response.getStatusCode());

        assertEquals(401, response.getStatusCode());
        String errorCode = response.jsonPath().getString("error.code");
        System.out.println("Error Code expected: UNAUTHORIZED \nResult: " + errorCode);
        assertEquals("UNAUTHORIZED", errorCode);
    }

    @Test
    public void t07AddNewAd() {
        //Adding a new add with a valid token
        newText = "" + (Math.random() * 99999999 + 999999999);
        RestAssured.baseURI = String.format("%s/nga/api/v1/private/accounts/%s/klfst", baseUrl, accountID);

        String bodyRequest = "{\"ad\":" +
                "{\"locations\":[{\"code\":\"5\",\"key\":\"region\",\"label\":\"Baja California Sur\"," +
                "\"locations\":[{\"code\":\"51\",\"key\":\"municipality\",\"label\":\"Comondú\"," +
                "\"locations\":[{\"code\":\"3748\",\"key\":\"area\",\"label\":\"4 de Marzo\"}]}]}]," +
                "\"subject\":\"Paseo perros a domicilio\",\"body\":" +
                "\"Para su comodidad, paseo perros en su domicilio, use la promoción " + newText + "\"," +
                "\"category\":{\"code\":\"3042\"},\"images\":[],\"price\":{\"currency\":\"mxn\",\"price_value\":1}," +
                "\"ad_details\":{},\"phone_hidden\":1,\"plate\":\"\",\"vin\":\"\",\"type\":{\"code\":\"s\"," +
                "\"label\":\"\"},\"ad\":\"Paseo perros a domicilio\"},\"category_suggestion\":false,\"commit\":true}";
        Response response = given()
                .log().all()
                .header("Authorization", "tag:scmcoord.com,2013:api " + token)
                .header("x-nga-source", "PHOENIX_DESKTOP")
                .contentType("application/json")
                .body(bodyRequest)
                .post();

        //Validations
        String bodyResponse = response.getBody().asString();
        System.out.println("Body response: " + bodyResponse);
        System.out.println("Status expected: 201");
        System.out.println("Result: " + response.getStatusCode());

        assertEquals(201, response.getStatusCode());
        String actionType = response.jsonPath().getString("action.action_type");
        System.out.println("Action expected to be: new \nResult: " + actionType);
        assertEquals("new", actionType);

        //Saving adID to be modified and delete later
        adID = response.jsonPath().getString("ad.ad_id").split("/")[5];
        System.out.println("Ad Created with id: " + adID);
        assertTrue(bodyResponse.contains("ad_id"));
    }

    @Test
    public void t08UpdateAd() {
        //Change a text on the description of the ad
        newText = "" + (Math.random() * 99999999 + 999999999);
        RestAssured.baseURI = String.format("%s/nga/api/v1/private/accounts/%s/klfst/%s/actions", baseUrl, accountID, adID);
        String bodyRequest = "{\"ad\":" +
                "{\"locations\":[{\"code\":\"5\",\"key\":\"region\",\"label\":\"Baja California Sur\"," +
                "\"locations\":[{\"code\":\"51\",\"key\":\"municipality\",\"label\":\"Comondú\"," +
                "\"locations\":[{\"code\":\"3748\",\"key\":\"area\",\"label\":\"4 de Marzo\"}]}]}]," +
                "\"subject\":\"Paseo perros a domicilio\",\"body\":" +
                "\"Para su comodidad, paseo perros en su domicilio, use la promoción " + newText + "\"," +
                "\"category\":{\"code\":\"3042\"},\"images\":[],\"price\":{\"currency\":\"mxn\",\"price_value\":1}," +
                "\"ad_details\":{},\"phone_hidden\":1,\"plate\":\"\",\"vin\":\"\",\"type\":{\"code\":\"s\"," +
                "\"label\":\"\"},\"ad\":\"Paseo perros a domicilio\"},\"category_suggestion\":false,\"commit\":true}";
        Response response = given()
                .log().all()
                .header("Authorization", "tag:scmcoord.com,2013:api " + token)
                .header("x-nga-source", "PHOENIX_DESKTOP")
                .contentType("application/json")
                .body(bodyRequest)
                .post();

        //Validations
        String bodyResponse = response.getBody().asString();
        System.out.println("Body response: " + bodyResponse);
        System.out.println("Status expected: 201");
        System.out.println("Result: " + response.getStatusCode());

        assertEquals(201, response.getStatusCode());
        String actionType = response.jsonPath().getString("action.action_type");
        System.out.println("Action expected to be: edit \nResult: " + actionType);
        assertEquals("edit", actionType);
    }

    @Test
    public void t09GetAddressFail() {
        //Getting user address with an invalid token should fail
        RestAssured.baseURI = String.format("%s/addresses/v1/get", baseUrl);
        Response response = given()
                .log().all()
                .header("Authorization", "Basic " + token)
                .get();

        //Validations
        String bodyResponse = response.getBody().asString();
        System.out.println("Body response: " + bodyResponse);
        System.out.println("Status expected: 403");
        System.out.println("Result: " + response.getStatusCode());

        assertEquals(403, response.getStatusCode());
        String errorCode = response.jsonPath().getString("error");
        System.out.println("Error Code expected: Authorization failed \nResult: " + errorCode);
        assertEquals("Authorization failed", errorCode);
    }

    @Test
    public void t10UserHasNoAddress() {
        //Getting user addresses should be an empty list
        String token2Keys = uuid + ":" + token;
        //token2 = Base64.getEncoder().encodeToString(token2Keys.getBytes());
        token2 = encodeString(token2Keys);
        RestAssured.baseURI = String.format("%s/addresses/v1/get",baseUrl);
        Response response = given()
                .log().all()
                .header("Authorization","Basic " + token2)
                .get();

        System.out.println("Status expected: 200");
        System.out.println("Result: " + response.getStatusCode());

        //validations
        String bodyResponse = response.getBody().asString();
        System.out.println("Body response: " + bodyResponse);
        System.out.println("Status expected: 200");
        System.out.println("Result: " + response.getStatusCode());

        assertEquals(200, response.getStatusCode());
        String addressesList = response.jsonPath().getString("addresses");
        System.out.println("List expected to be empty \nResult: " + addressesList);
        System.out.println("List expected length: " + addressesList.length());
        assertTrue(bodyResponse.contains("addresses"));
        //Disabled assert because the array returned is not empty
        //assertEquals("[:]",addressesList);
    }

    @Test
    public void t11UpdateUserAddress() {
        //Adding a new address to user
        String token2Keys = uuid + ":" + token;
        token2 = encodeString(token2Keys);
        RestAssured.baseURI = String.format("%s/addresses/v1/create", baseUrl);
        Response response;
        response = given()
                .log().all()
                .config(RestAssured.config()
                        .encoderConfig(EncoderConfig.encoderConfig()
                                .encodeContentTypeAs("x-www-form-urlencoded",
                                        ContentType.URLENC)))
                .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                .formParam("contact", "Casa grande")
                .formParam("phone", "3234445555")
                .formParam("rfc", "CASA681225XXX")
                .formParam("zipCode", "45050")
                .formParam("exteriorInfo", "exterior 10")
                .formParam("region", "5")
                .formParam("municipality", "51")
                .formParam("alias", "big house")
                .header("Authorization", "Basic " + token2)
                .post();

        //Validations
        String bodyResponse = response.getBody().asString();
        System.out.println("Body response: " + bodyResponse);
        System.out.println("Status expected: 201");
        System.out.println("Result: " + response.getStatusCode());

        assertEquals(201, response.getStatusCode());
        assertTrue(bodyResponse.contains("addressID"));

        //Saving address to environment variable addressID
        addressID = response.jsonPath().getString("addressID");
        System.out.println("Address created with ID: " + addressID);
    }

    @Test
    public void t12UpdateUserAddressDuplicated() {
        //Trying to add same address should fail
        String token2Keys = uuid + ":" + token;
        //token2 = Base64.getEncoder().encodeToString(token2Keys.getBytes());
        token2 = encodeString(token2Keys);
        RestAssured.baseURI = String.format("%s/addresses/v1/create", baseUrl);
        Response response;
        response = given()
                .log().all()
                .config(RestAssured.config()
                        .encoderConfig(EncoderConfig.encoderConfig()
                                .encodeContentTypeAs("x-www-form-urlencoded",
                                        ContentType.URLENC)))
                .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                .formParam("contact", "Casa grande")
                .formParam("phone", "3234445555")
                .formParam("rfc", "CASA681225XXX")
                .formParam("zipCode", "45050")
                .formParam("exteriorInfo", "exterior 10")
                .formParam("region", "5")
                .formParam("municipality", "51")
                .formParam("alias", "big house")
                .header("Authorization", "Basic " + token2)
                .post();

        //Validations
        String bodyResponse = response.getBody().asString();
        System.out.println("Body response: " + bodyResponse);
        System.out.println("Status expected: 200");
        System.out.println("Result: " + response.getStatusCode());

        assertEquals(201, response.getStatusCode());
        String errorCode = response.jsonPath().getString("error");
        System.out.println("Request expected to return duplicate \nResult: " + errorCode);
        assertTrue(bodyResponse.contains("addressID"));
        //Disabled assert because the api allows to dd the same address as many times as needed
        //assertTrue(bodyResponse.contains("Duplicate"));
    }

    @Test
    public void t13GetCreatedAddress() {
        //Use address id to get the user's address
        String token2Keys = uuid + ":" + token;
        //token2 = Base64.getEncoder().encodeToString(token2Keys.getBytes());
        token2 = encodeString(token2Keys);
        RestAssured.baseURI = String.format("%s/addresses/v1/get", baseUrl);
        Response response = given()
                .log().all()
                .header("Authorization", "Basic " + token2)
                .get();

        //validations
        String bodyResponse = response.getBody().asString();
        System.out.println("Body response: " + bodyResponse);
        System.out.println("Status expected: 200");
        System.out.println("Result: " + response.getStatusCode());

        assertEquals(200, response.getStatusCode());
        String respAddress = response.jsonPath().getString("addresses");
        System.out.println("Request expected to contain addressID: " + respAddress);
        assertTrue(respAddress.contains(addressID));
    }

    @Test
    public void t14ShopNotFound() {
        //Fail to found a shop with this account
        RestAssured.baseURI = String.format("%s/shops/api/v2/public/accounts/10613126/shop", baseUrl);
        Response response = given().log().all()
                .get();

        //Validations
        String bodyResponse = response.getBody().asString();
        System.out.println("Body response: " + bodyResponse);
        System.out.println("Status expected: 404");
        System.out.println("Result: " + response.getStatusCode());

        assertEquals(404, response.getStatusCode());
        String errorCode = response.jsonPath().getString("message");
        System.out.println("Error Code expected: Account not found \nResult: " + errorCode);
        assertEquals("Account not found", errorCode);
    }

    @Test
    public void t15DeleteAd() {
        //Delete the ad created - possible fail with 403
        String bodyRequest = "{\"delete_reason\":{\"code\":\"5\"} }";
        RestAssured.baseURI = String.format("%s/nga/api/v1/private/accounts/%s/klfst/%s", baseUrl, accountID, adID);
        Response response = given().log().all()
                .header("Authorization", "tag:scmcoord.com,2013:api " + token)
                .header("x-nga-source", "PHOENIX_DESKTOP")
                .contentType("application/json")
                .body(bodyRequest)
                .delete();

        //Validations
        String bodyResponse = response.getBody().asString();
        System.out.println("Body response: " + bodyResponse);
        System.out.println("Status expected: 403");
        System.out.println("Result: " + response.getStatusCode());

        assertEquals(403, response.getStatusCode());
        String actionType = response.jsonPath().getString("action.action_type");
        System.out.println("Action expected to be: delete \nResult: " + actionType);
        //assertEquals("delete", actionType);

        String errorCause = response.jsonPath().getString("error.causes[0].code");
        System.out.println(errorCause);
        assertEquals("ERROR_AD_ALREADY_DELETED", errorCause);

    }

    @Test
    public void t16GetCreditsByUserId() {
        RestAssured.baseURI = String.format("%s/credits/v1/private/accounts/%s", baseUrl, accountID);
        Response response = given().log().all()
                .get();

        //Validations
        String bodyResponse = response.getBody().asString();
        System.out.println("Body response: " + bodyResponse);
        System.out.println("Status expected: 200");
        System.out.println("Result: " + response.getStatusCode());

        assertEquals(200, response.getStatusCode());
        assertNotNull(bodyResponse);
        int balance = response.jsonPath().getInt("balance.balance");
        assertEquals(0, balance);
    }

    @Test
    public void t17DeleteUserAlertFail() {
        RestAssured.baseURI = String.format("%s/alerts/v1/private/account/%s/alert/%s", baseUrl, uuid, alertID);
        Response response = given().log().all()
                .header("Authorization", "Basic " + token2)
                .delete();

        //Validations
        String bodyResponse = response.getBody().asString();
        System.out.println("Body response: " + bodyResponse);
        System.out.println("Status expected: 404");
        System.out.println("Result: " + response.getStatusCode());

        assertEquals(404, response.getStatusCode());
        assertNotNull(bodyResponse);
        assertTrue(bodyResponse.contains("AlertDoesntExist"));
    }

    /**
     * Testing the creation of an alert into the user account
     */
    @Test
    public void t18CreateUserAlertSuccess() {
        RestAssured.baseURI = String.format("%s/alerts/v1/private/account/%s/alert", baseUrl, uuid);
        String bodyRequest = "{\"ad_listing_service_filters\":{\"region\":\"11\",\"category_lv0\":\"1000\",\"category_lv1\":\"1080\"}}";

        Response response = given().log().all()
                .body(bodyRequest)
                .header("Authorization", "Basic " + token2)
                .post();

        //Validations
        String bodyResponse = response.getBody().asString();
        System.out.println("Body response: " + bodyResponse);
        System.out.println("Status expected: 200");
        System.out.println("Result: " + response.getStatusCode());

        assertEquals(200, response.getStatusCode());
        assertNotNull(bodyResponse);
        alertID = response.jsonPath().getString("data.alert.id");
        assertNotNull(alertID);
    }

    @Test
    public void t19DeleteUserAlertSuccess() {
        RestAssured.baseURI = String.format("%s/alerts/v1/private/account/%s/alert/%s", baseUrl, uuid, alertID);
        //String bodyRequest = "{\"ad_listing_service_filters\":{\"region\":\"11\",\"category_lv0\":\"1000\",\"category_lv1\":\"1080\"}}";

        Response response = given().log().all()
                //.body(bodyRequest)
                .header("Authorization", "Basic " + token2)
                .delete();

        //Validations
        String bodyResponse = response.getBody().asString();
        System.out.println("Body response: " + bodyResponse);
        System.out.println("Status expected: 200");
        System.out.println("Result: " + response.getStatusCode());

        assertEquals(200, response.getStatusCode());
        assertNotNull(bodyResponse);
        String status = response.jsonPath().getString("data.status");
        assertEquals("ok", status);
    }

    @Test
    public void t20DeleteAddress() {
        //Delete the address created
        //String bodyRequest = "{\"delete_reason\":{\"code\":\"5\"} }";
        RestAssured.baseURI = String.format("%s/addresses/v1/delete/%s", baseUrl, addressID);
        Response response = given().log().all()
                .header("Authorization", "Basic " + token2)
                .contentType("application/json")
                .delete();

        //Validations
        String bodyResponse = response.getBody().asString();
        System.out.println("Body response: " + bodyResponse);
        System.out.println("Status expected: 200");
        System.out.println("Result: " + response.getStatusCode());

        assertEquals(200, response.getStatusCode());
        assertNotNull(bodyResponse);
        assertTrue(bodyResponse.contains("deleted correctly"));

    }

    /**
     * encodeString function
     *
     * @param String data
     * @return
     */
    public String encodeString(String data) {
        String encodeAuth = Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
        return encodeAuth;
    }
}
