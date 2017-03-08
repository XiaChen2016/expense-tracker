package com.tracker.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionScopes;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.Vertex;
import com.google.common.collect.ImmutableList;
import com.tracker.domain.pictures.DetailBox;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

@Service
public class VisionService {
	private GoogleCredential credentials;
	private Vision vision;
	private int MAX_RESULTS = 10;

	@PostConstruct
	public void init() throws GeneralSecurityException, IOException {
		credentials = GoogleCredential.fromStream(new FileInputStream("../ExpenseTracker/ExpenseTracker-579b2651e126.json"))
				.createScoped(VisionScopes.all());
		JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
		vision = new Vision.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, credentials)
				.setApplicationName("ExpenseTracker").build();
	}

	public BatchAnnotateImagesResponse detect( byte[] data ) throws Exception {
		// The path to the image file to annotate
//		String fileName = "/Users/hunt/temp/receipt.jpg";

		// Reads the image file into memory
//		Path path = Paths.get(fileName);
//		byte[] data = Files.readAllBytes(path);

		// Builds the image annotation request
		List<AnnotateImageRequest> requests = new ArrayList<>();
		AnnotateImageRequest request = new AnnotateImageRequest().setImage(new Image().encodeContent(data))
				.setFeatures(ImmutableList.of(new Feature().setType("TEXT_DETECTION").setMaxResults(MAX_RESULTS)));
		requests.add(request);

		Vision.Images.Annotate annotate = vision.images()
				.annotate(new BatchAnnotateImagesRequest().setRequests(requests));

		// Due to a bug: requests to Vision API containing large images fail
		// when GZipped.
		annotate.setDisableGZipContent(true);
		BatchAnnotateImagesResponse batchResponse = annotate.execute();
		List<String> result = new ArrayList<>();
		for (AnnotateImageResponse response : batchResponse.getResponses()) {
			for (EntityAnnotation text : response.getTextAnnotations()) {
				result.add(text.getDescription() + " at " + text.getBoundingPoly());
			}
		}

		// return result; -- you could return a processed version of the
		// batchresponse
		return batchResponse;
	}
	
	public List<DetailBox> detect1( byte[] data ) throws Exception {
		// Builds the image annotation request
		List<AnnotateImageRequest> requests = new ArrayList<>();
		AnnotateImageRequest request = new AnnotateImageRequest().setImage(new Image().encodeContent(data))
				.setFeatures(ImmutableList.of(new Feature().setType("TEXT_DETECTION").setMaxResults(MAX_RESULTS)));
		requests.add(request);

		Vision.Images.Annotate annotate = vision.images()
				.annotate(new BatchAnnotateImagesRequest().setRequests(requests));

		// Due to a bug: requests to Vision API containing large images fail
		// when GZipped.
		List<DetailBox> result = new ArrayList<DetailBox>();
		
		annotate.setDisableGZipContent(true);
		BatchAnnotateImagesResponse batchResponse = annotate.execute();
		for (AnnotateImageResponse response : batchResponse.getResponses()) {
			for (EntityAnnotation text : response.getTextAnnotations()) {
				DetailBox unit = new DetailBox();
				 unit.setDescription( text.getDescription() );
				 int[][] vertices = new int[4][2];
				 List<Vertex> vertex = text.getBoundingPoly().getVertices();
				 for( int i=0; i < vertices.length; i++ ){
						 vertices[i][0] = vertex.get(i).getX();
						 vertices[i][1] = vertex.get(i).getY();
				 }
				 unit.setVertices(vertices);
				 result.add(unit);
			}
		}
		return result;
	}
}
