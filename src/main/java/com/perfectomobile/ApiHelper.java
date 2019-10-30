/**
 * 
 */
package com.perfectomobile;

import com.google.gson.*;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import java.io.InputStreamReader;
import java.net.*;

/**
 * @author davidre@perfectomobile.com
 * 
 *         See public API dox
 *         https://developers.perfectomobile.com/display/PD/Smart+Reporting+Public+API
 * 
 *         Borrowed heavily from
 *         https://github.com/PerfectoCode/Reporting-Samples/blob/master/Java/export-api-sample/src/main/java/com/perfecto/reporting/sample/api/ReportiumExportUtils.java
 *         https://github.com/PerfectoCode/Reporting-Samples/blob/master/Java/export-api-sample/src/main/java/com/perfecto/reporting/sample/api/ApiExportCodeSample.java
 * 
 *         Refactored into utility class.
 */
public class ApiHelper {
	private String _securityToken = null;
	private String _reportingServerUrl = null;

	public ApiHelper(String reportingServerUrl, String securityToken) {
		_reportingServerUrl = reportingServerUrl;
		_securityToken = securityToken;
	}

	/**
	 * Returns executions by Execution ID a.k.a. Management ID.
	 * 
	 * @param executionId
	 * @throws Throwable
	 */
	public JsonObject retrieveTestExecutions(String executionId) throws Throwable {
		URIBuilder uriBuilder = new URIBuilder(_reportingServerUrl + "/export/api/v1/test-executions/");
		uriBuilder.addParameter("id", executionId);
		return fetchJson(uriBuilder);
	}

	public JsonObject retrieveTestExecutionsByTag(String tag) throws Throwable {
		URIBuilder uriBuilder = new URIBuilder(_reportingServerUrl + "/export/api/v1/test-executions/");
		uriBuilder.addParameter("tags[0]", tag);
		return fetchJson(uriBuilder);
	}

	public JsonObject retrieveTestExecutions(long unixStartTime, long unixEndTime) throws Throwable {
		URIBuilder uriBuilder = new URIBuilder(_reportingServerUrl + "/export/api/v1/test-executions");
		uriBuilder.addParameter("startExecutionTime[0]", Long.toString(unixStartTime));
		uriBuilder.addParameter("endExecutionTime[0]", Long.toString(unixEndTime));
		return fetchJson(uriBuilder);
	}

	public JsonObject retrieveTestExecutions(String jobName, String jobNumber) throws Throwable {
		URIBuilder uriBuilder = new URIBuilder(_reportingServerUrl + "/export/api/v1/test-executions");
		uriBuilder.addParameter("jobName[0]", jobName);
		uriBuilder.addParameter("jobNumber[0]", jobNumber);
		return fetchJson(uriBuilder);
	}

	public JsonObject retrieveTestCommands(String executionId) throws Throwable {
		URIBuilder uriBuilder = new URIBuilder(_reportingServerUrl + "/export/api/v1/test-executions/" + executionId + "/commands");
		JsonObject json = fetchJson(uriBuilder);
		System.out.println(json);
		return json;
	}

	private JsonObject fetchJson(URIBuilder uriBuilder) throws Throwable {
		URI uri = uriBuilder.build();
		HttpGet getExecutions = new HttpGet(uri);
		JsonObject executions = getJson(getExecutions);
		return executions;
	}

    private void addDefaultRequestHeaders(HttpRequestBase request) {
		if (_securityToken == null || _securityToken.equals("") || _securityToken.length() == 0) {
			throw new RuntimeException("Invalid security token '" + _securityToken + "'. Please set a security token");
		}
		request.addHeader("PERFECTO_AUTHORIZATION", _securityToken);
	}

	private JsonObject getJson(HttpGet httpGet) throws Throwable {
		JsonObject result;
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		addDefaultRequestHeaders(httpGet);
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse getExecutionsResponse = httpClient.execute(httpGet);
		try (InputStreamReader inputStreamReader = new InputStreamReader(
				getExecutionsResponse.getEntity().getContent())) {
			String response = IOUtils.toString(inputStreamReader);
			try {
				result = gson.fromJson(response, JsonObject.class);
			} catch (JsonSyntaxException e) {
				throw new RuntimeException("Unable to parse response: " + response);
			}
		} catch (Exception ex) {
			throw ex;
		}
		return result;
	}
}
