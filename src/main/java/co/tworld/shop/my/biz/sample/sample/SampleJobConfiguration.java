package co.tworld.shop.my.biz.sample.sample;

import co.tworld.shop.my.config.UniqueRunIdIncrementer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class SampleJobConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job sampleJob(Step sampleStep1, Step sampleStep2) {

        log.info("#################### sampleJob start #################");
        // 순차스텝: start, next... 선후관계가 있는경우 사용.
        // 부모 잡 실행 컨텍스트와 순서를 공유 함.
        return jobBuilderFactory.get("sampleJob")
                .preventRestart()
                .start(sampleStep1)
                .incrementer(new UniqueRunIdIncrementer())
                .next(sampleStep2)
                .build();
    }

    @Bean
    @JobScope
    @Retryable(backoff = @Backoff(delay = 1000, maxDelay = 10000, multiplier = 2))
    public Step sampleStep1(@Value("#{jobParameters[requestDate]}") String requestDate) {
        return stepBuilderFactory.get("sampleStep1")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>> This is Step1");
                    log.info(">>>> requestDate : {}", requestDate);
                    return RepeatStatus.FINISHED;
//                    throw new IllegalArgumentException("step1에서 실패합니다.");
                })
                .build();
    }

    @Bean
    @JobScope
    public Step sampleStep2(@Value("#{jobParameters[requestDate]}") String requestDate) {
        return stepBuilderFactory.get("sampleStep2")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>> This is Step2");
                    log.info(">>>> requestDate : {}", requestDate);
                    return RepeatStatus.FINISHED;
                })
                .build();
    }
}
