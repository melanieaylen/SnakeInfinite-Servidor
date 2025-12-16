package red;

import java.net.InetAddress;

public class Cliente {

    private String id;
    private int numero;
    private InetAddress ip;
    private int puerto;

    public Cliente(int numero, InetAddress ip, int puerto) {
        this.numero = numero;
        this.id = ip.toString() + ":" + puerto;
        this.ip = ip;
        this.puerto = puerto;
    }

    public void actualizarPuerto(int nuevoPuerto) {
        this.puerto = nuevoPuerto;
        this.id = ip.toString() + ":" + nuevoPuerto;
    }
    
    public String obtenerId() {
        return id;
    }

    public InetAddress obtenerIp() {
        return ip;
    }

    public int obtenerPuerto() {
        return puerto;
    }

    public int obtenerNumero() {
        return numero;
    }

    @Override
    public String toString() {
        return "Cliente " + numero + " [" + ip.getHostAddress() + ":" + puerto + "]";
    }
}