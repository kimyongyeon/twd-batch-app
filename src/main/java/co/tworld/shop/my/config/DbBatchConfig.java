package co.tworld.shop.my.config;

import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;


@Profile(value = {"default"})
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "co.tworld.shop.my.biz", // TODO Repository 패키지 지정
        transactionManagerRef = "dataSourceTransactionManager"
)
@PropertySource(value = "classpath:/application.yml")
public class DbBatchConfig {

    @Autowired
    private Environment defaultEnv;

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setBackOffPolicy(backOffPolicy());
        return retryTemplate;
    }

    @Bean
    public ExponentialBackOffPolicy backOffPolicy() {
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(defaultEnv.getProperty("batch.retry.initInterval", Integer.class)); // 최초 1초 대기
        backOffPolicy.setMaxInterval(defaultEnv.getProperty("batch.retry.maxInterval", Integer.class)); // 최대 10초 이후는 계속 10초 후에 재시도
        backOffPolicy.setMultiplier(defaultEnv.getProperty("batch.retry.multiplier", Integer.class)); // 2배초 만큼 시간이 늘어난다.
        return backOffPolicy;
    }

    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(@Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }

    private DataSource dataSource(Environment env) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("spring.datasource.driver-class-name"));
        dataSource.setUrl(env.getProperty("spring.datasource.url"));
        dataSource.setUsername(env.getProperty("spring.datasource.username"));
        dataSource.setPassword(env.getProperty("spring.datasource.password"));
        return dataSource;
    }


    @Profile(value = {"dev", "prd"})
    @PropertySource(value = {"classpath:/application-prd.yml", "classpath:/application-dev.yml"})
    class PrdDevEnv {
        @Autowired
        private Environment env;

        @Autowired
        DataSource dataSource;

        @Autowired
        PlatformTransactionManager transactionManager;

        @Autowired
        DbBatchConfig dbBatchConfig;

        @Bean
        public DataSource dataSource() {
            return dbBatchConfig.dataSource(env);
        }

        @Bean(name = "entityManagerFactory")
        public LocalContainerEntityManagerFactoryBean entityManagerFactory(
                EntityManagerFactoryBuilder builder,
                @Qualifier("dataSource") DataSource dataSource) {
            Map<String, String> map = new HashMap<>();
            map.put("hibernate.ejb.naming_strategy", "org.hibernate.cfg.ImprovedNamingStrategy");
            map.put("hibernate.dialect", "org.hibernate.dialect.MySQL5InnoDBDialect");
            return builder.dataSource(dataSource)
                    .packages("co.tworld.shop.my.biz") // TODO Model 패키지 지정
                    .properties(map)
                    .build();
        }

        @Bean(name = "jobRepository")
        public JobRepositoryFactoryBean jobRepository() {
            JobRepositoryFactoryBean jobRepositoryFactoryBean =
                    new JobRepositoryFactoryBean();
            jobRepositoryFactoryBean.setDataSource(dataSource);
            jobRepositoryFactoryBean.setTransactionManager(transactionManager);
            return jobRepositoryFactoryBean;
        }

        /**
         * 배치 잡을 시동하는 메커니즘을 건네주는 역할
         * 배치 솔루션과 매개변수를 지정
         *
         * @return
         * @throws Exception
         */
        @Bean(name = "jobLauncher")
        public JobLauncher jobLauncher() throws Exception {
            SimpleJobLauncher simpleJobLauncher = new SimpleJobLauncher();
            simpleJobLauncher.setJobRepository(jobRepository().getObject());
            return simpleJobLauncher;
        }

        @Bean(name = "jobRegistryBeanPostProcessor")
        public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor() {
            JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor =
                    new JobRegistryBeanPostProcessor();
            jobRegistryBeanPostProcessor.setJobRegistry(jobRegistry());
            return jobRegistryBeanPostProcessor;
        }

        /**
         * 특정 잡에 관한 정보를 담고 있는 중앙 저장소 이자, 시스템 내부의 전체 잡들의 큰 그림을 그리며 관장하는 빈
         *
         * @return
         */
        @Bean(name = "jobRegistry")
        public JobRegistry jobRegistry() {
            return new MapJobRegistry();
        }

        @Bean(name = "dataSourceInitializer")
        public DataSourceInitializer dataSourceInitializer() {
            DataSourceInitializer initializer = new DataSourceInitializer();
            initializer.setDataSource(dataSource);
            initializer.setDatabasePopulator(databasePopulator());
            return initializer;
        }

        private DatabasePopulator databasePopulator() {
            ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
            databasePopulator.setContinueOnError(true);
            databasePopulator.addScript(new ClassPathResource("/org/springframework/batch/core/schema-h2.sql"));
            // todo: 초기 구동되어야할 쿼리 스크립트 삽입 하시오
            databasePopulator.addScript(new ClassPathResource("/schema-all.sql"));
            return databasePopulator;
        }
    }

    @Profile(value = {"default", "local"})
    @PropertySource(value = {"classpath:/application-default.yml", "classpath:/application-local.yml"})
    class LocalEnv {

        @Autowired
        private Environment env;

        @Autowired
        DbBatchConfig dbBatchConfig;

        @Bean(name = "h2DataSource")
        public DataSource dataSource() {
            return dbBatchConfig.dataSource(env);
        }

//        @Bean(name = "entityManagerFactory")
//        public LocalContainerEntityManagerFactoryBean entityManagerFactory(
//                EntityManagerFactoryBuilder builder,
//                @Qualifier("dataSource") DataSource dataSource) {
//            Map<String, String> map = new HashMap<>();
//            map.put("hibernate.ejb.naming_strategy", "org.hibernate.cfg.ImprovedNamingStrategy");
//            map.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
//            return builder.dataSource(dataSource)
//                    .packages("co.tworld.shop.my.biz") // TODO Model 패키지 지정
//                    .properties(map)
//                    .build();
//        }
    }

}