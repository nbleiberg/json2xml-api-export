package com.perfectomobile;

import java.io.StringWriter;
import java.util.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.google.gson.*;

/**
 * Transform and output functions for TestSuites XML class.
 * 
 * @author davidre@perfectomobile.com
 */
public class XmlHelper {
	public static String emitTestSuites(TestSuites xmlTests) throws Throwable {
		JAXBContext c = JAXBContext.newInstance(TestSuites.class);
		Marshaller m = c.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		StringWriter sw = new StringWriter();
		m.marshal(xmlTests, sw);
		return sw.toString();
	}

	public static TestSuites convertPerfectoJson(JsonObject executionList) {
		String suiteName = "";
		Long suiteStartTime = 0L;
		Long suiteEndTime = 0L;
		Double suiteDuration = 0.0;
		String jobName = "";
		String jobNumber = "";
		String jobBranch = "";
		int suiteFailures = 0;
		String suiteOwner = "";
		int suiteTestCount = 0;
		String automationFramework = "";

		JsonArray executions = executionList.getAsJsonArray("resources");

		TestSuites testWrapper = new TestSuites();
		List<TestSuites.TestSuite> tests = new ArrayList<TestSuites.TestSuite>();
		TestSuites.TestSuite suite = TestSuites.TestSuite.create("", 0D);
		suiteTestCount = executions.size();
		List<TestSuites.TestCase> testCases = new ArrayList<TestSuites.TestCase>();

		for (JsonElement ex : executions) {
			JsonObject obj = ex.getAsJsonObject();

			// Capture global properties for the suite the first time through.
			if (automationFramework.isEmpty()) {
				automationFramework = obj.get("automationFramework").getAsString();
			}
			if (suiteOwner.isEmpty()) {
				suiteOwner = obj.get("owner").getAsString();
			}
			if (jobName.isEmpty() || jobNumber.isEmpty() || jobBranch.isEmpty()) {
				JsonObject job = obj.getAsJsonObject("job");
				if (jobName.isEmpty()) {
					jobName = job.get("name").getAsString();
				}
				if (jobNumber.isEmpty()) {
					jobNumber = job.get("number").getAsString();
				}
				if (jobBranch.isEmpty()) {
					jobBranch = job.get("branch").getAsString();
				}
			}
			Long start = Long.parseLong(obj.get("startTime").getAsString());
			if (start < suiteStartTime || suiteStartTime == 0) {
				suiteStartTime = start;
			}
			Long end = Long.parseLong(obj.get("endTime").getAsString());
			if (end > suiteEndTime || suiteEndTime == 0) {
				suiteEndTime = end;
			}
			Double duration = (end.doubleValue() - start.doubleValue()) / 1000; // Convert from millis to seconds
			suiteDuration += duration;

			// Setup TestCase and append it.
			String caseName = obj.get("name").getAsString();
			TestSuites.TestCase testCase = TestSuites.TestCase.create("", caseName, duration);
			if (suiteName.isEmpty()) {
				suiteName = obj.get("externalId").getAsString();
			}
			testCase.setTime(duration);
			testCase.property("id", obj.get("id").getAsString());
			testCase.property("startTime", start.toString());
			testCase.property("endTime", end.toString());
			testCase.property("uxDuration", obj.get("uxDuration").getAsString());
			String status = obj.get("status").getAsString();
			if (status.equalsIgnoreCase("failed")) {
				suiteFailures++;
			}
			testCase.property("status", status);
			testCase.property("reportURL", obj.get("reportURL").getAsString());

			/**
			 * There's no logical place to append the following to TestCase:
			 * 
			 * platforms[] tags[] videos[] executionEngine[] artifacts[] customFields[]
			 * parameters[] commands[]
			 */

			testCases.add(testCase);
		}

		// Apply globals to TestSuite.
		suite.setTestCases(testCases);
		suite.setId(suiteName);
		suite.setTime(suiteDuration);
		suite.setTests(suiteTestCount);
		suite.setFailures(suiteFailures);

		TestSuites.Properties props = new TestSuites.Properties();
		props.put("automationFramework", automationFramework);
		props.put("owner", suiteOwner);
		props.put("job-name", jobName);
		props.put("job-number", jobNumber);
		props.put("job-branch", jobBranch);
		props.put("startTime", suiteStartTime.toString());
		props.put("endTime", suiteEndTime.toString());
		suite.setProperties(props);
		tests.add(suite);

		// Apply globals to TestSuites.
		testWrapper.setName(suiteName);
		testWrapper.setTime(suiteDuration);
		testWrapper.setTests(suiteTestCount);
		// It doesn't seem that can we count skips for XCTest/Espresso runs.
		testWrapper.setFailures(suiteFailures);
		testWrapper.setTestSuites(tests);

		return testWrapper;
	}
}
