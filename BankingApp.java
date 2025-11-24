// Java Socket-Based Banking ATM Simulation
// Server asks client questions after withdrawal (like a real ATM)

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class BankingApp {

    // ------------------- SERVER (ATM MACHINE) -------------------
    static class BankServer {
        private ServerSocket serverSocket;
        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private int balance = 5000; // default balance

        public BankServer(int port, JTextArea display) {
            new Thread(() -> {
                try {
                    serverSocket = new ServerSocket(port);
                    display.append("ATM Server started... waiting for user...\n");
                    client = serverSocket.accept();
                    display.append("User connected!\n\n");

                    in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    out = new PrintWriter(client.getOutputStream(), true);

                    out.println("Welcome to ATM! Enter amount to withdraw:");

                    String msg;
                    while ((msg = in.readLine()) != null) {

                        if (msg.startsWith("WITHDRAW:")) {
                            int amount = Integer.parseInt(msg.split(":")[1]);
                            if (amount <= balance) {
                                balance -= amount;
                                out.println("Withdrawal successful! Amount: " + amount);
                                out.println("Do you want to check your balance? YES/NO");
                            } else {
                                out.println("Insufficient balance!");
                                out.println("Try again or type EXIT");
                            }
                        }

                        else if (msg.equalsIgnoreCase("YES")) {
                            out.println("Your current balance is: " + balance);
                            out.println("Do you want another transaction? YES/NO");
                        }

                        else if (msg.equalsIgnoreCase("NO")) {
                            out.println("Thank you for using ATM. Goodbye!");
                            break;
                        }

                        else {
                            out.println("Invalid input. Try again.");
                        }

                        display.append("Client: " + msg + "\n");
                    }

                } catch (Exception e) {
                    display.append("Error: " + e.getMessage() + "\n");
                }
            }).start();
        }
    }

    // ------------------- CLIENT (ATM USER) -------------------
    static class BankClient {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public BankClient(String host, int port, JTextArea display) {
            new Thread(() -> {
                try {
                    socket = new Socket(host, port);
                    display.append("Connected to ATM Server!\n");

                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = new PrintWriter(socket.getOutputStream(), true);

                    String msg;
                    while ((msg = in.readLine()) != null) {
                        display.append("ATM: " + msg + "\n");
                    }

                } catch (Exception e) {
                    display.append("Error: " + e.getMessage() + "\n");
                }
            }).start();
        }

        public void send(String msg) {
            if (out != null) out.println(msg);
        }
    }

    // ------------------- UI -------------------
    public static void main(String[] args) {
        JFrame frame = new JFrame("ATM Banking System");
        frame.setSize(600, 650);
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Color bg = new Color(230,240,255);
        Color accent = new Color(0,120,215);
        Color green = new Color(0,180,75);
        Color dark = new Color(40,40,40);

        frame.getContentPane().setBackground(bg);

        JTextArea display = new JTextArea();
        display.setEditable(false);
        display.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        display.setBackground(Color.WHITE);
        display.setForeground(dark);
        display.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        JScrollPane scroll = new JScrollPane(display);
        scroll.setBorder(BorderFactory.createLineBorder(accent,3));

        JTextField input = new JTextField();
        input.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        input.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent,2),
                BorderFactory.createEmptyBorder(8,8,8,8)));

        JButton send = new JButton("Send ➤");
        send.setBackground(accent);
        send.setForeground(Color.WHITE);
        send.setFont(new Font("Segoe UI", Font.BOLD, 16));
        send.setFocusPainted(false);
        send.setBorder(BorderFactory.createEmptyBorder(10,20,10,20));
        send.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(bg);
        bottom.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        bottom.add(input, BorderLayout.CENTER);
        bottom.add(send, BorderLayout.EAST);

        String[] modes = {"Server","Client"};
        JComboBox<String> modeSelect = new JComboBox<>(modes);
        modeSelect.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JTextField hostField = new JTextField("localhost");
        JTextField portField = new JTextField("6000");

        JButton connect = new JButton("Start");
        connect.setBackground(green);
        connect.setForeground(Color.WHITE);
        connect.setFont(new Font("Segoe UI", Font.BOLD, 16));
        connect.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        connect.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel top = new JPanel(new GridLayout(1,4,10,0));
        top.setBackground(bg);
        top.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        top.add(modeSelect);
        top.add(hostField);
        top.add(portField);
        top.add(connect);

        frame.add(top,BorderLayout.NORTH);
        frame.add(scroll,BorderLayout.CENTER);
        frame.add(bottom,BorderLayout.SOUTH);

        final BankServer[] server = new BankServer[1];
        final BankClient[] client = new BankClient[1];

        connect.addActionListener(e->{
            String mode = (String) modeSelect.getSelectedItem();
            int port = Integer.parseInt(portField.getText());
            if(mode.equals("Server")) server[0] = new BankServer(port,display);
            else client[0] = new BankClient(hostField.getText(),port,display);
        });

        send.addActionListener(e->{
            String text = input.getText().trim();
            if(text.isEmpty()) return;
            if(client[0] != null){
                if(text.matches("[0-9]+")) client[0].send("WITHDRAW:"+text);
                else client[0].send(text.toUpperCase());
            }
            display.append("You: "+text+"\n");
            input.setText("");
        });

        frame.setVisible(true);
    }
}
