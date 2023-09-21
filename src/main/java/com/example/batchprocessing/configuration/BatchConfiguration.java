package com.example.batchprocessing.configuration;

import com.example.batchprocessing.entity.Person;
import com.example.batchprocessing.notification.JobCompletionNotificationListener;
import com.example.batchprocessing.processor.PersonItemProcessor;
import com.example.batchprocessing.repository.PersonRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.orm.jpa.JpaTransactionManager;

@Configuration
public class BatchConfiguration {

    @Autowired
    private PersonRepository personRepository;

    @Bean
    public FlatFileItemReader<Person> reader() {
        return new FlatFileItemReaderBuilder<Person>()
                .name("personItemReader")
                .resource(new ClassPathResource("sample-data.csv"))
                .delimited()
                .names(new String[]{"firstName", "lastName"})
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
                    setTargetType(Person.class);
                }})
                .build();
    }

    @Bean
    public PersonItemProcessor processor() {
        return new PersonItemProcessor();
    }

    /*  @Bean
      public JdbcBatchItemWriter<Person> writer(DataSource dataSource) {
          return new JdbcBatchItemWriterBuilder<Person>()
                  .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                  .sql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)")
                  .dataSource(dataSource)
                  .build();
      }*/
    @Bean
    public RepositoryItemWriter<Person> writer() {
        RepositoryItemWriter<Person> writer = new RepositoryItemWriter<>();
        writer.setRepository(personRepository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public Job importUserJob(JobRepository jobRepository,
                             JobCompletionNotificationListener listener, Step step1) {
        return new JobBuilder("importUserJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step1)
                .end()
                .build();
    }

    @Bean
    public Step step1(SkipListener<Person, Person> customSkipListener, ItemReader<Person> itemReader, ItemWriter<Person> itemWriter, JobRepository jobRepository, JpaTransactionManager transactionManager)
            throws Exception {

        return new StepBuilder("step1", jobRepository)
                .<Person, Person>chunk(10, transactionManager)
                .reader(itemReader)
                .processor(processor())
                .writer(itemWriter)
                .faultTolerant()
                .skip(FlatFileParseException.class)
                .skipLimit(2)
                .listener(customSkipListener)
                .build();
    }
}
