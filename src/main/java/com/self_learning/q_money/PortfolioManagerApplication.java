package com.self_learning.q_money;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.self_learning.q_money.dto.AnnualizedReturn;
import com.self_learning.q_money.dto.Candle;
import com.self_learning.q_money.dto.PortfolioTrade;
import com.self_learning.q_money.dto.TiingoCandle;
import com.self_learning.q_money.dto.TotalReturnsDto;
import com.self_learning.q_money.log.UncaughtExceptionHandler;
import com.self_learning.q_money.portfolio.PortfolioManager;
import com.self_learning.q_money.portfolio.PortfolioManagerFactory;

@SpringBootApplication
public class PortfolioManagerApplication {

	public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
		if (args.length == 0) {
			throw new IllegalArgumentException("Missing JSON file argument.");
		}
		// Initialize an ObjectMapper to read JSON
		ObjectMapper objectMapper = getObjectMapper();
		// Read the JSON file from the provided argument
		File file = resolveFileFromResources(args[0]);
		// File file = resolveFileFromResources("trades.json");
		// Deserialize JSON to POJO
		PortfolioTrade[] tradesFromJson = objectMapper.readValue(file, PortfolioTrade[].class);
		// List<PortfolioTrade> portofolioTrades = new Collection<>();
		// Create a list to store symbols
		List<String> symbolList = new ArrayList<>();
		// Itreate through the trade and extract symbol
		for (PortfolioTrade trade : tradesFromJson) {
			String symbol = trade.getSymbol();
			symbolList.add(symbol);
		}
		// print symbol to console
		for (String symbol : symbolList) {
			System.out.println("Symbol: " + symbol);
		}
		return symbolList;
	}

	private static void printJsonObject(Object object) throws IOException {
		Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
		ObjectMapper mapper = new ObjectMapper();
		logger.info(mapper.writeValueAsString(object));
	}

	private static File resolveFileFromResources(String filename) throws URISyntaxException {
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
		String resultOfResolveFilePathArgs0 = "/home/crio-user/workspace/meher-jagannath453-ME_QMONEY_V2/qmoney/bin/main/trades.json";
		String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@6150c3ec";
		String functionNameFromTestFileInStackTrace = "mainReadFile";
		String lineNumberFromTestFileInStackTrace = "27";
		return Arrays.asList(
				new String[] { valueOfArgument0, resultOfResolveFilePathArgs0, toStringOfObjectMapper,
						functionNameFromTestFileInStackTrace, lineNumberFromTestFileInStackTrace });
	}

	public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
		final String tiingoToken = "c010f4bd4369796a57865b9a9e48b2663c9de69f";
		// List<PortfolioTrade> portfolioTrades = readTradesFromJson(args[0]);
		List<PortfolioTrade> portfolioTrades = readTradesFromJson("trades.json");
		// LocalDate endDate = LocalDate.parse(args[1]);
		LocalDate endDate = LocalDate.parse("2020-01-01");
		RestTemplate restTemplate = new RestTemplate();
		List<TotalReturnsDto> totalReturnsDtos = new ArrayList<>();
		List<String> listOfSortSymbolsOnClosingPrice = new ArrayList<>();
		for (PortfolioTrade portfolioTrade : portfolioTrades) {
			String tiingoURL = prepareUrl(portfolioTrade, endDate, tiingoToken);
			TiingoCandle[] tiingoCandleArray = restTemplate.getForObject(tiingoURL, TiingoCandle[].class);
			totalReturnsDtos.add(new com.self_learning.q_money.dto.TotalReturnsDto(portfolioTrade.getSymbol(),
					tiingoCandleArray[tiingoCandleArray.length - 1].getClose()));
		}
		Collections.sort(totalReturnsDtos,
				(a, b) -> Double.compare(a.getClosingPrice(), b.getClosingPrice()));
		for (TotalReturnsDto totalReturnsDto : totalReturnsDtos) {
			listOfSortSymbolsOnClosingPrice.add(totalReturnsDto.getSymbol());
		}
		return listOfSortSymbolsOnClosingPrice;
	}

	public static List<PortfolioTrade> readTradesFromJson(String filename)
			throws IOException, URISyntaxException {
		ObjectMapper objectMapper = getObjectMapper();
		File file = resolveFileFromResources(filename);
		return Arrays.asList(objectMapper.readValue(file, PortfolioTrade[].class));
	}

	public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
		return "https://api.tiingo.com/tiingo/daily/" + trade.getSymbol() + "/prices?startDate="
				+ trade.getPurchaseDate() + "&endDate=" + endDate + "&token=" + token;
	}

	public static String getToken() {
		return "c010f4bd4369796a57865b9a9e48b2663c9de69f";
	}

	public static Double getOpeningPriceOnStartDate(List<Candle> candles) {
		return candles.get(0).getOpen();
	}

	public static Double getClosingPriceOnEndDate(List<Candle> candles) {
		return candles.get(candles.size() - 1).getClose();
	}

	public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
		RestTemplate restTemplate = new RestTemplate();
		String tiingoRestURL = prepareUrl(trade, endDate, token);
		TiingoCandle[] tiingoCandleArray = restTemplate.getForObject(tiingoRestURL, TiingoCandle[].class);
		return Arrays.stream(tiingoCandleArray).collect(Collectors.toList());
	}

	public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
			throws IOException, URISyntaxException {
		List<PortfolioTrade> portfolioTrades = readTradesFromJson(args[0]);
		List<AnnualizedReturn> annualizedReturns = new ArrayList<>();
		LocalDate localDate = LocalDate.parse(args[1]);
		for (PortfolioTrade portfolioTrade : portfolioTrades) {
			List<Candle> candles = fetchCandles(portfolioTrade, localDate, getToken());
			AnnualizedReturn annualizedReturn = calculateAnnualizedReturns(localDate, portfolioTrade,
					getOpeningPriceOnStartDate(candles), getClosingPriceOnEndDate(candles));
			annualizedReturns.add(annualizedReturn);
		}

		return annualizedReturns.stream()
				.sorted((a1, a2) -> Double.compare(a2.getAnnualizedReturn(), a1.getAnnualizedReturn()))
				.collect(Collectors.toList());
	}

	public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate, PortfolioTrade trade,
			double buyPrice, double sellPrice) {
		double total_num_years = ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate) / 365.2422;
		double totalReturns = (sellPrice - buyPrice) / buyPrice;
		double annualized_returns = Math.pow((1.0 + totalReturns), (1.0 / total_num_years)) - 1;
		return new AnnualizedReturn(trade.getSymbol(), annualized_returns, totalReturns);
	}

	private static String readFileAsString(String fileName) throws IOException, URISyntaxException {
		return new String(Files.readAllBytes(resolveFileFromResources(fileName).toPath()), "UTF-8");
	}

	public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
			throws Exception {
		RestTemplate restTemplate = new RestTemplate();
		// String file = args[0];
		String file = "trades.json";
		// LocalDate endDate = LocalDate.parse(args[1]);
		LocalDate endDate = LocalDate.parse("2020-01-01");
		String contents = readFileAsString(file);
		ObjectMapper objectMapper = getObjectMapper();
		PortfolioManager portfolioManager = PortfolioManagerFactory.getPortfolioManager(restTemplate);
		PortfolioTrade[] portfolioTrades = objectMapper.readValue(contents, PortfolioTrade[].class);
		return portfolioManager.calculateAnnualizedReturn(Arrays.asList(portfolioTrades), endDate);
	}

	public static void main(String[] args) throws Exception {
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
		ThreadContext.put("runId", UUID.randomUUID().toString());
		// printJsonObject(mainReadFile(args));
		printJsonObject(mainReadFile(new String[] { "trades.json" }));
		printJsonObject(mainReadQuotes(args));
		printJsonObject(mainReadQuotes(new String[] { "trades.json", "2020-01-01" }));
		printJsonObject(mainCalculateSingleReturn(new String[] { "trades.json", "2020-01-01" }));
		printJsonObject(mainCalculateReturnsAfterRefactor(args));
	}
}
