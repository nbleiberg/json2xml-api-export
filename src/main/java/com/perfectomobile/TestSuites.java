package com.perfectomobile;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * XML wrapper class for Junit XML.
 * 
 * @author davidre@perfectomobile.com Borrowed heavily (with added extensions
 *         and modifications) from https://gist.github.com/agentgt/8583649.
 */
@XmlRootElement(name = "testsuites")
public class TestSuites {

	private Boolean disabled;
	private int errors = 0;
	private int failures = 0;
	private String name = "";
	private int tests = 0;
	// Seconds
	private Double time;
	private List<TestSuite> testSuites = new ArrayList<TestSuites.TestSuite>();
	private Properties properties;

	@XmlAttribute
	public Boolean getDisabled() {
		return disabled;
	}

	public void setDisabled(Boolean disabled) {
		this.disabled = disabled;
	}

	@XmlAttribute
	public int getErrors() {
		return errors;
	}

	public void setErrors(int errors) {
		this.errors = errors;
	}

	@XmlAttribute
	public int getFailures() {
		return failures;
	}

	public void setFailures(int failures) {
		this.failures = failures;
	}

	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute
	public int getTests() {
		return tests;
	}

	public void setTests(int tests) {
		this.tests = tests;
	}

	@XmlAttribute
	public Double getTime() {
		return time;
	}

	public void setTime(Double time) {
		this.time = time;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	@XmlElement(name = "testsuite")
	public List<TestSuite> getTestSuites() {
		return testSuites;
	}

	public void setTestSuites(List<TestSuite> testSuites) {
		this.testSuites = testSuites;
	}

	@XmlRootElement(name = "testsuite")
	public static class TestSuite {
		private Boolean disabled;
		private int errors = 0;
		private int failures = 0;
		private String hostname;
		private String id;
		private String name;
		private String packageName;
		private int skipped = 0;
		private int tests = 0;
		// Seconds
		private Double time = 0.0;
		private Long timestamp;
		private Properties properties;
		private String systemOut;
		private String systemError;

		private List<TestCase> testCases = new ArrayList<TestSuites.TestCase>();

		public static TestSuite create(String name, Double seconds) {
			TestSuite ts = new TestSuite();
			ts.setName(name);
			ts.setTime(seconds);
			return ts;
		}

		public TestSuite property(String name, String value) {
			if (properties == null)
				properties = new Properties();
			properties.put(name, value);
			return this;
		}

		public TestSuite addTest(TestCase tc) {
			getTestCases().add(tc);
			return this;
		}

		public TestCase createAndAddTest(String name, Double time) {
			TestCase tc = TestCase.create(getName(), name, time);
			addTest(tc);
			return tc;
		}

		@XmlElement
		public Properties getProperties() {
			return properties;
		}

		public void setProperties(Properties properties) {
			this.properties = properties;
		}

		@XmlAttribute
		public Boolean getDisabled() {
			return disabled;
		}

		public void setDisabled(Boolean disabled) {
			this.disabled = disabled;
		}

		@XmlAttribute
		public int getErrors() {
			if (errors == 0 && getTestCases() != null) {
				int e = 0;
				for (TestCase tc : getTestCases()) {
					if (tc.getError() != null)
						e++;
				}
				return e;
			}
			return errors;
		}

		public void setErrors(int errors) {
			this.errors = errors;
		}

		@XmlAttribute
		public int getFailures() {
			if (failures == 0 && getTestCases() != null) {
				int f = 0;
				for (TestCase tc : getTestCases()) {
					if (tc.getFailure() != null)
						f++;
				}
				return f;
			}
			return failures;
		}

		public void setFailures(int failures) {
			this.failures = failures;
		}

		@XmlAttribute
		public String getHostname() {
			return hostname;
		}

		public void setHostname(String hostname) {
			this.hostname = hostname;
		}

		@XmlAttribute
		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		@XmlAttribute
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@XmlAttribute
		public String getPackageName() {
			return packageName;
		}

		public void setPackageName(String packageName) {
			this.packageName = packageName;
		}

		@XmlAttribute
		public int getSkipped() {
			if (skipped == 0 && getTestCases() != null) {
				int f = 0;
				for (TestCase tc : getTestCases()) {
					if (tc.getSkipped() != null)
						f++;
				}
				return f;
			}
			return skipped;
		}

		public void setSkipped(int skipped) {
			this.skipped = skipped;
		}

		@XmlAttribute
		public int getTests() {
			if (tests == 0 && getTestCases() != null)
				return getTestCases().size();
			return tests;
		}

		public void setTests(int tests) {
			this.tests = tests;
		}

		@XmlAttribute
		public Double getTime() {
			return time;
		}

		public void setTime(Double time) {
			this.time = time;
		}

		@XmlAttribute
		public Long getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(Long timestamp) {
			this.timestamp = timestamp;
		}

		@XmlElement(name = "testcase")
		public List<TestCase> getTestCases() {
			return testCases;
		}

		public void setTestCases(List<TestCase> testCases) {
			this.testCases = testCases;
		}

		public String getSystemError() {
			return systemError;
		}

		public void setSystemError(String systemError) {
			this.systemError = systemError;
		}

		public String getSystemOut() {
			return systemOut;
		}

		public void setSystemOut(String systemOut) {
			this.systemOut = systemOut;
		}

	}

	@XmlRootElement(name = "testcase")
	public static class TestCase {
		private String assertions;
		private String classname;
		private String name;
		private String status;
		private Double time;
		private Properties properties;
		private Error error;
		private Failure failure;
		private Skipped skipped;
		private String systemOut;
		private String systemError;

		public static TestCase create(String className, String name, Double seconds) {
			return createSuccess(className, name, seconds);
		}

		public static TestCase createSuccess(String className, String name, Double seconds) {
			TestCase tc = new TestCase();
			tc.setClassname(className);
			tc.setName(name);
			tc.setTime(seconds);
			return tc;
		}

		public static TestCase createSkipped(String className, String name, Double seconds) {
			TestCase tc = createSuccess(className, name, seconds);
			tc.setSkipped(new Skipped());
			return tc;
		}

		public TestCase error(String message, String type, String stack) {
			preValidateStatus();
			this.setError(Error.create(message, type, stack));
			return this;
		}

		public TestCase failure(String message, String type, String stack) {
			preValidateStatus();
			this.setFailure(Failure.create(message, type, stack));
			return this;
		}

		public TestCase skip() {
			preValidateStatus();
			this.setSkipped(new Skipped());
			return this;
		}

		private void preValidateStatus() {
			if (!(getError() == null && getSkipped() == null && getFailure() == null)) {
				throw new IllegalStateException("Error, Skipped, or Failure already set.");
			}
		}

		public TestCase property(String name, String value) {
			if (properties == null)
				properties = new Properties();
			properties.put(name, value);
			return this;
		}

		@XmlElement
		public Properties getProperties() {
			return properties;
		}

		public void setProperties(Properties properties) {
			this.properties = properties;
		}

		@XmlAttribute
		public String getAssertions() {
			return assertions;
		}

		public void setAssertions(String assertions) {
			this.assertions = assertions;
		}

		@XmlAttribute
		public String getClassname() {
			return classname;
		}

		public void setClassname(String classname) {
			this.classname = classname;
		}

		@XmlAttribute
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@XmlAttribute
		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		@XmlAttribute
		public Double getTime() {
			return time;
		}

		public void setTime(Double time) {
			this.time = time;
		}

		@XmlElement
		public Error getError() {
			return error;
		}

		public void setError(Error error) {
			this.error = error;
		}

		@XmlElement
		public Failure getFailure() {
			return failure;
		}

		public void setFailure(Failure failure) {
			this.failure = failure;
		}

		@XmlElement
		public Skipped getSkipped() {
			return skipped;
		}

		public void setSkipped(Skipped skipped) {
			this.skipped = skipped;
		}

		@XmlElement(name = "system-out")
		public String getSystemOut() {
			return systemOut;
		}

		public void setSystemOut(String systemOut) {
			this.systemOut = systemOut;
		}

		@XmlElement(name = "system-error")
		public String getSystemError() {
			return systemError;
		}

		public void setSystemError(String systemError) {
			this.systemError = systemError;
		}

	}

	public static class Error {
		private String message;
		private String type;
		private String stackTrace;

		public static Error create(String message, String type, String stackTrace) {
			Error e = new Error();
			e.setMessage(message);
			e.setType(type);
			e.setStackTrace(stackTrace);
			return e;
		}

		@XmlAttribute
		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		@XmlAttribute
		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		@XmlValue
		public String getStackTrace() {
			return stackTrace;
		}

		public void setStackTrace(String stackTrace) {
			this.stackTrace = stackTrace;
		}

	}

	public static class Failure {
		private String message;
		private String type;
		private String stackTrace;

		public static Failure create(String message, String type, String stackTrace) {
			Failure e = new Failure();
			e.setMessage(message);
			e.setType(type);
			e.setStackTrace(stackTrace);
			return e;
		}

		@XmlAttribute
		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		@XmlAttribute
		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		@XmlValue
		public String getStackTrace() {
			return stackTrace;
		}

		public void setStackTrace(String stackTrace) {
			this.stackTrace = stackTrace;
		}

	}

	public static class Skipped {
	}

	public static class Properties {
		private List<Property> properties = new ArrayList<TestSuites.Property>();

		@XmlElement(name = "property")
		public List<Property> getProperties() {
			return properties;
		}

		public void setProperties(List<Property> properties) {
			this.properties = properties;
		}

		public Properties put(String name, String value) {
			getProperties().add(Property.create(name, value));
			return this;
		}
	}

	public static class Property {
		private String name;
		private String value;

		public static Property create(String name, String value) {
			Property p = new Property();
			p.setName(name);
			p.setValue(value);
			return p;
		}

		@XmlAttribute
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@XmlAttribute
		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

	}

}