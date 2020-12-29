package co.tworld.shop.my;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableBatchProcessing
@SpringBootApplication
@EnableRetry
public class TwdBatchAppApplication implements CommandLineRunner {

//    @Autowired
//    ApplicationContext context;

    public static void main(String[] args) {
        SpringApplication.run(TwdBatchAppApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
//        JobLauncher jobLauncher = context.getBean(JobLauncher.class);
//        Job job = context.getBean("sampleJob", Job.class);
//        jobLauncher.run(job, new JobParametersBuilder().addDate("date", new Date()).toJobParameters());
    }
}
