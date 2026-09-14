import java.net.*;
import java.io.*;

public class UDPServer {
    public static void main(String args[]) {
        DatagramSocket aSocket = null;
        int L = 0; 

        try {
            aSocket = new DatagramSocket(6789);
            byte[] buffer = new byte[1000];
            System.out.println("Servidor UDP iniciado no porto 6789. Estado inicial L = " + L);

            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);

                String receivedStr = new String(request.getData(), 0, request.getLength()).trim();
                System.out.println("\n[Servidor] Recebido: \"" + receivedStr + "\" | Estado atual L = " + L);

                String responseText;

                int commaIndex = receivedStr.indexOf(',');
                if (commaIndex == -1) {
                    responseText = "erro,formato_invalido";
                } else {
                    try {
                        int N = Integer.parseInt(receivedStr.substring(0, commaIndex));
                        String msgPayload = receivedStr.substring(commaIndex + 1);

                        // Regra de decisão
                        if (N == L + 1) {
                            L = N; // Atualiza estado
                            responseText = N + "," + msgPayload; // Echo
                            System.out.println("[Servidor] MENSAGEM ACEITA. Novo L = " + L);
                        } else {
                            int expected = L + 1;
                            responseText = "waitingfor," + expected;
                            System.out.println("[Servidor] REJEITADA (esperado " + expected + ", recebido " + N + "). L mantem-se = " + L);
                        }
                    } catch (NumberFormatException e) {
                        responseText = "erro,numero_invalido";
                    }
                }

                byte[] replyBytes = responseText.getBytes();
                DatagramPacket reply = new DatagramPacket(
                    replyBytes,
                    replyBytes.length,
                    request.getAddress(),
                    request.getPort()
                );
                aSocket.send(reply);
            }
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null) aSocket.close();
        }
    }
}