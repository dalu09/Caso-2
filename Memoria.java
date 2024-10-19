import java.util.HashMap;
import java.util.Map;


public class Memoria {
    private Map<Integer, int[]> memoria; // Simula los marcos en la memoria física

    public Memoria(int marcosDePagina) {
        memoria = new HashMap<>();
        for (int i = 0; i < marcosDePagina; i++) {
            int[] marcoVacio = {-1, 0, 0}; // {página, bit de referencia, bit de modificado}
            memoria.put(i, marcoVacio);
        }
    }

    public synchronized int realizarSwap(int paginaVirtual) {
        int marcoReemplazo = getMarcoDisponibleSwap(); // Selecciona el marco a reemplazar
        memoria.put(marcoReemplazo, new int[] {paginaVirtual, 1, 0}); // {página, bit referencia, modificado}
        return marcoReemplazo;
    }
    private synchronized int getMarcoDisponibleSwap() {
        for (Integer marco : memoria.keySet()) {
            if (memoria.get(marco)[1] == 0) { // Si no ha sido referenciada
                return marco;
            }
        }
        return 0; // Si todas han sido referenciadas, reemplazar la primera
    }
}
