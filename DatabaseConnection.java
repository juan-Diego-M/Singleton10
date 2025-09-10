public class DatabaseConnection {
    // Capacidad máxima del "multiton" (pool)
    private static final int MAX_INSTANCES = 10;
    private static final DatabaseConnection[] POOL = new DatabaseConnection[MAX_INSTANCES];
    private static int nextIndex = 0; // para round-robin cuando el pool está lleno

    private String host;
    private String user;
    private String pass;
    private String name;
    private static int uniqueIdentificator = 0; // cuenta cuántas instancias se han creado

    private DatabaseConnection(String host, String user, String pass, String name) {
        this.host = host;
        this.user = user;
        this.pass = pass;
        this.name = name;
        uniqueIdentificator++;
    }

    // Devuelve una conexión existente con los mismos parámetros
    // o crea una nueva hasta un máximo de 10. Si está lleno, reutiliza por round-robin.
    public static synchronized DatabaseConnection getDatabaseConnection(String host, String user, String pass, String name) {
        // 1) Si ya existe una instancia con los mismos datos, regresála
        for (DatabaseConnection db : POOL) {
            if (db != null
                    && db.host.equals(host)
                    && db.user.equals(user)
                    && db.pass.equals(pass)
                    && db.name.equals(name)) {
                return db;
            }
        }

        // 2) Si hay espacio libre, crea una nueva
        for (int i = 0; i < MAX_INSTANCES; i++) {
            if (POOL[i] == null) {
                POOL[i] = new DatabaseConnection(host, user, pass, name);
                nextIndex = (i + 1) % MAX_INSTANCES; // siguiente posición sugerida
                return POOL[i];
            }
        }

        // 3) Si el pool está lleno, reutiliza (round-robin)
        DatabaseConnection reused = POOL[nextIndex];
        nextIndex = (nextIndex + 1) % MAX_INSTANCES;
        return reused;
    }

    public int getUniqueIdentificator() {
        return uniqueIdentificator;
    }

    // Getters opcionales para host, user, pass, name
    public String getHost() { return host; }
    public String getUser() { return user; }
    public String getPass() { return pass; }
    public String getName() { return name; }
}

class Client {
    public static void main(String[] args) {
        // Crea hasta 12 solicitudes para ver cómo se llena y luego se reutiliza
        for (int i = 1; i <= 12; i++) {
            DatabaseConnection db = DatabaseConnection.getDatabaseConnection(
                    "localhost", "user" + ((i - 1) % 3), "1234", "db" + ((i - 1) % 4));
            System.out.println("Petición " + i + " -> instancia: " + System.identityHashCode(db)
                    + ", host=" + db.getHost() + ", user=" + db.getUser() + ", name=" + db.getName());
        }

        // Comprobación de cuántas instancias se crearon realmente (máx 10)
        DatabaseConnection any = DatabaseConnection.getDatabaseConnection("localhost", "user0", "1234", "db0");
        System.out.println("Instancias creadas (máx 10): " + any.getUniqueIdentificator());
    }
}

