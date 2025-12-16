package red;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import elementos.Direcciones;
import interfaces.ControladorJuegoServidor;
import logica.LogicaJuegoServidor;

public class HiloServidor extends Thread {

    private DatagramSocket conexion;
    private int puertoServidor = 9998;
    private boolean fin = false;
    private final int MIN_JUGADORES = 2; // ✅ CAMBIO: Mínimo 2
    private final int MAX_JUGADORES = 2; // ✅ CAMBIO: Máximo 2
    private int clientesConectados = 0;
    private ArrayList<Cliente> clientes = new ArrayList<>();
    private ControladorJuegoServidor controladorJuego;
    
    private Map<String, Long> ultimoMensajeCliente = new HashMap<>();
    private static final long TIMEOUT_DESCONEXION = 10000; 
    
    private Map<Integer, String> nombresJugadores = new HashMap<>();

    public HiloServidor(ControladorJuegoServidor controladorJuego) {
        this.controladorJuego = controladorJuego;
        try {
            conexion = new DatagramSocket(puertoServidor);
            conexion.setSoTimeout(1000);
            System.out.println("Servidor iniciado en puerto " + puertoServidor);
            System.out.println("Configuracion: " + MIN_JUGADORES + "-" + MAX_JUGADORES + " jugadores");
            System.out.println("Timeout de desconexion: " + (TIMEOUT_DESCONEXION/1000) + " segundos");
        } catch (SocketException e) {
            System.err.println("Error al crear socket del servidor");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println("Servidor escuchando conexiones...");
        
        while (!fin) {
            DatagramPacket paquete = new DatagramPacket(new byte[1024], 1024);
            try {
                conexion.receive(paquete);
                procesarMensaje(paquete);
            } catch (java.net.SocketTimeoutException e) {
                verificarDesconexiones();
            } catch (IOException e) {
                if (!fin) {
                    System.err.println("Error al recibir paquete");
                }
            }
        }
        
        System.out.println("Servidor detenido");
    }
    
    private void verificarDesconexiones() {
        long tiempoActual = System.currentTimeMillis();
        
        ultimoMensajeCliente.entrySet().removeIf(entry -> {
            boolean existe = false;
            for (Cliente c : clientes) {
                if (c.obtenerId().equals(entry.getKey())) {
                    existe = true;
                    break;
                }
            }
            return !existe;
        });
        
        for (Cliente cliente : new ArrayList<>(clientes)) {
            Long ultimoMensaje = ultimoMensajeCliente.get(cliente.obtenerId());
            
            if (ultimoMensaje != null) {
                long tiempoSinMensaje = tiempoActual - ultimoMensaje;
                
                if (tiempoSinMensaje > TIMEOUT_DESCONEXION) {
                    System.err.println("Cliente " + cliente.obtenerNumero() + " (" + 
                                     obtenerNombreJugador(cliente.obtenerNumero()) + 
                                     ") sin respuesta por " + (tiempoSinMensaje/1000) + "s - eliminado");
                    eliminarCliente(cliente);
                }
            }
        }
    }
    
    private void eliminarCliente(Cliente cliente) {
        int numeroJugador = cliente.obtenerNumero();
        clientes.remove(cliente);
        clientesConectados--;
        ultimoMensajeCliente.remove(cliente.obtenerId());
        
        enviarMensajeATodos("JugadorDesconectado:" + numeroJugador);
        controladorJuego.jugadorDesconectado(numeroJugador);
        
        System.out.println("Jugadores: " + clientesConectados + "/" + MAX_JUGADORES);
    }

    private void procesarMensaje(DatagramPacket paquete) {
        String mensaje = (new String(paquete.getData())).trim();
        
        int indice = buscarIndiceCliente(paquete);
        if (indice != -1) {
            Cliente cliente = clientes.get(indice);
            ultimoMensajeCliente.put(cliente.obtenerId(), System.currentTimeMillis());
        }
        
        if (mensaje.equals("Heartbeat")) {
            return;
        }
        
        String[] partes = mensaje.split(":", 2);

        if (!mensaje.equals("Heartbeat") && !partes[0].equals("Mover")) {
            System.out.println("[Cliente #" + (indice + 1) + "] " + 
                             mensaje.substring(0, Math.min(60, mensaje.length())));
        }

        if (partes[0].equals("Conectar")) {
            manejarConexion(paquete, indice, partes.length > 1 ? partes[1] : "Jugador");
            return;
        }

        if (partes[0].equals("Desconectar")) {
            manejarDesconexion(paquete, indice);
            return;
        }

        if (indice == -1) {
            System.out.println("Mensaje de cliente desconocido ignorado");
            return;
        }

        Cliente cliente = clientes.get(indice);
        String comando = partes[0];
        String datos = partes.length > 1 ? partes[1] : "";
        
        switch (comando) {
            case "Mover":
                manejarMovimiento(cliente, datos);
                break;
            default:
                System.out.println("Comando desconocido: " + comando);
        }
    }

    private void manejarConexion(DatagramPacket paquete, int indice, String nombre) {
        if (indice != -1) {
            Cliente cliente = clientes.get(indice);
            System.out.println("Cliente ya conectado - Reconfirmando Jugador #" + cliente.obtenerNumero());
            
            ultimoMensajeCliente.put(cliente.obtenerId(), System.currentTimeMillis());
            enviarMensaje("Conectado:" + cliente.obtenerNumero() + ":" + obtenerNombreJugador(cliente.obtenerNumero()), 
                         paquete.getAddress(), paquete.getPort());
            
            enviarListaNombresATodos();
            
            if (controladorJuego.estaJuegoIniciado()) {
                enviarMensaje("Iniciar", paquete.getAddress(), paquete.getPort());
            }
            return;
        }

        if (clientesConectados >= MAX_JUGADORES) {
            System.out.println("Servidor lleno (" + MAX_JUGADORES + "/" + MAX_JUGADORES + ")");
            enviarMensaje("Lleno", paquete.getAddress(), paquete.getPort());
            return;
        }

        clientesConectados++;
        Cliente nuevoCliente = new Cliente(clientesConectados, paquete.getAddress(), paquete.getPort());
        clientes.add(nuevoCliente);
        
        String id = nuevoCliente.obtenerId();
        ultimoMensajeCliente.put(id, System.currentTimeMillis());
        
        String nombreFinal = (nombre == null || nombre.trim().isEmpty()) ? 
                            ("Jugador " + clientesConectados) : nombre.trim();
        nombresJugadores.put(clientesConectados, nombreFinal);
        
        enviarMensaje("Conectado:" + clientesConectados + ":" + nombreFinal, 
                     paquete.getAddress(), paquete.getPort());
        
        System.out.println(nombreFinal + " (#" + clientesConectados + ") conectado");
        System.out.println("Jugadores conectados: " + clientesConectados + "/" + MAX_JUGADORES);

        enviarListaNombresATodos();
        
        if (clientesConectados >= MIN_JUGADORES && !controladorJuego.estaJuegoIniciado()) {
            if (controladorJuego instanceof LogicaJuegoServidor) {
                ((LogicaJuegoServidor) controladorJuego).programarInicioJuego();
            }
        }
    }

    private void enviarListaNombresATodos() {
        StringBuilder sb = new StringBuilder("ActualizarNombres:");
        // ✅ CAMBIO: Solo 2 jugadores
        for (int i = 1; i <= MAX_JUGADORES; i++) {
            if (i > 1) sb.append("|");
            String nombre = nombresJugadores.get(i);
            sb.append(nombre != null ? nombre : "");
        }
        
        String mensaje = sb.toString();
        enviarMensajeATodos(mensaje);
    }

    private void manejarDesconexion(DatagramPacket paquete, int indice) {
        if (indice == -1) {
            System.out.println("Cliente no encontrado para desconectar");
            return;
        }

        Cliente cliente = clientes.get(indice);
        System.out.println(obtenerNombreJugador(cliente.obtenerNumero()) + " desconectado");
        
        eliminarCliente(cliente);
        
        if (clientesConectados == 0) {
            System.out.println("Todos los clientes desconectados");
            System.out.println("Esperando nuevos jugadores...");
        }
    }

    private void manejarMovimiento(Cliente cliente, String direccionStr) {
        try {
            Direcciones direccion = Direcciones.valueOf(direccionStr);
            controladorJuego.moverJugador(cliente.obtenerNumero(), direccion);
        } catch (IllegalArgumentException e) {
            System.err.println("Direccion invalida: " + direccionStr);
        }
    }

    private int buscarIndiceCliente(DatagramPacket paquete) {
        InetAddress ip = paquete.getAddress();
        int puerto = paquete.getPort();
        String idCompleto = ip.toString() + ":" + puerto;
        
        for (int i = 0; i < clientes.size(); i++) {
            if (idCompleto.equals(clientes.get(i).obtenerId())) {
                return i;
            }
        }
        
        return -1;
    }

    public void enviarMensaje(String mensaje, InetAddress ipCliente, int puertoCliente) {
        byte[] datosMensaje = mensaje.getBytes();
        DatagramPacket paquete = new DatagramPacket(datosMensaje, datosMensaje.length, ipCliente, puertoCliente);
        try {
            conexion.send(paquete);
        } catch (IOException e) {
            System.err.println("Error al enviar mensaje: " + mensaje);
        }
    }

    public void enviarMensajeATodos(String mensaje) {
        ArrayList<Cliente> clientesCopia = new ArrayList<>(clientes);
        for (Cliente cliente : clientesCopia) {
            enviarMensaje(mensaje, cliente.obtenerIp(), cliente.obtenerPuerto());
        }
    }

    public void desconectarClientesYResetear() {
        if (clientes.size() > 0) {
            enviarMensajeATodos("Desconectar");
        }
        clientes.clear();
        clientesConectados = 0;
        ultimoMensajeCliente.clear();
        nombresJugadores.clear();
        System.out.println("Todos los clientes desconectados - Servidor reseteado");
        System.out.println("Esperando nuevos jugadores...");
    }

    public void terminar() {
        this.fin = true;
        if (conexion != null && !conexion.isClosed()) {
            conexion.close();
        }
        this.interrupt();
        System.out.println("Hilo del servidor terminado");
    }

    public ArrayList<Cliente> obtenerClientes() {
        return clientes;
    }

    public int obtenerClientesConectados() {
        return clientesConectados;
    }
    
    public String obtenerNombreJugador(int numeroJugador) {
        return nombresJugadores.getOrDefault(numeroJugador, "Jugador " + numeroJugador);
    }
    
    public Map<Integer, String> obtenerTodosLosNombres() {
        return new HashMap<>(nombresJugadores);
    }
}