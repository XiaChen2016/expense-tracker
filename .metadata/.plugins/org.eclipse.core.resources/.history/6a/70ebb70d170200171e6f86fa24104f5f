package com.tracker.api;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import com.google.cloud.vision.spi.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.protobuf.ByteString;

public class Detect {

	 public Detect(ImageAnnotatorClient client) {
		    visionApi = client;
		  }
	 
	private static ImageAnnotatorClient visionApi;
	 
	public static List<EntityAnnotation> detectText( String filePath, PrintStream out ) throws IOException {
		  List<AnnotateImageRequest> requests = new ArrayList<>();

		  ByteString imgBytes = ByteString.readFrom(new FileInputStream(filePath));

		  Image img = Image.newBuilder().setContent(imgBytes).build();
		  Feature feat = Feature.newBuilder().setType(Type.TEXT_DETECTION).build();
		  AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
		      .addFeatures(feat)
		      .setImage(img)
		      .build();
		  requests.add(request);

		  BatchAnnotateImagesResponse response = visionApi.batchAnnotateImages(requests);
		  List<AnnotateImageResponse> responses = response.getResponsesList();

		  List<EntityAnnotation> result = new ArrayList<EntityAnnotation>();
		  for (AnnotateImageResponse res : responses) {
		    if (res.hasError()) {
		      out.printf("Error: %s\n", res.getError().getMessage());
		      return null;
		    }

		    // For full list of available annotations, see http://g.co/cloud/vision/docs
		    for (EntityAnnotation annotation : res.getTextAnnotationsList()) {
		      out.printf("Text: %s\n", annotation.getDescription());
		      out.printf("Position : %s\n", annotation.getBoundingPoly());
		    }
		    result.addAll( res.getTextAnnotationsList() );
		  }
		  return result;
	}
}
