
package com.crio.warmup.stock;


import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.crio.warmup.stock.portfolio.PortfolioManagerImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {

 
  private static String token="ba5ff70641d605770fd6c3d0e96e05d00969ba8e";

  public static String getToken(){
    return token;
  }

 

  // TODO:
  //  After refactor, make sure that the tests pass by using these two commands
  //  ./gradlew test --tests PortfolioManagerApplicationTest.readTradesFromJson
  //  ./gradlew test --tests PortfolioManagerApplicationTest.mainReadFile

   public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    RestTemplate restTemplate= new RestTemplate();
    ObjectMapper mapper = new ObjectMapper();
    DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate endDate = LocalDate.parse(args[1], format);

   String token="ba5ff70641d605770fd6c3d0e96e05d00969ba8e";
   List<PortfolioTrade> list=new ArrayList<>();
   list=readTradesFromJson(args[0]);
   
   
   List<TotalReturnsDto> tDtos= new ArrayList<>();

   for (PortfolioTrade li :list) {
    String symbol=li.getSymbol();
    if(li.getPurchaseDate().isAfter(endDate)){
      throw new RuntimeException("invalid date");
    }
    
    String url=prepareUrl(li, endDate, token);
///////////////////////////////////////////////////////////////////////
    
    ResponseEntity<TiingoCandle[]> responseEntity =restTemplate.getForEntity(url, TiingoCandle[].class);
    TiingoCandle[] tiingoCandles = responseEntity.getBody();
    LocalDate currentMax=LocalDate.of(1900, 1, 8);
    Double close=0.0d;

    for (TiingoCandle pft : tiingoCandles) {
      if(pft.getDate().isAfter(currentMax)){
        currentMax=pft.getDate();
        close=pft.getClose();
      }
    }
    TotalReturnsDto td=new TotalReturnsDto(symbol, close);
    tDtos.add(td);
  }
  Collections.sort(tDtos, new Comparator<TotalReturnsDto>() {
    @Override
    public int compare(TotalReturnsDto t1, TotalReturnsDto t2) {
      return Double.compare(t1.getClosingPrice(), t2.getClosingPrice());
    }
  });
    
  List<String> ans=new ArrayList<>();
   for(TotalReturnsDto t1:tDtos){
      ans.add(t1.getSymbol());
   }

    return ans;
  }
  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    File file= resolveFileFromResources(args[0]);
    PortfolioTrade[] portfolioTrades=getObjectMapper().readValue(file, PortfolioTrade[].class);
    List<String> list= new ArrayList<>();
    for (PortfolioTrade pft : portfolioTrades) {
      list.add(pft.getSymbol());
    }
     return list;
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(
        Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
    File file= resolveFileFromResources(filename);
    PortfolioTrade[] portfolioTrades=getObjectMapper().readValue(file, PortfolioTrade[].class);
    List<PortfolioTrade> list= new ArrayList<>();
    for (PortfolioTrade pft : portfolioTrades) {
      list.add(pft);
    }
     return list;
  }


  // TODO:
  //  Build the Url using given parameters and use this function in your code to cann the API.
  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
    String urlHead="https://api.tiingo.com/tiingo/daily/";
    String url=urlHead+trade.getSymbol()+"/"+"prices?startDate="+trade.getPurchaseDate()+"&endDate="+endDate+"&token="+token;
     return url;
  }

  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  public static List<String> debugOutputs() {

    String valueOfArgument0 = "trades.json";
    String resultOfResolveFilePathArgs0 = "/home/crio-user/workspace/mihirhans-ME_QMONEY_V2/qmoney/bin/main/trades.json";
    String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@7c6908d7";
    String functionNameFromTestFileInStackTrace = "mainReadFile";
    String lineNumberFromTestFileInStackTrace = "29";


   return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
       toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
       lineNumberFromTestFileInStackTrace});
 }



  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Now that you have the list of PortfolioTrade and their data, calculate annualized returns
  //  for the stocks provided in the Json.
  //  Use the function you just wrote #calculateAnnualizedReturns.
  //  Return the list of AnnualizedReturns sorted by annualizedReturns in descending order.

  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.




  // TODO:
  //  Ensure all tests are passing using below command
  //  ./gradlew test --tests ModuleThreeRefactorTest
  static Double getOpeningPriceOnStartDate(List<Candle> candles) {
    candles.sort((c1, c2) -> c1.getDate().compareTo(c2.getDate()));
    return candles.get(0).getOpen();
  }


  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
     candles.sort((c1, c2) -> c1.getDate().compareTo(c2.getDate()));
     return candles.get(candles.size()-1).getClose();
  }


  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
    String url=prepareUrl(trade, endDate, token);
    RestTemplate restTemplate= new RestTemplate();
    ResponseEntity<TiingoCandle[]> responseEntity =restTemplate.getForEntity(url, TiingoCandle[].class);
    TiingoCandle[] tiingoCandles = responseEntity.getBody();
    List<Candle> candleList = new ArrayList<>(Arrays.asList(tiingoCandles));
    return candleList;
  }

  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException {
      List<PortfolioTrade> trade=new ArrayList<>();
      trade=readTradesFromJson(args[0]);
      LocalDate endDate = LocalDate.parse(args[1],DateTimeFormatter.ISO_LOCAL_DATE);
      List<AnnualizedReturn> annualizedReturnList = trade.stream()
        .map(portfolioTrade -> {
          List<Candle> candles=fetchCandles(portfolioTrade, endDate, getToken());
          Double buyPrice=getOpeningPriceOnStartDate(candles);
          Double sellPrice=getClosingPriceOnEndDate(candles);
          AnnualizedReturn annualizedReturn=calculateAnnualizedReturns(endDate, portfolioTrade, buyPrice, sellPrice);
          return annualizedReturn;
        })
        .collect(Collectors.toList());
      
      
        Collections.sort(annualizedReturnList, new Comparator<AnnualizedReturn>() {
          @Override
          public int compare(AnnualizedReturn t1, AnnualizedReturn t2) {
            return Double.compare(t1.getAnnualizedReturn(), t2.getAnnualizedReturn());
          }
        });
        Collections.reverse(annualizedReturnList);

       return annualizedReturnList;
     
  }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Return the populated list of AnnualizedReturn for all stocks.
  //  Annualized returns should be calculated in two steps:
  //   1. Calculate totalReturn = (sell_value - buy_value) / buy_value.
  //      1.1 Store the same as totalReturns
  //   2. Calculate extrapolated annualized returns by scaling the same in years span.
  //      The formula is:
  //      annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  //      2.1 Store the same as annualized_returns
  //  Test the same using below specified command. The build should be successful.
  //     ./gradlew test --tests PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {
        Double totalReturns=(sellPrice-buyPrice)/buyPrice;
        Double totalNumbersOfYear = trade.getPurchaseDate().until(endDate, ChronoUnit.DAYS)/365.24;
        Double annualizedReturn=Math.pow(1+totalReturns, (1/totalNumbersOfYear))-1;
      return new AnnualizedReturn(trade.getSymbol(), annualizedReturn,totalReturns);
  }

















  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Once you are done with the implementation inside PortfolioManagerImpl and
  //  PortfolioManagerFactory, create PortfolioManager using PortfolioManagerFactory.
  //  Refer to the code from previous modules to get the List<PortfolioTrades> and endDate, and
  //  call the newly implemented method in PortfolioManager to calculate the annualized returns.

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
       String file = args[0];
       LocalDate endDate = LocalDate.parse(args[1]);
       String contents = readFileAsString(file);
       ObjectMapper objectMapper = getObjectMapper();
       PortfolioTrade[] portfolioTrades=objectMapper.readValue(contents, PortfolioTrade[].class);
       RestTemplate restTemplate=new RestTemplate();
       PortfolioManager portfolioManager=PortfolioManagerFactory.getPortfolioManager(restTemplate);  
       return portfolioManager.calculateAnnualizedReturn(Arrays.asList(portfolioTrades), endDate);
  }


  private static String readFileAsString(String file)throws URISyntaxException, IOException {
    return new String(Files.readAllBytes(resolveFileFromResources(file).toPath()),"UTF-8");
  }



  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());


    //printJsonObject(mainReadQuotes(args));


    //printJsonObject(mainCalculateSingleReturn(args));



    printJsonObject(mainCalculateReturnsAfterRefactor(args));
  }
}

