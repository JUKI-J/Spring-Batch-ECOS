package setlog.spring.batch.job;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import setlog.spring.batch.dao.CurrencyDAO;
import setlog.spring.batch.vo.CurrencyVO;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class ExchangeCrwalJobConfiguration {

	@Autowired
	private CurrencyDAO currencyDAO;
	
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    
    @Value("${ECOS.API.KEY}")
	String priKey;
    
	@Bean
    public Job startJob() {
		return jobBuilderFactory.get("startJob")
                .start(requestJob())
                .build()
                ;
    }
	
    @Bean
    @JobScope
    public Step requestJob() {
        return stepBuilderFactory.get("requestJob")
                .tasklet((contribution, chunkContext) -> {
                	//get api
                	String srchDate = DateFormatUtils.format(new Date(), "yyyyMMdd");
                	String fullUrl = "http://ecos.bok.or.kr/api/StatisticSearch/"+priKey+"/json/kr/0/100/036Y001/DD/"+srchDate+"/"+srchDate;
                	
                	URL url = new URL(fullUrl);
                	HttpURLConnection con = (HttpURLConnection) url.openConnection();
                	con.setRequestMethod("GET");
                	con.setConnectTimeout(10000);
            		
                	int status = con.getResponseCode();
                	BufferedReader br = null;
                	String inputLine;
			        StringBuffer sb = new StringBuffer();
			        
                	if (status == 200) {
                		br = new BufferedReader(new InputStreamReader(
                									con.getInputStream()));
                	    while ((inputLine = br.readLine()) != null) {
    			        	sb.append(inputLine);
    			        }
                	} else {
                		br = new BufferedReader(new InputStreamReader(
													con.getErrorStream()));
                	}
                	
                	if( !StringUtils.isEmpty(sb.toString()) && sb != null) {
                		
                		JSONParser parser = new JSONParser();
                		JSONObject jsonObj = (JSONObject) parser.parse(sb.toString());
                		JSONObject statJson = (JSONObject) jsonObj.get("StatisticSearch");
                		
                		if( statJson != null) {
                			long totCnt = (long) statJson.get("list_total_count");
                    		JSONArray rows = (JSONArray) statJson.get("row");
                    		
                    		if(totCnt > 0) {
                    			log.info("parseJob result : [" + insertJob(rows) + "]");
                    		}
                		}
                	}

                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Transactional
	public String insertJob(JSONArray rows) {
		
		String result = "FAIL";
		List<CurrencyVO> list = new ArrayList<CurrencyVO>();
		
		for(int ii=0; ii<rows.size(); ii++) {

			JSONObject curJson = (JSONObject) rows.get(ii);
			String code = (String) curJson.get("ITEM_CODE1");
			String value = (String) curJson.get("DATA_VALUE");
			
			CurrencyVO vo = new CurrencyVO();
			vo.setBokCode(code);
			vo.setValue(value);
			
			list.add(vo);
		}
		
		if(list.size() > 0) {
			HashMap<String, List<CurrencyVO>> paramMap = new HashMap<String, List<CurrencyVO>>();
			paramMap.put("currencyVOList", list);
			int copyCnt = currencyDAO.insertCurrencyHstFromMst();
			int deleteCnt = currencyDAO.deleteCurrencyMst();
			int rsltCnt = currencyDAO.insertCurrencyMstAllList(paramMap);
			result = (copyCnt > 0 && rsltCnt > 0) ? "SUCCESS" : "FAIL";
		}
		
		return result;
	}

}
