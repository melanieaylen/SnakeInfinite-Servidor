package red;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

import elementos.Direcciones;
import interfaces.ControladorJuegoServidor;

/**
 * HiloServidor - Hilo del servidor que maneja la red
 */
public class HiloServidor extends Thread {

    private DatagramSocket conexion;
    private int puertoServidor = 9998;
    private boolean fin = false;
    private final int MAX_CLIENTES = 2;
    private int clientesConectados = 0;
    private ArrayList<Cliente> clientes = new ArrayList<>();
    private ControladorJuegoServidor controladorJuego;

    public HiloServidor(ControladorJuegoServidor controladorJuego) {
        this.controladorJuego = controladorJuego;
        try {
            conexion = new DatagramSocket(puertoServidor);
            System.out.println("✅ Servidor iniciado en puerto " + puertoServidor);
            System.out.println("📡 Esperando conexiones de clientes...");
        } catch (SocketException e) {
            System.err.println("❌ Error al crear socket del servidor");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println("👂 Servidor escuchando conexiones...");
        do {
            DatagramPacket paquete = new DatagramPacket(new byte[1024], 1024);
            try {
                conexion.receive(paquete);
                procesarMensaje(paquete);
            } catch (IOException e) {
                if (!fin) {
                    System.err.println("⚠️ Error al recibir paquete");
                }
            }
        } while (!fin);
        System.out.println("✅ Servidor detenido");
    }

    /**
     * Procesa los mensajes recibidos de los clientes
     */
    private void procesarMensaje(DatagramPacket paquete) {
        String mensaje = (new String(paquete.getData())).trim();
        String[] partes = mensaje.split(":");
        int indice = buscarIndiceCliente(paquete);

        System.out.println("📨 [" + paquete.getAddress().getHostAddress() + "] " + mensaje);

        // Comando Conectar
        if (partes[0].equals("Conectar")) {
            manejarConexion(paquete, indice);
            return;
        }

        // Comando Desconectar
        if (partes[0].equals("Desconectar")) {
            manejarDesconexion(paquete, indice);
            return;
        }

        // Si el cliente no está conectado, ignorar otros mensajes
        if (indice == -1) {
            System.out.println("⚠️ Cliente no conectado, ignorando mensaje");
            enviarMensaje("NoConectado", paquete.getAddress(), paquete.getPort());
            return;
        }

        // Otros comandos (requieren estar conectado)
        Cliente cliente = clientes.get(indice);
        switch (partes[0]) {
            case "Mover":
                if (partes.length >= 2) {
                    manejarMovimiento(cliente, partes[1]);
                }
                break;
            default:
                System.out.println("⚠️ Comando desconocido: " + partes[0]);
        }
    }

    /**
     * Maneja la conexión de un nuevo cliente
     */
    private void manejarConexion(DatagramPacket paquete, int indice) {
        // Ya está conectado
        if (indice != -1) {
            System.out.println("⚠️ Cliente ya conectado");
            enviarMensaje("YaConectado", paquete.getAddress(), paquete.getPort());
            return;
        }

        // Servidor lleno
        if (clientesConectados >= MAX_CLIENTES) {
            System.out.println("⚠️ Servidor lleno (2/2 jugadores)");
            enviarMensaje("Lleno", paquete.getAddress(), paquete.getPort());
            return;
        }

        // Conectar nuevo cliente
        clientesConectados++;
        Cliente nuevoCliente = new Cliente(clientesConectados, paquete.getAddress(), paquete.getPort());
        clientes.add(nuevoCliente);
        enviarMensaje("Conectado:" + clientesConectados, paquete.getAddress(), paquete.getPort());
        
        System.out.println("✅ " + nuevoCliente + " conectado desde " + 
                         paquete.getAddress().getHostAddress());
        System.out.println("📊 Jugadores conectados: " + clientesConectados + "/" + MAX_CLIENTES);

        // Si hay 2 jugadores, iniciar el juego
        if (clientesConectados == MAX_CLIENTES) {
            System.out.println("🎮 ¡Todos los jugadores conectados! Iniciando partida...");
            enviarMensajeATodos("Iniciar");
            controladorJuego.iniciarJuego();
        }
    }

    /**
     * Maneja la desconexión de un cliente
     */
    private void manejarDesconexion(DatagramPacket paquete, int indice) {
        if (indice == -1) {
            return;
        }

        Cliente cliente = clientes.get(indice);
        int numeroJugador = cliente.obtenerNumero();
        clientes.remove(indice);
        clientesConectados--;

        System.out.println("🔌 " + cliente + " desconectado");
        enviarMensajeATodos("JugadorDesconectado:" + numeroJugador);
        controladorJuego.jugadorDesconectado(numeroJugador);
    }

    /**
     * Maneja el movimiento de un jugador
     */
    private void manejarMovimiento(Cliente cliente, String direccionStr) {
        try {
            Direcciones direccion = Direcciones.valueOf(direccionStr);
            controladorJuego.moverJugador(cliente.obtenerNumero(), direccion);
        } catch (IllegalArgumentException e) {
            System.err.println("⚠️ Dirección inválida: " + direccionStr);
        }
    }

    /**
     * Encuentra el índice de un cliente en la lista
     */
    private int buscarIndiceCliente(DatagramPacket paquete) {
        String id = paquete.getAddress().toString() + ":" + paquete.getPort();
        for (int i = 0; i < clientes.size(); i++) {
            if (id.equals(clientes.get(i).obtenerId())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Envía un mensaje a un cliente específico
     */
    public void enviarMensaje(String mensaje, InetAddress ipCliente, int puertoCliente) {
        byte[] datosMensaje = mensaje.getBytes();
        DatagramPacket paquete = new DatagramPacket(datosMensaje, datosMensaje.length, ipCliente, puertoCliente);
        try {
            conexion.send(paquete);
            // System.out.println("📤 Enviado a " + ipCliente.getHostAddress() + ": " + mensaje);
        } catch (IOException e) {
            System.err.println("❌ Error al enviar mensaje: " + mensaje);
        }
    }

    /**
     * Envía un mensaje a todos los clientes conectados
     */
    public void enviarMensajeATodos(String mensaje) {
        for (Cliente cliente : clientes) {
            enviarMensaje(mensaje, cliente.obtenerIp(), cliente.obtenerPuerto());
        }
    }

    /**
     * Desconecta todos los clientes
     */
    public void desconectarClientes() {
        enviarMensajeATodos("Desconectar");
        clientes.clear();
        clientesConectados = 0;
        System.out.println("✅ Todos los clientes desconectados");
    }

    /**
     * Termina el servidor
     */
    public void terminar() {
        this.fin = true;
        if (conexion != null && !conexion.isClosed()) {
            conexion.close();
        }
        this.interrupt();
        System.out.println("✅ Hilo del servidor terminado");
    }

    /**
     * Obtiene la lista de clientes conectados
     */
    public ArrayList<Cliente> obtenerClientes() {
        return clientes;
    }

    /**
     * Obtiene el número de clientes conectados
     */
    public int obtenerClientesConectados() {
        return clientesConectados;
    }
}