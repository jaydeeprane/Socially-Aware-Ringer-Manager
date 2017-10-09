import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class RingerManager {

	static ArrayList<Neighbours> NeighbourList = new ArrayList<Neighbours>();
	static ArrayList<Feedbacks> FeedbackList = new ArrayList<Feedbacks>();
	static ArrayList<PlaceDetailsFeedback> PlaceCallDetailsList = new ArrayList<PlaceDetailsFeedback>();
	public static HashMap<Integer, String> hmap = new HashMap<Integer, String>();
	public static HashMap<String, String> rmap = new HashMap<String, String>();
	public static HashMap<Integer, String> NormsLearned = new HashMap<Integer, String>();
	public static int NormsLearnedCounter = 0;
	public static JSONObject Neighbour;
	public static String ringMode;
	public static String callreason;
	public static String overallExpectedMode;
	public static int flag;
	public static String jsonFeedback;
	public static String urgentRationale;
	public static String normalRationale;
	static ArrayList<String> places = new ArrayList<String>();
	static String F1;
	static String F2;

	public static void main(String args[])
			throws ClientProtocolException, IOException, InterruptedException, ParseException {

		// Adding pre-defined places
		places.add("hunt");
		places.add("eb2");
		places.add("carmichael");
		places.add("oval");
		places.add("seminar");
		places.add("lab");
		places.add("meeting");
		places.add("party");

		// Defining relations
		hmap.put(1001, "Family");
		hmap.put(1002, "Friend");
		hmap.put(1003, "Colleague");
		hmap.put(1004, "Stranger");
		hmap.put(1005, "Family");
		hmap.put(1006, "Friend");
		hmap.put(1007, "Colleague");
		hmap.put(1008, "Stranger");
		hmap.put(1009, "Family");
		hmap.put(1010, "Friend");
		hmap.put(1011, "Colleague");
		hmap.put(1012, "Stranger");
		hmap.put(1013, "Family");
		hmap.put(1014, "Friend");
		hmap.put(1015, "Colleague");

		// Initial values of ring modes

		// Case: Family
		rmap.put("fcl", "loud");// fcl = caller is "f"amily,reason is "c"asual,
								// ring mode returned is "l"oud

		rmap.put("fcs", "silent");// fcs = caller is "f"amily,reason is
									// "c"asual,ring mode returned is "s"ilent

		rmap.put("fcv", "vibrate");// fcv = caller is "f"amily,reason is
									// "c"asual,ring mode returned is "v"ibrate

		rmap.put("fn", "vibrate");// fn = caller is "f"amily,reason is
									// "n"one,ring mode returned is "v"ibrate

		// Case: Friend
		rmap.put("frcl", "loud");// frcl = caller is "fr"iend,reason is
									// "c"asual,returned ring mode is "l"oud

		rmap.put("frcs", "silent");// frcs = caller is "fr"iend,reason is
									// "c"asual,returned ring mode is "s"ilent

		rmap.put("frcv", "vibrate");// frcv = caller is "fr"iend,reason is
									// "c"asual,returned ring mode is "v"ibrate

		rmap.put("frn", "silent");// frn = caller is "fr"iend,reason is
									// "n"one,returned ring mode is "s"ilent

		// Case: Colleague
		rmap.put("ccl", "vibrate");// ccl = caller is "c"olleague,reason is
									// "c"asual,overall expected ring mode is
									// "l"oud, returned ring-mode is vibrate

		rmap.put("ccs", "silent");// ccs = caller is "c"olleague,reason is
									// "c"asual,returned ring mode is "s"ilent

		rmap.put("ccv", "vibrate");// ccv = caller is "c"olleague,reason is
									// "c"asual,returned ring mode is "v"ibrate

		rmap.put("cn", "silent");// cn = caller is "c"olleague, reason is
									// "n"one, returned ring mode is "s"ilent

		// Case: Stranger
		rmap.put("st", "silent");// caller is "st"ranger, returned ring mode is
									// "s"ilent

		Scanner sc = new Scanner(System.in);

		System.out.println("Enter the name of the place or enter 'exit' to terminate program: ");
		String placeName = sc.next();

		while (!(placeName.equalsIgnoreCase("exit"))) {
			System.out.println("Please enter your Ring-Mode for this place: ");
			String myRingMode = sc.next();
			System.out.println("Please enter your expected Ring-Mode for this place: ");
			String myExpectedMode = sc.next();
			// Entering place, Requesting Neighbors, returning response and
			// exiting place 5 times because parameters of place keep changing.
			// This also tests robustness of adapt feature of RMA
			for (int k = 0; k < 6; k++) {
				System.out.println();
				NeighbourList.clear();
				FeedbackList.clear();
				PlaceCallDetailsList.clear();
				flag = 0;

				// Enter place
				HttpPost request1 = new HttpPost(
						"http://yangtze.csc.ncsu.edu:9090/csc555sd/services.jsp?action=enterPlace&place=" + placeName
								+ "&userId=5041&myMode=" + myRingMode + "&expectedMode=" + myExpectedMode);

				HttpClient client1 = new DefaultHttpClient();
				HttpResponse result1 = client1.execute(request1);
				String json1 = EntityUtils.toString(result1.getEntity(), "UTF-8");
				JSONParser parserEnter = new JSONParser();
				JSONObject jsonEN = (JSONObject) parserEnter.parse(json1);
				String placeDetails = (String) jsonEN.get("error");

				// if the place exists in the given list, then proceed, else
				// "Place does not exist." error will be thrown
				if (placeDetails == null) {

					System.out.println("The name of the place is: " + placeName);

					// get Neighbour List
					HttpGet request2 = new HttpGet(
							"http://yangtze.csc.ncsu.edu:9090/csc555sd/services.jsp?action=getNeighbors&userId=5041");
					request2.addHeader("content-type", "application/json");
					HttpClient client2 = new DefaultHttpClient();
					HttpResponse result2 = client2.execute(request2);
					String json2 = EntityUtils.toString(result2.getEntity(), "UTF-8");
					// System.out.println("Your neighbours are "+json2);

					JSONParser parser = new JSONParser();
					JSONObject jsonNC = (JSONObject) parser.parse(json2);
					// System.out.println(json2);
					JSONArray neighbourDetails = (JSONArray) jsonNC.get("user");

					int noiseLevel = ((Long) jsonNC.get("noiselevel")).intValue();
					System.out.println("The noise level here is: " + noiseLevel);

					int familyMemberNeighbour = 0;
					int friendNeighbour = 0;
					int colleagueNeighbour = 0;
					int strangerNeighbour = 0;

					for (int i = 0; i < neighbourDetails.size(); i++) {
						Neighbours current = new Neighbours();
						current.neighbourID = Integer
								.valueOf((String) ((JSONObject) neighbourDetails.get(i)).get("id"));
						current.neighbourName = (String) ((JSONObject) neighbourDetails.get(i)).get("name");
						current.relationship = Integer
								.valueOf((String) ((JSONObject) neighbourDetails.get(i)).get("relationship"));
						current.ringerMode = (String) ((JSONObject) neighbourDetails.get(i)).get("ringer-mode");
						current.expectedMode = (String) ((JSONObject) neighbourDetails.get(i)).get("expected");

						if (current.relationship == 1)
							familyMemberNeighbour++;

						if (current.relationship == 2)
							friendNeighbour++;

						if (current.relationship == 3)
							colleagueNeighbour++;

						if (current.relationship == 4)
							strangerNeighbour++;

						NeighbourList.add(current);
					}

					int countsilent = 0;
					int countvibrate = 0;
					int countloud = 0;

					for (int i = 0; i < neighbourDetails.size(); i++) {

						if (NeighbourList.get(i).expectedMode.equals("Silent"))
							countsilent++;
						if (NeighbourList.get(i).expectedMode.equals("Vibrate"))
							countvibrate++;
						if (NeighbourList.get(i).expectedMode.equals("Loud"))
							countloud++;

					}

					int finalvalue = Math.max(countsilent, Math.max(countloud, countvibrate));

					if (finalvalue == countloud) {
						overallExpectedMode = "loud";
					}
					if (finalvalue == countsilent) {
						overallExpectedMode = "silent";
					}
					if (finalvalue == countvibrate) {
						overallExpectedMode = "vibrate";
					}

					if ((countvibrate == countloud) && noiseLevel >= 8)
						overallExpectedMode = "loud";// in case of a tie between
					// loud and vibrate expected mode, if the noise level is
					// greater than
					// 8, then loud will be given preference over vibrate

					System.out.println("Overall Expected Mode is: " + overallExpectedMode);

					socialbenefit();
					Thread.sleep(500);
					//Getting calls in the current place

					HttpGet requestPlaceLog = new HttpGet(
							"http://yangtze.csc.ncsu.edu:9090/csc555sd/services.jsp?action=getCallsInCurrentPlace&userId=5041");
					requestPlaceLog.addHeader("content-type", "application/json");
					HttpClient clientPlaceLog = new DefaultHttpClient();
					HttpResponse resultPlaceLog = clientPlaceLog.execute(requestPlaceLog);
					String jsonPlaceLog = EntityUtils.toString(resultPlaceLog.getEntity(), "UTF-8");
					System.out.println("Calls in Place are " + jsonPlaceLog);

					JSONParser parser3 = new JSONParser();
					JSONObject jsonPC = (JSONObject) parser3.parse(jsonPlaceLog);
					// System.out.println(jsonPlaceLog);
					JSONArray PlaceCallDetails = (JSONArray) jsonPC.get("calls");
					String[] tempSplit = new String[2];// to store only the
														// ArgsInFav and not
														// ArgsInOpp

					for (int i = 0; i < PlaceCallDetails.size(); i++) {
						PlaceDetailsFeedback current = new PlaceDetailsFeedback();
						current.ringermode = (String) ((JSONObject) PlaceCallDetails.get(i)).get("ringermode");
						String s = (String) ((JSONObject) PlaceCallDetails.get(i)).get("rationale");
						current.reason = (String) ((JSONObject) PlaceCallDetails.get(i)).get("reason");
						current.callid = Integer.valueOf((String) ((JSONObject) PlaceCallDetails.get(i)).get("callId"));
						// System.out.println("The rationale is : "+s+" and the
						// length of rationale is "+s.length());

						if (s.length() > 9) {
							// Eliminating part of Argument starting from
							// ArgsInOpp
							if (s.contains("ArgInOpp")) {
								tempSplit = s.split("ArgInOpp");
								current.rationale = tempSplit[0].substring(9, tempSplit[0].length() - 3);
							} else
								current.rationale = s.substring(9, s.length() - 1);
							// Only considering ArgInFav and trimming
							// unnecessary
							// parts of string like "++" and "ArgInFav" and ")"
							// in the end

						}

						PlaceCallDetailsList.add(current);

					}

					for (int i = 0; i < PlaceCallDetails.size(); i++) {
						F1 = "";
						F2 = "";
						// The first feedback sent is positive as long as the
						// ring mode is not loud as loud ring mode will be
						// disturbing
						if (PlaceCallDetailsList.get(i).ringermode.equals(overallExpectedMode)
								| (PlaceCallDetailsList.get(i).ringermode.equalsIgnoreCase(myExpectedMode))) {

							F1 = "positive";
						}
						// If the reason of the call is urgent, then return
						// positive
						else if (PlaceCallDetailsList.get(i).reason.equalsIgnoreCase("urgent")
								&& PlaceCallDetailsList.get(i).ringermode.equalsIgnoreCase("Loud")) {
							F1 = "positive";
						}

						else
							F1 = "negative";

						System.out.println(
								"The value of F1 for callId: " + PlaceCallDetailsList.get(i).callid + " is " + F1);
						// Following are the cases to be considered for sending
						// updatedFeedback

						// if F1 is negative then consider the following cases
						if (F1.equals("negative") && !(PlaceCallDetailsList.get(i).rationale == null)) {

							// If the incoming calls are from caller_relationip
							// 1
							if ((PlaceCallDetailsList.get(i).rationale.contains("caller_relationship-IS-1")))
								F2 = "positive";

							// If the incoming calls are from caller_relationip
							// 2
							if (PlaceCallDetailsList.get(i).rationale.contains("caller_relationship-IS-2"))
								F2 = "neutral";
							
							// if incoming call is from a colleague and the
							// ringer mode of callee is silent/vibrate then
							// positive feedback
							if ((overallExpectedMode.equalsIgnoreCase("loud") | myExpectedMode.equalsIgnoreCase("loud"))
									&& !(PlaceCallDetailsList.get(i).ringermode.equals("loud"))
									&& PlaceCallDetailsList.get(i).rationale.contains("caller_relationship-IS-3")) {
								F2 = "positive";
							}
							// If the incoming calls are from caller_relationip
							// 4
							if (PlaceCallDetailsList.get(i).rationale.contains("caller_relationship-IS-4"))
								F2 = "positive";

							// if noise level is 10 and ring-mode was loud
							else if (((PlaceCallDetailsList.get(i).rationale.contains("noise-IS-10"))
									&& (PlaceCallDetailsList.get(i).ringermode.equals("loud"))))
								F2 = "positive";

							// if majority of the neighbors are relationship 1
							// and ring mode returned was silent
							else if (((PlaceCallDetailsList.get(i).rationale
									.contains("Majority(neighbor_relationship)-IS-1"))
									&& (PlaceCallDetailsList.get(i).ringermode.equals("silent"))))
								F2 = "positive";
							else
								F2 = "negative";
						}
						// If the person is ignoring calls from family under any
						// situation, then send negative feedback
						if (F1.equalsIgnoreCase("positive")
								&& PlaceCallDetailsList.get(i).rationale.contains("ringermode-IS-SILENT")
								&& PlaceCallDetailsList.get(i).rationale.contains("caller_relationship-IS-1"))
							F2 = "negative";
						if (!(F2.equals("")))
							System.out.println(
									"The value of F2 for callId: " + PlaceCallDetailsList.get(i).callid + " is " + F2);

						// returning F1 and F2 for each of the calls in place
						HttpPost sendFeedback = new HttpPost(
								"http://yangtze.csc.ncsu.edu:9090/csc555sd/services.jsp?action=giveFeedback&callId="
										+ PlaceCallDetailsList.get(i).callid + "&userId=5041&feedback=" + F1
										+ "&feedbackUpdated=" + F2);
						HttpClient clientFeedback = new DefaultHttpClient();
						HttpResponse resultFeedback = clientFeedback.execute(sendFeedback);

					}

					// Now, adding the new norms learned in this place

					// We will be adding only those norms that have been
					// verified above, i.e. if F1 or F2 positive that means the
					// rationale is verified

					// This learning will be done by adding those rationales
					// that consider any other extra parameter apart from the
					// ones that i considered while returning ring modes
					if ((F1.equalsIgnoreCase("positive") && !(F2.equalsIgnoreCase("negative")))
							| F2.equalsIgnoreCase("positive")) {
						for (int j = 0; j < PlaceCallDetailsList.size(); j++) {
							if ((PlaceCallDetailsList.get(j).rationale.contains("expected_mode"))
									&& (PlaceCallDetailsList.get(j).rationale.contains("call_reason")))
								if ((PlaceCallDetailsList.get(j).rationale.contains("neighbour_relationship"))
										| (PlaceCallDetailsList.get(j).rationale.contains("noise"))) {
									NormsLearned.put(NormsLearnedCounter, PlaceCallDetailsList.get(j).rationale);
									System.out.println(
											"The new norm learned is : " + PlaceCallDetailsList.get(j).rationale);
									NormsLearnedCounter++;
								}

						}
					}
					//Exiting the place
					HttpPost requestexit = new HttpPost(
							"http://yangtze.csc.ncsu.edu:9090/csc555sd/services.jsp?action=exitPlace&userId=5041");
					HttpClient clientexit = new DefaultHttpClient();
					HttpResponse resultexit = clientexit.execute(requestexit);

					System.out.println(
							"----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
					// partition between different RequestCall and Response
					// events

				}

				else {
					System.out.println("Place does not exist/Invalid Ring-mode or Expected Mode entered.");
					break;
				}

			}
			System.out.println("Enter the name of the place or enter 'exit' to terminate program: ");
			placeName = sc.next();
		}

	}

	private static void socialbenefit() throws ClientProtocolException, IOException, ParseException {

		// RequestCall
		HttpGet request3 = new HttpGet(
				"http://yangtze.csc.ncsu.edu:9090/csc555sd/services.jsp?action=requestCall&userId=5041");
		request3.addHeader("content-type", "application/json");
		HttpClient client3 = new DefaultHttpClient();
		HttpResponse result3 = client3.execute(request3);

		String json3 = EntityUtils.toString(result3.getEntity(), "UTF-8");
		JSONParser parser1 = new JSONParser();
		JSONObject jsonRC = (JSONObject) parser1.parse(json3);

		System.out.println("Details of the call Received : " + json3);
		Caller requestedCall = new Caller();
		int callid = (Integer.parseInt((String) jsonRC.get("callId")));
		int callerid = (Integer.parseInt((String) jsonRC.get("callerId")));
		String callername = (String) jsonRC.get("callerName");
		callreason = (String) jsonRC.get("reason");

		String relation = "Stranger"; // If the caller does not exist in the
										// pre-defined list, then assuming the
										// caller to be a stranger
		if (hmap.containsKey(callerid))
			relation = hmap.get(callerid);

		if (callreason.equals("urgent")) {
			ringMode = "Loud";
			System.out.println("The returned ring mode is: " + ringMode);
			urgentRationale = "ArgInFav(ringermode-IS-LOUD+WHEN+call_reason-IS-URGENT)";
			// Returning computed ring-mode and capturing feedback
			HttpPost requestresponse = new HttpPost(
					"http://yangtze.csc.ncsu.edu:9090/csc555sd/services.jsp?action=responseCall&callId=" + callid
							+ "&ringerMode=" + ringMode + "&rationale=" + urgentRationale);
			HttpClient client33 = new DefaultHttpClient();
			HttpResponse result33 = client33.execute(requestresponse);
			@SuppressWarnings("deprecation")

			HttpGet requestresponsefeedback = new HttpGet(
					"http://yangtze.csc.ncsu.edu:9090/csc555sd/services.jsp?action=listFeedbacks&callId=" + callid);
			requestresponsefeedback.addHeader("content-type", "application/json");
			HttpClient clientresponse = new DefaultHttpClient();
			HttpResponse resultresponse = clientresponse.execute(requestresponsefeedback);
			jsonFeedback = EntityUtils.toString(resultresponse.getEntity(), "UTF-8");
			System.out.println("The feedback received is: ");
			System.out.println(jsonFeedback);

		}

		else {
			System.out.println("The returned ring mode is: " + assignRingMode(relation));

			System.out.println("Flag value is: " + flag);

			// Capturing feedback
			HttpPost requestresponse = new HttpPost(
					"http://yangtze.csc.ncsu.edu:9090/csc555sd/services.jsp?action=responseCall&callId=" + callid
							+ "&ringerMode=" + assignRingMode(relation) + "&rationale=" + normalRationale);
			@SuppressWarnings("deprecation")
			HttpClient client33 = new DefaultHttpClient();
			HttpResponse result33 = client33.execute(requestresponse);

			HttpGet requestresponsefeedback = new HttpGet(
					"http://yangtze.csc.ncsu.edu:9090/csc555sd/services.jsp?action=listFeedbacks&callId=" + callid);
			requestresponsefeedback.addHeader("content-type", "application/json");
			HttpClient clientresponse = new DefaultHttpClient();
			HttpResponse resultresponse = clientresponse.execute(requestresponsefeedback);
			jsonFeedback = EntityUtils.toString(resultresponse.getEntity(), "UTF-8");
			System.out.println("The feedback received is: ");
			System.out.println(jsonFeedback);
		}

		JSONParser parserRes = new JSONParser();
		JSONObject jsonResC = (JSONObject) parserRes.parse(jsonFeedback);
		JSONArray FeedbackArray = (JSONArray) jsonResC.get("feedbacks");

		int positiveUpdatedFeedbackCount = 0;

		for (int l = 0; l < FeedbackArray.size(); l++) {
			Feedbacks currentFeedback = new Feedbacks();
			currentFeedback.id = Integer.valueOf((String) ((JSONObject) FeedbackArray.get(l)).get("id"));
			currentFeedback.feedback = (String) ((JSONObject) FeedbackArray.get(l)).get("feedback");
			currentFeedback.feedbackUpdated = (String) ((JSONObject) FeedbackArray.get(l)).get("feedbackUpdated");

			// if initial feedback was negative and updated feedback was
			// positive

			if (currentFeedback.feedback.equals("negative") && currentFeedback.feedbackUpdated.equals("positive"))
				positiveUpdatedFeedbackCount++;

			FeedbackList.add(currentFeedback);
		}

		boolean callerNegativefeedback = false;
		int positiveFeedbackCount = 0;
		int negativeFeedbackCount = 0;

		for (int k = 0; k < FeedbackArray.size(); k++) {

			if (FeedbackList.get(k).id == callerid && FeedbackList.get(k).feedback.equals("negative")) {
				callerNegativefeedback = true;
				System.out.println("Feedback of the Caller is negative");
				continue;
			}
			if (FeedbackList.get(k).id == callerid && FeedbackList.get(k).feedback.equals("positive")) {
				System.out.println("Feedback of the Caller is positive");
				continue;
			}
			if (FeedbackList.get(k).id == callerid && FeedbackList.get(k).feedback.equals("neutral")) {
				System.out.println("Feedback of the Caller is neutral");
				continue;
			}
			if (FeedbackList.get(k).feedback.equals("positive")) {

				positiveFeedbackCount++;
			}

			if (FeedbackList.get(k).feedback.equals("negative")) {

				negativeFeedbackCount++;
			}
		}
		negativeFeedbackCount -= positiveUpdatedFeedbackCount;
		System.out.println("Positive feedback count of Neighbours: " + positiveFeedbackCount);
		System.out
				.println("Negative feedback count of Neighbours after subtracting count of updated positive feedbacks: "
						+ negativeFeedbackCount);

		// adapting according to feedback

		// if flag value is not 0,only then implement following steps because
		// urgent reason is given priority
		// if caller reason was none and majority neighbor feedback was
		// negative then update the returned ringMode to one level lower
		if ((callreason.equals("none") | callreason.equals("casual"))
				&& (negativeFeedbackCount > positiveFeedbackCount)) {
			System.out.println("Updating Ring Mode to one level lower based on negative neighbours' feedback");
			adapt(flag, "lower");
		}

		// if caller reason was casual and majority neighbor feedback was
		// positive and caller feedback was negative
		// update the returned ringMode to one level higher
		if (callreason.equals("casual") && (negativeFeedbackCount <= positiveFeedbackCount) && callerNegativefeedback) {
			System.out.println("Updating Ring Mode to one level higher based on negative caller feedback");
			adapt(flag, "higher");
		}

	}

	private static void adapt(int i, String change) {
		switch (i) {
		case 1:
			if (change.equals("lower")) {
				if (!(rmap.get("fcl").equals("silent"))) {
					if (rmap.get("fcl").equals("loud"))
						rmap.put("fcl", "vibrate");
					else
						rmap.put("fcl", "silent");
				}
			}
			if (change.equals("higher")) {
				if (!(rmap.get("fcl").equals("loud"))) {
					if (rmap.get("fcl").equals("silent"))
						rmap.put("fcl", "vibrate");
					else
						rmap.put("fcl", "loud");
				}
			}
		case 2:
			if (change.equals("lower")) {
				if (!(rmap.get("fcs").equals("silent"))) {
					if (rmap.get("fcs").equals("loud"))
						rmap.put("fcs", "vibrate");
					else
						rmap.put("fcs", "silent");

				}
			}
			if (change.equals("higher")) {
				if (!(rmap.get("fcs").equals("loud"))) {
					if (rmap.get("fcs").equals("silent"))
						rmap.put("fcs", "vibrate");
					else
						rmap.put("fcs", "loud");
				}
			}
		case 3:
			if (change.equals("lower")) {
				if (!(rmap.get("fcv").equals("silent"))) {
					if (rmap.get("fcv").equals("loud"))
						rmap.put("fcv", "vibrate");
					else
						rmap.put("fcv", "silent");
				}
			}
			if (change.equals("higher")) {
				if (!(rmap.get("fcv").equals("loud"))) {
					if (rmap.get("fcv").equals("silent"))
						rmap.put("fcv", "vibrate");
					else
						rmap.put("fcv", "loud");
				}
			}
		case 4:
			if (change.equals("lower")) {
				if (!(rmap.get("fn").equals("silent"))) {
					if (rmap.get("fn").equals("loud"))
						rmap.put("fn", "vibrate");
					else
						rmap.put("fn", "silent");
				}
			}
			if (change.equals("higher")) {
				if (!(rmap.get("fn").equals("loud"))) {
					if (rmap.get("fn").equals("silent"))
						rmap.put("fn", "vibrate");
					else
						rmap.put("fn", "loud");
				}
			}
		case 5:
			if (change.equals("lower")) {
				if (!(rmap.get("frcl").equals("silent"))) {
					if (rmap.get("frcl").equals("loud"))
						rmap.put("frcl", "vibrate");
					else
						rmap.put("frcl", "silent");
				}
			}
			if (change.equals("higher")) {
				if (!(rmap.get("frcl").equals("loud"))) {
					if (rmap.get("frcl").equals("silent"))
						rmap.put("frcl", "vibrate");
					else
						rmap.put("frcl", "loud");
				}
			}
		case 6:
			if (change.equals("lower")) {
				if (!(rmap.get("frcs").equals("silent"))) {
					if (rmap.get("frcs").equals("loud"))
						rmap.put("frcs", "vibrate");
					else
						rmap.put("frcs", "silent");
				}
			}
			if (change.equals("higher")) {
				if (!(rmap.get("frcs").equals("loud"))) {
					if (rmap.get("frcs").equals("silent"))
						rmap.put("frcs", "vibrate");
					else
						rmap.put("frcs", "loud");
				}
			}
		case 7:
			if (change.equals("lower")) {
				if (!(rmap.get("frcv").equals("silent"))) {
					if (rmap.get("frcv").equals("loud"))
						rmap.put("frcv", "vibrate");
					else
						rmap.put("frcv", "silent");
				}
			}
			if (change.equals("higher")) {
				if (!(rmap.get("frcv").equals("loud"))) {
					if (rmap.get("frcv").equals("silent"))
						rmap.put("frcv", "vibrate");
					else
						rmap.put("frcv", "loud");
				}
			}
		case 8:
			if (change.equals("lower")) {
				if (!(rmap.get("frn").equals("silent"))) {
					if (rmap.get("frn").equals("loud"))
						rmap.put("frn", "vibrate");
					else
						rmap.put("frn", "silent");
				}
			}
			if (change.equals("higher")) {
				if (!(rmap.get("frn").equals("loud"))) {
					if (rmap.get("frn").equals("silent"))
						rmap.put("frn", "vibrate");
					else
						rmap.put("frn", "loud");
				}
			}
		case 9:
			if (change.equals("lower")) {
				if (!(rmap.get("ccl").equals("silent"))) {
					if (rmap.get("ccl").equals("loud"))
						rmap.put("ccl", "vibrate");
					else
						rmap.put("ccl", "silent");
				}
			}
			if (change.equals("higher")) {
				if (!(rmap.get("ccl").equals("loud"))) {
					if (rmap.get("ccl").equals("silent"))
						rmap.put("ccl", "vibrate");
					else
						rmap.put("ccl", "loud");
				}
			}
		case 10:
			if (change.equals("lower")) {
				if (!(rmap.get("ccs").equals("silent"))) {
					if (rmap.get("ccs").equals("loud"))
						rmap.put("ccs", "vibrate");
					else
						rmap.put("ccs", "silent");
				}
			}
			if (change.equals("higher")) {
				if (!(rmap.get("ccs").equals("loud"))) {
					if (rmap.get("ccs").equals("silent"))
						rmap.put("ccs", "vibrate");
					else
						rmap.put("ccs", "loud");
				}
			}
		case 11:
			if (change.equals("lower")) {
				if (!(rmap.get("ccv").equals("silent"))) {
					if (rmap.get("ccv").equals("loud"))
						rmap.put("ccv", "vibrate");
					else
						rmap.put("ccv", "silent");
				}
			}
			if (change.equals("higher")) {
				if (!(rmap.get("ccv").equals("loud"))) {
					if (rmap.get("ccv").equals("silent"))
						rmap.put("ccv", "vibrate");
					else
						rmap.put("ccv", "loud");
				}
			}
		case 12:
			if (change.equals("lower")) {
				if (!(rmap.get("cn").equals("silent"))) {
					if (rmap.get("cn").equals("loud"))
						rmap.put("cn", "vibrate");
					else
						rmap.put("cn", "silent");
				}
			}
			if (change.equals("higher")) {
				if (!(rmap.get("cn").equals("loud"))) {
					if (rmap.get("cn").equals("silent"))
						rmap.put("cn", "vibrate");
					else
						rmap.put("cn", "loud");
				}
			}
		case 13:
			if (change.equals("lower")) {
				if (!(rmap.get("st").equals("silent"))) {
					if (rmap.get("st").equals("loud"))
						rmap.put("st", "vibrate");
					else
						rmap.put("st", "silent");
				}
			}
			if (change.equals("higher")) {
				if (!(rmap.get("st").equals("loud"))) {
					if (rmap.get("st").equals("silent"))
						rmap.put("st", "vibrate");
					else
						rmap.put("st", "loud");
				}
			}
		}

	}

	private static String assignRingMode(String relation) {
		switch (relation) {
		case "Family":
			if (callreason.equals("casual")) {
				if (overallExpectedMode.equals("loud")) {
					flag = 1;
					normalRationale = "ArgInFav(ringermode-IS-" + rmap.get("fcl").toUpperCase()
							+ "+WHEN+call_reason-IS-casual+AND+caller_relationship-IS-1+AND+Majority(expected_mode)-IS-LOUD)";
					return rmap.get("fcl");
				}
				if (overallExpectedMode.equals("silent")) {
					flag = 2;
					normalRationale = "ArgInFav(ringermode-IS-" + rmap.get("fcs").toUpperCase()
							+ "+WHEN+call_reason-IS-casual+AND+caller_relationship-IS-1+AND+Majority(expected_mode)-IS-SILENT)";
					return rmap.get("fcs");
				}
				if (overallExpectedMode.equals("vibrate")) {
					flag = 3;
					normalRationale = "ArgInFav(ringermode-IS-" + rmap.get("fcv").toUpperCase()
							+ "+WHEN+call_reason-IS-casual+AND+caller_relationship-IS-1+AND+Majority(expected_mode)-IS-VIBRATE)";
					return rmap.get("fcv");
				}

			}
			if (callreason.equals("none")) {
				flag = 4;
				normalRationale = "ArgInFav(ringermode-IS-" + rmap.get("fn").toUpperCase()
						+ "+WHEN+call_reason-IS-none+AND+caller_relationship-IS-1)";
				return rmap.get("fn");
			}

		case "Friend":
			if (callreason.equals("casual")) {
				if (overallExpectedMode.equals("loud")) {
					flag = 5;
					normalRationale = "ArgInFav(ringermode-IS-" + rmap.get("frcl").toUpperCase()
							+ "+WHEN+call_reason-IS-casual+AND+caller_relationship-IS-2+AND+Majority(expected_mode)-IS-LOUD)";
					return rmap.get("frcl");
				}
				if (overallExpectedMode.equals("silent")) {
					flag = 6;
					normalRationale = "ArgInFav(ringermode-IS-" + rmap.get("frcs").toUpperCase()
							+ "+WHEN+call_reason-IS-casual+AND+caller_relationship-IS-2+AND+Majority(expected_mode)-IS-SILENT)";
					return rmap.get("frcs");
				}
				if (overallExpectedMode.equals("vibrate")) {
					flag = 7;
					normalRationale = "ArgInFav(ringermode-IS-" + rmap.get("frcl").toUpperCase()
							+ "+WHEN+call_reason-IS-casual+AND+caller_relationship-IS-2+AND+Majority(expected_mode)-IS-VIBRATE)";
					return rmap.get("frcv");
				}

			}
			if (callreason.equals("none")) {
				flag = 8;
				normalRationale = "ArgInFav(ringermode-IS-" + rmap.get("frn").toUpperCase()
						+ "+WHEN+call_reason-IS-NONE+AND+caller_relationship-IS-2)";
				return rmap.get("frn");
			}

		case "Colleague":
			if (callreason.equals("casual")) {
				if (overallExpectedMode.equals("loud")) {
					flag = 9;
					normalRationale = "ArgInFav(ringermode-IS-" + rmap.get("ccl").toUpperCase()
							+ "+WHEN+call_reason-IS-casual+AND+caller_relationship-IS-3+AND+Majority(expected_mode)-IS-LOUD)";
					return rmap.get("ccl");
				}
				if (overallExpectedMode.equals("silent")) {
					flag = 10;
					normalRationale = "ArgInFav(ringermode-IS-" + rmap.get("ccs").toUpperCase()
							+ "+WHEN+call_reason-IS-casual+AND+caller_relationship-IS-3+AND+Majority(expected_mode)-IS-SILENT)";
					return rmap.get("ccs");
				}
				if (overallExpectedMode.equals("vibrate")) {
					flag = 11;
					normalRationale = "ArgInFav(ringermode-IS-" + rmap.get("ccv").toUpperCase()
							+ "+WHEN+call_reason-IS-casual+AND+caller_relationship-IS-3+AND+Majority(expected_mode)-IS-VIBRATE)";
					return rmap.get("ccv");
				}

			}
			if (callreason.equals("none")) {
				flag = 12;
				normalRationale = "ArgInFav(ringermode-IS-" + rmap.get("cn").toUpperCase()
						+ "+WHEN+call_reason-IS-none+AND+caller_relationship-IS-3)";
				return rmap.get("cn");
			}

		case "Stranger":
			if (callreason.equals("none")) {
				flag = 13;
				normalRationale = "ArgInFav(ringermode-IS-" + rmap.get("st").toUpperCase()
						+ "+WHEN+call_reason-IS-none+AND+caller_relationship-IS-4)";
				return rmap.get("st");
			}
			if (callreason.equals("casual")) {
				flag = 13;
				normalRationale = "ArgInFav(ringermode-IS-" + rmap.get("st").toUpperCase()
						+ "+WHEN+call_reason-IS-casual+AND+caller_relationship-IS-4)";
				return rmap.get("st");
			}

		default:
			return ("");

		}

	}

}
