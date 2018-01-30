package com.analyzer.simulator;

import com.analyzer.Scheduler;
import com.analyzer.client.OandaReaderClient;
import com.analyzer.client.SimulatorClient;
import com.analyzer.constants.ActionType;
import com.analyzer.constants.GranularityType;
import com.analyzer.constants.InstrumentValue;
import com.analyzer.enricher.action.Action;
import com.analyzer.model.ActionStrategy;
import com.analyzer.model.RawCandlestick;
import com.analyzer.model.repository.RawCandlestickRepository;
import com.analyzer.reader.ReadRequestForm;
import com.analyzer.reader.ReaderUtil;
import com.analyzer.trader.TradeStrategy;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Instant;
import java.util.List;


@RestController
public class SimulatorController {

    private static final Logger log = LoggerFactory.getLogger(SimulatorController.class);

    private final RawCandlestickRepository rawCandlestickRepository;
    private final String dailyTradingPropertiesPath;
    private final Scheduler scheduler;

    @Autowired
    SimulatorController(
            RawCandlestickRepository rawCandlestickRepository,
            Scheduler scheduler,
            @Value("${trader.configuration.daily}")String dailyTradingPropertiesPath) {
        this.rawCandlestickRepository = rawCandlestickRepository;
        this.dailyTradingPropertiesPath = dailyTradingPropertiesPath;
        this.scheduler = scheduler;
    }

    @RequestMapping(value = "/runDaily", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> runDaily(){
        try {
            scheduler.elaborateOperations(dailyTradingPropertiesPath);

            return new ResponseEntity<>("ok", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/simulate", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> simulate(
            @Valid @RequestBody SimulationRequestForm simulationRequestForm,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            Instant fromDate = ReaderUtil.parse(simulationRequestForm.getFromDate(),ReadRequestForm.DATE_TIME_PATTERN);
            Instant toDate = ReaderUtil.parse(simulationRequestForm.getToDate(),ReadRequestForm.DATE_TIME_PATTERN);


            // Read the files in daily directory
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(dailyTradingPropertiesPath);

            // For each instrument retriew candlestick to elaborate for prediction
            for (Resource resource : resources) {
                log.info("Reading trading settings file: " + resource.getFilename());
                PropertiesConfiguration config = new PropertiesConfiguration(resource.getFile());
                GranularityType granularity = GranularityType.D;
                InstrumentValue instrument = InstrumentValue.valueOf(config.getString("trade.instrument"));
                List<RawCandlestick> rawCandlestickList = ReaderUtil.getRawCandlesticks(
                        fromDate, toDate,
                        granularity, instrument,
                        rawCandlestickRepository);
                SimulatorClient simulatorClient = new SimulatorClient(rawCandlestickRepository);
                TradeStrategy tradeStrategy = new TradeStrategy(simulatorClient, config);
                int correct = 0;
                int incorrect = 0;
                int nothing = 0;
                for (RawCandlestick rawCandlestick : rawCandlestickList) {
                    tradeStrategy.calculateInput(rawCandlestick);
                    Action action = tradeStrategy.checkForTrade();

                    for (ActionStrategy actionStrategy : rawCandlestick.getActionStrategies()) {
                        if (actionStrategy.getStrategyTypeValue().equals(tradeStrategy.getStrategy().name())) {
                            if (actionStrategy.getActionTypeValue().equals(action.getType().getValue())) {
                                log.info("Predicted correct "+action.getType() + " on date: "+rawCandlestick.getRawCandlestickKey().getDateTime());
                                correct++;
                            } else if (action.getType() != ActionType.NOTHING){
                                log.info("Predicted incorrect "+action.getType() + " on date: "+rawCandlestick.getRawCandlestickKey().getDateTime());
                                incorrect++;
                            } else {
                                log.info("Predicted to do nothing instead of "+actionStrategy.getActionTypeValue());
                                nothing++;
                            }
                        }
                    }
                }
                log.info("Instrument "+ instrument + " correct predictions: "+correct);
                log.info("Instrument "+ instrument + " incorrect predictions: "+incorrect);
                log.info("Instrument "+ instrument + " no predictions: "+nothing);
                //log.info("Instrument "+ instrument + " correct ratio: "+ratio+"%");
            }
            return new ResponseEntity<>("ok", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
