package com.example.batchprocessing.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(path = "/batch")
public class BatchController {
    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    private Job job;

    @Value("${external.property.database.password}")
    private String databasePassword;

    @Value("${external.property.jwt.key}")
    private String jwtKey;

    @Autowired
    private Environment env;

    @Value("${encryption.key}")
    private String encryptionKey;

    @PostMapping(path = "/start")
    public void startBatch() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis()).toJobParameters();
        try {
            jobLauncher.run(job, jobParameters);
        } catch (JobExecutionAlreadyRunningException | JobRestartException
                 | JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {

            log.info(e.getMessage());
        }
    }

    @GetMapping("/test")
    public String testEncryption() {
        System.out.println(encryptionKey);
        System.out.println(env.getProperty("ENCRYPTION_KEY"));
        log.info("Batch Controller. Database Password: {}. Jwt Key: {}", databasePassword, jwtKey);
        return "Testing Encryption";
    }


//    @Scheduled(cron = "0/5 * * * * ?")
//    public void everyFiveSecond() {
//        log.info("Periodic task:  {}", new Date());
//    }

}

