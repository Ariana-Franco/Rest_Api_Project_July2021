
import org.junit.Test;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import static org.junit.Assert.*;
import static io.restassured.RestAssured.*;
import java.util.Base64;


public class RestApi1 {

 //Variables
    static private String baseUrl  = "https://webapi.segundamano.mx";
    static private String token;
    static private String accountID;
    static private String name;
    static private String uuid;
    static private String newText;
    static private String adID;
    static private String token2;
    static private String address_id;

    @Test
    public void get_token_status_fail_test(){
        given().queryParam("lang","es").when()
                .log().all()
                .post("https://webapi.segundamano.mx/nga/api/v1.1/private/accounts")
                .then().statusCode(400);
    }


    @Test
    public void Test_token_fail() {
        //Request an account token without authorization header
        RestAssured.baseURI = String.format("https://webapi.segundamano.mx/nga/api/v1.1/private/accounts");
        Response response = given().log().all()
                .post();
        //validations
        System.out.println("Status expected: 400");
        System.out.println("Result: " + response.getStatusCode());
        assertEquals(400, response.getStatusCode());
        String errorCode = response.jsonPath().getString("error.code");
        System.out.println("Error Code expected: VALIDATION FAILED \nResult: " + errorCode);
        assertEquals("VALIDATION_FAILED", errorCode);
    }

    @Test
    public void Create_account() {
        RestAssured.baseURI = String.format("https://webapi.segundamano.mx/nga/api/v1.1/private/accounts?lang=es");
        Response response = given().log().all()
                .post();
        System.out.println("Result: " + response.getStatusCode());


    }

    @Test
    public void validateStatusCode() {
        given().queryParam("lang", "es").when()
                .get("https://webapi.segundamano.mx/nga/api/v1.1/private/accounts")
                .then().statusCode(401);
    }


    @Test
    public void t02_get_token_correct(){
        //Request an account token with an authorization header
        String authorizationToken = "cGFwaXRhc2xleXM5MUBnbWFpbC5jb206Y29udHJhMTIz";
        RestAssured.baseURI = String.format("%s/nga/api/v1.1/private/accounts",baseUrl);
        Response response = given().log().all()
                .header("Authorization","Basic " + authorizationToken)
                .post();
        //validations
        String body = response.getBody().asString();
        System.out.println("Status expected: 200" );
        System.out.println("Body reposne:" + body );
        System.out.println("Result token: " + response.getStatusCode());
        assertEquals(200,response.getStatusCode());
        assertTrue(body.contains("access_token"));
        token = response.jsonPath().getString("access_token");
        //token = response.access_token;
    }

    @Test
    public void t10_user_has_no_address(){
        //Get user addresses shoudl be an empty list
        String token2Keys = uuid + ":" + token;
        token2 = Base64.getEncoder().encodeToString(token2Keys.getBytes());
        RestAssured.baseURI = String.format("%s/addresses/v1/get",baseUrl);
        Response response = given()
                .log().all()
                .header("Authorization","Basic " + token2)
                .get();
        //validations
        String body = response.getBody().asString();
        System.out.println("Body: " + body );
        System.out.println("Status expected: 200" );
        System.out.println("Result: " + response.getStatusCode());
        assertEquals(200,response.getStatusCode());
        String addressesList = response.jsonPath().getString("addresses");
        System.out.println("List expected to be empty \nResult: " + addressesList);
        assertEquals("[:]",addressesList);
    }

    @Test
    public void j_adAddress() {
        RestAssured.baseURI = String.format("%s/addresses/v1/create", baseUrl);
        Response response = given().log().all().
                header("Authorization", "Basic " + token).
                header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8").
                formParam("contact", "Casa Grande").
                formParam("phone", "3366778899").
                formParam("rfc", "CASE090102").
                formParam("zipCode", "98101").
                formParam("exteriorInfo", "Lopez Mateos 890").
                formParam("region", "11").
                formParam("municipality", "296").
                formParam("alias", "Casa grande").
                post();

        //Save body response as a String
        String body = response.getBody().asString();
        System.out.println("Test 10 Add valid Address - Response Body:" + body);

        //Assertions
        assertEquals(201, response.getStatusCode());
        assertNotNull(body);
        assertTrue(body.contains("addressID"));

        //Set response value into address_id variable
        address_id = response.jsonPath().getString("addressID");
        System.out.println("Address id created: "+ address_id);
    }

}
