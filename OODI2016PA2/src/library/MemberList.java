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
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
/**
 * The collection class for Member objects
 * @author Brahma Dathan and Sarnath Ramnath
 *
 */
public class MemberList implements Serializable {
  private static final long serialVersionUID = 1L;
  private List<Member> members = new LinkedList<Member>();
  private static MemberList memberList;
  /*
   * Private constructor for singleton pattern
   * 
   */
  private MemberList() {
  }
  /**
   * Supports the singleton pattern
   * 
   * @return the singleton object
   */
  public static MemberList instance() {
    if (memberList == null) {
      return (memberList = new MemberList());
    } else {
      return memberList;
    }
  }
  /**
   * Checks whether a member with a given member id exists.
   * @param memberId the id of the member
   * @return true iff member exists
   * 
   */
  public Member search(String memberId) {
    for (Iterator<Member> iterator = members.iterator(); iterator.hasNext(); ) {
      Member member = (Member) iterator.next();
      if (member.getId().equals(memberId)) {
        return member;
      }
    }
    return null;
  }
  /**
   * Inserts a member into the collection
   * @param member the member to be inserted
   * @return true iff the member could be inserted. Currently always true
   */
  public boolean insertMember(Member member) {
    members.add(member);
    return true;
  }
  /*
   * Supports serialization
   * @param output the stream to be written to
   */
  private void writeObject(java.io.ObjectOutputStream output) {
    try {
      output.defaultWriteObject();
      output.writeObject(memberList);
    } catch(IOException ioe) {
      ioe.printStackTrace();
    }
  }
  /*
   * Supports serialization
   *  @param input the stream to be read from
   */
  private void readObject(java.io.ObjectInputStream input) {
    try {
      if (memberList != null) {
        return;
      } else {
        input.defaultReadObject();
        if (memberList == null) {
          memberList = (MemberList) input.readObject();
        } else {
          input.readObject();
        }
      }
    } catch(IOException ioe) {
      ioe.printStackTrace();
    } catch(ClassNotFoundException cnfe) {
      cnfe.printStackTrace();
    }
  }
  /** String form of the collection
  * 
  */
  @Override
  public String toString() {
    return members.toString();
  }
}