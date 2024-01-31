package com.self_learning.q_money;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
import com.self_learning.q_money.dto.AnnualizedReturn;
import com.self_learning.q_money.dto.Candle;
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
		String resultOfResolveFilePathArgs0 = "/home/crio-user/workspace/fazilshafi-fsb382-ME_QMONEY_V2/qmoney/bin/main/trades.json";
		String toStringOfObjectMapper = "getObjectMapper().toString()";
		String functionNameFromTestFileInStackTrace = "PortfolioManagerApplicationTest.mainReadFile";
		String lineNumberFromTestFileInStackTrace = "30";
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
		String baseUrl = "https://api.tiingo.com/tiingo/daily/";
		String symbol = trade.getSymbol();
		// Parse the date string from PortfolioTrade to OffsetDateTime
		LocalDate startDate = trade.getPurchaseDate();
		// Format start and end dates as "yyyy-MM-dd" for URL
		String formattedStartDate = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
		String formattedEndDate = endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		// Form the complete URL
		return baseUrl + symbol + "/prices?startDate=" + formattedStartDate + "&endDate="
				+ formattedEndDate + "&token=" + token;
	}

	static Double getOpeningPriceOnStartDate(List<Candle> candles) {
		return candles.get(0).getOpen();
	}

	public static Double getClosingPriceOnEndDate(List<Candle> candles) {
		return candles.get(candles.size() - 1).getClose();
	}

	public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
		String tiingoUrl = prepareUrl(trade, endDate, getToken());
		RestTemplate restTemplate = new RestTemplate();
		List<Candle> candlesList = new ArrayList<>();
		TiingoCandle[] candles = restTemplate.getForObject(tiingoUrl, TiingoCandle[].class);
		for (int i = 0; i < candles.length; i++) {
			candlesList.add(candles[i]);
		}
		return candlesList;
	}

	public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
			throws IOException, URISyntaxException {
		// List<PortfolioTrade> trades = readTradesFromJson(args[0]);
		List<PortfolioTrade> trades = readTradesFromJson("trades.json");
		// LocalDate endDate = LocalDate.parse(args[1]);
		LocalDate endDate = LocalDate.parse("2020-01-01");
		List<AnnualizedReturn> list = new ArrayList<>();
		for (int i = 0; i < trades.size(); i++) {
			List<Candle> candles = fetchCandles(trades.get(i), endDate, getToken());
			Double openPrice = getOpeningPriceOnStartDate(candles);
			Double closePrice = getClosingPriceOnEndDate(candles);
			AnnualizedReturn ar = calculateAnnualizedReturns(endDate, trades.get(i), openPrice, closePrice);

			list.add(ar);
		}
		Collections.sort(list, Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed());
		return list;
	}

	public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate, PortfolioTrade trade,
			Double buyPrice, Double sellPrice) {
		Double totalReturn = (sellPrice - buyPrice) / buyPrice;
		Double total_num_years = ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate) / 365.24;
		Double annualized_returns = Math.pow((1 + totalReturn), (1 / total_num_years)) - 1;
		return new AnnualizedReturn(trade.getSymbol(), annualized_returns, totalReturn);
	}

	public static void main(String[] args) throws Exception {
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
		ThreadContext.put("runId", UUID.randomUUID().toString());
		printJsonObject(mainReadFile(args));
		printJsonObject(mainReadQuotes(args));
		printJsonObject(mainCalculateSingleReturn(args));
	}

	public static String getToken() {
		return "8721f01d335349e747fa731f81a4c7cb735743d8";
	}

}
