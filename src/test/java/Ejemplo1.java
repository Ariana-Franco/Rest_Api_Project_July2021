import org.junit.Test;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import static org.junit.Assert.*;
import static io.restassured.RestAssured.*;

public class Ejemplo1 {

    @Test
    public void Coronavirus(){

        RestAssured.baseURI = String.format("https://api.quarantine.country/api/v1/summary/latest");
        Response response = given()
                .log().all()
                .header("Accept","application/json")
                .get();

        //validations
        String body = response.getBody().asString();
        System.out.println("Status expected: 200" );
        System.out.println("Response: " + body);
        System.out.println("Response Status: " + response.getStatusCode());
        System.out.println("Response Headers: " + response.getHeaders());
        System.out.println("Response ContentType: " + response.contentType());
        assertEquals(200,response.getStatusCode());
        assertTrue(body.contains("summary"));
    }
}
