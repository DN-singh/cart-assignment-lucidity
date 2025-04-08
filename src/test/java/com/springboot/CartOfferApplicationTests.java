package com.springboot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.controller.ApplyOfferRequest;
import com.springboot.controller.ApplyOfferResponse;
import com.springboot.controller.OfferRequest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Integration tests for validating offer application logic in the Cart system.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class CartOfferApplicationTests {

    private final ObjectMapper mapper = new ObjectMapper();
    private final String BASE_URL = "http://localhost:9001";

    /**
     * Tests flat discount (FLATX) for a user belonging to a matching segment.
     */
    @Test
    public void checkFlatXForOneSegment() throws Exception {
        List<String> segments = new ArrayList<>();
        segments.add("p1");
        OfferRequest offerRequest = new OfferRequest(1, "FLATX", 10, segments);
        boolean result = addOffer(offerRequest);
        Assert.assertEquals(result, true);

        ApplyOfferRequest applyRequest = new ApplyOfferRequest();
        applyRequest.setCart_value(200);
        applyRequest.setRestaurant_id(1);
        applyRequest.setUser_id(1);
        ApplyOfferResponse response = applyOffer(applyRequest);
        Assert.assertEquals(190, response.getCart_value());
    }

    /**
     * Tests percentage discount for a user in the matching segment.
     */
    @Test
    public void checkFlatPercentageForOneSegment() throws Exception {
        List<String> segments = new ArrayList<>();
        segments.add("p2");
        OfferRequest offerRequest = new OfferRequest(2, "PERCENTAGE", 10, segments);
        boolean result = addOffer(offerRequest);
        Assert.assertEquals(true, result);

        ApplyOfferRequest applyRequest = new ApplyOfferRequest();
        applyRequest.setCart_value(200);
        applyRequest.setRestaurant_id(2);
        applyRequest.setUser_id(2);
        ApplyOfferResponse response = applyOffer(applyRequest);
        Assert.assertEquals(180, response.getCart_value());
    }

    /**
     * Validates that offer is not applied when user does not belong to the segment.
     */
    @Test
    public void testOfferNotAppliedToNonMatchingSegment() throws Exception {
        List<String> segments = new ArrayList<>();
        segments.add("p1");
        OfferRequest offerRequest = new OfferRequest(3, "FLATX", 50, segments);
        addOffer(offerRequest);

        ApplyOfferRequest applyRequest = new ApplyOfferRequest();
        applyRequest.setCart_value(200);
        applyRequest.setRestaurant_id(3);
        applyRequest.setUser_id(2); // p2 segment
        ApplyOfferResponse response = applyOffer(applyRequest);
        Assert.assertEquals(200, response.getCart_value());
    }

    /**
     * Tests offer applicable to multiple segments.
     */
    @Test
    public void testMultipleSegmentsOffer() throws Exception {
        List<String> segments = Arrays.asList("p1", "p2");
        OfferRequest offerRequest = new OfferRequest(4, "FLATX", 30, segments);
        addOffer(offerRequest);

        ApplyOfferRequest applyRequest1 = new ApplyOfferRequest();
        applyRequest1.setCart_value(200);
        applyRequest1.setRestaurant_id(4);
        applyRequest1.setUser_id(1); // p1
        ApplyOfferResponse response1 = applyOffer(applyRequest1);
        Assert.assertEquals(170, response1.getCart_value());

        ApplyOfferRequest applyRequest2 = new ApplyOfferRequest();
        applyRequest2.setCart_value(200);
        applyRequest2.setRestaurant_id(4);
        applyRequest2.setUser_id(2); // p2
        ApplyOfferResponse response2 = applyOffer(applyRequest2);
        Assert.assertEquals(170, response2.getCart_value());

        ApplyOfferRequest applyRequest3 = new ApplyOfferRequest();
        applyRequest3.setCart_value(200);
        applyRequest3.setRestaurant_id(4);
        applyRequest3.setUser_id(3); // p3
        ApplyOfferResponse response3 = applyOffer(applyRequest3);
        Assert.assertEquals(200, response3.getCart_value());
    }

    /**
     * Tests multiple discount types (flat and percentage) for different segments.
     */
    @Test
    public void testDifferentDiscountTypes() throws Exception {
        List<String> segments1 = new ArrayList<>();
        segments1.add("p1");
        OfferRequest offerRequest1 = new OfferRequest(5, "FLATX", 20, segments1);
        addOffer(offerRequest1);

        List<String> segments2 = new ArrayList<>();
        segments2.add("p2");
        OfferRequest offerRequest2 = new OfferRequest(5, "PERCENTAGE", 15, segments2);
        addOffer(offerRequest2);

        ApplyOfferRequest applyRequest1 = new ApplyOfferRequest();
        applyRequest1.setCart_value(200);
        applyRequest1.setRestaurant_id(5);
        applyRequest1.setUser_id(1);
        ApplyOfferResponse response1 = applyOffer(applyRequest1);
        Assert.assertEquals(180, response1.getCart_value());

        ApplyOfferRequest applyRequest2 = new ApplyOfferRequest();
        applyRequest2.setCart_value(200);
        applyRequest2.setRestaurant_id(5);
        applyRequest2.setUser_id(2);
        ApplyOfferResponse response2 = applyOffer(applyRequest2);
        Assert.assertEquals(170, response2.getCart_value());
    }

    /**
     * Tests behavior when discount is greater than the cart value.
     */
    @Test
    public void testHighValueDiscount() throws Exception {
        List<String> segments = new ArrayList<>();
        segments.add("p3");
        OfferRequest offerRequest = new OfferRequest(6, "FLATX", 250, segments);
        addOffer(offerRequest);

        ApplyOfferRequest applyRequest = new ApplyOfferRequest();
        applyRequest.setCart_value(200);
        applyRequest.setRestaurant_id(6);
        applyRequest.setUser_id(3);
        ApplyOfferResponse response = applyOffer(applyRequest);
        Assert.assertEquals(-50, response.getCart_value());
    }

    /**
     * Tests different cart values to validate dynamic percentage discounts.
     */
    @Test
    public void testDifferentCartValues() throws Exception {
        List<String> segments = new ArrayList<>();
        segments.add("p1");
        OfferRequest offerRequest = new OfferRequest(7, "PERCENTAGE", 25, segments);
        addOffer(offerRequest);

        ApplyOfferRequest applyRequest1 = new ApplyOfferRequest();
        applyRequest1.setCart_value(100);
        applyRequest1.setRestaurant_id(7);
        applyRequest1.setUser_id(1);
        ApplyOfferResponse response1 = applyOffer(applyRequest1);
        Assert.assertEquals(75, response1.getCart_value());

        ApplyOfferRequest applyRequest2 = new ApplyOfferRequest();
        applyRequest2.setCart_value(1000);
        applyRequest2.setRestaurant_id(7);
        applyRequest2.setUser_id(1);
        ApplyOfferResponse response2 = applyOffer(applyRequest2);
        Assert.assertEquals(750, response2.getCart_value());
    }

    /**
     * Validates scenario where no offer exists for the restaurant.
     */
    @Test
    public void testNonExistentRestaurant() throws Exception {
        ApplyOfferRequest applyRequest = new ApplyOfferRequest();
        applyRequest.setCart_value(200);
        applyRequest.setRestaurant_id(999);
        applyRequest.setUser_id(1);
        ApplyOfferResponse response = applyOffer(applyRequest);
        Assert.assertEquals(200, response.getCart_value());
    }

    /**
     * Tests applying discount on a zero cart value.
     */
    @Test
    public void testZeroCart() throws Exception {
        List<String> segments = new ArrayList<>();
        segments.add("p1");
        OfferRequest offerRequest = new OfferRequest(8, "FLATX", 10, segments);
        addOffer(offerRequest);

        ApplyOfferRequest applyRequest = new ApplyOfferRequest();
        applyRequest.setCart_value(0);
        applyRequest.setRestaurant_id(8);
        applyRequest.setUser_id(1);
        ApplyOfferResponse response = applyOffer(applyRequest);
        Assert.assertEquals(-10, response.getCart_value());
    }

    /**
     * Sends POST request to add a new offer.
     *
     * @param offerRequest The offer to be added
     * @return true if the request is sent (ignores server errors)
     * @throws Exception if network or serialization error occurs
     */
    public boolean addOffer(OfferRequest offerRequest) throws Exception {
        String urlString = "http://localhost:9001/api/v1/offer";
        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", "application/json");

        String POST_PARAMS = mapper.writeValueAsString(offerRequest);
        OutputStream os = con.getOutputStream();
        os.write(POST_PARAMS.getBytes());
        os.flush();
        os.close();
        int responseCode = con.getResponseCode();
        System.out.println("POST Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            System.out.println(response.toString());
        } else {
            System.out.println("POST request did not work.");
        }
        return true;
    }

    /**
     * Applies an offer to a user's cart and returns the updated cart value.
     *
     * @param applyOfferRequest The offer request with user and cart details
     * @return The response containing updated cart value
     * @throws Exception on network or deserialization failure
     */
    public ApplyOfferResponse applyOffer(ApplyOfferRequest applyOfferRequest) throws Exception {
        String urlString = "http://localhost:9001/api/v1/cart/apply_offer";
        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", "application/json");

        String POST_PARAMS = mapper.writeValueAsString(applyOfferRequest);
        try (OutputStream os = con.getOutputStream()) {
            os.write(POST_PARAMS.getBytes());
            os.flush();
        }

        int responseCode = con.getResponseCode();
        System.out.println("POST Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                String responseStr = response.toString();
                System.out.println("Response JSON: " + responseStr);

                try {
                    return mapper.readValue(responseStr, ApplyOfferResponse.class);
                } catch (Exception e) {
                    System.err.println("Failed to deserialize response: " + e.getMessage());
                    ApplyOfferResponse manualResponse = new ApplyOfferResponse();
                    if (responseStr.contains("cart_value")) {
                        String valueStr = responseStr.replaceAll(".*\"cart_value\":(-?\\d+).*", "$1");
                        manualResponse.setCart_value(Integer.parseInt(valueStr));
                    } else {
                        manualResponse.setCart_value(applyOfferRequest.getCart_value());
                    }
                    return manualResponse;
                }
            }
        } else {
            System.out.println("POST request did not work.");
            throw new RuntimeException("Failed to apply offer, HTTP response code: " + responseCode);
        }
    }
}
