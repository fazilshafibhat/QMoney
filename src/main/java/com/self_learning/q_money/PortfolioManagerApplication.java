package com.self_learning.q_money;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.self_learning.q_money.dto.PortfolioTrade;
import com.self_learning.q_money.dto.TiingoCandle;
import com.self_learning.q_money.dto.TotalReturnsDto;
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
		return new String(Files.readAllBytes(resolveFileFromResources(fileName).toPath()), "UTF-8");
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

	public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
		// List<PortfolioTrade> trades = readTradesFromJson(args[0]);
		List<PortfolioTrade> trades = readTradesFromJson("trades.json");
		RestTemplate restTemplate = new RestTemplate();
		List<TotalReturnsDto> list = new ArrayList<>();
		ArrayList<String> output = new ArrayList<>();
		// String endDate = args[1];
		String endDate = "2020-01-01";
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		// Parse the string to LocalDate using the defined formatter
		LocalDate localDate = LocalDate.parse(endDate, formatter);
		for (int i = 0; i < trades.size(); i++) {
			String tiingoUrl = prepareUrl(trades.get(i), localDate, "8721f01d335349e747fa731f81a4c7cb735743d8");
			TiingoCandle[] result = restTemplate.getForObject(tiingoUrl, TiingoCandle[].class);
			list.add(
					new TotalReturnsDto(trades.get(i).getSymbol(), result[result.length - 1].getClose()));
		}

		Collections.sort(list, Comparator.comparingDouble(TotalReturnsDto::getClosingPrice));
		for (TotalReturnsDto dto : list) {
			output.add(dto.getSymbol());
		}
		return output;

	}

	public static List<PortfolioTrade> readTradesFromJson(String filename)
			throws IOException, URISyntaxException {
		String file = filename;
		String content = readFileAsString(file);
		ObjectMapper om = getObjectMapper();
		PortfolioTrade[] pt = om.readValue(content, PortfolioTrade[].class);
		return Arrays.asList(pt);
	}

	public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
		// return null;
		String baseUrl = "https://api.tiingo.com/tiingo/daily/";
		String symbol = trade.getSymbol();

		// Parse the date string from PortfolioTrade to OffsetDateTime
		LocalDate startDate = trade.getPurchaseDate();

		// Format start and end dates as "yyyy-MM-dd" for URL
		String formattedStartDate = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE); // string
		String formattedEndDate = endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

		// Form the complete URL
		return baseUrl + symbol + "/prices?startDate=" + formattedStartDate + "&endDate="
				+ formattedEndDate + "&token=" + token;
	}

	public static void main(String[] args) throws Exception {
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
		ThreadContext.put("runId", UUID.randomUUID().toString());
		printJsonObject(mainReadFile(args));
		printJsonObject(mainReadQuotes(args));
	}
}
