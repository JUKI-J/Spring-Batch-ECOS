package setlog.spring.batch.dao;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import setlog.spring.batch.vo.CurrencyVO;

@Repository
public class CurrencyDAO {

	/** MySQL DB*/
	@Resource(name="currencySqlSessionTemplate")
	private SqlSessionTemplate mysqlCurrency;
	
	public int selectALL() {
		return mysqlCurrency.selectOne("setlog.spring.batch.dao.CurrencyMapper.selectALL");
	}
	
	public int insertCurrencyMstAllList(HashMap<String,List<CurrencyVO>> paramMap) {
		return mysqlCurrency.insert("setlog.spring.batch.dao.CurrencyMapper.insertCurrencyMstAllList", paramMap);
	}

	public int insertCurrencyHstFromMst() {
		return mysqlCurrency.insert("setlog.spring.batch.dao.CurrencyMapper.insertCurrencyHstFromMst");
	}

	public int deleteCurrencyMst() {
		return mysqlCurrency.delete("setlog.spring.batch.dao.CurrencyMapper.deleteCurrencyMst");
	}
	
	
}
