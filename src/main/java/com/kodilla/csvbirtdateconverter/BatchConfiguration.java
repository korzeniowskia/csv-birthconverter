package com.kodilla.csvbirtdateconverter;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {
  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;

  BatchConfiguration(
      JobBuilderFactory jobBuilderFactory,
      StepBuilderFactory stepBuilderFactory
  ) {
    this.jobBuilderFactory = jobBuilderFactory;
    this.stepBuilderFactory = stepBuilderFactory;
  }

  @Bean
  FlatFileItemReader<User> reader() {
    FlatFileItemReader<User> reader = new FlatFileItemReader<>();
    reader.setResource(new ClassPathResource("input_name.csv"));
    DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
    tokenizer.setNames("first_name", "last_name", "birth_date");

    BeanWrapperFieldSetMapper<User> mapper = new BeanWrapperFieldSetMapper<>();
    mapper.setConversionService(localDateConversionService());
    mapper.setTargetType(User.class);

    DefaultLineMapper<User> lineMapper = new DefaultLineMapper<>();
    lineMapper.setFieldSetMapper(mapper);
    lineMapper.setLineTokenizer(tokenizer);

    reader.setLineMapper(lineMapper);

    return reader;
  }

  @Bean
  UserProcessor processor() {
    return new UserProcessor();
  }

  @Bean
  public ConversionService localDateConversionService() {
    DefaultConversionService lodalDateConversionService = new DefaultConversionService();
    DefaultConversionService.addDefaultConverters(lodalDateConversionService);
    lodalDateConversionService.addConverter(new Converter<String, LocalDate>() {
      @Override
      public LocalDate convert(String text) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.uuuu");
        return LocalDate.parse(text, dateTimeFormatter);
      }
    });

    return lodalDateConversionService;
  }

  @Bean
  FlatFileItemWriter<UserAge> writer() {
    BeanWrapperFieldExtractor<UserAge> extractor = new BeanWrapperFieldExtractor<>();
    extractor.setNames(new String[] {"firstName", "lastName", "age"});

    DelimitedLineAggregator<UserAge> aggregator = new DelimitedLineAggregator<>();
    aggregator.setDelimiter(",");
    aggregator.setFieldExtractor(extractor);

    FlatFileItemWriter<UserAge> writer = new FlatFileItemWriter<>();
    writer.setResource(new FileSystemResource("output_age.csv"));
    writer.setShouldDeleteIfExists(true);
    writer.setLineAggregator(aggregator);

    return writer;
  }

  @Bean
  Step dateToAgeChange(
      ItemReader<User> reader,
      ItemProcessor<User, UserAge> processor,
      ItemWriter<UserAge> writer
  ) {
    return stepBuilderFactory.get("ageCount")
        .<User, UserAge>chunk(100)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .build();
  }

  @Bean
  Job priceChangeJob(Step dateToAgeChange) {
    return jobBuilderFactory.get("changePriceJob")
        .incrementer(new RunIdIncrementer())
        .flow(dateToAgeChange)
        .end()
        .build();
  }
}
