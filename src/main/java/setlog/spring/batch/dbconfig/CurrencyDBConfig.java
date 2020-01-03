package setlog.spring.batch.dbconfig;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@Configuration
public class CurrencyDBConfig {
	
	@Bean(name="currencyDataSource")
	@Primary
	@ConfigurationProperties(prefix="spring.currency.datasource.hikari")
	public DataSource currencyDataSource() {
		return DataSourceBuilder.create().build();
	}
	
	@Bean(name="currencySqlSessionFactory")
	@Primary
	public SqlSessionFactory currencySqlSessionFactory(@Qualifier("currencyDataSource") DataSource currencyDataSource, 
															ApplicationContext applicationContext) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(currencyDataSource);
        Resource confiigLocation =  new PathMatchingResourcePatternResolver().getResource("classpath:spring/query/currency-mybatis-config.xml");
        sqlSessionFactoryBean.setConfigLocation(confiigLocation);
        sqlSessionFactoryBean.setMapperLocations(applicationContext.getResources("classpath:spring/query/currency/*.xml"));
        return sqlSessionFactoryBean.getObject();
	}
	
    @Bean(name = "currencySqlSessionTemplate")
    @Primary
    public SqlSessionTemplate db1SqlSessionTemplate(SqlSessionFactory currencySqlSessionFactory) throws Exception { 
        return new SqlSessionTemplate(currencySqlSessionFactory);
    }

}
