import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Imagen {
    private byte[] header = new byte[54]; 
    private byte[][][] imagen;
    private int alto, ancho;

    public Imagen(String input) {
        try {
            FileInputStream fis = new FileInputStream(input);
            fis.read(header);

            // Extraer el ancho y alto de la imagen
            ancho = ((header[21] & 0xFF) << 24) | ((header[20] & 0xFF) << 16) | 
                    ((header[19] & 0xFF) << 8) | (header[18] & 0xFF);
            alto = ((header[25] & 0xFF) << 24) | ((header[24] & 0xFF) << 16) | 
                   ((header[23] & 0xFF) << 8) | (header[22] & 0xFF);

            imagen = new byte[alto][ancho][3]; 

            // Leer los píxeles de la imagen
            byte[] pixel = new byte[3];
            for (int i = 0; i < alto; i++) {
                for (int j = 0; j < ancho; j++) {
                    fis.read(pixel);
                    imagen[i][j][0] = pixel[2]; 
                    imagen[i][j][1] = pixel[1]; 
                    imagen[i][j][2] = pixel[0]; 
                }
            }

            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void esconderMensaje(char[] mensaje, int longitud) {
        int contador = 0;
        escribirBits(contador, longitud, 16); 

        contador = 2;
        for (int i = 0; i < longitud; i++) {
            byte elByte = (byte) mensaje[i];
            escribirBits(contador, elByte, 8);
            contador++;
        }
    }

    public char[] recuperarMensaje() {
        int longitud = leerLongitud(); 
        char[] mensaje = new char[longitud];

        int bytesFila = ancho * 3;
        for (int posCaracter = 0; posCaracter < longitud; posCaracter++) {
            mensaje[posCaracter] = 0;
            for (int i = 0; i < 8; i++) {
                int numBytes = 16 + (posCaracter * 8) + i;
                int fila = numBytes / bytesFila;
                int col = (numBytes % bytesFila) / 3;
                int color = (numBytes % bytesFila) % 3;
                mensaje[posCaracter] |= (char) ((imagen[fila][col][color] & 1) << i);
            }
        }
        return mensaje;
    }

    private void escribirBits(int contador, int valor, int numbits) {
        int bytesPorFila = ancho * 3;
        for (int i = 0; i < numbits; i++) {
            int fila = (8 * contador + i) / bytesPorFila;
            int col = ((8 * contador + i) % bytesPorFila) / 3;
            int color = ((8 * contador + i) % bytesPorFila) % 3;
            int mascara = (valor >> i) & 1;
            imagen[fila][col][color] = (byte) ((imagen[fila][col][color] & 0xFE) | mascara);
        }
    }

    public int leerLongitud() {
        int longitud = 0;
        for (int i = 0; i < 16; i++) {
            int col = (i % (ancho * 3)) / 3;
            longitud |= (imagen[0][col][(i % 3)] & 1) << i;
        }
        return longitud;
    }

    public int getAncho() {
        return this.ancho;
    }

    public int getAlto() {
        return this.alto;
    }

    public void escribirImagen(String output) {
        try {
            FileOutputStream fos = new FileOutputStream(output);
            fos.write(header);

            byte[] pixel = new byte[3];
            for (int i = 0; i < alto; i++) {
                for (int j = 0; j < ancho; j++) {
                    pixel[0] = imagen[i][j][2]; 
                    pixel[1] = imagen[i][j][1]; 
                    pixel[2] = imagen[i][j][0]; 
                    fos.write(pixel);
                }
            }

            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
