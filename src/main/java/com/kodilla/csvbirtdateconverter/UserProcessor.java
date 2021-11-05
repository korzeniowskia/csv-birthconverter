package com.kodilla.csvbirtdateconverter;

import org.springframework.batch.item.ItemProcessor;

import java.time.LocalDate;

public class UserProcessor implements ItemProcessor<User, UserAge> {

  @Override
  public UserAge process(User user) {
    return new UserAge(user.getFirstName(), user.getLastName(),
        LocalDate.now().minusYears(user.getBirthDate().getYear()).getYear());
  }
}
