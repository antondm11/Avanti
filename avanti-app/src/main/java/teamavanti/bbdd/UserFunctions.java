package teamavanti.bbdd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import teamavanti.model.User;

/**
 * Operaciones CRUD sobre la tabla 'usuario'.
 */
public class UserFunctions {

    // ─── Registrar usuario nuevo ────────────────────────────────────────────────

    /**
     * Inserta un nuevo usuario en la base de datos.
     *
     * @param user Usuario a registrar (nombre, email, contraseña, rol)
     */
    public void registerUser(User user) throws SQLException {
        Connection conn = DatabaseManager.getInstance().connectToDb();
        if (conn == null)
            return;

        String sql = "INSERT INTO usuario (nombre, email, contrasena, rol) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getNombre());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getContrasena());
            ps.setString(4, user.getRol() != null ? user.getRol() : "cliente");
            ps.executeUpdate();
            System.out.println("Usuario registrado: " + user.getEmail());
        } finally {
            DatabaseManager.getInstance().closeConnection(conn);
        }
    }

    // ─── Autenticar usuario (login) ─────────────────────────────────────────────

    /**
     * Busca un usuario por email y contraseña.
     *
     * @return El objeto User si las credenciales son correctas, null si no.
     */
    public User loginUser(String email, String contrasena) throws SQLException {
        Connection conn = DatabaseManager.getInstance().connectToDb();
        if (conn == null)
            return null;

        String sql = "SELECT * FROM usuario WHERE email = ? AND contrasena = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, contrasena);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setNombre(rs.getString("nombre"));
                u.setEmail(rs.getString("email"));
                u.setContrasena(rs.getString("contrasena"));
                u.setRol(rs.getString("rol"));
                return u;
            }
            return null;
        } finally {
            DatabaseManager.getInstance().closeConnection(conn);
        }
    }

    // ─── Insertar usuario ───────────────────────────────────────────────────────

    /** Alias de registerUser para mantener compatibilidad con código anterior. */
    public void insertUser(User u) throws SQLException {
        registerUser(u);
    }

    // ─── Eliminar usuario ───────────────────────────────────────────────────────

    public void deleteUser(int id) throws SQLException {
        Connection conn = DatabaseManager.getInstance().connectToDb();
        if (conn == null)
            return;

        String sql = "DELETE FROM usuario WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Usuario eliminado (id=" + id + ")");
        } finally {
            DatabaseManager.getInstance().closeConnection(conn);
        }
    }

    // ─── Obtener todos los usuarios ─────────────────────────────────────────────

    public List<User> getUsers() throws SQLException {
        Connection conn = DatabaseManager.getInstance().connectToDb();
        List<User> list = new ArrayList<>();
        if (conn == null)
            return list;

        String sql = "SELECT * FROM usuario";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setNombre(rs.getString("nombre"));
                u.setEmail(rs.getString("email"));
                u.setRol(rs.getString("rol"));
                list.add(u);
            }
        } finally {
            DatabaseManager.getInstance().closeConnection(conn);
        }
        return list;
    }

    // ─── Actualizar rol de usuario ──────────────────────────────────────────────

    public void updateUserRole(int id, String newRole) throws SQLException {
        Connection conn = DatabaseManager.getInstance().connectToDb();
        if (conn == null)
            return;

        String sql = "UPDATE usuario SET rol = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newRole);
            ps.setInt(2, id);
            ps.executeUpdate();
            System.out.println("Rol de usuario actualizado (id=" + id + ", nuevo_rol=" + newRole + ")");
        } finally {
            DatabaseManager.getInstance().closeConnection(conn);
        }
    }
}
