package com.kodilla.csvbirtdateconverter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAge {
  private String firstName;
  private String lastName;
  private int age;
}
