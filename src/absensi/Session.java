/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package absensi;

/**
 *
 * @author rafli
 */
public class Session {
    private static String nip;
    private static String nama;
    private static String role;

    public static void setUser(String nipUser, String namaUser, String roleUser) {
        nip = nipUser;
        nama = namaUser;
        role = roleUser;
    }

    public static String getNip() {
        return nip;
    }

    public static String getNama() {
        return nama;
    }

    public static String getRole() {
        return role;
    }

    public static void clearSession() {
        nip = null;
        nama = null;
        role = null;
    }
}

