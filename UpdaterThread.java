public class UpdaterThread extends Thread {
    private PageTable pageTable;

    public UpdaterThread(PageTable pageTable) {
        this.pageTable = pageTable;
    }

    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) { // Mantener el hilo corriendo
                synchronized (pageTable) {
                    // Aquí podemos actualizar el estado de la tabla de páginas según sea necesario.
                    pageTable.updateTableState();  // Nueva función para actualizar estados en la tabla de páginas
                }
                Thread.sleep(2); // Esperar 2 ms antes de volver a actualizar
            }
        } catch (InterruptedException e) {
            System.out.println("Thread de Updater interrumpido.");
        }
    }
}
