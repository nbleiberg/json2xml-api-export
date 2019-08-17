/**
 * Perfecto Reporting API JSON 2 XML Exporter
 */
package com.perfectomobile;

import org.apache.commons.cli.*;
import com.google.gson.*;
import java.io.*;
import java.text.*;
import java.util.*;
import javax.naming.*;

/**
 * Pull JSON response from Perfecto Reporting API, transform into Junit XML, and
 * write to file.
 * 
 * Built for v1 of
 * https://developers.perfectomobile.com/display/PD/DigitalZoom+Reporting+Public+API
 * 
 * @author davidre@perfectomobile.com
 */
public class Exporter {

	private static String _perfectoCloudReportingServer = "";
	private static String _perfectoSecurityToken = "";
	private static String _testExecutionId = "";
	private static String _xmlOutputFilePath = "./TestSuites.xml";
	private static Options _options = new Options();

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Throwable {
		System.out.println();
		System.out.println(consoleTime() + "Perfecto Reporting API Junit XML Exporter for v1 API");
		System.out.println();
		setupCliOptions();
		applyCommandLineParameters(args);
		validateConfig();
		emitXml(convertJson2Xml(callTargetAPI(_perfectoCloudReportingServer, _perfectoSecurityToken)));
		System.out.println();
	}

	private static void setupCliOptions() {
		_options.addOption("c", "perfectoCloudReportingServer", true,
				"The URL, including protocol, for the target Perfecto Reporting API server.");
		_options.addOption("s", "perfectoSecurityToken", true,
				"The authentication token for access to the Perfecto Reporting API server");
		_options.addOption("e", "testExecutionId", true,
				"The Execution ID of a set of XCTest or Espresso test results to export. Also called Management ID.");
		_options.addOption("x", "xmlOutputFilePath", true, "The target file to write Junit XML to.");
	}

	private static void applyCommandLineParameters(String[] args) throws Throwable {
		System.out.println();
		System.out.println(consoleTime() + "Parsing command line arguments.");
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(_options, args);

		if (cmd.hasOption("c")) {
			_perfectoCloudReportingServer = cmd.getOptionValue("c");
			System.out
					.println(consoleTime() + "  Found perfectoCloudReportingServer: " + _perfectoCloudReportingServer);
		}

		if (cmd.hasOption("s")) {
			_perfectoSecurityToken = cmd.getOptionValue("s");
			System.out.println(consoleTime() + "  Found perfectoSecurityToken: " + _perfectoSecurityToken);
		}

		if (cmd.hasOption("e")) {
			_testExecutionId = cmd.getOptionValue("e");
			System.out.println(consoleTime() + "  Found testExecutionId: " + _testExecutionId);
		}

		if (cmd.hasOption("x")) {
			_xmlOutputFilePath = cmd.getOptionValue("x");
			System.out.println(consoleTime() + "  Found xmlOutputFilePath: " + _xmlOutputFilePath);
		}

		System.out.println(consoleTime() + "Finished parsing command line arguments.");
	}

	private static void validateConfig() throws Throwable {
		if (_perfectoCloudReportingServer.isEmpty()) {
			throw new ConfigurationException("Perfecto Cloud Reporting Server parameter is required.");
		}

		if (_perfectoSecurityToken.isEmpty()) {
			throw new ConfigurationException(
					"Perfecto Security Token parameter is required to access the Perfecto Cloud Reporting Server.");
		}

		if (_testExecutionId.isEmpty()) {
			throw new ConfigurationException(
					"Execution ID parameter is required to retrieve XCTest or Espresso test execution results.");
		}

		if (_xmlOutputFilePath.isEmpty()) {
			throw new ConfigurationException(
					"The XML Output File Path parameter is required to write the Junit XML output.");
		}
	}

	private static JsonObject callTargetAPI(String reportingServerUrl, String securityToken) throws Throwable {
		System.out.println();
		System.out.println(consoleTime() + "Calling Perfecto Reporting API.");
		ApiHelper ah = new ApiHelper(reportingServerUrl, securityToken);
		System.out.println(consoleTime() + "  reportingServerUrl: " + reportingServerUrl);
		System.out.println(consoleTime() + "  securityToken: " + securityToken);
		JsonObject json = ah.retrieveTestExecutions(_testExecutionId);
		System.out.println(consoleTime() + "Finished calling Perfecto Reporting API.");
		return json;
	}

	private static TestSuites convertJson2Xml(JsonObject jsonTests) throws Throwable {
		System.out.println();
		System.out.println(consoleTime() + "Converting JSON to XML.");
		TestSuites tests = XmlHelper.convertPerfectoJson(jsonTests);
		System.out.println(consoleTime() + "  Test Suite ID: " + tests.getName());
		System.out.println(consoleTime() + "  Test Count: " + tests.getTests());
		System.out.println(consoleTime() + "  Test Failures: " + tests.getFailures());
		System.out.println(consoleTime() + "Finished converting JSON to XML.");
		return tests;
	}

	private static void emitXml(TestSuites xmlTests) throws Throwable {
		System.out.println();
		System.out.println(consoleTime() + "Writing XML to file.");
		File f = new File(_xmlOutputFilePath);
		if (!f.exists()) {
			System.out.println(consoleTime() + "  Creating file: " + _xmlOutputFilePath);
			f.createNewFile();
		}

		OutputStream o = new FileOutputStream(_xmlOutputFilePath);
		String xml = XmlHelper.emitTestSuites(xmlTests);
		o.write(xml.getBytes());
		o.flush();
		o.close();
		System.out.println(consoleTime() + "Finished writing XML to file: " + _xmlOutputFilePath);
	}

	private static String consoleTime() {
		Date now = new Date(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss ");
		return sdf.format(now);
	}
}