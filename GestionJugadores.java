package com.mongodb.gestion.jugadores;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import static com.mongodb.client.model.Filters.eq;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import java.util.Scanner;
import org.bson.Document;
import org.bson.conversions.Bson;

public class GestionJugadores {

    static InsertOneResult insertOneResult;
    static MongoCollection<Document> coleccion;
    static Scanner t = new Scanner(System.in);
    static String nombre = "", posicion = "";
    static int edad = 0, dorsal = 0, eleccion = 0;

    public static void main(String[] args) {
        //Para silenciar
        System.setProperty("org.mongodb.level", "WARNING");
        // Cadena de conexión con la base de datos MongoDB
        String uri = "mongodb://localhost:27017";
        boolean terminar = true;

        try (MongoClient mongoClient = MongoClients.create(uri)) {
            // Obtener o crear una base de datos MongoDB desde Java
            MongoDatabase database = mongoClient.getDatabase("equipoFutbol");
            System.out.println("Base de datos creada o conectada.");
            // Obtener o crear una colección MongoDB
            coleccion = database.getCollection("Jugadores");
            System.out.println("Colección creada o conectada.");

            while (terminar) {
                System.out.println("Bienvenido mister, haz creado tu equipo de fútbol");
                System.out.println("Ahora tienes que fichar a tus jugadores y asignarles su posición,");
                System.out.println("actualizar sus datos o despedirlos.");
                System.out.println("1: Registrar nuevo jugador.");
                System.out.println("2: Mostrar todos los jugadores.");
                System.out.println("3: Actualizar datos de un jugador.");
                System.out.println("4: Despedir jugador.");
                System.out.println("5: Buscar jugador por posición.");
                System.out.println("6: Salir.");
                System.out.print("Selecciona una de estas opciones (escoge un número): ");
                eleccion = t.nextInt();
                t.nextLine(); // Limpiar buffer

                switch(eleccion){
                    case 1:
                        registrarJugador();
                        break;
                    case 2:
                        listarJugadores();
                        break;
                    case 3:
                        actualizarJugador();
                        break;
                    case 4:
                        despedirJugador();
                        break;
                    case 5:
                        buscarPorPosicion();
                        break;
                    case 6:
                        terminar = false;
                        System.out.println("Saliendo del programa...");
                        break;
                    default:
                        System.out.println("Selecciona una de esas 6 opciones.");
                }
            }

        } catch (Exception ex) {
            System.out.println("Error con MongoDB: " + ex.getMessage());
        }
    }

    public static void registrarJugador() {
        System.out.print("Nombre del jugador a fichar: ");
        nombre = t.nextLine();

        System.out.print("Edad de " + nombre + ": ");
        edad = t.nextInt();
        t.nextLine(); // Limpiar buffer

        System.out.print("Posición de " + nombre + ": ");
        posicion = t.nextLine();

        System.out.print("Dorsal de " + nombre + ": ");
        dorsal = t.nextInt();
        t.nextLine(); // Limpiar buffer

        if (esNumeroRepetido(dorsal)) {
            System.out.println("Este dorsal ya está en uso. Por favor, escoge otro.");
            return;
        }

        // Crear documento y guardarlo
        Document data = new Document()
                .append("Nombre", nombre)
                .append("Edad", edad)
                .append("Posicion", posicion)
                .append("Dorsal", dorsal);
        coleccion.insertOne(data);
        System.out.println("¡Jugador fichado!");
    }

    private static boolean esNumeroRepetido(int dorsal) {
        Document query = new Document("Dorsal", dorsal);
        return coleccion.countDocuments(query) > 0;
    }

    public static void listarJugadores(){
        FindIterable<Document> jugadores = coleccion.find();

        System.out.println("\n--- Lista de Jugadores ---");
        for(Document jugador : jugadores){
            mostrarJugador(jugador);
        }
    }

    private static void mostrarJugador(Document jugador) {
        System.out.println("Nombre: " + jugador.getString("Nombre"));
        System.out.println("Posición: " + jugador.getString("Posicion"));
        System.out.println("Edad: " + jugador.getInteger("Edad"));
        System.out.println("Dorsal: " + jugador.getInteger("Dorsal"));
        System.out.println("--------------------------");
    }

    public static void actualizarJugador(){
        System.out.print("Nombre del jugador a actualizar: ");
        nombre = t.nextLine();

        Document actualizar = new Document("Nombre", nombre);
        Document jugador = coleccion.find(actualizar).first();

        if(jugador == null){
            System.out.println("No se encontró el jugador, por favor introduce otro nombre.");
            return;
        }

        System.out.println("------ Datos actuales del jugador -------");
        mostrarJugador(jugador);

        System.out.println("¿Qué dato quieres actualizar?");
        System.out.println("1: Edad.");
        System.out.println("2: Posición.");
        System.out.println("3: Dorsal.");
        System.out.print("Elige una opción: ");
        eleccion = t.nextInt();
        t.nextLine(); // Limpiar buffer

        switch(eleccion) {
            case 1:
                System.out.print("Nueva edad: ");
                int nuevaEdad = t.nextInt();
                t.nextLine();
                coleccion.updateOne(
                    eq("Nombre", nombre),
                    Updates.set("Edad", nuevaEdad)
                );
                break;

            case 2:
                System.out.print("Nueva posición: ");
                String nuevaPosicion = t.nextLine();
                coleccion.updateOne(
                    eq("Nombre", nombre),
                    Updates.set("Posicion", nuevaPosicion)
                );
                break;

            case 3:
                System.out.print("Nuevo número de camiseta: ");
                int nuevoDorsal = t.nextInt();
                t.nextLine();

                if (esNumeroRepetido(nuevoDorsal)) {
                    System.out.println("Este número de camiseta ya está asignado. No se ha actualizado.");
                    return;
                }

                coleccion.updateOne(
                    eq("Nombre", nombre),
                    Updates.set("Dorsal", nuevoDorsal)
                );
                break;

            default:
                System.out.println("Opción no válida.");
                return;
        }

        System.out.println("¡Jugador actualizado con éxito!");
    }

    private static void despedirJugador() {
        System.out.print("Nombre del jugador a despedir: ");
        nombre = t.nextLine();

        Document nom = new Document("Nombre", nombre);
        Document jugador = coleccion.find(nom).first();

        if (jugador == null) {
            System.out.println("No se encontró ningún jugador con ese nombre.");
            return;
        }

        System.out.println("Datos del jugador a eliminar:");
        mostrarJugador(jugador);

        System.out.print("¿Estás seguro de que deseas despedir a este jugador? (S/N): ");
        String confirmacion = t.nextLine();

        if(confirmacion.equalsIgnoreCase("S")){
            coleccion.deleteOne(nom);
            System.out.println("¡Jugador eliminado con éxito!");
        } else {
            System.out.println("Operación cancelada.");
        }
    }

    private static void buscarPorPosicion() {
        System.out.print("Posición a buscar: ");
        posicion = t.nextLine();

        Document pos = new Document("Posicion", posicion);
        FindIterable<Document> jugadores = coleccion.find(pos);

        boolean hayJugadores = false;
        System.out.println("\nJugadores en la posición '" + posicion + "':");

        for(Document jugador : jugadores){
            mostrarJugador(jugador);
            hayJugadores = true;
        }

        if(!hayJugadores){
            System.out.println("No hay jugadores registrados en esa posición.");
        }
    }
}