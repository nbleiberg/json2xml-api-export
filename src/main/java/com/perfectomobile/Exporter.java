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
import java.util.concurrent.TimeUnit;

/**
 * Pull JSON response from Perfecto Reporting API, transform into Junit XML, and
 * write to file.
 * 
 * Built for v1 of
 * https://developers.perfectomobile.com/display/PD/Smart+Reporting+Public+API
 * 
 * @author davidre@perfectomobile.com
 */
public class Exporter {

	private static String _perfectoCloudReportingServer = "";
	private static String _perfectoSecurityToken = "";
	private static String _testExecutionId = "";
	private static String _xmlOutputFilePath = "./TestSuites.xml";
	private static String _jsonOutputFilePath = "./TestSuites.json";
	private static boolean _verbose = false;
	private static Options _options = new Options();
	private static boolean _list = false;
	private static String _tag = "";

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Throwable {
		System.out.println();
		System.out.println(consoleTime() + "Perfecto Reporting API Junit XML Exporter for v1 API");
		setupCliOptions();
		applyCommandLineParameters(args);
		validateConfig();
		JsonObject testJson = callTargetAPI(_perfectoCloudReportingServer, _perfectoSecurityToken);
		emitJsonList(testJson);
		emitJsonToFile(testJson);
		emitXmlToFile(convertJson2Xml(testJson));
		System.out.println();
	}

	private static void setupCliOptions() {
		_options.addOption("c", "perfectoCloudReportingServer", true,
				"The URL, including protocol, for the target Perfecto Reporting API server.");
		_options.addOption("s", "perfectoSecurityToken", true,
				"The authentication token for access to the Perfecto Reporting API server");
		_options.addOption("e", "testExecutionId", true,
				"The Execution ID of a set of XCTest or Espresso test results to export. Also called Management ID.");
		_options.addOption("t", "tag", true,
				"A tag that identifies a set of XCTest or Espresso test results to export.");
		_options.addOption("x", "xmlOutputFilePath", true, "The target file to write Junit XML to.");
		_options.addOption("j", "jsonOutputFilePath", true, "The target file to write the JSON payload to.");
		_options.addOption("v", "verbose", false, "Emit additional verbose telemetry to the console.");
		_options.addOption("h", "help", false, "Output the help documentation to the console.");
		_options.addOption("l", "list", false, "Emit the JSON for today's test executions to the console.");
	}

	private static void applyCommandLineParameters(String[] args) throws Throwable {
		System.out.println(consoleTime() + "Parsing command line arguments.");
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(_options, args);

		if (cmd.hasOption("h")) {
			emitHelp();
			System.exit(0);
		}

		if (cmd.hasOption("v")) {
			_verbose = true;
		}

		if (cmd.hasOption("l")) {
			_list = true;
		}

		_perfectoCloudReportingServer = popOption(cmd, "c");
		_perfectoSecurityToken = popOption(cmd, "s");
		_testExecutionId = popOption(cmd, "e");
		_xmlOutputFilePath = popOption(cmd, "x");
		_jsonOutputFilePath = popOption(cmd, "j");
		_tag = popOption(cmd, "t");

		System.out.println(consoleTime() + "Finished parsing command line arguments.");
	}

	private static void emitHelp() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java -jar json2xml-api-export.jar", _options);
	}

	private static String popOption(CommandLine cmd, String option) {
		String targetMember = "";
		if (cmd.hasOption(option)) {
			targetMember = cmd.getOptionValue(option);
			if (_verbose) {
				System.out.println(consoleTime() + "  Found " + option + " :: " + targetMember);
			}
		} else {
			if (_verbose) {
				System.out.println(consoleTime() + "  Skipped " + option + " :: No value provided.");
			}
		}
		return targetMember;
	}

	private static void validateConfig() throws Throwable {
		if (_perfectoCloudReportingServer.isEmpty()) {
			emitHelp();
			throw new ConfigurationException("Perfecto Cloud Reporting Server parameter is required.");
		}

		if (_perfectoSecurityToken.isEmpty()) {
			emitHelp();
			throw new ConfigurationException(
					"Perfecto Security Token parameter is required to access the Perfecto Cloud Reporting Server.");
		}

		if (!_list && _testExecutionId.isEmpty() && _tag.isEmpty()) {
			emitHelp();
			throw new ConfigurationException(
					"Either Execution ID or Tag parameter is required to retrieve XCTest or Espresso test execution results.");
		}

		if (!_list && _xmlOutputFilePath.isEmpty()) {
			emitHelp();
			throw new ConfigurationException(
					"The XML Output File Path parameter is required to write the Junit XML output.");
		}
	}

	private static JsonObject callTargetAPI(String reportingServerUrl, String securityToken) throws Throwable {
		System.out.println(consoleTime() + "Calling Perfecto Reporting API.");
		ApiHelper ah = new ApiHelper(reportingServerUrl, securityToken);
		if (_verbose) {
			System.out.println(consoleTime() + "  reportingServerUrl: " + reportingServerUrl);
			System.out.println(consoleTime() + "  securityToken: " + securityToken);
		}
		JsonObject json;
		if (_list) {
			if (_verbose) {
				System.out.println(consoleTime() + "  retrieving list of today's test executions.");
			}
			Long start = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30);
			Long end = System.currentTimeMillis();
			json = ah.retrieveTestExecutions(start, end);
		} else {
			if (!_testExecutionId.isEmpty()) {
				json = ah.retrieveTestExecutions(_testExecutionId);
			} else {
				json = ah.retrieveTestExecutionsByTag(_tag);
			}
		}
		System.out.println(consoleTime() + "Finished calling Perfecto Reporting API.");
		return json;
	}

	private static TestSuites convertJson2Xml(JsonObject jsonTests) throws Throwable {
		System.out.println(consoleTime() + "Converting JSON to XML.");
		TestSuites tests = XmlHelper.convertPerfectoJson(_testExecutionId, jsonTests);
		if (_verbose) {
			System.out.println(consoleTime() + "  Test Suite ID: " + tests.getName());
			System.out.println(consoleTime() + "  Test Count: " + tests.getTests());
			System.out.println(consoleTime() + "  Test Failures: " + tests.getFailures());
		}
		System.out.println(consoleTime() + "Finished converting JSON to XML.");
		return tests;
	}

	private static void emitJsonList(JsonObject testJson) throws Throwable {
		if (!_list) {
			return;
		}
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		System.out.println();
		System.out.println(gson.toJson(testJson));
		System.out.println();
	}

	private static void emitJsonToFile(JsonObject testJson) throws Throwable {
		if (_jsonOutputFilePath.isEmpty()) {
			return;
		}

		System.out.println(consoleTime() + "Writing Json to file.");
		File f = new File(_jsonOutputFilePath);
		if (!f.exists() && _verbose) {
			System.out.println(consoleTime() + "  Creating file: " + _jsonOutputFilePath);
			f.createNewFile();
		}

		OutputStream o = new FileOutputStream(_jsonOutputFilePath);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		o.write(gson.toJson(testJson).getBytes());
		o.flush();
		o.close();
		System.out.println(consoleTime() + "Finished writing Json to file: " + _jsonOutputFilePath);
	}

	private static void emitXmlToFile(TestSuites xmlTests) throws Throwable {
		if (_list) {
			return;
		}
		System.out.println(consoleTime() + "Writing XML to file.");
		File f = new File(_xmlOutputFilePath);
		if (!f.exists() && _verbose) {
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
