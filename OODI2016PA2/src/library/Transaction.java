package library;
/**
 * 
 * @author Brahma Dathan and Sarnath Ramnath
 * @Copyright (c) 2010
 
 * Redistribution and use with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - the use is for academic purpose only
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   - Neither the name of Brahma Dathan or Sarnath Ramnath
 *     may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS"AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  
 */
import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;
/**
 * Represents a single Transaction (issue, renew, etc.)
 * 
 * @author Brahma Dathan
 *
 */
public class Transaction implements Serializable {
  private static final long serialVersionUID = 1L;
  private String type;
  private String title;
  private Calendar date;
  /**
   * Creates the transaction with a given type and book title.
   * The date is the current date.
   * @param type The type of transaction
   * @param title The title of the book
   * 
   */
  public Transaction (String type, String title) {
    this.type = type;
    this.title = title;
    date = new GregorianCalendar();
    date.setTimeInMillis(System.currentTimeMillis());
  }
  /**
   * Checks whether this transaction is on the given date
   * 
   * @param date The date for which transactions are being sought
   * @return true iff the dates match
   */
  public boolean onDate(Calendar date) {
    return ((date.get(Calendar.YEAR) == this.date.get(Calendar.YEAR)) &&
            (date.get(Calendar.MONTH) == this.date.get(Calendar.MONTH)) &&
            (date.get(Calendar.DATE) == this.date.get(Calendar.DATE)));
  }
  /**
   * Returns the type field
   * @return type field
   */
  public String getType() {
    return type;
  }
  /**
   * Returns the title field
   * @return title field
   */
  public String getTitle() {
    return title;
  }
  /**
   * Returns the date as a String
   * @return date with month, date, and year
   */
  public String getDate() {
    return date.get(Calendar.MONTH) + "/" + date.get(Calendar.DATE) + "/" + date.get(Calendar.YEAR);
  }
  /**
   * String form of the transaction
   * 
   */
  @Override
  public String toString(){
    return (type + "   " + title);
  }
}