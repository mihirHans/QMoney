
package com.crio.warmup.stock;


import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication_LOCAL_6290 {

  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    File file= resolveFileFromResources(args[0]);
    PortfolioTrade[] portfolioTrades=getObjectMapper().readValue(file, PortfolioTrade[].class);
    List<String> list= new ArrayList<>();
    for (PortfolioTrade pft : portfolioTrades) {
      list.add(pft.getSymbol());
    }
     return list;
  }
  // TODO: CRIO_TASK_MODULE_REST_API
  //  Find out the closing price of each stock on the end_date and return the list
  //  of all symbols in ascending order by its close value on end date.

  // Note:
  // 1. You may have to register on Tiingo to get the api_token.
  // 2. Look at args parameter and the module instructions carefully.
  // 2. You can copy relevant code from #mainReadFile to parse the Json.
  // 3. Use RestTemplate#getForObject in order to call the API,
  //    and deserialize the results in List<Candle>

  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
     
    RestTemplate restTemplate= new RestTemplate();
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

  // TODO:
  //  After refactor, make sure that the tests pass by using these two commands
  //  ./gradlew test --tests PortfolioManagerApplicationTest.readTradesFromJson
  //  ./gradlew test --tests PortfolioManagerApplicationTest.mainReadFile

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



  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());


    //printJsonObject(mainReadQuotes(args));


  }
}

