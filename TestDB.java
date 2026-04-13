package com.test;

import java.sql.*;
import java.util.Scanner;

public class TestDB {
    public static void main(String[] args) {
        try {
            Scanner sc = new Scanner(System.in);

            Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/railway_db", "root", ""
            );

            Statement stmt = con.createStatement();

            System.out.print("Enter username: ");
            String name = sc.nextLine();

            stmt.executeUpdate(
                "INSERT INTO users (name, password) VALUES ('" + name + "', '123')"
            );

            while (true) {
                System.out.println("\n1.Search Train");
                System.out.println("2.Book Ticket");
                System.out.println("3.Cancel Ticket");
                System.out.println("4.View Tickets");
                System.out.println("5.Exit");

                int choice = sc.nextInt();

                if (choice == 1) {
                    sc.nextLine();
                    System.out.print("Enter source: ");
                    String src = sc.nextLine();

                    PreparedStatement ps = con.prepareStatement(
                        "SELECT * FROM trains WHERE source=?"
                    );
                    ps.setString(1, src);

                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {
                        System.out.println(rs.getInt("train_id") + " " +
                                rs.getString("source") + " " +
                                rs.getString("destination") +
                                " Seats:" + rs.getInt("seats_available"));
                    }

                } else if (choice == 2) {
                    sc.nextLine();

                    System.out.print("Enter passenger name: ");
                    String pname = sc.nextLine();

                    System.out.print("Enter age: ");
                    int age = sc.nextInt();

                    sc.nextLine();
                    System.out.print("Enter gender: ");
                    String gender = sc.nextLine();

                    stmt.executeUpdate(
                        "INSERT INTO passengers (name, age, gender) VALUES ('" + pname + "', " + age + ", '" + gender + "')"
                    );

                    ResultSet pRs = stmt.executeQuery("SELECT LAST_INSERT_ID() as pid");
                    pRs.next();
                    int pid = pRs.getInt("pid");

                    System.out.print("Enter train id: ");
                    int id = sc.nextInt();

                    ResultSet rs = stmt.executeQuery(
                        "SELECT seats_available FROM trains WHERE train_id=" + id
                    );

                    if (rs.next()) {
                        int seats = rs.getInt("seats_available");

                        if (seats > 0) {
                            stmt.executeUpdate(
                                "UPDATE trains SET seats_available=seats_available-1 WHERE train_id=" + id
                            );

                            stmt.executeUpdate(
                                "INSERT INTO bookings (user_id, train_id) VALUES (1," + id + ")"
                            );

                            stmt.executeUpdate(
                                "INSERT INTO tickets (user_id, train_id, status) VALUES (1," + id + ",'CONFIRMED')"
                            );

                            System.out.println("Ticket Booked");
                        } else {
                            stmt.executeUpdate(
                                "INSERT INTO waiting_list (train_id, user_id, position) VALUES (" + id + ",1,1)"
                            );

                            stmt.executeUpdate(
                                "INSERT INTO tickets (user_id, train_id, status) VALUES (1," + id + ",'WAITING')"
                            );

                            System.out.println("Added to Waiting List");
                        }
                    }

                } else if (choice == 3) {
                    System.out.print("Enter ticket id: ");
                    int tid = sc.nextInt();

                    ResultSet rs = stmt.executeQuery(
                        "SELECT train_id FROM tickets WHERE ticket_id=" + tid
                    );

                    if (rs.next()) {
                        int trainId = rs.getInt("train_id");

                        stmt.executeUpdate(
                            "DELETE FROM tickets WHERE ticket_id=" + tid
                        );

                        stmt.executeUpdate(
                            "UPDATE trains SET seats_available=seats_available+1 WHERE train_id=" + trainId
                        );

                        ResultSet rs2 = stmt.executeQuery(
                            "SELECT ticket_id FROM tickets WHERE status='WAITING' AND train_id=" + trainId + " LIMIT 1"
                        );

                        if (rs2.next()) {
                            int wid = rs2.getInt("ticket_id");

                            stmt.executeUpdate(
                                "UPDATE tickets SET status='CONFIRMED' WHERE ticket_id=" + wid
                            );

                            stmt.executeUpdate(
                                "UPDATE trains SET seats_available=seats_available-1 WHERE train_id=" + trainId
                            );

                            System.out.println("Waiting ticket auto-confirmed");
                        }

                        System.out.println("Ticket Cancelled");
                    } else {
                        System.out.println("Invalid Ticket ID");
                    }

                } else if (choice == 4) {
                    ResultSet rs = stmt.executeQuery("SELECT * FROM tickets");

                    while (rs.next()) {
                        System.out.println(rs.getInt("ticket_id") + " " +
                                rs.getInt("train_id") + " " +
                                rs.getString("status"));
                    }

                } else if (choice == 5) {
                    break;
                } else {
                    System.out.println("Invalid choice");
                }
            }

            con.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}