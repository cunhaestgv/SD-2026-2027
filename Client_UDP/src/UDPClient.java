import java.net.*;
import java.io.*;
import java.util.Scanner;

public class UDPClient {

  public static void main(String args[]) {
    DatagramSocket aSocket = null;
    int message_number = 1;

    try {
      aSocket = new DatagramSocket();
      Scanner scanner = new Scanner(System.in);
      String user_option = "0";

      do{
        System.out.print("Escolha o método de númeração de mensagens(1-automático, 2-manual): ");
        user_option = scanner.nextLine();
      }while(!user_option.equals("1") && !user_option.equals("2"));

      while (true) {
        System.out.print("Escreva a sua mensagem para o servidor: ");
        String user_text = scanner.nextLine();
        if(user_option.equals("2")){
            System.out.print("Escreva o número: ");
            message_number = Integer.parseInt(scanner.nextLine());
        }
        user_text = message_number + "," + user_text;
        byte[] m = user_text.getBytes();

        InetAddress aHost = InetAddress.getByName("localhost");
        int serverPort = 6789;

        DatagramPacket request = new DatagramPacket(m, m.length, aHost, serverPort);

        aSocket.send(request);

        message_number++;

        byte[] buffer = new byte[1000];

        DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

        aSocket.receive(reply);

        System.out.println("Reply: " + new String(reply.getData()));
      }

    } catch (SocketException e) {
      System.out.println("Socket: " + e.getMessage());
    } catch (IOException e) {
      System.out.println("IO: " + e.getMessage());
    } finally {
      if (aSocket != null)
        aSocket.close();
    }
  }
}