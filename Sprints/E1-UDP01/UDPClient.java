import java.net.*;
import java.io.*;
import java.util.Scanner;

public class UDPClient {
    public static void main(String args[]) {
        DatagramSocket aSocket = null;
        Scanner scanner = new Scanner(System.in);

        try {
            aSocket = new DatagramSocket();
            InetAddress aHost = InetAddress.getByName("localhost");
            int serverPort = 6789;

            System.out.println("Escolha o modo de numeracao:");
            System.out.println("1 - Modo Automatico (1, 2, 3...)");
            System.out.println("2 - Modo Manual (indica o numero de sequencia em cada envio)");
            System.out.print("Opcao: ");
            
            int modo = Integer.parseInt(scanner.nextLine());
            int seqAuto = 1;

            System.out.println("\nCliente pronto. Escreva 'sair' para terminar.");

            while (true) {
                int N;
                
                if (modo == 2) {
                    System.out.print("\nIndique o numero N da mensagem: ");
                    String nStr = scanner.nextLine();
                    if (nStr.equalsIgnoreCase("sair")) break;
                    N = Integer.parseInt(nStr);
                } else {
                    N = seqAuto;
                }

                System.out.print("Texto da mensagem: ");
                String texto = scanner.nextLine();
                if (texto.equalsIgnoreCase("sair")) break;

                // Formato: <N>,<Mensagem>
                String payload = N + "," + texto;
                byte[] m = payload.getBytes();
                DatagramPacket request = new DatagramPacket(m, m.length, aHost, serverPort);
                aSocket.send(request);

                // Receção com tamanho de payload correto
                byte[] buffer = new byte[1000];
                DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(reply);

                String resposta = new String(reply.getData(), 0, reply.getLength());

                // Interpretacao e apresentação ao utilizador
                if (resposta.startsWith("waitingfor,")) {
                    String esperado = resposta.substring(11);
                    System.out.println(">>> [ALERTA DE DESORDENACAO] O servidor rejeitou N=" + N + " e aguarda pela mensagem N=" + esperado);
                } else {
                    System.out.println(">>> [RESPOSTA ECHO] " + resposta);
                    if (modo == 1) seqAuto++;
                }
            }

        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null) aSocket.close();
            scanner.close();
        }
    }
}