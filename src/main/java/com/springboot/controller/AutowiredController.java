package com.springboot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.service.Dog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import com.springboot.service.Animal;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class AutowiredController {


	List<OfferRequest> allOffers = new ArrayList<>();

	@PostMapping(path = "/api/v1/offer")
	public ApiResponse postOperation(@RequestBody OfferRequest offerRequest) {
		System.out.println(offerRequest);
		allOffers.add(offerRequest);
		return new ApiResponse("success");
	}
	
	@GetMapping(path = "/api/v1/user_segment")
	public SegmentResponse getOperation(@RequestParam int user_id) {
		
		SegmentResponse segmentResponse = new SegmentResponse();
	    
		if (user_id % 2 == 0) {
	        segmentResponse.setSegment("p2");
	    } else if (user_id % 3 == 0) {
	        segmentResponse.setSegment("p3");
	    } else {
	        segmentResponse.setSegment("p1");
	    }
//	    
	    System.out.println("Returning segment for user_id: " + user_id + ", segment: " + segmentResponse.getSegment());
	    
		return segmentResponse;
	}

	@PostMapping(path = "/api/v1/cart/apply_offer")
	public ApplyOfferResponse applyOffer(@RequestBody ApplyOfferRequest applyOfferRequest) throws Exception {
		System.out.println(applyOfferRequest);
		int cartVal = applyOfferRequest.getCart_value();
		SegmentResponse segmentResponse = getSegmentResponse(applyOfferRequest.getUser_id());
		Optional<OfferRequest> matchRequest = allOffers.stream().filter(x->x.getRestaurant_id()==applyOfferRequest.getRestaurant_id())
				.filter(x->x.getCustomer_segment().contains(segmentResponse.getSegment()))
				.findFirst();

		if(matchRequest.isPresent()){
			System.out.println("got a match");
//			System.out.println(matchRequest.get());
			OfferRequest gotOffer = matchRequest.get();

			if(gotOffer.getOffer_type().equals("FLATX")) {
				cartVal = cartVal - gotOffer.getOffer_value();
			} else {
				cartVal = (int) (cartVal - cartVal * gotOffer.getOffer_value()*(0.01));
				// For percentage discount (assuming offer_value is the percentage)
				//cartVal = (int) (cartVal * (1 - gotOffer.getOffer_value() * 0.01));
			}

		}
		return new ApplyOfferResponse(cartVal);
	}

	private SegmentResponse getSegmentResponse(int userid)
	{
		SegmentResponse segmentResponse = new SegmentResponse();
		try {
			String urlString = "http://localhost:9001/api/v1/user_segment?" + "user_id=" + userid;
			URL url = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();


			connection.setRequestProperty("accept", "application/json");

			// This line makes the request
			InputStream responseStream = connection.getInputStream();

			// Manually converting the response body InputStream to APOD using Jackson
			ObjectMapper mapper = new ObjectMapper();
			segmentResponse = mapper.readValue(responseStream,SegmentResponse.class);
			System.out.println("got segment response" + segmentResponse);


		} catch (Exception e) {
			System.out.println(e);
		}
		return segmentResponse;
	}


}
