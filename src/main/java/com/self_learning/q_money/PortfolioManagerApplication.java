package com.self_learning.q_money;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.self_learning.q_money.dto.PortfolioTrade;
import com.self_learning.q_money.log.UncaughtExceptionHandler;

@SpringBootApplication
public class PortfolioManagerApplication {

	public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
		// String file = args[0];
		String file = "trades.json";
		String content = readFileAsString(file);
		ObjectMapper om = getObjectMapper();
		PortfolioTrade[] pt = om.readValue(content, PortfolioTrade[].class);
		ArrayList<String> result = new ArrayList<>();
		for (int i = 0; i < pt.length; i++) {
			result.add(pt[i].getSymbol());
		}
		return result;
	}

	private static String readFileAsString(String fileName)
			throws UnsupportedEncodingException, IOException, URISyntaxException {
		return new String(Files.readAllBytes(resolveFileFromResources(fileName).toPath()),
				"UTF-8");
	}

	private static void printJsonObject(Object object) throws IOException {
		Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
		ObjectMapper mapper = new ObjectMapper();
		logger.info(mapper.writeValueAsString(object));
	}

	private static File resolveFileFromResources(String filename) throws URISyntaxException {
		System.out.println(filename);
		return Paths.get(Thread.currentThread().getContextClassLoader().getResource(filename).toURI())
				.toFile();
	}

	private static ObjectMapper getObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		return objectMapper;
	}

	// ./gradlew test --tests PortfolioManagerApplicationTest.testDebugValues

	public static List<String> debugOutputs() {

		String valueOfArgument0 = "trades.json";
		String resultOfResolveFilePathArgs0 = "";
		String toStringOfObjectMapper = "";
		String functionNameFromTestFileInStackTrace = "";
		String lineNumberFromTestFileInStackTrace = "";

		return Arrays.asList(
				new String[] { valueOfArgument0, resultOfResolveFilePathArgs0, toStringOfObjectMapper,
						functionNameFromTestFileInStackTrace, lineNumberFromTestFileInStackTrace });
	}

	public static void main(String[] args) throws Exception {
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
		ThreadContext.put("runId", UUID.randomUUID().toString());
		printJsonObject(mainReadFile(args));

	}
}
