import java.net.*;
import java.io.*;

public class UDPServer {

  public static void main(String args[]) {
    DatagramSocket aSocket = null;
    int num_messages = 0;

    try {
      aSocket = new DatagramSocket(6789);
      byte[] buffer = new byte[1000];
      byte[] response;
      String request_string;

      while (true) {
        //recebimento da mensagem
        DatagramPacket request = new DatagramPacket(buffer, buffer.length);
        aSocket.receive(request);
        request_string = new String(request.getData(), 0, request.getLength());
        //tratamento da mensagem
        String[] partes = request_string.split(",", 2);
        request_string = partes[1];
        int numeroRecebido = Integer.parseInt(partes[0]);
        if(numeroRecebido != num_messages + 1){
          request_string = "waitingfor," + (num_messages + 1);
        }
        else{
          num_messages++;
        }
        response = request_string.getBytes();
        //envio da mesagem
        DatagramPacket reply = new DatagramPacket(response,
            response.length, request.getAddress(), request.getPort());
        aSocket.send(reply);
      }
    } catch (SocketException e) { System.out.println("Socket: " + e.getMessage());
    } catch (IOException e)     { System.out.println("IO: " + e.getMessage());
    } finally { if (aSocket != null) aSocket.close(); }
  }
}