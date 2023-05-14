
package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {




  private RestTemplate restTemplate;


  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }


  //TODO: CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from main anymore.
  //    Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  //    into #calculateAnnualizedReturn function here and ensure it follows the method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required further as our
  //    clients will take care of it, going forward.

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command below:
  // ./gradlew test --tests PortfolioManagerTest

  //CHECKSTYLE:OFF




  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo third-party APIs to a separate function.
  //  Remember to fill out the buildUri function and use that.


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException {
        String url=buildUri(symbol,from,to);
        ResponseEntity<TiingoCandle[]> responseEntity =restTemplate.getForEntity(url, TiingoCandle[].class);
        TiingoCandle[] tiingoCandles = responseEntity.getBody();
        List<Candle> candleList = new ArrayList<>(Arrays.asList(tiingoCandles));
    return candleList;
     
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
       String uriTemplate = "https:api.tiingo.com/tiingo/daily/$SYMBOL/prices?"
            + "startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";
            return "https://api.tiingo.com/tiingo/daily/"+symbol+"/"+"prices?startDate="+startDate+"&endDate="+endDate+"&token=ba5ff70641d605770fd6c3d0e96e05d00969ba8e";
  
  }


  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate) {
    // TODO Auto-generated method stub
    List<AnnualizedReturn> annualizedReturnList = portfolioTrades.stream()
        .map(portfolioTrade -> {
          List<Candle> candles = new ArrayList<>();
          try {
             candles = getStockQuote(portfolioTrade.getSymbol(), portfolioTrade.getPurchaseDate(), endDate);
        } catch (JsonProcessingException e) {
            System.out.println("json processing exception");
        }
        
          Double totalReturns=(candles.get(candles.size()-1).getClose()-candles.get(0).getOpen())/candles.get(0).getOpen();
          Double totalNumbersOfYear = portfolioTrade.getPurchaseDate().until(endDate, ChronoUnit.DAYS)/365.24;
          Double annualizedReturn=Math.pow(1+totalReturns, (1/totalNumbersOfYear))-1;
          return new AnnualizedReturn(portfolioTrade.getSymbol(), annualizedReturn,totalReturns);
        })
        .collect(Collectors.toList());
        return annualizedReturnList.stream().sorted(getComparator()).collect(Collectors.toList());
  }
}
