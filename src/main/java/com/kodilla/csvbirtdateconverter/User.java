package com.kodilla.csvbirtdateconverter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
  private String firstName;
  private String lastName;
  private LocalDate birthDate;
}
