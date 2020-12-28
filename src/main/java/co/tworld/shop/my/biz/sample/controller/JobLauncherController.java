package co.tworld.shop.my.biz.sample.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping(value = "/sample")
public class JobLauncherController {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JobLauncher jobLauncher;

    // http://localhost:10012/sample/jobLauncher/simpleJob?requestDate=20190101
    @GetMapping("/jobLauncher/{jobName}")
    public String launch(@PathVariable String jobName, @RequestParam Map<String, String> jobParameters) {
        try {
            log.info("[job launch] jobParameters : {}", jobParameters);
            jobLauncher.run(getJob(jobName), getJobParameters(jobParameters));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return "Done";
    }

    private Job getJob(String jobName) {
        return (Job) context.getBean(jobName);
    }

    private JobParameters getJobParameters(Map<String, String> jobParameters) {
        JobParametersBuilder builder = new JobParametersBuilder();

        for (Map.Entry<String, String> jobParameter : jobParameters.entrySet()) {
            builder.addString(jobParameter.getKey(), jobParameter.getValue());
        }

        return builder.toJobParameters();
    }
}
