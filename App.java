import java.io.*;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Imagen imagenModificada = null;

        while (true) {
            System.out.println("Menú:");
            System.out.println("0. Salir");
            System.out.println("1. Generar referencias");
            System.out.println("2. Calcular hits y fallas de página");
            System.out.println("3. Esconder mensaje en una imagen");
            System.out.println("4. Recuperar mensaje de una imagen");
            System.out.print("Seleccione una opción: ");
            int opcion = scanner.nextInt();

            switch (opcion) {
                case 0:
                    System.out.println("Saliendo del programa.");
                    scanner.close();
                    System.exit(0);
                    break;
                case 1:
                    System.out.print("Ingrese el tamaño de página: ");
                    int tamanoPagina = scanner.nextInt();
                    System.out.print("Ingrese el nombre del archivo BMP con el mensaje escondido: ");
                    String archivoImagen = scanner.next();
                    imagenModificada = new Imagen(archivoImagen);

                    generarReferencias(imagenModificada, tamanoPagina);
                    break;

                case 2:
                    System.out.print("Ingrese el número de marcos de página: ");
                    int numMarcos = scanner.nextInt();
                    System.out.print("Ingrese el nombre del archivo de referencias: ");
                    String archivoReferencias = scanner.next();
                    simularPaginacion(archivoReferencias, numMarcos);
                    break;

                case 3:
                    System.out.print("Ingrese el nombre de la imagen: ");
                    String archivoImg = scanner.next();
                    imagenModificada = new Imagen(archivoImg);

                    System.out.print("Ingrese el nombre del archivo de texto con el mensaje: ");
                    String archivoMensaje = scanner.next();

                    char[] mensaje = leerArchivoTexto(archivoMensaje);
                    imagenModificada.esconderMensaje(mensaje, mensaje.length);
                    imagenModificada.escribirImagen(archivoImg + "_modificado");

                    System.out.println("Mensaje escondido");
                    break;

                case 4:
                    System.out.print("Ingrese el nombre del archivo BMP con el mensaje escondido: ");
                    String archivo = scanner.next();
                    imagenModificada = new Imagen(archivo);

                    char[] mensajeOculto = imagenModificada.recuperarMensaje();
                    escribirArchivoTexto("Mensaje_oculto_" + archivo, mensajeOculto);

                    System.out.println("Mensaje_oculto_" + archivo + " se creó con éxito");
                    break;
            }
        }
    }

    // Método para generar referencias desde la imagen BMP
    public static void generarReferencias(Imagen imagen, int tamanoPagina) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("referencias.txt"))) {
            int filas = imagen.getAlto();
            int columnas = imagen.getAncho();
            int tamanoMensaje = imagen.leerLongitud();
            int tamanoImagen = filas * columnas * 3;

            // Calcular el número de referencias necesarias
            System.out.println("tamano del mensaje: " + tamanoMensaje);

            int num_referencias = 16 + 17 * tamanoMensaje;
            int paginasVirtuales = (tamanoImagen + tamanoMensaje + tamanoPagina - 1) / tamanoPagina;

            // Escribir datos generales
            bufferedWriter.write("TP=" + tamanoPagina + "\n");
            bufferedWriter.write("NF=" + filas + "\n");
            bufferedWriter.write("NC=" + columnas + "\n");
            bufferedWriter.write("NR=" + num_referencias + "\n");
            bufferedWriter.write("NP=" + paginasVirtuales + "\n");

            // Generar referencias para los primeros 16 bits (longitud del mensaje)
            int columna_matriz = 0;
            String[] colors = {"R", "G", "B"};
            int desplazamiento = 0;

            for (int cuenta = 0; cuenta < 16; cuenta++) {
                String letter_color = colors[cuenta % 3];
                if (cuenta % 3 == 0 && cuenta != 0) {
                    columna_matriz++;
                }
                bufferedWriter.write("Imagen[0][" + columna_matriz + "]." + letter_color + ",0," + desplazamiento + ",R\n");
                desplazamiento++;
            }

            // Generar referencias para el mensaje en la imagen
            int fila_matriz = 0;
            int numero_pagina = 0;
            int pos_mensaje = 0;
            int numero_pagina_mensaje = (3 * imagen.getAncho() * imagen.getAlto() + tamanoPagina - 1) / tamanoPagina;
            
            int cuenta_imagen = 16; // Continuar donde se dejó
            int letra_mensaje = 0;
            boolean sigue_mensaje = false;

            for (; cuenta_imagen < num_referencias;) {
                bufferedWriter.write("Mensaje[" + letra_mensaje + "]," + numero_pagina_mensaje + "," + pos_mensaje + ",W\n");
                cuenta_imagen++;

                for (int i = 0; i < 8; i++) {
                    String letter_color = colors[cuenta_imagen % 3];
                    if (cuenta_imagen % 3 == 0) {
                        columna_matriz++;
                        if (columna_matriz >= imagen.getAncho()) {
                            fila_matriz++;
                            columna_matriz = 0;
                        }
                    }
                    bufferedWriter.write("Imagen[" + fila_matriz + "][" + columna_matriz + "]." + letter_color + "," + numero_pagina + "," + desplazamiento + ",R\n");
                    desplazamiento++;
                    if (desplazamiento >= tamanoPagina) {
                        numero_pagina++;
                        desplazamiento = 0;
                    }
                    cuenta_imagen++;
                    sigue_mensaje = true;
                }
                pos_mensaje++;
                letra_mensaje++;
                if (pos_mensaje >= tamanoPagina) {
                    pos_mensaje = 0;
                    numero_pagina_mensaje++;
                }
            }

            System.out.println("Referencias generadas y guardadas en 'referencias.txt'.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
        
    
    public static void simularPaginacion(String archivoReferencias, int numMarcos) {
        PageTable pageTable = new PageTable(numMarcos);
        FaultsCounter faultsCounter = new FaultsCounter();
        
        // Variables para calcular tiempos
        long tiempoTotal = 0;
        long tiempoHits = 25;  // ns
        long tiempoMisses = 10_000_000;  // 10 ms en ns
    
        try {
            BufferedReader reader = new BufferedReader(new FileReader(archivoReferencias));
            String linea;
            while ((linea = reader.readLine()) != null) {
                // Procesar cada línea del archivo de referencias
                if (linea.startsWith("Imagen") || linea.startsWith("Mensaje")) {
                    String[] partes = linea.split(",");
                    int paginaVirtual = Integer.parseInt(partes[1]);
    
                    // Intentamos cargar la página y verificamos si es un hit o un fallo de página
                    boolean hit = pageTable.loadPage(paginaVirtual);
                    if (hit) {
                        faultsCounter.countHit();
                        tiempoTotal += tiempoHits;
                    } else {
                        faultsCounter.countFault();
                        tiempoTotal += tiempoMisses;
                    }
                }
            }
    
            reader.close();
            System.out.println("Simulación completada.");
            System.out.println("Total de fallas de página: " + faultsCounter.getFaults());
            System.out.println("Total de hits: " + faultsCounter.getHits());
            System.out.println("Tiempo total de acceso: " + tiempoTotal + " nanosegundos");
    
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    //Metodo para leer el archivo de texto
    public static char[] leerArchivoTexto(String rutaArchivo) {
        StringBuilder mensaje = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(rutaArchivo));
            String linea;
            while ((linea = reader.readLine()) != null) {
                mensaje.append(linea);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mensaje.toString().toCharArray(); // Convertir a arreglo de char[]
    }
    
    // Método para escribir el mensaje en un archivo de texto
    public static void escribirArchivoTexto(String rutaArchivo, char[] mensaje) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(rutaArchivo));
            writer.write(mensaje);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void recuperarMensajeReferencias(Imagen imagen, int tamanoPagina) {
        try {
            FileWriter writer = new FileWriter("referencias.txt");
            int filas = imagen.getAlto();
            int columnas = imagen.getAncho();
            int tamanoImagen = filas * columnas * 3;
            int tamanoMensaje = imagen.leerLongitud();
            
            // Calcular número de páginas necesarias
            int ref = 0;
            int paginasVirtuales = (((filas*columnas*3)+tamanoMensaje)/tamanoPagina);

            writer.write("TP=" + tamanoPagina + "\n");
            writer.write("NF=" + filas + "\n");
            writer.write("NC=" + columnas + "\n");
            writer.write("NR=" + ref + "\n");
            writer.write("NP=" + paginasVirtuales + "\n");

            
            System.out.println("Referencias generadas y guardadas en 'referencias.txt'.");
            int longitud = imagen.leerLongitud(); 
            char[] mensaje = new char[longitud];

            int bytesFila = imagen.getAncho() * 3;
            for (int posCaracter = 0; posCaracter < longitud; posCaracter++) {
                //mensaje[posCaracter] = 0;
                writer.write("Mensaje[" + String.valueOf(posCaracter)+"],"+((tamanoImagen + posCaracter)/tamanoPagina)+","+((tamanoImagen + posCaracter)%tamanoPagina)+","+"W"+ "\n");
                ref++;
                for (int i = 0; i < 8; i++) {
                    int numBytes = 16 + (posCaracter * 8) + i;
                    int fila = numBytes / bytesFila;
                    int col = (numBytes % bytesFila) / 3;
                    int color = (numBytes % bytesFila) % 3;
                    //mensaje[posCaracter] |= (char) ((imagen[fila][col][color] & 1) << i);
                    writer.write("Imagen[" + String.valueOf(posCaracter)+"],"+((tamanoImagen + posCaracter)/tamanoPagina)+","+((tamanoImagen + posCaracter)%tamanoPagina)+","+"R"+ "\n");
                    ref++;
                    writer.write("Mensaje[" + String.valueOf(posCaracter)+"],"+((tamanoImagen + posCaracter)/tamanoPagina)+","+((tamanoImagen + posCaracter)%tamanoPagina)+","+"W"+ "\n");
                    ref++;
                    }
                }
                
                writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}


