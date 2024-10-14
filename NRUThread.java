public class NRUThread extends Thread {
    private PageTable pageTable;

    public NRUThread(PageTable pageTable) {
        this.pageTable = pageTable;
    }

    public void run() {
        try {
            while (true) {
                synchronized(pageTable) {
                    pageTable.categorizePages(); // Clasifica las páginas para el algoritmo NRU
                }
                Thread.sleep(1); // Ejecutar el algoritmo NRU cada 1 ms
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}