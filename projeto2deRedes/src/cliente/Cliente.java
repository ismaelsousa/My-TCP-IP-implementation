/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import pacote.Pacote;
import java.io.*;
import java.nio.file.Files;

/**
 *
 * @author ismae
 */
public class Cliente {

    private ArrayList<byte[]> pedacoDoArq = new ArrayList<>();
    private String hostName;
    private int porta;
    private DatagramSocket clienteUDP;
    private String caminho;

    public Cliente(String hostName, int porta, String caminho) {
        this.hostName = hostName;
        this.porta = porta;
        this.caminho = caminho;
        quebrarArquivo();
        CriarArquivo();
        try {
            clienteUDP = new DatagramSocket(porta);
        } catch (SocketException ex) {
            System.out.println("erro: não foi possivel abrir o datagramSocket no cliente na porta" + porta);
        }
    }

    public static void main(String[] args) throws IOException {
        int id;
        int numSequencia = 12345;
        int portaDoServidor;
        //criando a propria instancia da classe cliente        
        Cliente c = new Cliente("localhost", 5556, "C:\\Users\\ismae\\Google Drive\\ufc\\4 semestre\\redes\\StitchesShawnMendes.mp4");
        //vou começar enviando um pacote com o numero de sequencia, ack = 0, id=0 e SYN ativo
        Pacote pacoteDeSicro = new Pacote();
        pacoteDeSicro.setSyn(true);
        pacoteDeSicro.setSequenceNumber(numSequencia++);
        System.out.println("seq:" + pacoteDeSicro.getSequenceNumber() + " ack:" + pacoteDeSicro.getAckNumber() + " id:" + pacoteDeSicro.getConnectionID() + " syn:" + pacoteDeSicro.isSyn());
        System.out.println("------------------------------------------->");
        byte envio[] = converterPacoteEmByte(pacoteDeSicro);
        //pega o ip      
        InetAddress IPAddress = InetAddress.getByName("localhost");
        //criar o datagram para enviar para o servidor
        DatagramPacket pkt = new DatagramPacket(envio, envio.length, IPAddress, 5555);
        c.clienteUDP.send(pkt);

        ///////////////////////esperar o retorno 
        byte dataReceive[] = new byte[675];
        DatagramPacket receive = new DatagramPacket(dataReceive, dataReceive.length);
        c.clienteUDP.receive(receive);
        Pacote confirmacao = converterByteParaPacote(dataReceive);

        //passo o numerro da porta que irei usar para enviar o arq
        portaDoServidor = confirmacao.getNotUsed();
        id = confirmacao.getConnectionID();
        System.out.println("seq:" + confirmacao.getSequenceNumber() + " ack:" + confirmacao.getAckNumber() + " id:" + confirmacao.getConnectionID() + " ack:" + confirmacao.isAck() + " syn:" + confirmacao.isSyn());
        System.out.println("<-------------------------------------------");

        ////////////////////////////// envia o ack de confirmação
        Pacote ackDeSyn = new Pacote();
        ackDeSyn.setAck(true);
        ackDeSyn.setSequenceNumber(numSequencia);
        ackDeSyn.setAckNumber(confirmacao.getSequenceNumber() + 1);
        ackDeSyn.setConnectionID(id);

        byte ack[] = converterPacoteEmByte(ackDeSyn);

        DatagramPacket Dack = new DatagramPacket(ack, ack.length, IPAddress, portaDoServidor);
        c.clienteUDP.send(Dack);

        System.out.println("seq:" + ackDeSyn.getSequenceNumber() + " ack:" + ackDeSyn.getAckNumber() + " id:" + ackDeSyn.getConnectionID() + " ack:" + ackDeSyn.isAck());
        System.out.println("------------------------------------------->");
    }

    public static void paraArquivo(byte dado[], File destino) {
        try {
            FileOutputStream fos = new FileOutputStream(destino);
            fos.write(dado);
            fos.close();
        } catch (FileNotFoundException ex) {
            System.out.println("erro ao cria o fileOutputStream");
        } catch (IOException ex) {
            System.out.println("erro ao escrever o arquivo");
        }

    }

    public void CriarArquivo() {
        File test = new File("teste.mp4");
        
        byte junto[] = new byte[pedacoDoArq.size()*512];
        int i = 0;
        int posicao= 0;
        int posiDoPedaco=0;
        
        while (i < pedacoDoArq.size()) {
            for (int j = 0; j < pedacoDoArq.get(i).length; j++) {
                junto[posicao] = pedacoDoArq.get(i)[j];
                posicao++;
            }            
            i++;                        
        }
        
            try {
                Files.write(test.toPath(), junto);
                System.out.println("salvei o pedaco:"+ i );
                i++;
            } catch (IOException ex) {
                System.out.println("erro ao tentar criar arquivo");
            }
        
    }

    public void quebrarArquivo() {
        try {
            FileInputStream in = new FileInputStream(new File(caminho));
            BufferedInputStream bufferMusica = new BufferedInputStream(in);
            int n = 0;
            int i = 0;

            while (n != -1) {
                byte[] byteDoArquivo = new byte[512];
                n = bufferMusica.read(byteDoArquivo);
                pedacoDoArq.add(byteDoArquivo);
                System.out.println("quebrei pedaço do arquivo:" + i);
                i++;
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Erro ao pegar o arquivo" + ex);
        } catch (IOException ex) {
            System.out.println("Erro IO" + ex);
        }

    }

    private static Pacote converterByteParaPacote(byte[] pacote) {

        try {
            ByteArrayInputStream bao = new ByteArrayInputStream(pacote);
            ObjectInputStream ous;
            ous = new ObjectInputStream(bao);
            return (Pacote) ous.readObject();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    private static byte[] converterPacoteEmByte(Pacote pkt) {
        try {
            //cria um  array de byte  que irei passar para o objectOutput para retornar o byte[] , 
            //o pacote tem q implementar o Serializable
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            ObjectOutputStream ous;
            ous = new ObjectOutputStream(bao);
            ous.writeObject(pkt);
            return bao.toByteArray();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println("erro ao converte pacote em byte");
            e.printStackTrace();
        }

        return null;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getPorta() {
        return porta;
    }

    public void setPorta(int porta) {
        this.porta = porta;
    }

    public String getCaminho() {
        return caminho;
    }

    public void setCaminho(String caminho) {
        this.caminho = caminho;
    }

    public DatagramSocket getClienteUDP() {
        return clienteUDP;
    }

    public void setClienteUDP(DatagramSocket clienteUDP) {
        this.clienteUDP = clienteUDP;
    }

}
