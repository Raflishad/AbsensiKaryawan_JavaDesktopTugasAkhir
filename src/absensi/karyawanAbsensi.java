package absensi;

import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Panel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.Timer;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author rafis_g7mh0rp
 */
public class karyawanAbsensi extends javax.swing.JFrame {

    /**
     * Creates new form Admin
     */
    public karyawanAbsensi() {
        initComponents();
        setLocationRelativeTo(null);
        setTanggal();
        applyHoverEffect(panel1);
        String nip = Session.getNip();
    if (nip != null) {
        jTextField1.setText(nip);

    if (nip != null && !nip.trim().isEmpty()) {
        if (cekAbsensiHariIni(nip)) {
            String jamMasuk = getJamMasukHariIni(nip);
            if (jamMasuk != null) {
                jTextField2.setText(jamMasuk); // Tampilkan jam masuk
            }
            JOptionPane.showMessageDialog(null, "Anda sudah absen hari ini!", "Peringatan", JOptionPane.WARNING_MESSAGE);
        }
    } else {
        JOptionPane.showMessageDialog(null, "NIP tidak ditemukan!", "Peringatan", JOptionPane.WARNING_MESSAGE);
    }
    }
    }

    public static void applyHoverEffect(Panel panel) {
    Color hoverColor = new Color(0, 102, 102);
    Color hoverTextColor = Color.WHITE; // Warna teks saat hover

    for (Component comp : panel.getComponents()) {
        if (comp instanceof Button) {
            Button button = (Button) comp;
            Color defaultColor = button.getBackground();
            Color defaultTextColor = button.getForeground(); // Simpan warna teks awal

            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(hoverColor);
                    button.setForeground(hoverTextColor); // Ubah warna teks jadi putih
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBackground(defaultColor);
                    button.setForeground(defaultTextColor); // Kembalikan warna teks awal
                }
            });
        }
    }
}
    
      private void setTanggal() {
    Timer timer = new Timer(1000, e -> {
        // Format untuk tanggal
        SimpleDateFormat sdfTanggal = new SimpleDateFormat("dd MMMM yyyy");
        String tanggal = sdfTanggal.format(new Date());
        label2.setText(tanggal);

    });
    timer.start();
}
      
      public String getJamMasukHariIni(String nip) {
    String query = "SELECT jam_masuk FROM absensi a " +
                   "JOIN rel_absensi r ON a.id_absensi = r.id_absensi " +
                   "WHERE r.nip_pegawai = ? AND tanggal_absen = CURDATE() LIMIT 1";

    try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/absensi", "root", "");
         PreparedStatement stmt = conn.prepareStatement(query)) {
        
        stmt.setString(1, nip);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return rs.getString("jam_masuk");
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return null; // Tidak ada absen hari ini
}


      
    public boolean cekLogin(String nip, String password) {
    String sql = "SELECT * FROM pegawai WHERE nip_pegawai = ? AND password = ?";

    try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/absensi", "root", "");
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, nip);
        stmt.setString(2, password);
        ResultSet rs = stmt.executeQuery(); // Eksekusi setelah parameter diatur

        return rs.next(); // Jika ada hasil, berarti login valid

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
     public boolean cekAbsensiHariIni(String nip) {
        String sql = "SELECT * FROM absensi WHERE tanggal_absen = CURDATE() AND id_absensi IN (SELECT id_absensi FROM rel_absensi WHERE nip_pegawai = ?)";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/absensi", "root", "");
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nip);
            ResultSet rs = stmt.executeQuery();

            return rs.next(); // Jika ada hasil, berarti pegawai sudah absen hari ini

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
     
public boolean tambahAbsensi(String nip, String status) {
    if (cekAbsensiHariIni(nip)) {
        System.out.println("Pegawai sudah absen hari ini!");
        return false;
    }

    String statusMasuk = "H";
    String keterangan = "Hadir";
    String jamKeluar = null;

    if (status.equalsIgnoreCase("Izin")) {
        statusMasuk = "I";
        keterangan = "Izin";
    } else if (status.equalsIgnoreCase("Sakit")) {
        statusMasuk = "S";
        keterangan = "Sakit";
    } else if (status.equalsIgnoreCase("Masuk")) {
        jamKeluar = "18:00:00";
    }

    String terlambat = "N";

    String insertAbsensi = "INSERT INTO absensi (tanggal_absen, jam_masuk, jam_keluar, status_masuk, keterangan, terlambat) " +
                           "VALUES (CURDATE(), CURTIME(), ?, ?, ?, ?)";
    String getLastId = "SELECT LAST_INSERT_ID() AS id_absensi";
    String insertRelasi = "INSERT INTO rel_absensi (id_absensi, nip_pegawai) VALUES (?, ?)";

    try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/absensi", "root", "");
         Statement stmtPengaturan = conn.createStatement();
         PreparedStatement stmtAbsensi = conn.prepareStatement(insertAbsensi);
         PreparedStatement stmtLastId = conn.prepareStatement(getLastId);
         PreparedStatement stmtRelasi = conn.prepareStatement(insertRelasi)) {

        // 🔸 Ambil batas jam masuk dari database
        String batasJamMasuk = "08:00:00"; // default
        ResultSet rsPengaturan = stmtPengaturan.executeQuery("SELECT batas_jam_masuk FROM pengaturan LIMIT 1");
        if (rsPengaturan.next()) {
            batasJamMasuk = rsPengaturan.getString("batas_jam_masuk");
        }

        // 🔸 Cek keterlambatan
        String jamSekarang = new SimpleDateFormat("HH:mm:ss").format(new Date());
        if (jamSekarang.compareTo(batasJamMasuk) > 0) {
            terlambat = "Y";
            JOptionPane.showMessageDialog(null, "Anda terlambat masuk!", "Peringatan", JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "Anda masuk tepat waktu.", "Informasi", JOptionPane.INFORMATION_MESSAGE);
        }

        // 🔸 Insert data absensi
        if (jamKeluar != null) {
            stmtAbsensi.setString(1, jamKeluar);
        } else {
            stmtAbsensi.setNull(1, java.sql.Types.TIME);
        }
        stmtAbsensi.setString(2, statusMasuk);
        stmtAbsensi.setString(3, keterangan);
        stmtAbsensi.setString(4, terlambat);
        stmtAbsensi.executeUpdate();

        // 🔸 Ambil ID absensi terakhir
        ResultSet rs = stmtLastId.executeQuery();
        int idAbsensi = -1;
        if (rs.next()) {
            idAbsensi = rs.getInt("id_absensi");
        }

        if (idAbsensi == -1) {
            System.out.println("Gagal mendapatkan ID absensi!");
            return false;
        }

        // 🔸 Insert ke rel_absensi
        stmtRelasi.setInt(1, idAbsensi);
        stmtRelasi.setString(2, nip);
        stmtRelasi.executeUpdate();

        return true;

    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}




    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panel1 = new java.awt.Panel();
        label1 = new java.awt.Label();
        button1 = new java.awt.Button();
        button2 = new java.awt.Button();
        button3 = new java.awt.Button();
        button8 = new java.awt.Button();
        panel2 = new java.awt.Panel();
        jLabel1 = new javax.swing.JLabel();
        label2 = new java.awt.Label();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));

        panel1.setBackground(new java.awt.Color(0, 102, 102));

        label1.setAlignment(java.awt.Label.CENTER);
        label1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        label1.setForeground(new java.awt.Color(255, 255, 0));
        label1.setText("PT. SUMBER TEKNIK INDONESIA");

        button1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        button1.setLabel("HOME");
        button1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button1ActionPerformed(evt);
            }
        });

        button2.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        button2.setLabel("DATA PEGAWAI");
        button2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button2ActionPerformed(evt);
            }
        });

        button3.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        button3.setLabel("ABSENSI");
        button3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button3ActionPerformed(evt);
            }
        });

        button8.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        button8.setLabel("LOGOUT");
        button8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button8ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panel1Layout = new javax.swing.GroupLayout(panel1);
        panel1.setLayout(panel1Layout);
        panel1Layout.setHorizontalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel1Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(button8, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                    .addComponent(button1, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                    .addComponent(button2, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                    .addComponent(button3, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                    .addComponent(label1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(24, Short.MAX_VALUE))
        );
        panel1Layout.setVerticalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35)
                .addComponent(button1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addComponent(button2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addComponent(button3, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addComponent(button8, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(281, Short.MAX_VALUE))
        );

        panel2.setBackground(new java.awt.Color(255, 255, 255));
        panel2.setPreferredSize(new java.awt.Dimension(577, 590));

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Absensi Pegawai");
        jLabel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        label2.setAlignment(java.awt.Label.CENTER);
        label2.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        label2.setText("Tanggal");

        jTextField1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jTextField1.setEnabled(false);

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel2.setText("NIP               :");

        jTextField2.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jTextField2.setEnabled(false);

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel4.setText("Jam Masuk   :");

        jButton1.setBackground(new java.awt.Color(0, 51, 51));
        jButton1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Masuk");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panel2Layout = new javax.swing.GroupLayout(panel2);
        panel2.setLayout(panel2Layout);
        panel2Layout.setHorizontalGroup(
            panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 577, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panel2Layout.createSequentialGroup()
                        .addGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel4))
                        .addGap(23, 23, 23)
                        .addGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTextField1)
                            .addComponent(jTextField2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(132, 132, 132))
            .addComponent(label2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panel2Layout.setVerticalGroup(
            panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel2Layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(label2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 2, Short.MAX_VALUE)
                .addComponent(panel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void button1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button1ActionPerformed
        karyawan admin = new karyawan();
        admin.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_button1ActionPerformed

    private void button2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button2ActionPerformed
        karyawanDataPegawai adminDT = new karyawanDataPegawai();
        adminDT.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_button2ActionPerformed

    private void button3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button3ActionPerformed
        karyawanAbsensi adminA = new karyawanAbsensi();
        adminA.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_button3ActionPerformed

    private void button8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button8ActionPerformed
int response = JOptionPane.showConfirmDialog(this, 
        "Apakah Anda yakin ingin logout dari Pegawai?", 
        "Konfirmasi Logout", JOptionPane.YES_NO_OPTION);
    
    if (response == JOptionPane.YES_OPTION) {
        Session.clearSession();
        login login = new login();
        login.setVisible(true);
        this.dispose(); //Menutup form saat ini
    }        // TODO add your handling code here:
    }//GEN-LAST:event_button8ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        String nip = Session.getNip();
        jTextField1.setText(nip);

        // 🔸 Validasi: NIP tidak boleh kosong
        if (nip == null || nip.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Anda belum login!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (cekAbsensiHariIni(nip)) {
            JOptionPane.showMessageDialog(null, "Anda sudah absen hari ini!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (tambahAbsensi(nip, "Masuk")) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String waktuSekarang = sdf.format(new Date());
            jTextField2.setText(waktuSekarang);
            JOptionPane.showMessageDialog(null, "Absensi berhasil!", "Success", JOptionPane.INFORMATION_MESSAGE);

            Session.clearSession();
            login login = new login();
            login.setVisible(true);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(null, "Gagal mencatat absensi!", "Error", JOptionPane.ERROR_MESSAGE);
        }

    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(karyawanAbsensi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(karyawanAbsensi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(karyawanAbsensi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(karyawanAbsensi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new karyawanAbsensi().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private java.awt.Button button1;
    private java.awt.Button button2;
    private java.awt.Button button3;
    private java.awt.Button button8;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private java.awt.Label label1;
    private java.awt.Label label2;
    private java.awt.Panel panel1;
    private java.awt.Panel panel2;
    // End of variables declaration//GEN-END:variables
}
